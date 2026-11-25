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
import com.marche.dao.ReviewDAO;
import com.marche.model.Product;
import com.marche.model.Review;
import com.marche.model.User;

/**
 * レビュー投稿を受け取るServlet（POSTのみ）。
 * ・ログイン必須（未ログインは /login へ）
 * ・order_id はフォームから受け取らず、ReviewDAO.findReviewableOrderId の値を使う
 * ・rating は 1〜5 を先に確認する
 * ・処理後は商品詳細へリダイレクトする（PRGパターン）
 */
@WebServlet("/reviews")
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** コメントの最大文字数 */
    private static final int MAX_COMMENT_LENGTH = 500;

    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /** GETで直接開かれたら、商品一覧へ戻す */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/products");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. ログイン確認
        HttpSession session = request.getSession(false);
        User loginUser = (session == null) ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 2. productId の確認
        int productId;
        try {
            productId = Integer.parseInt(request.getParameter("productId"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Product product = productDAO.findActiveById(productId);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String backUrl = request.getContextPath() + "/products/" + productId;

            // 3. rating の確認（1〜5）
            int rating;
            try {
                rating = Integer.parseInt(request.getParameter("rating"));
            } catch (NumberFormatException e) {
                rating = 0;
            }
            if (rating < 1 || rating > 5) {
                session.setAttribute("reviewError", "評価は1〜5から選んでください。");
                response.sendRedirect(backUrl);
                return;
            }

            // 4. コメントの確認（空なら null にする）
            String comment = request.getParameter("comment");
            if (comment != null) {
                comment = comment.trim();
                if (comment.isEmpty()) {
                    comment = null;
                }
            }
            if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
                session.setAttribute("reviewError",
                        "コメントは" + MAX_COMMENT_LENGTH + "文字以内で入力してください。");
                response.sendRedirect(backUrl);
                return;
            }

            // 5. 投稿できる注文があるか確認（配達完了の購入で、まだレビューしていない）
            Integer orderId = reviewDAO.findReviewableOrderId(loginUser.getUser_id(), productId);
            if (orderId == null) {
                session.setAttribute("reviewError",
                        "この商品は、購入して配達が完了したあとにレビューできます。");
                response.sendRedirect(backUrl);
                return;
            }

            // 6. 登録（status は承認待ち pending になる）
            Review review = new Review();
            review.setProductId(productId);
            review.setOrderId(orderId);
            review.setRating(rating);
            review.setComment(comment);
            reviewDAO.insert(review);

            session.setAttribute("reviewMessage",
                    "レビューを投稿しました。承認されると公開されます。");
            response.sendRedirect(backUrl);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "データベースエラーが発生しました。");
        }
    }
}