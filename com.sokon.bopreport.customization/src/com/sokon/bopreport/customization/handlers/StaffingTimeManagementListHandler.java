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
import com.sokon.bopreport.customization.datamodels.StaffingTimeManagement;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
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
 * ��Ա����&��ʱ��
 * 
 * @author zhoutong
 *
 */
public class StaffingTimeManagementListHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try {
			String prefName = "S4CUST_StaffingTimeManagementList_Type";
			String[] staffingTimeManagementListTypes = TcUtil.getPrefStringValues(prefName);
			if (staffingTimeManagementListTypes == null || staffingTimeManagementListTypes.length < 1) {
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
			if (!Utilities.contains(targetType, staffingTimeManagementListTypes)) {
				MessageBox.post(ReportMessages.getString("notSelectSpecifiedObject.Msg"), ReportMessages.getString("hint.Title"), 2);
				return null;
			}
			
			final Shell shell = HandlerUtil.getActiveShell(event);
			
			TCComponentItemRevision documentRevision = TcUtil.getRelatedDocumentRevision(targetRevision, "IMAN_reference", "S4_IT_ProcessDoc", ReportMessages.getString("Staffing&TimeManagementList.documentType"));
			if (documentRevision != null)
			{
				boolean confirm = MessageDialog.openConfirm(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("confirmToUpdateStaffing&TimeManagementList.Msg"));
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
				
				StaffingTimeManagementListJob staffingTimeManagementListJob = new StaffingTimeManagementListJob(ReportMessages.getString("hint.Title"), targetBOP);
				staffingTimeManagementListJob.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event) 
					{
						StaffingTimeManagementListJob staffingTimeManagementListJob = (StaffingTimeManagementListJob) event.getJob();
						if (staffingTimeManagementListJob.isCompleted()) {
							TcUtil.openReportDataset(shell, staffingTimeManagementListJob.getReportDataset());
						}
					}
				});
				staffingTimeManagementListJob.setPriority(Job.INTERACTIVE);
				staffingTimeManagementListJob.setUser(true);
				staffingTimeManagementListJob.schedule();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

	class StaffingTimeManagementListJob extends Job
	{
		private TargetBOP targetBOP;
		
		private boolean completed = false;
		private TCComponentDataset reportDataset;
		
		public StaffingTimeManagementListJob(String name, TargetBOP targetBOP) 
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
//				LinkedHashMap<String, Vector<StaffingTimeManagement>> staffingTimeManagementMap = new LinkedHashMap<String, Vector<StaffingTimeManagement>>();
				
				// �޸ĵ��ඨ��ϲ����⣬����λ��ַǰ��λ��ͬʱ���ϲ����ඨ�by tzhou, 2019-01-23
				LinkedHashMap<String, LinkedHashMap<String, Vector<StaffingTimeManagement>>> staffingTimeManagementMap = new LinkedHashMap<String, LinkedHashMap<String, Vector<StaffingTimeManagement>>>();
				traverseBOP(this.targetBOP.bopLine, staffingTimeManagementMap, this.targetBOP.languageSelection);
				
				File staffingTimeManagementListFile = generateStaffingTimeManagementListFile(staffingTimeManagementMap);
				if (staffingTimeManagementListFile.exists()) 
				{
					tempWorkingDir = staffingTimeManagementListFile.getParent();
					
					// �����ĵ� ����/ ��Ա����&��ʱ�������ݼ�����
					String objectName = ReportMessages.getString("Staffing&TimeManagementList.enUS.Title");
					this.reportDataset = TcUtil.createOrUpdateReportDataset(this.targetBOP, staffingTimeManagementListFile, objectName);
					if (this.reportDataset != null) {
						this.targetBOP.documentRevision.setProperty("s4_AT_DocumentType", ReportMessages.getString("Staffing&TimeManagementList.documentType"));
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
//		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, Vector<StaffingTimeManagement>> staffingTimeManagementMap, int languageSelection) throws TCException
		private void traverseBOP(TCComponentBOPLine paramBOPLine, LinkedHashMap<String, LinkedHashMap<String, Vector<StaffingTimeManagement>>> staffingTimeManagementMap, int languageSelection) throws TCException
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
						StaffingTimeManagement staffingTimeManagement = new StaffingTimeManagement(childBOPLine, languageSelection);
						String processSection = staffingTimeManagement.processSection; // ���ݹ��η���
						/*Vector<StaffingTimeManagement> vector = staffingTimeManagementMap.get(processSection);
						if (vector == null) {
							vector = new Vector<StaffingTimeManagement>();
						}
						vector.add(staffingTimeManagement);
						staffingTimeManagementMap.put(processSection, vector);*/
						
						// ���ӹ�λ��ַǰ6λ�ķ��飬added by tzhou, 2019-01-23
						String subStationAddress = staffingTimeManagement.subStationAddress; // ��λ��ַǰ6λ
						LinkedHashMap<String, Vector<StaffingTimeManagement>> hashMap = staffingTimeManagementMap.get(processSection);
						if (hashMap == null) {
							Vector<StaffingTimeManagement> vector = new Vector<StaffingTimeManagement>();
							vector.add(staffingTimeManagement);
							
							hashMap = new LinkedHashMap<String, Vector<StaffingTimeManagement>>();
							hashMap.put(subStationAddress, vector);
						} else {
							Vector<StaffingTimeManagement> vector = hashMap.get(subStationAddress);
							if (vector == null) {
								vector = new Vector<StaffingTimeManagement>();
							}
							vector.add(staffingTimeManagement);
							hashMap.put(subStationAddress, vector);
						}
						staffingTimeManagementMap.put(processSection, hashMap);
					}
					
//					traverseBOP(childBOPLine, staffingTimeManagementMap, languageSelection);
					// ���ļ��򱳾����������±�����modified by zhoutong, 2019-02-16
					String occType = childBOPLine.getProperty("bl_occ_type");
					if (!Utilities.contains(occType, new String[] { "MEConsumed", "S4_MEBackground" }))
					{
						traverseBOP(childBOPLine, staffingTimeManagementMap, languageSelection);
					}
				}
			}
		}

		/**
		 * ������Ա����&��ʱ����
		 * 
		 * @param staffingTimeManagementMap
		 * @return
		 * @throws Exception
		 */
