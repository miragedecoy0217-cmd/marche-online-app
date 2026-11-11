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

import com.marche.dao.AdminUserDAO;
import com.marche.dao.UserDAO;
import com.marche.model.AdminUser;
import com.marche.model.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String loginKey = request.getParameter("email");
		String password = request.getParameter("password");

		UserDAO userDAO = new UserDAO();
		AdminUserDAO adminDAO = new AdminUserDAO();

		try {
			// ① 客（users）として認証
			User user = userDAO.findByEmail(loginKey);
			if (user != null && BCrypt.checkpw(password, user.getPassword_hash())) {
				HttpSession session = newSession(request);
				session.setAttribute("loginUser", user);
				response.sendRedirect(request.getContextPath() + "/");
				return;
			}

			// ② 管理者（admin_users）として認証
			AdminUser admin = adminDAO.findByLoginId(loginKey);
			if (admin != null && BCrypt.checkpw(password, admin.getPasswordHash())) {
				HttpSession session = newSession(request);
				session.setAttribute("loginAdmin", admin);
				adminDAO.updateLastLogin(admin.getAdminId());
				response.sendRedirect(request.getContextPath() + "/admin/top");
				return;
			}

			// ③ どちらも失敗
			request.setAttribute("errorMessage", "ログインIDまたはパスワードが正しくありません。");
			request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);

		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	/** 古いセッションを破棄して作り直す（セッション固定攻撃対策） */
	private HttpSession newSession(HttpServletRequest request) {
		HttpSession old = request.getSession(false);
		if (old != null) {
			old.invalidate();
		}
		return request.getSession(true);
	}
}