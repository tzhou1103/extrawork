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
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.datamodels.ToolList;
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
 * �����嵥
 * 
 * @author zhoutong
 *
 */
public class ToolListHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_ToolList_Type";
			String[] toolListTypes = TcUtil.getPrefStringValues(prefName);
			if (toolListTypes == null || toolListTypes.length < 1) {
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
			if (!Utilities.contains(targetType, toolListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("ToolList.zhCN.Title"));
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", "ToolList");
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("ToolList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateToolList.Msg"));
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
				
				ToolListJob toolListJob = new ToolListJob(ReportMessages.getString("hint.Title"), targetBOP);
				toolListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						ToolListJob toolListJob = (ToolListJob) event.getJob();
						if (toolListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, toolListJob.getReportDataset());
						}
					}
				});
				toolListJob.setPriority(Job.INTERACTIVE);
				toolListJob.setUser(true);
				toolListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

	class ToolListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private String[] toolListTypes;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public ToolListJob(String name, TargetBOP targetBOP) 
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
				String prefName = "S4CUST_ToolList_ToolType";
				this.toolListTypes = TcUtil.getPrefStringValues(prefName);
				if (this.toolListTypes == null || this.toolListTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				LinkedHashMap<String, ToolList> toolListMap = new LinkedHashMap<String, ToolList>();
				traverseBOP(this.targetBOP.bopLine, toolListMap, this.targetBOP.languageSelection);
				
				File toolListFile = generateToolListFile(toolListMap);
				if (toolListFile.exists()) 
				{
					tempWorkingDir = toolListFile.getParent();
					
					// �����ĵ� ����/ �����嵥�������ݼ�����
					String objectName = ReportMessages.getString("ToolList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, toolListFile, objectName);
					if (this.reportDataset != null) {
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("ToolList.zhCN.Title"));
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", "ToolList");
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("ToolList.documentType"));
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
		 * ������ȡ����
		 * 
		 * @param paramBOPLine
		 * @param vector
		 * @throws TCException
		 */
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, ToolList> toolListMap, int languageSelection) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					if (itemRevision instanceof TCComponentMEOPRevision) {
						getTool(childBOPLine, toolListMap);
					}
					traverseBOP(childBOPLine, toolListMap, languageSelection);
				}
			}
		}
		
		/**
		 * ��ȡ�����µĹ���
		 * 
		 * @param opBopLine
		 * @param toolListMap
		 * @throws TCException
		 */
		private void getTool(TCComponentBOPLine opBopLine, LinkedHashMap<String, ToolList> toolListMap) throws TCException
		{
			AIFComponentContext[] contexts = opBopLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine toolBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = toolBOPLine.getItemRevision();
					if (itemRevision.isTypeOf(this.toolListTypes)) 
					{
						String itemId = itemRevision.getProperty("item_id");
						String stationAddress = TcUtil.getStationAddress(opBopLine); 
						String key = itemId + "," + stationAddress; // ����ID�͹�λ��ַ��ͬ�����ݣ��ϲ����
						ToolList toolList = toolListMap.get(key);
						if (toolList == null) {
							toolList = new ToolList(toolBOPLine, this.targetBOP.languageSelection, opBopLine, stationAddress);
						} else {
							toolList.quantity += TcUtil.getUsageQuantity(toolBOPLine);
							toolList.remark = TcUtil.getAppendRemark(toolList.remark, toolBOPLine);
						}
						toolListMap.put(key, toolList);
					}
				}
			}
		}
		
		/**
		 * ���ɹ����嵥����
		 * 
		 * @param toolListMap
		 * @return
		 * @throws Exception
		 */
		private File generateToolListFile(LinkedHashMap<String, ToolList> toolListMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("ToolList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("ToolList.Template.RowCount");
			if (rowCount < 1) {
				throw new Exception(ReportMessages.getString("InvalidRowCountConfigurationForTemplate.Msg", templateDatasetName));
			}
			
			ActiveXComponent excelApp = JacobUtil.openExcelApp();
			Dispatch workBook = null;
			
			try {
				workBook = JacobUtil.getWorkBook(excelApp, templateFile);
				Dispatch sheets = JacobUtil.getSheets(workBook);
				Dispatch sheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
				
				// ��������
				int dataSize = toolListMap.size();
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 5, dataSize - rowCount); // ����ģ�����������Ʋ���������
				}
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // ����
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // רҵ
				
				JacobUtil.writeCellData(sheet, "C2", variant);
				JacobUtil.writeCellData(sheet, "F2" , profession);
				JacobUtil.writeCellData(sheet, "F3", TcUtil.getCurrentDate()); // �������� 
				
				int index = 0;
				for (Entry<String, ToolList> entry : toolListMap.entrySet()) 
				{
					ToolList toolList = entry.getValue();
					JacobUtil.writeCellData(sheet, "A" + (index + 5), index + 1);
					JacobUtil.writeCellData(sheet, "B" + (index + 5), toolList.stationAddress);
					JacobUtil.writeCellData(sheet, "C" + (index + 5), toolList.toolName);
					JacobUtil.writeCellData(sheet, "D" + (index + 5), toolList.toolNumber);
					JacobUtil.writeCellData(sheet, "E" + (index + 5), toolList.quantity);
					JacobUtil.writeCellData(sheet, "F" + (index + 5), toolList.remark);
					
					index++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			// 2018-11-08, �����ļ�������
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("ToolList.enUS.Title"));
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