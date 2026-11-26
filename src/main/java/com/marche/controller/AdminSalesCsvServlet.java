package com.marche.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marche.dao.SalesDAO;
import com.marche.model.SalesData;

/**
 * 管理：売上CSVエクスポート（P6-1）。
 *
 *   GET /admin/sales/csv?type=daily|monthly|category|product
 *
 * ・UTF-8 BOM付きで出力する（Excelで文字化けしない）。
 * ・ファイルは作らず、レスポンスに直接書き出す（FileWriter は使わない）。
 * ・権限（superadmin / order）は AdminFilter でチェックする。
 * ・集計の対象は、キャンセル・返金以外の注文（SalesDAO の仕様）。
 */
@WebServlet("/admin/sales/csv")
public class AdminSalesCsvServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final SalesDAO salesDAO = new SalesDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String type = request.getParameter("type");
		if (type == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			List<SalesData> list;
			String labelHeader;
			String typeName;
			boolean withQty;

			switch (type) {
			case "daily":
				list = salesDAO.findDaily();
				labelHeader = "日付";
				typeName = "daily";
				withQty = false;
				break;
			case "monthly":
				list = salesDAO.findMonthly();
				labelHeader = "年月";
				typeName = "monthly";
				withQty = false;
				break;
			case "category":
				list = salesDAO.findByCategory();
				labelHeader = "カテゴリ";
				typeName = "category";
				withQty = true;
				break;
			case "product":
				list = salesDAO.findByProduct();
				labelHeader = "商品名";
				typeName = "product";
				withQty = true;
				break;
			default:
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			String fileName = "sales_" + typeName + "_" + LocalDate.now() + ".csv";

			response.setContentType("text/csv; charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

			OutputStream out = response.getOutputStream();
			// UTF-8 の BOM（Excelが、UTF-8だと判断するための目印）
			out.write(0xEF);
			out.write(0xBB);
			out.write(0xBF);

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

			// ヘッダー行
			writer.print(escape(labelHeader));
			writer.print(',');
			writer.print(escape("注文件数"));
			if (withQty) {
				writer.print(',');
				writer.print(escape("販売数量"));
			}
			writer.print(',');
			writer.print(escape("売上金額（円）"));
			writer.print("\r\n");

			// データ行
			for (SalesData s : list) {
				writer.print(escape(s.getLabel()));
				writer.print(',');
				writer.print(s.getOrderCount());
				if (withQty) {
					writer.print(',');
					writer.print(s.getTotalQty());
				}
				writer.print(',');
				writer.print(yen(s.getTotalSales()));
				writer.print("\r\n");
			}

			writer.flush();

		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "データベースエラーが発生しました。");
		}
	}

	/** 金額を、小数なしの文字列にする（例: 1176.00 → 1176）。 */
	private String yen(BigDecimal amount) {
		if (amount == null) {
			return "0";
		}
		return amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
	}

	/**
	 * CSVの1項目を安全な形にする。
	 * ・カンマ、改行、ダブルクォートを含むときは、"..." で囲む（中の " は "" にする）
	 * ・先頭が = + - @ のときは、Excelで数式として実行されないよう、先頭に ' を付ける
	 */
	private String escape(String value) {
		if (value == null) {
			return "";
		}
		String v = value;
		if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
			v = "'" + v;
		}
		if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
			v = "\"" + v.replace("\"", "\"\"") + "\"";
		}
		return v;
	}
}