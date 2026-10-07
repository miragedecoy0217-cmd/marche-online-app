package com.marche.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DBConnection{

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
        Properties props = new Properties();
        try {
    		props.load( DBConnection.class.getResourceAsStream("/db.properties" ) );
    	}catch (IOException e) {
    		throw new RuntimeException("db.propertiesの読み込みに失敗しました",e);
    	}
        String url = props.getProperty("db.url");
    	String user = props.getProperty("db.user");
    	String pass = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
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