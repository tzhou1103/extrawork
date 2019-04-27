package com.sokon.bopreport.customization.handlers;

import java.io.File;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.datamodels.CrucialProcess;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.util.JacobUtil;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 关键工序清单
 * 
 * @author zhoutong
 */
public class CrucialProcessListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_CrucialProcessList_Type";
			String[] crucialProcessListTypes = TcUtil.getPrefStringValues(prefName);
			if (crucialProcessListTypes == null || crucialProcessListTypes.length < 1) {
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent == null || !(targetComponent instanceof TCComponentBOPLine)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			TCComponentBOPLine targetBOPLine = (TCComponentBOPLine)targetComponent;
			TCComponentItemRevision targetRevision = targetBOPLine.getItemRevision();
			String targetType = targetRevision.getType();
			if (!Utilities.contains(targetType, crucialProcessListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("CrucialProcessList.zhCN.Title"));
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", "CrucialProcessList");
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("CrucialProcessList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateCrucialProcessList.Msg"));
				if (!confirm) {
					return null;
				}
			}
			
			int languageSelection = TcUtil.getLanguageSelction(shell);
			if (languageSelection > -1)
			{
				TargetBOP targetBOP = new TargetBOP(targetBOPLine, targetRevision, targetType);
				targetBOP.setLanguageSelection(languageSelection);
				targetBOP.setDocumentRevision(documentRevision);
				
				GenerateCrucialProcessListJob generateCrucialProcessListJob = new GenerateCrucialProcessListJob(ReportMessages.getString("hint.Title"), targetBOP);
				generateCrucialProcessListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						GenerateCrucialProcessListJob generateCrucialProcessListJob = (GenerateCrucialProcessListJob) event.getJob();
						if (generateCrucialProcessListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, generateCrucialProcessListJob.getReportDataset());
						}
					}
				});
				generateCrucialProcessListJob.setPriority(Job.INTERACTIVE);
				generateCrucialProcessListJob.setUser(true);
				generateCrucialProcessListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
	class GenerateCrucialProcessListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public GenerateCrucialProcessListJob(String name, TargetBOP targetBOP) 
		{
			super(name);
			this.targetBOP = targetBOP;
		}

		@Override
		protected IStatus run(IProgressMonitor progressMonitor) 
		{
			progressMonitor.beginTask(ReportMessages.getString("workingAndWait.Msg"), -1);
			
			String tempWorkingDir = "";
			try {
				Vector<CrucialProcess> crucialProcessVector = new Vector<CrucialProcess>();
				traverseBOP(this.targetBOP.bopLine, crucialProcessVector);
				
				File crucialProcessListFile = generateCrucialProcessListFile(crucialProcessVector);
				if (crucialProcessListFile.exists()) 
				{
					tempWorkingDir = crucialProcessListFile.getParent();
					
					// 工艺文档 名称/ 关键工序清单报表数据集名称
					String objectName = ReportMessages.getString("CrucialProcessList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, crucialProcessListFile, objectName);
					if (this.reportDataset != null) {
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("CrucialProcessList.zhCN.Title"));
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", "CrucialProcessList");
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("CrucialProcessList.documentType"));
						this.completed = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				TcUtil.deleteFolder(tempWorkingDir);
				progressMonitor.done();
			}
			return Status.OK_STATUS;
		}
		
		/**
		 * 遍历获取关键工序
		 * 
		 * @param paramBOPLine
		 * @param vector
		 * @throws TCException
		 */
		private void traverseBOP(TCComponentBOPLine paramBOPLine, Vector<CrucialProcess> vector) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					if (itemRevision instanceof TCComponentMEOPRevision) 
					{
						String keyOperation = itemRevision.getStringProperty("s4_AT_KeyOperation");
						if (keyOperation.equals("Y")) {
							CrucialProcess crucialProcess = new CrucialProcess(childBOPLine, this.targetBOP.languageSelection);
							vector.add(crucialProcess);
						}
					}
					traverseBOP(childBOPLine, vector);
				}
			}
		}

		/**
		 * 生成关键工序清单报表
		 * 
		 * @param crucialProcessVector
		 * @return
		 * @throws Exception
		 */
		private File generateCrucialProcessListFile(Vector<CrucialProcess> crucialProcessVector) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("CrucialProcessList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("CrucialProcessList.Template.RowCount");
			if (rowCount < 1) {
				throw new Exception(ReportMessages.getString("InvalidRowCountConfigurationForTemplate.Msg", templateDatasetName));
			}
			
			ActiveXComponent excelApp = JacobUtil.openExcelApp();
			Dispatch workBook = null;
			
			try {
				workBook = JacobUtil.getWorkBook(excelApp, templateFile);
				Dispatch sheets = JacobUtil.getSheets(workBook);
				Dispatch sheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
				
				// 计算行数
				int dataSize = crucialProcessVector.size();
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 5, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // 专业
				
				JacobUtil.writeCellData(sheet, "C2", variant);
				JacobUtil.writeCellData(sheet, "F2" , profession);
				JacobUtil.writeCellData(sheet, "F3", TcUtil.getCurrentDate()); // 编制日期 
				
				for (int i = 0; i < dataSize; i++) 
				{
					CrucialProcess crucialProcess = crucialProcessVector.get(i);
					
					JacobUtil.writeCellData(sheet, "A" + (i + 5), i + 1);
					JacobUtil.writeCellData(sheet, "B" + (i + 5), crucialProcess.station);
					JacobUtil.writeCellData(sheet, "C" + (i + 5), crucialProcess.processName);
					JacobUtil.writeCellData(sheet, "D" + (i + 5), crucialProcess.controlEssentials);
					JacobUtil.writeCellData(sheet, "E" + (i + 5), crucialProcess.CCSC);
					JacobUtil.writeCellData(sheet, "F" + (i + 5), crucialProcess.standard);
					JacobUtil.writeCellData(sheet, "G" + (i + 5), crucialProcess.remark);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("CrucialProcessList.enUS.Title"));
			return reportFile;
		}
		
		public boolean isCompleted() {
			return completed;
		}

		public TCComponentDataset getReportDataset() {
			return reportDataset;
		}
		
	}
	
}
