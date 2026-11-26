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

		// 画面ごとの権限チェック（superadmin はすべて開ける）
		//   /admin/products … product
		//   /admin/orders   … order
		//   /admin/stock    … inventory
		//   /admin/reviews  … review
		//   /admin/sales    … order（売上CSV。superadmin も可）
		// ダッシュボード（/admin/top）など、ここに無いものは、ログインしていれば開ける
		String path = request.getServletPath();
		String role = admin.getRole();

		boolean denied = (path.startsWith("/admin/products") && !hasRole(role, "product"))
				|| (path.startsWith("/admin/orders") && !hasRole(role, "order"))
				|| (path.startsWith("/admin/stock") && !hasRole(role, "inventory"))
				|| (path.startsWith("/admin/reviews") && !hasRole(role, "review"))
				|| (path.startsWith("/admin/sales") && !hasRole(role, "order"));

		if (denied) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "この画面を開く権限がありません。");
			return;
		}

		chain.doFilter(req, res);
	}

	/** superadmin、または、必要な役割（required）と同じなら true。 */
	private boolean hasRole(String role, String required) {
		return "superadmin".equals(role) || required.equals(role);
	}

	@Override
	public void destroy() {
		// 特に後始末は不要
	}
}