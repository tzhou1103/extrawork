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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.sokon.bopreport.customization.datamodels.StampingIHPartsBlankList;
import com.sokon.bopreport.customization.datamodels.StampingIHPartsBlankList.Part;
import com.sokon.bopreport.customization.datamodels.StampingIHPartsBlankList.SheetMetal;
import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.util.JacobUtil;
import com.sokon.bopreport.customization.util.ProcessReportDialog;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCCObject;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * 原材料清单
 * 
 * @author zhoutong
 *
 */
public class RawMaterialListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final Shell activeShell = HandlerUtil.getActiveShell(event);
		
		try {
			String prefName = "S4CUST_StampingBlankList_Type";
			String[] stampingBlankListTypes = TcUtil.getPrefStringValues(prefName);
			if (stampingBlankListTypes == null || stampingBlankListTypes.length < 1) {
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent == null || !(targetComponent.getType().equals("MECollaborationContext"))) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			TCComponentCCObject ccObject = (TCComponentCCObject) targetComponent;
			Vector<TCComponentItemRevision> bopRevisionVector = TcUtil.getBopRevisionsByCCObject(ccObject, stampingBlankListTypes);
			if (bopRevisionVector.size() == 0) {
				MessageBox.post(ReportMessages.getString("noSpecifiedBOP.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			ProcessReportDialog dialog = new ProcessReportDialog(activeShell);
			if (dialog.open() == Dialog.OK) 
			{
				int languageSelection = dialog.getLanguageSelection();
				TCComponentItem reportItem = dialog.getReportItem();
				TCComponentItemRevision reportRevision = null;
				if (reportItem != null) 
				{
					reportRevision = reportItem.getLatestItemRevision();
//					String documentType = reportRevision.getProperty("s4_AT_DocumentType");
					String documentType = reportRevision.getStringProperty("s4_AT_DocumentType");
//					if (!documentType.equals(ReportMessages.getString("StampingIHPartsBlankList.zhCN.Title"))) {
//					if (!documentType.equals("StampingIHPartsBlankList")) {
					if (!documentType.equals(ReportMessages.getString("StampingIHPartsBlankList.documentType"))) {
						MessageBox.post(ReportMessages.getString("noStampingIHPartsBlankListToUpdate.Msg"), ReportMessages.getString("hint.Title"), 2);
						return null;
					}
					
					if (TcUtil.isComponentReleased(reportRevision)) {
						reportRevision = reportRevision.saveAs(reportItem.getNewRev());
					}
				}
				
				if (languageSelection > -1) 
				{
					RawMaterialListJob rawMaterialListJob = new RawMaterialListJob(ReportMessages.getString("hint.Title"), bopRevisionVector, languageSelection, reportRevision);
					rawMaterialListJob.addJobChangeListener(new JobChangeAdapter()
					{
						@Override
						public void done(IJobChangeEvent event) 
						{
							RawMaterialListJob rawMaterialListJob = (RawMaterialListJob) event.getJob();
							if (rawMaterialListJob.isCompleted()) {
								TcUtil.openReportDataset(activeShell, rawMaterialListJob.getReportDataset());
							}
						}
					});
					rawMaterialListJob.setPriority(Job.INTERACTIVE);
					rawMaterialListJob.setUser(true);
					rawMaterialListJob.schedule();
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	class RawMaterialListJob extends Job 
	{
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		private Vector<TCComponentItemRevision> bopRevisionVector;
		private int languageSelection;
		private TCComponentItemRevision reportRevision;
		
		private String[] partTypes, blankTypes;

		public RawMaterialListJob(String name, Vector<TCComponentItemRevision> bopRevisions, int languageSelection, TCComponentItemRevision reportRevision) 
		{
			super(name);
			this.bopRevisionVector = bopRevisions;
			this.languageSelection = languageSelection;
			this.reportRevision = reportRevision;
		}

		@Override
		protected IStatus run(IProgressMonitor progressMonitor) 
		{
			progressMonitor.beginTask(ReportMessages.getString("workingAndWait.Msg"), -1);
			
			String tempWorkingDir = "";
			try {
				String prefName = "S4CUST_StampingBlankList_PartType";
				this.partTypes = TcUtil.getPrefStringValues(prefName);
				if (this.partTypes == null || this.partTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				prefName = "S4CUST_StampingBlankList_BlankType";
				this.blankTypes = TcUtil.getPrefStringValues(prefName);
				if (this.blankTypes == null || this.blankTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				String variant = this.bopRevisionVector.get(0).getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				Vector<StampingIHPartsBlankList> stampingIHPartsBlankListVector = getStampingIHPartsBlankList();
				File stampingIHPartsBlankListFile = generateStampingIHPartsBlankListFile(variant, stampingIHPartsBlankListVector);
				if (stampingIHPartsBlankListFile.exists()) 
				{
					tempWorkingDir = stampingIHPartsBlankListFile.getParent();
					
					// 工艺文档 名称/ 原材料清单数据集名称
					String objectName = ReportMessages.getString("StampingIHPartsBlankList.enUS.Title");
//					String documentType = ReportMessages.getString("StampingIHPartsBlankList.zhCN.Title");
//					String documentType = "StampingIHPartsBlankList";
					String documentType = ReportMessages.getString("StampingIHPartsBlankList.documentType");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(reportRevision, stampingIHPartsBlankListFile, objectName, documentType);
					if (this.reportDataset != null) {
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
		
		private Vector<StampingIHPartsBlankList> getStampingIHPartsBlankList() throws TCException
		{
			Vector<StampingIHPartsBlankList> stampingIHPartsBlankListVector = new Vector<StampingIHPartsBlankList>();
			for (TCComponentItemRevision bopRevision : this.bopRevisionVector) 
			{
				TCComponentBOPLine bopLine = TcUtil.getTopBopLine(bopRevision);
				Vector<Part> partVector = new Vector<Part>();
				Vector<SheetMetal> sheetMetalVector = new Vector<SheetMetal>();
				StampingIHPartsBlankList stampingIHPartsBlankList = new StampingIHPartsBlankList();
				
				traverseBOP(bopLine, partVector , sheetMetalVector, stampingIHPartsBlankList);
				
				stampingIHPartsBlankList.partVector = partVector;
				stampingIHPartsBlankList.sheetMetalVector = sheetMetalVector;
				stampingIHPartsBlankList.rowCount = partVector.size();
				stampingIHPartsBlankListVector.add(stampingIHPartsBlankList);
			}
			
			return stampingIHPartsBlankListVector;
		}
		
		private void traverseBOP(TCComponentBOPLine bopLine, Vector<Part> partVector, Vector<SheetMetal> sheetMetalVector, StampingIHPartsBlankList stampingIHPartsBlankList) throws TCException
		{
			AIFComponentContext[] contexts = bopLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					TCComponentBOPLine childBopLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBopLine.getItemRevision();
					if (childBopLine.getProperty("bl_occ_type").equals("METarget")) 
					{
						if (itemRevision.isTypeOf(this.partTypes)) {
							partVector.add(stampingIHPartsBlankList.new Part(childBopLine, this.languageSelection));
						} 
					}
					if (itemRevision.isTypeOf(this.blankTypes)) {
						sheetMetalVector.add(stampingIHPartsBlankList.new SheetMetal(childBopLine));
					}
					
					traverseBOP(childBopLine, partVector, sheetMetalVector, stampingIHPartsBlankList);
				}
			}
		}
		
		/**
		 * 生成原材料清单
		 * 
		 * @param variant
		 * @param stampingIHPartsBlankListVector 
		 * @return
		 * @throws Exception
		 */
		private File generateStampingIHPartsBlankListFile(String variant, Vector<StampingIHPartsBlankList> stampingIHPartsBlankListVector) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("StampingIHPartsBlankList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("StampingIHPartsBlankList.Template.RowCount");
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
				int dataSize = 0;
				for (StampingIHPartsBlankList stampingIHPartsBlankList : stampingIHPartsBlankListVector) {
					dataSize += stampingIHPartsBlankList.rowCount;
				}
				
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, rowCount, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				JacobUtil.writeCellData(sheet, "D1", variant); // 车型
				
				int startNo = 4;
				for (int i = 0; i < stampingIHPartsBlankListVector.size(); i++) {
					StampingIHPartsBlankList stampingIHPartsBlankList = stampingIHPartsBlankListVector.get(i);
					batchWriteCellData(startNo, stampingIHPartsBlankList, sheet, i + 1);
					startNo += stampingIHPartsBlankList.rowCount;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("StampingIHPartsBlankList.enUS.Title"));
			return reportFile;
		}
		
		
		/**
		 * 批量写入模具清单数据
		 * 
		 * @param startNo
		 * @param stampingIHPartsDieList
		 * @param sheet
		 * @param sequenceNo
		 * @throws Exception 
		 */
		private void batchWriteCellData(int startNo, StampingIHPartsBlankList stampingIHPartsBlankList, Dispatch sheet, int sequenceNo) throws Exception
		{
			if (stampingIHPartsBlankList.rowCount > 0) 
			{
				int rowNo = startNo + stampingIHPartsBlankList.rowCount - 1;
				
				int partSize = stampingIHPartsBlankList.partVector.size();
				int sheetMetalSize = stampingIHPartsBlankList.sheetMetalVector.size();
				
				if (stampingIHPartsBlankList.rowCount > 1) 
				{
					String mergeArea = "A" + startNo + ":" + "A" + rowNo;
					JacobUtil.mergeCell(sheet, mergeArea);
					JacobUtil.writeCellData(sheet, mergeArea, sequenceNo); // 序号
					
					mergeArea = "K" + startNo + ":" + "K" + rowNo;
					JacobUtil.mergeCell(sheet, mergeArea);
					double vehicleMaterialWeight = 0;
					if (sheetMetalSize == 1) {
						String materialWeight = stampingIHPartsBlankList.sheetMetalVector.get(0).materialWeight;
						vehicleMaterialWeight = Double.valueOf(materialWeight) * stampingIHPartsBlankList.partVector.get(0).vehicleUsage;
					} else if (sheetMetalSize == 2 && partSize == 2) {
						for (int i = 0; i < 2; i++) {
							String materialWeight = stampingIHPartsBlankList.sheetMetalVector.get(i).materialWeight;
							vehicleMaterialWeight += Double.valueOf(materialWeight) * stampingIHPartsBlankList.partVector.get(i).vehicleUsage;
						}
					}
					
					JacobUtil.writeCellData(sheet, mergeArea, vehicleMaterialWeight); // 单车材料重量
					
				} else {
					JacobUtil.writeCellData(sheet, "A" + rowNo, sequenceNo);
					if (partSize > 0 && sheetMetalSize > 0) {
						String materialWeight = stampingIHPartsBlankList.sheetMetalVector.get(0).materialWeight;
						double vehicleMaterialWeight = Double.valueOf(materialWeight) * stampingIHPartsBlankList.partVector.get(0).vehicleUsage;
						JacobUtil.writeCellData(sheet, "K" + rowNo, vehicleMaterialWeight);
					}
				}
				
				Vector<Part> partVector = stampingIHPartsBlankList.partVector;
				for (int i = 0; i < partSize; i++) 
				{
					Part part = partVector.get(i);
					JacobUtil.writeCellData(sheet, "B" + (startNo + i), part.partNumber);
					JacobUtil.writeCellData(sheet, "C" + (startNo + i), part.partName);
					JacobUtil.writeCellData(sheet, "E" + (startNo + i), part.vehicleUsage);
				}
				
				Vector<SheetMetal> sheetMetalVector = stampingIHPartsBlankList.sheetMetalVector;
				for (int i = 0; i < sheetMetalSize; i++) 
				{
					SheetMetal sheetMetal = sheetMetalVector.get(i);
					JacobUtil.writeCellData(sheet, "F" + (startNo + i), sheetMetal.material);
					JacobUtil.writeCellData(sheet, "G" + (startNo + i), sheetMetal.thickness);
					JacobUtil.writeCellData(sheet, "H" + (startNo + i), sheetMetal.length);
					JacobUtil.writeCellData(sheet, "I" + (startNo + i), sheetMetal.width);
					JacobUtil.writeCellData(sheet, "J" + (startNo + i), sheetMetal.materialWeight);
					JacobUtil.writeCellData(sheet, "L" + (startNo + i), sheetMetal.rollWidth);
					JacobUtil.writeCellData(sheet, "M" + (startNo + i), sheetMetal.remark);
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
