package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * ����嵥 ���ݶ���
 * 
 * @author zhoutong
 *
 */
public class CheckingFixtureList 
{
	public String checkingFixtureName = "";		// �������
	public String checkingFixtureNumber = "";	// ��߱��
	public String productSize = "";				// ��߳ߴ�
	public double quantity;						// ����
	public String remark = "";					// ��ע
	
	public CheckingFixtureList(TCComponentBOPLine checkingFixtureBopLine, int languageSelection)
	{
		try {
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "s4_BAT_ToolNumber", "s4_BAT_Size", "S4_NT_Remarks" };
			String[] propValues = checkingFixtureBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 5) 
			{
				this.checkingFixtureName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				this.checkingFixtureNumber = propValues[2];
				this.productSize = propValues[3];
				this.remark = propValues[4];
			}
			this.quantity = TcUtil.getUsageQuantity(checkingFixtureBopLine);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
}
