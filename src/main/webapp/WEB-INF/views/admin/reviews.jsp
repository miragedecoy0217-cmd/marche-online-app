<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>レビュー管理</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">レビュー管理 <small class="text-muted fs-6">この表示 ${total} 件</small></h1>
    <div>
      <a href="${pageContext.request.contextPath}/admin/top" class="btn btn-outline-secondary">ダッシュボードへ</a>
    </div>
  </div>

  <!-- 操作の結果メッセージ -->
  <c:if test="${not empty message}">
    <div class="alert alert-info"><c:out value="${message}"/></div>
  </c:if>

  <!-- ステータスの切り替え -->
  <ul class="nav nav-tabs mb-3">
    <li class="nav-item">
      <a class="nav-link ${status == 'pending' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews?status=pending">承認待ち</a>
    </li>
    <li class="nav-item">
      <a class="nav-link ${status == 'approved' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews?status=approved">公開中</a>
    </li>
    <li class="nav-item">
      <a class="nav-link ${status == 'rejected' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews?status=rejected">却下</a>
    </li>
    <li class="nav-item">
      <a class="nav-link ${status == 'hidden' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/reviews?status=hidden">非公開</a>
    </li>
  </ul>

  <!-- レビュー一覧 -->
  <div class="card shadow-sm">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-dark">
        <tr><th>ID</th><th>商品</th><th>投稿者</th><th>評価</th><th>コメント</th><th>ステータス</th><th></th></tr>
      </thead>
      <tbody>
        <c:forEach var="r" items="${reviews}">
          <tr>
            <td>${r.reviewId}</td>
            <td><c:out value="${r.productName}"/></td>
            <td><c:out value="${r.userName}"/></td>
            <td class="text-warning text-nowrap">
              <c:forEach begin="1" end="${r.rating}">★</c:forEach>
              <span class="text-muted small">${r.rating} / 5</span>
            </td>
            <td>
              <c:choose>
                <c:when test="${not empty r.comment}"><c:out value="${r.comment}"/></c:when>
                <c:otherwise><span class="text-muted">（コメントなし）</span></c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:choose>
                <c:when test="${r.status == 'pending'}"><span class="badge bg-secondary">承認待ち</span></c:when>
                <c:when test="${r.status == 'approved'}"><span class="badge bg-success">公開中</span></c:when>
                <c:when test="${r.status == 'rejected'}"><span class="badge bg-danger">却下</span></c:when>
                <c:when test="${r.status == 'hidden'}"><span class="badge bg-dark">非公開</span></c:when>
                <c:otherwise><c:out value="${r.status}"/></c:otherwise>
              </c:choose>
            </td>
            <td class="text-end text-nowrap">
              <!-- ステータスごとに、押せるボタンを変える -->
              <c:if test="${r.status == 'pending' || r.status == 'hidden'}">
                <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                  <input type="hidden" name="action" value="approve">
                  <input type="hidden" name="id" value="${r.reviewId}">
                  <input type="hidden" name="back" value="status=${status}&page=${page}">
                  <button type="submit" class="btn btn-sm btn-outline-success">${r.status == 'hidden' ? '公開する' : '承認'}</button>
                </form>
              </c:if>
              <c:if test="${r.status == 'pending'}">
                <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                  <input type="hidden" name="action" value="reject">
                  <input type="hidden" name="id" value="${r.reviewId}">
                  <input type="hidden" name="back" value="status=${status}&page=${page}">
                  <button type="submit" class="btn btn-sm btn-outline-warning">却下</button>
                </form>
              </c:if>
              <c:if test="${r.status == 'approved'}">
                <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                  <input type="hidden" name="action" value="hide">
                  <input type="hidden" name="id" value="${r.reviewId}">
                  <input type="hidden" name="back" value="status=${status}&page=${page}">
                  <button type="submit" class="btn btn-sm btn-outline-secondary">非公開</button>
                </form>
              </c:if>
              <c:if test="${r.status == 'approved' || r.status == 'rejected' || r.status == 'hidden'}">
                <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                  <input type="hidden" name="action" value="pending">
                  <input type="hidden" name="id" value="${r.reviewId}">
                  <input type="hidden" name="back" value="status=${status}&page=${page}">
                  <button type="submit" class="btn btn-sm btn-outline-primary">承認待ちに戻す</button>
                </form>
              </c:if>
              <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline"
                    onsubmit="return confirm('このレビューを削除します。よろしいですか？');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${r.reviewId}">
                <input type="hidden" name="back" value="status=${status}&page=${page}">
                <button type="submit" class="btn btn-sm btn-outline-danger">削除</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty reviews}">
          <tr><td colspan="7" class="text-center text-muted py-4">該当するレビューはありません。</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <c:if test="${totalPages > 1}">
    <nav class="mt-3">
      <ul class="pagination justify-content-center mb-0">
        <li class="page-item ${page <= 1 ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/reviews?status=<c:out value='${status}'/>&page=${page - 1}">前へ</a>
        </li>
        <c:forEach var="i" begin="1" end="${totalPages}">
          <li class="page-item ${i == page ? 'active' : ''}">
            <a class="page-link" href="${pageContext.request.contextPath}/admin/reviews?status=<c:out value='${status}'/>&page=${i}">${i}</a>
          </li>
        </c:forEach>
        <li class="page-item ${page >= totalPages ? 'disabled' : ''}">
          <a class="page-link" href="${pageContext.request.contextPath}/admin/reviews?status=<c:out value='${status}'/>&page=${page + 1}">次へ</a>
        </li>
      </ul>
    </nav>
  </c:if>
</div>
</body>
</html>