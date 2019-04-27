package com.zy.common;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.aif.kernel.AIFComponentDeleteEvent;
import com.teamcenter.rac.aif.kernel.AIFComponentEvent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponentEventListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCSession;

public class UDSStartup implements IStartup {

	public void earlyStartup() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				// 自动更新
				try {
					Thread.sleep(3000);
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new PreferredPerspectivePartListener());

				} catch (Exception e) {
					//
				}

			}
		});
	}

}
