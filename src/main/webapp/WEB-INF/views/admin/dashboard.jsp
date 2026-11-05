<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>管理ダッシュボード</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
  <div class="container py-5">
    <h1>管理ダッシュボード</h1>
    <p>ようこそ、<c:out value="${loginAdmin.name}"/> さん（${loginAdmin.role}）</p>
    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-primary">商品管理へ</a>
  </div>
</body>
</html>