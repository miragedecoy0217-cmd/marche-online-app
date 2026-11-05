<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>商品一覧</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container my-5">
    <h1 class="mb-4 fw-bold text-center">商品一覧</h1>

    <!-- 検索・カテゴリフォーム -->
    <form method="get" action="${pageContext.request.contextPath}/products" class="row g-3 mb-5 align-items-center bg-white p-3 rounded shadow-sm">
        <div class="col-md-4">
            <input type="text" name="keyword" class="form-control" value="<c:out value='${keyword}'/>" placeholder="キーワード">
        </div>
        <div class="col-md-4">
            <select name="category_id" class="form-select">
                <option value="">すべてのカテゴリ</option>
                <c:forEach var="c" items="${categories}">
                     <option value="${c.categoryId}" ${c.categoryId == categoryId ? 'selected' : ''}><c:out value="${c.categoryName}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-4">
            <button type="submit" class="btn btn-primary w-100">検索</button>
        </div>
    </form>

    <!-- 件数表示 -->
    <div class="mb-3">
        <p class="text-muted mb-0">${total}件 / ${page} / ${totalPages} ページ</p>
    </div>

    <!-- 商品カード -->
    <div class="row row-cols-1 row-cols-md-3 row-cols-lg-4 g-4">
        <c:forEach var="p" items="${products}">
            <div class="col">
                <div class="card h-100 shadow-sm">
                    <!-- 画像は登録されている商品だけ表示 -->
                    <c:if test="${not empty p.imagePath}">
                        <img src="${pageContext.request.contextPath}/${p.imagePath}" class="card-img-top" alt="<c:out value='${p.productName}'/>" style="height: 180px; object-fit: cover;" onerror="this.style.display='none'">
                    </c:if>

                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title fw-bold"><c:out value="${p.productName}"/></h5>
                        <p class="card-text text-danger fs-5 fw-bold mb-1">${p.price}円</p>
                        <p class="card-text text-muted small mb-3">在庫: ${p.stockCount}</p>

                        <a href="${pageContext.request.contextPath}/products/${p.productId}" class="btn btn-outline-primary mt-auto">詳細を見る</a>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- ページネーション -->
    <div class="d-flex justify-content-center gap-2 mt-5">
        <c:if test="${page > 1}">
            <c:url var="prevUrl" value="/products">
                <c:param name="page" value="${page - 1}"/>
                <c:param name="keyword" value="${keyword}"/>
                <c:param name="category_id" value="${categoryId}"/>
            </c:url>
            <a href="${prevUrl}" class="btn btn-secondary">← 前へ</a>
        </c:if>

        <c:if test="${page < totalPages}">
            <c:url var="nextUrl" value="/products">
                <c:param name="page" value="${page + 1}"/>
                <c:param name="keyword" value="${keyword}"/>
                <c:param name="category_id" value="${categoryId}"/>
            </c:url>
            <a href="${nextUrl}" class="btn btn-secondary">次へ →</a>
        </c:if>
    </div>

    <!-- ECサイトのトップ（/）へ戻る -->
    <div class="text-center mt-4">
       <a href="/">トップへ戻る</a>
    </div>
</div>

</body>
</html>