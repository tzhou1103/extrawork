package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * �豸�嵥 ���ݶ���
 * 
 * @author zhoutong
 *
 */
public class DeviceList implements Cloneable
{
	public String lcationName = "";			// ��λ����
	public String lcationAddress = "";		// ��λ��ַ��= ��λID + ��λ���򣩣�2018-11-08���޸�ȡֵΪ��λID + �豸�ϵĹ�λ����
	
	public String equipmentName = "";		// �豸����
	public String equipmentModels = "";		// �豸�ͺ�
	public double quantity;					// ����
	public String remark = "";				// ��ע
	
	public DeviceList(TCComponentBOPLine equipmentBopLine, int languageSelection, TCComponentBOPLine stationBopLine, String lcationAddress)
	{
		this.lcationAddress = lcationAddress;
		
		try {
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName" };
			String[] propValues = stationBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 2) {
				this.lcationName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
			}
			
			propNames = new String[] { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "s4_BAT_SpecificationModel", "S4_NT_Remarks" };
			propValues = equipmentBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 4) 
			{
				this.equipmentName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				this.equipmentModels = propValues[2];
				this.remark = propValues[3];
			}
			this.quantity = TcUtil.getUsageQuantity(equipmentBopLine);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object clone()
	{
		DeviceList cloneDeviceList = null;
		try {
			cloneDeviceList = (DeviceList) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return cloneDeviceList;
	}

}
