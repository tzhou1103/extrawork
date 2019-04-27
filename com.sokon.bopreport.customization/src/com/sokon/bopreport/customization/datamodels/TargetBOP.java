package com.sokon.bopreport.customization.datamodels;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * 基于所选BOP行的自定义对象
 * <p>避免常规属性重复取值, 减少参数传递
 * 
 * @author zhoutong
 *
 */
public class TargetBOP 
{
	public TCComponentBOPLine bopLine; // 所选BOP行
	public TCComponentItemRevision itemRevision;
	public String objectType = "";
	public String objectName = "";
	
	public int languageSelection = -1;					// 语言选择
	public TCComponentItemRevision documentRevision;	// 工艺文档版本
	
	public TCComponent variantModel;
	
	public TargetBOP(TCComponentBOPLine bopLine, TCComponentItemRevision itemRevision, String objectType) 
	{
		super();
		this.bopLine = bopLine;
		this.itemRevision = itemRevision;
		this.objectType = objectType;
		try {
			this.objectName = itemRevision.getStringProperty("object_name");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	public int getLanguageSelection() {
		return languageSelection;
	}

	public void setLanguageSelection(int languageSelection) {
		this.languageSelection = languageSelection;
	}
	
	public TCComponentItemRevision getDocumentRevision() {
		return documentRevision;
	}

	public void setDocumentRevision(TCComponentItemRevision documentRevision) {
		this.documentRevision = documentRevision;
	}
	
	public TCComponent getVariantModel() {
		return variantModel;
	}

	public void setVariantModel(TCComponent variantModel) {
		this.variantModel = variantModel;
	}

	/**
	 * 获取String类型属性值
	 * 
	 * @param propertyName 属性名
	 * @return
	 */
	public String getPropertyValue(String propertyName)
	{
		try {
			if (this.itemRevision != null) {
				return this.itemRevision.getStringProperty(propertyName);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
}
