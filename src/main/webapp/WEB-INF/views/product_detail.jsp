<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>商品詳細</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
</head>
<body class="bg-light">

	<div class="container my-5" style="max-width: 720px;">
		<div class="card shadow-sm">
			<c:if test="${not empty product.imagePath}">
				<img src="${pageContext.request.contextPath}/${product.imagePath}"
					class="card-img-top" alt="<c:out value='${product.productName}'/>"
					style="max-height: 320px; object-fit: cover;"
					onerror="this.style.display='none'">
			</c:if>
			<div class="card-body">
				<h1 class="h3 fw-bold">
					<c:out value="${product.productName}" />
				</h1>
				<p class="text-danger fs-4 fw-bold mb-1">${product.price}円</p>
				<p class="text-muted">在庫: ${product.stockCount}</p>
				<p>
					<c:out value="${product.description}" />
				</p>
			</div>
		</div>

		<!-- レビュー一覧（承認済みのみ） -->
		<div class="mt-4">
			<h2 class="h5 fw-bold">レビュー（${reviews.size()}件）</h2>
			<c:if test="${empty reviews}">
				<p class="text-muted">まだレビューはありません。</p>
			</c:if>
			<c:forEach var="r" items="${reviews}">
				<div class="card shadow-sm mb-2">
					<div class="card-body py-2">
						<div class="small text-muted">
							<c:out value="${r.userName}" />
							評価：${r.rating} / 5
						</div>
						<c:if test="${not empty r.comment}">
							<div>
								<c:out value="${r.comment}" />
							</div>
						</c:if>
					</div>
				</div>
			</c:forEach>
		</div>

		<div class="mt-4">
			<a href="${pageContext.request.contextPath}/products"
				class="btn btn-outline-secondary">商品一覧へ戻る</a>
		</div>
	</div>

</body>
</html>