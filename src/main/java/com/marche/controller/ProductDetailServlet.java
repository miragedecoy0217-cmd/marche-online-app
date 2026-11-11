package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.ProductDAO;
import com.marche.dao.ReviewDAO;
import com.marche.model.Product;

@WebServlet("/products/*")
public class ProductDetailServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// URLの /products/{id} または /products/{id}/stock を読み取る
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.length() <= 1) {
			response.sendRedirect(request.getContextPath() + "/products");
			return;
		}

		String[] parts = pathInfo.substring(1).split("/");
		int productId;
		try {
			productId = Integer.parseInt(parts[0]);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		boolean stockOnly = parts.length > 1 && "stock".equals(parts[1]);

		try {
			Product product = new ProductDAO().findActiveById(productId);
			if (product == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			// 在庫だけをJSONで返す（/products/{id}/stock）
			if (stockOnly) {
				response.setContentType("application/json; charset=UTF-8");
				response.getWriter().write("{\"productId\":" + product.getProductId()
						+ ",\"stock\":" + product.getStockCount() + "}");
				return;
			}

			request.setAttribute("product", product);
			// 承認済みレビューを取得して画面へ渡す
			request.setAttribute("reviews", new ReviewDAO().findApprovedByProduct(productId));
			request.getRequestDispatcher("/WEB-INF/views/product_detail.jsp").forward(request, response);

		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}
}