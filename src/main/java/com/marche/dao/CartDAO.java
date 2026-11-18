package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.CartItem;
import com.marche.util.DBConnection;

/**
 * cart テーブルに対するデータアクセスクラス。
 *
 * ・cart.status は ENUM(cart_status)：active / ordered / expired。
 *   画面に出すカートは status = 'active' のものだけ。
 * ・cart に (user_id, product_id) の UNIQUE 制約は無いので、
 *   ON CONFLICT は使わず「UPDATE → 0件なら INSERT」で追加・加算する。
 */
public class CartDAO {

	//1.ユーザIDに紐づくカート内の商品一覧を取得(GET /cart)
	public List<CartItem> findByUserID(int userId) {
		List<CartItem> cartList = new ArrayList<>();
		String sql = "SELECT c.user_id, c.product_id, c.quantity, "
				   + "p.product_name, p.price, p.stock_count "
				   + "FROM cart c JOIN products p ON c.product_id = p.product_id "
				   + "WHERE c.user_id = ? AND c.status = 'active' "
				   + "ORDER BY c.cart_id";

		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					CartItem item = new CartItem();
					item.setUserId(rs.getInt("user_id"));
					item.setProductId(rs.getInt("product_id"));
					item.setQuantity(rs.getInt("quantity"));
					item.setProductName(rs.getString("product_name"));
					// price は numeric(10,2)。CartItem が int のため、整数に変換して渡す
					item.setPrice(rs.getBigDecimal("price").intValue());
					item.setStock(rs.getInt("stock_count"));
					cartList.add(item);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cartList;
	}

	//2.カートへの追加・数量加算(POST /cart/add)
	//  すでにカートにある商品なら数量を足し、無ければ新しく追加する
	public void addOrUpdate(int userId, int productId, int quantity) {
		String updateSql = "UPDATE cart SET quantity = quantity + ? "
						 + "WHERE user_id = ? AND product_id = ? AND status = 'active'";
		String insertSql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";

		try (Connection conn = DBConnection.getConnection()) {

			int updated;
			try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
				stmt.setInt(1, quantity);
				stmt.setInt(2, userId);
				stmt.setInt(3, productId);
				updated = stmt.executeUpdate();
			}

			// 更新できなかった（まだカートに無い）ときだけ、新しく追加する
			if (updated == 0) {
				try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
					stmt.setInt(1, userId);
					stmt.setInt(2, productId);
					stmt.setInt(3, quantity);
					stmt.executeUpdate();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//3.カート内商品の数量変更(POST /cart/update/{product_id})
	public void updateQuantity(int userId, int productId, int quantity) {
		String sql = "UPDATE cart SET quantity = ? "
				   + "WHERE user_id = ? AND product_id = ? AND status = 'active'";

		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, quantity);
			stmt.setInt(2, userId);
			stmt.setInt(3, productId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//4.カート内商品の削除(POST /cart/remove/{product_id})
	public void delete(int userId, int productId) {
		String sql = "DELETE FROM cart "
				   + "WHERE user_id = ? AND product_id = ? AND status = 'active'";

		try (Connection conn = DBConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, userId);
			stmt.setInt(2, productId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//5.注文確定時のカートクリア(POST /order/confirm のトランザクション内で使う)
	//  カートの行は消さず、status を 'ordered' にする。
	//  commit / rollback / close は呼び出し側（OrderServlet）が行う。
	//  更新した行数を返す。
	public int markOrdered(Connection con, int userId) throws SQLException {
		String sql = "UPDATE cart SET status = CAST('ordered' AS cart_status) "
				   + "WHERE user_id = ? AND status = 'active'";

		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			return stmt.executeUpdate();
		}
	}
}