package com.marche.dao;

import java.sql.SQLException;
import java.util.List;

import com.marche.model.Order;
import com.marche.model.OrderDetail;

/**
 * OrderDAO の動作確認用（参照のみ。DBは変更しない）。
 * 右クリック → 実行 → Javaアプリケーション。
 * 初期データ投入済みなら、user_id=1 の注文が表示される。
 */
public class OrderDAOTest {

    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();

        try {
            List<Order> orders = dao.findByUserIdWithDetails(1);

            System.out.println("=== user_id=1 の注文履歴: " + orders.size() + "件 ===");
            for (Order o : orders) {
                System.out.println("注文#" + o.getOrderId() + " " + o.getOrderAt()
                        + " [" + o.getStatus() + "] 合計=" + o.getTotalAmount());
                for (OrderDetail d : o.getDetails()) {
                    System.out.println("    " + d.getProductName() + " x" + d.getQuantity()
                            + " @" + d.getUnitPrice() + " = " + d.getSubtotal());
                }
            }

            System.out.println("=== 直近3件（全ユーザー） ===");
            for (Order o : dao.findRecent(3)) {
                System.out.println("注文#" + o.getOrderId() + " user=" + o.getUserId()
                        + " [" + o.getStatus() + "]");
            }
        } catch (SQLException e) {
            System.out.println("=== 失敗 ===");
            e.printStackTrace();
        }
    }
}