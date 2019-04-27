package com.hasco.ssdt.report.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.hasco.ssdt.report.dialog.OutputReportDialog;
import com.hasco.ssdt.report.meopflowchart.ExportMeopFlowchartJob;
import com.hasco.ssdt.report.utils.FileUtility;
import com.hasco.ssdt.report.utils.TcUtil;
import com.hasco.ssdt.report.utils.ViewUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.commands.open.OpenCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.sequence.pert.PertView;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

public class OutputReportHandler extends AbstractHandler 
{
	/** ���հ汾���� */
	private static final String[] PROCESSREV_TYPES = { "H5_BHSSAsmPrRevision", "H5_BHSSMachPrRevision" };
	
	private Shell activeShell;
	private TCComponentBOMLine processBOMLine;
	
	/** PERT��ͼID */
	private static final String PERT_VIEWID = "com.teamcenter.rac.cme.sequence.pert";
	private PertView pertView;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent == null || !(targetComponent instanceof TCComponentBOMLine)) {
				MessageBox.post("��ѡ���ն���", "��ʾ", 2);
				return null;
			}
			
			this.processBOMLine = (TCComponentBOMLine) targetComponent;
			TCComponentItemRevision itemRevision = this.processBOMLine.getItemRevision();
			if (this.processBOMLine.parent() != null || !Utilities.contains(itemRevision.getType(), PROCESSREV_TYPES)) {
				MessageBox.post("��ѡ��ָ�����͵Ĺ��ն���", "��ʾ", 2);
				return null;
			}
			
			if (!TcUtil.checkUserWriteAccessPrivilige(itemRevision)) {
				MessageBox.post("���Ե�ǰ����û��дȨ�ޣ�", "��ʾ", 2);
				return null;
			}
			
			this.activeShell = HandlerUtil.getActiveShell(event);
			
			List<String> reportNameList = new ArrayList<String>();
			String reportNames = FileUtility.getValue("reportNames");
			String[] splitStrs = reportNames.split(";");
			for (String reportName : splitStrs) {
				reportNameList.add(reportName);
			}
			if (reportNameList.size() < 1) {
				MessageBox.post("�����ļ���config.properties����δ���á�reportNames����ֵ��", "����", 1);
				return null;
			}
			
			OutputReportDialog outputReportDialog = new OutputReportDialog(activeShell, reportNameList);
			if (Dialog.OK == outputReportDialog.open()) 
			{
				String reportName = outputReportDialog.getReportName();
				final boolean inDataset = outputReportDialog.isInDataset();
				final boolean openOnGenerate = outputReportDialog.isOpenOnGenerate();
				final String dirctoryPath = outputReportDialog.getDirctoryPath();
				
				if (reportName.equals("��������ͼ")) 
				{
					IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					this.pertView = ViewUtil.findView(PERT_VIEWID);
					if (this.pertView == null || this.pertView.getInputRootObject() == null 
							|| this.pertView.getInputRootObject() != this.processBOMLine) {
						MessageBox.post("δ������ " + this.processBOMLine + " ��PERT��ʽ�򿪣�", "����", 4);
						return null;
						
						/*System.out.println(">>> ������ " + this.processBOMLine + " ��PERT��ʽ��...");
						 * ����򿪺���δ�ҵ������ȴ����ݼ�����
						this.pertView = ViewUtil.openView(PERT_VIEWID);
						this.pertView.setInputRootObject(workbenchPage.getActivePart(), this.processBOMLine);*/
					} 
					
					workbenchPage.activate(this.pertView);
					exportMeopFlowchart(inDataset, dirctoryPath, openOnGenerate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
	private void exportMeopFlowchart(final boolean inDataset, String dirctoryPath, final boolean openOnGenerate) 
	{
		ExportMeopFlowchartJob exportMeopFlowchartJob = new ExportMeopFlowchartJob("����", this.processBOMLine, this.pertView, inDataset, dirctoryPath);
		exportMeopFlowchartJob.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(IJobChangeEvent event) 
			{
				final ExportMeopFlowchartJob exportMeopFlowchartJob = (ExportMeopFlowchartJob) event.getJob();
				if (exportMeopFlowchartJob.isCompleted()) 
				{
					OutputReportHandler.this.activeShell.getDisplay().asyncExec(new Runnable() 
					{						
						@Override
						public void run() 
						{
							MessageDialog.openInformation(OutputReportHandler.this.activeShell, "��ʾ", "��������ͼ������ɣ�");
							if (openOnGenerate) 
							{
								if (inDataset) {
									OpenCommand command = new OpenCommand(AIFDesktop.getActiveDesktop(), exportMeopFlowchartJob.getReportDataset());
									try {
										command.executeModal();
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									FileUtility.openFile(exportMeopFlowchartJob.getMeopFlowchartFile());
								}
							} 
						}
					});
				}
			}
		});
		exportMeopFlowchartJob.setPriority(10);
		exportMeopFlowchartJob.setUser(true);
		exportMeopFlowchartJob.schedule();
	}

}
