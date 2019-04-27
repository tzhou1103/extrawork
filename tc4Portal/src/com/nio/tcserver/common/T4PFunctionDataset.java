package com.nio.tcserver.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import sun.misc.BASE64Encoder;

import com.nio.tcserver.T4PGetPartPdfResp;
import com.nio.tcserver.session.LoggerDefault;
import com.nio.tcserver.session.SessionPoolManager;
import com.nio.tcserver.session.T4PContext;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.httpsconn.HttpsUtils;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.FileManagementService;
import com.teamcenter.services.strong.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PDF;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class T4PFunctionDataset {

	public static void main(String[] args) throws Exception {

		String re = getPdfFile("E0000003", "AB");

		System.out.println(re);
	}

	public static String getPdfFile(String item_id, String item_revision_id) throws IOException {

		if (item_id == null || item_id.isEmpty()) {
			throw new ServiceException("item_id is empty!!");
		}
		if (item_revision_id == null || item_revision_id.isEmpty()) {
			throw new ServiceException("item_revision_id is empty!!");
		}

		AppXSession session = SessionPoolManager.getUserSession();

		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, item_id, item_revision_id);

		if (itemRevResp.output.length == 0) {
			throw new ServiceException("item not found!!");
		} else if (itemRevResp.output[0].itemRevOutput.length == 0) {
			throw new ServiceException("revision not found!!");
		}

		ItemRevision itemRev = itemRevResp.output[0].itemRevOutput[0].itemRevision;

		String result = null;
		try {
			result = send2Portal(session, itemRev);
		} catch (NotLoadedException e) {
			e.printStackTrace();
		} catch (Exception anyE) {
			anyE.printStackTrace();
			LoggerDefault.logError(anyE.getMessage());
			throw anyE;
		}

		return result;
	}

	public static String send2Portal(AppXSession session, ItemRevision itemRev) throws ServiceException,
			ServerException, IOException, NotLoadedException {

		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		dmService.getProperties(new ModelObject[] { itemRev }, new String[] { "item_id", "item_revision_id" });
		String item_id = itemRev.getPropertyDisplayableValue("item_id");
		String item_revision_id = itemRev.getPropertyDisplayableValue("item_revision_id");

		return send2Portal(session, itemRev, item_id, item_revision_id);

	}

	public static String send2Portal(AppXSession session, ItemRevision itemRev, String item_id, String item_revision_id)
			throws ServiceException, ServerException, IOException {

		String crNum = null;
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		String[] relations = new String[] { "S8_XPT_PDF_for_DataExchange", "IMAN_Rendering", "IMAN_specification" };
		dmService.getProperties(new ModelObject[] { itemRev }, relations);

		PDF pdfData = null;

		for (String relation : relations) {
			try {
				ModelObject[] datas = itemRev.getPropertyObject(relation).getModelObjectArrayValue();

				for (ModelObject data : datas) {
					if (data instanceof PDF) {
						pdfData = (PDF) data;
						break;
					}
				}
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}

			if (pdfData != null)
				break;
		}

		if (pdfData == null)
			return "no pdf file";

		dmService.getProperties(new ModelObject[] { pdfData }, new String[] { "ref_list" });
		ModelObject[] imanfiles = null;
		try {
			imanfiles = pdfData.get_ref_list();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		if (imanfiles.length < 1)
			throw new ServiceException("imanfile not found!!");

		ImanFile file = (ImanFile) imanfiles[0];

		FileManagementService fms = FileManagementService.getService(session.getConnection());
		FileTicketsResponse tksResp = fms.getFileReadTickets(new ImanFile[] { file });
		T4PUtils.serviceDataErrorCheck(tksResp.serviceData);

		Map<?, ?> tksmap = tksResp.tickets;

		String ticket = (String) tksmap.get(file);
		if (ticket == null)
			throw new ServiceException("file broken!!");

		// debug
		// dmService.refreshObjects(new ModelObject[] { itemRev });
		// dmService.getProperties(new ModelObject[] { itemRev }, new String[]{"object_desc"});
		// String desc = "";
		// try {
		// desc = itemRev.get_object_desc();
		// } catch (NotLoadedException e1) {
		// e1.printStackTrace();
		// }
		// if (desc.equals("ex2")) {
		// throw new ServiceException(ticket);
		// }

		StringBuilder fullUrl = new StringBuilder();
		fullUrl.append(T4PContext.fms_url);
		fullUrl.append(T4PContext.fms_url.endsWith("/") ? "" : "/");
		fullUrl.append(item_id);
		fullUrl.append(item_revision_id);
		fullUrl.append(".pdf?ticket=");
		fullUrl.append(ticket);

		URL url = new URL(fullUrl.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/67.0.3396.99");

		InputStream inputStream = conn.getInputStream();

		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) > 0) {
			bos.write(buffer, 0, len);
		}
		bos.close();

		// download to local
		// File localfile = new File("D:\\test2\\test111.pdf");
		//
		// FileOutputStream fos = new FileOutputStream(localfile);
		// fos.write(bos.toByteArray());
		// if (fos != null) {
		// fos.close();
		// }

		BASE64Encoder encoder = new BASE64Encoder();
		String fileName = item_id + item_revision_id + ".pdf";
		Map<String, String> paramsPdf = new HashMap<String, String>();
		paramsPdf.put("base64", encoder.encodeBuffer(bos.toByteArray()).trim());
		paramsPdf.put("fileName", fileName);
		
		//获取crNum
		String[] desEngLink = new String[] { "S8_DesignEngLinkage_Rel" };
		dmService.getProperties(new ModelObject[] { itemRev }, desEngLink);
		
		try {
			ModelObject[] engDatas = itemRev.getPropertyObject(desEngLink[0]).getModelObjectArrayValue();
//			dmService.getProperties(new ModelObject[] { engDatas[0] }, new String[] { "s8_ZT_CR_Num" });
//			crNum = engDatas[0].getPropertyObject("s8_ZT_CR_Num").getStringValue();
			
			// 修改crNum属性获取，modified by zhoutong, 2018-12-22
			ModelObject[] relatedObjects = T4PUtils.getRelatedObjects(session, engDatas[0], "S8_XPT_Related_CR");
			if (relatedObjects != null && relatedObjects.length > 0) {
				ItemRevision crItemRev = (ItemRevision) relatedObjects[0];
				dmService.getProperties(new ModelObject[] { crItemRev }, new String[] { "item_id" });
				crNum = crItemRev.get_item_id();
			} 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		String result = null;

		LoggerDefault.logInfo("----- fileName : " + paramsPdf.get("fileName"));
		LoggerDefault.logInfo("----- poratlUploadUrl : " + T4PContext.poratlUploadUrl);

		try {
			result = HttpsUtils.doPost(T4PContext.poratlUploadUrl, paramsPdf);

			LoggerDefault.logInfo("======dataset result1====== " + result);

			JSONObject jsonObj = JSONObject.fromObject(result);

			String resultDesc = jsonObj.getString("resultDesc");

			LoggerDefault.logInfo("======dataset resultDesc1====== " + resultDesc);

			if ("Success".equals(resultDesc)) {
				// partNo\revId必传
				Map<String, String> paramsPart = new HashMap<>();
				paramsPart.put("partNo", item_id);
				paramsPart.put("revId", item_revision_id);
				paramsPart.put("crNum", crNum);
				paramsPart.put("pdfFilePath", fileName);
				result = HttpsUtils.doPost(T4PContext.poratlUpdatePartUrl, paramsPart);

				LoggerDefault.logInfo("======dataset result12====== " + result);

				jsonObj = JSONObject.fromObject(result);
				resultDesc = jsonObj.getString("resultDesc");

				LoggerDefault.logInfo("======dataset resultDesc2====== " + resultDesc);
			}

			return resultDesc;

		} catch (JSONException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

			result = e.getMessage();

		}

		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (conn != null)
				conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static T4PGetPartPdfResp getPartPdf(String item_id, String item_revision_id) throws RemoteException {

		try {

			String re = getPdfFile(item_id, item_revision_id);

			if (re == null)
				re = "---null---";

			return new T4PGetPartPdfResp(true, re);

		} catch (RemoteException e) {

			throw e;

		} catch (IOException e) {
			e.printStackTrace();

			return new T4PGetPartPdfResp(false, e.getMessage());
		}

	}
}
