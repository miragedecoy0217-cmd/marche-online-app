package com.marche.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marche.dao.CartDAO;
import com.marche.dao.OrderDAO;
import com.marche.model.CartItem;
import com.marche.model.Order;
import com.marche.model.User;
import com.marche.util.DBConnection;

@WebServlet("/order/*")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CartDAO cartDAO = new CartDAO();
    private OrderDAO orderDAO = new OrderDAO();

 // No.13 & No.15 GET /order および GET /order/complete
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // ----------------------------------------------------
            // 1. 注文入力・確認画面の表示処理
            // ----------------------------------------------------
            HttpSession session = request.getSession();
            User loginUser = (User) session.getAttribute("loginUser");

            if (loginUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // カート内商品の取得
            List<CartItem> cartItems = cartDAO.findByUserID(loginUser.getUser_id());
            if (cartItems.isEmpty()) {
                // 空カートの場合は /cart へリダイレクト
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            // CSRFトークンの生成・セッション保存
            String csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
            request.setAttribute("csrfToken", csrfToken);

            // 合計金額の計算
            int totalAmount = 0;
            for (CartItem item : cartItems) {
                totalAmount += item.getSubtotal();
            }

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalAmount", totalAmount);

            request.getRequestDispatcher("/WEB-INF/views/order.jsp").forward(request, response);

        } else if ("/complete".equals(pathInfo)) {
            // ----------------------------------------------------
            // 2. 注文完了画面の表示処理 (PRGパターンの遷移先)
            // ----------------------------------------------------
            HttpSession session = request.getSession();
            User loginUser = (User) session.getAttribute("loginUser");

            if (loginUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // セッションから直近の注文IDを取得
            Integer orderId = (Integer) session.getAttribute("lastOrderId");
            request.setAttribute("orderId", orderId);

            // ※一度表示したら二重保持を防ぐためにセッションからクリアする場合
            session.removeAttribute("lastOrderId");

            // 注文完了JSPへフォワード
            request.getRequestDispatcher("/WEB-INF/views/order_complete.jsp").forward(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // No.14 POST /order/confirm (注文確定・トランザクション処理)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if ("/confirm".equals(pathInfo)) {
            HttpSession session = request.getSession();
            User loginUser = (User) session.getAttribute("loginUser");

            if (loginUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // Step 1: CSRFトークン検証
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getParameter("csrf_token");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "CSRFトークンが一致しません。");
                return;
            }

            String addressStr = request.getParameter("address_id"); 
            String paymentMethod = request.getParameter("payment_method");

            // Step 2: バリデーション
            if (addressStr == null || addressStr.trim().isEmpty() ||
                paymentMethod == null || (!paymentMethod.equals("credit") && !paymentMethod.equals("transfer"))) {
                request.setAttribute("errorMessage", "配送先または支払い方法が不正です。");
                doGet(request, response);
                return;
            }

            int addressId;
            try {
                addressId = Integer.parseInt(addressStr);
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "配送先IDの形式が不正です。");
                doGet(request, response);
                return;
            }

            // Step 3: カート内容を取得
            List<CartItem> cartItems = cartDAO.findByUserID(loginUser.getUser_id());
            if (cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            Connection con = null;
            try {
                con = DBConnection.getConnection();
                con.setAutoCommit(false); // Step 4: トランザクション開始

                BigDecimal totalAmount = BigDecimal.ZERO;
                for (CartItem item : cartItems) {
                    totalAmount = totalAmount.add(BigDecimal.valueOf(item.getSubtotal()));
                }

                // Step 5 & 6: 在庫ロック (FOR UPDATE) & 在庫確認
                String lockSql = "SELECT stock FROM products WHERE product_id = ? FOR UPDATE";
                try (PreparedStatement psLock = con.prepareStatement(lockSql)) {
                    for (CartItem item : cartItems) {
                        psLock.setInt(1, item.getProductId());
                        try (ResultSet rs = psLock.executeQuery()) {
                            if (rs.next()) {
                                int stock = rs.getInt("stock");
                                if (stock < item.getQuantity()) {
                                    con.rollback();
                                    request.setAttribute("errorMessage", "「" + item.getProductName() + "」の在庫が不足しています。");
                                    doGet(request, response);
                                    return;
                                }
                            } else {
                                con.rollback();
                                request.setAttribute("errorMessage", "商品が存在しません。");
                                doGet(request, response);
                                return;
                            }
                        }
                    }
                }

                // Step 7: 在庫減算
                String updateStockSql = "UPDATE products SET stock = stock - ? WHERE product_id = ?";
                try (PreparedStatement psStock = con.prepareStatement(updateStockSql)) {
                    for (CartItem item : cartItems) {
                        psStock.setInt(1, item.getQuantity());
                        psStock.setInt(2, item.getProductId());
                        psStock.addBatch();
                    }
                    psStock.executeBatch();
                }

                // Step 8: 注文登録 (既存の OrderDAO.insert を活用)
                Order order = new Order();
                order.setUserId(loginUser.getUser_id());
                order.setAddressId(addressId);
                order.setTotalAmount(totalAmount);
                order.setStatus("pending");
                order.setPaymentMethod(paymentMethod);

                int orderId = orderDAO.insert(con, order);

                // Step 9: 注文詳細登録 (order_details)
                String insertDetailSql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psDetail = con.prepareStatement(insertDetailSql)) {
                    for (CartItem item : cartItems) {
                        psDetail.setInt(1, orderId);
                        psDetail.setInt(2, item.getProductId());
                        psDetail.setInt(3, item.getQuantity());
                        psDetail.setBigDecimal(4, BigDecimal.valueOf(item.getPrice()));
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }

                // Step 10: カート削除
                String deleteCartSql = "DELETE FROM carts WHERE user_id = ?";
                try (PreparedStatement psCart = con.prepareStatement(deleteCartSql)) {
                    psCart.setInt(1, loginUser.getUser_id());
                    psCart.executeUpdate();
                }

                // Step 11: コミット
                con.commit();

                // Step 12: lastOrderId をセッションに保存（直リンク・二重送信防止）
                session.setAttribute("lastOrderId", orderId);

                // Step 13: 完了画面へリダイレクト (PRGパターン)
                response.sendRedirect(request.getContextPath() + "/order/complete");

            } catch (Exception e) {
                if (con != null) {
                    try {
                        con.rollback(); // 異常時ロールバック
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            } finally {
                if (con != null) {
                    try {
                        con.setAutoCommit(true);
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}