package com.zht.customization.utils;



import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.services.IPortalService;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.UIUtilities;

/**
 * @author ChenRui
 * @purpose ʵ��swing��dialog��swt��shell�����µ�modal
 *          ��dialog���ɺ󣬴���һ���յ�Shell�������������ShellΪfocus��modal
 *          ����dialog����setVisible(true)
 */ 
public class SetModalCommand {

	private Shell helperShell = null;

	private Runnable runnable;

  /**
	 * @param runnable  һ��ʵ����Runnable�ӿڵĶ���
   *                  Teamcenter��AbstractAIFDialog����౾���ʵ����Runnable,
   *                  ����ֱ�Ӵ���new AbstractAIFDialog()
	 */
	public SetModalCommand(Runnable runnable) {
		this.runnable = runnable;
	}

	public void executeModal() {
		if (SwingUtilities.isEventDispatchThread())
			executeSwingCommand();
		else
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					executeSwingCommand();
				}
			});
	}

	private void executeSwingCommand() {
		IPortalService localIPortalService = (IPortalService) OSGIUtil.getService(AifrcpPlugin.getDefault(), IPortalService.class.getName());
		
		if (!localIPortalService.login()) {
			Dialog localDialog = (Dialog) runnable;
			localDialog.addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent paramWindowEvent) {
					if (((Dialog) runnable).isModal()) {
						//UIUtilities.setCurrentModalShell(paramShell);
						
						openHelperModalShell((Dialog) runnable);
					}
				}

				public void windowClosed(WindowEvent paramWindowEvent) {
					if (((Dialog) runnable).isModal()) {
						closeHelperModalShell();
						//UIUtilities.setCurrentModalDialog((Dialog) null);
					}
				}
			});
		}
		if (runnable != null)
			runnable.run();
	}

	public void openHelperModalShell(final Dialog dialog) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Shell localShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				if ((localShell != null) && (helperShell == null)) {
					helperShell = new Shell(localShell, 65544);
					Rectangle localRectangle = localShell.getBounds();
					int i = localRectangle.x + localRectangle.width / 2;
					int j = localRectangle.y + localRectangle.height / 2;
					helperShell.setBounds(i, j, 0, 0);
					helperShell.setVisible(true);
					UIUtilities.setCurrentModalShell(helperShell);
					helperShell.addFocusListener(new FocusAdapter() {
						public void focusGained(FocusEvent paramFocusEvent) {
							if (dialog != null)
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										dialog.toFront();
									}
								});
						}
					});
					helperShell.setFocus();
				}
			}
		});
	}

	public void closeHelperModalShell() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (helperShell != null) {
					helperShell.dispose();
					helperShell = null;
					UIUtilities.setCurrentModalShell((Shell) null);
				}
			}
		});
	}

}

