package com.marche.model;

import java.math.BigDecimal;

/**
 * order_details テーブルに対応するモデルクラス。
 * productName / imagePath は表示用（products をJOINして取得）で、テーブルのカラムではない。
 */
public class OrderDetail {

    private int detailId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice; // 注文時点の単価（スナップショット）

    // 表示用（JOINで取得）
    private String productName;
    private String imagePath;

    public OrderDetail() {
    }

    public OrderDetail(int productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** 小計（数量 × 注文時単価） */
    public BigDecimal getSubtotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getDetailId() { return detailId; }
    public void setDetailId(int detailId) { this.detailId = detailId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}