package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * ��ǯ�嵥 ���ݶ���
 * 
 * @author zhoutong
 *
 */
public class ElectrodeHolderList implements Cloneable
{
	public String lcationName = "";				// ��λ����
	public String lcationAddress = "";			// ��λ��ַ��= ��λID + ��λ����
	
	public String electrodeHolderNumber = "";	// ��ǯ���
	public String electrodeHolderModels = "";	// ��ǯ�ͺ�
	public double quantity;						// ����
	public String remark = "";					// ��ע
	
	public ElectrodeHolderList(TCComponentBOPLine pinchWeldBopLine, int languageSelection, TCComponentBOPLine stationBopLine, String lcationAddress)
	{
		this.lcationAddress = lcationAddress;
		
		try {
			if (stationBopLine != null)
			{
				String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName" };
				String[] propValues = stationBopLine.getProperties(propNames);
				if (propValues != null && propValues.length == 2) {
					this.lcationName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				}
			}
			
			String[] propNames = { "s4_BAT_ToolNumber", "s4_BAT_SpecificationModel", "S4_NT_Remarks" };
			String[] propValues = pinchWeldBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 3) 
			{
				this.electrodeHolderNumber = propValues[0];
				this.electrodeHolderModels = propValues[1];
				this.remark = propValues[2];
			}
			this.quantity = TcUtil.getUsageQuantity(pinchWeldBopLine);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object clone()
	{
		ElectrodeHolderList clonElectrodeHolderList = null;
		try {
			clonElectrodeHolderList = (ElectrodeHolderList) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clonElectrodeHolderList;
	}

}
