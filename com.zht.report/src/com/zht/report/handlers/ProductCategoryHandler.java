package com.zht.report.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.MessageBox;
import com.zht.report.dialogs.ZHTConstants;
import com.zht.report.utils.SaveFileCommand;

public class ProductCategoryHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
		if (targetComponent == null || !(targetComponent instanceof TCComponentBOMLine)) {
			MessageBox.post(ZHTConstants.NOBOMLINE_SELCTD_MSG, ZHTConstants.HINT, 2);
			return null;
		}
		
		final TCComponentBOMLine targetBOMLine = (TCComponentBOMLine) targetComponent;
		
		SaveFileCommand command = new SaveFileCommand(ZHTConstants.PRODUCTCATEGORY, targetBOMLine);
		try {
			command.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
	
	
}
