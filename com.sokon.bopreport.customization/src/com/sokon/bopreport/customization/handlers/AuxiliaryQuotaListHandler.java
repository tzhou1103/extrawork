package com.sokon.bopreport.customization.handlers;

import java.io.File;
import java.util.LinkedHashMap;

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
import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.datamodels.AuxiliaryQuota;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
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
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 辅料定额清单
 * 
 * @author zhoutong
 * @version 2018-11-08 将文档类型、数据集名称、数据集命名引用名称均改为配置
 * 	<p> 其他类一样修改
 */
public class AuxiliaryQuotaListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String prefName = "S4CUST_AuxiliaryQuotaList_Type";
			String[] auxiliaryQuotaListTypes = TcUtil.getPrefStringValues(prefName);
			if (auxiliaryQuotaListTypes == null || auxiliaryQuotaListTypes.length < 1) {
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
			if (!Utilities.contains(targetType, auxiliaryQuotaListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("AuxiliaryQuotaList.zhCN.Title"));
//			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", "AuxiliaryQuotaList");
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("AuxiliaryQuotaList.documentType"));
			
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateAuxiliaryQuotaList.Msg"));
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
				
				GenerateAuxiliaryQuotaListJob generateAuxiliaryQuotaListJob = new GenerateAuxiliaryQuotaListJob(ReportMessages.getString("hint.Title"), targetBOP);
				generateAuxiliaryQuotaListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						GenerateAuxiliaryQuotaListJob generateAuxiliaryQuotaListJob = (GenerateAuxiliaryQuotaListJob) event.getJob();
						if (generateAuxiliaryQuotaListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, generateAuxiliaryQuotaListJob.getReportDataset());
						}
					}
				});
				generateAuxiliaryQuotaListJob.setPriority(Job.INTERACTIVE);
				generateAuxiliaryQuotaListJob.setUser(true);
				generateAuxiliaryQuotaListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
	
	class GenerateAuxiliaryQuotaListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		private LinkedHashMap<String, AuxiliaryQuota> directAuxiliaryQuotaMap = null;
		private LinkedHashMap<String, AuxiliaryQuota> indirectAuxiliaryQuotaMap = null;
		
		public GenerateAuxiliaryQuotaListJob(String name, TargetBOP targetBOP) 
		{
			super(name);
			this.targetBOP = targetBOP;
			
			this.directAuxiliaryQuotaMap = new LinkedHashMap<String, AuxiliaryQuota>();
			this.indirectAuxiliaryQuotaMap = new LinkedHashMap<String, AuxiliaryQuota>();
		}

		@Override
		protected IStatus run(IProgressMonitor progressMonitor) 
		{
			progressMonitor.beginTask(ReportMessages.getString("workingAndWait.Msg"), -1);
			
			String tempWorkingDir = "";
			try {
				String prefName = "S4CUST_AuxiliaryQuotaList_AuxiliaryType";
				String[] auxiliaryTypes = TcUtil.getPrefStringValues(prefName);
				if (auxiliaryTypes == null || auxiliaryTypes.length == 0) {
					MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
					return Status.CANCEL_STATUS;
				}
				
				traverseBOP(this.targetBOP.bopLine, auxiliaryTypes);
				
				File auxiliaryQuotaListFile = generateAuxiliaryQuotaListFile();
				if (auxiliaryQuotaListFile.exists()) 
				{
					tempWorkingDir = auxiliaryQuotaListFile.getParent();
					
					// 工艺文档 名称/ 辅料定额清单报表数据集名称
					String objectName = ReportMessages.getString("AuxiliaryQuotaList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, auxiliaryQuotaListFile, objectName);
					if (this.reportDataset != null) {
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("AuxiliaryQuotaList.zhCN.Title"));
//						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", "AuxiliaryQuotaList");
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("AuxiliaryQuotaList.documentType"));
						
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
		private void traverseBOP(TCComponentBOPLine paramBOPLine, String[] auxiliaryTypes) throws TCException
		{
			AIFComponentContext[] contexts = paramBOPLine.getChildren();
			if (contexts != null && contexts.length > 0) 
			{
				for (AIFComponentContext context : contexts) 
				{
					boolean belongToAux = false; // 标记对象是否为辅料
					
					TCComponentBOPLine childBOPLine = (TCComponentBOPLine) context.getComponent();
					TCComponentItemRevision itemRevision = childBOPLine.getItemRevision();
					String itemRevType = itemRevision.getType();
					if (Utilities.contains(itemRevType, auxiliaryTypes)) {
						belongToAux = true;
					} else if (itemRevType.equals("S4_IT_PartRevision")) {
						TCComponent auxPartRevision = itemRevision.getRelatedComponent("S4_REL_AuxiliaryPart");
						if (auxPartRevision != null && auxPartRevision.isTypeOf("S4_IT_AuxPartRevision")) {
							belongToAux = true;
						}
					}
					
					if (belongToAux) 
					{
						String[] propNames = { "bl_occ_type", "bl_child_id" };
						String[] propValues = childBOPLine.getProperties(propNames);
						if (propValues != null && propValues.length == 2) 
						{
							String blOccType = propValues[0];
							String itemId = propValues[1];
							if (blOccType.equals("MEConsumed")) // 直接辅料
							{ 
								AuxiliaryQuota auxiliaryQuota = this.directAuxiliaryQuotaMap.get(itemId);
								if (auxiliaryQuota == null) {
									auxiliaryQuota = new AuxiliaryQuota(childBOPLine, itemRevision, itemRevType, ReportMessages.getString("DirectAuxiliaryQuota.Title"), itemId, this.targetBOP.languageSelection);
								} else {
									auxiliaryQuota.usageQuota += TcUtil.getUsageQuantity(childBOPLine);
									auxiliaryQuota.remark = TcUtil.getAppendRemark(auxiliaryQuota.remark, childBOPLine);
								}
								this.directAuxiliaryQuotaMap.put(itemId, auxiliaryQuota);
							} else if (blOccType.equals("MEResource")) { // 间接辅料
								AuxiliaryQuota auxiliaryQuota = this.indirectAuxiliaryQuotaMap.get(itemId);
								if (auxiliaryQuota == null) {
									auxiliaryQuota = new AuxiliaryQuota(childBOPLine, itemRevision, itemRevType, ReportMessages.getString("IndirectAuxiliaryQuota.Title"), itemId, this.targetBOP.languageSelection);
								} else {
									auxiliaryQuota.remark = TcUtil.getAppendRemark(auxiliaryQuota.remark, childBOPLine);
								}
								this.indirectAuxiliaryQuotaMap.put(itemId, auxiliaryQuota);
							}
						}
					}
					
					traverseBOP(childBOPLine, auxiliaryTypes);
				}
			}
		}

		/**
		 * 生成辅料定额清单报表
		 * 
		 * @param crucialProcessVector
		 * @return
		 * @throws Exception
		 */
		private File generateAuxiliaryQuotaListFile() throws Exception
		{
			String templateDatasetName = ReportMessages.getString("AuxiliaryQuotaList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("AuxiliaryQuotaList.Template.RowCount");
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
				int directSize = this.directAuxiliaryQuotaMap.size();
				int indirectSize = this.indirectAuxiliaryQuotaMap.size();
				int dataSize = directSize + indirectSize;
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 5, dataSize - rowCount); // 超出模板行数，复制并插入新行
				}
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // 车型
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // 专业
				
				JacobUtil.writeCellData(sheet, "C2", variant);
				JacobUtil.writeCellData(sheet, "F2" , profession);
				
				// 2018-11-29,  修改配置取值
				JacobUtil.writeCellData(sheet, "E3" , TcUtil.getConfiguration(this.targetBOP)); // 配置
				
				JacobUtil.writeCellData(sheet, "G3", TcUtil.getCurrentDate()); // 编制日期 
				
				String[] directAuxiliary = this.directAuxiliaryQuotaMap.keySet().toArray(new String[directSize]);
				for (int i = 0; i < directAuxiliary.length; i++) 
				{
					AuxiliaryQuota auxiliaryQuota = this.directAuxiliaryQuotaMap.get(directAuxiliary[i]);
					JacobUtil.writeCellData(sheet, "B" + (5 + i), auxiliaryQuota.number);
					JacobUtil.writeCellData(sheet, "C" + (5 + i), auxiliaryQuota.auxName);
					JacobUtil.writeCellData(sheet, "D" + (5 + i), auxiliaryQuota.specification);
					JacobUtil.writeCellData(sheet, "E" + (5 + i), auxiliaryQuota.unit);
					JacobUtil.writeCellData(sheet, "F" + (5 + i), auxiliaryQuota.usageQuota);
					JacobUtil.writeCellData(sheet, "G" + (5 + i), auxiliaryQuota.remark);
				}
				
				// 合并直接辅料单元格
				if (directSize > 1) {
					JacobUtil.mergeCell(sheet, "A5:" + "A" + (directSize + 4));
					JacobUtil.writeCellData(sheet, "A5", ReportMessages.getString("DirectAuxiliaryQuota.Title"));
				} else if (directSize == 1) {
					JacobUtil.writeCellData(sheet, "A5", ReportMessages.getString("DirectAuxiliaryQuota.Title"));
				}
				
				String[] indirectAuxiliary = this.indirectAuxiliaryQuotaMap.keySet().toArray(new String[indirectSize]);
				for (int i = 0; i < indirectAuxiliary.length; i++) 
				{
					AuxiliaryQuota auxiliaryQuota = this.indirectAuxiliaryQuotaMap.get(indirectAuxiliary[i]);
					JacobUtil.writeCellData(sheet, "B" + (5 + directSize + i), auxiliaryQuota.number);
					JacobUtil.writeCellData(sheet, "C" + (5 + directSize + i), auxiliaryQuota.auxName);
					JacobUtil.writeCellData(sheet, "D" + (5 + directSize + i), auxiliaryQuota.specification);
					JacobUtil.writeCellData(sheet, "E" + (5 + directSize + i), auxiliaryQuota.unit);
					if (auxiliaryQuota.usageQuota > 0) {
						JacobUtil.writeCellData(sheet, "F" + (5 + directSize + i), auxiliaryQuota.usageQuota);
					}
					JacobUtil.writeCellData(sheet, "G" + (5 + directSize + i), auxiliaryQuota.remark);
				}
				
				// 合并间接辅料单元格
				if (indirectSize > 1) {
					JacobUtil.mergeCell(sheet, "A" + (5 + directSize) + ":A" + (dataSize + 4));
					JacobUtil.writeCellData(sheet, "A" + (5 + directSize), ReportMessages.getString("IndirectAuxiliaryQuota.Title"));
				} else if (indirectSize == 1) {
					JacobUtil.writeCellData(sheet, "A" + (5 + directSize), ReportMessages.getString("IndirectAuxiliaryQuota.Title"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
			// 2018-11-08, 增加文件重命名
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("AuxiliaryQuotaList.enUS.Title"));
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
