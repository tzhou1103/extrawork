package com.zht.customization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.zht.customization.dialogs.GenerateBOMInfoDialog;
import com.zht.customization.listeners.BT_OKMouseListener;
import com.zht.customization.utils.BOMUtil;
import com.zht.customization.utils.SessionUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SendNew2ERPHandler extends AbstractHandler {

	/**
	 * The constructor.
	 */
	public SendNew2ERPHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// IWorkbenchWindow window =
		// HandlerUtil.getActiveWorkbenchWindowChecked(event);
		SessionUtil.event = event;
		System.out.println(SessionUtil.GetCommand());
		BT_OKMouseListener.ProgressDialogTitle = "正在导出向ERP系统传递新增信息";
		BT_OKMouseListener.templateName = "修改单表模板";
		AbstractAIFUIApplication application = SessionUtil.GetApplication();
		InterfaceAIFComponent targetComponent = application
				.getTargetComponent();
		if (targetComponent.getType().equals("Z9_PartRevision")) {
			GenerateBOMInfoDialog dialog = new GenerateBOMInfoDialog(SessionUtil.GetTCShell());
			BT_OKMouseListener.dialog = dialog;
			dialog.open();
		} else {
			MessageBox.post("请选择零件版本!", "提示", MessageBox.INFORMATION);
		}

		return null;
	}
}
