package com.nio.tcserver.common;

import java.rmi.ServerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.nio.tcserver.T4PAttrProperty;
import com.nio.tcserver.session.LoggerDefault;
import com.nio.tcserver.session.SessionPoolManager;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.internal.strong.core.ICTService;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.Arg;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.Array;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.Entry;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.InvokeICTMethodResponse;
import com.teamcenter.services.internal.strong.core._2011_06.ICT.Structure;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.Structure.CreateOrSaveAsPSBOMViewRevisionInput;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.Structure.CreateOrSaveAsPSBOMViewRevisionResponse;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.Structure.GetAllAvailableViewTypesInput;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.Structure.GetAvailableViewTypesResponse;
import com.teamcenter.services.strong.administration.PreferenceManagementService;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateRelationsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ObjectOwner;
import com.teamcenter.services.strong.core._2006_03.DataManagement.Relationship;
import com.teamcenter.services.strong.core._2007_01.DataManagement.GetItemFromIdPref;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsData;
import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsOutput;
import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsPref;
import com.teamcenter.services.strong.core._2007_06.DataManagement.ExpandGRMRelationsResponse;
import com.teamcenter.services.strong.core._2007_06.DataManagement.RelationAndTypesFilter2;
import com.teamcenter.services.strong.core._2008_06.DataManagement.CreateOrUpdateRelationsInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.CreateOrUpdateRelationsResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.strong.core._2008_06.DataManagement.SecondaryData;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeInfo;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.strong.core._2014_10.DataManagement.ChildrenInputData;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.DescribeSavedQueriesResponse;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.GetSavedQueriesResponse;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.SavedQueryFieldListObject;
import com.teamcenter.services.strong.query._2006_03.SavedQuery.SavedQueryFieldObject;
import com.teamcenter.services.strong.query._2007_06.SavedQuery.ExecuteSavedQueriesResponse;
import com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryInput;
import com.teamcenter.services.strong.query._2007_06.SavedQuery.SavedQueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.services.strong.query._2010_09.SavedQuery.BusinessObjectQueryClause;
import com.teamcenter.services.strong.query._2010_09.SavedQuery.BusinessObjectQueryInput;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.ContextData;
import com.teamcenter.services.strong.workflow._2008_06.Workflow.InstanceInfo;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.PropertyDescription;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ImanQuery;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.PSBOMViewRevision;
import com.teamcenter.soa.client.model.strong.PSViewType;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class T4PUtils {

	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static String DEFAULT_BOM_VIEW_TYPE = "view";

	public static String[] getPropertyValues(AppXSession session, Property propObj) {

		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		PropertyDescription pd = propObj.getPropertyDescription();
		String attr_name = pd.getName();
		int srvPropType = pd.getType();
		List<String> list = new ArrayList<String>();

		if (!propObj.isNull()) {
			switch (srvPropType) {

			case PropertyDescription.SERVER_PROP_double:
				double d = propObj.getDoubleValue();
				list.add(String.format("%.3f", d));
				break;

			case PropertyDescription.SERVER_PROP_date:
				Calendar cal = propObj.getCalendarValue();
				if (cal != null)
					list.add(sdf.format(cal.getTime()));
				break;

			case PropertyDescription.SERVER_PROP_logical:
				list.add(propObj.getBoolValue() ? "Y" : "N");
				break;

			case PropertyDescription.SERVER_PROP_external_reference:
			case PropertyDescription.SERVER_PROP_typed_reference:
			case PropertyDescription.SERVER_PROP_typed_relation:
			case PropertyDescription.SERVER_PROP_untyped_reference:
			case PropertyDescription.SERVER_PROP_untyped_relation:

				if (!pd.isArray()) {
					ModelObject obj = propObj.getModelObjectValue();
					if (obj != null) {
						if (obj instanceof User) {
							try {
								dmService.getProperties(new ModelObject[] { obj }, new String[] { "user_id" });
								list.add(((User) obj).get_user_id());
							} catch (NotLoadedException e) {
								e.printStackTrace();
							}
						} else {
							list.add(propObj.getDisplayableValue());
						}
					}

				} else {
					ModelObject[] objs = propObj.getModelObjectArrayValue();
					if (objs.length > 0) {
						if (attr_name.equals("S8_XPT_Buyer_Relation")) {

							dmService.getProperties(objs, new String[] { "s8_XPT_BuyerEmail" });
							for (ModelObject obj : objs) {
								try {
									list.add(obj.getPropertyDisplayableValue("s8_XPT_BuyerEmail"));
								} catch (NotLoadedException e) {
									e.printStackTrace();
								}
							}

						} else if (attr_name.equals("S8_XPT_Supplier_Relation")) {

							dmService.getProperties(objs, new String[] { "s8_XPT_Supplier_ID" });
							for (ModelObject obj : objs) {
								try {
									list.add(obj.getPropertyDisplayableValue("s8_XPT_Supplier_ID"));
								} catch (NotLoadedException e) {
									e.printStackTrace();
								}
							}
						} else {
							// (attr_name.equals("release_status_list"))
							list.addAll(propObj.getDisplayableValues());

						}
					}
				}
				break;

			case PropertyDescription.SERVER_PROP_string:
				list.add(propObj.getStringValue());
				break;

			case PropertyDescription.SERVER_PROP_int:
			case PropertyDescription.SERVER_PROP_note:
			case PropertyDescription.SERVER_PROP_char:
			default:
				list = propObj.getDisplayableValues();
			}
		}

		if (list.size() == 0)
			list.add(""); // 无值时保证返回空字符串值
		return list.toArray(new String[list.size()]);
	}

	public static void serviceDataErrorCheck(ServiceData serviceData) throws ServerException {
		if (serviceData == null)
			return;

		int j = serviceData.sizeOfPartialErrors();
		if (j > 0) {
			StringBuilder localStringBuilder = new StringBuilder();
			for (int m = 0; m < j; m++) {
				ErrorStack localErrorStack = serviceData.getPartialError(m);
				int[] arrayOfInt = localErrorStack.getCodes();
				for (int n = 0; n < arrayOfInt.length; n++) {
					if (arrayOfInt[n] == 48053)
						continue;
					String str = localErrorStack.getMessages()[n];
					localStringBuilder.append(str);
				}
			}
			if (localStringBuilder.length() > 0)
				throw new ServerException(localStringBuilder.toString());
		}
	}

	public static void main(String[] args) throws Exception {
		AppXSession session = SessionPoolManager.getUserSession();
		getUserById(session, "testemail");
	}

	public static User getUserById(AppXSession session, String user_id) throws ServiceException {
		User user = null;

		ICTService ictService = ICTService.getService(session.getConnection());
		Arg[] args = new Arg[3];
		args[0] = new Arg();
		args[0].val = "User";
		args[1] = new Arg();
		args[1].val = "TYPE::User::User::POM_user";
		args[2] = new Arg();
		args[2].val = user_id;

		InvokeICTMethodResponse userresp = ictService.invokeICTMethod("ICCTUser", "find", args);

		String uid = null;
		if (userresp.output.length > 0 && userresp.output[0].structure.length > 0) {

			Structure re0 = userresp.output[0].structure[0];
			uid = re0.args[1].val;

		} else {

			// id查不到，用email查找
			SavedQueryService queryService = SavedQueryService.getService(session.getConnection());
			FindSavedQueriesCriteriaInput queryCI = new FindSavedQueriesCriteriaInput();
			queryCI.queryNames = new String[] { "___find_user_by_email" };
			FindSavedQueriesCriteriaInput[] qCIs = new FindSavedQueriesCriteriaInput[1];
			qCIs[0] = queryCI;
			FindSavedQueriesResponse resp = queryService.findSavedQueries(qCIs);
			if (resp.savedQueries.length > 0) {

				ImanQuery qry = resp.savedQueries[0];

				QueryInput savedQueryInput[] = new QueryInput[1];
				savedQueryInput[0] = new QueryInput();
				savedQueryInput[0].query = qry;
				savedQueryInput[0].maxNumToReturn = 1;
				savedQueryInput[0].limitList = new ModelObject[0];
				savedQueryInput[0].entries = new String[] { "_email" };
				savedQueryInput[0].values = new String[] { user_id + "@*" };

				SavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
				if (savedQueryResult.arrayOfResults.length > 0
						&& savedQueryResult.arrayOfResults[0].objectUIDS.length > 0) {
					uid = savedQueryResult.arrayOfResults[0].objectUIDS[0];
				}

			} else {
				LoggerDefault.logError("Teamcenter could not find Query : ___find_user_by_email");
			}

		}

		if (uid != null) {
			DataManagementService dmService = DataManagementService.getService(session.getConnection());
			dmService.loadObjects(new String[] { uid });
			ModelObject obj = session.getConnection().getModelManager().getObject(uid);
			if (obj instanceof User)
				user = (User) obj;
		}

		return user;
	}

	public static Group getGrpById(AppXSession session, String grp_id) throws ServiceException {
		ICTService ictService = ICTService.getService(session.getConnection());
		Arg[] args = new Arg[3];
		args[0] = new Arg();
		args[0].val = "Group";
		args[1] = new Arg();
		args[1].val = "TYPE::Group::Group::POM_group";
		args[2] = new Arg();
		args[2].val = grp_id;

		InvokeICTMethodResponse userresp = ictService.invokeICTMethod("ICCTGroup", "find", args);

		Structure re0 = userresp.output[0].structure[0];
		String uid = re0.args[1].val;

		ModelObject obj = session.getConnection().getModelManager().getObject(uid);

		return (Group) obj;
	}

	// 版本输入为空时，返回最新版
	// TODO need confirm 当输入版本字符含数字时，视为CR号，按CR-Part逻辑查找:
	// 当返回EngPart时，在所有版本中遍历cr_num字段；当返回cadPart时，遍历解决方案零组件关系，并取对应EngPart版本
	// 将符合条件版本重新覆盖返回结果
	@SuppressWarnings("unchecked")
	public static GetItemFromAttributeResponse getItemRevision(DataManagementService dmService, String item_id,
			String item_revision_id) throws ServerException {

		GetItemFromAttributeResponse resp = null;

		if (item_revision_id.matches(".*\\d{3}.*")) { // TODO need confirm 至少3位数字

			String cr_num = item_revision_id;
			resp = getItemAllRevs(dmService, item_id);

			if (resp.output.length > 0) {
				replaceResponseByCRNum(dmService, resp, item_id, cr_num);
			}

		} else {

			GetItemFromAttributeInfo[] itemAttrInfo = new GetItemFromAttributeInfo[1];
			itemAttrInfo[0] = new GetItemFromAttributeInfo();
			itemAttrInfo[0].itemAttributes.put("item_id", item_id);
			itemAttrInfo[0].revIds = new String[1];
			itemAttrInfo[0].revIds[0] = item_revision_id;
			int i = (item_revision_id == null || item_revision_id.isEmpty()) ? 1 : 0;
			GetItemFromIdPref pref = new GetItemFromIdPref();
			resp = dmService.getItemFromAttribute(itemAttrInfo, i, pref);
		}

		// serviceDataErrorCheck(localGetItemFromAttributeResponse.serviceData);
		return resp;
	}

	public static void replaceResponseByCRNum(DataManagementService dmService, GetItemFromAttributeResponse resp,
			String item_id, String cr_num) throws ServerException {
		Item item = resp.output[0].item;
		ModelObject[] objs = new ModelObject[resp.output[0].itemRevOutput.length];
		for (int i = 0; i < resp.output[0].itemRevOutput.length; i++) {
			objs[i] = resp.output[0].itemRevOutput[i].itemRevision;
		}
		ArrayList<ModelObject> realResults = new ArrayList<ModelObject>();

		if (item.getTypeObject().isInstanceOf("S8_XPT_ENGPart")) {

			dmService.refreshObjects(objs);
			dmService.getProperties(objs, new String[] { "s8_ZT_CR_Num" });
			for (ModelObject obj : objs) {
				try {
					String thiscrnum = obj.getPropertyDisplayableValue("s8_ZT_CR_Num");
					if (cr_num.equals(thiscrnum)) {
						realResults.add(obj);
						break;
					}
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}
			}

		} else {

			// 视为cadPart
			// 先以+_ENG的方式查找ENGPart，若符合则直接返回
			GetItemFromAttributeResponse partCRsResp = getItemAllRevs(dmService, item_id + "_ENG");
			if (partCRsResp.output.length > 0 && partCRsResp.output[0].itemRevOutput.length > 0) {
				ModelObject[] partCRobjs = new ModelObject[partCRsResp.output[0].itemRevOutput.length];
				for (int i = 0; i < partCRsResp.output[0].itemRevOutput.length; i++) {
					partCRobjs[i] = partCRsResp.output[0].itemRevOutput[i].itemRevision;
				}
				dmService.refreshObjects(partCRobjs);
				dmService.getProperties(partCRobjs, new String[] { "s8_ZT_CR_Num" });
				for (ModelObject obj : partCRobjs) {
					try {
						String thiscrnum = obj.getPropertyDisplayableValue("s8_ZT_CR_Num");
						if (cr_num.equals(thiscrnum)) {
							realResults.add(obj);
							break;
						}
					} catch (NotLoadedException e) {
						e.printStackTrace();
					}
				}
				resp.output[0].item = partCRsResp.output[0].item;

			} else {

				// 查找cadPart解决方案零组件
				ExpandGRMRelationsPref expandInfo = new ExpandGRMRelationsPref();
				expandInfo.expItemRev = false;
				expandInfo.info = new RelationAndTypesFilter2[] { new RelationAndTypesFilter2() };
				expandInfo.info[0].relationName = "CMHasSolutionItem";
				// 展开主对象
				ExpandGRMRelationsResponse relResp = dmService.expandGRMRelationsForSecondary(objs, expandInfo);
				for (ExpandGRMRelationsOutput output : relResp.output) {

					ModelObject inObject = output.inputObject;
					for (ExpandGRMRelationsData data : output.otherSideObjData) {
						String relName = data.relationName;
						if ("CMHasSolutionItem".equals(relName)) {
							for (ModelObject otherObj : data.otherSideObjects) {
								dmService.getProperties(new ModelObject[] { otherObj }, new String[] { "item_id" });
								try {
									String thiscrnum = otherObj.getPropertyDisplayableValue("item_id");
									if (cr_num.equals(thiscrnum)) {
										// realResults.add(inObject);

										ItemRevision engpart = getEngPartRevisionByCADPart(
												SessionPoolManager.getDefaultSession(), inObject);
										if (engpart == null) {
											// TODO need confirm 按常规逻辑找不到，视为历史数据
											dmService.getProperties(new ModelObject[] { inObject },
													new String[] { "item_revision_id" });
											String thisRevID = inObject.getPropertyDisplayableValue("item_revision_id");
											engpart = _getEngPartRevision_old(SessionPoolManager.getDefaultSession(),
													item_id, thisRevID);
										}

										// 更新ENGPart
										HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
										VecStruct struct1 = new VecStruct();
										struct1.stringVec = new String[] { cr_num };
										valueMap.put("s8_ZT_CR_Num", struct1);
										dmService.setProperties(new ModelObject[] { engpart }, valueMap);

										realResults.add(engpart);

										dmService.getProperties(new ModelObject[] { engpart },
												new String[] { "items_tag" });
										Item engItem = engpart.get_items_tag();
										resp.output[0].item = engItem;

										break;
									}
								} catch (NotLoadedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

			}

		}

		// 覆盖GetItemFromAttributeResponse
		resp.output[0].itemRevOutput = genItemRevOutput(realResults);
	}

	private static com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeItemRevOutput[] genItemRevOutput(
			ArrayList<ModelObject> realResults) {
		com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeItemRevOutput[] outputs = new com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeItemRevOutput[realResults
				.size()];

		for (int i = 0; i < realResults.size(); i++) {
			outputs[i] = new com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeItemRevOutput();
			outputs[i].itemRevision = (ItemRevision) realResults.get(i);
		}

		return outputs;
	}

	@SuppressWarnings("unchecked")
	public static GetItemFromAttributeResponse getItemAllRevs(DataManagementService dmService, String item_id)
			throws ServerException {

		GetItemFromAttributeInfo[] itemAttrInfo = new GetItemFromAttributeInfo[1];
		itemAttrInfo[0] = new GetItemFromAttributeInfo();
		itemAttrInfo[0].itemAttributes.put("item_id", item_id);
		int i = -1;
		GetItemFromIdPref pref = new GetItemFromIdPref();
		GetItemFromAttributeResponse resp = null;
		resp = dmService.getItemFromAttribute(itemAttrInfo, i, pref);

		return resp;
	}

	public static void updateRelations(AppXSession session, String relationType, ModelObject primaryObject,
			ArrayList<ModelObject> secondaryList) throws ServerException {
		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		CreateOrUpdateRelationsInfo[] relInfo = new CreateOrUpdateRelationsInfo[] { new CreateOrUpdateRelationsInfo() };
		relInfo[0].clientId = "Portal";
		relInfo[0].relationType = relationType;
		relInfo[0].primaryObject = primaryObject;
		relInfo[0].secondaryData = new SecondaryData[secondaryList.size()];
		for (int i = 0; i < secondaryList.size(); i++) {
			ModelObject secObj = secondaryList.get(i);
			relInfo[0].secondaryData[i] = new SecondaryData();
			relInfo[0].secondaryData[i].clientId = "Portal";
			relInfo[0].secondaryData[i].secondary = secObj;
		}

		CreateOrUpdateRelationsResponse resp = dmService.createOrUpdateRelations(relInfo, true);

		T4PUtils.serviceDataErrorCheck(resp.serviceData);
	}

	public static ArrayList<ModelObject> searchWithKeys(AppXSession session, String typeName, String propName,
			String[] values) throws ServerException {

		ArrayList<ModelObject> resultList = new ArrayList<ModelObject>();

		if (values.length < 1 || values[0].isEmpty())
			return resultList;

		SavedQueryService queryService = SavedQueryService.getService(session.getConnection());
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		BusinessObjectQueryInput[] input = new BusinessObjectQueryInput[] { new BusinessObjectQueryInput() };
		input[0].clientId = "Portal";
		input[0].typeName = typeName;
		input[0].maxNumToReturn = 0;
		input[0].clauses = new BusinessObjectQueryClause[values.length];

		for (int i = 0; i < values.length; i++) {
			input[0].clauses[i] = new BusinessObjectQueryClause();
			input[0].clauses[i].propName = propName;
			input[0].clauses[i].propValue = values[i];
			input[0].clauses[i].mathOperator = "=";
			input[0].clauses[i].logicOperator = "OR";
		}
		SavedQueriesResponse resp = queryService.executeBusinessObjectQueries(input);

		T4PUtils.serviceDataErrorCheck(resp.serviceData);

		for (QueryResults results : resp.arrayOfResults) {

			ServiceData sdata = dmService.loadObjects(results.objectUIDS);
			T4PUtils.serviceDataErrorCheck(sdata);

			int num = sdata.sizeOfPlainObjects();
			for (int i = 0; i < num; i++) {

				try {
					if (sdata.getPlainObject(i) instanceof WorkspaceObject) {
						dmService.getProperties(new ModelObject[] { sdata.getPlainObject(i) },
								new String[] { "active_seq" });
						if (sdata.getPlainObject(i).getPropertyObject("active_seq").getIntValue() != 0)
							resultList.add(sdata.getPlainObject(i));
					} else {
						resultList.add(sdata.getPlainObject(i));
					}
				} catch (NotLoadedException e) {
					e.printStackTrace();
				}
			}
		}
		return resultList;
	}

	public static void updateBuyers(AppXSession session, ItemRevision rev, String[] attr_values) throws ServerException {

		// search buyer
		ArrayList<ModelObject> secondaryList = searchWithKeys(session, "S8_XPT_Buyer", "s8_XPT_BuyerEmail", attr_values);

		// update relation
		updateRelations(session, "S8_XPT_Buyer_Relation", rev, secondaryList);

	}

	public static void updateSuppliers(AppXSession session, ItemRevision rev, String[] attr_values)
			throws ServerException {

		ArrayList<ModelObject> secondaryList = searchWithKeys(session, "S8_XPT_Supplier", "s8_XPT_Supplier_ID",
				attr_values);

		updateRelations(session, "S8_XPT_Supplier_Relation", rev, secondaryList);
	}

	public static void updateMaterials(AppXSession session, ItemRevision rev, String material_name, String material_spec)
			throws ServerException, ServiceException {

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
		savedQueryInput[0].maxNumToReturn = 1;
		savedQueryInput[0].limitList = new ModelObject[0];
		savedQueryInput[0].entries = new String[] { "Name", "MatName", "MatSpec" };
		savedQueryInput[0].values = new String[] { "S8_Released", material_name, material_spec };

		SavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
		T4PUtils.serviceDataErrorCheck(savedQueryResult.serviceData);

		ArrayList<ModelObject> resultList = new ArrayList<ModelObject>();
		for (QueryResults results : savedQueryResult.arrayOfResults) {

			ServiceData sdata = dmService.loadObjects(results.objectUIDS);
			T4PUtils.serviceDataErrorCheck(sdata);

			if (sdata.sizeOfPlainObjects() > 0) {
				resultList.add(sdata.getPlainObject(0));
				break;
			}
		}

		if (resultList.size() > 0) {
			updateRelations(session, "S8_BasicMaterialRelation", rev, resultList);
		}
	}

	public static void setbypass(AppXSession session, String bp) throws ServiceException {

		ICTService ictService = ICTService.getService(session.getConnection());

		// bypass
		Arg[] args = new Arg[2];
		args[0] = new Arg();
		args[0].val = "s8_set_bypass";
		args[1] = new Arg();
		args[1].structure = new Structure[1];
		args[1].structure[0] = new Structure();
		args[1].structure[0].args = new Arg[2];
		args[1].structure[0].args[0] = new Arg();
		args[1].structure[0].args[0].val = Boolean.toString(true);
		args[1].structure[0].args[1] = new Arg();
		args[1].structure[0].args[1].array = new Array[1];
		args[1].structure[0].args[1].array[0] = new Array();
		args[1].structure[0].args[1].array[0].entries = new Entry[1];
		args[1].structure[0].args[1].array[0].entries[0] = new Entry();
		args[1].structure[0].args[1].array[0].entries[0].val = bp;
		ictService.invokeICTMethod("ICCTUserService", "callMethod", args);

	}

	public static void refreshPref(AppXSession session) throws ServiceException {
		PreferenceManagementService pmSrv = PreferenceManagementService.getService(session.getConnection());
		pmSrv.refreshPreferences();
	}

	public static String getPref(AppXSession session, String key) throws ServiceException {
		PreferenceManagementService pmSrv = PreferenceManagementService.getService(session.getConnection());
		GetPreferencesResponse resp = pmSrv.getPreferences(new String[] { key }, false);
		if (resp.response.length > 0 && resp.response[0].values.values.length > 0) {

			String value = resp.response[0].values.values[0];
			return value;
		}

		return null;
	}

	public static String[] getPrefs(AppXSession session, String key) throws ServiceException {
		PreferenceManagementService pmSrv = PreferenceManagementService.getService(session.getConnection());
		GetPreferencesResponse resp = pmSrv.getPreferences(new String[] { key }, false);
		if (resp.response.length > 0) {

			return resp.response[0].values.values;
		}

		return null;
	}

	public static void printT4PAttrProperty(T4PAttrProperty attr) {
		String name = attr.getAttr_name();
		// String disp = attr.getAttr_display_name();
		String[] values = attr.getAttr_values();

		String v = "";
		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				v += ",";
			v += values[i];
		}

		LoggerDefault.logInfo("\t" + name + "\t:\t" + v);
	}

	public static ModelObject createWorkflow(AppXSession session, String workflowName, String jobName, ModelObject obj)
			throws ServiceException, ServerException {
		WorkflowService wfSrv = WorkflowService.getService(session.getConnection());

		ContextData flowData = new ContextData();
		flowData.processTemplate = workflowName;//
		flowData.attachmentCount = 1;
		flowData.attachments = new String[] { obj.getUid() };
		flowData.attachmentTypes = new int[] { 1 }; // EPM_target_attachment
		flowData.processOwner = "";
		flowData.processAssignmentList = "";
		InstanceInfo info = wfSrv.createInstance(true, "Portal", jobName, "", "", flowData);

		serviceDataErrorCheck(info.serviceData);

		return info.serviceData.getCreatedObject(0);
	}

	public static ModelObject createWorkflow2(AppXSession session, String workflowName, String jobName,
			ModelObject[] objs) throws ServiceException, ServerException {
		WorkflowService wfSrv = WorkflowService.getService(session.getConnection());

		ContextData flowData = new ContextData();
		flowData.processTemplate = workflowName;//
		flowData.attachmentCount = 1;
		// flowData.attachments = new String[] { obj.getUid() };
		flowData.attachments = new String[objs.length];
		for (int i = 0; i < objs.length; i++)
			flowData.attachments[i] = objs[i].getUid();
		flowData.attachmentTypes = new int[] { 1 }; // EPM_target_attachment
		flowData.processOwner = "";
		flowData.processAssignmentList = "";
		InstanceInfo info = wfSrv.createInstance(true, "Portal", jobName, "", "", flowData);

		serviceDataErrorCheck(info.serviceData);

		return info.serviceData.getCreatedObject(0);
	}

	public static boolean isInteger(String str) {

		try {
			double dd = Double.parseDouble(str);
			int ii = (int) dd;

			return (dd == ii);

		} catch (Exception e) {

			return false;
		}

	}

	public static int parseIntEx(String str) {
		try {
			double dd = Double.parseDouble(str);
			int ii = (int) dd;

			return ii;

		} catch (Exception e) {

			return 0;
		}
	}

	public static PSBOMViewRevision createViewRevision(AppXSession session, Item item, ItemRevision itemRevision)
			throws ServerException {
		PSViewType type = T4PUtils.getDefaultViewType(session, item, itemRevision);
		if (type != null) {
			com.teamcenter.services.internal.strong.structuremanagement.StructureService inSSrv = com.teamcenter.services.internal.strong.structuremanagement.StructureService
					.getService(session.getConnection());
			CreateOrSaveAsPSBOMViewRevisionInput[] viewInput = new CreateOrSaveAsPSBOMViewRevisionInput[] { new CreateOrSaveAsPSBOMViewRevisionInput() };

			viewInput[0].clientId = "Portal";
			viewInput[0].isPrecise = true; // 精确
			viewInput[0].itemObject = item;
			viewInput[0].itemRevObj = itemRevision;
			viewInput[0].viewTypeTag = type;
			CreateOrSaveAsPSBOMViewRevisionResponse resp = inSSrv.createOrSavePSBOMViewRevision(viewInput);
			serviceDataErrorCheck(resp.serviceData);

			return resp.psBVROutputs[0].bvrTag;
		}
		return null;
	}

	public static PSViewType getDefaultViewType(AppXSession session, Item item, ItemRevision itemRevision) {

		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		com.teamcenter.services.internal.strong.structuremanagement.StructureService inSSrv = com.teamcenter.services.internal.strong.structuremanagement.StructureService
				.getService(session.getConnection());

		try {
			GetAllAvailableViewTypesInput[] viewTypeInput = new GetAllAvailableViewTypesInput[] { new GetAllAvailableViewTypesInput() };
			viewTypeInput[0].clientId = "Portal";
			viewTypeInput[0].itemObject = item;
			viewTypeInput[0].itemRevisionObj = itemRevision;
			GetAvailableViewTypesResponse typeResp = inSSrv.getAvailableViewTypes(viewTypeInput);
			serviceDataErrorCheck(typeResp.serviceData);
			PSViewType[] tags = typeResp.viewTypesOutputs[0].viewTags;
			dmService.getProperties(tags, new String[] { "name" });
			for (PSViewType tag : tags) {
				String typeName = tag.get_name();
				if (typeName.equals(DEFAULT_BOM_VIEW_TYPE)) {
					return tag;
				}
			}
		} catch (ServerException e) {
			e.printStackTrace();
		} catch (NotLoadedException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 此方法仅用于过渡时期历史数据自动处理
	// 调用前提为对应item_id已存在且为cad
	public static ItemRevision _getEngPartRevision_old(AppXSession session, String item_id, String item_revision_id)
			throws ServerException {

		SavedQueryService queryService = SavedQueryService.getService(session.getConnection());
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		BusinessObjectQueryInput[] input = new BusinessObjectQueryInput[] { new BusinessObjectQueryInput() };
		input[0].clientId = "Portal";
		input[0].typeName = "S8_XPT_ENGPartRevision";
		input[0].maxNumToReturn = 0;
		input[0].clauses = new BusinessObjectQueryClause[3];

		input[0].clauses[0] = new BusinessObjectQueryClause();
		input[0].clauses[0].propName = "s8_ZT_linkedCADPartNo";
		input[0].clauses[0].propValue = item_id;
		input[0].clauses[0].mathOperator = "=";
		input[0].clauses[0].logicOperator = "AND";

		input[0].clauses[1] = new BusinessObjectQueryClause();
		input[0].clauses[1].propName = "s8_ZT_linkedCADRevID";
		input[0].clauses[1].propValue = item_revision_id;
		input[0].clauses[1].mathOperator = "=";
		input[0].clauses[1].logicOperator = "AND";

		input[0].clauses[2] = new BusinessObjectQueryClause();
		input[0].clauses[2].propName = "active_seq";
		input[0].clauses[2].propValue = "0";
		input[0].clauses[2].mathOperator = "!=";
		input[0].clauses[2].logicOperator = "AND";

		SavedQueriesResponse resp = queryService.executeBusinessObjectQueries(input);
		ArrayList<ModelObject> resultList = new ArrayList<ModelObject>();
		for (QueryResults results : resp.arrayOfResults) {

			ServiceData sdata = dmService.loadObjects(results.objectUIDS);

			int num = sdata.sizeOfPlainObjects();
			for (int i = 0; i < num; i++) {
				resultList.add(sdata.getPlainObject(i));
			}
		}

		if (resultList.size() > 0) {
			// 找到，返回最高版
			if (resultList.size() == 1) {
				return (ItemRevision) resultList.get(0);
			}

			dmService.getProperties(resultList.toArray(new ModelObject[resultList.size()]),
					new String[] { "item_revision_id" });
			String currentVer = "";
			ModelObject currentRev = resultList.get(0);
			for (ModelObject thisRev : resultList) {
				try {
					// 版本号 直接compare
					String thisVer = thisRev.getPropertyDisplayableValue("item_revision_id");
					if (thisVer.compareTo(currentVer) > 0) {
						currentVer = thisVer;
						currentRev = thisRev;
					}

				} catch (NotLoadedException e) {
					e.printStackTrace();
				}
			}
			return (ItemRevision) currentRev;

		} else {

			// 未找到，查找
			GetItemFromAttributeResponse resp2 = getItemRevision(dmService, item_id + "_ENG", item_revision_id);

			ItemRevision returnRev = null;

			if (resp2.output.length < 1) {

				// _ENG不存在，新建
				ItemProperties[] props = new ItemProperties[1];
				props[0] = new ItemProperties();
				props[0].clientId = "Portal";
				// props[0].type = T4PModelDefinitions.getEBOMTypeByCADType(cadType);
				props[0].type = "S8_XPT_ENGPart"; // 直接使用父类以规避命名规则
				props[0].itemId = item_id + "_ENG";
				props[0].revId = item_revision_id;
				CreateItemsResponse respCre = dmService.createItems(props, null, null);
				returnRev = respCre.output[0].itemRev;

			} else if (resp2.output[0].itemRevOutput.length < 1) {

				// 无相应版本，取最高版修订
				resp2 = getItemRevision(dmService, item_id + "_ENG", "");
				ItemRevision oldRev = resp2.output[0].itemRevOutput[0].itemRevision;

				ReviseInfo[] reviseInfos = new ReviseInfo[] { new ReviseInfo() };
				reviseInfos[0].baseItemRevision = oldRev;
				reviseInfos[0].clientId = "Portal";
				reviseInfos[0].newRevId = item_revision_id;

				ReviseResponse2 reviseResp = dmService.revise2(reviseInfos);
				ReviseOutput reviseOutput = (ReviseOutput) reviseResp.reviseOutputMap.get("Portal");
				returnRev = reviseOutput.newItemRev;

			} else {

				// 存在
				returnRev = resp2.output[0].itemRevOutput[0].itemRevision;

			}

			// 刷新其属性
			HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
			VecStruct struct1 = new VecStruct();
			struct1.stringVec = new String[] { item_id };
			valueMap.put("s8_ZT_linkedCADPartNo", struct1);
			VecStruct struct2 = new VecStruct();
			struct2.stringVec = new String[] { item_revision_id };
			valueMap.put("s8_ZT_linkedCADRevID", struct2);
			dmService.setProperties(new ModelObject[] { returnRev }, valueMap);

			// item和版本挂接S8_DesignEngLinkage_Rel关系
			try {
				dmService.getProperties(new ModelObject[] { returnRev }, new String[] { "items_tag" });
				Item engItem = returnRev.get_items_tag();

				GetItemFromAttributeResponse cadResp = getItemRevision(dmService, item_id, item_revision_id);
				Item cadItem = cadResp.output[0].item;

				CreateOrUpdateRelationsInfo[] relInfo = new CreateOrUpdateRelationsInfo[] { new CreateOrUpdateRelationsInfo() };
				relInfo[0].clientId = "Portal";
				relInfo[0].relationType = "S8_DesignEngLinkage_Rel";
				relInfo[0].primaryObject = cadItem;
				relInfo[0].secondaryData = new SecondaryData[] { new SecondaryData() };
				relInfo[0].secondaryData[0] = new SecondaryData();
				relInfo[0].secondaryData[0].clientId = "Portal";
				relInfo[0].secondaryData[0].secondary = engItem;
				CreateOrUpdateRelationsResponse relResp = dmService.createOrUpdateRelations(relInfo, true);
				serviceDataErrorCheck(relResp.serviceData);

				ItemRevision cadRev = cadResp.output[0].itemRevOutput[0].itemRevision;

				relInfo = new CreateOrUpdateRelationsInfo[] { new CreateOrUpdateRelationsInfo() };
				relInfo[0].clientId = "Portal";
				relInfo[0].relationType = "S8_DesignEngLinkage_Rel";
				relInfo[0].primaryObject = cadRev;
				relInfo[0].secondaryData = new SecondaryData[] { new SecondaryData() };
				relInfo[0].secondaryData[0] = new SecondaryData();
				relInfo[0].secondaryData[0].clientId = "Portal";
				relInfo[0].secondaryData[0].secondary = returnRev;
				relResp = dmService.createOrUpdateRelations(relInfo, true);
				serviceDataErrorCheck(relResp.serviceData);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return returnRev;
		}

	}

	public static ItemRevision getEngPartRevisionByCADPart(AppXSession session, ModelObject obj) throws ServerException {

		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		ExpandGRMRelationsPref expandInfo = new ExpandGRMRelationsPref();
		expandInfo.expItemRev = false;
		expandInfo.info = new RelationAndTypesFilter2[] { new RelationAndTypesFilter2() };
		expandInfo.info[0].relationName = "S8_DesignEngLinkage_Rel";
		// 展开次对象
		ExpandGRMRelationsResponse relResp = dmService.expandGRMRelationsForPrimary(new ModelObject[] { obj },
				expandInfo);

		if (relResp.output[0].otherSideObjData[0].otherSideObjects.length == 1) {

			ModelObject secondObj = relResp.output[0].otherSideObjData[0].otherSideObjects[0];

			if (secondObj instanceof ItemRevision) {
				return (ItemRevision) secondObj;
			}

			// // 若是item，返回最高版
			// if (secondObj instanceof Item) {
			//
			// try {
			// dmService.getProperties(new ModelObject[] { secondObj }, new String[] { "revision_list" });
			// ModelObject[] revs = ((Item) secondObj).get_revision_list();
			// dmService.getProperties(revs, new String[] { "item_revision_id" });
			// String currentVer = "";
			// ModelObject currentRev = revs[0];
			// for (ModelObject thisRev : revs) {
			// // 由于版本号均为两位字母，直接compare
			// String thisVer = thisRev.getPropertyDisplayableValue("item_revision_id");
			// if (thisVer.compareTo(currentVer) > 0) {
			// currentVer = thisVer;
			// currentRev = thisRev;
			// }
			// }
			// return (ItemRevision) currentRev;
			//
			// } catch (NotLoadedException e) {
			// e.printStackTrace();
			// }
			//
			// }

			throw new ServerException(
					"S8_DesignEngLinkage_Rel Relation Error!! The secondary object is not Item/ItemRevision");

		} else if (relResp.output[0].otherSideObjData[0].otherSideObjects.length > 1) {

			throw new ServerException("S8_DesignEngLinkage_Rel Relation Error!! Multi-secondary objects");

		} else {

			// 无次对象

			// if (obj instanceof ItemRevision) {
			//
			// // 若是cad版本，用尝试用item展开
			// dmService.getProperties(new ModelObject[] { obj }, new String[] { "items_tag" });
			// try {
			// Item item = ((ItemRevision) obj).get_items_tag();
			//
			// return getEngPartRevisionByCADPart(session, item);
			//
			// } catch (NotLoadedException e) {
			// e.printStackTrace();
			// }
			//
			// }

			// 未关联，返回null
			return null;
		}

	}
	
	
	/**
	 * 查找零组件
	 * 
	 * @param item_id
	 * @return
	 */
	public static final Item findItem(AppXSession session, String item_id)
	{
		ModelObject[] modelObjects = query(session, "Item ID", new String[] { "Item ID" }, new String[] { item_id });
		if (modelObjects != null && modelObjects.length > 0)
			return (Item) modelObjects[0];
		return null;
	}
	
	/**
	 * 获取零组件最新版本
	 * 
	 * @param item
	 * @return
	 * @throws NotLoadedException
	 */
	public static final ItemRevision getLatestItemRev(AppXSession session, Item item) throws NotLoadedException
	{
		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		dmService.getProperties(new ModelObject[] { item },	new String[] { "revision_list" });
		ModelObject[] revision_list = item.get_revision_list();
		if (revision_list != null && revision_list.length > 0) 
			return (ItemRevision) revision_list[revision_list.length - 1];
		return null;
	}
	
	/**
	 * 获取零组件最新版本
	 * 
	 * @param item
	 * @return
	 * @throws NotLoadedException
	 */
	public static final ItemRevision getCRAARev(AppXSession session, Item crItem) throws NotLoadedException
	{
		ItemRevision crAARev = null;
		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		dmService.getProperties(new ModelObject[] { crItem },	new String[] { "revision_list" });
		ModelObject[] revision_list = crItem.get_revision_list();
		if (revision_list != null && revision_list.length > 0) 
		{
			for (ModelObject modelObject : revision_list) 
			{
				ItemRevision itemRevision = (ItemRevision) modelObject;
				dmService.getProperties(new ModelObject[] { itemRevision }, new String[] { "item_revision_id" });
				String item_revision_id = itemRevision.get_item_revision_id();
				if (item_revision_id.equals("AA")) {
					crAARev = itemRevision;
					break;
				}
			}
		}
		return crAARev;
	}

	/**
	 * 调用系统查询
	 * 
	 * @param queryName
	 * @param keys
	 * @param values
	 * @return
	 */
	public static final ModelObject[] query(AppXSession session, String queryName, String[] keys, String[] values) 
	{
		ImanQuery query = null;

		SavedQueryService queryService = SavedQueryService.getService(session.getConnection());
		try 
		{
			GetSavedQueriesResponse savedQueries = queryService.getSavedQueries();

			if (savedQueries.queries.length == 0) 
			{
				System.out.println("There are no saved queries in the system.");
				return null;
			}

			for (int i = 0; i < savedQueries.queries.length; i++) 
			{
				if (savedQueries.queries[i].name.equals(queryName)) 
				{
					query = savedQueries.queries[i].query;
					break;
				}
			}
		} catch (ServiceException e) {
			System.out.println("GetSavedQueries service request failed.");
			System.out.println(e.getMessage());
			return null;
		}

		if (query == null) {
			System.out.println("There is not an " + queryName + " query.");
			return null;
		}

		try {
			Vector<String> newKeys = new Vector<String>();
			DescribeSavedQueriesResponse desc = queryService.describeSavedQueries(new ImanQuery[] {query});
			SavedQueryFieldListObject fieldLists = desc.fieldLists[0];
			SavedQueryFieldObject[] fields = fieldLists.fields;
			
			for (int i = 0; i < keys.length; i++) 
			{
				String key = keys[i];
				String newKey = key;
				for (int j = 0; j < fields.length; j++) 
				{
					SavedQueryFieldObject field = fields[j];
					if (field.attributeName.equals(key))
					{
						newKey = field.entryName;
						System.out.println("info:" + newKey);
					}
				}
				newKeys.add(newKey);
			}
			
			SavedQueryInput[] savedQueryInput = new SavedQueryInput[1];
			savedQueryInput[0] = new SavedQueryInput();
			savedQueryInput[0].query = query;
			savedQueryInput[0].maxNumToReturn = 25;
			savedQueryInput[0].limitListCount = 0;
			savedQueryInput[0].limitList = new ModelObject[0];
			savedQueryInput[0].entries = newKeys.toArray(new String[newKeys.size()]);
			savedQueryInput[0].values = values;
			savedQueryInput[0].maxNumToInflate = 25;

			ExecuteSavedQueriesResponse savedQueryResult = queryService.executeSavedQueries(savedQueryInput);
			SavedQueryResults found = savedQueryResult.arrayOfResults[0];
			
			return found.objects;
		} catch (Exception e) {
			System.out.println("ExecuteSavedQuery service request failed.");
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	
	/**
	 * 更改所有权
	 * 
	 * @param session
	 * @param user
	 * @param objs
	 * @throws NotLoadedException
	 */
	public static void changeOwnership(AppXSession session, User user, Group group, ModelObject[] objs) 
	{
		DataManagementService dmService = DataManagementService.getService(session.getConnection());
//		dmService.getProperties(new ModelObject[] { user }, new String[] { "default_group" });
//		ModelObject group = null;
//		try {
//			group = user.get_default_group();
//		} catch (NotLoadedException e) {
//			e.printStackTrace();
//		}
		
		ObjectOwner[] object_owner = new ObjectOwner[objs.length];
		for (int i = 0; i < object_owner.length; i++)
		{			
			ObjectOwner ow = new ObjectOwner();
			ow.owner = user;
			ow.group = group;
			ow.object = objs[i];
			object_owner[i] = ow;
		}
		ServiceData serd = dmService.changeOwnership(object_owner);
		if (serd.sizeOfPartialErrors() > 0)
		{
			System.out.println("error: " + serd.getPartialError(0).getMessages()[0]);
			return;
		}
	}
	
	/**
	 * 根据指定关系获取关联对象
	 * 
	 * @param object
	 * @param relation
	 * @return
	 * @throws Exception
	 */
	public static ModelObject[] getRelatedObjects(AppXSession session, ModelObject object, String relation) throws Exception
	{
		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		dmService.getProperties(new ModelObject[] { object }, new String[] { relation });
		ModelObject[] modelObjects = object.getPropertyObject(relation).getModelObjectArrayValue();
		return modelObjects;
	}
	
	public static void removeObjectByRelation(AppXSession session, ModelObject parentObj, String relation) throws Exception
	{
		DataManagementService dmService = DataManagementService.getService(session.getConnection());
		ModelObject[] modelObjects = getRelatedObjects(session, parentObj, relation);
		if (modelObjects != null && modelObjects.length > 0) 
		{
			ChildrenInputData[] inputDatas = new ChildrenInputData[] { new ChildrenInputData() };
			inputDatas[0].parentObj = parentObj;
			inputDatas[0].childrenObj = modelObjects;
			inputDatas[0].propertyName = relation;
			inputDatas[0].clientId = "Portal";
			dmService.removeChildren(inputDatas);
			dmService.refreshObjects(new ModelObject[] { parentObj });
		}
	}
	
	/**
	 * 根据关系关联对象
	 * 
	 * @param obj1
	 * @param obj2
	 * @param relationType
	 * @throws Exception
	 */
	public static void createRelation(AppXSession session, ModelObject obj1, ModelObject obj2, String relationType) throws Exception 
	{
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		Relationship relationship = new Relationship();
		relationship.primaryObject = obj1;
		relationship.secondaryObject = obj2;
		relationship.relationType = relationType;

		CreateRelationsResponse response = dmService.createRelations(new Relationship[] { relationship });

		if (response.serviceData.sizeOfPartialErrors() > 0) {
			throw new Exception(response.serviceData.getPartialError(0).getMessages()[0]);
		}
	}
}
