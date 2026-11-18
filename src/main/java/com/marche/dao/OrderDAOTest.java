package com.marche.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.marche.model.Order;
import com.marche.model.OrderDetail;
import com.marche.util.DBConnection;

/**
 * OrderDAO の動作確認用。
 * 右クリック → 実行 → Javaアプリケーション。
 *
 * 【DBへの影響】
 * ・参照系のテストは、DBを変更しない。
 * ・登録・更新系のテスト（testWriteWithRollback）は、最後に必ず ROLLBACK するので、
 *   データは残らない（ただし order_id の連番は1つ進む。害はない）。
 * ・ローカルもサーバーも同じDBなので、COMMIT するコードは書かないこと。
 *
 * 初期データ投入済み（orders に1件以上ある）ことを前提にしている。
 */
public class OrderDAOTest {

    private static final String[] STATUSES = {
        "pending", "confirmed", "shipped", "delivered", "cancelled", "refunded"
    };

    private static int okCount = 0;
    private static int ngCount = 0;

    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();
        OrderDetailDAO detailDao = new OrderDetailDAO();

        try {
            showExisting(dao);
            testSearchAndCount(dao);
            testMypage(dao, detailDao);
            testWriteWithRollback(dao, detailDao);
        } catch (SQLException e) {
            System.out.println("=== 失敗（SQLエラー） ===");
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("=== 結果: OK " + okCount + " 件 / NG " + ngCount + " 件 ===");
    }

    // ------------------------------------------------------------
    // 1. 既存のテスト（表示のみ）
    // ------------------------------------------------------------
    private static void showExisting(OrderDAO dao) throws SQLException {
        List<Order> orders = dao.findByUserIdWithDetails(1);

        System.out.println("=== user_id=1 の注文履歴: " + orders.size() + "件 ===");
        for (Order o : orders) {
            System.out.println("注文#" + o.getOrderId() + " " + o.getOrderAt()
                    + " [" + o.getStatus() + "] 合計=" + o.getTotalAmount());
            for (OrderDetail d : o.getDetails()) {
                System.out.println("    " + d.getProductName() + " x" + d.getQuantity()
                        + " @" + d.getUnitPrice() + " = " + d.getSubtotal());
            }
        }

        System.out.println("=== 直近3件（全ユーザー） ===");
        for (Order o : dao.findRecent(3)) {
            System.out.println("注文#" + o.getOrderId() + " user=" + o.getUserId()
                    + " [" + o.getStatus() + "]");
        }
    }

    // ------------------------------------------------------------
    // 2. 管理画面の絞り込み・ページング（search / count）
    // ------------------------------------------------------------
    private static void testSearchAndCount(OrderDAO dao) throws SQLException {
        System.out.println();
        System.out.println("=== search / count のテスト ===");

        int total = dao.count(null, null, null);
        System.out.println("全件数 = " + total);

        check("count(条件なし) = findAll() の件数", total == dao.findAll().size());

        // ステータス別の件数を全部足すと、全件になるはず
        int sum = 0;
        for (String s : STATUSES) {
            int c = dao.count(s, null, null);
            System.out.println("    " + s + " = " + c);
            sum += c;
        }
        check("ステータス別の件数の合計 = 全件数", sum == total);

        // 1ページ目の件数
        List<Order> page1 = dao.search(null, null, null, 1);
        check("search 1ページ目の件数 = min(PAGE_SIZE, 全件数)",
                page1.size() == Math.min(OrderDAO.PAGE_SIZE, total));

        // 新しい順に並んでいるか
        boolean sorted = true;
        for (int i = 0; i + 1 < page1.size(); i++) {
            if (page1.get(i).getOrderAt().isBefore(page1.get(i + 1).getOrderAt())) {
                sorted = false;
            }
        }
        check("search は新しい順に並んでいる", sorted);

        // page=0 や範囲外
        List<Order> page0 = dao.search(null, null, null, 0);
        check("page=0 は 1ページ目と同じ扱い",
                page0.size() == page1.size()
                && (page1.isEmpty() || page0.get(0).getOrderId() == page1.get(0).getOrderId()));
        check("page=9999 は空のリスト", dao.search(null, null, null, 9999).isEmpty());

        // ステータスで絞ると、そのステータスだけ返る
        for (String s : STATUSES) {
            boolean allMatch = true;
            for (Order o : dao.search(s, null, null, 1)) {
                if (!s.equals(o.getStatus())) {
                    allMatch = false;
                }
            }
            check("search(status=" + s + ") は全部 " + s, allMatch);
        }

        // 空文字のステータスは「絞り込まない」
        check("status が空文字なら絞り込まない", dao.count("", null, null) == total);

        // 日付の絞り込み
        check("2000-01-01 ～ 2100-01-01 は全件",
                dao.count(null, LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1)) == total);
        check("2100年だけを指定すると 0件",
                dao.count(null, LocalDate.of(2100, 1, 1), LocalDate.of(2100, 1, 2)) == 0);
        check("date_from > date_to は 0件",
                dao.count(null, LocalDate.of(2100, 1, 1), LocalDate.of(2000, 1, 1)) == 0);

        // 一番新しい注文の日付で絞ると、その注文が入っている（dateTo はその日を含む）
        if (!page1.isEmpty()) {
            Order newest = page1.get(0);
            LocalDate day = newest.getOrderAt().toLocalDate();
            boolean found = false;
            for (Order o : dao.search(null, day, day, 1)) {
                if (o.getOrderId() == newest.getOrderId()) {
                    found = true;
                }
            }
            check("date_from = date_to = 注文日 で、その日の注文が取れる（dateTo含む）", found);
        }

