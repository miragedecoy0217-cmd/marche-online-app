<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>注文管理</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">注文管理 <small class="text-muted fs-6">全 ${total} 件</small></h1>
    <div>
      <a href="${pageContext.request.contextPath}/admin/top" class="btn btn-outline-secondary">ダッシュボードへ</a>
    </div>
  </div>

  <!-- 更新・キャンセルの結果メッセージ -->
  <c:if test="${not empty message}">
    <div class="alert alert-info"><c:out value="${message}"/></div>
  </c:if>

  <!-- 絞り込み -->
  <form method="get" action="${pageContext.request.contextPath}/admin/orders"
        class="row g-2 align-items-end mb-3 bg-white p-3 rounded shadow-sm">
    <div class="col-md-3">
      <label class="form-label small mb-1">ステータス</label>
      <select name="status" class="form-select">
        <option value="">すべて</option>
        <option value="pending"   ${status == 'pending'   ? 'selected' : ''}>注文受付</option>
        <option value="confirmed" ${status == 'confirmed' ? 'selected' : ''}>確認済み</option>
        <option value="shipped"   ${status == 'shipped'   ? 'selected' : ''}>発送済み</option>
        <option value="delivered" ${status == 'delivered' ? 'selected' : ''}>配達完了</option>
        <option value="cancelled" ${status == 'cancelled' ? 'selected' : ''}>キャンセル</option>
        <option value="refunded"  ${status == 'refunded'  ? 'selected' : ''}>返金済み</option>
      </select>
    </div>
    <div class="col-md-3">
      <label class="form-label small mb-1">注文日（から）</label>
      <input type="date" name="date_from" value="<c:out value='${dateFrom}'/>" class="form-control">
    </div>
    <div class="col-md-3">
      <label class="form-label small mb-1">注文日（まで）</label>
      <input type="date" name="date_to" value="<c:out value='${dateTo}'/>" class="form-control">
    </div>
    <div class="col-md-3 d-flex gap-2">
      <button type="submit" class="btn btn-primary">検索</button>
      <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-outline-secondary">クリア</a>
    </div>
  </form>

  <!-- 注文一覧 -->
  <div class="card shadow-sm">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-dark">
        <tr>
          <th>注文ID</th><th>注文日時</th><th>ユーザーID</th>
          <th class="text-end">合計</th><th>支払方法</th><th>ステータス</th><th></th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="o" items="${orders}">
          <tr>
            <td>${o.orderId}</td>
            <td><c:out value="${fn:substring(fn:replace(o.orderAt, 'T', ' '), 0, 16)}"/></td>
            <td>${o.userId}</td>
            <td class="text-end">¥<fmt:formatNumber value="${o.totalAmount}" pattern="#,##0" /></td>
            <td><c:out value="${o.paymentMethod}"/></td>
            <td>
              <c:choose>
                <c:when test="${o.status == 'pending'}"><span class="badge bg-secondary">注文受付</span></c:when>
                <c:when test="${o.status == 'confirmed'}"><span class="badge bg-info text-dark">確認済み</span></c:when>
                <c:when test="${o.status == 'shipped'}"><span class="badge bg-primary">発送済み</span></c:when>
                <c:when test="${o.status == 'delivered'}"><span class="badge bg-success">配達完了</span></c:when>
                <c:when test="${o.status == 'cancelled'}"><span class="badge bg-danger">キャンセル</span></c:when>
                <c:when test="${o.status == 'refunded'}"><span class="badge bg-warning text-dark">返金済み</span></c:when>
                <c:otherwise><c:out value="${o.status}"/></c:otherwise>
              </c:choose>
              <c:if test="${not empty o.cancelReason}">
                <div class="small text-muted"><c:out value="${o.cancelReason}"/></div>
              </c:if>
            </td>
            <td class="text-end">
              <!-- ステータス変更（キャンセル・返金済みは変更できない） -->
              <c:if test="${o.status != 'cancelled' && o.status != 'refunded'}">
                <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="d-inline-flex gap-1">
                  <input type="hidden" name="action" value="status">
                  <input type="hidden" name="id" value="${o.orderId}">
                  <input type="hidden" name="back" value="<c:out value='${queryBase}'/>page=${page}">
                  <select name="status" class="form-select form-select-sm" style="width:auto;">
                    <option value="pending"   ${o.status == 'pending'   ? 'selected' : ''}>注文受付</option>
                    <option value="confirmed" ${o.status == 'confirmed' ? 'selected' : ''}>確認済み</option>
                    <option value="shipped"   ${o.status == 'shipped'   ? 'selected' : ''}>発送済み</option>
                    <option value="delivered" ${o.status == 'delivered' ? 'selected' : ''}>配達完了</option>
                  </select>
                  <button type="submit" class="btn btn-sm btn-outline-primary">変更</button>
                </form>
              </c:if>

              <!-- キャンセル（注文受付・確認済みのみ。在庫も戻る） -->
              <c:if test="${o.status == 'pending' || o.status == 'confirmed'}">
                <form action="${pageContext.request.contextPath}/admin/orders" method="post" class="d-inline">
                  <input type="hidden" name="action" value="cancel">
                  <input type="hidden" name="id" value="${o.orderId}">
                  <input type="hidden" name="back" value="<c:out value='${queryBase}'/>page=${page}">
                  <input type="hidden" name="reason" value="">
                  <button type="submit" class="btn btn-sm btn-outline-danger"
                          onclick="var r = prompt('キャンセル理由を入力してください（空のままでもOK）'); if (r === null) { return false; } this.form.reason.value = r; return confirm('この注文をキャンセルし、在庫を戻します。よろしいですか？');">キャンセル</button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty orders}">
          <tr><td colspan="7" class="text-center text-muted py-4">該当する注文はありません。</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <!-- ページ移動（絞り込み条件を引き継ぐ） -->
  <c:if test="${totalPages > 1}">
    <nav class="mt-3">
      <ul class="pagination justify-content-center mb-0">
        <li class="page-item ${page <= 1 ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/orders?<c:out value='${queryBase}'/>page=${page - 1}">前へ</a>
        </li>
        <c:forEach var="i" begin="1" end="${totalPages}">
          <li class="page-item ${i == page ? 'active' : ''}">
            <a class="page-link" href="${pageContext.request.contextPath}/admin/orders?<c:out value='${queryBase}'/>page=${i}">${i}</a>
          </li>
        </c:forEach>
        <li class="page-item ${page >= totalPages ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/orders?<c:out value='${queryBase}'/>page=${page + 1}">次へ</a>
        </li>
      </ul>
    </nav>
  </c:if>
</div>
</body>
</html>