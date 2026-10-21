package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.SalesData;
import com.marche.util.DBConnection;

/**
 * 売上集計用のデータアクセスクラス（参照のみ）。
 * 集計の対象は、キャンセル(cancelled)・返金(refunded)以外の注文。
  * ビュー(v_sales_by_product)は使わず、orders との JOIN で集計する。
 */
public class SalesDAO {

    /** 日次売上（日付の古い順）。label は "yyyy-MM-dd"。 */
    public List<SalesData> findDaily() throws SQLException {
        String sql = "SELECT CAST(order_at AS DATE) AS label, "
                + "COUNT(*) AS order_count, SUM(total_amount) AS total_sales "
                + "FROM orders "
                + "WHERE status NOT IN ('cancelled', 'refunded') "
                + "GROUP BY CAST(order_at AS DATE) "
                + "ORDER BY label";
        return query(sql, false);
    }

    /** 月次売上（全体、月の古い順）。label は "yyyy-MM"。 */
    public List<SalesData> findMonthly() throws SQLException {
        String sql = "SELECT TO_CHAR(order_at, 'YYYY-MM') AS label, "
                + "COUNT(*) AS order_count, SUM(total_amount) AS total_sales "
                + "FROM orders "
                + "WHERE status NOT IN ('cancelled', 'refunded') "
                + "GROUP BY TO_CHAR(order_at, 'YYYY-MM') "
                + "ORDER BY label";
        return query(sql, false);
    }

    /** カテゴリ別売上（売上の多い順）。label はカテゴリ名。 */
    public List<SalesData> findByCategory() throws SQLException {
        String sql = "SELECT c.category_name AS label, "
                + "COUNT(DISTINCT d.order_id) AS order_count, "
                + "SUM(d.quantity) AS total_qty, "
                + "SUM(d.quantity * d.unit_price) AS total_sales "
                + "FROM order_details d "
                + "JOIN orders o ON d.order_id = o.order_id "
                + "AND o.status NOT IN ('cancelled', 'refunded') "
                + "JOIN products p ON d.product_id = p.product_id "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "GROUP BY c.category_id, c.category_name "
                + "ORDER BY total_sales DESC";
        return query(sql, true);
    }

    /** 商品別売上（売上の多い順、売上のない商品は含まない）。label は商品名。 */
    public List<SalesData> findByProduct() throws SQLException {
        String sql = "SELECT p.product_name AS label, "
                + "COUNT(DISTINCT d.order_id) AS order_count, "
                + "SUM(d.quantity) AS total_qty, "
                + "SUM(d.quantity * d.unit_price) AS total_sales "
                + "FROM order_details d "
                + "JOIN orders o ON d.order_id = o.order_id "
                + "AND o.status NOT IN ('cancelled', 'refunded') "
                + "JOIN products p ON d.product_id = p.product_id "
                + "GROUP BY p.product_id, p.product_name "
                + "ORDER BY total_sales DESC";
        return query(sql, true);
    }

    /**
     * 全期間の売上合計と注文件数（ダッシュボードのサマリカード用）。
     * 注文が1件もなくても、売上0・件数0の SalesData を返す。
     */
    public SalesData findTotal() throws SQLException {
        String sql = "SELECT COUNT(*) AS order_count, "
                + "COALESCE(SUM(total_amount), 0) AS total_sales "
                + "FROM orders "
                + "WHERE status NOT IN ('cancelled', 'refunded')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            SalesData s = new SalesData();
            s.setLabel("合計");
            if (rs.next()) {
                s.setOrderCount(rs.getLong("order_count"));
                s.setTotalSales(rs.getBigDecimal("total_sales"));
            }
            return s;
        }
    }

    // ------------------------------------------------------------

    /**
     * SQLを実行して SalesData のリストにする共通処理。
     * withQty が true のとき、total_qty 列も読む。
     */
    private List<SalesData> query(String sql, boolean withQty) throws SQLException {
        List<SalesData> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SalesData s = new SalesData();
                s.setLabel(rs.getString("label"));
                s.setOrderCount(rs.getLong("order_count"));
                s.setTotalSales(rs.getBigDecimal("total_sales"));
                if (withQty) {
                    s.setTotalQty(rs.getLong("total_qty"));
                }
                list.add(s);
            }
        }
        return list;
    }
}