package com.zht.report.log;

import java.io.File;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;

public class StringViewerCommand extends AbstractAIFCommand 
{
	public StringViewerCommand(File paramFile)
	{
		StringViewerDialog viewerDialog = new StringViewerDialog(AIFDesktop.getActiveDesktop(), paramFile);
		setRunnable(viewerDialog);
	}

}
