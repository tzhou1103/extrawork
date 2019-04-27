package com.nio.tcserver.common;

import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.util.ArrayList;

import com.nio.tcserver.T4PBuyerInfo;
import com.nio.tcserver.T4PGetBuyersResp;
import com.nio.tcserver.T4PGetMaterialsResp;
import com.nio.tcserver.T4PGetSuppliersResp;
import com.nio.tcserver.T4PMaterialInfo;
import com.nio.tcserver.T4PSupplierInfo;
import com.nio.tcserver.session.SessionPoolManager;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.query.FinderService;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2007_06.Finder.FindWorkspaceObjectsOutput;
import com.teamcenter.services.strong.query._2007_06.Finder.FindWorkspaceObjectsResponse;
import com.teamcenter.services.strong.query._2007_06.Finder.WSOFindSet;
import com.teamcenter.services.strong.query._2007_06.Finder.WSOFindCriteria;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class T4PSync {

	public static T4PGetBuyersResp getBuyers() throws ConnectException, ServiceException, ServerException {
		AppXSession session = SessionPoolManager.getUserSession();

		FinderService fndService = FinderService.getService(session.getConnection());
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		WSOFindSet[] findSet = new WSOFindSet[] { new WSOFindSet() };
		findSet[0].criterias = new WSOFindCriteria[] { new WSOFindCriteria() };
		WSOFindCriteria criteria = findSet[0].criterias[0];
		criteria.objectType = "S8_XPT_Buyer";
		criteria.objectName = "*";
		criteria.scope = "WSO_scope_All";
		FindWorkspaceObjectsResponse resp = fndService.findWorkspaceObjects(findSet);

		T4PUtils.serviceDataErrorCheck(resp.serviceData);

		ArrayList<T4PBuyerInfo> list = new ArrayList<T4PBuyerInfo>();
		for (FindWorkspaceObjectsOutput output : resp.outputList) {

			dmService.getProperties(output.foundObjects, new String[] { "s8_XPT_BuyerName", "s8_XPT_BuyerEmail",
					"s8_XPT_Buyer_Status" });

			for (WorkspaceObject obj : output.foundObjects) {

				try {
					list.add(new T4PBuyerInfo(obj.getPropertyDisplayableValue("s8_XPT_BuyerName"), //
							obj.getPropertyDisplayableValue("s8_XPT_BuyerEmail"), //
							obj.getPropertyDisplayableValue("s8_XPT_Buyer_Status")));
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}

			}
		}

		return new T4PGetBuyersResp("OK!", list.toArray(new T4PBuyerInfo[list.size()]));
	}

	public static T4PGetSuppliersResp getSuppliers() throws ConnectException, ServiceException, ServerException {
		AppXSession session = SessionPoolManager.getUserSession();

		FinderService fndService = FinderService.getService(session.getConnection());
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		WSOFindSet[] findSet = new WSOFindSet[] { new WSOFindSet() };
		findSet[0].criterias = new WSOFindCriteria[] { new WSOFindCriteria() };
		WSOFindCriteria criteria = findSet[0].criterias[0];
		criteria.objectType = "S8_XPT_Supplier";
		criteria.objectName = "*";
		criteria.scope = "WSO_scope_All";
		FindWorkspaceObjectsResponse resp = fndService.findWorkspaceObjects(findSet);

		T4PUtils.serviceDataErrorCheck(resp.serviceData);

		ArrayList<T4PSupplierInfo> list = new ArrayList<T4PSupplierInfo>();
		for (FindWorkspaceObjectsOutput output : resp.outputList) {

			dmService.getProperties(output.foundObjects, new String[] { "s8_XPT_Supplier_ID", "s8_XPT_Supplier_Name",
					"s8_XPT_Supplier_Type", "s8_XPT_Contact_Person", "s8_XPT_Supplier_CP_EMail",
					"s8_XPT_Supplier_CP_Tel", "s8_XPT_Supplier_Comment", "s8_XPT_Supplier_Status" });

			for (WorkspaceObject obj : output.foundObjects) {

				try {
					T4PSupplierInfo info = new T4PSupplierInfo();
					info.setId(obj.getPropertyDisplayableValue("s8_XPT_Supplier_ID"));
					info.setName(obj.getPropertyDisplayableValue("s8_XPT_Supplier_Name"));
					info.setType(obj.getPropertyDisplayableValue("s8_XPT_Supplier_Type"));
					info.setContact_person(obj.getPropertyDisplayableValue("s8_XPT_Contact_Person"));
					info.setEmail(obj.getPropertyDisplayableValue("s8_XPT_Supplier_CP_EMail"));
					info.setTel(obj.getPropertyDisplayableValue("s8_XPT_Supplier_CP_Tel"));
					info.setComment(obj.getPropertyDisplayableValue("s8_XPT_Supplier_Comment"));
					info.setStatus(obj.getPropertyDisplayableValue("s8_XPT_Supplier_Status"));
					list.add(info);
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}

			}
		}

		return new T4PGetSuppliersResp("OK!", list.toArray(new T4PSupplierInfo[list.size()]));
	}

	public static T4PGetMaterialsResp getMaterials() throws ConnectException, ServiceException, ServerException {

		AppXSession session = SessionPoolManager.getUserSession();

		SavedQueryService queryService = SavedQueryService.getService(session.getConnection());
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		FindSavedQueriesCriteriaInput queryCI = new FindSavedQueriesCriteriaInput();
		queryCI.queryNames = new String[] { "S8_XPT_Basic_Material" };
		FindSavedQueriesCriteriaInput[] qCIs = new FindSavedQueriesCriteriaInput[1];
		qCIs[0] = queryCI;
		FindSavedQueriesResponse resp = queryService.findSavedQueries(qCIs);
		if (resp.savedQueries.length < 1) {
			throw new ServiceException("Teamcenter could not find Query :" + queryCI.queryNames);
		}

		ImanQuery qry = resp.savedQueries[0];

		QueryInput savedQueryInput[] = new QueryInput[1];
		savedQueryInput[0] = new QueryInput();
		savedQueryInput[0].query = qry;
		savedQueryInput[0].maxNumToReturn = 0;
		savedQueryInput[0].limitList = new ModelObject[0];
		savedQueryInput[0].entries = new String[] { "Name" };
		savedQueryInput[0].values = new String[] { "S8_Released" };

		SavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
		T4PUtils.serviceDataErrorCheck(savedQueryResult.serviceData);

		ArrayList<T4PMaterialInfo> list = new ArrayList<T4PMaterialInfo>();
		for (QueryResults results : savedQueryResult.arrayOfResults) {

			ServiceData sdata = dmService.loadObjects(results.objectUIDS);
			T4PUtils.serviceDataErrorCheck(sdata);

			int num = sdata.sizeOfPlainObjects();
			ModelObject[] objs = new ModelObject[num];
			for (int i = 0; i < num; i++) {
				objs[i] = sdata.getPlainObject(i);
			}

			dmService.getProperties(objs, new String[] { "item_id", "items_tag", "object_name", "s8_XPT_BM_Density",
					"s8_XPT_BM_Spec", "s8_XPT_BM_GroupL1", "s8_XPT_BM_GroupL2", "s8_XPT_BM_GroupL3",
					"s8_XPT_BM_GroupL4", "s8_XPT_BM_GroupL5", "s8_XPT_BM_GroupL6", "s8_XPT_Mat_Lib_Grp_Lev1" });

			for (ModelObject obj : objs) {

				try {
					T4PMaterialInfo info = new T4PMaterialInfo();
					info.setId(obj.getPropertyDisplayableValue("item_id"));
					info.setItem(obj.getPropertyDisplayableValue("items_tag"));
					info.setName(obj.getPropertyDisplayableValue("object_name"));
					info.setDensity(obj.getPropertyDisplayableValue("s8_XPT_BM_Density"));
					info.setSpec(obj.getPropertyDisplayableValue("s8_XPT_BM_Spec"));

					info.setLv1(obj.getPropertyDisplayableValue("s8_XPT_BM_GroupL1"));
					info.setLv2(obj.getPropertyDisplayableValue("s8_XPT_BM_GroupL2"));
					info.setLv3(obj.getPropertyDisplayableValue("s8_XPT_BM_GroupL3"));
					info.setLv4(obj.getPropertyDisplayableValue("s8_XPT_BM_GroupL4"));
					info.setLv5(obj.getPropertyDisplayableValue("s8_XPT_BM_GroupL5"));
					info.setLv6(obj.getPropertyDisplayableValue("s8_XPT_BM_GroupL6"));
					info.setGrplv1(obj.getPropertyDisplayableValue("s8_XPT_Mat_Lib_Grp_Lev1"));

					list.add(info);

				} catch (NotLoadedException e) {
					e.printStackTrace();
				}
			}
		}

		return new T4PGetMaterialsResp("OK!", list.toArray(new T4PMaterialInfo[list.size()]));
	}
}
