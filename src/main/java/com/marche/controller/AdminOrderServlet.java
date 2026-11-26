package com.marche.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marche.dao.OrderDAO;
import com.marche.dao.OrderDetailDAO;
import com.marche.dao.StockDAO;
import com.marche.model.AdminUser;
import com.marche.model.Order;
import com.marche.model.OrderDetail;
import com.marche.util.DBConnection;

/**
 * 管理：注文管理（設計書 No.25 / No.26）。
 *
 *   GET  /admin/orders  … 注文一覧（status, date_from, date_to, page で絞り込み）
 *   POST /admin/orders  … action=status  : ステータス更新（pending / confirmed / shipped / delivered）
 *                         action=cancel  : キャンセル（注文を取り消し、在庫も戻す）
 *
 * ・権限（superadmin / order）は AdminFilter でチェックする。
 * ・キャンセルは、注文の更新と在庫の戻しを、1つのトランザクションで行う。
 */
@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

	private static final String VIEW_LIST = "/WEB-INF/views/admin/orders.jsp";
	private static final String SESSION_MESSAGE = "adminMessage";

	/** 一覧の絞り込みに使えるステータス（order_status ENUM の全部） */
	private static final List<String> ALL_STATUS = Arrays.asList(
			"pending", "confirmed", "shipped", "delivered", "cancelled", "refunded");
	/** 画面から変更できるステータス（キャンセルは、別ボタンで在庫も戻す） */
	private static final List<String> CHANGEABLE_STATUS = Arrays.asList(
			"pending", "confirmed", "shipped", "delivered");
	/** キャンセルできる注文（発送済み以降は、キャンセルできない） */
	private static final List<String> CANCELABLE_STATUS = Arrays.asList("pending", "confirmed");

	private final OrderDAO orderDAO = new OrderDAO();
	private final OrderDetailDAO orderDetailDAO = new OrderDetailDAO();
	private final StockDAO stockDAO = new StockDAO();

	// ------------------------------------------------------------
	// GET：注文一覧
	// ------------------------------------------------------------
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String status = emptyToNull(request.getParameter("status"));
			if (status != null && !ALL_STATUS.contains(status)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			LocalDate dateFrom = parseDate(request.getParameter("date_from"));
			LocalDate dateTo = parseDate(request.getParameter("date_to"));
			int page = parsePage(request.getParameter("page"));

			int total = orderDAO.count(status, dateFrom, dateTo);
			int totalPages = Math.max(1, (int) Math.ceil(total / (double) OrderDAO.PAGE_SIZE));
			page = Math.min(page, totalPages);

			request.setAttribute("orders", orderDAO.search(status, dateFrom, dateTo, page));
			request.setAttribute("page", page);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("total", total);

			// 画面の入力欄に、今の絞り込み条件を戻す
			request.setAttribute("status", status == null ? "" : status);
			request.setAttribute("dateFrom", dateFrom == null ? "" : dateFrom.toString());
			request.setAttribute("dateTo", dateTo == null ? "" : dateTo.toString());
			// ページ移動のリンクに付ける、絞り込み条件（例: "status=pending&date_from=2026-06-01&"）
			request.setAttribute("queryBase", buildQuery(status, dateFrom, dateTo));

			// 更新・キャンセルのあとに出すメッセージ（1回だけ表示する）
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object message = session.getAttribute(SESSION_MESSAGE);
				if (message != null) {
					request.setAttribute("message", message);
					session.removeAttribute(SESSION_MESSAGE);
				}
			}

			request.getRequestDispatcher(VIEW_LIST).forward(request, response);

		} catch (DateTimeParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	// ------------------------------------------------------------
	// POST：ステータス更新 / キャンセル
	// ------------------------------------------------------------
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");

		try {
			int id = Integer.parseInt(request.getParameter("id"));
			AdminUser admin = (AdminUser) request.getSession().getAttribute("loginAdmin");

			Order order = orderDAO.findById(id);
			if (order == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			String message;
			if ("status".equals(action)) {
				message = changeStatus(order, request.getParameter("status"));
			} else if ("cancel".equals(action)) {
				message = cancelOrder(order, admin, request.getParameter("reason"));
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			request.getSession().setAttribute(SESSION_MESSAGE, message);
			response.sendRedirect(request.getContextPath() + "/admin/orders" + backQuery(request));

		} catch (IllegalArgumentException e) {
			// 数字でない id（NumberFormatException も含む）、不正なステータス
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	/**
	 * ステータスを更新する。画面に出すメッセージを返す。
	 * キャンセル・返金済みの注文は変更できない。
	 */
	private String changeStatus(Order order, String newStatus) throws SQLException {
		if (newStatus == null || !CHANGEABLE_STATUS.contains(newStatus)) {
			throw new IllegalArgumentException("不正なステータスです: " + newStatus);
		}
		if ("cancelled".equals(order.getStatus()) || "refunded".equals(order.getStatus())) {
			return "注文#" + order.getOrderId() + " は、" + label(order.getStatus())
					+ "のため、ステータスを変更できません。";
		}

		orderDAO.updateStatus(order.getOrderId(), newStatus);
		return "注文#" + order.getOrderId() + " のステータスを「" + label(newStatus) + "」に変更しました。";
	}

	/**
	 * 注文をキャンセルし、注文の商品の在庫を戻す（1つのトランザクション）。
	 * 画面に出すメッセージを返す。失敗したときは、すべて取り消す（ROLLBACK）。
	 */
	private String cancelOrder(Order order, AdminUser admin, String reasonParam) throws SQLException {
		if (!CANCELABLE_STATUS.contains(order.getStatus())) {
			return "注文#" + order.getOrderId() + " は「" + label(order.getStatus())
					+ "」のため、キャンセルできません。";
		}

		String reason = (reasonParam == null || reasonParam.trim().isEmpty())
				? "管理者によるキャンセル" : reasonParam.trim();
		if (reason.length() > 200) {
			reason = reason.substring(0, 200);
		}

		// 明細は、変更されない（読むだけ）ので、トランザクションの前に取っておく
		List<OrderDetail> details = orderDetailDAO.findByOrderId(order.getOrderId());

		try (Connection con = DBConnection.getConnection()) {
			con.setAutoCommit(false);

			try {
				// すでにキャンセル済みなら false（同時に2人が押しても、在庫が二重に戻らない）
				if (!orderDAO.cancel(con, order.getOrderId(), reason)) {
					con.rollback();
					return "注文#" + order.getOrderId() + " は、すでにキャンセルされています。";
				}

				// 注文した数だけ、在庫を戻す（履歴は reason = cancel で記録）
				for (OrderDetail d : details) {
					boolean ok = stockDAO.changeStock(con, d.getProductId(), d.getQuantity(),
							"cancel", admin.getAdminId(), order.getOrderId());
					if (!ok) {
						throw new SQLException("在庫を戻せませんでした。商品ID=" + d.getProductId());
					}
				}

				con.commit();

			} catch (SQLException | RuntimeException e) {
				con.rollback();
				throw e;
			}
		}
		return "注文#" + order.getOrderId() + " をキャンセルし、在庫を戻しました。";
	}

	// ------------------------------------------------------------
	// 内部メソッド
	// ------------------------------------------------------------

	private String emptyToNull(String s) {
		return (s == null || s.trim().isEmpty()) ? null : s.trim();
	}

	/** yyyy-MM-dd の文字列を日付にする。空なら null。形式が違えば DateTimeParseException。 */
	private LocalDate parseDate(String s) {
		String v = emptyToNull(s);
		return (v == null) ? null : LocalDate.parse(v);
	}

	private int parsePage(String s) {
		try {
			return Math.max(1, Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/** ページ移動のリンクに付ける、絞り込み条件。値は検証済みなので、そのまま連結できる。 */
	private String buildQuery(String status, LocalDate dateFrom, LocalDate dateTo) {
		StringBuilder sb = new StringBuilder();
		if (status != null) {
			sb.append("status=").append(status).append('&');
		}
		if (dateFrom != null) {
			sb.append("date_from=").append(dateFrom).append('&');
		}
		if (dateTo != null) {
			sb.append("date_to=").append(dateTo).append('&');
		}
		return sb.toString();
	}

	/**
	 * 更新のあと、元の一覧（同じ絞り込み・同じページ）へ戻るための、"?～" を返す。
	 * フォームの back には、"status=pending&page=2" のような文字だけを許可する。
	 */
	private String backQuery(HttpServletRequest request) {
		String back = request.getParameter("back");
		if (back == null || back.isEmpty() || !back.matches("[A-Za-z0-9_=&-]*")) {
			return "";
		}
		return "?" + back;
	}

	/** ステータスの日本語表示（メッセージ用） */
	private String label(String status) {
		switch (status) {
		case "pending":
			return "注文受付";
		case "confirmed":
			return "確認済み";
		case "shipped":
			return "発送済み";
		case "delivered":
			return "配達完了";
		case "cancelled":
			return "キャンセル";
		case "refunded":
			return "返金済み";
		default:
			return status;
		}
	}
}