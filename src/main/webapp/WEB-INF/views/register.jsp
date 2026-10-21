<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>新規会員登録 - Marche Online</title>
<style>
    body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f5f5f5; }
    .register-box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 320px; }
    .register-box h2 { text-align: center; margin-bottom: 20px; color: #333; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; margin-bottom: 5px; color: #666; }
    .form-group input { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
    .btn-submit { width: 100%; padding: 10px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
    .btn-submit:hover { background-color: #0056b3; }
    .error-msg { color: #dc3545; margin-bottom: 15px; font-size: 14px; text-align: center; }
    .login-link { text-align: center; margin-top: 15px; font-size: 14px; }
    .login-link a { color: #007bff; text-decoration: none; }
    .login-link a:hover { text-decoration: underline; }
</style>
</head>
<body>

<div class="register-box">
    <h2>新規会員登録</h2>
    
    <%-- 登録失敗時のエラーメッセージを表示 --%>
    <c:if test="${not empty errorMessage}">
        <div class="error-msg">${errorMessage}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="post">
        <div class="form-group">
            <label for="name">氏名</label>
            <input type="text" id="name" name="name" required>
        </div>

        <div class="form-group">
            <label for="email">メールアドレス</label>
            <input type="email" id="email" name="email" required>
        </div>
        
        <div class="form-group">
            <label for="password">パスワード</label>
            <input type="password" id="password" name="password" required>
        </div>
        
        <button type="submit" class="btn-submit">登録する</button>
    </form>
    
    <div class="login-link">
        <a href="${pageContext.request.contextPath}/login">すでにアカウントをお持ちの方はこちら</a>
    </div>
</div>

</body>
</html>