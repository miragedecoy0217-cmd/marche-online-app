package com.marche.dao;

import com.marche.model.SalesData;

import java.sql.SQLException;
import java.util.List;

/**
 * SalesDAO の動作確認用（参照のみ。DBは変更しない）。
 * 右クリック → 実行 → Javaアプリケーション。
 */
public class SalesDAOTest {

    public static void main(String[] args) {
        SalesDAO dao = new SalesDAO();

        try {
            SalesData total = dao.findTotal();
            System.out.println("=== 全期間合計: " + total.getTotalSales()
                    + " 円 / " + total.getOrderCount() + "件 ===");

            print("日次売上", dao.findDaily());
            print("月次売上", dao.findMonthly());
            print("カテゴリ別売上", dao.findByCategory());
            print("商品別売上", dao.findByProduct());
        } catch (SQLException e) {
            System.out.println("=== 失敗 ===");
            e.printStackTrace();
        }
    }

    private static void print(String title, List<SalesData> list) {
        System.out.println("=== " + title + " (" + list.size() + "行) ===");
        for (SalesData s : list) {
            System.out.println("  " + s.getLabel()
                    + "  売上=" + s.getTotalSales()
                    + "  注文=" + s.getOrderCount() + "件"
                    + "  数量=" + s.getTotalQty());
        }
    }
}