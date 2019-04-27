package com.byd.cyc.bom.generatechangenotice;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
//import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

import com.byd.cyc.bom.utils.*;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.util.*;

/**
 * @author zhoutong
 *
 */
public class GenerateChangeNoticeJob extends Job
{
	private TCComponentItemRevision dcnItemRev;
	
	private boolean completed = false;
	private TCComponentDataset reportDataset;
	
	// ����ǰ
	private Map<String, TCComponent> hasImpactedItemMap = new HashMap<String, TCComponent>();
	// ���ĺ�
	private Map<String, TCComponent> hasSolutionItemMap = new HashMap<String, TCComponent>();
	// ��¼����ǰ��Ķ���ID����
	private Vector<String> itemIDVector = new Vector<String>();
	private Map<String, String> itemIDToTypeMap = new HashMap<String, String>();
	// ��¼������Z9_PBYDPART���е����ID
	private Map<String, String> pItemIDMap = new HashMap<String, String>();
	
	public GenerateChangeNoticeJob(String name, TCComponentItemRevision itemRev) {
		super(name);
		this.dcnItemRev = itemRev;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		String tempDirectory = "";
		
		try {
			progressMonitor.beginTask("����������Ʊ��֪ͨ���������ĵȴ�...", -1);
			
			String projectStage = this.dcnItemRev.getStringProperty("z9_Project_Stage");
			if (!Utilities.contains(projectStage, new String[] { "beforeMP", "afterMP" })) {
				MessageBox.post("������ϵͳ��Ŀ�׶Ρ�����ֵ��д����", "����", 1);
				return Status.CANCEL_STATUS;
			}
			
			String preferenceValue = "";
			if (projectStage.equals("beforeMP")) 
			{
				preferenceValue = TcUtil.getSitePreferenceValue("BYD_DCN_BeforeMassProduction");
				if (preferenceValue.isEmpty() || !preferenceValue.contains("/")) {
					MessageBox.post("��ѡ�BYD_DCN_BeforeMassProduction��������������ϵ����Ա��", "����", 1);
					return Status.CANCEL_STATUS;
				}
			} else if (projectStage.equals("afterMP")) {
				preferenceValue = TcUtil.getSitePreferenceValue("BYD_DCN_AfterMassProduction");
				if (preferenceValue.isEmpty() || !preferenceValue.contains("/")) {
					MessageBox.post("��ѡ�BYD_DCN_AfterMassProduction��������������ϵ����Ա��", "����", 1);
					return Status.CANCEL_STATUS;
				}
			}
			
			// ����ģ�����
			String[] splitStrs = preferenceValue.split("/");
			TCComponentItemRevision templateItemRevision = TcUtil.findItemRevision(splitStrs[0], splitStrs[1]);
			if (templateItemRevision == null) {
				MessageBox.post("�޷��ҵ�IDΪ " + splitStrs[0] + " , �汾Ϊ " + splitStrs[1] + " ��ģ�����" , "����", 1);
				return Status.CANCEL_STATUS;
			}
			TCComponentDataset templateDataset = getRelatedDataset(templateItemRevision);
			if (templateDataset == null) {
				MessageBox.post("ģ����� " + templateItemRevision + " ��δ�ҵ�ģ�����ݼ���" , "����", 1);
				return Status.CANCEL_STATUS;
			}
			
			// ECO
			TCComponent relatedComponent = this.dcnItemRev.getRelatedComponent("Z9_RelatedECO");
			if (relatedComponent == null) {
				MessageBox.post("��ѡDCN�汾����δ����ECO����", "��ʾ", 2);
				return Status.CANCEL_STATUS;
			}
			String ecoItemID = relatedComponent.getProperty("item_id");
			
			// ����ǰ�����
			TCComponent[] hasImpactedItems = this.dcnItemRev.getRelatedComponents("CMHasImpactedItem");
			if (hasImpactedItems == null || hasImpactedItems.length == 0) {
				MessageBox.post("��ѡDCN�汾����û�и���ǰ�������", "��ʾ", 2);
				return Status.CANCEL_STATUS;
			}
			for (TCComponent hasImpactedItem : hasImpactedItems) 
			{
				String itemID = hasImpactedItem.getProperty("item_id");
				this.hasImpactedItemMap.put(itemID, hasImpactedItem);
				
				String type = hasImpactedItem.getType();
				this.itemIDToTypeMap.put(itemID, type);
			}
			
			// ���ĺ������
			TCComponent[] hasSolutionItems = this.dcnItemRev.getRelatedComponents("CMHasSolutionItem");
			if (hasSolutionItems != null && hasSolutionItems.length > 0) 
			{
				for (TCComponent hasSolutionItem : hasSolutionItems) 
				{
					String itemID = hasSolutionItem.getProperty("item_id");
					this.hasSolutionItemMap.put(itemID, hasSolutionItem);
					String type = hasSolutionItem.getType();
					this.itemIDToTypeMap.put(itemID, type);
				}
			}
			
			StringBuffer stringBuffer = new StringBuffer("");
			if (hasNoRecordNum(ecoItemID, stringBuffer) > 0) {
				String message = stringBuffer.substring(0, stringBuffer.lastIndexOf("\n\n"));
				MessageBox.post(message, "��ʾ", 2);
				return Status.CANCEL_STATUS;
			}
			
			tempDirectory = FileUtil.createTempDirectory("GenerateChangeNotice");
			File[] files = templateDataset.getFiles("word", tempDirectory);
			if (files == null || files.length == 0) {
				MessageBox.post("ģ�����ݼ� " + templateDataset + " ��δ�ҵ�ģ���ļ���" , "����", 1);
				return Status.CANCEL_STATUS;
			}
			
			ReportData reportData = new ReportData(this.dcnItemRev, ecoItemID, projectStage, 
					this.hasImpactedItemMap, this.hasSolutionItemMap, this.itemIDVector, this.pItemIDMap);
			File reportFile = generateReport(files[0], reportData);
			
			this.reportDataset = getRelatedDataset(this.dcnItemRev);
			if (this.reportDataset != null) {
				this.reportDataset.removeFiles("word");
			} else {
				String dcnItemRevisionID = this.dcnItemRev.getProperty("item_revision_id");
				this.reportDataset = TcUtil.createDataset(reportData.dcnItemID + "/" + dcnItemRevisionID, "", "MSWordX");
				this.dcnItemRev.add("IMAN_specification", this.reportDataset);
			}
			this.reportDataset.setFiles(new String[] { reportFile.getAbsolutePath() }, new String[] { "word" });
			
			this.completed = true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			FileUtil.deleteFolder(tempDirectory);
			progressMonitor.done();
		}
		
		return Status.OK_STATUS;
	}
	
