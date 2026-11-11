<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.math.BigDecimal, java.math.RoundingMode, com.marche.model.Order, com.marche.model.OrderDetail" %>
<%!
    private static String h(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String statusLabel(String status) {
        if ("pending".equals(status)) return "注文受付";
        if ("confirmed".equals(status)) return "確認済み";
        if ("shipped".equals(status)) return "発送済み";
        if ("delivered".equals(status)) return "配達完了";
        if ("cancelled".equals(status)) return "キャンセル";
        if ("refunded".equals(status)) return "返金済み";
        return status;
    }

    private static String yen(BigDecimal v) {
        if (v == null) {
            return "";
        }
        return v.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    Object userId = request.getAttribute("userId");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>注文履歴（動作確認用）</title>
<style>
  body { font-family: sans-serif; margin: 24px; }
  .order { border: 1px solid #ccc; border-radius: 6px; padding: 12px; margin-bottom: 16px; }
  .order h2 { margin: 0 0 8px; font-size: 1.1em; }
  table { border-collapse: collapse; width: 100%; }
  th, td { border-bottom: 1px solid #ddd; padding: 4px 8px; text-align: left; }
  td.num, th.num { text-align: right; }
  .note { color: #666; font-size: 0.9em; }
</style>
</head>
<body>
  <h1>注文履歴（ユーザーID: <%= h(String.valueOf(userId)) %>）</h1>
  <p class="note">動作確認用のページです。URLの userId を変えると、別のユーザーの履歴が見られます。</p>

<% if (orders == null || orders.isEmpty()) { %>
  <p>注文履歴はありません。</p>
<% } else { %>
  <% for (Order o : orders) { %>
  <div class="order">
    <h2>注文番号 <%= o.getOrderId() %>
      （<%= o.getOrderAt() == null ? "" : o.getOrderAt().toString().replace('T', ' ') %>）
      ／ <%= h(statusLabel(o.getStatus())) %></h2>
    <table>
      <tr><th>商品</th><th class="num">単価</th><th class="num">数量</th><th class="num">小計</th></tr>
      <% for (OrderDetail d : o.getDetails()) { %>
      <tr>
        <td><%= h(d.getProductName()) %></td>
        <td class="num"><%= yen(d.getUnitPrice()) %> 円</td>
        <td class="num"><%= d.getQuantity() %></td>
        <td class="num"><%= yen(d.getSubtotal()) %> 円</td>
      </tr>
      <% } %>
    </table>
    <p><strong>合計: <%= yen(o.getTotalAmount()) %> 円</strong></p>
  </div>
  <% } %>
<% } %>
</body>
</html>