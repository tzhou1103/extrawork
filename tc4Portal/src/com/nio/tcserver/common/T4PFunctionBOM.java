package com.nio.tcserver.common;

import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashSet;

import com.nio.tcserver.T4PAttrProperty;
import com.nio.tcserver.T4PBOMLine;
import com.nio.tcserver.T4PUpdateBOMInfo;
import com.nio.tcserver.T4PUpdateBOMResp;
import com.nio.tcserver.session.LoggerDefault;
import com.nio.tcserver.session.SessionPoolManager;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineResponse;
import com.teamcenter.services.strong.bom._2008_06.StructureManagement.ItemLineInfo;
import com.teamcenter.services.strong.bom._2008_06.StructureManagement.AddOrUpdateChildrenToParentLineInfo;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsOutput;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsPref;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsResponse2;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class T4PFunctionBOM {

	/**
	 * @param args
	 * @throws ConnectException
	 * @throws ServerException
	 */
	// public static void main(String[] args) throws ConnectException, ServerException {
	//
	// AppXSession session = SessionPoolManager.getUserSession();
	//
	// DataManagementService dmService = DataManagementService.getService(session.getConnection());
	// GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, "E0000001", "AC");
	//
	// StructureManagementService smService = StructureManagementService.getService(session.getConnection());
	//
	// com.teamcenter.services.strong.bom.StructureManagementService smSrv2 =
	// com.teamcenter.services.strong.bom.StructureManagementService
	// .getService(session.getConnection());
	//
	// CreateBOMWindowsInfo[] createWindowIn = new CreateBOMWindowsInfo[] { new CreateBOMWindowsInfo() };
	// createWindowIn[0].clientId = "Portal";
	// createWindowIn[0].item = itemRevResp.output[0].item;
	// createWindowIn[0].itemRev = itemRevResp.output[0].itemRevOutput[0].itemRevision;
	// CreateBOMWindowsResponse windowResp = smService.createBOMWindows(createWindowIn);
	//
	// BOMLine topline = windowResp.output[0].bomLine;
	// BOMWindow window = windowResp.output[0].bomWindow;
	//
	// // 清除原bom
	// dmService.getProperties(new ModelObject[] { topline }, new String[] { "bl_child_lines" });
	// try {
	// ModelObject[] objs = topline.get_bl_child_lines();
	//
	// BOMLine[] lines = new BOMLine[objs.length];
	// for (int i = 0; i < objs.length; i++) {
	// lines[i] = (BOMLine) objs[i];
	// }
	// smSrv2.removeChildrenFromParentLine(lines);
	//
	// } catch (NotLoadedException e) {
	// e.printStackTrace();
	// }
	//
	// AddOrUpdateChildrenToParentLineInfo[] bomInfos = new AddOrUpdateChildrenToParentLineInfo[] { new
	// AddOrUpdateChildrenToParentLineInfo() };
	// bomInfos[0].parentLine = topline;
	// bomInfos[0].items = new ItemLineInfo[2];
	//
	// GetItemFromAttributeResponse re2 = T4PUtils.getItemRevision(dmService, "E0000003", "AA");
	//
	// bomInfos[0].items[0] = new ItemLineInfo();
	// bomInfos[0].items[0].itemRev = re2.output[0].itemRevOutput[0].itemRevision;
	// bomInfos[0].items[0].itemLineProperties.put("S8_XPT_Custquantity", "333");
	//
	// re2 = T4PUtils.getItemRevision(dmService, "E0000002", "AA");
	// bomInfos[0].items[1] = new ItemLineInfo();
	// bomInfos[0].items[1].itemRev = re2.output[0].itemRevOutput[0].itemRevision;
	// bomInfos[0].items[1].itemLineProperties.put("S8_XPT_Custquantity", "222");
	//
	// smSrv2.addOrUpdateChildrenToParentLine(bomInfos);
	//
	// BOMWindow[] allwindows = new BOMWindow[] { window };
	// smService.saveBOMWindows(allwindows);
	// smService.closeBOMWindows(allwindows);
	// }

	public static T4PUpdateBOMResp updateBOM(T4PUpdateBOMInfo[] updateBOMInput) throws ConnectException {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		boolean isOK = true;

		AppXSession session = SessionPoolManager.getUserSession();

		HashSet<String> hasCheck = new HashSet<String>();

		for (T4PUpdateBOMInfo info : updateBOMInput) {

			String item_id = info.getParentPartNo();
			String rev_id = info.getParentRevId();

			if (hasCheck.contains(item_id + rev_id)) {
				LoggerDefault.logInfo(item_id + rev_id + " has been updated! Ignore...");
				continue;
			}

			hasCheck.add(item_id + rev_id);

			try {
				doUpdateBOM(session, item_id, rev_id, info.getChildLines());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new T4PUpdateBOMResp(isOK, sb.toString());
	}

	@SuppressWarnings("unchecked")
	private static boolean doUpdateBOM(AppXSession session, String item_id, String rev_id, T4PBOMLine[] childLines)
			throws ServerException, ServiceException {

		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, item_id, rev_id);

		// 父项需要版本
		if (itemRevResp.output.length == 0) {
			throw new ServiceException(item_id + " item not found!!");
		} else if (itemRevResp.output[0].itemRevOutput.length == 0) {
			throw new ServiceException(item_id + " " + rev_id + " revision not found!!");
		}

		Item item = itemRevResp.output[0].item;
		ItemRevision itemRevision = itemRevResp.output[0].itemRevOutput[0].itemRevision;
		T4PUtils.serviceDataErrorCheck(dmService.refreshObjects2(new ModelObject[] { itemRevision }, true));

		StructureManagementService smService = StructureManagementService.getService(session.getConnection());

		com.teamcenter.services.strong.bom.StructureManagementService smSrv2 = com.teamcenter.services.strong.bom.StructureManagementService
				.getService(session.getConnection());

		// 设置版本规则
		RevisionRule preciseRule = null;
		GetRevisionRulesResponse rulesResp = smService.getRevisionRules();
		for (RevisionRuleInfo ruleInfo : rulesResp.output) {
			RevisionRule rule = ruleInfo.revRule;
			dmService.getProperties(new ModelObject[] { rule }, new String[] { "object_name" });
			try {
				String name = rule.get_object_name();
				if (name.equals("Precise Only"))
					preciseRule = rule;
			} catch (NotLoadedException e) {
				e.printStackTrace();
			}
		}
		if (preciseRule == null) {
			throw new ServerException("获取Precise Only规则失败");
		}

		CreateBOMWindowsInfo[] createWindowIn = new CreateBOMWindowsInfo[] { new CreateBOMWindowsInfo() };
		createWindowIn[0].clientId = "Portal";
		createWindowIn[0].item = item;
		createWindowIn[0].itemRev = itemRevision;
		CreateBOMWindowsResponse windowResp = smService.createBOMWindows(createWindowIn);

		BOMLine topline = windowResp.output[0].bomLine;
		BOMWindow window = windowResp.output[0].bomWindow;
		BOMWindow[] allwindows = new BOMWindow[] { window };

		// 清除原bom
		try {
			dmService.getProperties(new ModelObject[] { topline }, new String[] { "bl_child_lines" });
			try {
				ModelObject[] objs = topline.get_bl_child_lines();

				BOMLine[] lines = new BOMLine[objs.length];
				for (int i = 0; i < objs.length; i++) {
					lines[i] = (BOMLine) objs[i];
				}
				smSrv2.removeChildrenFromParentLine(lines);

				// 忽视返回的错误
				// RemoveChildrenFromParentLineResponse re = smSrv2.removeChildrenFromParentLine(lines);
				// T4PUtils.serviceDataErrorCheck(re.serviceData);

			} catch (NotLoadedException e) {
				e.printStackTrace();
			}
			smService.saveBOMWindows(allwindows);

			// 确保生成bomview
			T4PUtils.createViewRevision(session, item, itemRevision);

			ArrayList<ItemLineInfo> childList = new ArrayList<ItemLineInfo>();

			for (T4PBOMLine childLine : childLines) {

				T4PAttrProperty[] attrs = childLine.getAttrs();
				String thisItemId = null;
				String thisRevId = null;
				int count = 1;

				ItemLineInfo lineInfo = new ItemLineInfo();
				for (T4PAttrProperty attr : attrs) {
					String attrname = attr.getAttr_name();
					String attrvalue = attr.getAttr_values(0);

					String attrkey = T4PModelDefinitions.getTCBOMAttrName(attrname);
					if (attrkey.equals("bl_item_item_id")) {
						thisItemId = attrvalue;
					}
					if (attrkey.equals("bl_rev_item_revision_id")) {
						thisRevId = attrvalue;
					}

					// 判断S8_XPT_Custquantity是否整数
					// 若是，需要拆分为多行形成bl_pack_count，不写S8_XPT_Custquantity；否则直接写入S8_XPT_Custquantity
					if (attrkey.equals("S8_XPT_Custquantity")) {
						if (T4PUtils.isInteger(attrvalue)) {
							count = T4PUtils.parseIntEx(attrvalue);
							lineInfo.itemLineProperties.put(attrkey, "");
							continue;
						} else {
							lineInfo.itemLineProperties.put(attrkey, attrvalue);
						}
					}

					if (T4PModelDefinitions.bomModifyAttrs.contains(attrkey))
						lineInfo.itemLineProperties.put(attrkey, attrvalue);
				}

				if (thisItemId == null || thisItemId.isEmpty())
					throw new ServiceException("item id of child is null or empty!!");
				if (thisRevId == null)
					thisRevId = "";

				GetItemFromAttributeResponse thisResp = T4PUtils.getItemRevision(dmService, thisItemId, thisRevId);
				if (thisResp.output.length == 0) {
					throw new ServiceException(thisItemId + " item not found!!");
				} else if (thisResp.output[0].itemRevOutput.length == 0) {

					// 逻辑上不应出现
					throw new ServiceException(thisItemId + " " + thisRevId + " revision not found!!");
				}
				lineInfo.itemRev = thisResp.output[0].itemRevOutput[0].itemRevision;

				for (int i = 0; i < count; i++)
					childList.add(lineInfo);
			}

			AddOrUpdateChildrenToParentLineInfo[] bomInfos = new AddOrUpdateChildrenToParentLineInfo[] { new AddOrUpdateChildrenToParentLineInfo() };
			bomInfos[0].parentLine = topline;
			bomInfos[0].items = childList.toArray(new ItemLineInfo[childList.size()]);
			bomInfos[0].viewType = T4PUtils.DEFAULT_BOM_VIEW_TYPE;

			AddOrUpdateChildrenToParentLineResponse addResp = smSrv2.addOrUpdateChildrenToParentLine(bomInfos);
			T4PUtils.serviceDataErrorCheck(addResp.serviceData);

		} catch (Exception e) {
			throw e;
		} finally {
			smService.saveBOMWindows(allwindows);
			smService.closeBOMWindows(allwindows);
		}

		T4PUtils.serviceDataErrorCheck(dmService.refreshObjects2(new ModelObject[] { itemRevision }, false));

		return true;
	}

	public static void getBOM(String[] args) throws ConnectException, ServerException {

		AppXSession session = SessionPoolManager.getUserSession();

		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, "M0000001", "AA");

		StructureManagementService smService = StructureManagementService.getService(session.getConnection());

		CreateBOMWindowsInfo[] createWindowIn = new CreateBOMWindowsInfo[] { new CreateBOMWindowsInfo() };
		createWindowIn[0].clientId = "Portal";
		createWindowIn[0].item = itemRevResp.output[0].item;
		createWindowIn[0].itemRev = itemRevResp.output[0].itemRevOutput[0].itemRevision;
		CreateBOMWindowsResponse windowResp = smService.createBOMWindows(createWindowIn);

		BOMLine topline = windowResp.output[0].bomLine;
		BOMWindow window = windowResp.output[0].bomWindow;

		ExpandPSAllLevelsInfo info = new ExpandPSAllLevelsInfo();
		info.parentBomLines = new BOMLine[] { topline };
		info.excludeFilter = "ExcludeImanItemLines2";
		ExpandPSAllLevelsPref pref = new ExpandPSAllLevelsPref();
		pref.expItemRev = false;
		ExpandPSAllLevelsResponse2 expandResp = smService.expandPSAllLevels(info, pref);

		for (ExpandPSAllLevelsOutput output : expandResp.output) {

			// 处理有子项的数据
			if (output.children.length > 0) {

			}
		}
		smService.closeBOMWindows(new BOMWindow[] { window });

		System.out.println("============");
	}
}
