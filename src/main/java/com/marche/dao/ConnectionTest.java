package com.marche.dao;

import java.sql.SQLException;
import java.util.List;

import com.marche.model.Category;

/**
 * DB接続確認用のテストクラス。
 * CategoryDAO#findAll()を呼び出し、結果をコンソールに出力する。
 */
public class ConnectionTest {

    public static void main(String[] args) {
        CategoryDAO categoryDAO = new CategoryDAO();

        try {
            List<Category> categories = categoryDAO.findAll();

            System.out.println("=== 接続成功。カテゴリ一覧 ===");
            for (Category c : categories) {
                System.out.println(c.getCategoryId() + " : " + c.getCategoryName()
                        + " (display_order=" + c.getDisplayOrder() + ")");
            }
        } catch (SQLException e) {
            System.out.println("=== 接続失敗 ===");
            e.printStackTrace();
        }
    }
}