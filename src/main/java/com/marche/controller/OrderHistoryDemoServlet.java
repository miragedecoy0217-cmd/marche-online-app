package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.OrderDAO;
import com.marche.model.Order;

/**
 * 【学習・動作確認用】OrderDAO を Servlet から使う最小の例。
 * URL: /demo/orders?userId=1
 * 流れ: ブラウザ → この Servlet → OrderDAO → DB → JSP(orders.jsp) → ブラウザ
 */
@WebServlet("/demo/orders")
public class OrderHistoryDemoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = 1;
        String param = request.getParameter("userId");
        if (param != null && !param.isEmpty()) {
            try {
                userId = Integer.parseInt(param);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "userId は数字で指定してください");
                return;
            }
        }

        try {
            OrderDAO dao = new OrderDAO();
            List<Order> orders = dao.findByUserIdWithDetails(userId);

            request.setAttribute("userId", userId);
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/demo/orders.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("注文履歴の取得に失敗しました", e);
        }
    }
}