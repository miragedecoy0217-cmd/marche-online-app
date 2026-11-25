package com.marche.model;

import java.time.LocalDateTime;

/**
 * stock_history テーブルに対応するモデルクラス。
 * productName / adminName は表示用（JOINで取得）で、テーブルのカラムではない。
 *
 * adminId / orderId は NULL がありうるので Integer（客の操作は adminId が null、
 * 注文に関係しない変更は orderId が null）。
 *
 * reason は DB の ENUM(stock_change_reason) に対応する文字列:
 *   purchase / order / cancel / adjustment / loss
 */
public class StockHistory {

    private int historyId;
    private int productId;
    private Integer adminId;
    private Integer orderId;
    private int changeQty; // 増減数（+ / -）
    private String reason;
    private LocalDateTime createdAt;

    // 表示用（JOINで取得）
    private String productName;
    private String adminName;

    public StockHistory() {
    }

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public int getChangeQty() { return changeQty; }
    public void setChangeQty(int changeQty) { this.changeQty = changeQty; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
}