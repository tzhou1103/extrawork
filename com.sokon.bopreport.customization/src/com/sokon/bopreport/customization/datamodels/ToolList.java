package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * �����嵥 ���ݶ���
 * 
 * @author zhoutong
 *
 */
public class ToolList 
{
	public String stationAddress = "";	// ��λ��ַ
	public String toolName = "";		// ��������
	public String toolNumber = "";		// ����ͺ�
	public double quantity;				// ����
	public String remark = "";			// ��ע
	
	public ToolList(TCComponentBOPLine toolBopLine, int languageSelection, TCComponentBOPLine opBopLine, String stationAddress) 
	{
		try {
			this.stationAddress = stationAddress;
			
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "s4_BAT_SpecificationModel", "S4_NT_Remarks" };
			String[] propValues = toolBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 4) 
			{
				this.toolName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				
				this.toolNumber = propValues[2];
				this.remark = propValues[3];
			}
			
			this.quantity = TcUtil.getUsageQuantity(toolBopLine);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
}
