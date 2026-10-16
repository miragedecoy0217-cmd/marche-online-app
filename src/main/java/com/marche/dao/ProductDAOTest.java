package com.marche.dao;

import java.sql.SQLException;
import java.util.List;

import com.marche.model.Product;

public class ProductDAOTest {

    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();
        StockDAO stockDao = new StockDAO();

        try {
            // 1. 全件取得
            List<Product> all = dao.findAll();
            System.out.println("=== 全件: " + all.size() + "件 ===");
            for (Product p : all) {
                System.out.println(p.getProductId() + " : " + p.getProductName()
                        + " / " + p.getPrice() + "円 / 在庫" + p.getStockCount());
            }

            // 2. ID検索（存在する・しない）
            Product p1 = dao.findById(1);
            System.out.println("=== ID=1: " + (p1 == null ? "null" : p1.getProductName()) + " ===");
            System.out.println("=== ID=9999: " + dao.findById(9999) + " (nullならOK) ===");

            // 3. カテゴリ検索
            System.out.println("=== カテゴリ1: " + dao.findByCategory(1).size() + "件 ===");
            System.out.println("=== カテゴリ999: " + dao.findByCategory(999).size() + "件 (0ならOK) ===");

            // 4. キーワード検索
            System.out.println("=== 「トマト」: " + dao.searchByKeyword("トマト").size() + "件 ===");
            System.out.println("=== 「zzzz」: " + dao.searchByKeyword("zzzz").size() + "件 (0ならOK) ===");
            System.out.println("=== SQLi試行: " + dao.searchByKeyword("' OR '1'='1").size() + "件 (0ならOK) ===");

            // 5. 在庫の取得と更新（元の値に戻す）
            int before = stockDao.getStock(1);
            System.out.println("=== 在庫(ID=1) 更新前: " + before + " ===");
            stockDao.updateStock(1, before + 1);
            System.out.println("=== 在庫(ID=1) 更新後: " + stockDao.getStock(1) + " (前+1ならOK) ===");
            stockDao.updateStock(1, before); // 元に戻す
            System.out.println("=== 在庫(ID=1) 復元後: " + stockDao.getStock(1) + " ===");
            System.out.println("=== 在庫(ID=9999): " + stockDao.getStock(9999) + " (-1ならOK) ===");

        } catch (SQLException e) {
            System.out.println("=== エラー ===");
            e.printStackTrace();
        }
    }
}