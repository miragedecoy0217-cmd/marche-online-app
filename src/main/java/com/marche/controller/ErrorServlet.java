package com.marche.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/error")
public class ErrorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		show(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		show(request, response);
	}

	private void show(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int code = statusOf(request, response);
		String title;
		String message;

		switch (code) {
		case 400:
			title = "リクエストが正しくありません";
			message = "入力内容をご確認のうえ、もう一度お試しください。";
			break;
		case 403:
			title = "アクセスできません";
			message = "このページを開く権限がありません。";
			break;
		case 404:
			title = "ページが見つかりません";
			message = "URLが間違っているか、ページが削除された可能性があります。";
			break;
		case 500:
			title = "システムエラーが発生しました";
			message = "しばらくしてから、もう一度お試しください。";
			break;
		default:
			title = "エラーが発生しました";
			message = "お手数ですが、トップページからやり直してください。";
			break;
		}

		request.setAttribute("errorCode", code);
		request.setAttribute("errorTitle", title);
		request.setAttribute("errorMessage", message);
		request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
	}

	/** エラーコードを取得する。/error を直接開いた場合は 0（コード非表示）。 */
	private int statusOf(HttpServletRequest request, HttpServletResponse response) {
		Object attr = request.getAttribute("javax.servlet.error.status_code");
		if (attr == null) {
			attr = request.getAttribute("jakarta.servlet.error.status_code");
		}
		if (attr instanceof Integer) {
			return (Integer) attr;
		}
		int status = response.getStatus();
		return status >= 400 ? status : 0;
	}
}