	/**
	 * ��ȡ�汾�����¹���ϵ��word���ݼ�
	 * 
	 * @param itemRevision
	 * @return
	 * @throws TCException
	 */
	private TCComponentDataset getRelatedDataset(TCComponentItemRevision itemRevision) throws TCException
	{
		TCComponentDataset dataset = null;
		TCComponent[] relatedComponents = itemRevision.getRelatedComponents("IMAN_specification");
		if (relatedComponents != null && relatedComponents.length > 0) 
		{
			for (TCComponent relatedComponent : relatedComponents) 
			{
				if (relatedComponent instanceof TCComponentDataset && relatedComponent.isTypeOf("MSWordX")) {
					dataset = (TCComponentDataset) relatedComponent;
					break;
				}
			}
		}
		return dataset;
	}
	
	/**
	 * ������ǰ������Ƿ������ݿ����м�¼, ����û�����ݵ�����
	 * 
	 * @param ecoItemID
	 * @param hasImpactedItems
	 * @param stringBuffer
	 * @return
	 * @throws TCException
	 */
	private int hasNoRecordNum(String ecoItemID, StringBuffer stringBuffer)
	{
		int hasNoRecordNum = 0;
		
		DBCon dbCon = new DBCon();
	    Connection connection = null;
		try {
			connection = dbCon.getCon();
			
			Iterator<Entry<String, String>> iterator = this.itemIDToTypeMap.entrySet().iterator();
			while (iterator.hasNext()) 
			{
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
				String itemID = entry.getKey();
				String type = entry.getValue();
				
				this.itemIDVector.add(itemID);
				
				if (noCheck(itemID, type)) {
					continue;
				}
				
				String sql = "select PITEMID from Z9_PBYDPART where PECOID = ? and";
				if (Utilities.contains(type, ReportData.TYPES_3D)) {
					sql += " PDESIGNPARTNUMBER = ?";
				} else if (type.equals("Z9_DrawingRevision")) {
					sql += " PDRAWINGNUMBER = ?";
				} else if (type.equals("Z9_DocumentRevision")) {
					sql += " PTRSNUMBER = ?";
				}
				
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, ecoItemID);
				preparedStatement.setString(2, itemID);
				ResultSet resultSet = preparedStatement.executeQuery();
				if (resultSet == null || !resultSet.next()) {
					hasNoRecordNum++;
					stringBuffer.append(itemID + " ��������� " + ecoItemID + " ���\n\n");
				} else {
					this.pItemIDMap.put(itemID, resultSet.getString(1));
				}
				dbCon.close(resultSet, preparedStatement, null);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			dbCon.close(null, null, connection);
		}
		
		return hasNoRecordNum;
	}
	