//		private File generateStaffingTimeManagementListFile(LinkedHashMap<String, Vector<StaffingTimeManagement>> staffingTimeManagementMap) throws Exception
		private File generateStaffingTimeManagementListFile(LinkedHashMap<String, LinkedHashMap<String, Vector<StaffingTimeManagement>>> staffingTimeManagementMap) throws Exception
		{
			String templateDatasetName = ReportMessages.getString("Staffing&TimeManagementList.Template");
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(templateDatasetName, "MSExcelX");
			if (templateDataset == null) {
				throw new Exception(ReportMessages.getString("datasetDoesNotExist.Msg", templateDatasetName));
			}
			
			File templateFile = TcUtil.getTemplateFile(templateDataset);
			if (templateFile == null) {
				throw new Exception(ReportMessages.getString("datasetHasNoNamedReference.Msg", templateDatasetName));
			}
			
			int rowCount = ReportMessages.getIntValue("Staffing&TimeManagementList.Template.RowCount");
			if (rowCount < 1) {
				throw new Exception(ReportMessages.getString("InvalidRowCountConfigurationForTemplate.Msg", templateDatasetName));
			}
			
			ActiveXComponent excelApp = JacobUtil.openExcelApp();
			Dispatch workBook = null;
			
			try {
				workBook = JacobUtil.getWorkBook(excelApp, templateFile);
				Dispatch sheets = JacobUtil.getSheets(workBook);
				Dispatch sheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
				
				String variant = this.targetBOP.itemRevision.getTCProperty("s4_AT_EngineeringModel").getUIFValue(); // ����
				String profession = TcUtil.getProfession(this.targetBOP.objectType); // רҵ
				
				JacobUtil.writeCellData(sheet, "C2", variant);
				JacobUtil.writeCellData(sheet, "F2" , profession);
				JacobUtil.writeCellData(sheet, "E3", TcUtil.getCurrentDate()); // �������� 
				
				// ��������
				int dataSize = 0;
				String[] processSections = staffingTimeManagementMap.keySet().toArray(new String[staffingTimeManagementMap.size()]);
				for (String processSection : processSections) 
				{
					/*Vector<StaffingTimeManagement> vector = staffingTimeManagementMap.get(processSection);
					dataSize += vector.size();*/
					
					LinkedHashMap<String,Vector<StaffingTimeManagement>> hashMap = staffingTimeManagementMap.get(processSection);
					String[] keys = hashMap.keySet().toArray(new String[0]);
					for (String key : keys) {
						Vector<StaffingTimeManagement> vector = hashMap.get(key);
						dataSize += vector.size();
					}
				}
				
				if (dataSize > rowCount) {
					JacobUtil.copyRow(sheet, 5, dataSize - rowCount); // ����ģ�����������Ʋ���������
				}
				
				/*int startNo = 5;
				for (String processSection : processSections) {
					Vector<StaffingTimeManagement> vector = staffingTimeManagementMap.get(processSection);
					batchWriteCellData(vector, startNo, sheet, processSection);
					startNo += vector.size();
				}*/
				
				int count = 0;
				for (String processSection : processSections) 
				{
					String mergeArea = "A" + (count + 5) + ":";
					
					LinkedHashMap<String,Vector<StaffingTimeManagement>> hashMap = staffingTimeManagementMap.get(processSection);
					String[] keys = hashMap.keySet().toArray(new String[0]);
					for (String key : keys) {
						Vector<StaffingTimeManagement> vector = hashMap.get(key);
						batchWriteCellData(vector, count + 5, sheet);
						count += vector.size();
					}
					
					mergeArea += "A" + (count + 5 - 1);
					
					JacobUtil.mergeCell(sheet, mergeArea);
					JacobUtil.writeCellData(sheet, mergeArea, processSection);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {
				JacobUtil.closeExcelApp(excelApp, workBook);
			}
			
			// 2018-11-08, �����ļ�������
			File reportFile = TcUtil.renameFile(templateFile, ReportMessages.getString("Staffing&TimeManagementList.enUS.Title"));
			return reportFile;
		}
		
		/**
		 * ����д����Ա����&��ʱ������
		 * 
		 * @param vector
		 * @param startNo	��ʼд�����ݵ��к�
		 * @param sheet
		 * @param processSection
		 * @throws Exception
		 */
//		private void batchWriteCellData(Vector<StaffingTimeManagement> vector, int startNo, Dispatch sheet, String processSection) throws Exception
		private void batchWriteCellData(Vector<StaffingTimeManagement> vector, int startNo, Dispatch sheet) throws Exception
		{
			int count = vector.size();
			for (int i = 0; i < count; i++) 
			{
				StaffingTimeManagement staffingTimeManagement = vector.get(i);
				
				int startIndex = i + startNo;
				
				JacobUtil.writeCellData(sheet, "B" + startIndex, staffingTimeManagement.stationAddress);
				JacobUtil.writeCellData(sheet, "C" + startIndex, staffingTimeManagement.operationalEssentials);
//				JacobUtil.writeCellData(sheet, "E" + startIndex, staffingTimeManagement.singleShiftStaffing);
				JacobUtil.writeCellData(sheet, "F" + startIndex, staffingTimeManagement.timeManagement);
				JacobUtil.writeCellData(sheet, "G" + startIndex, staffingTimeManagement.remark);
			}
			
			// �ϲ���ͬ��λ��Ԫ��, �ϲ����ඨ��
			/*if (count > 1) {
				String mergeArea = "A" + startNo + ":" + "A" + (count + startNo - 1);
				JacobUtil.mergeCell(sheet, mergeArea);
				JacobUtil.writeCellData(sheet, mergeArea, processSection);
				
				String mergeArea = "E" + startNo + ":" + "E" + (count + startNo - 1);
				JacobUtil.mergeCell(sheet, mergeArea);
				JacobUtil.writeCellData(sheet, mergeArea, vector.get(0).singleShiftStaffing);
			} else if (count == 1) {
				JacobUtil.writeCellData(sheet, "A" + startNo, processSection);
				JacobUtil.writeCellData(sheet, "E" + startNo, vector.get(0).singleShiftStaffing);
			}*/
			
			// �ϲ����ඨ��, 2019-01-23
			String mergeArea = "E" + startNo + ":" + "E" + (count + startNo - 1);
			JacobUtil.mergeCell(sheet, mergeArea);
			JacobUtil.writeCellData(sheet, mergeArea, vector.get(0).singleShiftStaffing);
		}
		
		public boolean isCompleted() {
			return completed;
		}

		public TCComponentDataset getReportDataset() {
			return reportDataset;
		}
		
	}
}
