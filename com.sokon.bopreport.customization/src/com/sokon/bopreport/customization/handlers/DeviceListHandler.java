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
import com.sokon.bopreport.customization.datamodels.DeviceList;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.sokon.bopreport.customization.util.JacobUtil;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 设备清单
 * 
 * @author zhoutong
 *
 */
public class DeviceListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_DeviceList_Type";
			String[] deviceListTypes = TcUtil.getPrefStringValues(prefName);
			if (deviceListTypes == null || deviceListTypes.length < 1) {
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
			if (!Utilities.contains(targetType, deviceListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("DeviceList.zhCN.Title"));
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", "DeviceList");
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("DeviceList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateDeviceList.Msg"));
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
				
				DeviceListJob deviceListJob = new DeviceListJob(ReportMessages.getString("hint.Title"), targetBOP);
				deviceListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						DeviceListJob deviceListJob = (DeviceListJob) event.getJob();
						if (deviceListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, deviceListJob.getReportDataset());
						}
					}
				});
				deviceListJob.setPriority(Job.INTERACTIVE);
				deviceListJob.setUser(true);
				deviceListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

	class DeviceListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private String[] deviceListTypes;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public DeviceListJob(String name, TargetBOP targetBOP) 
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
				String prefName = "S4CUST_DeviceList_DeviceType";
				this.deviceListTypes = TcUtil.getPrefStringValues(prefName);
				if (this.deviceListTypes == null || this.deviceListTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				LinkedHashMap<String, DeviceList> deviceListMap = new LinkedHashMap<String, DeviceList>();
				traverseBOP(this.targetBOP.bopLine, deviceListMap, this.targetBOP.languageSelection);
				
				File deviceListFile = generateDeviceListFile(deviceListMap);
				if (deviceListFile.exists()) 
				{
					tempWorkingDir = deviceListFile.getParent();
					
					// 工艺文档 名称/ 设备清单报表数据集名称
					String objectName = ReportMessages.getString("DeviceList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, deviceListFile, objectName);
					if (this.reportDataset != null) {
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("DeviceList.zhCN.Title"));
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", "DeviceList");
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("DeviceList.documentType"));
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
		 * 遍历获取工厂工位
		 * 
		 * @param paramBOPLine
		 * @param vector
		 * @throws TCException
		 */
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, DeviceList> deviceListMap, int languageSelection) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					if (itemRevision.isTypeOf("S4_IT_StationRevision")) {
//					if (itemRevision instanceof TCComponentMEOPRevision) {
						getEquipment(childBOPLine, deviceListMap);
					}
					traverseBOP(childBOPLine, deviceListMap, languageSelection);
				}
			}
		}
		
		/**
		 * 获取工厂工位下的设备
		 * 
		 * @param stationBopLine
		 * @param deviceListMap
		 * @throws TCException
		 */
		private void getEquipment(TCComponentBOPLine stationBopLine, LinkedHashMap<String, DeviceList> deviceListMap) throws TCException
		{
//			TCComponentBOPLine stationBopLine = getStationBopLine(opBopLine);
			
//			AIFComponentContext[] contexts = opBopLine.getChildren();
			AIFComponentContext[] contexts = stationBopLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine equipmentBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = equipmentBOPLine.getItemRevision();
					if (itemRevision.isTypeOf(this.deviceListTypes)) 
					{
						String itemId = itemRevision.getProperty("item_id");
//						String lcationAddress = TcUtil.getLcationAddress(stationBopLine);
						// 修改工位地址取值，2018-11-08
						String lcationAddress = getLocationAddress(stationBopLine, equipmentBOPLine);
						String key = itemId + "," + lcationAddress; // 设备ID和工位地址相同的数据，合并输出
						
						DeviceList deviceList = deviceListMap.get(key);
						if (deviceList == null) {
							deviceList = new DeviceList(equipmentBOPLine, this.targetBOP.languageSelection, stationBopLine, lcationAddress);
						} else {
							deviceList.quantity += TcUtil.getUsageQuantity(equipmentBOPLine);
							deviceList.remark = TcUtil.getAppendRemark(deviceList.remark, equipmentBOPLine);
						}
						deviceListMap.put(key, deviceList);
					}
				}
			}
		}
		
		/**
		 * 获取工位地址
		 * 
		 * @param stationBopLine
		 * @return
		 * @throws TCException
		 */
		private String getLocationAddress(TCComponentBOPLine stationBopLine, TCComponentBOPLine equipmentBOPLine) throws TCException
		{
			String stationId = stationBopLine.getStringProperty("bl_item_item_id");
			String area = equipmentBOPLine.getStringProperty("S4_NT_Area");
			
			return stationId + area;
		}
		
		/**
		 * 生成设备清单
		 * 
		 * @param deviceListMap
		 * @return
		 * @throws Exception
		 */
		private File generateDeviceListFile(LinkedHashMap<String, DeviceList> deviceListMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("DeviceList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("DeviceList.Template.RowCount");
			if (rowCount < 1) {
				throw new Exception(ReportMessages.getString("InvalidRowCountConfigurationForTemplate.Msg", templateDatasetName));
			}
			
			// 拆分工位地址以L-R结尾的数据为两行, 除了工位地址，其他列数据一样
			Vector<DeviceList> deviceListVector = new Vector<DeviceList>();
			for (Entry<String, DeviceList> entry : deviceListMap.entrySet()) 
			{
				DeviceList deviceList = entry.getValue();
//				if (deviceList.lcationAddress.endsWith("L-R")) 
//				{
//					String lcationId = deviceList.lcationAddress.substring(0, deviceList.lcationAddress.lastIndexOf("-") - 1);
//					
//					DeviceList lDeviceList = (DeviceList) deviceList.clone();
//					lDeviceList.lcationAddress = lcationId + "L";
//					deviceListVector.add(lDeviceList);
//					
//					DeviceList rDeviceList = (DeviceList) deviceList.clone();
//					rDeviceList.lcationAddress = lcationId + "R";
//					deviceListVector.add(rDeviceList);
//				} else {
					deviceListVector.add(deviceList);
//				}
			}
			
			ActiveXComponent excelApp = JacobUtil.openExcelApp();
			Dispatch workBook = null;
			
			try {
				workBook = JacobUtil.getWorkBook(excelApp, templateFile);
				Dispatch sheets = JacobUtil.getSheets(workBook);
				Dispatch sheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
				
				// 计算行数
				int dataSize = deviceListVector.size();
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 4, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // 专业
				
				JacobUtil.writeCellData(sheet, "E2", variant);
				JacobUtil.writeCellData(sheet, "I2" , profession);
				
				for (int i = 0; i < dataSize; i++) 
				{
					DeviceList deviceList = deviceListVector.get(i);
					JacobUtil.writeCellData(sheet, "A" + (i + 4), i + 1);
					JacobUtil.writeCellData(sheet, "B" + (i + 4), deviceList.lcationName);
					JacobUtil.writeCellData(sheet, "E" + (i + 4), deviceList.lcationAddress);
					JacobUtil.writeCellData(sheet, "F" + (i + 4), deviceList.equipmentName);
					JacobUtil.writeCellData(sheet, "G" + (i + 4), deviceList.equipmentModels);
					JacobUtil.writeCellData(sheet, "H" + (i + 4), deviceList.quantity);
					JacobUtil.writeCellData(sheet, "I" + (i + 4), deviceList.remark);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("DeviceList.enUS.Title"));
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