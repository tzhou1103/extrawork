package com.hasco.ssdt.de.nximport;


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
				NXCloneImportDlg dialog = new NXCloneImportDlg( AIFUtility.getCurrentApplication().getDesktop(),"模具部NX数模导入界面");
				dialog.setVisible(true);
				
			}
		}.start();
		
		return null;
	}

	
	
	
}