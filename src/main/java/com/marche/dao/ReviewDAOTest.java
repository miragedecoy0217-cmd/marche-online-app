package com.marche.dao;

import java.sql.SQLException;
import java.util.List;

import com.marche.model.Review;

/**
 * ReviewDAO の動作確認用（参照のみ。DBは変更しない）。
 * 事前に ALTER TABLE reviews ADD COLUMN comment TEXT; を実行しておくこと。
 * 右クリック → 実行 → Javaアプリケーション。
 */
public class ReviewDAOTest {

    public static void main(String[] args) {
        ReviewDAO dao = new ReviewDAO();

        try {
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
        } catch (SQLException e) {
            System.out.println("=== 失敗 ===");
            e.printStackTrace();
        }
    }

    private static void print(Review r) {
        System.out.println("  #" + r.getReviewId() + " " + r.getProductName()
                + " ★" + r.getRating() + " [" + r.getStatus() + "] "
                + r.getUserName() + " : " + r.getComment());
    }
}