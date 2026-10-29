package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.marche.util.DBConnection;

/**
 * productsテーブルのstock_countカラムと、stock_history（在庫履歴）に対する
 * データアクセスクラス。
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
     * ※ stock_history には記録されない。新しい処理では changeStock / adjustTo を使うこと。
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

    // =====================================================================
    // 追加：在庫の増減＋履歴記録（stock_history）
    // =====================================================================

    /**
     * 在庫を増減し、同じ接続で stock_history に履歴を記録する。
     * トランザクション（commit / rollback）は呼び出し側が管理する。
     * 注文確定のように、他の処理と一緒に1つのトランザクションにしたいときに使う。
     *
     * @param changeQty 増減数（出庫は負、入庫・キャンセル戻しは正）
     * @param reason    purchase / order / cancel / adjustment / loss のいずれか
     * @param adminId   管理者の操作なら admin_id、客の操作なら null
     * @param orderId   注文に関する変更なら order_id、それ以外は null
     * @return 更新できたら true。商品が存在しない、または在庫が0未満になる場合は false
     */
    public boolean changeStock(Connection conn, int productId, int changeQty,
            String reason, Integer adminId, Integer orderId) throws SQLException {

        // 読んでから書くのではなく、1文で増減する（同時注文でも在庫が狂わない）
        String updateSql = "UPDATE products SET stock_count = stock_count + ?, updated_at = now() "
                + "WHERE product_id = ? AND stock_count + ? >= 0";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, changeQty);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, changeQty);

            if (pstmt.executeUpdate() == 0) {
                return false;
            }
        }

        // reason は ENUM 型なので ?::stock_change_reason でキャストする
        String insertSql = "INSERT INTO stock_history "
                + "(product_id, admin_id, order_id, change_qty, reason, created_at) "
                + "VALUES (?, ?, ?, ?, ?::stock_change_reason, now())";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, productId);

            if (adminId != null) {
                pstmt.setInt(2, adminId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            if (orderId != null) {
                pstmt.setInt(3, orderId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setInt(4, changeQty);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
        }

        return true;
    }

    /**
     * 管理画面の在庫調整用：在庫数を指定の値にそろえ、差分を adjustment として記録する。
     * 自分で接続を開き、1つのトランザクションで処理する。
     *
     * @return 更新できたら true。商品が存在しない、または newStockCount が負なら false
     */
    public boolean adjustTo(int productId, int newStockCount, int adminId) throws SQLException {
        if (newStockCount < 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int current = 0;

                // 行をロックして現在の在庫数を読む（調整中に他の更新が割り込まないように）
                String selectSql = "SELECT stock_count FROM products WHERE product_id = ? FOR UPDATE";
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setInt(1, productId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        current = rs.getInt(1);
                    }
                }

                int diff = newStockCount - current;
                if (diff != 0) {
                    boolean ok = changeStock(conn, productId, diff, "adjustment", adminId, null);
                    if (!ok) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}