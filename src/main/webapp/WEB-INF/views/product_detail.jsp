<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>商品詳細</title>

<link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    rel="stylesheet">

<style>

body{
    background:#f5f7fa;
}

/* 商品画像 */
.product-image-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

.product-image{
    width:100%;
    height:500px;
    object-fit:contain;
    padding:20px;
}

/* 商品情報 */
.product-info-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

/* 価格 */
.product-price{
    color:#dc3545;
    font-size:2rem;
    font-weight:bold;
}

/* 発送情報 */
.shipping-box{
    background:#f8f9fa;
    border:1px solid #dee2e6;
    border-radius:8px;
    padding:12px;
}

/* 商品説明 */
.description-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

/* レビュー */
.review-card{
    border:none;
    box-shadow:0 1px 6px rgba(0,0,0,.08);
}

/* レビュー投稿フォーム */
.review-form-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

/* カートボタン */
.cart-btn{
    height:55px;
    font-size:18px;
    font-weight:bold;
}

</style>

</head>

<body>

<div class="container my-5">

    <!-- 戻るボタン -->
    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/products"
           class="btn btn-outline-secondary">
            ← 商品一覧へ戻る
        </a>
    </div>

    <!-- 商品詳細 -->
    <div class="row g-4">

        <!-- 左：商品画像 -->
        <div class="col-lg-7">

            <div class="product-image-card">

                <c:if test="${not empty product.imagePath}">
                    <img
                        src="${pageContext.request.contextPath}/${product.imagePath}"
                        class="product-image"
                        alt="<c:out value='${product.productName}'/>"
                        onerror="this.style.display='none'">
                </c:if>

            </div>

        </div>

        <!-- 右：商品情報 -->
        <div class="col-lg-5">

            <div class="product-info-card p-4 h-100">

                <h1 class="fw-bold mb-3">
                    <c:out value="${product.productName}" />
                </h1>

                <p class="text-muted mb-4">
                    販売会社：マルシェオンライン
                </p>

                <div class="product-price mb-3">
                    <fmt:formatNumber
                        value="${product.price}"
                        pattern="#,##0" />
                    円
                </div>

                <div class="mb-3">
                    在庫：
                    <strong>${product.stockCount}</strong>
                </div>

                <div class="shipping-box mb-4">
                    発送予定日：2～3営業日以内
                </div>

                <!-- カート -->
                <form action="${pageContext.request.contextPath}/cart"
                      method="post">

                    <input type="hidden"
                           name="productId"
                           value="${product.productId}">

                    <input type="hidden"
                           name="quantity"
                           value="1">

                    <button type="submit"
                            class="btn btn-primary w-100 cart-btn">
                        カートに入れる
                    </button>

                </form>

            </div>

        </div>

    </div>

    <!-- 商品説明 -->
    <div class="description-card p-4 mt-5">

        <h2 class="h4 fw-bold mb-3">
            商品説明
        </h2>

        <p class="mb-0">
            <c:out value="${product.description}" />
        </p>

    </div>

    <!-- レビュー -->
    <div class="mt-5">

        <h2 class="h4 fw-bold mb-3">
            レビュー（${reviews.size()}件）
        </h2>

        <!-- 投稿結果のメッセージ -->
        <c:if test="${not empty reviewMessage}">
            <div class="alert alert-success">
                <c:out value="${reviewMessage}" />
            </div>
        </c:if>
        <c:if test="${not empty reviewError}">
            <div class="alert alert-danger">
                <c:out value="${reviewError}" />
            </div>
        </c:if>

        <!-- 投稿フォーム（購入済みで配達完了、かつ未レビューの人だけ） -->
        <c:choose>
            <c:when test="${canReview}">
                <div class="review-form-card p-4 mb-4">

                    <h3 class="h5 fw-bold mb-3">
                        この商品のレビューを書く
                    </h3>

                    <form action="${pageContext.request.contextPath}/reviews"
                          method="post">

                        <input type="hidden"
                               name="productId"
                               value="${product.productId}">

                        <div class="mb-3">
                            <label for="rating" class="form-label">評価</label>
                            <select id="rating"
                                    name="rating"
                                    class="form-select w-auto"
                                    required>
                                <option value="">選んでください</option>
                                <option value="5">5（とても良い）</option>
                                <option value="4">4（良い）</option>
                                <option value="3">3（ふつう）</option>
                                <option value="2">2（あまり良くない）</option>
                                <option value="1">1（悪い）</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="comment" class="form-label">コメント（任意・500文字まで）</label>
                            <textarea id="comment"
                                      name="comment"
                                      class="form-control"
                                      rows="4"
                                      maxlength="500"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            投稿する
                        </button>

                        <div class="form-text mt-2">
                            投稿したレビューは、承認されたあとに公開されます。
                        </div>

                    </form>

                </div>
            </c:when>
            <c:when test="${not loggedIn}">
                <div class="alert alert-light border">
                    レビューを書くには
                    <a href="${pageContext.request.contextPath}/login">ログイン</a>
                    してください。
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-light border">
                    レビューは、この商品を購入して配達が完了したあとに書けます。
                </div>
            </c:otherwise>
        </c:choose>

        <!-- レビュー一覧 -->
        <c:if test="${empty reviews}">
            <div class="alert alert-light border">
                まだレビューはありません。
            </div>
        </c:if>

        <c:forEach var="r" items="${reviews}">
            <div class="card review-card mb-3">

                <div class="card-body">

                    <div class="small text-muted mb-2">

                        <c:out value="${r.userName}" />
                        さん

                        ｜ 評価：${r.rating}/5

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

</div>

