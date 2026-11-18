package com.marche.dao;

import java.util.List;

import com.marche.model.CartItem;

/**
 * CartDAO の動作確認用（参照のみ。DBは変更しない）。
 * 右クリック → 実行 → Javaアプリケーション。
 * 初期データが入っていれば、user_id=1 のカートに1件（active）ある。
 */
public class CartDAOTest {

    public static void main(String[] args) {
        CartDAO dao = new CartDAO();

        List<CartItem> list = dao.findByUserID(1);
        System.out.println("user_id=1 のカート: " + list.size() + "件");
        System.out.println("（1件なら成功。0件で赤い文字が出たら、SQLのエラーです）");
    }
}