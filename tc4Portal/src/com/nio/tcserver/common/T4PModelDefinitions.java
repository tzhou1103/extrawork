package com.nio.tcserver.common;

import java.util.ArrayList;

public class T4PModelDefinitions {

	public static ArrayList<String> partTypes_E = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("S8_B_Consum_Eng");
			add("S8_B_Lables_Eng");
			add("S8_B_Part_Eng");
			add("S8_B_SW_Eng");
			add("S8_B_CCABase_Eng");
			add("S8_B_Elec_Eng");
			add("S8_B_PWB_Eng");
			add("S8_B_Schem_Eng");

			add("S8_E_Consum_Eng");
			add("S8_E_Lables_Eng");
			add("S8_E_Part_Eng");
			add("S8_E_SW_Eng");
			add("S8_E_CCABase_Eng");
			add("S8_E_Elec_Eng");
			add("S8_E_PWB_Eng");
			add("S8_E_Schem_Eng");

			add("S8_VCU_SW_Eng");
			add("S8_StandPart_Eng");

			add("S8_ComPart_Eng");

		}
	};

	public static ArrayList<String> partTypes = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("S8_B_Consumables");
			add("S8_B_Labels");
			add("S8_B_Part");
			add("S8_B_Software");
			add("S8_B_EDACCABase");
			add("S8_B_Electronics");
			add("S8_B_PWB");
			add("S8_B_EDASchem");

			add("S8_E_Consumables");
			add("S8_E_Labels");
			add("S8_E_Part");
			add("S8_E_Software");
			add("S8_E_EDACCABase");
			add("S8_E_Electronics");
			add("S8_E_PWB");
			add("S8_E_EDASchem");

			add("S8_VCU_Software");
			add("S8_XPT_Standard");

			add("EDAComPart"); // 外部生成编码，中台不创建
		}
	};

	// 返回tc中的cad类，同时也是portal的类名
	public static String getCADTypeByEBOMType(String ebomType) {
		int i = partTypes_E.indexOf(ebomType);
		if (i >= 0)
			return partTypes.get(i);
		else
			return null;
	}

	// 返回tc中的engpart类
	public static String getEBOMTypeByCADType(String cadType) {
		int i = partTypes.indexOf(cadType);
		if (i >= 0)
			return partTypes_E.get(i);
		else
			return null;
	}

	public static ArrayList<String> partAttributes = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("creation_date"); // 1
			add("date_released");
			add("item_id");
			add("item_revision_id");
			add("last_mod_date"); // 5
			add("last_mod_user");
			add("object_desc");
			add("object_name");
			add("object_type");
			add("owning_group"); // 10
			add("owning_user"); //
			add("last_release_status"); // 12 release_status_list
			add("s8_XPT_ChinesePartName");
			add("s8_XPT_CriticalPart");
			add("s8_XPT_DesignResponsibility"); // 15
			add("s8_XPT_PartionCode");
			add("s8_XPT_ServicePart");
			add("s8_XPT_SparePart");
			add("s8_XPT_UOM");
			add("s8_XPT_WeightFrom"); // 20
			add("s8_XPT_Weights");
			add("s8_XPT_First_Project");
			add("s8_XPT_Material");
			add("s8_XPT_Material_Spec");
			add("S8_XPT_Buyer_Relation"); // 25
			add("S8_XPT_Supplier_Relation"); // 26
			add("");
			add("");
			add("");
			add(""); // 30
			add("");
			add("");
			add("");
			add("");
			add(""); // 35
			add("");
			add("");
			add("");
			add("");
			add(""); // 40
			add("");
			add("");
			add("");
		}
	};

	public static ArrayList<String> partAttributes_Portal = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("createTime"); // 1
			add("dateReleased");
			add("partNo");
			add("revId");
			add("updateTime");// 5
			add("updateUserId");
			add("partDescription");
			add("partName");
			add("partType");
			add("ownerGroup");// 10
			add("owner");
			add("cadStatus");
			add("partNameCn");
			add("criticalPart");
			add("xptDesignResponsibility"); // 15
			add("xptPartionCode");
			add("servicePart");
			add("sparePart");
			add("uom");
			add("weightFrom");// 20
			add("weight");
			add("initialProjectCode");
			add("materialName");
			add("materialSpecification");
			add("buyer"); // 25
			add("supplier");// 26
			add("ebomOnly");
			add("activeEcr");
			add("previousEcr");
			add("activeEcrDate");// 30
			add("constitutionPart");
			add("createdUserId");
			add("dynamicTorque");
			add("torqueAngle");
			add("torqueRemark");// 35
			add("qcos");
			add("status");
			add("editVersion");
			add("topLevelPart");
			add("epBomType");// 40
			add("currentStatus");
			add("previousEcrDate");
			add("crNum");
		}
	};

	public static ArrayList<String> partAttributes_E = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("s8_ZT_createTime"); // 1
			add("s8_ZT_datereleased");
			add("s8_ZT_linkedCADPartNo");
			add("s8_ZT_linkedCADRevID");
			add("s8_ZT_updateTime"); // 5
			add("s8_ZT_updateUserID");
			add("object_desc");
			add("object_name");
			add("s8_ZT_parttype");
			add("s8_ZT_ownergroup"); // 10
			add("s8_ZT_owner"); //
			add("s8_ZT_CADStatus"); // 12 release_status_list
			add("s8_XPT_ChinesePartName");
			add("s8_XPT_CriticalPart");
			add("s8_XPT_DesignResponsibility"); // 15
			add("s8_XPT_PartionCode");
			add("s8_XPT_ServicePart");
			add("s8_XPT_SparePart");
			add("s8_XPT_UOM");
			add("s8_XPT_WeightFrom"); // 20
			add("s8_XPT_Weights");
			add("s8_XPT_First_Project");
			add("s8_XPT_Material");
			add("s8_XPT_Material_Spec");
			add("S8_XPT_Buyer_Relation"); // 25
			add("S8_XPT_Supplier_Relation"); // 26
			add("s8_ZT_EBOMOnly");
			add("s8_ZT_activeECR");
			add("s8_ZT_previousECR");
			add("s8_ZT_activeECRDate"); // 30
			add("s8_ZT_constitutionpart");
			add("s8_ZT_createdUserID");
			add("s8_ZT_dynamicTorque");
			add("s8_ZT_torqueAngle");
			add("s8_ZT_torqueRemark");// 35
			add("s8_ZT_qcos");
			add("s8_ZT_status");
			add("s8_ZT_editversion");
			add("s8_ZT_topLevelPart");
			add("s8_ZT_EPBOMType");// 40
			add("s8_ZT_current_status");
			add("s8_ZT_previousECRDate");
			add("s8_ZT_CR_Num");
		}
	};

	public static String getPortalAttrNameByE(String attr) {
		int i = partAttributes_E.indexOf(attr);
		if (i >= 0)
			return partAttributes_Portal.get(i);
		else
			return attr;
	}

	public static String getTCAttrName(String attr) {
		int i = partAttributes_Portal.indexOf(attr);
		if (i >= 0)
			return partAttributes.get(i);
		else
			return attr;
	}

	public static String getTCAttrName_E(String attr) {
		int i = partAttributes_Portal.indexOf(attr);
		if (i >= 0)
			return partAttributes_E.get(i);
		else
			return attr;
	}

	// public static ArrayList<String> attrCanNotChange = new ArrayList<String>() {
	// private static final long serialVersionUID = 1L;
	// {
	// add("creation_date");
	// add("date_released");
	// add("item_id");
	// add("item_revision_id");
	// add("last_mod_date");
	// add("last_mod_user");
	// add("object_type");
	// add("owning_group");
	// add("owning_user");
	// add("release_status_list");
	// add("last_release_status");
	// }
	// };

	public static ArrayList<String> attrSent2Portal = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			// add("partNo");
			 add("revId"); 
			 add("partDescription");
			// add("partName");
			 add("cadStatus");
			// add("partNameCn");
			// add("criticalPart");
			// add("xptDesignResponsibility");
			// add("xptPartionCode");
			// add("servicePart");
			// add("sparePart");
			// add("uom");
			add("weightFrom");
			add("weight");
		}
	};

	public static ArrayList<String> bomAttributes = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("bl_item_item_id"); // id
			add("bl_rev_item_revision_id"); // rev
			add("S8_XPT_Custquantity"); // qty
			add("bl_sequence_no");
			add("S8_XPT_CarryoverNew");
			add("S8_XPT_MakeorBuyNote");
			add("S8_XPT_ConsignDirect");
			add("S8_XPT_BuyLevel");
			// add("s8_XPT_Gauge");
			add("s8_XPT_ConsumptionMax"); // --Compound
			add("s8_XPT_ConsumptionNormal"); // --Compound
			add("S8_XPT_Nom_Torque");
			add("S8_XPT_MaxTorque");
			add("S8_XPT_MinTorque");
			add("S8_XPT_Nom_Angle");
			add("S8_XPT_Max_Angle");
			add("S8_XPT_Min_Angle");
			add("S8_XPT_LinkedECN");
			add("S8_XPT_EffectiveDate");
			// add("previous_date");
			// add("latest_dr_no");
			// add("latest_dr_release_date");
			// add("latest_cn_no");
			// add("latest_cn_release_date");
		}
	};

	public static ArrayList<String> bomAttributes_Portal = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("partNo");
			add("parentRevId");
			add("quantity");
			add("findNo");
			add("carryoverOrNew");
			add("makeOrBuy");
			add("consignmentDirectBuy");
			add("parentBuyLevel");
			// add("xptGauge");
			add("maxPowerConsumption");
			add("normalPowerConsumption");
			add("xptNominalTorque");
			add("maxTorque");
			add("minTorque");
			add("nominalTorqueAngle");
			add("maxTorqueAngle");
			add("minTorqueAngle");
			add("xptLinkedEcn");
			add("effectiveDate");
			// add("previousDate");
			// add("latestDrNo");
			// add("latestDrReleaseDate");
			// add("latestCnNo");
			// add("latestCnReleaseDate");
		}
	};

	public static String getPortalBOMAttrName(String attr) {
		int i = bomAttributes.indexOf(attr);
		if (i >= 0)
			return bomAttributes_Portal.get(i);
		else
			return attr;
	}

	public static String getTCBOMAttrName(String attr) {
		int i = bomAttributes_Portal.indexOf(attr);
		if (i >= 0)
			return bomAttributes.get(i);
		else
			return attr;
	}

	public static ArrayList<String> bomModifyAttrs = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("S8_XPT_Custquantity"); // qty
			add("bl_sequence_no");
			add("S8_XPT_CarryoverNew");
			add("S8_XPT_MakeorBuyNote");
			add("S8_XPT_ConsignDirect");
			add("S8_XPT_BuyLevel");
			// add("s8_XPT_ConsumptionMax"); // --Compound
			// add("s8_XPT_ConsumptionNormal"); // --Compound
			add("S8_XPT_Nom_Torque");
			add("S8_XPT_MaxTorque");
			add("S8_XPT_MinTorque");
			add("S8_XPT_Nom_Angle");
			add("S8_XPT_Max_Angle");
			add("S8_XPT_Min_Angle");
			add("S8_XPT_LinkedECN");
			add("S8_XPT_EffectiveDate");
		}
	};
}
