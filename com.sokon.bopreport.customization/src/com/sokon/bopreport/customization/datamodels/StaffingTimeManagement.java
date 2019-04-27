package com.sokon.bopreport.customization.datamodels;

import java.util.Vector;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;

/**
 * 人员定额&工时表 数据对象
 * 
 * @author zhoutong
 */
public class StaffingTimeManagement 
{
	public String processSection = "";			// 工段
	public String stationAddress = "";			// 工位地址
	
	public String operationalEssentials = "";	// 操作要素
	public int singleShiftStaffing;				// 单班定额
	public int timeManagement;					// 工时（s）
	public String remark = "";					// 备注
	
	public String subStationAddress = "";		// 工位地址前6位
	
	public StaffingTimeManagement(TCComponentBOPLine opBopLine, int languageSelection) 
	{
		try {
			TCComponentBOPLine stationBopLine = (TCComponentBOPLine) opBopLine.parent();
			
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "Mfg0allocated_time", "S4_NT_Remarks" };
			String[] propValues = opBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 4) {
				this.operationalEssentials = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				// 四舍五入，取整，20180926
				this.timeManagement = Math.round(Float.parseFloat(propValues[2]));
				this.remark = propValues[3];
			}
			
//			this.stationAddress = getStationAddress(stationBopLine);
			// 2018-11-08, 修改工位地址取值
			String tempStationAddress = getStationAddress(stationBopLine) + TcUtil.getProcResArea(opBopLine);
			this.stationAddress = TcUtil.getLast7String(tempStationAddress);
			
			// 2019-01-23，截取工位地址前6位
			int srcLength = this.stationAddress.length();
			if (srcLength >= 6) {
				this.subStationAddress = this.stationAddress.substring(0, 6);
			} else {
				this.subStationAddress = this.stationAddress;
			}
			
			// 工段
			/*propNames = new String[] { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName" };
//			propValues = stationBopLine.getProperties(propNames);
			propValues = stationBopLine.parent().getProperties(propNames); // 修改工段取值来源， modified by zhoutong, 2018-10-09
			if (propValues != null && propValues.length == 2) {
				this.processSection = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
			}*/
			this.processSection = getProcessSection(stationBopLine, languageSelection);
			
			this.singleShiftStaffing = getSingleShiftStaffing(stationBopLine);
			// 修改单班定额获取，20180927
//			TCComponent relatedComponent = opBopLine.getRelatedComponent("Mfg0processResource");
//			if (relatedComponent != null && relatedComponent.getStringProperty("bl_item_object_type").equals("S4_IT_Worker"))  {
//				this.singleShiftStaffing = 1;
//			}
			
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取单班定额
	 * 
	 * @param stationBopLine
	 * @return
	 * @throws TCException
	 */
	private int getSingleShiftStaffing(TCComponentBOPLine stationBopLine) throws TCException 
	{
		int singleShiftStaffing = 0;
		AIFComponentContext[] contexts = stationBopLine.getChildren();
		if (contexts != null && contexts.length > 0) 
		{
			for (AIFComponentContext context : contexts) 
			{
				TCComponentBOPLine childBopLine = (TCComponentBOPLine) context.getComponent();
				if (childBopLine.getStringProperty("bl_item_object_type").equals("S4_IT_Worker")) {
					singleShiftStaffing++;
				}
			}
		}
		return singleShiftStaffing;
	}

	/**
	 * 获取工位地址
	 * 
	 * @param stationBopLine
	 * @return
	 * @throws TCException
	 */
	private String getStationAddress(TCComponentBOPLine stationBopLine) throws TCException
	{
		// 获取工位工艺下事例类型为MEWorkArea的BOM行
		TCComponent[] relatedComponents = stationBopLine.getRelatedComponents("Mfg0assigned_workarea");
		if (relatedComponents != null && relatedComponents.length > 0) {
			return relatedComponents[0].getProperty("bl_child_id");
		}
		
		return "";
	}
	
	
	/**
	 * 获取工段
	 * 
	 * @param stationBopLine
	 * @param languageSelection
	 * @return
	 * @throws TCException
	 */
	private String getProcessSection(TCComponentBOPLine stationBopLine, int languageSelection) throws TCException
	{
		String processSection = "";
		
		TCComponentItemRevision bopRevision = stationBopLine.window().getTopBOMLine().getItemRevision();
		TCComponentItemRevision workAreaRevision = (TCComponentItemRevision) bopRevision.getRelatedComponent("IMAN_MEWorkArea");
		if (workAreaRevision != null) 
		{
			TCComponentBOMLine workAreaBOMLine = getWorkAreaBOMLine(workAreaRevision);
			if (workAreaBOMLine == null) {
				workAreaBOMLine = TcUtil.getTopBomLine(workAreaRevision); // 若未打开，则程序构建BOMWindow
			}
			if (workAreaBOMLine != null)
			{
//				String stationId = stationBopLine.getProperty("bl_item_item_id");
				String stationId = getStationAddress(stationBopLine);
				Vector<TCComponentBOMLine> vector = new Vector<TCComponentBOMLine>();
				traverseBOM(workAreaBOMLine, stationId, vector);
				if (vector.size() > 0) {
					String[] propNames = new String[] { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName" };
					String[] propValues = vector.get(0).getProperties(propNames); // 修改工段取值来源， modified by zhoutong, 2018-10-09
					if (propValues != null && propValues.length == 2) {
						processSection = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
					}
				}
			}
		}
		return processSection;
	}
	
	/**
	 * 从当前打开的视图中获取车间工厂顶层行
	 * 
	 * @param workAreaRevision
	 * @return
	 * @throws TCException
	 */
	private TCComponentBOMLine getWorkAreaBOMLine(TCComponentItemRevision workAreaRevision) throws TCException
	{
		MFGLegacyApplication mfgLegacyApplication = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
		AbstractViewableTreeTable[] viewableTreeTables = mfgLegacyApplication.getViewableTreeTables();
		if (viewableTreeTables != null && viewableTreeTables.length > 0) 
		{
			for (AbstractViewableTreeTable abstractViewableTreeTable : viewableTreeTables) 
			{
				TCComponentBOMLine topBOMLine = abstractViewableTreeTable.getBOMRoot();
				TCComponentItemRevision itemRevision = topBOMLine.getItemRevision();
				if (topBOMLine != null && workAreaRevision == itemRevision) {
					return topBOMLine;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 遍历车间工厂，获取工厂工位的上一级
	 * 
	 * @param paramBOMLine
	 * @return
	 * @throws TCException
	 */
	private void traverseBOM(TCComponentBOMLine paramBOMLine, String stationId, Vector<TCComponentBOMLine> vector) throws TCException
	{
		AIFComponentContext[] contexts = paramBOMLine.getChildren();
		if (contexts != null && contexts.length > 0) 
		{
			for (AIFComponentContext context : contexts) 
			{
				TCComponentBOMLine childBOPLine = (TCComponentBOMLine) context.getComponent();
				if (childBOPLine.getProperty("bl_item_item_id").equals(stationId) && vector.size() == 0) {
					vector.add(paramBOMLine);
					break;
				}
				traverseBOM(childBOPLine, stationId, vector);
			}
		}
	}
	
}
