package com.marche.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.marche.model.Product;

public class ProductDAOTest {

    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();
        StockDAO stockDao = new StockDAO();

        try {
            // ---------------------------------------------------------
            // 既存テスト
            // ---------------------------------------------------------

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

            // ---------------------------------------------------------
            // 追加テスト：客側（No.6 / No.7）
            // ---------------------------------------------------------
            System.out.println();
            System.out.println("########## 追加テスト（客側） ##########");

            // 6. キーワード＋カテゴリの組み合わせ検索
            System.out.println("=== search(いちご, 指定なし): "
                    + dao.search("いちご", null, 1, 5).size() + "件 (濃厚いちご・ジャムで2件の想定) ===");
            System.out.println("=== search(いちご, カテゴリ2): "
                    + dao.search("いちご", 2, 1, 5).size() + "件 (果物の濃厚いちごだけで1件の想定) ===");
            System.out.println("=== search(指定なし, カテゴリ1): "
                    + dao.search(null, 1, 1, 5).size() + "件 ===");
            System.out.println("=== search(空文字, 指定なし): "
                    + dao.search("", null, 1, 5).size() + "件 (空文字は条件なし扱い) ===");

            // 7. ページング（3件ずつ）
            System.out.println("=== 総件数 countSearch(null, null): "
                    + dao.countSearch(null, null) + "件 (10件の想定) ===");
            System.out.println("=== 1ページ目(3件ずつ): "
                    + dao.search(null, null, 1, 3).size() + "件 (3件の想定) ===");
            System.out.println("=== 4ページ目(3件ずつ): "
                    + dao.search(null, null, 4, 3).size() + "件 (残り1件の想定) ===");
            System.out.println("=== 5ページ目(3件ずつ): "
                    + dao.search(null, null, 5, 3).size() + "件 (0件の想定) ===");
            System.out.println("=== 0ページ目(不正値): "
                    + dao.search(null, null, 0, 3).size() + "件 (1ページ目扱いで3件の想定) ===");

            // 8. LIKEの特殊文字・大文字小文字
            System.out.println("=== search(%): "
                    + dao.search("%", null, 1, 5).size() + "件 (0ならOK。全件なら未エスケープ) ===");
            System.out.println("=== search(_): "
                    + dao.search("_", null, 1, 5).size() + "件 (0ならOK) ===");

            // 9. 客側の詳細取得
            System.out.println("=== findActiveById(1): "
                    + (dao.findActiveById(1) == null ? "null" : "取得できた") + " ===");
            System.out.println("=== findActiveById(9999): "
                    + dao.findActiveById(9999) + " (nullならOK) ===");

            // ---------------------------------------------------------
            // 追加テスト：管理側（No.19〜24）
            // ---------------------------------------------------------
            System.out.println();
            System.out.println("########## 追加テスト（管理側） ##########");

            // 10. 管理用一覧
            System.out.println("=== 管理用 countForAdmin: " + dao.countForAdmin() + "件 ===");
            System.out.println("=== 管理用 1ページ目(5件ずつ): "
                    + dao.findAllForAdmin(1, 5).size() + "件 (5件の想定) ===");

            // 11. 登録 → 取得 → 更新 → 画像維持 → 論理削除
            Product np = new Product();
            np.setCategoryId(1);
            np.setProductName("【テスト】登録確認用");
            np.setDescription("DAOテストで作成した商品");
            np.setPrice(new BigDecimal("123"));
            np.setStockCount(5);
            np.setImagePath("test.jpg");

            int newId = dao.insert(np);
            System.out.println("=== insert: 採番ID=" + newId + " ===");

            Product got = dao.findById(newId);
            System.out.println("=== insert後に取得: " + got.getProductName()
                    + " / " + got.getPrice() + "円 / 画像=" + got.getImagePath() + " ===");

            // 更新（画像はnull → 既存の画像を維持するはず）
            got.setProductName("【テスト】更新後");
            got.setPrice(new BigDecimal("456"));
            got.setImagePath(null);
            boolean updated = dao.update(got);
            Product afterUpdate = dao.findById(newId);
            System.out.println("=== update: " + updated + " / " + afterUpdate.getProductName()
                    + " / " + afterUpdate.getPrice() + "円 ===");
            System.out.println("=== 画像維持: " + afterUpdate.getImagePath()
                    + " (test.jpgのままならOK) ===");

            // 論理削除
            boolean deleted = dao.logicalDelete(newId);
            System.out.println("=== logicalDelete: " + deleted + " ===");
            System.out.println("=== 削除後 findActiveById: " + dao.findActiveById(newId)
                    + " (nullならOK) ===");
            System.out.println("=== 削除後 findById: "
                    + (dao.findById(newId) == null ? "null" : "取得できた(削除済みも返す)") + " ===");
            System.out.println("=== 削除後 全件: " + dao.findAll().size() + "件 (10件に戻るならOK) ===");
            System.out.println("=== 2回目のlogicalDelete: " + dao.logicalDelete(newId)
                    + " (falseならOK) ===");
            System.out.println("=== 存在しないIDのupdate: " + dao.update(idOnly(9999))
                    + " (falseならOK) ===");

            System.out.println();
            System.out.println("※ テスト商品(ID=" + newId + ")は論理削除済みでDBに残ります。"
                    + "気になる場合はpgAdminで確認してください。");

        } catch (SQLException e) {
            System.out.println("=== エラー ===");
            e.printStackTrace();
        }
    }

    /** 存在しないIDのupdateを試すための最小のProduct */
    private static Product idOnly(int id) {
        Product p = new Product();
        p.setProductId(id);
        p.setCategoryId(1);
        p.setProductName("存在しない商品");
        p.setDescription("");
        p.setPrice(BigDecimal.ONE);
        p.setStockCount(0);
        return p;
    }
}