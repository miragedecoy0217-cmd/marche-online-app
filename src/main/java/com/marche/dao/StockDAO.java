package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.marche.util.DBConnection;

/**
 * productsテーブルのstock_countカラムに対するデータアクセスクラス。
 */
public class StockDAO {

    /**
     * 指定した商品の在庫数を取得する。
     * 商品が存在しない場合は-1を返す。
     */
    public int getStock(int productId) throws SQLException {
        String sql = "SELECT stock_count FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_count");
                }
                return -1;
            }
        }
    }

    /**
     * 指定した商品の在庫数を新しい値に更新する（updated_atも同時更新）。
     * 更新できた場合はtrue、対象の商品が存在しなかった場合はfalseを返す。
     */
    public boolean updateStock(int productId, int newStockCount) throws SQLException {
        String sql = "UPDATE products SET stock_count = ?, updated_at = now() "
                + "WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newStockCount);
            pstmt.setInt(2, productId);

            int updatedRows = pstmt.executeUpdate();
            return updatedRows > 0;
        }
    }
}
