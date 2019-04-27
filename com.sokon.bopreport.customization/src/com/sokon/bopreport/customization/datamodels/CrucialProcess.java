package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * 关键工序 数据对象
 * 
 * @author zhoutong
 * 
 */
public class CrucialProcess 
{
	public String station = "";					// 工位
	public String processName = "";				// 工序名称
	public String controlEssentials = "";		// 控制方法
	public String CCSC = "";					// 特殊特性符号
	public String standard = "";				// 规范/公差
	public String remark = "";					// 备注
	
	public String processChinessName = "";		// 工序中文名称

	public CrucialProcess(TCComponentBOPLine opBopLine, int languageSelection) 
	{
		try {
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "s4_BAT_ControlEssentials", "s4_BAT_CCSC", "s4_BAT_Standard", "S4_NT_Remarks" };
			String[] propValues = opBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 6) 
			{
				this.processName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				
				this.controlEssentials = propValues[2];
				this.CCSC = propValues[3];
				this.standard = propValues[4];
				this.remark = propValues[5];
			}
			
//			this.station = TcUtil.getWorkAreaId(opBopLine);
			// 2018-11-08, 修改工位取值
			String tempStation = TcUtil.getWorkAreaId(opBopLine) + TcUtil.getProcResArea(opBopLine);
			this.station = TcUtil.getLast7String(tempStation);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
}
