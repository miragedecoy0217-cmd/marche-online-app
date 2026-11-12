<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>マルシェオンライン</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .site-header { background: #1e2d42; }
        .btn-accent { background: #c84c24; border-color: #c84c24; color: #fff; }
        .btn-accent:hover { background: #a63e1c; border-color: #a63e1c; color: #fff; }
        .hero { background: linear-gradient(135deg, #1e2d42 0%, #3b5b7f 60%, #c84c24 100%); color: #fff; }
    </style>
</head>
<body class="bg-light">

<!-- ヘッダー -->
<nav class="site-header navbar navbar-dark">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${ctx}/">マルシェオンライン</a>
        <div class="d-flex align-items-center gap-2">
            <c:choose>
                <c:when test="${not empty loginAdmin}">
                    <span class="text-white small"><c:out value="${loginAdmin.name}"/> さん</span>
                    <a class="btn btn-outline-light btn-sm" href="${ctx}/admin/top">管理画面</a>
                    <a class="btn btn-outline-light btn-sm" href="${ctx}/logout">ログアウト</a>
                </c:when>
                <c:when test="${not empty loginUser}">
                    <span class="text-white small"><c:out value="${loginUser.name}"/> さん</span>
                    <a class="btn btn-outline-light btn-sm" href="${ctx}/logout">ログアウト</a>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-outline-light btn-sm" href="${ctx}/login">ログイン</a>
                    <a class="btn btn-accent btn-sm" href="${ctx}/register">新規登録</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</nav>

<!-- バナー -->
<section class="hero py-5">
    <div class="container text-center py-4">
        <h1 class="display-5 fw-bold">旬の味を、お手元に。</h1>
        <p class="lead mb-4">ちいさなお店のこだわり商品が集まるオンラインマルシェ</p>
        <a href="${ctx}/products" class="btn btn-accent btn-lg">商品一覧を見る</a>
    </div>
</section>

<div class="container my-5">

    <!-- カテゴリ一覧 -->
    <h2 class="h4 fw-bold mb-3">カテゴリ</h2>
    <div class="d-flex flex-wrap gap-2 mb-5">
        <c:forEach var="cat" items="${categories}">
            <a class="btn btn-outline-primary" href="${ctx}/products?category_id=${cat.categoryId}"><c:out value="${cat.categoryName}"/></a>
        </c:forEach>
        <c:if test="${empty categories}">
            <span class="text-muted">カテゴリはまだありません。</span>
        </c:if>
    </div>

    <!-- 新着商品 -->
    <div class="d-flex justify-content-between align-items-end mb-3">
        <h2 class="h4 fw-bold mb-0">新着商品</h2>
        <a href="${ctx}/products">すべての商品を見る →</a>
    </div>

    <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-4">
        <c:forEach var="p" items="${newProducts}">
            <div class="col">
                <div class="card h-100 shadow-sm">
                    <c:if test="${not empty p.imagePath}">
                        <img src="${ctx}/${p.imagePath}" class="card-img-top" alt="<c:out value='${p.productName}'/>" style="height: 160px; object-fit: cover;" onerror="this.style.display='none'">
                    </c:if>
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title fw-bold fs-6"><c:out value="${p.productName}"/></h5>
                        <p class="card-text text-danger fw-bold mb-3"><fmt:formatNumber value="${p.price}" pattern="#,##0" />円</p>
                        <a href="${ctx}/products/${p.productId}" class="btn btn-outline-primary btn-sm mt-auto">詳細を見る</a>
                    </div>
                </div>
            </div>
        </c:forEach>
        <c:if test="${empty newProducts}">
            <div class="col-12"><p class="text-muted">商品がまだ登録されていません。</p></div>
        </c:if>
    </div>
</div>

<footer class="text-center text-muted small py-4">&copy; マルシェオンライン</footer>

</body>
</html>