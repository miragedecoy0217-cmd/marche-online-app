package com.marche.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

import com.marche.dao.UserDAO;
import com.marche.model.User;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 登録画面を表示する (GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    // 登録処理を実行する (POST)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // パスワードをBCryptでハッシュ化
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword_hash(hashedPassword);
        user.setRole("user");
        
        UserDAO userDAO = new UserDAO();
        
        try {
            // データベースに登録
            userDAO.insert(user);
            
            // 登録成功後はログイン画面へリダイレクト
            response.sendRedirect(request.getContextPath() + "/login");
            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "登録に失敗しました。メールアドレスが既に使われている可能性があります。");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}