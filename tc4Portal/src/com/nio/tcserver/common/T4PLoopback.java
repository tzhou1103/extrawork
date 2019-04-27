package com.nio.tcserver.common;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import com.nio.tcserver.session.LoggerDefault;
import com.nio.tcserver.session.SessionPoolManager;
import com.nio.tcserver.session.T4PContext;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.httpsconn.HttpsUtils;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusInput;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusOption;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class T4PLoopback {

	public static String callLoopback(String functionName, final String[] parts) throws ConnectException,
			ServerException {

		// System.out.println(new Date());
		final AppXSession session = SessionPoolManager.getUserSession();
		// System.out.println(new Date());

		(new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					DataManagementService dmService = DataManagementService.getService(session.getConnection());
					ServiceData serviceData = dmService.loadObjects(parts);
					T4PUtils.serviceDataErrorCheck(serviceData);

					int num = serviceData.sizeOfPlainObjects();

					ModelObject[] objs = new ModelObject[num];
					for (int i = 0; i < num; i++) {
						objs[i] = serviceData.getPlainObject(i);
					}

					updatePortalPart(session, objs);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		})).start();

		return functionName + " call Success!";
	}

	public static void updatePortalPart(AppXSession session, ModelObject[] objs) throws Exception {

		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		dmService.refreshObjects(objs);

		// 逐一推送属性修改
		ArrayList<String> attrList = T4PModelDefinitions.attrSent2Portal;
		String[] attrNames = attrList.toArray(new String[attrList.size()]);

		String[] tcAttrNames = new String[attrNames.length];
		for (int i = 0; i < attrNames.length; i++) {
			String tcAttr = T4PModelDefinitions.getTCAttrName(attrNames[i]);
			tcAttrNames[i] = tcAttr;
		}

		dmService.getProperties(objs, tcAttrNames);
		dmService.getProperties(objs, new String[] { "item_id", "item_revision_id", "object_string", "s8_XPT_Material",
				"s8_XPT_Material_Spec", "S8_XPT_Buyer_Relation", "S8_XPT_Supplier_Relation", "last_release_status" });

		for (ModelObject obj : objs) {

			try {

				LoggerDefault.logInfo("++++ objs num : " + objs.length + "\t");
				
				Map<String, String> paramsPart = new HashMap<>();

				for (String attrName : attrNames) {
					String tcAttr = T4PModelDefinitions.getTCAttrName(attrName);
					Property propObj = obj.getPropertyObject(tcAttr);

					String[] values = T4PUtils.getPropertyValues(session, propObj);

					paramsPart.put(attrName, values[0]);

				}

				// material
				paramsPart.put("materialNames", obj.getPropertyDisplayableValue("s8_XPT_Material"));
				paramsPart.put("materialSpecifications", obj.getPropertyDisplayableValue("s8_XPT_Material_Spec"));

				// // buyer
				// Property propObjBuyer = obj.getPropertyObject("S8_XPT_Buyer_Relation");
				// paramsPart.put("buyerEmails", T4PUtils.getPropertyValues(session, propObjBuyer)[0]);
				//
				// // supplier
				// Property propObjSupplier = obj.getPropertyObject("S8_XPT_Supplier_Relation");
				// paramsPart.put("sapSupplierIds", T4PUtils.getPropertyValues(session, propObjSupplier)[0]);

				// buyer/supplier 从对应eng part取
				ItemRevision engPart = T4PUtils.getEngPartRevisionByCADPart(session, obj);

				if (engPart == null) {
					// 未关联eng，不传递
					LoggerDefault.logInfo("!!!!! " + obj.getPropertyDisplayableValue("object_string")
							+ " has not related to an ebom part");
					continue;
				}

				dmService.refreshObjects(new ModelObject[] { engPart });

				// 更新Eng
				HashMap<String, VecStruct> updateEng = new HashMap<String, VecStruct>();
				/*
				for (String attrName : attrNames) {
					VecStruct struct = new VecStruct();
					struct.stringVec = new String[] { paramsPart.get(attrName) };
					// 这里包含了以下属性，下面写属性取值和存map(这个for循环注释掉), modified by tzhou, 2018-12-17
//					[revId, partDescription, cadStatus, weightFrom, weight]
//  				[item_revision_id, object_desc, last_release_status, s8_XPT_WeightFrom, s8_XPT_Weights]
//					[s8_ZT_linkedCADRevID, object_desc, s8_ZT_CADStatus, s8_XPT_WeightFrom, s8_XPT_Weights]
					updateEng.put(T4PModelDefinitions.getTCAttrName_E(attrName), struct);
				}
				*/
				VecStruct struct = new VecStruct();
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("item_id") };
				updateEng.put("s8_ZT_linkedCADPartNo", struct);
				struct = new VecStruct();
				String cadId = obj.getPropertyDisplayableValue("item_id");
				String cadRevId = obj.getPropertyDisplayableValue("item_revision_id");
				struct.stringVec = new String[] { cadRevId };
				updateEng.put("s8_ZT_linkedCADRevID", struct);
				struct = new VecStruct();
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("s8_XPT_Material") };
				updateEng.put("s8_XPT_Material", struct);
				struct = new VecStruct();
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("s8_XPT_Material_Spec") };
				updateEng.put("s8_XPT_Material_Spec", struct);

				// 将版本刷新
				// dmService.getProperties(new ModelObject[] { engPart }, new String[] { "item_id", "item_revision_id"
				// });
				// String oldEngRevId = engPart.get_item_revision_id();
				// if (!cadRevId.equals(oldEngRevId)) {
				
				// 去掉对ENG Part Rev版本号的更新, modified by zhoutong, 2018-12-14
				/*struct = new VecStruct();
				struct.stringVec = new String[] { cadRevId };
				updateEng.put("item_revision_id", struct);*/
				// }
				
				// 增加EngPartRev的属性更新s8_XPT_UOM，s8_XPT_Weithts，added by zhoutong, 2018-12-14
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("s8_XPT_UOM") };
				updateEng.put("s8_XPT_UOM", struct);
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("s8_XPT_Weights") };
				updateEng.put("s8_XPT_Weights", struct);
				
				// 其他发送给中台的属性，也更新到Tc，added by zhoutong, 2018-12-17
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("object_desc") };
				updateEng.put("object_desc", struct);
				
				ModelObject cadStatObj0 = obj.getPropertyObject("last_release_status").getModelObjectValue();
				String cadStatus0 = null;
				if (cadStatObj0 != null) {
					dmService.getProperties(new ModelObject[] { cadStatObj0 }, new String[] { "name" });
					cadStatus0 = cadStatObj0.getPropertyObject("name").getStringValue();
					
					struct.stringVec = new String[] { cadStatus0 };
					updateEng.put("s8_ZT_CADStatus", struct);
				}
				
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("s8_XPT_WeightFrom") };
				updateEng.put("s8_XPT_WeightFrom", struct);
				
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("S8_XPT_Buyer_Relation") };
				updateEng.put("S8_XPT_Buyer_Relation", struct);
				struct.stringVec = new String[] { obj.getPropertyDisplayableValue("S8_XPT_Supplier_Relation") };
				updateEng.put("S8_XPT_Supplier_Relation", struct);
				// end add

				dmService.setProperties(new ModelObject[] { engPart }, updateEng);

				dmService.getProperties(new ModelObject[] { engPart }, new String[] { "item_id", "item_revision_id",
						"S8_XPT_Buyer_Relation", "S8_XPT_Supplier_Relation", "s8_ZT_CR_Num", "last_release_status" });

				String engId = engPart.getPropertyDisplayableValue("item_id");
				if (engId.endsWith("_ENG"))
					engId = engId.substring(0, engId.lastIndexOf("_ENG"));
				
				String cadOwner = obj.getPropertyObject("owning_user").getDisplayableValue();
				
				paramsPart.put("partNo", engId);
				paramsPart.put("cadId", cadId);
				paramsPart.put("revId", cadRevId);
				paramsPart.put("cadOwner", cadOwner);
				paramsPart.put("cadStatus", cadStatus0);
