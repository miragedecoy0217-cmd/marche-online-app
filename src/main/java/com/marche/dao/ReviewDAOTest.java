package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.marche.model.Review;
import com.marche.util.DBConnection;

/**
 * ReviewDAO の動作確認用。
 * 右クリック → 実行 → Javaアプリケーション。
 *
 * 【DBへの影響】
 * ・参照系のテストは、DBを変更しない。
 * ・登録・承認・削除のテスト（testWriteWithRollback）は、最後に必ず ROLLBACK するので、
 *   データは残らない（ただし review_id の連番は1つ進む。害はない）。
 * ・ローカルもサーバーも同じDBなので、COMMIT するコードは書かないこと。
 *
 * 初期データ投入済み（reviews に1件以上ある）ことを前提にしている。
 */
public class ReviewDAOTest {

    private static int okCount = 0;
    private static int ngCount = 0;

    public static void main(String[] args) {
        ReviewDAO dao = new ReviewDAO();

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
    private static void testRead(ReviewDAO dao) throws SQLException {
        System.out.println("=== 商品1(新鮮トマト)の公開レビュー ===");
        for (Review r : dao.findApprovedByProduct(1)) {
            print(r);
        }
        System.out.println("平均評価=" + dao.getAverageRating(1)
                + " / 件数=" + dao.countApproved(1));

        System.out.println("=== 承認待ち(pending)のレビュー ===");
        for (Review r : dao.findByStatus("pending")) {
            print(r);
        }

        System.out.println("=== 管理画面用: 全件 ===");
        List<Review> all = dao.findAllForAdmin();
        for (Review r : all) {
            print(r);
        }

        // 初期データ: ユーザー1は注文#1・#6が delivered で、商品1・3・4・8は投稿済み
        System.out.println("=== 投稿できる注文か ===");
        System.out.println("user1 × 商品1(投稿済み)  → " + dao.findReviewableOrderId(1, 1));
        System.out.println("user1 × 商品2(未購入)    → " + dao.findReviewableOrderId(1, 2));
        System.out.println("user2 × 商品5(配送中)    → " + dao.findReviewableOrderId(2, 5));

        System.out.println();
        System.out.println("=== 参照系の確認 ===");

        // 公開レビューの件数と、一覧の件数が一致するか
        List<Review> approved = dao.findApprovedByProduct(1);
        check("商品1: 公開レビューの一覧の件数 = countApproved", approved.size() == dao.countApproved(1));

        boolean allApproved = true;
        for (Review r : approved) {
            if (!"approved".equals(r.getStatus())) {
                allApproved = false;
            }
        }
        check("findApprovedByProduct は承認済みだけ", allApproved);

        boolean allPending = true;
        for (Review r : dao.findByStatus("pending")) {
            if (!"pending".equals(r.getStatus())) {
                allPending = false;
            }
        }
        check("findByStatus(pending) は承認待ちだけ", allPending);

        check("存在しない review_id(-1) は null", dao.findById(-1) == null);
        check("投稿できない組み合わせは null",
                dao.findReviewableOrderId(1, 2) == null && dao.findReviewableOrderId(2, 5) == null);

        try {
            dao.findByStatus("abc");
            check("不正な status は IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            check("不正な status は IllegalArgumentException", true);
        }
    }

    // ------------------------------------------------------------
    // 2. 登録・承認・却下・削除（最後に必ず ROLLBACK する）
    // ------------------------------------------------------------
    private static void testWriteWithRollback(ReviewDAO dao) throws SQLException {
        System.out.println();
        System.out.println("=== 登録・承認・削除のテスト（最後に ROLLBACK してデータは残さない） ===");

        List<Review> all = dao.findAllForAdmin();
        if (all.isEmpty()) {
            System.out.println("レビューが1件もないため、このテストはスキップします。");
            return;
        }
        // 外部キー（product_id / order_id）が必要なので、既存のレビューから借りる
        Review base = all.get(0);

        Review newReview = new Review();
        newReview.setProductId(base.getProductId());
        newReview.setOrderId(base.getOrderId());
        newReview.setRating(4);
        newReview.setComment("テスト投稿");

        int newId = 0;
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // ここから先は、COMMIT するまで確定しない

            // 登録
            newId = dao.insert(con, newReview);
            check("insert で review_id が返る（" + newId + "）", newId > 0);

            Review got = dao.findById(con, newId);
            check("登録直後は pending（承認待ち）",
                    got != null && "pending".equals(got.getStatus()));
            check("コメントと評価が保存されている",
                    got != null && "テスト投稿".equals(got.getComment()) && got.getRating() == 4);

            // 承認（承認者は、DBにいる管理者を1人借りる）
            int adminId = findAnyAdminId(con);
            if (adminId > 0) {
                check("approve で承認できる", dao.approve(con, newId, adminId));
                got = dao.findById(con, newId);
                check("承認後は approved", got != null && "approved".equals(got.getStatus()));
            } else {
                System.out.println("  （管理者がいないため、承認のテストはスキップ）");
            }

            // 却下・不正な値
            check("updateStatus で rejected にできる", dao.updateStatus(con, newId, "rejected"));
            got = dao.findById(con, newId);
            check("却下後は rejected", got != null && "rejected".equals(got.getStatus()));

            check("存在しないレビューの更新は false", !dao.updateStatus(con, -1, "hidden"));

            try {
                dao.updateStatus(con, newId, "abc");
                check("不正なステータスの更新は IllegalArgumentException", false);
            } catch (IllegalArgumentException e) {
                check("不正なステータスの更新は IllegalArgumentException", true);
            }

            // 論理削除
            check("delete で論理削除できる", dao.delete(con, newId));
            check("削除済みをもう一度 delete すると false", !dao.delete(con, newId));
            check("削除済みは updateStatus できない（false）", !dao.updateStatus(con, newId, "hidden"));
        } finally {
            if (con != null) {
                con.rollback(); // 必ず取り消す（COMMIT はしない）
                con.close();
            }
        }

        // ROLLBACK したので、登録したレビューは残っていないはず
        check("ROLLBACK 後は、登録したレビューが残っていない", dao.findById(newId) == null);
    }

    // ------------------------------------------------------------
    // 補助
    // ------------------------------------------------------------

    /** admin_users から、管理者のIDを1つ取る（承認テスト用）。いなければ 0。 */
    private static int findAnyAdminId(Connection con) throws SQLException {
        String sql = "SELECT admin_id FROM admin_users ORDER BY admin_id LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void print(Review r) {
        System.out.println("  #" + r.getReviewId() + " " + r.getProductName()
                + " ★" + r.getRating() + " [" + r.getStatus() + "] "
                + r.getUserName() + " : " + r.getComment());
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