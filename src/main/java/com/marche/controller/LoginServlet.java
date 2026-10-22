package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import com.marche.dao.UserDAO;
import com.marche.model.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		UserDAO userDAO = new UserDAO();

		try {
			//②　DBからemailに一致するユーザーを検索
			User user = userDAO.findByEmail(email);
			//③ ユーザーが存在し、かつPWがBCryptで一致するかチェック
			if (user != null && BCrypt.checkpw(password, user.getPassword_hash())) {
				//④　一致したらセッションにユーザー情報をセット
				HttpSession session = request.getSession();
				session.setAttribute("loginUser", user);

				//⑤　トップぺージへリダイレクト
				response.sendRedirect(request.getContextPath() + "/");
			} else {
				//ログイン失敗：エラーメッセージをセットしてログイン画面に戻す
				request.setAttribute("errorMessage", "メールアドレスまたはパスワードが正しくありません。");
				request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}
}
