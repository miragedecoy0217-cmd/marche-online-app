package com.marche.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.marche.model.Review;
import com.marche.util.DBConnection;

/**
 * reviews テーブルに対するデータアクセスクラス。
 *
 * ・投稿者は reviews に user_id を持たないため、orders → users をJOINして取得する。
 * ・削除は論理削除（is_deleted = true）。物理削除はしない。
 * ・レビュー本文は comment カラム（要: ALTER TABLE reviews ADD COLUMN comment TEXT;）。
 */
public class ReviewDAO {

    /** review_status ENUM に存在する値 */
    private static final List<String> VALID_STATUS = Arrays.asList(
            "pending", "approved", "rejected", "hidden");

    // 一覧取得用のSELECT句（投稿者名・商品名つき）
    private static final String SELECT_BASE =
            "SELECT r.review_id, r.product_id, r.order_id, r.rating, r.comment, r.status, "
            + "r.approved_by, r.approved_at, r.is_deleted, "
            + "o.user_id, u.name AS user_name, p.product_name "
            + "FROM reviews r "
            + "JOIN orders o ON r.order_id = o.order_id "
            + "JOIN users u ON o.user_id = u.user_id "
            + "JOIN products p ON r.product_id = p.product_id ";

    // ------------------------------------------------------------
    // 投稿
    // ------------------------------------------------------------

    /**
     * レビューを投稿する。status は DB の既定値（pending = 承認待ち）になる。
     * 生成された review_id を返す。
     * ※投稿前に findReviewableOrderId() で、投稿できる注文かを確認すること。
     */
    public int insert(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, order_id, rating, comment) "
                + "VALUES (?, ?, ?, ?) RETURNING review_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getOrderId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * ユーザーが、その商品のレビューを投稿できるかを調べる。
     * 条件: そのユーザーの「配達完了(delivered)」の注文に商品が含まれ、
     *       かつ、その注文・商品の組み合わせでまだレビューしていない。
     * 投稿できる場合は、投稿に使う order_id を返す。できない場合は null。
     */
    public Integer findReviewableOrderId(int userId, int productId) throws SQLException {
        String sql = "SELECT o.order_id "
                + "FROM orders o "
                + "JOIN order_details d ON o.order_id = d.order_id "
                + "WHERE o.user_id = ? AND d.product_id = ? AND o.status = 'delivered' "
                + "AND NOT EXISTS (SELECT 1 FROM reviews r "
                + "  WHERE r.order_id = o.order_id AND r.product_id = d.product_id "
                + "  AND r.is_deleted = false) "
                + "ORDER BY o.order_id LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("order_id");
                }
                return null;
            }
        }
    }

    // ------------------------------------------------------------
    // 公開側（商品詳細ページ）
    // ------------------------------------------------------------

    /** 商品の公開レビュー（承認済み・未削除）を、新しい順に取得する。 */
    public List<Review> findApprovedByProduct(int productId) throws SQLException {
        String sql = SELECT_BASE
                + "WHERE r.product_id = ? AND r.status = 'approved' AND r.is_deleted = false "
                + "ORDER BY r.review_id DESC";
        return queryList(sql, productId);
    }

    /** 商品の平均評価（承認済み・未削除）。レビューがなければ 0 を返す。 */
    public double getAverageRating(int productId) throws SQLException {
        String sql = "SELECT AVG(rating) AS avg_rating FROM reviews "
                + "WHERE product_id = ? AND status = 'approved' AND is_deleted = false";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal avg = rs.getBigDecimal("avg_rating");
                    return (avg == null) ? 0.0 : avg.doubleValue();
                }
                return 0.0;
            }
        }
    }

    /** 商品の公開レビュー件数（承認済み・未削除）。 */
    public int countApproved(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews "
                + "WHERE product_id = ? AND status = 'approved' AND is_deleted = false";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ------------------------------------------------------------
    // 管理側
    // ------------------------------------------------------------

    /** 未削除のレビューを全件、新しい順に取得する（管理画面の一覧用）。 */
    public List<Review> findAllForAdmin() throws SQLException {
        String sql = SELECT_BASE
                + "WHERE r.is_deleted = false "
                + "ORDER BY r.review_id DESC";

        List<Review> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** ステータスで絞り込む（例: "pending" で承認待ちだけ）。未削除のみ。 */
    public List<Review> findByStatus(String status) throws SQLException {
        checkStatus(status);

        String sql = SELECT_BASE
                + "WHERE r.status = CAST(? AS review_status) AND r.is_deleted = false "
                + "ORDER BY r.review_id DESC";
        return queryList(sql, status);
    }

    /** review_id で1件取得する。見つからなければ null（削除済みも含む）。 */
    public Review findById(int reviewId) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.review_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reviewId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * レビューを承認する（status = approved、承認した管理者と日時を記録）。
     * 更新できたら true、該当がなければ false。
     */
    public boolean approve(int reviewId, int adminId) throws SQLException {
        String sql = "UPDATE reviews SET status = CAST('approved' AS review_status), "
                + "approved_by = ?, approved_at = now() "
                + "WHERE review_id = ? AND is_deleted = false";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setInt(2, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * ステータスを変更する（却下 rejected・非公開 hidden・承認待ちへ戻す pending など）。
     * 承認は approve() を使うこと（承認者・承認日時を記録するため）。
     */
    public boolean updateStatus(int reviewId, String status) throws SQLException {
        checkStatus(status);

        String sql = "UPDATE reviews SET status = CAST(? AS review_status) "
                + "WHERE review_id = ? AND is_deleted = false";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    /** レビューを論理削除する（is_deleted = true）。 */
    public boolean delete(int reviewId) throws SQLException {
        String sql = "UPDATE reviews SET is_deleted = true "
                + "WHERE review_id = ? AND is_deleted = false";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------
    // 内部メソッド
    // ------------------------------------------------------------

    private List<Review> queryList(String sql, int param) throws SQLException {
        List<Review> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private List<Review> queryList(String sql, String param) throws SQLException {
        List<Review> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setStatus(rs.getString("status"));

        int approvedBy = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            r.setApprovedBy(approvedBy);
        }
        Timestamp approvedAt = rs.getTimestamp("approved_at");
        if (approvedAt != null) {
            r.setApprovedAt(approvedAt.toLocalDateTime());
        }
        r.setDeleted(rs.getBoolean("is_deleted"));

        r.setUserId(rs.getInt("user_id"));
        r.setUserName(rs.getString("user_name"));
        r.setProductName(rs.getString("product_name"));
        return r;
    }

    private void checkStatus(String status) {
        if (status == null || !VALID_STATUS.contains(status)) {
            throw new IllegalArgumentException("不正なレビューステータスです: " + status);
        }
    }
}