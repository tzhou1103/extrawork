package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * 检具清单 数据对象
 * 
 * @author zhoutong
 *
 */
public class CheckingFixtureList 
{
	public String checkingFixtureName = "";		// 检具名称
	public String checkingFixtureNumber = "";	// 检具编号
	public String productSize = "";				// 检具尺寸
	public double quantity;						// 数量
	public String remark = "";					// 备注
	
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
