package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * �о��嵥 ���ݶ���
 * 
 * @author zhoutong
 *
 */
public class FixtureList
{
	public String assembly = "";		// �ܳ�
	public String fixtureName = "";		// �о�����
	public String fixtureNumber = "";	// �о߱��
	public double quantity;				// ����
	public String manufacturer = "";	// ������
	public String remark = "";			// ��ע
	
	public String material = "";		// ����
	
	public FixtureList(TCComponentBOPLine fixtureBopLine, int languageSelection)
	{
		try {
			String[] propNames = { "s4_BAT_FixAssEnName", "s4_BAT_FixAssChName",
					"bl_rev_object_name", "bl_rev_s4_CAT_ChineseName",
					"s4_BAT_ToolNumber", "s4_BAT_Manufacturer", "S4_NT_Remarks" };
			String[] propValues = fixtureBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 7) {
				this.assembly = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				this.fixtureName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[2], propValues[3]);
				this.fixtureNumber = propValues[4];
				this.manufacturer = propValues[5];
				this.remark = propValues[6];
			}
			this.quantity = TcUtil.getUsageQuantity(fixtureBopLine);
			
			this.material = TcUtil.getClassificationAttributeValue(fixtureBopLine.getItemRevision(), "material");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

}
