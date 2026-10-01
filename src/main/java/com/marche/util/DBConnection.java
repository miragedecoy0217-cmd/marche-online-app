package com.marche.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection{

	private static final String URL = "jdbc:postgresql://172.16.1.123:5432/postgres";
	private static final String USER = "abcde";
    private static final String PASSWORD = "2026aabb"; // PostgreSQLのパスワード

    // JDBCドライバのロード
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
        }
    }

    /**
     * データベース接続を取得するメソッド
     * 呼び出し元で try-with-resources を使用して Connection を close すること
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 接続テスト用 main メソッド
     */
    public static void main(String[] args) {
        System.out.println("データベース接続テストを開始します...");

        // WBS指示通りの try-with-resources 記述（自動で close される）
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("★ データベース接続成功！ ★");
            }
        } catch (SQLException e) {
            System.err.println("✕ データベース接続失敗 ✕");
            e.printStackTrace();
        }
    }
}