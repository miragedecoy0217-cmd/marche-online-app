package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.marche.model.User;
import com.marche.util.DBConnection;

public class UserDAO {
	
	public User findByEmail(String email) throws SQLException {
	
		String sql = "SELECT * FROM users WHERE email = ?";
		
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)){
			
			pstmt.setString(1, email);
			
			try(ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					User user = new User();
					user.setUser_id(rs.getInt("user_id"));
					user.setEmail(rs.getString("email"));
					user.setPassword_hash(rs.getString("password_hash"));
					user.setName(rs.getString("name"));
					user.setRole(rs.getString("role"));
					
					return user;
				}
			}
		}
		
		return null;
	}

}