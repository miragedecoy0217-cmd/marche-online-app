package com.marche.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.marche.model.Product;
import com.marche.util.DBConnection;

/**
 * productsテーブルに対するデータアクセスクラス。
 * すべてのSQL実行にPreparedStatementを使用する（SQLインジェクション対策）。
 */
public class ProductDAO {

    /** SELECTするカラム（mapRowと順序・名前を揃える） */
    private static final String COLUMNS =
            "product_id, category_id, product_name, description, price, "
            + "stock_count, image_path, created_at, updated_at, is_deleted";

    // =====================================================================
    // 既存メソッド（そのまま）
    // =====================================================================

    /**
     * 論理削除されていない商品を全件取得する。
     */
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT " + COLUMNS
                + " FROM products WHERE is_deleted = false ORDER BY product_id";

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * product_idを条件に1件取得する（削除済みも含む）。見つからない場合はnullを返す。
     * 管理画面の編集などで使う。客側の詳細画面では findActiveById を使うこと。
     */
    public Product findById(int productId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * category_idを条件に商品を絞り込む（論理削除されたものは除く）。
     */
    public List<Product> findByCategory(int categoryId) throws SQLException {
        String sql = "SELECT " + COLUMNS
                + " FROM products WHERE category_id = ? AND is_deleted = false "
                + "ORDER BY product_id";

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * product_name / descriptionに対してキーワードの部分一致検索を行う。
     */
    public List<Product> searchByKeyword(String keyword) throws SQLException {
        return search(keyword, null, 1, Integer.MAX_VALUE);
    }

    // =====================================================================
    // 追加：客側（No.6 商品一覧 / No.7 商品詳細）
    // =====================================================================

    /**
     * 商品一覧用：キーワード・カテゴリを組み合わせて絞り込み、ページ単位で取得する。
     *
     * @param keyword    キーワード（null・空文字なら条件に使わない）
     * @param categoryId カテゴリID（nullなら条件に使わない）
     * @param page       ページ番号（1始まり。1未満は1として扱う）
     * @param pageSize   1ページの件数
     */
    public List<Product> search(String keyword, Integer categoryId, int page, int pageSize)
            throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS
                + " FROM products WHERE is_deleted = false");
        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, keyword, categoryId);
        sql.append(" ORDER BY product_id LIMIT ? OFFSET ?");

        int safePage = Math.max(page, 1);
        long offset = (long) (safePage - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            bind(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 商品一覧用：search() と同じ条件での総件数（総ページ数の計算に使う）。
     */
    public int countSearch(String keyword, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM products WHERE is_deleted = false");
        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, keyword, categoryId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            bind(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * 客側の商品詳細用：論理削除されていない商品のみ取得する。
     * 見つからない（または削除済み）場合はnullを返す。
     */
    public Product findActiveById(int productId) throws SQLException {
        String sql = "SELECT " + COLUMNS
                + " FROM products WHERE product_id = ? AND is_deleted = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // =====================================================================
    // 追加：管理側（No.19〜24 商品管理）
    // =====================================================================

    /**
     * 管理用の商品一覧（論理削除されていないもの）をページ単位で取得する。
     */
    public List<Product> findAllForAdmin(int page, int pageSize) throws SQLException {
        String sql = "SELECT " + COLUMNS
                + " FROM products WHERE is_deleted = false "
                + "ORDER BY product_id LIMIT ? OFFSET ?";

        int safePage = Math.max(page, 1);
        List<Product> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setLong(2, (long) (safePage - 1) * pageSize);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 管理用一覧の総件数（論理削除されていないもの）。
     */
    public int countForAdmin() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_deleted = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * 商品を新規登録し、採番されたproduct_idを返す。
     * imagePath が null の場合は画像なしとして登録する。
     */
    public int insert(Product p) throws SQLException {
        String sql = "INSERT INTO products "
                + "(category_id, product_name, description, price, stock_count, "
                + "image_path, created_at, updated_at, is_deleted) "
                + "VALUES (?, ?, ?, ?, ?, ?, now(), now(), false)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, p.getCategoryId());
            pstmt.setString(2, p.getProductName());
            pstmt.setString(3, p.getDescription());
            pstmt.setBigDecimal(4, p.getPrice());
            pstmt.setInt(5, p.getStockCount());
            pstmt.setString(6, p.getImagePath());

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt("product_id");
                }
                throw new SQLException("product_idの採番結果を取得できませんでした。");
            }
        }
    }

    /**
     * 商品情報を更新する（updated_atも同時更新）。
     * imagePath が null の場合は既存の画像パスを維持する（No.23の仕様）。
     * 更新できた場合はtrue、対象が存在しない（または削除済み）場合はfalse。
     */
    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, product_name = ?, description = ?, "
                + "price = ?, stock_count = ?, image_path = COALESCE(?, image_path), "
                + "updated_at = now() "
                + "WHERE product_id = ? AND is_deleted = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, p.getCategoryId());
            pstmt.setString(2, p.getProductName());
            pstmt.setString(3, p.getDescription());
            pstmt.setBigDecimal(4, p.getPrice());
            pstmt.setInt(5, p.getStockCount());
            pstmt.setString(6, p.getImagePath());
            pstmt.setInt(7, p.getProductId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 論理削除（is_deleted = true）。物理削除は行わない（注文履歴の参照を保持するため）。
     * 更新できた場合はtrue、対象が存在しない（または削除済み）場合はfalse。
     */
    public boolean logicalDelete(int productId) throws SQLException {
        String sql = "UPDATE products SET is_deleted = true, updated_at = now() "
                + "WHERE product_id = ? AND is_deleted = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // =====================================================================
    // 内部ヘルパー
    // =====================================================================

    /**
     * 検索条件（キーワード・カテゴリ）をSQLとパラメータ一覧に追加する。
     * 値はSQLに連結せず、必ず ? でバインドする。
     */
    private void appendSearchConditions(StringBuilder sql, List<Object> params,
                                        String keyword, Integer categoryId) {
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (product_name ILIKE ? ESCAPE '\\' OR description ILIKE ? ESCAPE '\\')");
            String pattern = "%" + escapeLike(keyword.trim()) + "%";
            params.add(pattern);
            params.add(pattern);
        }
    }

    /**
     * LIKEの特殊文字（% _ \）を文字そのものとして検索するためにエスケープする。
     */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * パラメータ一覧を順番にPreparedStatementへ設定する。
     */
    private void bind(PreparedStatement pstmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            int idx = i + 1;
            if (v instanceof Integer) {
                pstmt.setInt(idx, (Integer) v);
            } else if (v instanceof Long) {
                pstmt.setLong(idx, (Long) v);
            } else {
                pstmt.setString(idx, (String) v);
            }
        }
    }

    /**
     * ResultSetの1行をProductオブジェクトに変換する。
     */
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockCount(rs.getInt("stock_count"));
        p.setImagePath(rs.getString("image_path"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            p.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        p.setDeleted(rs.getBoolean("is_deleted"));
        return p;
    }
}