package com.nio.portal.common.util;

//import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import com.nio.tcserver.session.LoggerDefault;
import com.nio.tcserver.session.T4PContext;
import com.teamcenter.httpsconn.HttpsUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * post模拟上传
 * 
 * @author teng.wang1.o
 * @date 2018-07-06 10:46
 */
public class HttpFileUtils {
	/** pdf上传请求接口地址 */
	private static final String uploadUrl = "http://ds3epj.natappfree.cc/tc/uploadPdf";
	/** 根据partNo更新中台part的Pdf路径 */
	private static final String updateUrl = "http://ds3epj.natappfree.cc/tchttp/updatePartPdf";

	// 存在的问题，不同环境下请求中台的地址如何切换
	// 文档说明:pdf上传接口

	public static void main(String[] args) throws Exception {
		HttpFileUtils.uploadPdf();
	}

	/**
	 * 上传PDF
	 * 
	 * @author teng.wang1.o 2018/7/6 16:06
	 * @return void
	 */
	public static void uploadPdf() {
		String fileName = "E0000419AA.pdf";
		String base64 = HttpFileUtils.fileToBase64("D:\\Draft.pdf");
		Map<String, String> paramsPdf = new HashMap<>();
		paramsPdf.put("base64", base64);
		paramsPdf.put("fileName", fileName);
		String result = HttpFileUtils.doPost(uploadUrl, paramsPdf);
		LoggerDefault.logInfo("返回结果：" + result);
		// 若返回结果为{"resultData":null,"resultCode":"WL-0000","resultDesc":"Success"},请调用中台更新操作(参数part No)
		// Map<String,Object> resultMap = JsonHelper.parseToMap(result);
		JSONObject jsonObj = JSONObject.fromObject(result);
		// String resultDesc = (String)resultMap.get("resultDesc");
		String resultDesc = jsonObj.getString("resultDesc");
		LoggerDefault.logInfo("resultDesc：" + resultDesc);
		Logger.getLogger("default").info("resultDesc：" + resultDesc);
		// 返回成功，执行更新操作
		if ("Success".equals(resultDesc)) {
			// partNo\revId必传
			Map<String, String> paramsPart = new HashMap<>();
			paramsPart.put("partNo", "E0000419");
			paramsPart.put("revId", "AA");
			paramsPart.put("pdfFilePath", fileName);
			HttpFileUtils.updatePartPdf(paramsPart);
		}

	}

	/**
	 * post请求(用于key-value格式的参数)
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String doPost(String url, Map params) {
		BufferedReader in;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost();
			request.setURI(new URI(url));

			// 设置参数
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String value = String.valueOf(params.get(name));
				nvps.add(new BasicNameValuePair(name, value));
			}
			request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			HttpResponse response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				return sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 文件转base64
	 * 
	 * @author teng.wang1.o 2018/7/6 15:18
	 * @param path
	 * @return java.lang.String
	 */
	public static String fileToBase64(String path) {
		BASE64Encoder encoder = new BASE64Encoder();
		FileInputStream fin = null;
		BufferedInputStream bin = null;
		ByteArrayOutputStream baos = null;
		BufferedOutputStream bout = null;
		try {
			fin = new FileInputStream(new File(path));
			bin = new BufferedInputStream(fin);
			baos = new ByteArrayOutputStream();
			bout = new BufferedOutputStream(baos);
			byte[] buffer = new byte[1024];
			int len = bin.read(buffer);
			while (len != -1) {
				bout.write(buffer, 0, len);
				len = bin.read(buffer);
			}
			bout.flush();
			byte[] bytes = baos.toByteArray();
			return encoder.encodeBuffer(bytes).trim();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fin.close();
				bin.close();
				bout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 根据base64生成文件
	 * 
	 * @author teng.wang1.o 2018/7/6 14:51
	 * @param base64
	 *            , filePath, fileName
	 * @return void
	 */
	public static void base64ToFile(String base64, String filePath, String fileName) throws Exception {
		BASE64Decoder decoder = new BASE64Decoder();
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;

		try {
			byte[] bytes = decoder.decodeBuffer(base64);// base64编码内容转换为字节数组
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
			bis = new BufferedInputStream(byteInputStream);
			File file = new File(filePath + fileName);
			File path = file.getParentFile();
			if (!path.exists()) {
				path.mkdirs();
			}
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);

			byte[] buffer = new byte[1024];
			int length = bis.read(buffer);
			while (length != -1) {
				bos.write(buffer, 0, length);
				length = bis.read(buffer);
			}
			bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bos.close();
			fos.close();
			bis.close();
		}
	}

	/**
	 * 更新Part
	 * 
	 * @author teng.wang1.o 2018/7/9 9:20
	 * @param params
	 * @return void
	 */
	public static void updatePartPdf(Map<String, String> params) {
		HttpFileUtils.doPost(updateUrl, params);
	}

}
