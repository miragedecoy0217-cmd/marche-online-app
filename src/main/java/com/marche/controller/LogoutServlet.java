package com.marche.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//①　セッションを取得（既存の物があれば）
		HttpSession session = request.getSession(false);

		if (session != null) {
			//②　セッションを破棄(ログイン情報を消去)
			session.invalidate();
		}
		//③　ログイン画面(またはトップページ)へリダイレクト
		response.sendRedirect(request.getContextPath() + "/login");
	}
}
