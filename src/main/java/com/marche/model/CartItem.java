package com.marche.model;

public class CartItem {
    private int userId;
    private int productId;
    private String productName;
    private int price;
    private int stock;
    private int quantity;

    // ゲッター・セッター
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // 小計（価格 × 数量）を計算する便利メソッド
    public int getSubtotal() {
        return this.price * this.quantity;
    }
}