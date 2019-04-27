package com.sokon.bopreport.customization.handlers;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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
import com.sokon.bopreport.customization.datamodels.CheckingFixtureList;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.util.JacobUtil;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 工装清单
 * 
 * @author zhoutong
 *
 */
public class OutillageListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_OutillageList_Type";
			String[] outillageListTypes = TcUtil.getPrefStringValues(prefName);
			if (outillageListTypes == null || outillageListTypes.length < 1) {
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
			if (!Utilities.contains(targetType, outillageListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("OutillageList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateOutillageList.Msg"));
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
				
				OutillageListJob outillageListJob = new OutillageListJob(ReportMessages.getString("hint.Title"), targetBOP);
				outillageListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						OutillageListJob outillageListJob = (OutillageListJob) event.getJob();
						if (outillageListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, outillageListJob.getReportDataset());
						}
					}
				});
				outillageListJob.setPriority(Job.INTERACTIVE);
				outillageListJob.setUser(true);
				outillageListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}
	
	class OutillageListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private String[] outillageListTypes;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public OutillageListJob(String name, TargetBOP targetBOP) 
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
				String prefName = "S4CUST_OutillageList_CheckingFixtureType";
				this.outillageListTypes = TcUtil.getPrefStringValues(prefName);
				if (this.outillageListTypes == null || this.outillageListTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				LinkedHashMap<String, CheckingFixtureList> checkingFixtureListMap = new LinkedHashMap<String, CheckingFixtureList>();
				traverseBOP(this.targetBOP.bopLine, checkingFixtureListMap);
				
				File outillageListFile = generateOutillageListFile(checkingFixtureListMap);
				if (outillageListFile.exists()) 
				{
					tempWorkingDir = outillageListFile.getParent();
					
					// 工艺文档 名称/ 工装清单报表数据集名称
					String objectName = ReportMessages.getString("OutillageList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, outillageListFile, objectName);
					if (this.reportDataset != null) {
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("OutillageList.documentType"));
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
		 * 遍历获取检具
		 * 
		 * @param paramBOPLine
		 * @param vector
		 * @throws TCException
		 */
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, CheckingFixtureList> checkingFixtureListMap) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					String itemId = itemRevision.getProperty("item_id");
					if (itemRevision.isTypeOf(this.outillageListTypes)) 
					{
						CheckingFixtureList checkingFixtureList = checkingFixtureListMap.get(itemId);
						if (checkingFixtureList == null) {
							checkingFixtureList = new CheckingFixtureList(childBOPLine, this.targetBOP.languageSelection);
						} else {
							checkingFixtureList.quantity += TcUtil.getUsageQuantity(childBOPLine);
							checkingFixtureList.remark = TcUtil.getAppendRemark(checkingFixtureList.remark, childBOPLine);
						}
						checkingFixtureListMap.put(itemId, checkingFixtureList);
					}
					traverseBOP(childBOPLine, checkingFixtureListMap);
				}
			}
		}
		
		/**
		 * 生成工装清单报表
		 * 
		 * @param checkingFixtureListMap
		 * @return
		 * @throws Exception
		 */
		private File generateOutillageListFile(LinkedHashMap<String, CheckingFixtureList> checkingFixtureListMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("OutillageList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("OutillageList.Template.RowCount");
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
				int dataSize = checkingFixtureListMap.size();
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 5, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // 专业
				
				JacobUtil.writeCellData(sheet, "B2", variant);
				JacobUtil.writeCellData(sheet, "E2" , profession);
				JacobUtil.writeCellData(sheet, "E3", TcUtil.getCurrentDate()); // 编制日期 
				
				int index = 0;
				for (Entry<String, CheckingFixtureList> entry : checkingFixtureListMap.entrySet()) 
				{
					CheckingFixtureList checkingFixtureList = entry.getValue();
					JacobUtil.writeCellData(sheet, "A" + (index + 5), index + 1);
					JacobUtil.writeCellData(sheet, "B" + (index + 5), checkingFixtureList.checkingFixtureName);
					JacobUtil.writeCellData(sheet, "C" + (index + 5), checkingFixtureList.checkingFixtureNumber);
					JacobUtil.writeCellData(sheet, "D" + (index + 5), checkingFixtureList.productSize);
					JacobUtil.writeCellData(sheet, "E" + (index + 5), checkingFixtureList.quantity);
					JacobUtil.writeCellData(sheet, "F" + (index + 5), checkingFixtureList.remark);
					
					index++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("OutillageList.enUS.Title"));
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
