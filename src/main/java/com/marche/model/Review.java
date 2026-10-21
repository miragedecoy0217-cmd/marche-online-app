package com.marche.model;

import java.time.LocalDateTime;

/**
 * reviews テーブルに対応するモデルクラス。
 * userName / productName は表示用（JOINで取得）で、reviews のカラムではない。
 *
 * 投稿者は reviews に user_id を持たず、order_id → orders.user_id から辿る設計。
 * status は DB の ENUM(review_status) に対応する文字列:
 *   pending / approved / rejected / hidden
 */
public class Review {

    private int reviewId;
    private int productId;
    private int orderId;
    private int rating;            // 1〜5
    private String comment;        // レビュー本文（NULL可）
    private String status = "pending";
    private Integer approvedBy;    // 承認した管理者ID（未承認ならnull）
    private LocalDateTime approvedAt;
    private boolean deleted;       // 論理削除フラグ

    // 表示用（JOINで取得）
    private int userId;
    private String userName;
    private String productName;

    public Review() {
    }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}