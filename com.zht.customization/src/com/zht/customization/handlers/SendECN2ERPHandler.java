package com.zht.customization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.zht.customization.dialogs.GenerateBOMInfoDialog;
import com.zht.customization.listeners.BT_OKMouseListener;
import com.zht.customization.utils.SessionUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SendECN2ERPHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SendECN2ERPHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SessionUtil.event = event;
		System.out.println(SessionUtil.GetCommand());
		
		BT_OKMouseListener.ProgressDialogTitle = "���ڵ�����ERPϵͳ���ݸ�����Ϣ";
		BT_OKMouseListener.templateName = "�޸ĵ���ģ��";
		AbstractAIFUIApplication application = SessionUtil.GetApplication();
		InterfaceAIFComponent[] targetComponent = application
				.getTargetComponents();
		if (targetComponent.length == 2 && targetComponent[0].getType().equals(targetComponent[0].getType())) {
			GenerateBOMInfoDialog dialog = new GenerateBOMInfoDialog(SessionUtil.GetTCShell());
			try {
				int itemRev1No = Integer.parseInt(targetComponent[0].getProperty("item_revision_id"));
				int itemRev2No = Integer.parseInt(targetComponent[1].getProperty("item_revision_id"));
				if(itemRev1No>itemRev2No){
					dialog.itemRevH=(TCComponentItemRevision) targetComponent[0];
					dialog.itemRevL=(TCComponentItemRevision) targetComponent[1];
				}else{
					dialog.itemRevL=(TCComponentItemRevision) targetComponent[0];
					dialog.itemRevH=(TCComponentItemRevision) targetComponent[1];
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			BT_OKMouseListener.dialog = dialog;
			dialog.open();
		} else {
			MessageBox.post("��ѡ��2����ͬ���͵Ķ������Ƚ�!", "��ʾ", MessageBox.INFORMATION);
		}
		return null;
	}
}
