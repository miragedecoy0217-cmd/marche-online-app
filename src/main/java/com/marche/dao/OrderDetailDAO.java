package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.OrderDetail;
import com.marche.util.DBConnection;

/**
 * order_details テーブルに対するデータアクセスクラス。
 *
 * 【トランザクションの注意】
 * insert / insertAll は Connection を引数で受け取る。
 * commit / rollback / close は呼び出し側（OrderServlet）が行い、DAO内では閉じない。
 */
public class OrderDetailDAO {

    /**
     * 注文明細を1件登録する（トランザクション内で使う）。
     * 生成された detail_id を返す。
     */
    public int insert(Connection con, OrderDetail d) throws SQLException {
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?) RETURNING detail_id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getOrderId());
            ps.setInt(2, d.getProductId());
            ps.setInt(3, d.getQuantity());
            ps.setBigDecimal(4, d.getUnitPrice());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * 注文明細をまとめて登録する（トランザクション内で使う）。
     * 各明細の orderId に、引数の orderId をセットしてから登録する。
     */
    public void insertAll(Connection con, int orderId, List<OrderDetail> details) throws SQLException {
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (OrderDetail d : details) {
                d.setOrderId(orderId);
                ps.setInt(1, orderId);
                ps.setInt(2, d.getProductId());
                ps.setInt(3, d.getQuantity());
                ps.setBigDecimal(4, d.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * 注文IDを条件に、明細を商品名・画像つきで取得する。
     */
    public List<OrderDetail> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT d.detail_id, d.order_id, d.product_id, d.quantity, d.unit_price, "
                + "p.product_name, p.image_path "
                + "FROM order_details d "
                + "JOIN products p ON d.product_id = p.product_id "
                + "WHERE d.order_id = ? "
                + "ORDER BY d.detail_id";

        List<OrderDetail> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * ResultSet の1行を OrderDetail に変換する（OrderDAO からも使う）。
     */
    static OrderDetail mapRow(ResultSet rs) throws SQLException {
        OrderDetail d = new OrderDetail();
        d.setDetailId(rs.getInt("detail_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setProductId(rs.getInt("product_id"));
        d.setQuantity(rs.getInt("quantity"));
        d.setUnitPrice(rs.getBigDecimal("unit_price"));
        d.setProductName(rs.getString("product_name"));
        d.setImagePath(rs.getString("image_path"));
        return d;
    }
}