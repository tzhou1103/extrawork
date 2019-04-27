package com.sokon.bopreport.customization.datamodels;

import java.util.Vector;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;

/**
 * 原材料清单 数据对象
 * 
 * @author zhoutong
 *
 */
public class StampingIHPartsBlankList 
{	
	public Vector<Part> partVector = new Vector<Part>();
	public Vector<SheetMetal> sheetMetalVector = new Vector<SheetMetal>();
	public int rowCount;
	
	public class Part 
	{
		public String partNumber = "";
		public String partName = "";
		public double vehicleUsage;
		
		public Part(TCComponentBOPLine partBopLine, int languageSelection) 
		{
			try {
				String[] propNames = { "bl_item_item_id", "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName" };
				String[] propValues = partBopLine.getProperties(propNames);
				if (propValues != null && propValues.length == 3) 
				{
					this.partNumber = propValues[0];
					this.partName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[1], propValues[2]);
				}
				this.vehicleUsage = TcUtil.getUsageQuantity(partBopLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public class SheetMetal
	{
		public String material = "";
		public String thickness = "";
		public String length = "";
		public String width = "";
		public String materialWeight = "";
		public String rollWidth = "";
		public String remark = "";
		
		public SheetMetal(TCComponentBOPLine sheetMetalBopLine) 
		{
			try {
				String[] propNames = { "s4_AT_Material", "s4_AT_Thickness", "s4_AT_Length", "s4_AT_Width", "s4_AT_ActualMass", "s4_AT_CoilWidth" };
				String[] propValues = sheetMetalBopLine.getItemRevision().getProperties(propNames);
				if (propValues != null && propValues.length == 6) 
				{
					this.material = propValues[0];
					this.thickness = propValues[1];
					this.length = propValues[2];
					this.width = propValues[3];
					this.materialWeight = propValues[4];
					this.rollWidth = propValues[5];
				}
				this.remark = sheetMetalBopLine.getProperty("S4_NT_Remarks");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
}
