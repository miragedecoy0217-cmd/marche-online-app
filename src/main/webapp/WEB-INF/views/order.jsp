<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>注文確認 - Marche Online</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <h1>ご注文内容の確認</h1>

        <!-- エラーメッセージの表示 -->
        <c:if test="${not empty errorMessage}">
            <p class="error-message" style="color: red; font-weight: bold;">${errorMessage}</p>
        </c:if>

        <h2>1. 注文商品一覧</h2>
        <table border="1" cellpadding="8" cellspacing="0">
            <thead>
                <tr>
                    <th>商品名</th>
                    <th>価格</th>
                    <th>数量</th>
                    <th>小計</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${cartItems}">
                    <tr>
                        <td>${item.productName}</td>
                        <td>&yen;${item.price}</td>
                        <td>${item.quantity}</td>
                        <td>&yen;${item.subtotal}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        <h3>合計金額: &yen;${totalAmount}</h3>

        <!-- 注文確定フォーム (POST /order/confirm) -->
        <form action="${pageContext.request.contextPath}/order/confirm" method="post">
            <!-- CSRFトークン -->
            <input type="hidden" name="csrf_token" value="${csrfToken}">

            <h2>2. 配送先住所IDの入力</h2>
            <p>
                <label for="address_id">配送先ID:</label>
                <input type="text" id="address_id" name="address_id" required placeholder="例: 1">
            </p>

            <h2>3. お支払い方法の選択</h2>
            <p>
                <input type="radio" id="credit" name="payment_method" value="credit" checked>
                <label for="credit">クレジットカード</label>
                <br>
                <input type="radio" id="transfer" name="payment_method" value="transfer">
                <label for="transfer">銀行振込</label>
            </p>

            <div class="actions" style="margin-top: 20px;">
                <a href="${pageContext.request.contextPath}/cart" class="btn">カートに戻る</a>
                <button type="submit" class="btn-submit" style="padding: 10px 20px; font-weight: bold;">注文を確定する</button>
            </div>
        </form>
    </div>
</body>
</html>