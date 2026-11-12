<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>注文・決済</title>

<style>
body{
    background:#f5f5f5;
    font-family:"Yu Gothic",sans-serif;
    margin:0;
}

.container{
    width:90%;
    max-width:900px;
    margin:40px auto;
}

.page-title{
    text-align:center;
    font-size:42px;
    font-weight:bold;
    margin-bottom:30px;
}

.card{
    background:#fff;
    border-radius:10px;
    padding:25px;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
    margin-bottom:25px;
}

.section-title{
    font-size:22px;
    font-weight:bold;
    margin-bottom:15px;
    color:#2563eb;
}

table{
    width:100%;
    border-collapse:collapse;
}

table th,
table td{
    padding:12px;
    border-bottom:1px solid #ddd;
    text-align:left;
}

.total{
    text-align:right;
    margin-top:15px;
    font-size:26px;
    font-weight:bold;
    color:#dc2626;
}

textarea{
    width:100%;
    padding:12px;
    font-size:16px;
    border:1px solid #ccc;
    border-radius:6px;
    resize:none;
    box-sizing:border-box;
}

.payment{
    margin:12px 0;
    font-size:18px;
}

.payment label{
    cursor:pointer;
}

.btn-area{
    text-align:center;
}

.submit-btn{
    background:#2563eb;
    color:white;
    border:none;
    width:300px;
    padding:15px;
    font-size:18px;
    border-radius:8px;
    cursor:pointer;
}

.submit-btn:hover{
    background:#1d4ed8;
}

.error{
    color:red;
    margin-bottom:10px;
}
</style>

<script>
function validateForm(){

    const address =
        document.getElementById("address").value.trim();

    const payment =
        document.querySelector(
        'input[name="payment"]:checked');

    if(address.length === 0){
        alert("住所を入力してください");
        return false;
    }

    if(address.length > 255){
        alert("住所は255文字以内で入力してください");
        return false;
    }

    if(payment == null){
        alert("支払い方法を選択してください");
        return false;
    }

    return true;
}
</script>

</head>

<body>

<div class="container">

    <h1 class="page-title">注文・決済</h1>

    <form action="OrderServlet" method="post"
          onsubmit="return validateForm();">

        <!-- カート内容 -->
        <div class="card">

            <div class="section-title">
                カート内容
            </div>

            <table>
                <tr>
                    <th>商品名</th>
                    <th>数量</th>
                    <th>金額</th>
                </tr>

                <tr>
                    <td>新鮮トマト</td>
                    <td>2</td>
                    <td>596円</td>
                </tr>

                <tr>
                    <td>完熟バナナ</td>
                    <td>1</td>
                    <td>258円</td>
                </tr>
            </table>

            <div class="total">
                合計金額：854円
            </div>

        </div>

        <!-- 配送先 -->
        <div class="card">

            <div class="section-title">
                お届け先住所
            </div>

            <textarea
                id="address"
                name="address"
                rows="4"
                maxlength="255"
                placeholder="住所を入力してください"></textarea>

        </div>

        <!-- 支払い方法 -->
        <div class="card">

            <div class="section-title">
                支払い方法
            </div>

            <div class="payment">
                <label>
                    <input type="radio"
                           name="payment"
                           value="credit">
                    クレジットカード
                </label>
            </div>

            <div class="payment">
                <label>
                    <input type="radio"
                           name="payment"
                           value="bank">
                    銀行振込
                </label>
            </div>

            <div class="payment">
                <label>
                    <input type="radio"
                           name="payment"
                           value="emoney">
                    電子マネー
                </label>
            </div>

        </div>

        <div class="btn-area">
            <button type="submit" class="submit-btn">
                注文を確定する
            </button>
        </div>

    </form>

</div>

</body>
</html>