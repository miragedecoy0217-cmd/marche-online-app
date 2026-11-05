package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.CategoryDAO;
import com.marche.dao.ProductDAO;
import com.marche.model.Category;
import com.marche.model.Product;

/**
 * 商品一覧（設計書 No.6）。GET /products
 * パラメータ: keyword, category_id, page
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** 1ページに表示する件数 */
    private static final int PAGE_SIZE = 12;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String keyword = request.getParameter("keyword");
        Integer categoryId = parseIntOrNull(request.getParameter("category_id"));
        int page = parseIntOrDefault(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        try {
            int total = productDAO.countSearch(keyword, categoryId);
            int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            if (page > totalPages) {
                page = totalPages;
            }

            List<Product> products = productDAO.search(keyword, categoryId, page, PAGE_SIZE);
            List<Category> categories = categoryDAO.findAll();

            request.setAttribute("products", products);
            request.setAttribute("categories", categories);
            request.setAttribute("total", total);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("keyword", keyword);
            request.setAttribute("categoryId", categoryId);

            request.getRequestDispatcher("/WEB-INF/views/product_list.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "データベースエラーが発生しました。");
        }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntOrDefault(String s, int defaultValue) {
        Integer v = parseIntOrNull(s);
        return (v == null) ? defaultValue : v;
    }
}