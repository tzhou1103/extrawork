package com.zht.customization.handlers;

import org.eclipse.swt.widgets.Display;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.zht.customization.dialogs.ExportBOMInfoDialog;
import com.zht.customization.listeners.OKListener;
import com.zht.customization.utils.SessionUtil;

public class BOMInfoCommand extends AbstractAIFCommand {

	@Override
	public void executeModal() throws Exception {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				ExportBOMInfoDialog dialog = new ExportBOMInfoDialog(
						SessionUtil.GetTCShell(), 64);
				OKListener.dialog = dialog;
				dialog.open();
			}
		});		
	}
}
	
