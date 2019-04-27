package com.hasco.ssdt.oem.nximport;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aifrcp.AIFUtility;

public class NXCloneImportHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
				
		new Thread()
		{
			@Override
			public void run()
			{
				NXCloneImportDlg dialog = new NXCloneImportDlg( AIFUtility.getCurrentApplication().getDesktop(),"OEM NX数模导入选择界面");
				dialog.setVisible(true);
				
			}
		}.start();
		
		return null;
	}

	
	
	
}