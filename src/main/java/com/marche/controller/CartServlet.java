package com.marche.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marche.dao.CartDAO;
import com.marche.model.CartItem;
import com.marche.model.User;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private CartDAO cartDAO = new CartDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/login");
            return;
        }

        User loginUser =
                (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/login");
            return;
        }

        List<CartItem> cartItems =
                cartDAO.findByUserID(
                        loginUser.getUser_id());

        request.setAttribute(
                "cartItems",
                cartItems);

        request.getRequestDispatcher(
                "/WEB-INF/views/cart.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        User loginUser =
                (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/login");
            return;
        }

        int productId =
                Integer.parseInt(
                        request.getParameter("productId"));

        int quantity =
                Integer.parseInt(
                        request.getParameter("quantity"));

        cartDAO.addOrUpdate(
                loginUser.getUser_id(),
                productId,
                quantity);

        response.sendRedirect(
                request.getContextPath()
                + "/cart");
    }
}