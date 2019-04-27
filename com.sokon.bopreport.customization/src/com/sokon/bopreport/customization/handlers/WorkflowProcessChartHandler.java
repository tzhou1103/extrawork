package com.sokon.bopreport.customization.handlers;

import java.io.File;
import java.util.LinkedHashMap;
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
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.datamodels.WorkflowProcessChart;
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
 * 工艺过程流程图
 * 
 * @author zhoutong
 *
 */
public class WorkflowProcessChartHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try {
			String prefName = "S4CUST_WorkflowProcessChart_Type";
			String[] workflowProcessChartTypes = TcUtil.getPrefStringValues(prefName);
			if (workflowProcessChartTypes == null || workflowProcessChartTypes.length < 1) {
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
			if (!Utilities.contains(targetType, workflowProcessChartTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("WorkflowProcessChart.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateWorkflowProcessChart.Msg"));
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
				
				WorkflowProcessChartJob workflowProcessChartJob = new WorkflowProcessChartJob(ReportMessages.getString("hint.Title"), targetBOP);
				workflowProcessChartJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						WorkflowProcessChartJob workflowProcessChartJob = (WorkflowProcessChartJob) event.getJob();
						if (workflowProcessChartJob.isCompleted()) {
							TcUtil.openReportDataset(shell, workflowProcessChartJob.getReportDataset());
						}
					}
				});
				workflowProcessChartJob.setPriority(Job.INTERACTIVE);
				workflowProcessChartJob.setUser(true);
				workflowProcessChartJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

	class WorkflowProcessChartJob extends Job
	{
		private TargetBOP targetBOP;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public WorkflowProcessChartJob(String name, TargetBOP targetBOP) 
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
				LinkedHashMap<String, Vector<WorkflowProcessChart>> workflowProcessChartMap = new LinkedHashMap<String, Vector<WorkflowProcessChart>>();
				traverseBOP(this.targetBOP.bopLine, workflowProcessChartMap, this.targetBOP.languageSelection);
				
				File workflowProcessChartFile = generateWorkflowProcessChartFile(workflowProcessChartMap);
				if (workflowProcessChartFile.exists()) 
				{
					tempWorkingDir = workflowProcessChartFile.getParent();
					
					// 工艺文档 名称/ 工艺过程流程图报表数据集名称
					String objectName = ReportMessages.getString("WorkflowProcessChart.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, workflowProcessChartFile, objectName);
					if (this.reportDataset != null) {
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("WorkflowProcessChart.documentType"));
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
		 * 遍历获取工序
		 * 
		 * @param paramBOPLine
		 * @param vector
		 * @throws TCException
		 */
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, Vector<WorkflowProcessChart>> workflowProcessChartMap, int languageSelection) throws TCException
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
						WorkflowProcessChart workflowProcessChart = new WorkflowProcessChart(childBOPLine, languageSelection);
						String station = workflowProcessChart.station; // 根据工位分组
						Vector<WorkflowProcessChart> vector = workflowProcessChartMap.get(station);
						if (vector == null) {
							vector = new Vector<WorkflowProcessChart>();
						}
						vector.add(workflowProcessChart);
						workflowProcessChartMap.put(station, vector);
					}
					
//					traverseBOP(childBOPLine, workflowProcessChartMap, languageSelection);
					// 消耗件或背景件不再向下遍历，modified by zhoutong, 2019-02-16
					String occType = childBOPLine.getProperty("bl_occ_type");
					if (!Utilities.contains(occType, new String[] { "MEConsumed", "S4_MEBackground" }))
					{
						traverseBOP(childBOPLine, workflowProcessChartMap, languageSelection);
					}
				}
			}
		}

		/**
		 * 生成工艺过程流程图报表
		 * 
		 * @param workflowProcessChartMap
		 * @return
		 * @throws Exception
		 */
		private File generateWorkflowProcessChartFile(LinkedHashMap<String, Vector<WorkflowProcessChart>> workflowProcessChartMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("WorkflowProcessChart.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("WorkflowProcessChart.Template.RowCount");
			if (rowCount < 1) {
				throw new Exception(ReportMessages.getString("InvalidRowCountConfigurationForTemplate.Msg", templateDatasetName));
			}
			
			ActiveXComponent excelApp = JacobUtil.openExcelApp();
			Dispatch workBook = null;
			
			try {
				workBook = JacobUtil.getWorkBook(excelApp, templateFile);
				Dispatch sheets = JacobUtil.getSheets(workBook);
				Dispatch sheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // 专业
				
				JacobUtil.writeCellData(sheet, "D2", variant);
				JacobUtil.writeCellData(sheet, "B4" , profession);
				JacobUtil.writeCellData(sheet, "D3", TcUtil.getCurrentDate()); // 编制日期 
				
				// 计算行数
				int dataSize = 0;
				String[] stations = workflowProcessChartMap.keySet().toArray(new String[workflowProcessChartMap.size()]);
				for (String station : stations) {
					Vector<WorkflowProcessChart> vector = workflowProcessChartMap.get(station);
					dataSize += vector.size();
				}
				
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 6, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				int startNo = 6;
				for (String station : stations) {
					Vector<WorkflowProcessChart> vector = workflowProcessChartMap.get(station);
					batchWriteCellData(vector, startNo, sheet, station);
					startNo += vector.size();
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("WorkflowProcessChart.enUS.Title"));
			return reportFile;
		}
		
		/**
		 * 批量写入工艺过程流程图数据
		 * 
		 * @param vector
		 * @param startNo	开始写入数据的行号
		 * @param sheet
		 * @param station
		 * @throws Exception
		 */
		private void batchWriteCellData(Vector<WorkflowProcessChart> vector, int startNo, Dispatch sheet, String station) throws Exception
		{
			int count = vector.size();
			for (int i = 0; i < count; i++) 
			{
				WorkflowProcessChart workflowProcessChart = vector.get(i);
				
				int rowNo = i + startNo; // 行号
				
				JacobUtil.writeCellData(sheet, "B" + rowNo, workflowProcessChart.step);
				JacobUtil.writeCellData(sheet, "C" + rowNo, workflowProcessChart.operator);
				JacobUtil.writeCellData(sheet, "D" + rowNo, workflowProcessChart.timeManagement);
				JacobUtil.writeCellData(sheet, "E" + rowNo, workflowProcessChart.remark);
			}
			
			// 合并相同工位单元格
			if (count > 1) {
				String mergeArea = "A" + startNo + ":" + "A" + (count + startNo - 1);
				JacobUtil.mergeCell(sheet, mergeArea);
				JacobUtil.writeCellData(sheet, mergeArea, station);
			} else if (count == 1) {
				JacobUtil.writeCellData(sheet, "A" + startNo, station);
			}
		}
		
		public boolean isCompleted() {
			return completed;
		}

		public TCComponentDataset getReportDataset() {
			return reportDataset;
		}
		
	}
}
