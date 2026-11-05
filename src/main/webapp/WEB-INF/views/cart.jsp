<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ショッピングカート - Marche Online</title>
    <style>
        body { font-family: sans-serif; margin: 20px; background-color: #f9f9f9; }
        .container { max-width: 800px; margin: 0 auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h1 { color: #333; border-bottom: 2px solid #eee; padding-bottom: 10px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f4f4f4; }
        .total-box { text-align: right; font-size: 1.2em; font-weight: bold; margin-top: 20px; }
        .btn { padding: 6px 12px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; }
        .btn-danger { background-color: #dc3545; }
        .btn:hover { opacity: 0.9; }
        .actions { display: flex; gap: 10px; align-items: center; }
        .empty-msg { text-align: center; color: #666; margin: 40px 0; }
        .nav-links { margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>ショッピングカート</h1>

        <c:choose>
            <c:when test="${empty cartItems}">
                <div class="empty-msg">
                    <p>カートに商品は入っていません。</p>
                </div>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                        <tr>
                            <th>商品名</th>
                            <th>価格</th>
                            <th>数量</th>
                            <th>小計</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${cartItems}">
                            <tr>
                                <td>${item.productName}</td>
                                <td>&yen;${item.price}</td>
                                <td>
                                    <!-- 数量変更フォーム (POST /cart/update/{id}) -->
                                    <form action="${pageContext.request.contextPath}/cart/update/${item.productId}" method="post" style="display:inline;">
                                        <input type="hidden" name="csrf_token" value="${csrfToken}">
                                        <input type="number" name="quantity" value="${item.quantity}" min="1" max="${item.stock}" style="width: 60px;">
                                        <button type="submit" class="btn">変更</button>
                                    </form>
                                </td>
                                <td>&yen;${item.subtotal}</td>
                                <td>
                                    <!-- 商品削除フォーム (POST /cart/remove/{id}) -->
                                    <form action="${pageContext.request.contextPath}/cart/remove/${item.productId}" method="post" style="display:inline;">
                                        <input type="hidden" name="csrf_token" value="${csrfToken}">
                                        <button type="submit" class="btn btn-danger">削除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <div class="total-box">
                    合計金額: &yen;${totalAmount}
                </div>
            </c:otherwise>
        </c:choose>

        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/products" class="btn" style="background-color: #6c757d;">商品一覧へ戻る</a>
        </div>
    </div>
</body>
</html>