<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>エラー - マルシェオンライン</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5 text-center" style="max-width: 640px;">
    <c:if test="${errorCode > 0}">
        <div class="display-1 fw-bold text-secondary">${errorCode}</div>
    </c:if>
    <h1 class="h3 fw-bold mb-3"><c:out value="${errorTitle}"/></h1>
    <p class="text-muted mb-4"><c:out value="${errorMessage}"/></p>

    <a href="${ctx}/" class="btn btn-primary">トップへ戻る</a>
    <a href="${ctx}/products" class="btn btn-outline-secondary">商品一覧へ</a>
</div>

</body>
</html>