package com.marche.model;

import java.math.BigDecimal;

/**
 * 売上集計の1行分を表すモデルクラス（日次・月次・カテゴリ別・商品別で共通利用）。
 *
 * label の中身は集計の種類で変わる:
 *   日次 → "2026-06-10" / 月次 → "2026-06" / カテゴリ別 → カテゴリ名 / 商品別 → 商品名
 * グラフ（Chart.js）に渡すときは label を横軸、totalSales を値に使う。
 */
public class SalesData {

    private String label;
    private long orderCount;      // 注文件数
    private long totalQty;        // 販売数量（日次・月次では未設定のまま0）
    private BigDecimal totalSales = BigDecimal.ZERO; // 売上金額

    public SalesData() {
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }

    public long getTotalQty() { return totalQty; }
    public void setTotalQty(long totalQty) { this.totalQty = totalQty; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }
}