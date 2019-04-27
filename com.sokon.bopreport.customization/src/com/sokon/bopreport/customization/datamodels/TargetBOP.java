package com.sokon.bopreport.customization.datamodels;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * ������ѡBOP�е��Զ������
 * <p>���ⳣ�������ظ�ȡֵ, ���ٲ�������
 * 
 * @author zhoutong
 *
 */
public class TargetBOP 
{
	public TCComponentBOPLine bopLine; // ��ѡBOP��
	public TCComponentItemRevision itemRevision;
	public String objectType = "";
	public String objectName = "";
	
	public int languageSelection = -1;					// ����ѡ��
	public TCComponentItemRevision documentRevision;	// �����ĵ��汾
	
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
	 * ��ȡString��������ֵ
	 * 
	 * @param propertyName ������
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
