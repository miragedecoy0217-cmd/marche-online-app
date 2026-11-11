<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>商品管理</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">商品管理 <small class="text-muted fs-6">全 ${total} 件</small></h1>
    <div>
      <a href="${pageContext.request.contextPath}/admin/top" class="btn btn-outline-secondary">ダッシュボードへ</a>
      <a href="${pageContext.request.contextPath}/admin/products?action=new" class="btn btn-primary">＋ 商品を登録</a>
    </div>
  </div>

  <div class="card shadow-sm">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-dark">
        <tr><th>ID</th><th>画像</th><th>商品名</th><th class="text-end">価格</th><th class="text-end">在庫</th><th></th></tr>
      </thead>
      <tbody>
        <c:forEach var="p" items="${products}">
          <tr>
            <td>${p.productId}</td>
            <td>
              <c:if test="${not empty p.imagePath}">
                <img src="${pageContext.request.contextPath}/${p.imagePath}" alt="" style="width:56px;height:56px;object-fit:cover;" class="rounded">
              </c:if>
            </td>
            <td><c:out value="${p.productName}"/></td>
            <td class="text-end">¥<c:out value="${p.price}"/></td>
            <td class="text-end">${p.stockCount}</td>
            <td class="text-end">
              <a href="${pageContext.request.contextPath}/admin/products?action=edit&id=${p.productId}" class="btn btn-sm btn-outline-primary">編集</a>
              <form action="${pageContext.request.contextPath}/admin/products" method="post" class="d-inline"
                    onsubmit="return confirm('この商品を削除します。よろしいですか？');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${p.productId}">
                <button type="submit" class="btn btn-sm btn-outline-danger">削除</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty products}">
          <tr><td colspan="6" class="text-center text-muted py-4">商品がまだ登録されていません。</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <c:if test="${totalPages > 1}">
    <nav class="mt-3">
      <ul class="pagination justify-content-center mb-0">
        <li class="page-item ${page <= 1 ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/products?page=${page - 1}">前へ</a>
        </li>
        <c:forEach var="i" begin="1" end="${totalPages}">
          <li class="page-item ${i == page ? 'active' : ''}">
            <a class="page-link" href="${pageContext.request.contextPath}/admin/products?page=${i}">${i}</a>
          </li>
        </c:forEach>
        <li class="page-item ${page >= totalPages ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/products?page=${page + 1}">次へ</a>
        </li>
      </ul>
    </nav>
  </c:if>
</div>
</body>
</html>