        // 不正なステータスは例外
        try {
            dao.count("abc", null, null);
            check("不正な status は IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            check("不正な status は IllegalArgumentException", true);
        }
    }

    // ------------------------------------------------------------
    // 3. マイページ（一覧・詳細・本人確認）
    // ------------------------------------------------------------
    private static void testMypage(OrderDAO dao, OrderDetailDAO detailDao) throws SQLException {
        System.out.println();
        System.out.println("=== マイページ（findByUserId / 本人確認）のテスト ===");

        List<Order> recent = dao.findRecent(1);
        if (recent.isEmpty()) {
            System.out.println("注文が1件もないため、このテストはスキップします。");
            return;
        }
        Order base = recent.get(0);
        int orderId = base.getOrderId();
        int userId = base.getUserId();
        System.out.println("使う注文: #" + orderId + "（user_id=" + userId + "）");

        // 一覧
        List<Order> mine = dao.findByUserId(userId, 1);
        boolean allMine = true;
        for (Order o : mine) {
            if (o.getUserId() != userId) {
                allMine = false;
            }
        }
        check("findByUserId は、そのユーザーの注文だけ", allMine && !mine.isEmpty());
        check("countByUserId >= findByUserId の件数", dao.countByUserId(userId) >= mine.size());
        check("存在しないユーザー(-1)の注文は 0件", dao.findByUserId(-1, 1).isEmpty()
                && dao.countByUserId(-1) == 0);

        // 詳細（本人確認つき）
        Order own = dao.findByIdWithDetails(orderId, userId);
        check("本人なら詳細が取れる", own != null && own.getOrderId() == orderId);

        Order other = dao.findByIdWithDetails(orderId, userId + 999999);
        check("他人のuser_idだと null（他人の注文は見せない）", other == null);

        check("存在しない注文IDだと null", dao.findByIdWithDetails(-1, userId) == null);

        // 明細の件数が、OrderDetailDAO と一致するか
        if (own != null) {
            int detailCount = detailDao.findByOrderId(orderId).size();
            check("JOINで取れた明細の件数 = OrderDetailDAO.findByOrderId の件数",
                    own.getDetails().size() == detailCount);
        }

        // 管理用（本人確認なし）の版は、他人でも取れる
        check("管理用 findByIdWithDetails(orderId) は取れる",
                dao.findByIdWithDetails(orderId) != null);
    }

    // ------------------------------------------------------------
    // 4. 登録・更新・キャンセル（最後に必ず ROLLBACK する）
    // ------------------------------------------------------------
    private static void testWriteWithRollback(OrderDAO dao, OrderDetailDAO detailDao)
            throws SQLException {
        System.out.println();
        System.out.println("=== 登録・更新のテスト（最後に ROLLBACK してデータは残さない） ===");

        List<Order> recent = dao.findRecent(1);
        if (recent.isEmpty()) {
            System.out.println("注文が1件もないため、このテストはスキップします。");
            return;
        }
        // 外部キー（user_id / address_id / product_id）が必要なので、既存の注文から借りる
        Order base = recent.get(0);
        List<OrderDetail> baseDetails = detailDao.findByOrderId(base.getOrderId());

        Order newOrder = new Order();
        newOrder.setUserId(base.getUserId());
        newOrder.setAddressId(base.getAddressId());
        newOrder.setTotalAmount(new BigDecimal("100.00"));
        newOrder.setPaymentMethod("テスト");

        int newId = 0;
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // ここから先は、COMMIT するまで確定しない

            newId = dao.insert(con, newOrder);
            check("insert で order_id が返る（" + newId + "）", newId > 0);

            if (!baseDetails.isEmpty()) {
                OrderDetail d = new OrderDetail(baseDetails.get(0).getProductId(), 1,
                        new BigDecimal("100.00"));
                d.setOrderId(newId);
                int detailId = detailDao.insert(con, d);
                check("OrderDetailDAO.insert で detail_id が返る（" + detailId + "）", detailId > 0);
            }

            check("updateStatus(con) で confirmed に更新できる",
                    dao.updateStatus(con, newId, "confirmed"));
            check("存在しない注文のステータス更新は false",
                    !dao.updateStatus(con, -1, "confirmed"));

            try {
                dao.updateStatus(con, newId, "abc");
                check("不正なステータスの更新は IllegalArgumentException", false);
            } catch (IllegalArgumentException e) {
                check("不正なステータスの更新は IllegalArgumentException", true);
            }

            check("cancel で cancelled にできる", dao.cancel(con, newId, "テスト"));
        } finally {
            if (con != null) {
                con.rollback(); // 必ず取り消す（COMMIT はしない）
                con.close();
            }
        }

        // ROLLBACK したので、登録した注文は残っていないはず
        check("ROLLBACK 後は、登録した注文が残っていない", dao.findById(newId) == null);
    }

    // ------------------------------------------------------------
    // 結果を OK / NG で表示する
    // ------------------------------------------------------------
    private static void check(String name, boolean condition) {
        if (condition) {
            okCount++;
            System.out.println("  [OK] " + name);
        } else {
            ngCount++;
            System.out.println("  [NG] " + name);
        }
    }
}