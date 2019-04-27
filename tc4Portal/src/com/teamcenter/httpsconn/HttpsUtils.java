package com.teamcenter.httpsconn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import com.nio.portal.common.util.HttpFileUtils;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;

public class HttpsUtils {

	public static String doPost(String urlStr, Map<String, String> params) throws ServiceException {

		// 非https用原方法
		if (!urlStr.startsWith("https"))
			return HttpFileUtils.doPost(urlStr, params);

		StringBuilder postData = new StringBuilder();
		postData.append("");

		int index = 0;
		for (Entry<String, String> entry : params.entrySet()) {
			index++;
			String name = entry.getKey();
			String value = entry.getValue();

			try {
				postData.append(URLEncoder.encode(name, "UTF-8"));
				postData.append("=");
				postData.append(URLEncoder.encode(value, "UTF-8"));

				if (index < params.size())
					postData.append("&");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		String json = null;
		HttpsURLConnection httpsURLConnection = null;
		try {
			// URL url = new URL(urlStr);
			URL url = new URL(null, urlStr, new sun.net.www.protocol.https.Handler()); // for weblogic
			URLConnection connection = url.openConnection();
			httpsURLConnection = (HttpsURLConnection) connection;
			httpsURLConnection.setSSLSocketFactory(new TLSSocketConnectionFactory());

			httpsURLConnection.setDoOutput(true);
			httpsURLConnection.setDoInput(true);
			httpsURLConnection.setRequestMethod("POST");
			httpsURLConnection.setUseCaches(false);
			httpsURLConnection.setInstanceFollowRedirects(true);
			httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

			httpsURLConnection.connect(); // not working on weblogic

			OutputStreamWriter os = null;
			os = new OutputStreamWriter(httpsURLConnection.getOutputStream());
			os.write(postData.toString());
			os.flush();

			json = getResponse(httpsURLConnection);

		} catch (Exception e) {

			e.printStackTrace();
			json = e.getMessage();

		} finally {

			if (httpsURLConnection != null) {
				httpsURLConnection.disconnect();
			}
		}

		return json;
	}

	public static String getResponse(HttpURLConnection Conn) throws IOException {

		InputStream is;
		if (Conn.getResponseCode() >= 400) {
			is = Conn.getErrorStream();
		} else {
			is = Conn.getInputStream();
		}

		String response = "";
		byte buff[] = new byte[512];
		int b = 0;
		while ((b = is.read(buff, 0, buff.length)) != -1) {
			response += new String(buff, 0, b);

		}
		is.close();

		return response;
	}
}
