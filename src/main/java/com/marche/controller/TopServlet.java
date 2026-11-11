package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.CategoryDAO;
import com.marche.dao.ProductDAO;

// "" = コンテキストルート（/marche-online/）。"/" にすると画像などが見えなくなるので注意
@WebServlet(urlPatterns = {"", "/index"})
public class TopServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int NEW_ARRIVALS = 8;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			request.setAttribute("newProducts", new ProductDAO().findNewArrivals(NEW_ARRIVALS));
			request.setAttribute("categories", new CategoryDAO().findAll());
			request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}
}