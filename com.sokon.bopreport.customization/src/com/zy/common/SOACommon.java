package com.zy.common;

import java.util.HashMap;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;

public class SOACommon {

	static public boolean IsReleased(TCComponent Comp) {
		try {
			String last_release_status = Comp.getProperty("last_release_status");
			if (last_release_status.length() > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	static public TCComponentItem createDesign(String itemType, String itemId, String itemName, String itemDesc, String revId, String UOM) {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		DataManagementService datamanagementservice = DataManagementService.getService(session);
		com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse createitemsresponse = null;
		com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties aitemproperties[] = new com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties[1];
		aitemproperties[0] = new com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties();
		aitemproperties[0].clientId = (new Integer(1)).toString();
		aitemproperties[0].description = itemDesc;
		aitemproperties[0].itemId = itemId;
		aitemproperties[0].name = itemName;
		aitemproperties[0].revId = revId;
		aitemproperties[0].type = itemType;

		com.teamcenter.services.rac.core._2006_03.DataManagement.ExtendedAttributes extendedattributes = new com.teamcenter.services.rac.core._2006_03.DataManagement.ExtendedAttributes();
		extendedattributes.objectType = "S9_PartMaster"; // is_designrequired
		HashMap hashmap = new HashMap();
		hashmap.put("is_designrequired", "false");
		extendedattributes.attributes = hashmap;

		aitemproperties[0].extendedAttributes = new com.teamcenter.services.rac.core._2006_03.DataManagement.ExtendedAttributes[] { extendedattributes };
		// aitemproperties[0].uom = UOM;

		TCComponent tccomponent = null;
		try {
			createitemsresponse = datamanagementservice.createItems(aitemproperties, tccomponent, null);
			if (createitemsresponse.serviceData.sizeOfPartialErrors() == 0) {
				TCComponentItem newComp = createitemsresponse.output[0].item;
				return newComp;
			} else if (createitemsresponse.serviceData.sizeOfPartialErrors() > 0) {
				String Err = createitemsresponse.serviceData.getPartialError(0).getErrorValues()[0].getMessage();
				System.out.println(Err);
				MessageBox.post(Err, "ÌבÊ¾", 2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static public TCComponentItem createItem(String itemType, String itemId, String itemName, String itemDesc, String revId) {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		DataManagementService datamanagementservice = DataManagementService.getService(session);
		com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse createitemsresponse = null;
		com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties aitemproperties[] = new com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties[1];
		aitemproperties[0] = new com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties();
		aitemproperties[0].clientId = (new Integer(1)).toString();
		aitemproperties[0].description = itemDesc;
		aitemproperties[0].itemId = itemId;
		aitemproperties[0].name = itemName;
		aitemproperties[0].revId = revId;
		aitemproperties[0].type = itemType;
		// aitemproperties[0].uom = UOM;

		TCComponent tccomponent = null;
		try {
			createitemsresponse = datamanagementservice.createItems(aitemproperties, tccomponent, null);
			if (createitemsresponse.serviceData.sizeOfPartialErrors() == 0) {
				TCComponentItem newComp = createitemsresponse.output[0].item;
				return newComp;
			} else if (createitemsresponse.serviceData.sizeOfPartialErrors() > 0) {
				String Err = createitemsresponse.serviceData.getPartialError(0).getErrorValues()[0].getMessage();
				System.out.println(Err);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
