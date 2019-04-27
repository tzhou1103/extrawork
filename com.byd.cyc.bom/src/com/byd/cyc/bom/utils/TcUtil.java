package com.byd.cyc.bom.utils;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;

/**
 * @author zhoutong
 *
 */
public class TcUtil 
{	
	public static final TCSession getTcSession()
	{
		return (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	public static final String getSitePreferenceValue(String preferenceName)
	{
		String preferenceValue = "";
		TCPreferenceService tcPreferenceService = getTcSession().getPreferenceService();
		if (tcPreferenceService.isDefinitionExistForPreference(preferenceName)) {
			preferenceValue = tcPreferenceService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
		}
		return preferenceValue;
	}
	
	public static final TCComponentItemRevision findItemRevision(String itemID, String itemRevisionID) throws TCException
	{
		TCComponentItemRevision itemRevision = null;
		TCComponentItemRevisionType itemRevisionType = (TCComponentItemRevisionType) getTcSession().getTypeComponent("ItemRevision");
		TCComponentItemRevision[] itemRevisions = itemRevisionType.findRevisions(itemID, itemRevisionID);
		if (itemRevisions != null && itemRevisions.length > 0) {
			itemRevision = itemRevisions[0];
		}
		return itemRevision;
	}
	
	
	public static final String getObjectStatus(TCComponent component) throws TCException
	{
		String status = "";
		
		TCComponent[] release_status_list = component.getReferenceListProperty("release_status_list");
		if (release_status_list != null && release_status_list.length > 0) {
			status = release_status_list[0].getProperty("object_name");
		}
		
		return status;
	}
	
	public static final TCComponentDataset createDataset(String name, String description, String type) throws TCException
	{
		TCComponentDataset dataset = null;
		TCComponentDatasetType datasetType = (TCComponentDatasetType) getTcSession().getTypeComponent(type);
		if (datasetType == null)
			throw new TCException("无法获取名为 " + type + " 的数据集类型！");
		dataset = datasetType.create(name, description, type);
		return dataset;
	}
	
}
