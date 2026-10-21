package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.marche.model.Order;
import com.marche.model.OrderDetail;
import com.marche.util.DBConnection;

/**
 * orders テーブルに対するデータアクセスクラス。
 *
 * 【トランザクションの注意】
 * 「Connection con」を受け取るメソッド（insert / updateStatus(con,...) / cancel）は、
 * 呼び出し側（OrderServlet・AdminOrderServlet）が commit / rollback / close を行う。
 * それ以外の参照系メソッドは、DAO内で接続を取得して閉じる。
 */
public class OrderDAO {

    /** order_status ENUM に存在する値（不正な値でSQLエラーになるのを防ぐ） */
    private static final List<String> VALID_STATUS = Arrays.asList(
            "pending", "confirmed", "shipped", "delivered", "cancelled", "refunded");

    // 注文＋明細を一括取得するSELECT句（JOIN）
    private static final String SELECT_WITH_DETAILS =
            "SELECT o.order_id, o.user_id, o.address_id, o.total_amount, o.status, "
            + "o.payment_method, o.cancel_reason, o.cancelled_at, o.order_at, "
            + "d.detail_id, d.product_id, d.quantity, d.unit_price, "
            + "p.product_name, p.image_path "
            + "FROM orders o "
            + "LEFT JOIN order_details d ON o.order_id = d.order_id "
            + "LEFT JOIN products p ON d.product_id = p.product_id ";

    // ------------------------------------------------------------
    // 登録・更新（トランザクション内で使う）
    // ------------------------------------------------------------

    /**
     * 注文を登録し、生成された order_id を返す。
     * order.getStatus() が null の場合は 'pending' で登録する。
     * order_at は DB の既定値（now()）が入る。
     */
    public int insert(Connection con, Order order) throws SQLException {
        String status = (order.getStatus() == null) ? "pending" : order.getStatus();
        checkStatus(status);

        String sql = "INSERT INTO orders (user_id, address_id, total_amount, status, payment_method) "
                + "VALUES (?, ?, ?, CAST(? AS order_status), ?) RETURNING order_id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, order.getUserId());
            ps.setInt(2, order.getAddressId());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, status);
            ps.setString(5, order.getPaymentMethod());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * 注文ステータスを更新する（トランザクション内で使う版）。
     * 更新できたら true、該当の注文がなければ false。
     */
    public boolean updateStatus(Connection con, int orderId, String status) throws SQLException {
        checkStatus(status);

        String sql = "UPDATE orders SET status = CAST(? AS order_status) WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 注文ステータスを更新する（単独で使う版。管理画面のステータス変更用）。
     */
    public boolean updateStatus(int orderId, String status) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return updateStatus(con, orderId, status);
        }
    }

    /**
     * 注文をキャンセル状態にする（理由と日時も記録。トランザクション内で使う）。
     * ※在庫の戻し（products.stock_count / stock_history）は呼び出し側で同じ con を使って行うこと。
     */
    public boolean cancel(Connection con, int orderId, String reason) throws SQLException {
        String sql = "UPDATE orders SET status = CAST('cancelled' AS order_status), "
                + "cancel_reason = ?, cancelled_at = now() WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------
    // 参照系（注文のみ）
    // ------------------------------------------------------------

    /**
     * 注文を1件取得する（明細なし）。見つからなければ null。
     */
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT order_id, user_id, address_id, total_amount, status, "
                + "payment_method, cancel_reason, cancelled_at, order_at "
                + "FROM orders WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
                return null;
            }
        }
    }

    /**
     * 全注文を新しい順に取得する（管理画面用。明細なし）。
     */
    public List<Order> findAll() throws SQLException {
        String sql = "SELECT order_id, user_id, address_id, total_amount, status, "
                + "payment_method, cancel_reason, cancelled_at, order_at "
                + "FROM orders ORDER BY order_at DESC, order_id DESC";

        List<Order> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        }
        return list;
    }

    /**
     * 直近の注文を件数指定で取得する（管理ダッシュボード用。明細なし）。
     */
    public List<Order> findRecent(int limit) throws SQLException {
        String sql = "SELECT order_id, user_id, address_id, total_amount, status, "
                + "payment_method, cancel_reason, cancelled_at, order_at "
                + "FROM orders ORDER BY order_at DESC, order_id DESC LIMIT ?";

        List<Order> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        }
        return list;
    }

    /**
     * ステータスで絞り込んで取得する（管理画面用。明細なし）。
     */
    public List<Order> findByStatus(String status) throws SQLException {
        checkStatus(status);

        String sql = "SELECT order_id, user_id, address_id, total_amount, status, "
                + "payment_method, cancel_reason, cancelled_at, order_at "
                + "FROM orders WHERE status = CAST(? AS order_status) "
                + "ORDER BY order_at DESC, order_id DESC";

        List<Order> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        }
        return list;
    }

    // ------------------------------------------------------------
    // 参照系（注文＋明細をJOINで一括取得）
    // ------------------------------------------------------------

    /**
     * ユーザーの注文履歴を、明細つきで新しい順に取得する（マイページ用）。
     */
    public List<Order> findByUserIdWithDetails(int userId) throws SQLException {
        String sql = SELECT_WITH_DETAILS
                + "WHERE o.user_id = ? "
                + "ORDER BY o.order_at DESC, o.order_id DESC, d.detail_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return groupByOrder(rs);
            }
        }
    }

    /**
     * 注文を1件、明細つきで取得する（注文詳細画面用）。見つからなければ null。
     */
    public Order findByIdWithDetails(int orderId) throws SQLException {
        String sql = SELECT_WITH_DETAILS
                + "WHERE o.order_id = ? "
                + "ORDER BY d.detail_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = groupByOrder(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    // ------------------------------------------------------------
    // 内部メソッド
    // ------------------------------------------------------------

    /**
     * JOINの結果（1明細=1行）を、注文ごとにまとめる。
     * order_id が同じ行は、同じ Order の details に追加する。
     */
    private List<Order> groupByOrder(ResultSet rs) throws SQLException {
        Map<Integer, Order> map = new LinkedHashMap<>();

        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            Order order = map.get(orderId);
            if (order == null) {
                order = mapOrder(rs);
                map.put(orderId, order);
            }

            // LEFT JOIN のため、明細が無い注文は detail_id が NULL になる
            rs.getInt("detail_id");
            if (!rs.wasNull()) {
                OrderDetail d = OrderDetailDAO.mapRow(rs);
                d.setOrderId(orderId);
                order.getDetails().add(d);
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * ResultSet の1行を Order に変換する（明細は含まない）。
     */
    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setAddressId(rs.getInt("address_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setCancelReason(rs.getString("cancel_reason"));

        Timestamp cancelledAt = rs.getTimestamp("cancelled_at");
        if (cancelledAt != null) {
            o.setCancelledAt(cancelledAt.toLocalDateTime());
        }
        Timestamp orderAt = rs.getTimestamp("order_at");
        if (orderAt != null) {
            o.setOrderAt(orderAt.toLocalDateTime());
        }
        return o;
    }

    private void checkStatus(String status) {
        if (status == null || !VALID_STATUS.contains(status)) {
            throw new IllegalArgumentException("不正な注文ステータスです: " + status);
        }
    }
}