//				paramsPart.put("crNum", engPart.getPropertyDisplayableValue("s8_ZT_CR_Num"));
				
				// 修改crNum属性获取，modified by zhoutong, 2018-12-20
				ModelObject[] relatedObjects = T4PUtils.getRelatedObjects(session, engPart, "S8_XPT_Related_CR");
				if (relatedObjects != null && relatedObjects.length > 0) {
					ItemRevision crItemRev = (ItemRevision) relatedObjects[0];
					dmService.getProperties(new ModelObject[] { crItemRev }, new String[] { "item_id" });
					paramsPart.put("crNum", crItemRev.get_item_id());
				} else {
					System.out.println(">>> Eng part " + engId + " does not related CR!!");
				}
				
				// buyer
				Property propObjBuyer = engPart.getPropertyObject("S8_XPT_Buyer_Relation");
				paramsPart.put("buyerEmails", T4PUtils.getPropertyValues(session, propObjBuyer)[0]);

				// supplier
				Property propObjSupplier = engPart.getPropertyObject("S8_XPT_Supplier_Relation");
				paramsPart.put("sapSupplierIds", T4PUtils.getPropertyValues(session, propObjSupplier)[0]);

				for (Entry<String, String> entry : paramsPart.entrySet()) {
					LoggerDefault.logInfo("++++ send : " + entry.getKey() + "\t" + entry.getValue());
				}

				String result1 = HttpsUtils.doPost(T4PContext.poratlUpdatePartUrl, paramsPart);

				LoggerDefault.logInfo("======result1====== " + result1);
				try {
					JSONObject jsonObj = JSONObject.fromObject(result1);

					String resultDesc1 = jsonObj.getString("resultDesc");
					LoggerDefault.logInfo("======resultDesc1====== " + resultDesc1);

					if (resultDesc1.equals("Success")) {
						String result2 = T4PFunctionDataset.send2Portal(session, (ItemRevision) obj,
								paramsPart.get("partNo"), paramsPart.get("revId"));
						LoggerDefault.logInfo("======result2====== " + result2);
					}

				} catch (Exception e) {
					LoggerDefault.logError(e.getMessage());
				}

				// 状态同步
				WorkflowService wfSrv = WorkflowService.getService(session.getConnection());
				ModelObject cadStatObj = obj.getPropertyObject("last_release_status").getModelObjectValue();
				ModelObject engStatObj = engPart.getPropertyObject("last_release_status").getModelObjectValue();
				String newStatus = null;
				if (cadStatObj != null && engStatObj == null) {
					dmService.getProperties(new ModelObject[] { cadStatObj }, new String[] { "name" });
					newStatus = cadStatObj.getPropertyObject("name").getStringValue();
				} else if (cadStatObj != null) {
					dmService.getProperties(new ModelObject[] { cadStatObj, engStatObj }, new String[] { "name" });
					String cadStatus = cadStatObj.getPropertyObject("name").getStringValue();
					String engStatus = engStatObj.getPropertyObject("name").getStringValue();
					if (!cadStatus.equals(engStatus)) {
						newStatus = cadStatus;

						// 去除原状态 ==
						ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
						rsInput[0].objects = new WorkspaceObject[] { engPart };
						rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
						rsInput[0].operations[0].operation = "Delete";
						rsInput[0].operations[0].existingreleaseStatusTypeName = engStatus;
						wfSrv.setReleaseStatus(rsInput);
					}
				}
				if (newStatus != null) {
					ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
					rsInput[0].objects = new WorkspaceObject[] { engPart };
					rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
					rsInput[0].operations[0].operation = "Append";
					rsInput[0].operations[0].newReleaseStatusTypeName = newStatus;
					wfSrv.setReleaseStatus(rsInput);
				}
			} catch (NotLoadedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
