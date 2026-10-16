package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.Category;
import com.marche.util.DBConnection;

/**
 * categoriesテーブルに対するデータアクセスクラス。
 */
public class CategoryDAO {

    /**
     * カテゴリ一覧をdisplay_order順に全件取得する。
     */
    public List<Category> findAll() throws SQLException {
        String sql = "SELECT category_id, category_name, display_order "
                + "FROM categories ORDER BY display_order";

        List<Category> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setDisplayOrder(rs.getInt("display_order"));
                list.add(c);
            }
        }
        return list;
    }
}
