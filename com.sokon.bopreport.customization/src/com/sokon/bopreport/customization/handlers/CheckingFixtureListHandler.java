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
 * 检具清单
 * 
 * @author zhoutong
 *
 */
public class CheckingFixtureListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_CheckingFixtureList_Type";
			String[] checkingFixtureTypes = TcUtil.getPrefStringValues(prefName);
			if (checkingFixtureTypes == null || checkingFixtureTypes.length < 1) {
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
			if (!Utilities.contains(targetType, checkingFixtureTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("CheckingFixtureList.zhCN.Title"));
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", "CheckingFixtureList");
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("CheckingFixtureList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateCheckingFixtureList.Msg"));
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
				
				CheckingFixtureListJob checkingFixtureListJob = new CheckingFixtureListJob(ReportMessages.getString("hint.Title"), targetBOP);
				checkingFixtureListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						CheckingFixtureListJob checkingFixtureListJob = (CheckingFixtureListJob) event.getJob();
						if (checkingFixtureListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, checkingFixtureListJob.getReportDataset());
						}
					}
				});
				checkingFixtureListJob.setPriority(Job.INTERACTIVE);
				checkingFixtureListJob.setUser(true);
				checkingFixtureListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}
	
	class CheckingFixtureListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private String[] checkingFixtureListTypes;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public CheckingFixtureListJob(String name, TargetBOP targetBOP) 
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
				String prefName = "S4CUST_CheckingFixtureList_CheckingFixtureType";
				this.checkingFixtureListTypes = TcUtil.getPrefStringValues(prefName);
				if (this.checkingFixtureListTypes == null || this.checkingFixtureListTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				LinkedHashMap<String, CheckingFixtureList> checkingFixtureListMap = new LinkedHashMap<String, CheckingFixtureList>();
				traverseBOP(this.targetBOP.bopLine, checkingFixtureListMap);
				
				File checkingFixtureListFile = generateCheckingFixtureListFile(checkingFixtureListMap);
				if (checkingFixtureListFile.exists()) 
				{
					tempWorkingDir = checkingFixtureListFile.getParent();
					
					// 工艺文档 名称/ 检具清单报表数据集名称
					String objectName = ReportMessages.getString("CheckingFixtureList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, checkingFixtureListFile, objectName);
					if (this.reportDataset != null) {
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("CheckingFixtureList.zhCN.Title"));
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", "CheckingFixtureList");
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("CheckingFixtureList.documentType"));
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
					if (itemRevision.isTypeOf(this.checkingFixtureListTypes)) 
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
		 * 生成检具清单报表
		 * 
		 * @param checkingFixtureListMap
		 * @return
		 * @throws Exception
		 */
		private File generateCheckingFixtureListFile(LinkedHashMap<String, CheckingFixtureList> checkingFixtureListMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("CheckingFixtureList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("CheckingFixtureList.Template.RowCount");
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
			
//			return templateFile;
			
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("CheckingFixtureList.enUS.Title"));
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
