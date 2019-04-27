package com.sokon.bopreport.customization.handlers;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Vector;
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
import com.sokon.bopreport.customization.datamodels.ElectrodeHolderList;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.util.JacobUtil;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 焊钳清单
 * 
 * @author zhoutong
 *
 */
public class ElectrodeHolderListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_ElectrodeholderList_Type";
			String[] electrodeHolderListTypes = TcUtil.getPrefStringValues(prefName);
			if (electrodeHolderListTypes == null || electrodeHolderListTypes.length < 1) {
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
			if (!Utilities.contains(targetType, electrodeHolderListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("ElectrodeHolderList.zhCN.Title"));
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", "ElectrodeHolderList");
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("ElectrodeHolderList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateElectrodeHolderList.Msg"));
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
				
				ElectrodeholderListJob electrodeholderListJob = new ElectrodeholderListJob(ReportMessages.getString("hint.Title"), targetBOP);
				electrodeholderListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						ElectrodeholderListJob electrodeholderListJob = (ElectrodeholderListJob) event.getJob();
						if (electrodeholderListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, electrodeholderListJob.getReportDataset());
						}
					}
				});
				electrodeholderListJob.setPriority(Job.INTERACTIVE);
				electrodeholderListJob.setUser(true);
				electrodeholderListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

	class ElectrodeholderListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private String[] electrodeHolderListTypes;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public ElectrodeholderListJob(String name, TargetBOP targetBOP) 
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
				String prefName = "S4CUST_ElectrodeholderList_ElectrodeholderType";
				this.electrodeHolderListTypes = TcUtil.getPrefStringValues(prefName);
				if (this.electrodeHolderListTypes == null || this.electrodeHolderListTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				LinkedHashMap<String, ElectrodeHolderList> electrodeHolderListMap = new LinkedHashMap<String, ElectrodeHolderList>();
				traverseBOP(this.targetBOP.bopLine, electrodeHolderListMap, this.targetBOP.languageSelection);
				
				File electrodeHolderListFile = generateElectrodeHolderListFile(electrodeHolderListMap);
				if (electrodeHolderListFile.exists()) 
				{
					tempWorkingDir = electrodeHolderListFile.getParent();
					
					// 工艺文档 名称/ 焊钳清单报表数据集名称
					String objectName = ReportMessages.getString("ElectrodeHolderList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, electrodeHolderListFile, objectName);
					if (this.reportDataset != null) {
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("ElectrodeHolderList.zhCN.Title"));
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", "ElectrodeHolderList");
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("ElectrodeHolderList.documentType"));
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
		 * 遍历获取焊钳
		 * 
		 * @param paramBOPLine
		 * @param vector
		 * @throws TCException
		 */
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, ElectrodeHolderList> electrodeHolderListMap, int languageSelection) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					if (itemRevision.isTypeOf(this.electrodeHolderListTypes)) 
					{
						String lcationAddress = "";
						TCComponentBOPLine stationBopLine = getStationBopLine(childBOPLine);
						if (stationBopLine != null) {
//							lcationAddress = TcUtil.getLcationAddress(stationBopLine);
							TCComponentBOPLine opBopLine = (TCComponentBOPLine) childBOPLine.parent();
							lcationAddress = getLcationAddress(stationBopLine, opBopLine);
						}
						
						String itemId = itemRevision.getProperty("item_id");
						String key = itemId + "," + lcationAddress;
						ElectrodeHolderList electrodeHolderList = electrodeHolderListMap.get(key);
						if (electrodeHolderList == null) {
							electrodeHolderList = new ElectrodeHolderList(childBOPLine, this.targetBOP.languageSelection, stationBopLine, lcationAddress);
						} else {
							electrodeHolderList.quantity += TcUtil.getUsageQuantity(childBOPLine);
							electrodeHolderList.remark = TcUtil.getAppendRemark(electrodeHolderList.remark, childBOPLine);
						}
						electrodeHolderListMap.put(key, electrodeHolderList);
					}
					traverseBOP(childBOPLine, electrodeHolderListMap, languageSelection);
				}
			}
		}
		
		/**
		 * 获取工厂工位
		 * 
		 * @param pinchWeldBopLine
		 * @return
		 * @throws TCException
		 */
		private TCComponentBOPLine getStationBopLine(TCComponentBOPLine pinchWeldBopLine) throws TCException
		{
			TCComponentBOPLine parentBopLine = (TCComponentBOPLine) pinchWeldBopLine.parent().parent();
			TCComponent[] relatedComponents = parentBopLine.getRelatedComponents("Mfg0assigned_workarea");
			if (relatedComponents != null && relatedComponents.length > 0) {
				return (TCComponentBOPLine) relatedComponents[0];
			}
			
			return null;
		}
		
		/**
		 * 获取工位地址
		 * 
		 * @param stationBopLine
		 * @return
		 * @throws TCException
		 */
		private String getLcationAddress(TCComponentBOPLine stationBopLine, TCComponentBOPLine opBopLine) throws TCException
		{
			String stationId = stationBopLine.getStringProperty("bl_item_item_id");
			String area = "0";
			TCComponent relatedComponent = opBopLine.getRelatedComponent("Mfg0processResource");
			if (relatedComponent != null && relatedComponent.getStringProperty("bl_item_object_type").equals("S4_IT_Worker")) 
			{
				String procResArea = relatedComponent.getStringProperty("s4_BAT_ProcResArea");
				area = procResArea.equals("") ? "0" : procResArea;
			}
			String lcationAddress = stationId + area;
			return lcationAddress;
		}
				
		/**
		 * 生成焊钳清单报表
		 * 
		 * @param electrodeHolderListMap
		 * @return
		 * @throws Exception
		 */
		private File generateElectrodeHolderListFile(LinkedHashMap<String, ElectrodeHolderList> electrodeHolderListMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("ElectrodeHolderList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("ElectrodeHolderList.Template.RowCount");
			if (rowCount < 1) {
				throw new Exception(ReportMessages.getString("InvalidRowCountConfigurationForTemplate.Msg", templateDatasetName));
			}
			
			// 拆分工位地址以L-R结尾的数据为两行, 除了工位地址，其他列数据一样
			Vector<ElectrodeHolderList> electrodeHolderListVector = new Vector<ElectrodeHolderList>();
			for (Entry<String, ElectrodeHolderList> entry : electrodeHolderListMap.entrySet()) 
			{
				ElectrodeHolderList electrodeHolderList = entry.getValue();
				if (electrodeHolderList.lcationAddress.endsWith("L-R")) 
				{
					String lcationId = electrodeHolderList.lcationAddress.substring(0, electrodeHolderList.lcationAddress.lastIndexOf("-") - 1);
					ElectrodeHolderList lElectrodeHolderList = (ElectrodeHolderList) electrodeHolderList.clone();
					lElectrodeHolderList.lcationAddress = lcationId + "L";
					electrodeHolderListVector.add(lElectrodeHolderList);
					
					ElectrodeHolderList rElectrodeHolderList = (ElectrodeHolderList) electrodeHolderList.clone();
					rElectrodeHolderList.lcationAddress = lcationId + "R";
					electrodeHolderListVector.add(rElectrodeHolderList);
				} else {
					electrodeHolderListVector.add(electrodeHolderList);
				}
			}
			
			ActiveXComponent excelApp = JacobUtil.openExcelApp();
			Dispatch workBook = null;
			
			try {
				workBook = JacobUtil.getWorkBook(excelApp, templateFile);
				Dispatch sheets = JacobUtil.getSheets(workBook);
				Dispatch sheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
				
				// 计算行数
				int dataSize = electrodeHolderListVector.size();
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 4, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // 专业
				
				JacobUtil.writeCellData(sheet, "E2", variant);
				JacobUtil.writeCellData(sheet, "I2" , profession);
				
				for (int i = 0; i < dataSize; i++) 
				{
					ElectrodeHolderList electrodeHolderList = electrodeHolderListVector.get(i);
					JacobUtil.writeCellData(sheet, "A" + (i + 4), i + 1);
					JacobUtil.writeCellData(sheet, "B" + (i + 4), electrodeHolderList.lcationName);
					JacobUtil.writeCellData(sheet, "E" + (i + 4), electrodeHolderList.lcationAddress);
					JacobUtil.writeCellData(sheet, "F" + (i + 4), electrodeHolderList.electrodeHolderNumber);
					JacobUtil.writeCellData(sheet, "G" + (i + 4), electrodeHolderList.electrodeHolderModels);
					JacobUtil.writeCellData(sheet, "H" + (i + 4), electrodeHolderList.quantity);
					JacobUtil.writeCellData(sheet, "I" + (i + 4), electrodeHolderList.remark);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("ElectrodeHolderList.enUS.Title"));
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