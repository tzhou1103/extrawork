package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * 工艺过程流程图 数据对象
 * 
 * @author zhoutong
 *
 */
public class WorkflowProcessChart 
{
	public String station = "";				// 工位
	public String step = "";				// 操作步骤
	public String operator = "";			// 操作人员
	public int timeManagement;				// 工时（s）
	public String remark = "";				// 备注
	
	public WorkflowProcessChart(TCComponentBOPLine opBopLine, int languageSelection) 
	{		
		try {
			TCComponentBOPLine stationBopLine = (TCComponentBOPLine) opBopLine.parent();
//			this.station = getStation(stationBopLine);
			// 2018-11-08, 修改工位取值
			String tempStation = getStation(stationBopLine) + TcUtil.getProcResArea(opBopLine);
			this.station = TcUtil.getLast7String(tempStation);
			
			TCComponent relatedComponent = opBopLine.getRelatedComponent("Mfg0processResource");
			if (relatedComponent != null && relatedComponent.getStringProperty("bl_item_object_type").equals("S4_IT_Worker"))  {
				this.operator = relatedComponent.getProperty("bl_rev_object_name");
			}
			
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "Mfg0allocated_time", "S4_NT_Remarks" };
			String[] propValues = opBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 4) 
			{
				this.step = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				// 四舍五入，取整，20180927
				this.timeManagement = Math.round(Float.parseFloat(propValues[2]));
				this.remark = propValues[3];
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取工位
	 * 
	 * @param stationBopLine
	 * @return
	 * @throws TCException
	 */
	private String getStation(TCComponentBOPLine stationBopLine) throws TCException
	{
		// 获取工位工艺下事例类型为MEWorkArea的BOM行
		TCComponent[] relatedComponents = stationBopLine.getRelatedComponents("Mfg0assigned_workarea");
		if (relatedComponents != null && relatedComponents.length > 0) {
			return relatedComponents[0].getProperty("bl_child_id");
		}
		
		return "";
	}
	
}
