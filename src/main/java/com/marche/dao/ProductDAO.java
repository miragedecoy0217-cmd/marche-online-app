package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.Product;
import com.marche.util.DBConnection;

/**
 * productsテーブルに対するデータアクセスクラス。
 * すべてのSQL実行にPreparedStatementを使用する（SQLインジェクション対策）。
 */
public class ProductDAO {

    /**
     * 論理削除されていない商品を全件取得する。
     */
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT product_id, category_id, product_name, description, price, "
                + "stock_count, image_path, created_at, updated_at, is_deleted "
                + "FROM products WHERE is_deleted = false ORDER BY product_id";

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * product_idを条件に1件取得する。見つからない場合はnullを返す。
     */
    public Product findById(int productId) throws SQLException {
        String sql = "SELECT product_id, category_id, product_name, description, price, "
                + "stock_count, image_path, created_at, updated_at, is_deleted "
                + "FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * category_idを条件に商品を絞り込む（論理削除されたものは除く）。
     */
    public List<Product> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT product_id, category_id, product_name, description, price, "
                + "stock_count, image_path, created_at, updated_at, is_deleted "
                + "FROM products WHERE category_id = ? AND is_deleted = false "
                + "ORDER BY product_id";

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * product_name / descriptionに対してキーワードの部分一致検索を行う。
     */
    public List<Product> searchByKeyword(String keyword) throws SQLException {
        String sql = "SELECT product_id, category_id, product_name, description, price, "
                + "stock_count, image_path, created_at, updated_at, is_deleted "
                + "FROM products "
                + "WHERE is_deleted = false "
                + "AND (product_name LIKE ? OR description LIKE ?) "
                + "ORDER BY product_id";

        List<Product> list = new ArrayList<>();
        String pattern = "%" + keyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * ResultSetの1行をProductオブジェクトに変換する。
     */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockCount(rs.getInt("stock_count"));
        p.setImagePath(rs.getString("image_path"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            p.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        p.setDeleted(rs.getBoolean("is_deleted"));
        return p;
    }
}
