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
import com.sokon.bopreport.customization.datamodels.StampingIHPartsDieList;
import com.sokon.bopreport.customization.datamodels.StampingIHPartsDieList.ProductInfo;
import com.sokon.bopreport.customization.datamodels.StampingIHPartsDieList.ToolInfo;
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
 * 模具清单
 * 
 * @author 
 *
 */
public class MouldListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{		
		final Shell activeShell = HandlerUtil.getActiveShell(event);
		
		try {
			String prefName = "S4CUST_StampingDieList_Type";
			String[] stampingDieListTypes = TcUtil.getPrefStringValues(prefName);
			if (stampingDieListTypes == null || stampingDieListTypes.length < 1) {
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent == null || !(targetComponent.getType().equals("MECollaborationContext"))) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			TCComponentCCObject ccObject = (TCComponentCCObject) targetComponent;
			Vector<TCComponentItemRevision> bopRevisionVector = TcUtil.getBopRevisionsByCCObject(ccObject, stampingDieListTypes);
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
//					if (!documentType.equals(ReportMessages.getString("StampingIHPartsDieList.zhCN.Title"))) {
//					if (!documentType.equals("StampingIHPartsDieList")) {
					if (!documentType.equals(ReportMessages.getString("StampingIHPartsDieList.documentType"))) {
						MessageBox.post(ReportMessages.getString("noStampingIHPartsDieListToUpdate.Msg"), ReportMessages.getString("hint.Title"), 2);
						return null;
					}
				}
				
				if (languageSelection > -1) 
				{
					MouldListJob mouldListJob = new MouldListJob(ReportMessages.getString("hint.Title"), bopRevisionVector, languageSelection, reportRevision);
					mouldListJob.addJobChangeListener(new JobChangeAdapter()
					{
						@Override
						public void done(IJobChangeEvent event) 
						{
							MouldListJob mouldListJob = (MouldListJob) event.getJob();
							if (mouldListJob.isCompleted()) {
								TcUtil.openReportDataset(activeShell, mouldListJob.getReportDataset());
							}
						}
					});
					mouldListJob.setPriority(Job.INTERACTIVE);
					mouldListJob.setUser(true);
					mouldListJob.schedule();
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	class MouldListJob extends Job 
	{
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		private Vector<TCComponentItemRevision> bopRevisionVector;
		private int languageSelection;
		private TCComponentItemRevision reportRevision;
		
		private String[] partTypes, dieTypes;

		public MouldListJob(String name, Vector<TCComponentItemRevision> bopRevisions, int languageSelection, TCComponentItemRevision reportRevision) 
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
				String prefName = "S4CUST_StampingDieList_PartType";
				this.partTypes = TcUtil.getPrefStringValues(prefName);
				if (this.partTypes == null || this.partTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				prefName = "S4CUST_StampingDieList_DieType";
				this.dieTypes = TcUtil.getPrefStringValues(prefName);
				if (this.dieTypes == null || this.dieTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				String variant = this.bopRevisionVector.get(0).getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				Vector<StampingIHPartsDieList> stampingIHPartsDieListVector = getStampingIHPartsDieList();
				
				File stampingIHPartsDieListFile = generateStampingIHPartsDieListFile(variant, stampingIHPartsDieListVector);
				if (stampingIHPartsDieListFile.exists()) 
				{
					tempWorkingDir = stampingIHPartsDieListFile.getParent();
					
					// 工艺文档 名称/ 模具清单数据集名称
					String objectName = ReportMessages.getString("StampingIHPartsDieList.enUS.Title");
//					String documentType = ReportMessages.getString("StampingIHPartsDieList.zhCN.Title");
//					String documentType = "StampingIHPartsDieList";
					String documentType = ReportMessages.getString("StampingIHPartsDieList.documentType");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(reportRevision, stampingIHPartsDieListFile, objectName, documentType);
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
		
		private Vector<StampingIHPartsDieList> getStampingIHPartsDieList() throws TCException
		{
			Vector<StampingIHPartsDieList> stampingIHPartsDieListVector = new Vector<StampingIHPartsDieList>();
			for (int i = 0; i < this.bopRevisionVector.size(); i++) 
			{
				TCComponentItemRevision bopRevision = this.bopRevisionVector.get(i);
				TCComponentBOPLine bopLine = TcUtil.getTopBopLine(bopRevision);
				Vector<ProductInfo> productInfoVector = new Vector<ProductInfo>();
				Vector<ToolInfo> toolInfoVector = new Vector<ToolInfo>();
				StampingIHPartsDieList stampingIHPartsDieList = new StampingIHPartsDieList(bopLine);
				traverseBOP(bopLine, productInfoVector, toolInfoVector, stampingIHPartsDieList);
				
				stampingIHPartsDieList.productInfoVector = productInfoVector;
				stampingIHPartsDieList.toolInfoVector = toolInfoVector;
//				stampingIHPartsDieList.quantity = productInfoVector.size();
				stampingIHPartsDieList.quantity = toolInfoVector.size();
				stampingIHPartsDieList.rowCount = getRowCount(stampingIHPartsDieList);
				
				stampingIHPartsDieListVector.add(stampingIHPartsDieList);
			}
			
			return stampingIHPartsDieListVector;
		}
		
		private void traverseBOP(TCComponentBOPLine bopLine, Vector<ProductInfo> productInfoVector, Vector<ToolInfo> toolInfoVector, StampingIHPartsDieList stampingIHPartsDieList) throws TCException
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
							productInfoVector.add(stampingIHPartsDieList.new ProductInfo(childBopLine, this.languageSelection));
						} 
					}
					
					if (itemRevision.isTypeOf(this.dieTypes)) {
						toolInfoVector.add(stampingIHPartsDieList.new ToolInfo(childBopLine, this.languageSelection));
					}
					
					traverseBOP(childBopLine, productInfoVector, toolInfoVector, stampingIHPartsDieList);
				}
			}
		}

