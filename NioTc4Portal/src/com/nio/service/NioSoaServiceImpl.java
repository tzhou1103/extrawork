package com.nio.service;

import java.io.File;
import java.util.HashMap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.nio.util.Base64Util;
import com.nio.util.MySOAUtil;
import com.nio.util.T4PModelDefinitions;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2007_06.DataManagement.RelationAndTypesFilter;
import com.teamcenter.services.strong.core._2007_06.DataManagement.WhereReferencedByRelationNameInfo;
import com.teamcenter.services.strong.core._2007_06.DataManagement.WhereReferencedByRelationNameOutput;
import com.teamcenter.services.strong.core._2007_06.DataManagement.WhereReferencedByRelationNameOutputInfo;
import com.teamcenter.services.strong.core._2007_06.DataManagement.WhereReferencedByRelationNameResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.EPMTask;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.GroupMember;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Role;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@WebService(endpointInterface="com.nio.service.NioSoaService")
public class NioSoaServiceImpl implements NioSoaService 
{
	/** 新建 ENG Part 名称*/
	private static final String NEW_ENGPARTNAME = "BOM Only Eng Part";
	/** XCR Reference 关系真实值*/
	private static final String RELATION_XCRREFERENCE = "CMReferences";
	/** ECN Report 数据集名称 */
	private static final String ENGREPORT_NAME = "ECN Report";
	/** 流程名前缀 */
	private static final String WL_PREFIX = "XPT-Change Request Process";
	
	
	@Override
	@WebMethod
	public void reviseENGPart(@WebParam(name="engPartID")String engPartID, @WebParam(name="crID")String crID) throws Exception 
	{
		if (engPartID == null || engPartID.length() <= 0)
			throw new Exception("Parameter engPartID cannot be null!");
		if (crID == null || crID.length() <= 0)
			throw new Exception("Parameter crID cannot be null!");
		
		// 登录Teamcenter
		User user = MySOAUtil.login();
		if (user == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		
		// 查找零组件(ENG Part)
		Item engPart = MySOAUtil.findItem(engPartID);
		if (engPart == null) 
			throw new Exception("Cannot find ENG Part by id " + engPartID + "!");
		
		// 获取零组件(ENG Part)最新版本
		ItemRevision latestRev = MySOAUtil.getLatestItemRev(engPart);
		if (latestRev == null) {
			throw new Exception("Get ENG Part's latest rev failed with id " + engPartID + "!");
		}
		
		// 修订零组件(ENG Part)
//		ItemRevision reviseItemRev = MySOAUtil.reviseItem(engPart, latestRev, "TMP");
//		if (reviseItemRev == null) 
//			throw new Exception("Revise ENG Part with id" + engPartID + " failed!");
		
		// 修改修订零组件的方法，2018-12-19
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		ReviseInfo[] reviseInfos = new ReviseInfo[] { new ReviseInfo() };
		reviseInfos[0].baseItemRevision = latestRev;
		reviseInfos[0].clientId = "Portal";
		reviseInfos[0].newRevId = "";
		ReviseResponse2 reviseResp = dmService.revise2(reviseInfos);
		ReviseOutput output = (ReviseOutput) reviseResp.reviseOutputMap.get("Portal");
		ItemRevision reviseItemRev = output.newItemRev;
		if (reviseItemRev == null) 
			throw new Exception("Revise ENG Part with id" + engPartID + " failed!");
		
		// 修订完成后，版本号增加TMP
		dmService.getProperties(new ModelObject[] { reviseItemRev }, new String[] { "item_revision_id" });
		String item_revision_id = reviseItemRev.get_item_revision_id();
		HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
		VecStruct struct1 = new VecStruct();
		struct1.stringVec = new String[] { "TMP" + item_revision_id };
		valueMap.put("item_revision_id", struct1);
		dmService.setProperties(new ModelObject[] { reviseItemRev }, valueMap);
		
		// 查找零组件(CR)
		Item crItem = MySOAUtil.findItem(crID);
		if (crItem == null) 
			throw new Exception("Cannot find CR by id " + crID + " !");
		// 获取CR对象的AA版本
		ItemRevision crAARev = MySOAUtil.getCRAARev(crItem);
		if (crAARev == null) 
			throw new Exception("Cannot find CR Rev by id " + crID + " and rev_id AA!");
		
		// 关联CR到ENG Part最新版本
		// 先移除原有的对象
		MySOAUtil.removeObjectByRelation(reviseItemRev, "S8_XPT_Related_CR");
		MySOAUtil.createRelation(reviseItemRev, crAARev, "S8_XPT_Related_CR");
		
		// 为零组件(ENG Part)版本增加状态
		MySOAUtil.addStatus(reviseItemRev, "S8_Temporary");
	}

	@Override
	@WebMethod
	public void releaseUpdate(@WebParam(name="crID")String crID, @WebParam(name="pdfFileBase64")String pdfFileBase64, @WebParam(name="pdfFileName")String pdfFileName, @WebParam(name="status")String status) throws Exception 
	{
		if (crID == null || crID.length() <= 0)
			throw new Exception("Parameter crID cannot be null!");
		if (pdfFileBase64 == null || pdfFileBase64.length() <= 0) 
			throw new Exception("Parameter pdfFileBase64 cannot be null!");
		if (pdfFileName == null || pdfFileName.length() <= 0) 
		{
			throw new Exception("Parameter pdfFileName cannot be null!");
		}
		else if (!pdfFileName.endsWith(".pdf")) {
			throw new Exception("Parameter pdfFileName must endwith .pdf !");
		}
		
		String filePath = System.getProperty("java.io.tmpdir") + File.separator + pdfFileName;
		boolean bool = Base64Util.str2File(pdfFileBase64, filePath);
		if (!bool) 
			throw new Exception("Pdf file decode failed!");
		
		if (status == null || status.length() <= 0)
			throw new Exception("Parameter status cannot be null!");
		
		User user = MySOAUtil.login();
		if (user == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		
		// 查找零组件(CR)
		Item crItem = MySOAUtil.findItem(crID);
		if (crItem == null) 
			throw new Exception("Cannot find CR by id " + crID + " !");
		
		// 获取零组件(CR)最新版本
		ItemRevision latestRev = MySOAUtil.getLatestItemRev(crItem);
		if (latestRev == null) {
			throw new Exception("Get CR's latest rev failed with id " + crID + "!");
		}
		
		// 上传PDF数据集
		Dataset dataset = MySOAUtil.findDataset(latestRev, RELATION_XCRREFERENCE, ENGREPORT_NAME, "PDF");
		if (dataset == null) {
			dataset = MySOAUtil.createDataset(ENGREPORT_NAME, "PDF");
			MySOAUtil.createRelation(latestRev, dataset, RELATION_XCRREFERENCE);
		} else {
			MySOAUtil.removeNamedReferenceFromDataset(dataset, "PDF_Reference");
		}
		MySOAUtil.uploadFiles(new File(filePath), dataset, "PDF_Reference");
		
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		
		// 获取零组件(CR)版本所在流程
		EPMTask epmTask = MySOAUtil.getEPMTask(latestRev, WL_PREFIX);
		if (epmTask == null) {
			dmService.getProperties(new ModelObject[] { latestRev }, new String[] { "item_revision_id" });
			throw new Exception("CR Rev with id " + crID + " and revid " + latestRev.get_item_revision_id() + " did not in workflow process!");
		}
		
		// 给流程附件添加状态
		dmService.getProperties(new ModelObject[] { epmTask }, new String[] { "target_attachments" });
		ModelObject[] target_attachments = epmTask.get_target_attachments();
		for (ModelObject target_attachment : target_attachments) {
			MySOAUtil.addStatus((WorkspaceObject) target_attachment, status);
		}
		
		new File(filePath).deleteOnExit();
	}
	
	@Override
	@WebMethod
	public String createBOMOnlyEngPart(@WebParam(name="partType")String partType, @WebParam(name="owning_user")String owning_user, @WebParam(name="owning_group")String owning_group, @WebParam(name="crNum")String crNum) throws Exception 
	{
		User loginUser = MySOAUtil.login();
		if (loginUser == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		
		if (partType == null || partType.length() <= 0)
			throw new Exception("Parameter partType cannot be null!");
		String engPartType = T4PModelDefinitions.getEBOMTypeByCADType(partType);
		if (engPartType == null || engPartType.isEmpty()) {
			throw new Exception(partType + " has not defined cad type!!");
		}
		
		if (owning_user == null || owning_user.length() <= 0)
			throw new Exception("Parameter owning_user cannot be null!");
		if (owning_group == null || owning_group.length() <= 0)
			throw new Exception("Parameter owning_group cannot be null!");
		if (crNum == null || crNum.length() <= 0)
			throw new Exception("Parameter crNum cannot be null!");
		
		
//		String engPartType = "S8_ENGPart";
//		Item item = MySOAUtil.createItem("", "AA", NEW_ENGPARTNAME, engPartType, null);
		
		// 修改Item创建, 2018-12-19
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		ItemProperties[] props = new ItemProperties[1];
		props[0] = new ItemProperties();
		props[0].clientId = "Portal";
		props[0].type = engPartType;
		props[0].revId = "AA";
		props[0].name = NEW_ENGPARTNAME;
		CreateItemsResponse resp = dmService.createItems(props, null, null);
		Item item = resp.output[0].item;
		ItemRevision itemRev = resp.output[0].itemRev;
		
		// 查找零组件(CR)
		Item crItem = MySOAUtil.findItem(crNum);
		if (crItem == null) 
			throw new Exception("Cannot find CR by id " + crNum + " !");
		
		// 获取CR对象的AA版本
		ItemRevision crAARev = MySOAUtil.getCRAARev(crItem);
		if (crAARev == null) 
			throw new Exception("Cannot find CR Rev by id " + crNum + " and rev_id AA!");
		
		// 关联CR到ENG Part最新版本
		ModelObject[] modelObjects = MySOAUtil.getRelatedObjects(itemRev, "S8_XPT_Related_CR");
		if (!MySOAUtil.isContain(modelObjects, crAARev)) {
			MySOAUtil.createRelation(itemRev, crAARev, "S8_XPT_Related_CR");
		}
		
		// 更改所有权，2018-12-20，新增
		User user = MySOAUtil.getUserById(owning_user);
		if (user == null) 
			throw new Exception("Can not find user by user_id: " + owning_user + " !");
		Group group = MySOAUtil.getGrpById(owning_group);
		if (group == null) 
			throw new Exception("Can not find group by group_name: " + owning_group + " !");
		MySOAUtil.changeOwnership(user, group, new ModelObject[] { item, itemRev });
		
		// 设置属性值,2018-12-20,新增
		MySOAUtil.setProperty(itemRev, "s8_ZT_EBOMOnly", "Yes");
		
		dmService.getProperties(new ModelObject[] { item }, new String[] { "item_id" });
		String item_id = item.get_item_id();
		return item_id;
	}

	@Override
	@WebMethod
	public void reviseBOMOnlyEngPart(@WebParam(name="engPartID")String engPartID, @WebParam(name="crID")String crID, @WebParam(name="status")String status) throws Exception 
	{
		if (engPartID == null || engPartID.length() <= 0)
			throw new Exception("Parameter engPartID cannot be null!");
		if (crID == null || crID.length() <= 0)
			throw new Exception("Parameter crID cannot be null!");
		
		// 登录Teamcenter
		User user = MySOAUtil.login();
		if (user == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		
		// 查找零组件(ENG Part)
		Item engPart = MySOAUtil.findItem(engPartID);
		if (engPart == null) 
			throw new Exception("Cannot find ENG Part by id " + engPartID + "!");
		
		// 获取零组件(ENG Part)最新版本
		ItemRevision latestRev = MySOAUtil.getLatestItemRev(engPart);
		if (latestRev == null) {
			throw new Exception("Get ENG Part's latest rev failed with id " + engPartID + "!");
		}
		
		// 判断最新版是否发布
		if (!MySOAUtil.isObjectReleased(latestRev)) {
			dmService.getProperties(new ModelObject[] { latestRev }, new String[] { "item_revision_id" });
			throw new Exception("ID为 " + engPartID + ", 版本号为 " + latestRev.get_item_revision_id() + " 的对象未发布");
		}
		
		// 修订零组件(ENG Part)
//		ItemRevision reviseItemRev = MySOAUtil.reviseItem(engPart, latestRev, "");
//		if (reviseItemRev == null) 
//			throw new Exception("Revise ENG Part with id" + engPartID + " failed!");
		
		// 修改修订零组件的方法，2018-12-19
		ReviseInfo[] reviseInfos = new ReviseInfo[] { new ReviseInfo() };
		reviseInfos[0].baseItemRevision = latestRev;
		reviseInfos[0].clientId = "Portal";
		reviseInfos[0].newRevId = "";
		ReviseResponse2 reviseResp = dmService.revise2(reviseInfos);
		ReviseOutput output = (ReviseOutput) reviseResp.reviseOutputMap.get("Portal");
		ItemRevision reviseItemRev = output.newItemRev;
		if (reviseItemRev == null) 
			throw new Exception("Revise ENG Part with id" + engPartID + " failed!");
		
		// 查找零组件(CR)
		Item crItem = MySOAUtil.findItem(crID);
		if (crItem == null) 
			throw new Exception("Cannot find CR by id " + crID + " !");
		
		// 获取CR对象的AA版本
		ItemRevision crAARev = MySOAUtil.getCRAARev(crItem);
		if (crAARev == null) 
			throw new Exception("Cannot find CR Rev by id " + crID + " and rev_id AA!");
		
		// 关联CR到ENG Part最新版本
		// 先移除原有的对象
		MySOAUtil.removeObjectByRelation(reviseItemRev, "S8_XPT_Related_CR");
		MySOAUtil.createRelation(reviseItemRev, crAARev, "S8_XPT_Related_CR");
		
		//添加状态
		MySOAUtil.addStatus((WorkspaceObject) reviseItemRev, status);
	}
	
	@Override
	@WebMethod
	public boolean isENGPartRevRelatedCADPartRev(@WebParam(name="engPartID")String engPartID, @WebParam(name="crID")String crID) throws Exception 
	{
		if (engPartID == null || engPartID.length() <= 0)
			throw new Exception("Parameter engPartID cannot be null!");
		if (crID == null || crID.length() <= 0)
			throw new Exception("Parameter crID cannot be null!");
		
		// 登录Teamcenter
		User user = MySOAUtil.login();
		if (user == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		
		// 查找零组件(ENG Part)
		Item engPart = MySOAUtil.findItem(engPartID);
		if (engPart == null) 
			throw new Exception("Cannot find ENG Part by id " + engPartID + "!");
		
		// 查找零组件(CR)
		Item crItem = MySOAUtil.findItem(crID);
		if (crItem == null) 
			throw new Exception("Cannot find CR by id " + crID + " !");
		// 获取CR对象的AA版本
		ItemRevision crAARev = MySOAUtil.getCRAARev(crItem);
		if (crAARev == null) 
			throw new Exception("Cannot find CR Rev by id " + crID + " and rev_id AA!");
		ItemRevision targetRev = null;
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		dmService.getProperties(new ModelObject[] { engPart },	new String[] { "revision_list" });
		ModelObject[] revision_list = engPart.get_revision_list();
		if (revision_list != null && revision_list.length > 0) 
		{
			for (int i = 0; i < revision_list.length; i++) 
			{
				ItemRevision itemRevision = (ItemRevision) revision_list[i];
				ModelObject[] relatedObjects = MySOAUtil.getRelatedObjects(itemRevision, "S8_XPT_Related_CR");
				if (relatedObjects != null && relatedObjects.length > 0)
				{
					for (ModelObject modelObject : relatedObjects) 
					{
						if (modelObject == crAARev) {
							targetRev = itemRevision;
							break;
						}
					}
				}
				if (targetRev != null) {
					break;
				}
			}
		}
		
		if (targetRev == null) {
			throw new Exception("Cannot find a Eng Part Rev related CR!");
		}
		
		// 获取零组件(ENG Part)最新版本
		/*ItemRevision latestRev = MySOAUtil.getLatestItemRev(engPart);
		if (latestRev == null) {
			throw new Exception("Get ENG Part's latest rev failed with id " + engPartID + "!");
		}*/
		
		// 获取关联对象
		/*ItemRevision cadPartRev = MySOAUtil.getRelatedItemRevision(targetRev, TYPE_CADPARTREV);
		if (cadPartRev != null) {
			return true;
		}*/
		ModelObject referencer = null;
		WhereReferencedByRelationNameInfo[] relationNameInfos = new WhereReferencedByRelationNameInfo[] {
				new WhereReferencedByRelationNameInfo() };
		RelationAndTypesFilter[] filters = new RelationAndTypesFilter[]{new RelationAndTypesFilter()};
		filters[0].relationTypeName = "S8_DesignEngLinkage_Rel";
//		filters[0].otherSideObjectTypes = new String[]{"*"};
		relationNameInfos[0].filter = filters;
		relationNameInfos[0].object = targetRev;
		WhereReferencedByRelationNameResponse response = dmService.whereReferencedByRelationName(relationNameInfos, 1);
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			return false;
		}
		WhereReferencedByRelationNameOutput[] outputs = response.output;
		if (outputs != null && outputs.length > 0) 
		{
			for (WhereReferencedByRelationNameOutput output : outputs) 
			{
				WhereReferencedByRelationNameOutputInfo[] infos = output.info;
				if (infos != null && infos.length > 0) 
				{
					for (WhereReferencedByRelationNameOutputInfo info : infos)
					{
						if (info.referencer.getTypeObject().isInstanceOf("S8_XPT_ManageRevision")) {
							referencer = info.referencer;
							break;
						}
					}
				}
			}
		}
		
		if (referencer != null) {
			return true;
		}
		
		return false;
	}

	@Override
	public void releaseCRUpdate(String crID, String status) throws Exception 
	{
		if (crID == null || crID.length() <= 0)
			throw new Exception("Parameter crID cannot be null!");
		if (status == null || status.length() <= 0)
			throw new Exception("Parameter status cannot be null!");
		
		// 登录Teamcenter
		User user = MySOAUtil.login();
		if (user == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		
		// 查找零组件(CR)
		Item crItem = MySOAUtil.findItem(crID);
		if (crItem == null) 
			throw new Exception("Cannot find CR by id " + crID + " !");
		
		// 获取零组件(CR)最新版本
		ItemRevision latestRev = MySOAUtil.getLatestItemRev(crItem);
		if (latestRev == null) {
			throw new Exception("Get CR's latest rev failed with id " + crID + "!");
		}
		
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		
		// 获取零组件(CR)版本所在流程
		EPMTask epmTask = MySOAUtil.getEPMTask(latestRev, WL_PREFIX);
		if (epmTask == null) {
			dmService.getProperties(new ModelObject[] { latestRev }, new String[] { "item_revision_id" });
			throw new Exception("CR Rev with id " + crID + " and revid " + latestRev.get_item_revision_id() + " did not in workflow process!");
		}
		
		// 给流程附件添加状态
		dmService.getProperties(new ModelObject[] { epmTask }, new String[] { "target_attachments" });
		ModelObject[] target_attachments = epmTask.get_target_attachments();
		for (ModelObject target_attachment : target_attachments) 
		{
			if (target_attachment.getTypeObject().isInstanceOf("ItemRevision")) 
			{
				// 更新 ENG Part Rev的版本号
				ItemRevision itemRevision = (ItemRevision) target_attachment;
				dmService.getProperties(new ModelObject[] { itemRevision }, new String[] { "object_type", "item_revision_id" });
//				String object_type = itemRevision.get_object_type();
				String item_revision_id = itemRevision.get_item_revision_id();
				if (itemRevision.getTypeObject().isInstanceOf("S8_XPT_ManageRevision") && item_revision_id.startsWith("TMP") && item_revision_id.length() > 3) 
				{
					HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
					VecStruct struct1 = new VecStruct();
					struct1.stringVec = new String[] { item_revision_id.substring(3) };
					valueMap.put("item_revision_id", struct1);
					dmService.setProperties(new ModelObject[] { itemRevision }, valueMap);
				}
			}
			
			MySOAUtil.addStatus((WorkspaceObject) target_attachment, status);
		}
	}

	@Override
	public String getGroupAndUser() throws Exception 
	{
		User user = MySOAUtil.login();
		if (user == null) {
			throw new Exception("Login Teamcenter failed，please contact administrator!");
		}
		JSONObject groupAndUserJSONObject = new JSONObject();
		
		DataManagementService dmService = DataManagementService.getService(AppXSession.getConnection());
		// 查询所有组
		ModelObject[] groups = MySOAUtil.query("GroupByName", new String[] { "name" }, new String[] { "*" });
		if (groups != null && groups.length > 0)
		{
			JSONArray groupJSONArray = new JSONArray();
			
			for (int i = 0; i < groups.length; i++) 
			{
				Group group = (Group) groups[i];
				dmService.getProperties(new ModelObject[] { group }, new String[] { "full_name", "list_of_role" });
				String full_name = group.get_full_name();
				String nameSuffix = full_name.substring(full_name.lastIndexOf(".")+1);
				if (!MySOAUtil.groupNameList.contains(nameSuffix)) {
					continue;
				}
				
				JSONObject groupJSONObject = new JSONObject();
				groupJSONObject.put("group_name", full_name);
				
				// 用户列表
				JSONArray userJSONArray = new JSONArray();
				
				// 获取组的角色列表
				Role[] list_of_role = group.get_list_of_role();
				for (Role role : list_of_role) 
				{
					dmService.getProperties(new ModelObject[] { role }, new String[] { "role_name" });
					String role_name = role.get_role_name();
					// 根据组和角色查询组成员
					ModelObject[] modelObjects = MySOAUtil.query("Admin - Group/Role Membership", new String[] { "Group:group.name", "role.role_name" }, new String[] { full_name, role_name });
					if (modelObjects != null && modelObjects.length > 0) 
					{
						for (ModelObject modelObject2 : modelObjects) 
						{
							GroupMember groupMember = (GroupMember) modelObject2;
							dmService.getProperties(new ModelObject[] { groupMember }, new String[] { "the_user" });
							User the_user = (User)groupMember.get_the_user();
							dmService.getProperties(new ModelObject[] { the_user }, new String[] { "user_id" });
							String user_id = the_user.get_user_id();
								
							JSONObject userJSONObject = new JSONObject();
							userJSONObject.put("user_name", user_id);
							
							userJSONArray.add(userJSONObject);
						}
					}
				}
				
				groupJSONObject.put("users", userJSONArray);
				groupJSONArray.add(groupJSONObject);
			}
			
			groupAndUserJSONObject.put("groups", groupJSONArray);
		}
		return groupAndUserJSONObject.toString();
	}
	
}
