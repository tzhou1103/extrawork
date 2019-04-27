package com.zht.customization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.zht.customization.utils.SessionUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GenerateBOMInfoHandler extends AbstractHandler {
	public static TCComponentUser user = null;

	/**
	 * The constructor.
	 */
	public GenerateBOMInfoHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			System.out.println("Enter" + event.getCommand().getName());
		} catch (NotDefinedException e) {
			e.printStackTrace();
		}
		SessionUtil.event = event;
		BOMInfoAction bomInfoAction = new BOMInfoAction(AIFUtility.getCurrentApplication().getDesktop(), "");
		new Thread(bomInfoAction).start();
//		ExportBOMInfoDialog dialog = new ExportBOMInfoDialog(
//				SessionUtil.GetTCShell(), 64);
//		OKListener.dialog = dialog;
//		dialog.open();

		return null;
	}
}
