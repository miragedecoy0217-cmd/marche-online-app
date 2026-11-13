package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import com.marche.util.DBConnection;

/**
 * productsテーブルのstock_countカラムと、stock_history（在庫履歴）に対する
 * データアクセスクラス。
 *
 * 【トランザクションの注意】
 * 「Connection conn」を受け取るメソッド（getStock / getStockForUpdate / changeStock / adjustTo）は、
 * 呼び出し側が commit / rollback / close を行う。DAO内では閉じない。
 * Connection を受け取らない版は、DAO内で接続を取得して閉じる（その場で確定する）。
 */
public class StockDAO {

    /** stock_change_reason ENUM に存在する値（不正な値でSQLエラーになるのを防ぐ） */
    private static final List<String> VALID_REASON = Arrays.asList(
            "purchase", "order", "cancel", "adjustment", "loss");

    /**
     * 指定した商品の在庫数を取得する。
     * 商品が存在しない場合は-1を返す。
     */
    public int getStock(int productId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return getStock(conn, productId);
        }
    }

    /**
     * 指定した商品の在庫数を取得する（トランザクション内で使う版）。
     * 同じ接続で更新した直後の値も読める。商品が存在しない場合は-1を返す。
     */
    public int getStock(Connection conn, int productId) throws SQLException {
        String sql = "SELECT stock_count FROM products WHERE product_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
     * 在庫数を取得しながら、その商品の行をロックする（SELECT ... FOR UPDATE）。
     * 注文確定（No.14）で「在庫ロック → 在庫確認 → 在庫減算」の順に処理するときに使う。
     * ロックは、呼び出し側が commit / rollback するまで続く。
     * 商品が存在しない場合は-1を返す。
     */
    public int getStockForUpdate(Connection conn, int productId) throws SQLException {
        String sql = "SELECT stock_count FROM products WHERE product_id = ? FOR UPDATE";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
     * @throws IllegalArgumentException reason が上の5つ以外のとき
     */
    public boolean changeStock(Connection conn, int productId, int changeQty,
            String reason, Integer adminId, Integer orderId) throws SQLException {

        checkReason(reason);

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
     * 在庫数を指定の値にそろえ、差分を adjustment として記録する（トランザクション内で使う版）。
     * 行をロックして現在の在庫数を読む。commit / rollback は呼び出し側が行う。
     *
     * @return 更新できたら true（すでに同じ値なら何もせず true）。
     *         商品が存在しない、または newStockCount が負なら false
     */
    public boolean adjustTo(Connection conn, int productId, int newStockCount, int adminId)
            throws SQLException {
        if (newStockCount < 0) {
            return false;
        }

        // 行をロックして現在の在庫数を読む（調整中に他の更新が割り込まないように）
        int current = getStockForUpdate(conn, productId);
        if (current < 0) {
            return false;
        }

        int diff = newStockCount - current;
        if (diff == 0) {
            return true;
        }
        return changeStock(conn, productId, diff, "adjustment", adminId, null);
    }

    /**
     * 管理画面の在庫調整用：在庫数を指定の値にそろえ、差分を adjustment として記録する。
     * 自分で接続を開き、1つのトランザクションで処理する（その場で確定する）。
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
                boolean ok = adjustTo(conn, productId, newStockCount, adminId);
                if (ok) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return ok;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------
    // 内部メソッド
    // ------------------------------------------------------------

    private void checkReason(String reason) {
        if (reason == null || !VALID_REASON.contains(reason)) {
            throw new IllegalArgumentException("不正な在庫変更の理由です: " + reason);
        }
    }
}