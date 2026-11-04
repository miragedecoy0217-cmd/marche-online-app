package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.CartItem;
import com.marche.util.DBConnection;

public class CartDAO {
	//1.ユーザIDに紐づくカートない商品一覧を取得(GET/cart)
	public List<CartItem> findByUserID(int userId) {
		List<CartItem> cartList = new ArrayList<>();
		String sql = "SELECT c.user_id, c.product_id,c.quantity,p.name AS product_name, p.price,p.stock "+
					 "FROM carts c JOIN products p ON c.product_id = p.id " +
					 "WHERE c.user_id = ?";
		
		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)){
			
			stmt.setInt(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					CartItem item = new CartItem();
					item.setUserId(rs.getInt("userId"));
					item.setProductId(rs.getInt("product_id"));
					item.setQuantity(rs.getInt("quantity"));
					item.setProductName(rs.getString("product_name"));
					item.setPrice(rs.getInt("price"));
					item.setStock(rs.getInt("stock"));
					cartList.add(item);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cartList;
	}
	
	//2.カートへの追加・数量加算(POST /cart/add - UPSERT)
	public void addOrUpdate(int userId, int productId, int quantity) {
		String sql = "INSERT INTO carts (user_id,product_id, quantity) VALUES (?,?,?)" +
					 "ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = carts.quantity + EXCLUDED.quantity";
		
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql)){
			
			stmt.setInt(1, userId);
			stmt.setInt(2, productId);
			stmt.setInt(3, quantity);
			stmt.executeUpdate();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	//3.カート内商品の数量変更(POST /cart/update/{id})
	public void updateQuantity(int userId,int productId,int quantity) {
		String sql = "UPDATE carts SET quantity = ? WHERE user_id = ? AND product_id = ?";
		
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql)) {
			
			stmt.setInt(1, quantity);
			stmt.setInt(2, userId);
			stmt.setInt(3, productId);
			stmt.executeUpdate();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	//4.カート内商品の削除(POST /cart/remove/{id})
	public void delete(int userId, int productId) {
		String sql = "DELETE FROM carts WHERE user_id = ? AND product_id = ?";
		
		try(Connection conn = DBConnection.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql)){
			
			stmt.setInt(1, userId);
			stmt.setInt(2, productId);
			stmt.executeUpdate();
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}