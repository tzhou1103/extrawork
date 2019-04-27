package com.zht.customization.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.AbstractPSEApplication;

public class SessionUtil {

	private static AbstractAIFUIApplication app;
	private static TCSession session;
	private static AIFDesktop desktop;
	private static Shell tcShell;
	private static String command = "";
	public static ExecutionEvent event;
	public static Map<String, String> pattern = new HashMap<>();

	public static AbstractAIFUIApplication GetApplication() {
		return app == null ? AIFUtility.getCurrentApplication() : app;
	}

	public static TCSession GetSession() {
		return (TCSession) (session == null ? GetApplication().getSession() : session);
	}

	public static AIFDesktop GetDesktop() {
		return desktop == null ? GetApplication().getDesktop() : desktop;
	}

	public static Shell GetTCShell() {
		if (event == null)
			return tcShell;

		try {
			tcShell = tcShell == null ? HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell() : tcShell;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return tcShell;
	}

	public static String GetCommand() {
		if (event == null)
			return "";
		try {
			command = event.getCommand().getName();
		} catch (NotDefinedException e) {
			e.printStackTrace();
		}
		return command;
	}

	public static TCComponentBOMLine[] GetTargetBOMLines() {
		Vector<TCComponentBOMLine> bomLinesVec = new Vector<TCComponentBOMLine>();
		Vector<TCComponent> targetItemRevisionVec = new Vector<TCComponent>();
		InterfaceAIFComponent[] targetComponents = GetApplication().getTargetComponents();

		for (InterfaceAIFComponent targetComponent : targetComponents) {
			TCComponentBOMLine bomLine = (TCComponentBOMLine) targetComponent;
			bomLinesVec.add(bomLine);
			try {
				targetItemRevisionVec.add(bomLine.getItemRevision());
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		BOMUtil.askParent(targetItemRevisionVec, targetItemRevisionVec, bomLinesVec);
		return bomLinesVec.toArray(new TCComponentBOMLine[] {});
	}

	public static String[] getPreference(String preferenceName) {
		TCPreferenceService preferenceService = GetSession().getPreferenceService();
		return preferenceService.getStringValues(preferenceName);
	}

	public static void getQCList() {
		if (pattern.isEmpty()) {
			String[] preferenceValues = SessionUtil.getPreference("zht_qcfl_list");
			for (String preferenceValue : preferenceValues) {
				String[] split = preferenceValue.split(",");
				pattern.put(split[1], split[0]);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AbstractPSEApplication getApplication = (AbstractPSEApplication) GetApplication();
		TCComponentBOMWindow bomWindow = getApplication.getBOMWindow();
	}

}
