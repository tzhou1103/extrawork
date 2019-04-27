package com.hasco.ssdt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hasco.ssdt.util.MsgBox;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFApplication;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class ImportInClassHandler extends AbstractHandler {
	public static AbstractAIFApplication application = null;
	private AIFDesktop parent;

	public ImportInClassHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		application = AIFUtility.getCurrentApplication();
	    if(application != null)
	    {
	        application.refresh();
	        parent = ((AbstractAIFUIApplication) application).getDesktop();
	    }
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		TCSession session = (TCSession)Activator.getDefault().getSessionService().getDefaultSession();
		InterfaceAIFComponent[] selectedObjects = Activator.getDefault().getSelectionMediatorService().getTargetComponents();
		TCComponentFolder pasterFolder = null;
		if (selectedObjects != null) {
			if (selectedObjects.length > 1) {
				MsgBox.showM("��ѡ�񵥸�������в���", "����", MessageBox.WARNING);
				return null;
			}
			if (!(selectedObjects[0] instanceof TCComponentFolder)) {
				MsgBox.showM("����ѡ��һ���ļ���", "����", MessageBox.WARNING);
				return null;
			}
			pasterFolder = (TCComponentFolder) selectedObjects[0];
		}else {
			try {
				pasterFolder = session.getUser().getNewStuffFolder();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		String currentRoleName = session.getCurrentRole().toDisplayString();
		
		boolean allowedFlag = false;
		TCPreferenceService prefSvc = session.getPreferenceService();
		String[] allowedRoles = prefSvc.getStringArray(TCPreferenceService.TC_preference_site,"HASCO_SSDT_Allowed_ImportInClass_Role");
		for (int i = 0; i < allowedRoles.length; i++) {
			if (allowedRoles[i].equals(currentRoleName)) {
				allowedFlag = true;
				break;
			}
		}
		
		if (allowedFlag) {
			//ImportInClassDialog dlg = new ImportInClassDialog(AIFDesktop.getActiveDesktop(), session, pasterFolder, shell);
			//modify by zhangmenglong 2014.3.15
			//new ImportInClassDialog(AIFDesktop.getActiveDesktop(), session, pasterFolder, parent);
			//dlg.showDialog();
		}else {
			MsgBox.showM(" ���ĵ�ǰ��ɫû��Ȩ��ִ�д˹���!", "����",  MessageBox.WARNING);
			return null;
		}
		
		return null;
	}
}

