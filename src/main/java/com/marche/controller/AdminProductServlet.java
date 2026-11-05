package com.marche.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.marche.dao.CategoryDAO;
import com.marche.dao.ProductDAO;
import com.marche.model.Product;

@WebServlet("/admin/products")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class AdminProductServlet extends HttpServlet {

	private static final int PAGE_SIZE = 10;
	private static final String UPLOAD_DIR = "images/products";
	private static final List<String> ALLOWED_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
	private static final String VIEW_LIST = "/WEB-INF/views/admin/products.jsp";
	private static final String VIEW_FORM = "/WEB-INF/views/admin/product_form.jsp";

	private final ProductDAO productDAO = new ProductDAO();
	private final CategoryDAO categoryDAO = new CategoryDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		try {
			if ("new".equals(action)) {
				request.setAttribute("categories", categoryDAO.findAll());
				request.getRequestDispatcher(VIEW_FORM).forward(request, response);

			} else if ("edit".equals(action)) {
				int id = Integer.parseInt(request.getParameter("id"));
				Product product = productDAO.findById(id);
				if (product == null || product.isDeleted()) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				request.setAttribute("product", product);
				request.setAttribute("categories", categoryDAO.findAll());
				request.getRequestDispatcher(VIEW_FORM).forward(request, response);

			} else {
				int page = parsePage(request.getParameter("page"));
				int total = productDAO.countForAdmin();
				int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
				page = Math.min(page, totalPages);

				request.setAttribute("products", productDAO.findAllForAdmin(page, PAGE_SIZE));
				request.setAttribute("page", page);
				request.setAttribute("totalPages", totalPages);
				request.setAttribute("total", total);
				request.getRequestDispatcher(VIEW_LIST).forward(request, response);
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");

		try {
			// ---- 削除（論理削除） ----
			if ("delete".equals(action)) {
				productDAO.logicalDelete(Integer.parseInt(request.getParameter("id")));
				response.sendRedirect(request.getContextPath() + "/admin/products");
				return;
			}

			// ---- 登録 / 更新 共通：入力チェック ----
			boolean isUpdate = "update".equals(action);
			Product product = new Product();
			String error = null;

			if (isUpdate) {
				product.setProductId(Integer.parseInt(request.getParameter("id")));
			}

			String name = request.getParameter("productName");
			product.setProductName(name == null ? null : name.trim());
			product.setDescription(request.getParameter("description"));
			if (name == null || name.trim().isEmpty()) {
				error = "商品名を入力してください。";
			}

			try {
				product.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
			} catch (NumberFormatException e) {
				error = "カテゴリを選択してください。";
			}

			try {
				BigDecimal price = new BigDecimal(request.getParameter("price"));
				product.setPrice(price);
				if (price.signum() < 0) {
					error = "価格は0以上で入力してください。";
				}
			} catch (NumberFormatException | NullPointerException e) {
				error = "価格は数字で入力してください。";
			}

			try {
				int stock = Integer.parseInt(request.getParameter("stockCount"));
				product.setStockCount(stock);
				if (stock < 0) {
					error = "在庫数は0以上で入力してください。";
				}
			} catch (NumberFormatException e) {
				error = "在庫数は数字で入力してください。";
			}

			// ---- 画像アップロード（選ばなければnullのまま＝DAO側で既存画像を維持） ----
			Part filePart = request.getPart("image");
			if (error == null && filePart != null && filePart.getSize() > 0) {
				String saved = saveImage(filePart);
				if (saved == null) {
					error = "画像は jpg / png / gif / webp のみアップロードできます。";
				} else {
					product.setImagePath(saved);
				}
			}

			if (error != null) {
				// エラー時にフォームへ戻すため、編集なら今の画像パスを表示用に補う
				if (isUpdate) {
					Product current = productDAO.findById(product.getProductId());
					if (current != null) {
						product.setImagePath(current.getImagePath());
					}
				}
				request.setAttribute("errorMessage", error);
				request.setAttribute("product", product);
				request.setAttribute("categories", categoryDAO.findAll());
				request.getRequestDispatcher(VIEW_FORM).forward(request, response);
				return;
			}

			if (isUpdate) {
				if (!productDAO.update(product)) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			} else {
				productDAO.insert(product);
			}
			response.sendRedirect(request.getContextPath() + "/admin/products");

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	private int parsePage(String s) {
		try {
			return Math.max(1, Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/** 画像を保存し、DBに入れる相対パスを返す。拡張子が不正なら null。 */
	private String saveImage(Part part) throws IOException {
		String submitted = part.getSubmittedFileName();
		int dot = (submitted == null) ? -1 : submitted.lastIndexOf('.');
		if (dot < 0) {
			return null;
		}
		String ext = submitted.substring(dot + 1).toLowerCase();
		String contentType = part.getContentType();
		if (!ALLOWED_EXT.contains(ext) || contentType == null || !contentType.startsWith("image/")) {
			return null;
		}

		String fileName = UUID.randomUUID().toString() + "." + ext;
		String dirPath = getServletContext().getRealPath("/" + UPLOAD_DIR);
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		part.write(new File(dir, fileName).getAbsolutePath());
		return UPLOAD_DIR + "/" + fileName;
	}
}