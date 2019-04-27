package com.sokon.bopreport.customization.handlers;

import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.processcarddownload.DownloadDialog;
import com.sokon.bopreport.customization.processcarddownload.ProcessCardDownloadJob;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentCCObject;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 工艺卡打包下载
 * 
 * @author zhoutong
 */
public class ProcessCardDownloadHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_ProcessCardDownload_SelectType";
			String[] selectTypes = TcUtil.getPrefStringValues(prefName);
			if (selectTypes == null || selectTypes.length < 1) {
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent == null || !(targetComponent instanceof TCComponent)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			Vector<TCComponentItemRevision> bopRevVector = new Vector<TCComponentItemRevision>();
			
			if (targetComponent instanceof TCComponentCCObject) {
				TCComponentCCObject ccObject = (TCComponentCCObject) targetComponent;
				bopRevVector = TcUtil.getBopRevisionsByCCObject(ccObject, selectTypes);
				if (bopRevVector.size() == 0) {
					MessageBox.post(ReportMessages.getString("noSpecifiedBOP.Msg"), ReportMessages.getString("hint.Title"), 2);
					return null;
				}
			} else if (targetComponent instanceof TCComponentItemRevision) {
				String type = ((TCComponent)targetComponent).getType();
				if (!Utilities.contains(type, selectTypes)) {
					MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
					return null;
				} else {
					TCComponentItemRevision itemRevision = (TCComponentItemRevision) targetComponent;
					bopRevVector.add(itemRevision);
				}
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			DownloadDialog downloadDialog = new DownloadDialog(shell);
			if (Dialog.OK == downloadDialog.open()) 
			{
				String path = downloadDialog.getPath();
				
				ProcessCardDownloadJob downloadJob = new ProcessCardDownloadJob(ReportMessages.getString("hint.Title"), bopRevVector, path);
				downloadJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event)
					{
						ProcessCardDownloadJob downloadJob = (ProcessCardDownloadJob) event.getJob();
						if (downloadJob.completed) 
						{
							if (downloadJob.fileCount == 0) {
								MessageBox.post(ReportMessages.getString("noProcessReportDataForDownload.Msg"), ReportMessages.getString("hint.Title"), 2);
								return;
							}
							MessageBox.post(ReportMessages.getString("processReportDownloadSucceed.Msg"), ReportMessages.getString("hint.Title"), 2);
						}
					}
				});
				downloadJob.setPriority(10);
				downloadJob.setUser(true);
				downloadJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}

}
