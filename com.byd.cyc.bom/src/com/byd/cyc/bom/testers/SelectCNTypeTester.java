package com.byd.cyc.bom.testers;

import org.eclipse.core.expressions.PropertyTester;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * �����Ҽ��˵������ɸ���֪ͨ������ʾ
 * 
 * @author zhoutong
 *
 */
public class SelectCNTypeTester extends PropertyTester 
{
	@Override
	public boolean test(Object targetObject, String property, Object[] args, Object expectValue) 
	{
		try {
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent != null && targetComponent instanceof TCComponentItemRevision) 
			{
				TCComponentItemRevision itemRev = (TCComponentItemRevision)targetComponent;
				if (itemRev.isTypeOf("Z9_DCNRevision")) {
					return true;
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
