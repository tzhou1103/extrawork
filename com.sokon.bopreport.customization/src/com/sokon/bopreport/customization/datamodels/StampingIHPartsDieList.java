package com.sokon.bopreport.customization.datamodels;

import java.util.Vector;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;

/**
 * 模具清单 数据对象
 * 
 * @author zhoutong
 *
 */
public class StampingIHPartsDieList 
{
	public TCComponentBOPLine bopLine;
	
	public Vector<ProductInfo> productInfoVector = new Vector<ProductInfo>();
	public Vector<ToolInfo> toolInfoVector = new Vector<ToolInfo>();
	
	public int quantity;
	public String method = "自动";
	public int rowCount = 1;
	
	public StampingIHPartsDieList(TCComponentBOPLine bopLine) {
		this.bopLine = bopLine;
	}

	public class ProductInfo 
	{
		public String partNumber = "";
		public String partName = "";
		public double vehicleUsage;
		public String material = "";
		public String thickness = "";
		public String weight = "";
		public String partSize = "";
		
		public ProductInfo(TCComponentBOPLine partBopLine, int languageSelection)
		{
			try {
				String[] propNames = { "item_id", "object_name", "s4_CAT_ChineseName", "s4_AT_Material", "s4_CAT_Thicknesee", "s4_AT_ActualMass" };
				String[] propValues = partBopLine.getItemRevision().getProperties(propNames);
				if (propValues != null && propValues.length == 6) 
				{
					this.partNumber = propValues[0];
					this.partName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[1], propValues[2]);
					this.material = propValues[3];
					this.thickness = propValues[4];
					this.weight = propValues[5];
				}
				this.partSize = partBopLine.getProperty("S4_NT_PartSize");
				this.vehicleUsage = TcUtil.getUsageQuantity(partBopLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public class ToolInfo 
	{
		public String station = "";
		public String stationName = "";
		public String moldSize = "";
		public String moldWeight = "";
		public String remark = "";
		
		public ToolInfo(TCComponentBOPLine partBopLine, int languageSelection)
		{
			try {
				TCComponentBOPLine operationBopLine = (TCComponentBOPLine) partBopLine.parent();
				String[] propNames = { "s4_BAT_OperationNumber", "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName" };
				String[] propValues = operationBopLine.getProperties(propNames);
				if (propValues != null && propValues.length == 3) 
				{
					this.station = propValues[0];
//					this.stationName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[1], propValues[2]);
					this.stationName = propValues[1];
				}
				
				propNames = new String[] { "s4_AT_Size", "s4_AT_DieWeight" };
				propValues = partBopLine.getItemRevision().getProperties(propNames);
				if (propValues != null && propValues.length == 2) 
				{
					this.moldSize = propValues[0];
					this.moldWeight = propValues[1];
				}
				this.remark = partBopLine.getProperty("S4_NT_Remarks");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
