package com.nio.tcserver.session;

import javax.servlet.ServletContext;

public class T4PContext {

	// public static String tc_url = "iiop:192.168.85.137:1572/TcServer1";
	// public static String tc_url = "iiop:192.168.85.142:1572/TcServer1";
	// public static String fms_url = "http://10.110.1.31:4544";
	public static String tc_url = "http://192.168.85.137:7001/tc";
	public static String fms_url = "http://192.168.85.137:4544";
	public static boolean isDebug = true;
	public static String username = "admin";
	public static String password = "123";

	public static String poratlUploadUrl = "http://ds3epj.natappfree.cc/tc/uploadPdf";
	public static String poratlUpdatePartUrl = "http://ds3epj.natappfree.cc/tchttp/updatePartPdf";

	public static void initWebValue(ServletContext sc) {
		tc_url = sc.getInitParameter("TcServiceUrl");
		fms_url = sc.getInitParameter("TcFMSServiceUrl");
		username = sc.getInitParameter("username");
		password = sc.getInitParameter("password");
		isDebug = !sc.getInitParameter("isDebug").equals("0");

		poratlUploadUrl = sc.getInitParameter("PoratlUploadUrl");
		poratlUpdatePartUrl = sc.getInitParameter("PoratlUpdatePartUrl");
	}

}
