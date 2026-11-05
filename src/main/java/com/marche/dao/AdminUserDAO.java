package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.marche.model.AdminUser;
import com.marche.util.DBConnection;

public class AdminUserDAO {

	public AdminUser findByLoginId(String loginId) throws SQLException {
		String sql = "SELECT admin_id, login_id, password_hash, name, role::text AS role "
				+ "FROM admin_users WHERE login_id = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, loginId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					AdminUser a = new AdminUser();
					a.setAdminId(rs.getInt("admin_id"));
					a.setLoginId(rs.getString("login_id"));
					a.setPasswordHash(rs.getString("password_hash"));
					a.setName(rs.getString("name"));
					a.setRole(rs.getString("role"));
					return a;
				}
			}
		}
		return null;
	}

	public void updateLastLogin(int adminId) throws SQLException {
		String sql = "UPDATE admin_users SET last_login_at = now() WHERE admin_id = ?";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, adminId);
			pstmt.executeUpdate();
		}
	}
}