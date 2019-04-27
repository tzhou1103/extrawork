package com.zht.report.handlers;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.zht.report.dialogs.ZHTConstants;
import com.zht.report.jobs.ERPToPLMUpdatePropertyJob;
import com.zht.report.utils.TcUtil;

public class ERPToPLMUpdatePropertyHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			if (!TcUtil.getTcSession().isUserSystemAdmin()) {
				MessageBox.post(ZHTConstants.ONLYDBAALLOWED_MSG, ZHTConstants.ERROR, 1);
				return null;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		FileDialog fileDialog = new FileDialog(AIFDesktop.getActiveDesktop().getShell(), SWT.OPEN);
		fileDialog.setText(ZHTConstants.OPEN);
		fileDialog.setFilterExtensions(new String[]{"*.txt"});
		fileDialog.setFilterNames(new String[]{ZHTConstants.TEXT_FILEFLITERNAMES});
		String filePath = fileDialog.open();
		if (filePath != null) 
		{
			ERPToPLMUpdatePropertyJob updatePropertyJob = new ERPToPLMUpdatePropertyJob(ZHTConstants.JOB_TITLE, filePath);
			updatePropertyJob.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(IJobChangeEvent event) 
				{
					ERPToPLMUpdatePropertyJob updatePropertyJob = (ERPToPLMUpdatePropertyJob) event.getJob();
					if (updatePropertyJob.isCompleted()) 
					{
						MessageBox.post(ZHTConstants.UPDATEPROPERTY_DONE_MSG, ZHTConstants.HINT, 2);
						return;
					}
					String logPath = updatePropertyJob.getLogFilePath();
					if ((logPath != null) && (!logPath.equals("")))
					{
						final File logFile = new File(logPath);
						if ((logFile.isFile()) && (logFile.canRead()))
						{
							SwingUtilities.invokeLater(new Runnable() 
							{
								public void run() 
								{
									Runtime runtime = Runtime.getRuntime();
									String cmd = "rundll32 url.dll FileProtocolHandler file://" + logFile.getAbsolutePath();
									try {
										runtime.exec(cmd);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							});
						}
						
						logFile.deleteOnExit();
		            }
				}
			});
			updatePropertyJob.setPriority(10);
			updatePropertyJob.setUser(true);
	        updatePropertyJob.schedule();
		}
		
		return null;
	}
}
