package com.marche.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.marche.model.AdminUser;

@WebFilter("/admin/*")
public class AdminFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// 特に初期化は不要
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		HttpSession session = request.getSession(false);
		AdminUser admin = (session != null) ? (AdminUser) session.getAttribute("loginAdmin") : null;

		// 未ログイン → ログイン画面へ
		if (admin == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		// 商品管理は superadmin と product だけ
		String path = request.getServletPath();
		if (path.startsWith("/admin/products")
				&& !("superadmin".equals(admin.getRole()) || "product".equals(admin.getRole()))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "この画面を開く権限がありません。");
			return;
		}

		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {
		// 特に後始末は不要
	}
}