</body>
</html><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>商品詳細</title>

<link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    rel="stylesheet">

<style>

body{
    background:#f5f7fa;
}

/* 商品画像 */
.product-image-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

.product-image{
    width:100%;
    height:500px;
    object-fit:contain;
    padding:20px;
}

/* 商品情報 */
.product-info-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

/* 価格 */
.product-price{
    color:#dc3545;
    font-size:2rem;
    font-weight:bold;
}

/* 発送情報 */
.shipping-box{
    background:#f8f9fa;
    border:1px solid #dee2e6;
    border-radius:8px;
    padding:12px;
}

/* 商品説明 */
.description-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

/* レビュー */
.review-card{
    border:none;
    box-shadow:0 1px 6px rgba(0,0,0,.08);
}

/* レビュー投稿フォーム */
.review-form-card{
    background:#fff;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,.08);
}

/* カートボタン */
.cart-btn{
    height:55px;
    font-size:18px;
    font-weight:bold;
}

</style>

</head>

<body>

<div class="container my-5">

    <!-- 戻るボタン -->
    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/products"
           class="btn btn-outline-secondary">
            ← 商品一覧へ戻る
        </a>
    </div>

    <!-- 商品詳細 -->
    <div class="row g-4">

        <!-- 左：商品画像 -->
        <div class="col-lg-7">

            <div class="product-image-card">

                <c:if test="${not empty product.imagePath}">
                    <img
                        src="${pageContext.request.contextPath}/${product.imagePath}"
                        class="product-image"
                        alt="<c:out value='${product.productName}'/>"
                        onerror="this.style.display='none'">
                </c:if>

            </div>

        </div>

        <!-- 右：商品情報 -->
        <div class="col-lg-5">

            <div class="product-info-card p-4 h-100">

                <h1 class="fw-bold mb-3">
                    <c:out value="${product.productName}" />
                </h1>

                <p class="text-muted mb-4">
                    販売会社：マルシェオンライン
                </p>

                <div class="product-price mb-3">
                    <fmt:formatNumber
                        value="${product.price}"
                        pattern="#,##0" />
                    円
                </div>

                <div class="mb-3">
                    在庫：
                    <strong>${product.stockCount}</strong>
                </div>

                <div class="shipping-box mb-4">
                    発送予定日：2～3営業日以内
                </div>

                <!-- カート -->
                <form action="${pageContext.request.contextPath}/cart"
                      method="post">

                    <input type="hidden"
                           name="productId"
                           value="${product.productId}">

                    <input type="hidden"
                           name="quantity"
                           value="1">

                    <button type="submit"
                            class="btn btn-primary w-100 cart-btn">
                        カートに入れる
                    </button>

                </form>

            </div>

        </div>

    </div>

    <!-- 商品説明 -->
    <div class="description-card p-4 mt-5">

        <h2 class="h4 fw-bold mb-3">
            商品説明
        </h2>

        <p class="mb-0">
            <c:out value="${product.description}" />
        </p>

    </div>

    <!-- レビュー -->
    <div class="mt-5">

        <h2 class="h4 fw-bold mb-3">
            レビュー（${reviews.size()}件）
        </h2>

        <!-- 投稿結果のメッセージ -->
        <c:if test="${not empty reviewMessage}">
            <div class="alert alert-success">
                <c:out value="${reviewMessage}" />
            </div>
        </c:if>
        <c:if test="${not empty reviewError}">
            <div class="alert alert-danger">
                <c:out value="${reviewError}" />
            </div>
        </c:if>

        <!-- 投稿フォーム（購入済みで配達完了、かつ未レビューの人だけ） -->
        <c:choose>
            <c:when test="${canReview}">
                <div class="review-form-card p-4 mb-4">

                    <h3 class="h5 fw-bold mb-3">
                        この商品のレビューを書く
                    </h3>

                    <form action="${pageContext.request.contextPath}/reviews"
                          method="post">

                        <input type="hidden"
                               name="productId"
                               value="${product.productId}">

                        <div class="mb-3">
                            <label for="rating" class="form-label">評価</label>
                            <select id="rating"
                                    name="rating"
                                    class="form-select w-auto"
                                    required>
                                <option value="">選んでください</option>
                                <option value="5">5（とても良い）</option>
                                <option value="4">4（良い）</option>
                                <option value="3">3（ふつう）</option>
                                <option value="2">2（あまり良くない）</option>
                                <option value="1">1（悪い）</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="comment" class="form-label">コメント（任意・500文字まで）</label>
                            <textarea id="comment"
                                      name="comment"
                                      class="form-control"
                                      rows="4"
                                      maxlength="500"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            投稿する
                        </button>

                        <div class="form-text mt-2">
                            投稿したレビューは、承認されたあとに公開されます。
                        </div>

                    </form>

                </div>
            </c:when>
            <c:when test="${not loggedIn}">
                <div class="alert alert-light border">
                    レビューを書くには
                    <a href="${pageContext.request.contextPath}/login">ログイン</a>
                    してください。
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-light border">
                    レビューは、この商品を購入して配達が完了したあとに書けます。
                </div>
            </c:otherwise>
        </c:choose>

        <!-- レビュー一覧 -->
        <c:if test="${empty reviews}">
            <div class="alert alert-light border">
                まだレビューはありません。
            </div>
        </c:if>

        <c:forEach var="r" items="${reviews}">
            <div class="card review-card mb-3">

                <div class="card-body">

                    <div class="small text-muted mb-2">

                        <c:out value="${r.userName}" />
                        さん

                        ｜ 評価：${r.rating}/5

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

</div>

</body>
</html>