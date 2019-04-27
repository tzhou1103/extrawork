package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * 设备清单 数据对象
 * 
 * @author zhoutong
 *
 */
public class DeviceList implements Cloneable
{
	public String lcationName = "";			// 工位名称
	public String lcationAddress = "";		// 工位地址（= 工位ID + 工位区域）；2018-11-08，修改取值为工位ID + 设备上的工位区域
	
	public String equipmentName = "";		// 设备名称
	public String equipmentModels = "";		// 设备型号
	public double quantity;					// 数量
	public String remark = "";				// 备注
	
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
