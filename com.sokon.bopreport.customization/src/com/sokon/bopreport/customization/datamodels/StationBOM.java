package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * 工位物料清单 数据对象
 * 
 * @author zhoutong
 *
 */
public class StationBOM 
{	
	public String station = "";			// 工位
	public String locationAddress = "";	// 工位地址
	
	public String partName = "";		// 零件名称
	public String partNumber = "";		// 零件件号
	public double quantity;				// 零件数量
//	public String remark = ""; // 2018-12-28, 不输出备注信息
	
	public StationBOM(TCComponentBOPLine partBopLine, String station, String lcationAddress, int languageSelection) 
	{
		try {
			this.station = station;
			this.locationAddress = lcationAddress;
			
			// 特殊处理
			boolean hasAuxiliaryPart = false;
			if (partBopLine.getItem().isTypeOf("S4_IT_Part")) {
				hasAuxiliaryPart = getITPart(partBopLine, languageSelection);
			} 
			
			if(!hasAuxiliaryPart)
			{
				String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "bl_item_item_id" };
				String[] propValues = partBopLine.getProperties(propNames);
				if (propValues != null && propValues.length == 3)
				{
					this.partName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
					this.partNumber = propValues[2];
				}
			}
			
			this.quantity = TcUtil.getUsageQuantity(partBopLine);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	public boolean getITPart(TCComponentBOPLine partBopLine, int languageSelection) throws TCException
	{
		TCComponentItemRevision partRevision = partBopLine.getItemRevision();
		TCComponentItemRevision auxPartItemRev = null;
		TCComponent relatedComponent = partRevision.getRelatedComponent("S4_REL_AuxiliaryPart");
		if (relatedComponent != null) 
		{
			if (relatedComponent instanceof TCComponentItem) {
				TCComponentItem auxPart = (TCComponentItem) relatedComponent;
				auxPartItemRev = auxPart.getLatestItemRevision();
			} else if (relatedComponent instanceof TCComponentItemRevision) {
				auxPartItemRev = (TCComponentItemRevision) relatedComponent;
			}
		}
		
		if (auxPartItemRev != null && auxPartItemRev.isTypeOf("S4_IT_AuxPartRevision")) 
		{
			String[] propNames = new String[] { "object_name", "s4_CAT_ChineseName", "item_id" };
			String[] propValues = auxPartItemRev.getProperties(propNames);
			if (propValues != null && propValues.length == 3)
			{
				this.partName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				this.partNumber = propValues[2];
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((partNumber == null) ? 0 : partNumber.hashCode());
		result = prime * result + ((locationAddress == null) ? 0 : locationAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StationBOM other = (StationBOM) obj;
		if (partNumber == null) {
			if (other.partNumber != null)
				return false;
		} else if (!partNumber.equals(other.partNumber))
			return false;
		if (locationAddress == null) {
			if (other.locationAddress != null)
				return false;
		} else if (!locationAddress.equals(other.locationAddress))
			return false;
		return true;
	}
	
}
