package com.marche.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * orders テーブルに対応するモデルクラス。
 * details は order_details を保持する（JOINで一括取得したとき用）。
 *
 * status は DB の ENUM(order_status) に対応する文字列:
 *   pending / confirmed / shipped / delivered / cancelled / refunded
 */
public class Order {

    private int orderId;
    private int userId;
    private int addressId;
    private BigDecimal totalAmount;
    private String status = "pending";
    private String paymentMethod;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime orderAt;

    // 注文明細（orders テーブルのカラムではない）
    private List<OrderDetail> details = new ArrayList<>();

    public Order() {
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getOrderAt() { return orderAt; }
    public void setOrderAt(LocalDateTime orderAt) { this.orderAt = orderAt; }

    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }
}