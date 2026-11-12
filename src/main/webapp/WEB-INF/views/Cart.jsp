<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>カート</title>

<style>

body{
    background:#f5f5f5;
    font-family:"Yu Gothic",sans-serif;
    margin:0;
}

.container{
    width:90%;
    max-width:1200px;
    margin:40px auto;
}

.page-title{
    text-align:center;
    font-size:48px;
    margin-bottom:40px;
}

.cart-item{
    background:white;
    border-radius:10px;
    padding:20px;
    margin-bottom:20px;
    display:flex;
    align-items:center;
    gap:20px;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
}

.product-image img{
    width:120px;
    height:120px;
    object-fit:cover;
    border-radius:8px;
}

.product-info{
    flex:1;
}

.product-name{
    font-size:24px;
    font-weight:bold;
    margin-bottom:10px;
}

.price{
    font-size:20px;
    color:#dc2626;
    margin-bottom:10px;
}

.stock{
    color:#666;
}

.quantity-area{
    display:flex;
    align-items:center;
    gap:10px;
}

.quantity-btn{
    width:40px;
    height:40px;
    border:none;
    border-radius:5px;
    background:#2563eb;
    color:white;
    font-size:20px;
    cursor:pointer;
}

.quantity{
    width:50px;
    text-align:center;
    font-size:20px;
}

.subtotal{
    width:150px;
    text-align:right;
    font-size:22px;
    color:#dc2626;
    font-weight:bold;
}

.summary{
    background:white;
    border-radius:10px;
    padding:25px;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
    margin-top:30px;
}

.total{
    font-size:32px;
    text-align:right;
    color:#dc2626;
    font-weight:bold;
    margin-bottom:25px;
}

.button-area{
    text-align:center;
}

.checkout-btn{
    width:350px;
    padding:15px;
    background:#2563eb;
    color:white;
    border:none;
    border-radius:8px;
    font-size:20px;
    cursor:pointer;
}

.checkout-btn:hover{
    background:#1d4ed8;
}

.back-link{
    display:inline-block;
    margin-bottom:20px;
    color:#2563eb;
    text-decoration:none;
}

</style>

<script>

function increase(id){

    let qty =
    document.getElementById(id);

    if(parseInt(qty.value) < 99){
        qty.value =
        parseInt(qty.value)+1;
    }

    calculate();
}

function decrease(id){

    let qty =
    document.getElementById(id);

    if(parseInt(qty.value) > 1){
        qty.value =
        parseInt(qty.value)-1;
    }

    calculate();
}

function calculate(){

    let total = 0;

    document.querySelectorAll(".cart-item")
    .forEach(function(item){

        let price =
        parseInt(item.dataset.price);

        let qty =
        parseInt(
        item.querySelector(".quantity").value);

        let subtotal =
        price * qty;

        item.querySelector(".subtotal")
        .innerHTML =
        subtotal.toLocaleString()+"円";

        total += subtotal;
    });

    document.getElementById("totalPrice")
    .innerHTML =
    total.toLocaleString()+"円";
}

window.onload = calculate;

</script>

</head>

<body>

<div class="container">


<a href="${pageContext.request.contextPath}/products"class="back-link">
        ← 商品一覧へ戻る
    </a>

    <h1 class="page-title">ショッピングカート</h1>



 <c:forEach var="item" items="${cartItems}">

<div class="cart-item"
     data-price="${item.price}">

    <div class="product-info">

        <div class="product-name">
            ${item.productName}
        </div>

        <div class="price">
            ${item.price}円
        </div>

        <div class="stock">
            在庫 : ${item.stock}
        </div>

    </div>

    <div class="quantity-area">

        <button
            type="button"
            class="quantity-btn"
            onclick="decrease('qty${item.productId}')">
            -
        </button>

        <input
            type="text"
            id="qty${item.productId}"
            value="${item.quantity}"
            class="quantity"
            readonly>

        <button
            type="button"
            class="quantity-btn"
            onclick="increase('qty${item.productId}')">
            +
        </button>

    </div>

    <div class="subtotal">
        ${item.subtotal}円
    </div>

</div>

</c:forEach>

    </div>

    <div class="summary">

        <div class="total">
            合計金額 :
            <span id="totalPrice"></span>
        </div>

        <div class="button-area">

            <form action="OrderServlet">

                <button
                type="submit"
                class="checkout-btn">

                注文・決済へ進む

                </button>

            </form>

        </div>

    </div>

</div>

</body>
</html>