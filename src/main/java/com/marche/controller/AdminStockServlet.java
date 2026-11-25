package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marche.dao.ProductDAO;
import com.marche.dao.StockDAO;
import com.marche.model.AdminUser;
import com.marche.model.Product;

/**
 * 管理：在庫管理（設計書 No.29 / No.30）。
 *
 *   GET  /admin/stock  … 商品ごとの在庫一覧（page）＋ 在庫の履歴（直近）
 *   POST /admin/stock  … 在庫の調整（id, stock）。stock_history に履歴を残す
 *
 * ・権限（superadmin / inventory）は AdminFilter でチェックする。
 * ・在庫の更新は StockDAO.adjustTo を使う（履歴も同じトランザクションで記録される）。
 *   ProductDAO の更新で stock_count を直接変えると、履歴に残らないので使わない。
 */
@WebServlet("/admin/stock")
public class AdminStockServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;
	/** この数以下の在庫は、画面で目立たせる */
	private static final int LOW_STOCK = 10;
	/** 入力できる在庫数の上限（入力ミスで、極端に大きな数字が入らないように） */
	private static final int MAX_STOCK = 100000;
	/** 履歴に表示する件数 */
	private static final int HISTORY_LIMIT = 20;

	private static final String VIEW_LIST = "/WEB-INF/views/admin/stock.jsp";
	private static final String SESSION_MESSAGE = "adminMessage";

	private final ProductDAO productDAO = new ProductDAO();
	private final StockDAO stockDAO = new StockDAO();

	// ------------------------------------------------------------
	// GET：在庫一覧
	// ------------------------------------------------------------
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int page = parsePage(request.getParameter("page"));
			int total = productDAO.countForAdmin();
			int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
			page = Math.min(page, totalPages);

			request.setAttribute("products", productDAO.findAllForAdmin(page, PAGE_SIZE));
			request.setAttribute("page", page);
			request.setAttribute("totalPages", totalPages);
			request.setAttribute("total", total);
			request.setAttribute("lowStock", LOW_STOCK);
			request.setAttribute("maxStock", MAX_STOCK);
			request.setAttribute("history", stockDAO.findRecentHistory(HISTORY_LIMIT));

			// 調整のあとに出すメッセージ（1回だけ表示する）
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
	// POST：在庫の調整
	// ------------------------------------------------------------
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		try {
			int id = Integer.parseInt(request.getParameter("id"));
			String stockParam = request.getParameter("stock");
			int newStock = Integer.parseInt(stockParam == null ? "" : stockParam.trim());

			AdminUser admin = (AdminUser) request.getSession().getAttribute("loginAdmin");

			Product product = productDAO.findById(id);
			if (product == null || product.isDeleted()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			String message;
			if (newStock < 0 || newStock > MAX_STOCK) {
				message = "在庫数は 0 〜 " + MAX_STOCK + " の整数で入力してください。";
			} else {
				int before = stockDAO.getStock(id);
				if (!stockDAO.adjustTo(id, newStock, admin.getAdminId())) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				if (before == newStock) {
					message = "「" + product.getProductName() + "」の在庫は、すでに " + newStock + " 個です（変更なし）。";
				} else {
					message = "「" + product.getProductName() + "」の在庫を " + before + " 個から "
							+ newStock + " 個に調整しました。";
				}
			}

			request.getSession().setAttribute(SESSION_MESSAGE, message);
			response.sendRedirect(request.getContextPath() + "/admin/stock" + backQuery(request));

		} catch (NumberFormatException e) {
			// 数字でない id や stock
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
	 * 調整のあと、元の一覧（同じページ）へ戻るための、"?～" を返す。
	 * フォームの back には、"page=2" のような文字だけを許可する。
	 */
	private String backQuery(HttpServletRequest request) {
		String back = request.getParameter("back");
		if (back == null || back.isEmpty() || !back.matches("[A-Za-z0-9_=&-]*")) {
			return "";
		}
		return "?" + back;
	}
}