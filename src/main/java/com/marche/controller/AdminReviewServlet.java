package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marche.dao.ReviewDAO;
import com.marche.model.AdminUser;
import com.marche.model.Review;

/**
 * 管理：レビュー管理（設計書 No.31 / No.32 / No.33）。
 *
 *   GET  /admin/reviews  … レビュー一覧（status で切り替え。既定は承認待ち pending。page）
 *   POST /admin/reviews  … action=approve : 承認（公開）。承認した管理者と日時を記録
 *                          action=reject  : 却下
 *                          action=hide    : 非公開
 *                          action=pending : 承認待ちに戻す
 *                          action=delete  : 削除（論理削除）
 *
 * ・権限（superadmin / review）は AdminFilter でチェックする。
 * ・承認は approve() を使う（updateStatus に "approved" を渡すと、承認者が記録されない）。
 */
@WebServlet("/admin/reviews")
public class AdminReviewServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;
	private static final String DEFAULT_STATUS = "pending";
	private static final String SESSION_MESSAGE = "adminMessage";

	/** 一覧で切り替えられるステータス（review_status ENUM の全部） */
	private static final List<String> STATUS_LIST = Arrays.asList(
			"pending", "approved", "rejected", "hidden");
	/** 画面から行える操作 */
	private static final List<String> ACTION_LIST = Arrays.asList(
			"approve", "reject", "hide", "pending", "delete");

	private static final String VIEW_LIST = "/WEB-INF/views/admin/reviews.jsp";

	private final ReviewDAO reviewDAO = new ReviewDAO();

	// ------------------------------------------------------------
	// GET：レビュー一覧
	// ------------------------------------------------------------
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String status = request.getParameter("status");
			if (status == null || status.trim().isEmpty()) {
				status = DEFAULT_STATUS;
			}
			if (!STATUS_LIST.contains(status)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			// DAOは全件を返すので、ここでページ分けする（レビューの件数は多くない前提）
			List<Review> all = reviewDAO.findByStatus(status);
			int total = all.size();
			int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
			int page = Math.min(parsePage(request.getParameter("page")), totalPages);
			int from = (page - 1) * PAGE_SIZE;
			int to = Math.min(from + PAGE_SIZE, total);

			request.setAttribute("reviews", all.subList(from, to));
			request.setAttribute("status", status);
			request.setAttribute("page", page);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("total", total);

			// 操作のあとに出すメッセージ（1回だけ表示する）
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object message = session.getAttribute(SESSION_MESSAGE);
				if (message != null) {
					request.setAttribute("message", message);
					session.removeAttribute(SESSION_MESSAGE);
				}
			}

			request.getRequestDispatcher(VIEW_LIST).forward(request, response);

		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	// ------------------------------------------------------------
	// POST：承認 / 却下 / 非公開 / 承認待ちに戻す / 削除
	// ------------------------------------------------------------
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		try {
			int id = Integer.parseInt(request.getParameter("id"));
			String action = request.getParameter("action");
			if (action == null || !ACTION_LIST.contains(action)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			AdminUser admin = (AdminUser) request.getSession().getAttribute("loginAdmin");

			boolean ok;
			String done;
			switch (action) {
			case "approve":
				ok = reviewDAO.approve(id, admin.getAdminId());
				done = "承認（公開）しました";
				break;
			case "reject":
				ok = reviewDAO.updateStatus(id, "rejected");
				done = "却下しました";
				break;
			case "hide":
				ok = reviewDAO.updateStatus(id, "hidden");
				done = "非公開にしました";
				break;
			case "pending":
				ok = reviewDAO.updateStatus(id, "pending");
				done = "承認待ちに戻しました";
				break;
			default: // delete
				ok = reviewDAO.delete(id);
				done = "削除しました";
				break;
			}

			String message = ok
					? "レビュー#" + id + " を" + done + "。"
					: "レビュー#" + id + " は、見つからないか、すでに削除されています。";

			request.getSession().setAttribute(SESSION_MESSAGE, message);
			response.sendRedirect(request.getContextPath() + "/admin/reviews" + backQuery(request));

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	// ------------------------------------------------------------
	// 内部メソッド
	// ------------------------------------------------------------

	private int parsePage(String s) {
		try {
			return Math.max(1, Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/**
	 * 操作のあと、元の一覧（同じステータス・同じページ）へ戻るための、"?～" を返す。
	 * フォームの back には、"status=pending&page=1" のような文字だけを許可する。
	 */
	private String backQuery(HttpServletRequest request) {
		String back = request.getParameter("back");
		if (back == null || back.isEmpty() || !back.matches("[A-Za-z0-9_=&-]*")) {
			return "";
		}
		return "?" + back;
	}
}