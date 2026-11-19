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

    <!-- 役割ごとに、開ける画面のボタンだけを出す（superadmin はすべて） -->
    <div class="d-flex flex-wrap gap-2">
      <c:if test="${loginAdmin.role == 'superadmin' || loginAdmin.role == 'product'}">
        <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-primary">商品管理へ</a>
      </c:if>
      <c:if test="${loginAdmin.role == 'superadmin' || loginAdmin.role == 'order'}">
        <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-primary">注文管理へ</a>
      </c:if>
      <c:if test="${loginAdmin.role == 'superadmin' || loginAdmin.role == 'inventory'}">
        <a href="${pageContext.request.contextPath}/admin/stock" class="btn btn-primary">在庫管理へ</a>
      </c:if>
    </div>
  </div>
</body>
</html>