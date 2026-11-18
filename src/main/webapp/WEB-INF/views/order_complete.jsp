<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>注文完了 - Marche Online</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container" style="text-align: center; margin-top: 50px;">
        <h1>ご注文ありがとうございました！</h1>
        <p>ご注文の受け付けが正常に完了いたしました。</p>
        
        <c:if test="${not empty orderId}">
            <p>ご注文番号: <strong>#${orderId}</strong></p>
        </c:if>

        <div style="margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/" class="btn" style="padding: 10px 20px; text-decoration: none; border: 1px solid #333; background: #f9f9f9; color: #333;">トップページに戻る</a>
        </div>
    </div>
</body>
</html>