package com.zht.report.utils;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.zht.report.dialogs.SaveFileDialog;

public class SaveFileCommand extends AbstractAIFCommand
{
	public SaveFileCommand(String paramTitle, TCComponentBOMLine paramBOMLine) {
		SaveFileDialog dialog = new SaveFileDialog(AIFDesktop.getActiveDesktop(), paramTitle, paramBOMLine);
		setRunnable(dialog);
	}
}
