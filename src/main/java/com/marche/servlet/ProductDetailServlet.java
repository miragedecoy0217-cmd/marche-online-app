package com.marche.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.ProductDAO;
import com.marche.model.Product;

@WebServlet("/product")
public class ProductDetailServlet extends HttpServlet {
    private final ProductDAO productDao = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	Integer id = toIntOrNull(req.getParameter("id"));
        if (id == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
        try {
            Product p = productDao.findActiveById(id);   // 削除済みは出さない
            if (p == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            req.setAttribute("product", p);
        } catch (SQLException e) {
            throw new ServletException("商品詳細の取得に失敗しました", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/product_detail.jsp").forward(req, resp);
    }
    private static Integer toIntOrNull(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

