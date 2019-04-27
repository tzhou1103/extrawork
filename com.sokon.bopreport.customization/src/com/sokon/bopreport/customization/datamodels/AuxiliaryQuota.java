package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * 辅料定额 数据对象
 * 
 * @author zhoutong
 *
 */
public class AuxiliaryQuota 
{	
	public String objectType = "";				// 对象类型
	
	public String auxiliary = "";				// 辅料分类

	public String number = "";					// 辅料件号
	public String auxName = "";					// 辅料名称
	public String specification = "";			// 规格型号
	public String unit = "";					// 计量单位
	public double usageQuota = 0;				// 单车定额
	public String remark = "";					// 备注
	
	public AuxiliaryQuota(TCComponentBOPLine auxBopLine, TCComponentItemRevision auxRevision, String objectType, String auxiliary, String number, int languageSelection) 
	{
		this.objectType = objectType;
		this.auxiliary = auxiliary;
		this.number = number;
		
		try {
			if (auxRevision.isTypeOf("S4_IT_PartRevision")) 
			{
				TCComponentItemRevision auxPartRevision = (TCComponentItemRevision) auxRevision.getRelatedComponent("S4_REL_AuxiliaryPart");
				if (auxPartRevision != null && auxPartRevision.isTypeOf("S4_IT_AuxPartRevision")) 
				{
					String[] propNames = { "item_id", "object_name", "s4_CAT_ChineseName", "s4_AT_AuxiliarySpec" };
					String[] propValues = auxPartRevision.getProperties(propNames);
					if (propValues != null && propValues.length == 4) 
					{
						this.number = propValues[0];
						this.auxName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[1], propValues[2]);
						this.specification = propValues[3];
					}
					this.unit = auxPartRevision.getItem().getTCProperty("uom_tag").getUIFValue();
					this.remark = auxBopLine.getProperty("S4_NT_Remarks");
				}
			} else {
				String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "s4_BAT_AuxiliarySpec", "bl_uom", "S4_NT_Remarks" };
				String[] propValues = auxBopLine.getProperties(propNames);
				if (propValues != null && propValues.length == 5) 
				{
					this.auxName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
					this.specification = propValues[2];
					this.unit = propValues[3];
					this.remark = propValues[4];
				}
				
				if (objectType.equals("S4_IT_ProcessAuxRevision")) {
					this.specification = auxBopLine.getProperty("s4_BAT_SpecificationModel");
				}
			}
			
			// 直接辅料统计单车定额
			if (auxiliary.equals(ReportMessages.getString("DirectAuxiliaryQuota.Title"))) {
				this.usageQuota = TcUtil.getUsageQuantity(auxBopLine);
			} 
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

}
