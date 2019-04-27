package com.zht.customization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.util.MessageBox;
import com.zht.customization.dialogs.CreateDrawingDialog;
import com.zht.customization.dialogs.ExportBOMInfoDialog;
import com.zht.customization.listeners.OKListener;
import com.zht.customization.utils.SessionUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CreateDrawingHandler extends AbstractHandler {
	public static TCComponentUser user = null;

	/**
	 * The constructor.
	 */
	public CreateDrawingHandler() {
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
		AbstractAIFUIApplication application = SessionUtil.GetApplication();
		InterfaceAIFComponent targetComponent = application.getTargetComponent();
		// String type = targetComponent.getType();
		// if (type.equals("Z9_PartRevision")) {
		// 修改对象类型，modidied by tzhou,20180711
		if (targetComponent instanceof TCComponentItemRevision) {
			CreateDrawingDialog createDrawingDialog = new CreateDrawingDialog(application.getDesktop().getShell());
			createDrawingDialog.setItemRevision((TCComponentItemRevision) targetComponent);
			createDrawingDialog.open();
		} else {
			// MessageBox.post("请选择零部件对象", "警告", MessageBox.WARNING);
			MessageBox.post("请选择版本对象", "警告", MessageBox.WARNING);
		}

		return null;
	}
}
