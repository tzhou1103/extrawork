package com.sokon.bopreport.customization.handlers;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.datamodels.StationBOM;
import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.util.JacobUtil;
import com.sokon.bopreport.customization.util.SelectLanguageDialog;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 工位物料清单
 * 
 * @author 
 *
 */
public class StationBOMHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_StationBOM_Type";
			String[] stationBOMTypes = TcUtil.getPrefStringValues(prefName);
			if (stationBOMTypes == null || stationBOMTypes.length < 1) {
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
			if (!Utilities.contains(targetType, stationBOMTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("StationBOM.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateStationBOM.Msg"));
				if (!confirm) {
					return null;
				}
			}

			TCComponentBOMWindow mbomWinbow = TcUtil.getMBOMWindow(targetBOPLine);
			if (mbomWinbow == null) {
				MessageBox.post(ReportMessages.getString("mbomWindowNotOpened.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(shell, TcUtil.getVariantModels(mbomWinbow));
			if (selectLanguageDialog.open() == Dialog.OK) 
			{
				int languageSelection = selectLanguageDialog.getLanguageSelection();
				TCComponent variantModel = selectLanguageDialog.getVariantModel();
				
				TargetBOP targetBOP = new TargetBOP(targetBOPLine, targetRevision, targetType);
				targetBOP.setLanguageSelection(languageSelection);
				targetBOP.setDocumentRevision(documentRevision);
				targetBOP.setVariantModel(variantModel);
				
				StationBOMJob stationBOMJob = new StationBOMJob(ReportMessages.getString("hint.Title"), targetBOP);
				stationBOMJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						StationBOMJob stationBOMJob = (StationBOMJob) event.getJob();
						if (stationBOMJob.isCompleted()) {
							TcUtil.openReportDataset(shell, stationBOMJob.getReportDataset());
						}
					}
				});
				stationBOMJob.setPriority(Job.INTERACTIVE);
				stationBOMJob.setUser(true);
				stationBOMJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

	class StationBOMJob extends Job
	{
		private TargetBOP targetBOP;
		
		private String[] stationBOMPartTypes;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		// 存储工位工艺对应的工厂工位，added by zhoutong, 2019-01-25
		private Map<TCComponentBOPLine, TCComponentBOPLine> workAreaBopMap = new HashMap<TCComponentBOPLine, TCComponentBOPLine>();
		
		// 存储工厂工位的属性信息，added by zhoutong, 2019-01-25
		private Map<TCComponentBOPLine, String[]> workAreaInfoMap = new HashMap<TCComponentBOPLine, String[]>();
		
		public StationBOMJob(String name, TargetBOP targetBOP) 
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
				String prefName = "S4CUST_StationBOM_PartType";
				this.stationBOMPartTypes = TcUtil.getPrefStringValues(prefName);
				if (this.stationBOMPartTypes == null || this.stationBOMPartTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				LinkedHashMap<String, Vector<StationBOM>> stationBOMMap = new LinkedHashMap<String, Vector<StationBOM>>();
				this.targetBOP.bopLine.window().refresh(); // added by zhoutong, 2019-02-16
				traverseBOP(this.targetBOP.bopLine, stationBOMMap, this.targetBOP.languageSelection);
				
				File stationBOMFile = generateStationBOMFile(stationBOMMap);
				if (stationBOMFile.exists()) 
				{
					tempWorkingDir = stationBOMFile.getParent();
					
					// 工艺文档 名称/ 工位物料清单数据集名称
					String objectName = ReportMessages.getString("StationBOM.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, stationBOMFile, objectName);
					if (this.reportDataset != null) {
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("StationBOM.documentType"));
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
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, Vector<StationBOM>> stationBOMMap, int languageSelection) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					if (itemRevision instanceof TCComponentMEOPRevision) {
						getStationBOM(childBOPLine, stationBOMMap);
					}
					
//					traverseBOP(childBOPLine, stationBOMMap, languageSelection);
					// 消耗件或背景件不再向下遍历，modified by zhoutong, 2019-02-16
					String occType = childBOPLine.getProperty("bl_occ_type");
					if (!Utilities.contains(occType, new String[] { "MEConsumed", "S4_MEBackground" }))
					{
						traverseBOP(childBOPLine, stationBOMMap, languageSelection);
					}
				}
			}
		}
		
		/**
		 * 获取工序下的物料
		 * 
		 * @param opBopLine
		 * @param stationBOMMap
		 * @throws TCException
		 */
		private void getStationBOM(TCComponentBOPLine opBopLine, LinkedHashMap<String, Vector<StationBOM>> stationBOMMap) throws TCException
		{
			TCComponentBOPLine workAreaBopLine = getWorkAreaBopLine(opBopLine);
			String station = "";
			String locationAddress = "";
			if (workAreaBopLine != null) 
			{
				String[] workAreaInfos = this.workAreaInfoMap.get(workAreaBopLine);
				if (workAreaInfos != null && workAreaInfos.length == 2) {
					station = workAreaInfos[0];
					locationAddress = workAreaInfos[1];
				} 
			}
			
			// 2018-11-08, 修改工位地址取值
			String tempLocationAddress = locationAddress + TcUtil.getProcResArea(opBopLine);
			locationAddress = TcUtil.getLast7String(tempLocationAddress);
			
			String key = station + "," + locationAddress;
			
			AIFComponentContext[] contexts = opBopLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine partBopLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = partBopLine.getItemRevision();
					if (itemRevision.isTypeOf(this.stationBOMPartTypes) && partBopLine.getProperty("bl_occ_type").equals("MEConsumed")) 
					{
						// 2018-11-08, 过滤掉对象类型为S4_IT_PartRevision且其s4_AT_IsColorPart属性值为“COL/SURF”（LOV真实值）的对象
						if (itemRevision.isTypeOf("S4_IT_PartRevision") && itemRevision.getStringProperty("s4_AT_IsColorPart").equals("COL/SURF")) {
							continue;
						}
						
						StationBOM stationBOM = new StationBOM(partBopLine, station, locationAddress, this.targetBOP.languageSelection);
						Vector<StationBOM> vector = stationBOMMap.get(key);
						if (vector == null) {
							vector = new Vector<StationBOM>();
							vector.add(stationBOM);
						} else {
							int index = vector.indexOf(stationBOM);
							if(vector.indexOf(stationBOM) != -1) 
							{
								stationBOM = vector.get(index);
								stationBOM.quantity += TcUtil.getUsageQuantity(partBopLine);
//								stationBOM.remark = TcUtil.getAppendRemark(stationBOM.remark, partBopLine);
							} else {
								vector.add(stationBOM);
							}
						}
						
						stationBOMMap.put(key, vector);
					}
				}
			}
		}
		
		/**
		 * 获取工厂工位BOP行
		 * 
		 * @param opBopLine
		 * @return
		 * @throws TCException
		 */
		private TCComponentBOPLine getWorkAreaBopLine(TCComponentBOPLine opBopLine) throws TCException
		{
			TCComponentBOPLine stationBopLine = (TCComponentBOPLine) opBopLine.parent();
			TCComponentBOPLine workAreaBopLine = this.workAreaBopMap.get(stationBopLine);
			if (workAreaBopLine != null) {
				return workAreaBopLine;
			}
			
			// 修改工厂工位获取方式，modified by zhoutong, 2019-01-26
			TCComponent[] relatedComponents = stationBopLine.getRelatedComponents("Mfg0assigned_workarea");
			if (relatedComponents != null && relatedComponents.length > 0) 
			{
				workAreaBopLine = (TCComponentBOPLine) relatedComponents[0];
				this.workAreaBopMap.put(stationBopLine, workAreaBopLine);
				
				String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "bl_item_item_id" };
				String[] propValues = workAreaBopLine.getProperties(propNames);
				if (propValues != null && propValues.length == 3) {
					String station =  TcUtil.getValueByLanguageSelection(this.targetBOP.languageSelection, propValues[0], propValues[1]);
					String locationAddress = propValues[2];
					
					String[] workAreaInfos = new String[] { station, locationAddress };
					this.workAreaInfoMap.put(workAreaBopLine, workAreaInfos);
				}
			}
			
			return workAreaBopLine;
		}
		
		/**
		 * 生成工位物料清单
		 * 
		 * @param stationBOMMap
		 * @return
		 * @throws Exception
		 */
		private File generateStationBOMFile(LinkedHashMap<String, Vector<StationBOM>> stationBOMMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("StationBOM.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("StationBOM.Template.RowCount");
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
				
				JacobUtil.writeCellData(sheet, "C2", variant);
				JacobUtil.writeCellData(sheet, "G2" , profession);
				
				// 2018-11-29,  修改配置取值
				JacobUtil.writeCellData(sheet, "F3" , TcUtil.getConfiguration(this.targetBOP)); // 配置
				
				JacobUtil.writeCellData(sheet, "I3", TcUtil.getCurrentDate()); // 编制日期 
				
				// 计算行数
				int dataSize = 0;
				String[] keys = stationBOMMap.keySet().toArray(new String[stationBOMMap.size()]);
				for (String key : keys) {
					Vector<StationBOM> vector = stationBOMMap.get(key);
					dataSize += vector.size();
				}
				
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 5, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				int startNo = 5;
				for (int i = 0; i < keys.length; i++) {
					Vector<StationBOM> vector = stationBOMMap.get(keys[i]);
					batchWriteCellData(vector, startNo, sheet, keys[i], i + 1);
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
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("StationBOM.enUS.Title"));
			return reportFile;
		}
		
		/**
		 * 批量写入工位物料清单数据
		 * 
		 * @param vector
		 * @param startNo	开始写入数据的行号
		 * @param sheet
		 * @param key 工位,工位地址
		 * @param sequenceNo
		 * @throws Exception
		 */
		private void batchWriteCellData(Vector<StationBOM> vector, int startNo, Dispatch sheet, String key, int sequenceNo) throws Exception
		{
			int count = vector.size();
			for (int i = 0; i < count; i++) 
			{
				StationBOM stationBOM = vector.get(i);
				
				int startIndex = i + startNo;
				
				JacobUtil.writeCellData(sheet, "D" + startIndex, stationBOM.partName);
				JacobUtil.writeCellData(sheet, "G" + startIndex, stationBOM.partNumber);
				JacobUtil.writeCellData(sheet, "H" + startIndex, stationBOM.quantity);
//				JacobUtil.writeCellData(sheet, "I" + startIndex, stationBOM.remark);
			}
			
			String[] splitStrs = key.split(",", -1);
			if (splitStrs != null && splitStrs.length == 2) 
			{
				// 合并相同工位单元格
				if (count > 1) {
					int rowNo = count + startNo - 1;
					
					String mergeArea = "A" + startNo + ":" + "A" + rowNo;
					JacobUtil.mergeCell(sheet, mergeArea);
					JacobUtil.writeCellData(sheet, mergeArea, sequenceNo);
					
					mergeArea = "B" + startNo + ":" + "B" + rowNo;
					JacobUtil.mergeCell(sheet, mergeArea);
					JacobUtil.writeCellData(sheet, mergeArea, splitStrs[0]);
					
					mergeArea = "C" + startNo + ":" + "C" + rowNo;
					JacobUtil.mergeCell(sheet, mergeArea);
					JacobUtil.writeCellData(sheet, mergeArea, splitStrs[1]);
				} else if (count == 1) {
					JacobUtil.writeCellData(sheet, "A" + startNo, sequenceNo);
					JacobUtil.writeCellData(sheet, "B" + startNo, splitStrs[0]);
					JacobUtil.writeCellData(sheet, "C" + startNo, splitStrs[1]);
				}
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