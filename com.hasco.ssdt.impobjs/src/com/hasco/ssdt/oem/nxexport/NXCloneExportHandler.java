package com.hasco.ssdt.oem.nxexport;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aifrcp.AIFUtility;

public class NXCloneExportHandler  extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
				
		new Thread()
		{
			@Override
			public void run()
			{
				NXCloneExportDlg dialog = new NXCloneExportDlg( AIFUtility.getCurrentApplication().getDesktop(),"OEM NX��ģ����ѡ�����");
				dialog.setVisible(true);
				
			}
		}.start();
		
		return null;
	}

	
	
	
}
