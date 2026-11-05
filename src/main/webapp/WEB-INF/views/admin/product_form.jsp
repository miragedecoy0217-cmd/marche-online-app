<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${product.productId > 0}" />
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>商品<c:out value="${isEdit ? '編集' : '登録'}"/></title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4" style="max-width:680px;">
  <h1 class="h3 mb-3">商品<c:out value="${isEdit ? '編集' : '登録'}"/></h1>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
  </c:if>

  <form action="${pageContext.request.contextPath}/admin/products" method="post"
        enctype="multipart/form-data" class="card card-body shadow-sm">
    <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
    <c:if test="${isEdit}"><input type="hidden" name="id" value="${product.productId}"></c:if>

    <div class="mb-3">
      <label class="form-label">商品名</label>
      <input type="text" name="productName" class="form-control" value="<c:out value='${product.productName}'/>" required maxlength="100">
    </div>

    <div class="mb-3">
      <label class="form-label">カテゴリ</label>
      <select name="categoryId" class="form-select" required>
        <option value="">選択してください</option>
        <c:forEach var="c" items="${categories}">
          <option value="${c.categoryId}" ${c.categoryId == product.categoryId ? 'selected' : ''}>
            <c:out value="${c.categoryName}"/>
          </option>
        </c:forEach>
      </select>
    </div>

    <div class="mb-3">
      <label class="form-label">説明</label>
      <textarea name="description" class="form-control" rows="4"><c:out value="${product.description}"/></textarea>
    </div>

    <div class="row">
      <div class="col mb-3">
        <label class="form-label">価格（円）</label>
        <input type="number" name="price" class="form-control" min="0" step="1" value="${product.price}" required>
      </div>
      <div class="col mb-3">
        <label class="form-label">在庫数</label>
        <input type="number" name="stockCount" class="form-control" min="0" value="${product.stockCount}" required>
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">商品画像（jpg / png / gif / webp、5MBまで）</label>
      <c:if test="${not empty product.imagePath}">
        <div class="mb-2">
          <img src="${pageContext.request.contextPath}/${product.imagePath}" alt="" style="max-height:120px;" class="rounded border">
          <div class="form-text">変更しない場合は、ファイルを選ばなくてOKです。</div>
        </div>
      </c:if>
      <input type="file" name="image" class="form-control" accept="image/*">
    </div>

    <div class="d-flex gap-2">
      <button type="submit" class="btn btn-primary">保存</button>
      <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">キャンセル</a>
    </div>
  </form>
</div>
</body>
</html>