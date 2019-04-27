package com.nio.tcserver.session;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class T4PServiceTester extends HttpServlet {

	private static final long serialVersionUID = 1070L;

	public T4PServiceTester() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doTester(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		doTester(request, response);
	}

	private void doTester(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		request.setCharacterEncoding("UTF-8");
		String sevId = request.getParameter("ID_NAME");

		if (sevId != null && sevId.equals("TESTER")) {

			String o = "";
			try {
				SessionPoolManager.getUserSession();

				o = "<script>" + "parent.document.getElementById('s_stat').innerHTML='OK';"
						+ "parent.document.getElementById('s_ver').innerHTML='" + ((double) serialVersionUID / 1000)
						+ "';" + "parent.document.title = 'Teamcenter - TC4PortalService';" + "</script>";

			} catch (Exception e) {
				e.printStackTrace();

				o = "<script>" + "parent.document.getElementById('s_stat').innerHTML='" + e.getMessage() + "';"
						+ "parent.document.getElementById('s_ver').innerHTML='" + ((double) serialVersionUID / 1000)
						+ "';" + "</script>";
			} finally {

				response.getOutputStream().write(o.getBytes("UTF-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();

			}
		}
	}

}
