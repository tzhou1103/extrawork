package com.nio.tcserver.common;

import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;

import com.nio.tcserver.T4PAttrDesc;
import com.nio.tcserver.T4PAttrProperty;
import com.nio.tcserver.T4PCreatePartResp;
import com.nio.tcserver.T4PPartAttrsOutput;
import com.nio.tcserver.T4PRevisePartResp;
import com.nio.tcserver.T4PSetPartAttrsResp;
import com.nio.tcserver.T4PSetPartObsoleteResp;
import com.nio.tcserver.session.LoggerDefault;
import com.nio.tcserver.session.SessionPoolManager;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.strong.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.strong.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ReviseResponse2;
import com.teamcenter.services.strong.core._2009_10.DataManagement.GetItemFromAttributeResponse;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusInput;
import com.teamcenter.services.strong.workflow._2007_06.Workflow.ReleaseStatusOption;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class T4PFunction {

	public static T4PCreatePartResp getCreateInfo(String cadType, String owning_user, String owning_group, String crNum) throws ConnectException, ServerException,
			ServiceException {
		if (cadType == null || cadType.isEmpty()) {
			throw new ServiceException("Part type is empty!!");
		}

		String part_type = T4PModelDefinitions.getEBOMTypeByCADType(cadType);
		if (part_type == null || part_type.isEmpty()) {
			throw new ServiceException(cadType + " has not defined cad type!!");
		}
		
		if (owning_user == null || owning_user.isEmpty()) {
			throw new ServiceException("Parameter owning_user is null!!");
		}
		if (owning_group == null || owning_group.isEmpty()) {
			throw new ServiceException("Parameter owning_group is null!!");
		}
		
		if (crNum == null || crNum.isEmpty()) {
			throw new ServiceException("Parameter crNum is null!!");
		}

		AppXSession session = SessionPoolManager.getUserSession();

		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		ItemProperties[] props = new ItemProperties[1];
		props[0] = new ItemProperties();
		props[0].clientId = "Portal";
		props[0].type = part_type;
		props[0].revId = "TMPAA"; // TODO need confirm 指定为临时版本号

		String item_id = null;
		String item_revision_id = null;
		// String rev_object_type = null;
		try {
			CreateItemsResponse resp = dmService.createItems(props, null, null);
			ItemRevision rev = resp.output[0].itemRev;
			dmService.getProperties(new ModelObject[] { rev }, new String[] { "item_id", "item_revision_id",
					"object_type", "last_release_status" });
			item_id = rev.getPropertyDisplayableValue("item_id");
			item_revision_id = rev.getPropertyDisplayableValue("item_revision_id");
			// rev_object_type = rev.get_object_type();
			
			// 修改属性值, added by zhoutong, 2018-12-20
			HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
			VecStruct struct1 = new VecStruct();
			struct1.stringVec = new String[] { "No" };
			valueMap.put("s8_ZT_EBOMOnly", struct1);
			dmService.setProperties(new ModelObject[] { rev }, valueMap);
			
			// 修改对象所有权，added by zhoutong, 2018-12-17
			Item item = resp.output[0].item;
			User user = T4PUtils.getUserById(session, owning_user);
			if (user == null) {
				throw new ServiceException("Can not find user by user_id: " + owning_user + " !!");
			}
			
			Group group = T4PUtils.getGrpById(session, owning_group);
			if (group == null) {
				throw new ServiceException("Can not find group by group_name: " + owning_group + " !!");
			}
			
			T4PUtils.changeOwnership(session, user, group, new ModelObject[] { item, rev });
			
			// 增加关联CR对象，added by zhoutong, 2018-12-19
			Item crItem = T4PUtils.findItem(session, crNum);
			if (crItem == null) {
				throw new ServerException("Cannot find cr with crNum:" + crNum);
			}
			
			try {
				ItemRevision crAARev = T4PUtils.getCRAARev(session, crItem);
				if (crAARev == null) 
					throw new ServerException("Cannot find cr AA rev with crNum:" + crNum);
				T4PUtils.createRelation(session, rev, crAARev, "S8_XPT_Related_CR");
			} catch (NotLoadedException e1) {
				e1.printStackTrace();
			}
			// end add
			
			//添加临时状态
			WorkflowService wfSrv = WorkflowService.getService(session.getConnection());
			ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
			rsInput[0].objects = new WorkspaceObject[] { rev };
			rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
			rsInput[0].operations[0].operation = "Append";
			rsInput[0].operations[0].newReleaseStatusTypeName = "S8_Temporary";
			wfSrv.setReleaseStatus(rsInput);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException("Part type " + part_type + " is not define!!");
		}

		// 以下暂不需要
		// PropDescriptorService pdService = PropDescriptorService.getService(session.getConnection());
		//
		// ArrayList<T4PAttrDesc> attrDescList = new ArrayList<T4PAttrDesc>();
		//
		// CreateDescResponse cdResp = pdService.getCreateDesc(new String[] { rev_object_type });
		// for (CreateDesc createDesc : cdResp.createDescs) {
		// if (createDesc.businessObjectTypeName.equals(rev_object_type)) {
		//
		// attrDescList.add(new T4PAttrDesc("object_name", "Name", true));
		// for (PropDesc propDesc : createDesc.propDescs) {
		// if (!propDesc.propName.equals("item_revision_id")
		// && T4PModelDefinitions.partAttributes.contains(propDesc.propName))
		//
		// attrDescList.add(new T4PAttrDesc(T4PModelDefinitions.getPortalAttrName(propDesc.propName),
		// propDesc.displayName, propDesc.isRequired));
		// }
		// }
		// }
		//
		// return new T4PCreatePartResp(item_id, item_revision_id, attrDescList.toArray(new T4PAttrDesc[attrDescList
		// .size()]));

		return new T4PCreatePartResp(item_id, item_revision_id, new T4PAttrDesc[0]);
	}

	public static T4PPartAttrsOutput getPartAttrs(String item_id, String item_revision_id, String[] attr_names)
			throws ServiceException, ServerException, ConnectException {
		if (item_id == null || item_id.isEmpty()) {
			throw new ServiceException("item_id is empty!!");
		}
		if (item_revision_id == null || item_revision_id.isEmpty()) {
			throw new ServiceException("item_revision_id is empty!!");
		}

		ArrayList<String> nameList = new ArrayList<String>();
		for (int i = 0; i < attr_names.length; i++) {
			String name = T4PModelDefinitions.getTCAttrName_E(attr_names[i]);
			if (name != null && name.length() > 0 && T4PModelDefinitions.partAttributes_E.contains(name))
				nameList.add(name);
		}
		String[] real_attr_names = nameList.toArray(new String[nameList.size()]);

		AppXSession session = SessionPoolManager.getUserSession();

		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, item_id, item_revision_id);

		if (itemRevResp.output.length == 0
				|| !itemRevResp.output[0].item.getTypeObject().isInstanceOf("S8_XPT_ENGPart")) {
			// TODO need confirm 历史数据特殊处理
			GetItemFromAttributeResponse itemRevRespTmp = T4PUtils.getItemRevision(dmService, item_id + "_ENG",
					item_revision_id);
			if (itemRevRespTmp.output.length > 0 && itemRevRespTmp.output[0].itemRevOutput.length > 0)
				itemRevResp = itemRevRespTmp;
		}

		if (itemRevResp.output.length == 0) {

			throw new ServiceException("item not found!!");

		} else if (itemRevResp.output[0].itemRevOutput.length == 0) {

			throw new ServiceException("revision not found!!");

		} else {
			ArrayList<T4PAttrProperty> list = new ArrayList<T4PAttrProperty>();
			ItemRevision rev = itemRevResp.output[0].itemRevOutput[0].itemRevision;

			// TODO need confirm 历史数据特殊处理
			if (!rev.getTypeObject().isInstanceOf("S8_XPT_ENGPartRevision")) {
				rev = T4PUtils._getEngPartRevision_old(session, item_id, item_revision_id);
			}

			try {

				dmService.refreshObjects(new ModelObject[] { rev });

				dmService.getProperties(new ModelObject[] { rev }, real_attr_names);

				for (String attr_name : real_attr_names) {
					Property propObj = rev.getPropertyObject(attr_name);

					String[] values = T4PUtils.getPropertyValues(session, propObj);

					if (attr_name.equals("object_type")) {
						String temp = values[0];
						if (temp.endsWith("Revision")) {
							temp = temp.substring(0, temp.lastIndexOf("Revision"));
							values = new String[] { temp };
						}
					}
					if (attr_name.equals("item_id")) {
						String temp = values[0];
						if (temp.endsWith("_ENG")) {
							temp = temp.substring(0, temp.lastIndexOf("_ENG"));
							values = new String[] { temp };
						}
					}

					list.add(new T4PAttrProperty(T4PModelDefinitions.getPortalAttrNameByE(attr_name), propObj
							.getPropertyDescription().getUiName(), values));
				}

			} catch (NotLoadedException e) {
				e.printStackTrace();
			}

			return new T4PPartAttrsOutput(item_id, item_revision_id, list.toArray(new T4PAttrProperty[list.size()]));
		}

	}

	public static T4PPartAttrsOutput getPartAttrsAll(String item_id, String item_revision_id) throws ServiceException,
			ServerException, ConnectException {

		String[] allPropNames = T4PModelDefinitions.partAttributes_Portal
				.toArray(new String[T4PModelDefinitions.partAttributes_Portal.size()]);
		return getPartAttrs(item_id, item_revision_id, allPropNames);
	}

	public static T4PSetPartAttrsResp setPartAttrs(String item_id, String item_revision_id, T4PAttrProperty[] attrs)
			throws ServiceException, ConnectException, ServerException {

		if (item_id == null || item_id.isEmpty()) {
			throw new ServiceException("item_id is empty!!");
		}

		if (item_revision_id == null || item_revision_id.isEmpty()) {
			throw new ServiceException("item_revision_id is empty!!");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("");

		AppXSession session = SessionPoolManager.getUserSession();

		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, item_id, item_revision_id);

		/*if (itemRevResp.output.length == 0
				|| !itemRevResp.output[0].item.getTypeObject().isInstanceOf("S8_XPT_ENGPart")) {
			// TODO need confirm 历史数据特殊处理
			GetItemFromAttributeResponse itemRevRespTmp = T4PUtils.getItemRevision(dmService, item_id + "_ENG",
					item_revision_id);
			// ENG版本存在，直接用
			if (itemRevRespTmp.output.length > 0 && itemRevRespTmp.output[0].itemRevOutput.length > 0) {
				itemRevResp = itemRevRespTmp;

				// ENG版本不存在，且相应cad版本也不存在，视为ENG被修订，生成ENG
			} else if (itemRevResp.output.length > 0 && itemRevResp.output[0].itemRevOutput.length == 0) {

				if (itemRevRespTmp.output.length == 0) {
					// 取cad最新版，生成ENG
					GetItemFromAttributeResponse itemRevRespTmp2 = T4PUtils.getItemRevision(dmService, item_id, "");
					ItemRevision tempRev = itemRevRespTmp2.output[0].itemRevOutput[0].itemRevision;
					dmService.getProperties(new ModelObject[] { tempRev }, new String[] { "item_revision_id" });

					try {
						T4PUtils._getEngPartRevision_old(session, item_id, tempRev.get_item_revision_id());

					} catch (NotLoadedException e) {
						e.printStackTrace();
					}
				}

				// 此时逻辑返回ENG item但无版本，适用后续修订规则
				itemRevResp = T4PUtils.getItemRevision(dmService, item_id + "_ENG", item_revision_id);
			}
		}*/

		// 去掉ID添加ENG后缀、去掉修订代码逻辑，modifid by zhoutong, 2018-12-20
		
		if (itemRevResp.output.length == 0) {
			throw new ServiceException("item not found!!");
		} else {

			boolean flag = true;
			ItemRevision rev = null;

			if (itemRevResp.output[0].itemRevOutput.length == 0) {

				 throw new ServiceException("revision not found!!");

				// 逻辑上此时应为ENGPart，若不是则需报错
				/*if (!itemRevResp.output[0].item.getTypeObject().isInstanceOf("S8_XPT_ENGPart")) {
					throw new ServiceException("revision not found!!");
				}

				// 取任意版本修订
				try {
					Item item = itemRevResp.output[0].item;
					dmService.getProperties(new ModelObject[] { item }, new String[] { "revision_list" });
					ModelObject[] revs = item.get_revision_list();

					ReviseInfo[] reviseInfos = new ReviseInfo[] { new ReviseInfo() };
					reviseInfos[0].baseItemRevision = (ItemRevision) revs[0];
					reviseInfos[0].clientId = "Portal";
					if (item_revision_id.length() == 2) {
						reviseInfos[0].newRevId = "TMP" + item_revision_id;
					}
					ReviseResponse2 reviseResp = dmService.revise2(reviseInfos);
					// T4PUtils.serviceDataErrorCheck(reviseResp.serviceData);

					ReviseOutput output = (ReviseOutput) reviseResp.reviseOutputMap.get("Portal");
					rev = output.newItemRev;

					dmService.getProperties(new ModelObject[] { rev }, new String[] { "item_revision_id" });
					String newRevId = rev.get_item_revision_id();

					HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();
					VecStruct struct = new VecStruct();
					struct.stringVec = new String[] { "" };
					valueMap.put("s8_ZT_linkedCADPartNo", struct); // 清空
					valueMap.put("s8_ZT_linkedCADRevID", struct);
					if (newRevId.length() == 2) {// 刷为临时版本号
						VecStruct struct1 = new VecStruct();
						struct1.stringVec = new String[] { "TMP" + newRevId };
						valueMap.put("item_revision_id", struct1);
					}
					if (item_revision_id.matches(".*\\d{3}.*")) { // 视为CR号
						VecStruct struct2 = new VecStruct();
						struct2.stringVec = new String[] { item_revision_id };
						valueMap.put("s8_ZT_CR_Num", struct2);
					}
					dmService.setProperties(new ModelObject[] { rev }, valueMap);

				} catch (NotLoadedException e) {
					e.printStackTrace();
				}*/

			} else {

				rev = itemRevResp.output[0].itemRevOutput[0].itemRevision;

			}

			// TODO need confirm 历史数据特殊处理
			if (!rev.getTypeObject().isInstanceOf("S8_XPT_ENGPartRevision")) {
				// 视为cadPart,转为engPart
				rev = T4PUtils._getEngPartRevision_old(session, item_id, item_revision_id);
			}

			HashMap<String, VecStruct> valueMap = new HashMap<String, VecStruct>();

			// String owning_user = null;
			// String owning_group = null;
			// String material_name = null;
			// String material_spec = null;

			String crId = "";
			String user_id = "";
			String group_name = "";
			String status = "";
			
			for (T4PAttrProperty attr : attrs) {

				T4PUtils.printT4PAttrProperty(attr);

				String attr_name = T4PModelDefinitions.getTCAttrName_E(attr.getAttr_name());

				// System.out.println("=== this attr_name : " + attr_name);

				if (attr_name != null && attr_name.length() > 0
						&& T4PModelDefinitions.partAttributes_E.contains(attr_name)) {

					// if (!T4PModelDefinitions.attrCanNotChange.contains(attr_name)) {

					if (attr_name.equals("S8_XPT_Buyer_Relation")) {

						try {
							T4PUtils.updateBuyers(session, rev, attr.getAttr_values());
						} catch (Exception e) {
							sb.append(e.getMessage() + "\n");
							LoggerDefault.logError(e.getMessage());
							flag = false;
						}

					} else if (attr_name.equals("S8_XPT_Supplier_Relation")) {

						try {
							T4PUtils.updateSuppliers(session, rev, attr.getAttr_values());
						} catch (Exception e) {
							sb.append(e.getMessage() + "\n");
							LoggerDefault.logError(e.getMessage());
							flag = false;
						}

					} 
					// added by zhoutong, 2018-12-17
					else if (attr.getAttr_name().equals("crNum")) {
						crId = attr.getAttr_values(0);
					} else if (attr.getAttr_name().equals("owner")) {
						user_id = attr.getAttr_values(0);
					} else if (attr.getAttr_name().equals("ownerGroup")) {
						group_name = attr.getAttr_values(0);
					} else if (attr.getAttr_name().equals("status")) {
						status = attr.getAttr_values(0);
					}
					// end add
					else {

						VecStruct struct = new VecStruct();
						struct.stringVec = attr.getAttr_values();
						if (struct.stringVec == null || struct.stringVec[0] == null) {
							struct.stringVec = new String[] { "" }; // 确保可以清空
						}
						valueMap.put(attr_name, struct);

						// // material关系处理
						// if (attr_name.equals("s8_XPT_Material")) {
						// material_name = attr.getAttr_values()[0];
						// } else if (attr_name.equals("s8_XPT_Material_Spec")) {
						// material_spec = attr.getAttr_values()[0];
						// }
					}

					// }

					// else if (attr_name.equals("owning_user")) {
					//
					// owning_user = attr.getAttr_values()[0];
					//
					// } else if (attr_name.equals("owning_group")) {
					//
					// owning_group = attr.getAttr_values()[0];
					//
					// } else {
					//
					// sb.append("Ignore attr not changeable on Part : " + attr_name + "\n");
					// System.out.println("Ignore attr not changeable on Part : " + attr_name);
					//
					// }

				} else {
					sb.append("Ignore attr not on Part : " + attr.getAttr_name() + "\n");
					LoggerDefault.logInfo("Ignore attr not on Part : " + attr.getAttr_name());
				}
			}
			
			// 关联Cr对象，修改所有权，added by zhoutong, 2018-12-17
			if (crId == null || crId.isEmpty()) {
				throw new ServerException("Property crNum is null!!");
			}
			Item crItem = T4PUtils.findItem(session, crId);
			if (crItem == null) {
				throw new ServerException("Cannot find cr with crNum:" + crId);
			}
			
			try {
				ItemRevision crAARev = T4PUtils.getCRAARev(session, crItem);
				if (crAARev == null) 
					throw new ServerException("Cannot find cr AA rev with crNum:" + crId);
				
				// 先移除原有对象，added by zhoutong, 2018-12-20
				T4PUtils.removeObjectByRelation(session, rev, "S8_XPT_Related_CR");
				T4PUtils.createRelation(session, rev, crAARev, "S8_XPT_Related_CR");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			if (user_id != null && !user_id.equals("")) {
				User user = T4PUtils.getUserById(session, user_id);
				if (user == null) {
					throw new ServiceException("Can not find user by user_id: " + user_id + " !!");
				}
				Group group = T4PUtils.getGrpById(session, group_name);
				if (group == null) {
					throw new ServiceException("Can not find group by group_name: " + group_name + " !!");
				}
				T4PUtils.changeOwnership(session, user, group, new ModelObject[] { itemRevResp.output[0].item, rev });
			} else {
				throw new ServerException("Property owner is null!!");
			}

			if (valueMap.size() > 0)
				T4PUtils.serviceDataErrorCheck((dmService.setProperties(new ModelObject[] { rev }, valueMap)));
			
			//添加状态, added by zhoutong, 2018-12-13 
			if (status != null && !status.equals("")) {
				WorkflowService wfSrv = WorkflowService.getService(session.getConnection());
				ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
				rsInput[0].objects = new WorkspaceObject[] { rev };
				rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
				rsInput[0].operations[0].operation = "Append";
				rsInput[0].operations[0].newReleaseStatusTypeName = status;
				wfSrv.setReleaseStatus(rsInput);
			}

			// // change owner
			// if (owning_user != null) {
			//
			// User user = null;
			// try {
			// user = T4PUtils.getUserById(session, owning_user);
			// } catch (Exception e1) {
			// // e1.printStackTrace();
			// System.out.println(e1.getMessage());
			// }
			// if (user == null) {
			// sb.append("User not found : " + owning_user + "\n");
			// System.out.println("User not found : " + owning_user);
			// }
			//
			// Group grp = null;
			// try {
			// grp = T4PUtils.getGrpById(session, owning_group);
			// } catch (Exception e1) {
			// // e1.printStackTrace();
			// System.out.println(e1.getMessage());
			// }
			// if (grp == null) {
			// sb.append("Group not found : " + owning_group + "\n");
			// System.out.println("Group not found : " + owning_group);
			// }
			//
			// // maybe need default group
			// if (grp == null && user != null) {
			// sb.append("try default group...s");
			// System.out.println("try default group...");
			// try {
			// dmService.getProperties(new ModelObject[] { user }, new String[] { "default_group" });
			// grp = (Group) user.get_default_group();
			// } catch (Exception e1) {
			// // e1.printStackTrace();
			// sb.append(e1.getMessage() + "\n");
			// System.out.println(e1.getMessage());
			// }
			// }
			//
			// // 找不到时保留接口帐号作为owner
			// if (user != null && grp != null) {
			// ObjectOwner[] objOwners = new ObjectOwner[] { new ObjectOwner() };
			// objOwners[0].object = rev;
			// objOwners[0].owner = user;
			// objOwners[0].group = grp;
			// try {
			// T4PUtils.serviceDataErrorCheck(dmService.changeOwnership(objOwners));
			// } catch (Exception e) {
			// // e.printStackTrace();
			// sb.append(e.getMessage() + "\n");
			// System.out.println(e.getMessage());
			// }
			// }
			// }
			//
			// if (material_name != null && material_name.length() > 0 && material_spec != null
			// && material_spec.length() > 0) {
			// try {
			// T4PUtils.updateMaterials(session, rev, material_name, material_spec);
			// } catch (Exception e) {
			// sb.append(e.getMessage() + "\n");
			// System.out.println(e.getMessage());
			// flag = false;
			// }
			// }

			String returnMsg = sb.toString();

			// if (returnMsg.length() > 0)
			// System.out.println(returnMsg);

			return new T4PSetPartAttrsResp(flag, returnMsg);

		}
	}

	public static T4PSetPartObsoleteResp setPartObsolete(String item_id, String item_revision_id)
			throws ConnectException, ServerException, ServiceException {

		LoggerDefault.logInfo("setPartObsolete : " + item_id + item_revision_id);

		if (item_id == null || item_id.isEmpty()) {
			throw new ServiceException("item_id is empty!!");
		}

		// boolean alltag = false;

		if (item_revision_id == null || item_revision_id.isEmpty()) {
			throw new ServiceException("item_revision_id is empty!!");
			// item_revision_id = "All";
			// alltag = true;
		}

		AppXSession session = SessionPoolManager.getUserSession();
		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		GetItemFromAttributeResponse itemRevResp;
		// if (alltag)
		// itemRevResp = T4PUtils.getItemAllRevs(dmService, item_id);
		// else
		itemRevResp = T4PUtils.getItemRevision(dmService, item_id, item_revision_id);

		if (itemRevResp.output.length == 0) {

			throw new ServiceException("item not found!!");

		} else if (itemRevResp.output[0].itemRevOutput.length == 0) {

			throw new ServiceException("revision not found!!");

		} else {

			// ModelObject[] objs = new ModelObject[itemRevResp.output[0].itemRevOutput.length];
			// for (int i = 0; i < objs.length; i++)
			// objs[i] = itemRevResp.output[0].itemRevOutput[i].itemRevision;
			// T4PUtils.createWorkflow2(session, "99-Portal-Obsolete", "Portal-Obsolete-" + item_id + "/"
			// + item_revision_id, objs);

			// T4PUtils.createWorkflow(session, "99-Portal-Obsolete", "Portal-Obsolete-" + item_id + "/"
			// + item_revision_id, itemRevResp.output[0].itemRevOutput[0].itemRevision);

			try {
				ItemRevision rev = itemRevResp.output[0].itemRevOutput[0].itemRevision;
				dmService.getProperties(new ModelObject[] { rev }, new String[] { "last_release_status" });
				ModelObject oldStatObj = rev.getPropertyObject("last_release_status").getModelObjectValue();

				WorkflowService wfSrv = WorkflowService.getService(session.getConnection());

				if (oldStatObj != null) {
					dmService.getProperties(new ModelObject[] { oldStatObj }, new String[] { "name" });
					String oldStatus = oldStatObj.getPropertyObject("name").getStringValue();
					// 去除原状态 ==
					ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
					rsInput[0].objects = new WorkspaceObject[] { rev };
					rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
					rsInput[0].operations[0].operation = "Delete";
					rsInput[0].operations[0].existingreleaseStatusTypeName = oldStatus;
					wfSrv.setReleaseStatus(rsInput);
				}

				ReleaseStatusInput[] rsInput = new ReleaseStatusInput[] { new ReleaseStatusInput() };
				rsInput[0].objects = new WorkspaceObject[] { rev };
				rsInput[0].operations = new ReleaseStatusOption[] { new ReleaseStatusOption() };
				rsInput[0].operations[0].operation = "Append";
				rsInput[0].operations[0].newReleaseStatusTypeName = "S8_Obsolete";
				wfSrv.setReleaseStatus(rsInput);

			} catch (NotLoadedException e) {
				e.printStackTrace();
			}

		}

		LoggerDefault.logInfo("Obsolete workflow created!");

		return new T4PSetPartObsoleteResp(true, "Obsolete workflow created!");
	}

	public static T4PRevisePartResp revisePart(String item_id, String old_rev_id, String new_rev_id)
			throws ServiceException, ConnectException, ServerException {

		if (item_id == null || item_id.isEmpty()) {
			throw new ServiceException("item_id is empty!!");
		}

		if (old_rev_id == null || old_rev_id.isEmpty()) {
			throw new ServiceException("old_rev_id is empty!!");
		}

		if (new_rev_id == null || new_rev_id.isEmpty()) {
			throw new ServiceException("new_rev_id is empty!!");
		}

		AppXSession session = SessionPoolManager.getUserSession();

		DataManagementService dmService = DataManagementService.getService(session.getConnection());

		// 检查新版是否已存在
		GetItemFromAttributeResponse itemRevResp = T4PUtils.getItemRevision(dmService, item_id, new_rev_id);

		if (itemRevResp.output.length == 0) {
			throw new ServiceException("item not found!!");
		}

		if (itemRevResp.output[0].itemRevOutput.length != 0) {

			// 检查状态，若有状态则报错
			ItemRevision rev = itemRevResp.output[0].itemRevOutput[0].itemRevision;
			dmService.getProperties(new ModelObject[] { rev }, new String[] { "last_release_status" });
			dmService.refreshObjects(new ModelObject[] { rev });

			try {
				String status = rev.getPropertyDisplayableValue("last_release_status");

				if (status == null || status.isEmpty()) {

					return new T4PRevisePartResp(true, item_id + "/" + new_rev_id
							+ " exists and its cad status is empty");

				} else {

					throw new ServiceException(item_id + "/" + new_rev_id + " exists and its cad status is " + status);

				}

			} catch (NotLoadedException e) {
				e.printStackTrace();

				throw new ServiceException(e.getMessage());
			}

		} else {

			GetItemFromAttributeResponse itemRevResp2 = T4PUtils.getItemRevision(dmService, item_id, old_rev_id);

			if (itemRevResp2.output[0].itemRevOutput.length != 0) {
				ItemRevision rev = itemRevResp2.output[0].itemRevOutput[0].itemRevision;
				ReviseInfo[] reviseInfos = new ReviseInfo[] { new ReviseInfo() };
				reviseInfos[0].baseItemRevision = rev;
				reviseInfos[0].clientId = "Portal";
				reviseInfos[0].newRevId = new_rev_id;

				ReviseResponse2 reviseResp = dmService.revise2(reviseInfos);

				T4PUtils.serviceDataErrorCheck(reviseResp.serviceData);

				return new T4PRevisePartResp(true, "success");

			} else {

				throw new ServiceException("item revision not found!!");

			}
		}

	}

}
