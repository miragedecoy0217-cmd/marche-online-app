package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.marche.util.DBConnection;

/**
 * StockDAO の動作確認用。
 * 右クリック → 実行 → Javaアプリケーション。
 *
 * 【DBへの影響】
 * ・参照系のテストは、DBを変更しない。
 * ・在庫の増減・調整のテスト（testWriteWithRollback）は、最後に必ず ROLLBACK するので、
 *   在庫も履歴も元に戻る（ただし history_id の連番は進む。害はない）。
 * ・ローカルもサーバーも同じDBなので、COMMIT するコードは書かないこと。
 * ・updateStock(int, int) と adjustTo(int, int, int)（Connection なし）は、
 *   その場で確定してしまうので、このテストでは呼ばない。
 *
 * 初期データ投入済み（products に商品1がある、admin_users に管理者がいる）ことを前提にしている。
 */
public class StockDAOTest {

    /** テストに使う商品（初期データの「新鮮トマト」） */
    private static final int PRODUCT_ID = 1;

    private static int okCount = 0;
    private static int ngCount = 0;

    public static void main(String[] args) {
        StockDAO dao = new StockDAO();

        try {
            testRead(dao);
            testWriteWithRollback(dao);
        } catch (SQLException e) {
            System.out.println("=== 失敗（SQLエラー） ===");
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("=== 結果: OK " + okCount + " 件 / NG " + ngCount + " 件 ===");
    }

    // ------------------------------------------------------------
    // 1. 参照系（DBは変更しない）
    // ------------------------------------------------------------
    private static void testRead(StockDAO dao) throws SQLException {
        System.out.println("=== 在庫の取得（商品1〜10） ===");
        for (int id = 1; id <= 10; id++) {
            System.out.println("  商品" + id + " の在庫 = " + dao.getStock(id));
        }

        System.out.println();
        System.out.println("=== 参照系の確認 ===");
        check("商品1の在庫は 0 以上", dao.getStock(PRODUCT_ID) >= 0);
        check("存在しない商品(-1)の在庫は -1", dao.getStock(-1) == -1);

        try (Connection con = DBConnection.getConnection()) {
            check("Connection版 getStock と、通常版が同じ値",
                    dao.getStock(con, PRODUCT_ID) == dao.getStock(PRODUCT_ID));
            check("getStockForUpdate が在庫数を返す",
                    dao.getStockForUpdate(con, PRODUCT_ID) == dao.getStock(PRODUCT_ID));
            check("getStockForUpdate: 存在しない商品は -1",
                    dao.getStockForUpdate(con, -1) == -1);
            // FOR UPDATE のロックは、この接続を閉じるときに解放される
        }
    }

    // ------------------------------------------------------------
    // 2. 増減・調整（最後に必ず ROLLBACK する）
    // ------------------------------------------------------------
    private static void testWriteWithRollback(StockDAO dao) throws SQLException {
        System.out.println();
        System.out.println("=== 在庫の増減・調整のテスト（最後に ROLLBACK して元に戻す） ===");

        int stockAtStart = dao.getStock(PRODUCT_ID);
        int historyAtStart = countHistoryNewCon(PRODUCT_ID);
        if (stockAtStart < 0) {
            System.out.println("商品" + PRODUCT_ID + "が存在しないため、このテストはスキップします。");
            return;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // ここから先は、COMMIT するまで確定しない

            int adminId = findAnyAdminId(con);
            int before = dao.getStock(con, PRODUCT_ID);
            int historyBefore = countHistory(con, PRODUCT_ID);

            // 入庫（管理者の操作）
            check("changeStock: 入庫 +5（adjustment）ができる",
                    dao.changeStock(con, PRODUCT_ID, 5, "adjustment",
                            adminId > 0 ? adminId : null, null));
            check("在庫が 5 増えた", dao.getStock(con, PRODUCT_ID) == before + 5);

            // 出庫（客の操作。admin_id は null）
            check("changeStock: 出庫 -3（order）ができる",
                    dao.changeStock(con, PRODUCT_ID, -3, "order", null, null));
            check("在庫が 3 減った", dao.getStock(con, PRODUCT_ID) == before + 2);
            check("履歴が 2 件増えた", countHistory(con, PRODUCT_ID) == historyBefore + 2);

            // 在庫が足りないとき
            int now = dao.getStock(con, PRODUCT_ID);
            check("在庫より多い出庫は false（売り切れ）",
                    !dao.changeStock(con, PRODUCT_ID, -(now + 1), "order", null, null));
            check("失敗したときは、在庫が変わらない", dao.getStock(con, PRODUCT_ID) == now);
            check("失敗したときは、履歴も増えない", countHistory(con, PRODUCT_ID) == historyBefore + 2);

            // 存在しない商品・不正な reason
            check("存在しない商品の増減は false",
                    !dao.changeStock(con, -1, 1, "adjustment", null, null));
            try {
                dao.changeStock(con, PRODUCT_ID, 1, "abc", null, null);
                check("不正な reason は IllegalArgumentException", false);
            } catch (IllegalArgumentException e) {
                check("不正な reason は IllegalArgumentException", true);
            }

            // 在庫を指定の値にそろえる（管理画面の在庫調整）
            if (adminId > 0) {
                check("adjustTo: 在庫を 100 にそろえられる",
                        dao.adjustTo(con, PRODUCT_ID, 100, adminId));
                check("在庫が 100 になった", dao.getStock(con, PRODUCT_ID) == 100);
                check("調整の履歴が 1 件増えた", countHistory(con, PRODUCT_ID) == historyBefore + 3);

                check("adjustTo: すでに同じ値なら true", dao.adjustTo(con, PRODUCT_ID, 100, adminId));
                check("同じ値のときは、履歴が増えない",
                        countHistory(con, PRODUCT_ID) == historyBefore + 3);

                check("adjustTo: 負の値は false", !dao.adjustTo(con, PRODUCT_ID, -1, adminId));
                check("adjustTo: 存在しない商品は false", !dao.adjustTo(con, -1, 10, adminId));
                check("false のときは、在庫が変わらない", dao.getStock(con, PRODUCT_ID) == 100);
            } else {
                System.out.println("  （管理者がいないため、adjustTo のテストはスキップ）");
            }
        } finally {
            if (con != null) {
                con.rollback(); // 必ず取り消す（COMMIT はしない）
                con.close();
            }
        }

        // ROLLBACK したので、在庫も履歴も元に戻っているはず
        check("ROLLBACK 後は、在庫が元の値に戻っている", dao.getStock(PRODUCT_ID) == stockAtStart);
        check("ROLLBACK 後は、履歴が増えていない", countHistoryNewCon(PRODUCT_ID) == historyAtStart);
    }

    // ------------------------------------------------------------
    // 補助
    // ------------------------------------------------------------

    /** admin_users から、管理者のIDを1つ取る。いなければ 0。 */
    private static int findAnyAdminId(Connection con) throws SQLException {
        String sql = "SELECT admin_id FROM admin_users ORDER BY admin_id LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** 商品の stock_history の件数（同じ接続。未確定の行も数える）。 */
    private static int countHistory(Connection con, int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock_history WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** 商品の stock_history の件数（新しい接続。確定した行だけ数える）。 */
    private static int countHistoryNewCon(int productId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return countHistory(con, productId);
        }
    }

    /** 結果を OK / NG で表示する */
    private static void check(String name, boolean condition) {
        if (condition) {
            okCount++;
            System.out.println("  [OK] " + name);
        } else {
            ngCount++;
            System.out.println("  [NG] " + name);
        }
    }
}