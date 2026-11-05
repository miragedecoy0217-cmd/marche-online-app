package com.marche.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.CartDAO;
import com.marche.model.CartItem;
import com.marche.model.User;

@WebServlet("/cart/*")
public class CartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private CartDAO cartDAO = new CartDAO();

	@Override
	protected void doGet(javax.servlet.http.HttpServletRequest request, HttpServletRespondse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();

		//GET /cart(カート一覧画面表示)
		if (pathInfo == null || pathInfo.equals("/")) {
			javax.servlet.http.HttpSession session = request.getSession();
			User loginUser = (User) session.getAttribute("loginUser");

			if (loginUser == null) {
				response.sendRedirect(request.getContextPath() + "/login");
				return;
			}

			int userId = loginUser.getUser_id();

			//CSRF トークンの生成・保持
			String csrfToken = (String) session.getAttribute("csrfToken");
			if (csrfToken == null) {
				csrfToken = UUID.randomUUID().toString();
				session.setAttribute("csrfToken", csrfToken);
			}
			request.setAttribute("csrfToken", csrfToken);

			//カート内商品一覧の取得
			List<CartItem> cartItems = cartDAO.findByUserID(userId);

			//合計金額の計算
			int totalAmount = 0;
			for (CartItem item : cartItems) {
				totalAmount += item.getSubtotal();
			}

			request.setAttribute("cartItems", cartItems);
			request.setAttribute("totalAmount", totalAmount);

			request.getRequestDispatcher("/WEB-INF/views/cart.isp").forward(request, response);
		} else {
			response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPost(javax.servlet.http.HttpServletRequest request,
			javax.servlet.http.HttpServletResponse response)
			throws ServletException,IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		javax.servlet.http.HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		//CSRF トークン検証
		String sessionToken = (String) session.getAttribute("csrfToken");
		String requestToken = request.getParameter("csrf token");
		if (sessionToken == null || !sessionToken.equals(requestToken)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSEFトークンが一致しません。");
			return;
		}

		int userId = loginUser.getUser_id();

		try {
			if (pathInfo.equals("/add")) {
				//POST /cart/add(商品追加)
				int productId = Integer.parseInt(request.getParameter("product_id"));
				int quantity = Integer.parseInt(request.getParameter("quantity"));

				if (quantity < 1) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "数量は1以上を指定してください。");

					return;
				}
				//TODO:必要に応じて在庫チェックや商品存在確認をここに挟むことができます
				cartDAO.addOrUpdate(userId, productId, quantity);
				response.sendRedirect(request.getContextPath() + "/cart");

			} else if (pathInfo.startsWith("/update/")) {
				//POST/cart/update/{product_id} (数量変更)
				int productId = Integer.parseInt(pathInfo.substring("/update/".length()));
				int quantity = Integer.parseInt(request.getParameter("quantity"));

				if (quantity < 1) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "数量は1以上を指定してください。");
					return;
				}

				cartDAO.updateQuantity(userId, productId, quantity);
				response.sendRedirect(request.getContextPath() + "/cart");

			} else if (pathInfo.startsWith("/remove/")) {
				// POST /cart/remove/{product_id} (商品削除)
				int productId = Integer.parseInt(pathInfo.substring("/remove/".length()));
				cartDAO.delete(userId, productId);
				response.sendRedirect(request.getContexPath() + "/cart");

			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "パラメータの形式が不正です。");
		}
	}
}