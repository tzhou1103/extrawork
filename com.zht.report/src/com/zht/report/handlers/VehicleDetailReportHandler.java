package com.zht.report.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.zht.report.dialogs.ZHTConstants;
import com.zht.report.utils.SaveFileCommand;

public class VehicleDetailReportHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(targetComponent instanceof TCComponentBOMLine)) {
			MessageBox.post(ZHTConstants.INVALID_SELCTION_MSG, ZHTConstants.HINT, 2);
			return null;
		}
		
		final TCComponentBOMLine targetBOMLine = (TCComponentBOMLine) targetComponent;
		try {
			String itemRevType = targetBOMLine.getItemRevision().getType();
			if (!itemRevType.equals("Z9_VEHICLERevision")) {
				MessageBox.post(ZHTConstants.INVALID_SELCTION_MSG, ZHTConstants.HINT, 2);
				return null;
			}
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		SaveFileCommand command = new SaveFileCommand(ZHTConstants.VEHICLEDTAILRPORT, targetBOMLine);
		try {
			command.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
}
