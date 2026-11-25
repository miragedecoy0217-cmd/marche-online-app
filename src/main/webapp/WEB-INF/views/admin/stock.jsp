<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>在庫管理</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">在庫管理 <small class="text-muted fs-6">全 ${total} 件</small></h1>
    <div>
      <a href="${pageContext.request.contextPath}/admin/top" class="btn btn-outline-secondary">ダッシュボードへ</a>
    </div>
  </div>

  <!-- 調整の結果メッセージ -->
  <c:if test="${not empty message}">
    <div class="alert alert-info"><c:out value="${message}"/></div>
  </c:if>

  <p class="text-muted small">
    在庫が ${lowStock} 個以下の商品は黄色、0 個は「売り切れ」と表示します。
    数字を入れて「調整」を押すと、在庫をその数にそろえ、履歴に記録します。
  </p>

  <!-- 在庫一覧 -->
  <div class="card shadow-sm">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-dark">
        <tr><th>ID</th><th>商品名</th><th class="text-end">在庫</th><th></th><th>在庫を調整</th></tr>
      </thead>
      <tbody>
        <c:forEach var="p" items="${products}">
          <tr class="${p.stockCount > 0 && p.stockCount <= lowStock ? 'table-warning' : ''}">
            <td>${p.productId}</td>
            <td><c:out value="${p.productName}"/></td>
            <td class="text-end">${p.stockCount}</td>
            <td>
              <c:choose>
                <c:when test="${p.stockCount <= 0}"><span class="badge bg-danger">売り切れ</span></c:when>
                <c:when test="${p.stockCount <= lowStock}"><span class="badge bg-warning text-dark">残りわずか</span></c:when>
              </c:choose>
            </td>
            <td>
              <form action="${pageContext.request.contextPath}/admin/stock" method="post" class="d-inline-flex gap-1">
                <input type="hidden" name="id" value="${p.productId}">
                <input type="hidden" name="back" value="page=${page}">
                <input type="number" name="stock" value="${p.stockCount}" min="0" max="${maxStock}"
                       class="form-control form-control-sm" style="width:100px;" required>
                <button type="submit" class="btn btn-sm btn-outline-primary">調整</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty products}">
          <tr><td colspan="5" class="text-center text-muted py-4">商品がまだ登録されていません。</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <c:if test="${totalPages > 1}">
    <nav class="mt-3">
      <ul class="pagination justify-content-center mb-0">
        <li class="page-item ${page <= 1 ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/stock?page=${page - 1}">前へ</a>
        </li>
        <c:forEach var="i" begin="1" end="${totalPages}">
          <li class="page-item ${i == page ? 'active' : ''}">
            <a class="page-link" href="${pageContext.request.contextPath}/admin/stock?page=${i}">${i}</a>
          </li>
        </c:forEach>
        <li class="page-item ${page >= totalPages ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/stock?page=${page + 1}">次へ</a>
        </li>
      </ul>
    </nav>
  </c:if>

  <!-- 在庫の履歴（直近） -->
  <h2 class="h5 mt-5 mb-2">在庫の履歴（直近）</h2>
  <div class="card shadow-sm">
    <table class="table table-sm align-middle mb-0">
      <thead class="table-light">
        <tr><th>日時</th><th>商品</th><th class="text-end">増減</th><th>理由</th><th>担当</th><th>注文</th></tr>
      </thead>
      <tbody>
        <c:forEach var="h" items="${history}">
          <tr>
            <td><c:out value="${fn:substring(fn:replace(h.createdAt, 'T', ' '), 0, 16)}"/></td>
            <td><c:out value="${h.productName}"/></td>
            <td class="text-end">
              <c:choose>
                <c:when test="${h.changeQty > 0}"><span class="text-success">+${h.changeQty}</span></c:when>
                <c:otherwise><span class="text-danger">${h.changeQty}</span></c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:choose>
                <c:when test="${h.reason == 'purchase'}">入荷</c:when>
                <c:when test="${h.reason == 'order'}">注文</c:when>
                <c:when test="${h.reason == 'cancel'}">キャンセル戻し</c:when>
                <c:when test="${h.reason == 'adjustment'}">調整</c:when>
                <c:when test="${h.reason == 'loss'}">廃棄・ロス</c:when>
                <c:otherwise><c:out value="${h.reason}"/></c:otherwise>
              </c:choose>
            </td>
            <td><c:out value="${empty h.adminName ? '―' : h.adminName}"/></td>
            <td><c:if test="${not empty h.orderId}">#${h.orderId}</c:if></td>
          </tr>
        </c:forEach>
        <c:if test="${empty history}">
          <tr><td colspan="6" class="text-center text-muted py-3">履歴はまだありません。</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>
</body>
</html>