		private int getRowCount(StampingIHPartsDieList stampingIHPartsDieList)
		{
			int productInfoCount = stampingIHPartsDieList.productInfoVector.size();
			int toolInfoCount = stampingIHPartsDieList.toolInfoVector.size();
			if (productInfoCount > toolInfoCount) {
				return productInfoCount;
			} else if (productInfoCount < toolInfoCount) {
				return toolInfoCount;
			} else if (productInfoCount == toolInfoCount && productInfoCount > 0) {
				return productInfoCount;
			}
			return 1;
		}
		
		/**
		 * 生成模具清单
		 * 
		 * @param stationBOMMap
		 * @return
		 * @throws Exception
		 */
		private File generateStampingIHPartsDieListFile(String variant, Vector<StampingIHPartsDieList> stampingIHPartsDieListVector) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("StampingIHPartsDieList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("StampingIHPartsDieList.Template.RowCount");
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
				for (StampingIHPartsDieList stampingIHPartsDieList : stampingIHPartsDieListVector) {
					dataSize += stampingIHPartsDieList.rowCount;
				}
				
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, rowCount, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				JacobUtil.writeCellData(sheet, "D1", variant); // 车型
				
				int startNo = 5;
				for (int i = 0; i < stampingIHPartsDieListVector.size(); i++) {
					StampingIHPartsDieList stampingIHPartsDieList = stampingIHPartsDieListVector.get(i);
					batchWriteCellData(startNo, stampingIHPartsDieList, sheet, i + 1);
					startNo += stampingIHPartsDieList.rowCount;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
//			return templateFile;
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("StampingIHPartsDieList.enUS.Title"));
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
		private void batchWriteCellData(int startNo, StampingIHPartsDieList stampingIHPartsDieList, Dispatch sheet, int sequenceNo) throws Exception
		{
			int rowNo = startNo + stampingIHPartsDieList.rowCount - 1;
			
			if (stampingIHPartsDieList.rowCount > 1) {
				String mergeArea = "A" + startNo + ":" + "A" + rowNo;
				JacobUtil.mergeCell(sheet, mergeArea);
				JacobUtil.writeCellData(sheet, mergeArea, sequenceNo); // 序号
				
				mergeArea = "I" + startNo + ":" + "I" + rowNo;
				JacobUtil.mergeCell(sheet, mergeArea);
				JacobUtil.writeCellData(sheet, mergeArea, stampingIHPartsDieList.quantity); // 模具数量
				
				mergeArea = "J" + startNo + ":" + "J" + rowNo;
				JacobUtil.mergeCell(sheet, mergeArea);
				JacobUtil.writeCellData(sheet, mergeArea, stampingIHPartsDieList.method); // 生产方式
				
			} else if (stampingIHPartsDieList.rowCount == 1){
				JacobUtil.writeCellData(sheet, "A" + rowNo, sequenceNo);
				JacobUtil.writeCellData(sheet, "I" + rowNo, stampingIHPartsDieList.quantity);
				JacobUtil.writeCellData(sheet, "J" + rowNo, stampingIHPartsDieList.method);
			}
			
			Vector<ProductInfo> productInfoVector = stampingIHPartsDieList.productInfoVector;
			for (int i = 0; i < productInfoVector.size(); i++) 
			{
				ProductInfo productInfo = productInfoVector.get(i);
				JacobUtil.writeCellData(sheet, "B" + (startNo + i), productInfo.partNumber);
				JacobUtil.writeCellData(sheet, "C" + (startNo + i), productInfo.partName);
				JacobUtil.writeCellData(sheet, "D" + (startNo + i), productInfo.vehicleUsage);
				JacobUtil.writeCellData(sheet, "E" + (startNo + i), productInfo.material);
				JacobUtil.writeCellData(sheet, "F" + (startNo + i), productInfo.thickness);
				JacobUtil.writeCellData(sheet, "G" + (startNo + i), productInfo.weight);
				JacobUtil.writeCellData(sheet, "H" + (startNo + i), productInfo.partSize);
			}
			
			// 2018-10-15, 增加外侧框线
			String rangeName = "";
			if (stampingIHPartsDieList.rowCount > 1) {
				rangeName = "A" + startNo + ":" + "H" + rowNo;
			} else if (stampingIHPartsDieList.rowCount == 1) {
				rangeName = "A" + rowNo + ":" + "H" + rowNo;
			}
			if (!rangeName.equals("")) {
				JacobUtil.setOutBorders(sheet, rangeName);
			}
			
			Vector<ToolInfo> toolInfoVector = stampingIHPartsDieList.toolInfoVector;
			for (int i = 0; i < toolInfoVector.size(); i++) 
			{
				ToolInfo productInfo = toolInfoVector.get(i);
				JacobUtil.writeCellData(sheet, "K" + (startNo + i), productInfo.station);
				JacobUtil.writeCellData(sheet, "L" + (startNo + i), productInfo.stationName);
				JacobUtil.writeCellData(sheet, "M" + (startNo + i), productInfo.moldSize);
				JacobUtil.writeCellData(sheet, "N" + (startNo + i), productInfo.moldWeight);
				JacobUtil.writeCellData(sheet, "O" + (startNo + i), productInfo.remark);
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