	private boolean noCheck(String itemID, String type)
	{
		if (Utilities.contains(type, ReportData.TYPES_3D)) 
		{
			if (this.hasImpactedItemMap.containsKey(itemID) && !this.hasSolutionItemMap.containsKey(itemID)) {
				return true;
			}
			
			if (!this.hasImpactedItemMap.containsKey(itemID) && this.hasSolutionItemMap.containsKey(itemID)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * ���ɱ����ļ�
	 * 
	 * @param templateFile
	 * @param reportData
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	private File generateReport(File templateFile, ReportData reportData) throws IOException, InvalidFormatException
	{
		String tempDirectory = templateFile.getParent() + File.separator;
		String suffix = FileUtil.getSuffix(templateFile.getName());
		
		Vector<PartInfo> allPartInfoVector = reportData.getAllPartInfoVector();
		Vector<TechFileInfo> allTechFileInfoVector = reportData.getAllTechFileInfoVector();
		
		FileInputStream fileInputStream = new FileInputStream(templateFile);
		CustomXWPFDocument document = new CustomXWPFDocument(fileInputStream);
		
		// ����ҳü����д����š�
		ReportUtil.updateHeader(document, reportData.dcnItemID);
		
		List<XWPFTable> tables = document.getTables();
		if (tables != null && tables.size() > 0) 
		{
			XWPFTable table = tables.get(0);
			
			int minus = 0;
			if (allPartInfoVector.size() > 0) // ģ����д���ݵ���ԭ��1�У�����1�������Ȳ�����
			{
				minus = allPartInfoVector.size() - 1;
				
				XWPFTableRow fourthRow = table.getRow(4);
				for (int i = 0; i < minus; i++) {
					table.addRow(fourthRow, 5); // ���Ƶ�5�У������뵽��5��֮ǰ
				}
				
				int techFileStartRowNum = 11 + minus;
				XWPFTableRow techFileStartRow = table.getRow(techFileStartRowNum);
				for (int i = 0; i < allTechFileInfoVector.size() - 1; i++) {
					table.addRow(techFileStartRow, techFileStartRowNum + 1); // ���Ƶ�12�У������뵽��12��֮ǰ
				}
			}
			
			// �����к󱣴�һ����ʱ�ļ�����Ȼ�����޷���ȡ���Ƴ�����
			fileInputStream.close();
			//д��Ŀ���ļ�
			String tempPath = tempDirectory + reportData.dcnItemID + "_��Ʊ��֪ͨ��_temp" + suffix;
			OutputStream outputStream = new FileOutputStream(tempPath);
			document.write(outputStream);
			outputStream.close();
			
			File tempFile = new File(tempPath);
			fileInputStream = new FileInputStream(tempFile);
			document = new CustomXWPFDocument(fileInputStream);
			table = document.getTables().get(0);
			
			XWPFTableRow firstRow = table.getRow(0); // ��һ��
			// ��һ�е����У���д��֧���ļ���š�
			firstRow.getCell(3).setText(reportData.supportFileNumber);
			
			XWPFTableRow secondRow = table.getRow(1); // �ڶ���
			// �ڶ��е�һ�У���д����Դ/���ԭ��
			XWPFParagraph paragraph_1 = secondRow.getCell(0).getParagraphs().get(0);
			XWPFRun run_1 = paragraph_1.createRun();
			run_1.addBreak(); // ����
			run_1.setText(reportData.reasonsChange + "/" + reportData.changeSource); 
			
			// �ڶ��еڶ��У���д�������ʩ��
			XWPFParagraph paragraph_2 = secondRow.getCell(1).getParagraphs().get(0);
			XWPFRun run_2 = paragraph_2.createRun();
			run_2.addBreak(); 
			run_2.setText(reportData.changeMeasures); 
			
			for (int i = 0; i < allPartInfoVector.size(); i++) 
			{
				PartInfo partInfo = allPartInfoVector.get(i);
				XWPFTableRow partInfoRow = table.getRow(4 + i);
				
				partInfoRow.getCell(0).setText(partInfo.itemID);
				partInfoRow.getCell(1).setText(partInfo.objectName);
				partInfoRow.getCell(2).setText(partInfo.changeStaus);
				partInfoRow.getCell(3).setText(partInfo.partVersionNum);
				
				partInfoRow.getCell(4).setText(partInfo.designPartPreStatus);
				partInfoRow.getCell(5).setText(partInfo.designPartPreVersion);
				partInfoRow.getCell(6).setText(partInfo.designPartPostVersion);
				
				partInfoRow.getCell(7).setText(partInfo.drawingPreVersion);
				partInfoRow.getCell(8).setText(partInfo.drawingPostVersion);
				
				partInfoRow.getCell(9).setText(partInfo.trsPreVersion);
				partInfoRow.getCell(10).setText(partInfo.trsPostVersion);
				
				partInfoRow.getCell(13).setText(partInfo.firstClassSupply);
				partInfoRow.getCell(15).setText(partInfo.weightChange);
				partInfoRow.getCell(16).setText(partInfo.interChangeability);
				partInfoRow.getCell(17).setText(partInfo.changesAffect);
				partInfoRow.getCell(18).setText(partInfo.affectCertification);
				partInfoRow.getCell(19).setText(partInfo.deliveryTime);
				partInfoRow.getCell(20).setText(partInfo.submitDate);
				partInfoRow.getCell(21).setText(partInfo.counterSignDrawing);
				
				if (reportData.projectStage.equals("beforeMP")) {
					partInfoRow.getCell(22).setText(partInfo.z9memo);
				} else {
					partInfoRow.getCell(22).setText(partInfo.internalTrial);
					partInfoRow.getCell(23).setText(partInfo.suggestMethod);
					partInfoRow.getCell(24).setText(partInfo.switchingTime);
					partInfoRow.getCell(25).setText(partInfo.expectedSwitching);
					partInfoRow.getCell(26).setText(partInfo.z9memo);
				}
			}
			
			// �����ļ�
			for (int i = 0; i < allTechFileInfoVector.size(); i++) 
			{
				TechFileInfo techFileInfo = allTechFileInfoVector.get(i);
				
				XWPFTableRow techFileRow = table.getRow(11 + minus + i);
				String sequenceNo = String.valueOf(i + 1);
				techFileRow.getCell(0).setText(sequenceNo);
				techFileRow.getCell(1).setText(techFileInfo.partInfo);
				techFileRow.getCell(2).setText(techFileInfo.changeType);
				if (!techFileInfo.itemID.isEmpty() && !techFileInfo.objectName.isEmpty()) {
					techFileRow.getCell(3).setText(techFileInfo.itemID + "/" + techFileInfo.objectName);
				}
				
				XWPFTableCell preChangeCell = techFileRow.getCell(4);
				preChangeCell.setText(techFileInfo.preChangeDescription);
				if (techFileInfo.preChangeImage != null && techFileInfo.preChangeImage.length > 3) // ����ͼƬ�Ŵ���ͼƬ���룬��ͬ
				{
					String preChangeImagePath = tempDirectory + techFileInfo.itemID + "_preChangeImage." + techFileInfo.preChangeImageType;
					FileUtil.decodeBytes2File(techFileInfo.preChangeImage, preChangeImagePath);
					// ����ǰͼƬ�����޿�253
					ReportUtil.insertPicture(preChangeCell, preChangeImagePath, document, techFileInfo.preChangeImageType, 253);
				}
				
				XWPFTableCell postChangeCell = techFileRow.getCell(5);
				postChangeCell.setText(techFileInfo.postChangeDescription);
				if (techFileInfo.postChangeImage != null && techFileInfo.postChangeImage.length > 3) 
				{
					String postChangeImagePath = tempDirectory + techFileInfo.itemID + "_postChangeImage." + techFileInfo.postChangeImageType;
					FileUtil.decodeBytes2File(techFileInfo.postChangeImage, postChangeImagePath);
					// ���ĺ�ͼƬ�����޿�381
					ReportUtil.insertPicture(postChangeCell, postChangeImagePath, document, techFileInfo.postChangeImageType, 381);
				}
			}
		}
		
		fileInputStream.close();
		//д��Ŀ���ļ�
		String reportFilePath = tempDirectory + reportData.dcnItemID + "_��Ʊ��֪ͨ��" + suffix;
		OutputStream outputStream = new FileOutputStream(reportFilePath);
		document.write(outputStream);
		outputStream.close();
		return new File(reportFilePath);
	}
	
	public boolean isCompleted() {
		return completed;
	}

	public TCComponentDataset getReportDataset() {
		return reportDataset;
	}
	
}