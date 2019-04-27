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
	
	// 更改前
	private Map<String, TCComponent> hasImpactedItemMap = new HashMap<String, TCComponent>();
	// 更改后
	private Map<String, TCComponent> hasSolutionItemMap = new HashMap<String, TCComponent>();
	// 记录更改前后的对象ID集合
	private Vector<String> itemIDVector = new Vector<String>();
	private Map<String, String> itemIDToTypeMap = new HashMap<String, String>();
	// 记录对象在Z9_PBYDPART表中的零件ID
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
			progressMonitor.beginTask("正在生成设计变更通知单，请耐心等待...", -1);
			
			String projectStage = this.dcnItemRev.getStringProperty("z9_Project_Stage");
			if (!Utilities.contains(projectStage, new String[] { "beforeMP", "afterMP" })) {
				MessageBox.post("【所属系统项目阶段】属性值填写有误！", "错误", 1);
				return Status.CANCEL_STATUS;
			}
			
			String preferenceValue = "";
			if (projectStage.equals("beforeMP")) 
			{
				preferenceValue = TcUtil.getSitePreferenceValue("BYD_DCN_BeforeMassProduction");
				if (preferenceValue.isEmpty() || !preferenceValue.contains("/")) {
					MessageBox.post("首选项【BYD_DCN_BeforeMassProduction】配置有误，请联系管理员！", "错误", 1);
					return Status.CANCEL_STATUS;
				}
			} else if (projectStage.equals("afterMP")) {
				preferenceValue = TcUtil.getSitePreferenceValue("BYD_DCN_AfterMassProduction");
				if (preferenceValue.isEmpty() || !preferenceValue.contains("/")) {
					MessageBox.post("首选项【BYD_DCN_AfterMassProduction】配置有误，请联系管理员！", "错误", 1);
					return Status.CANCEL_STATUS;
				}
			}
			
			// 查找模板对象
			String[] splitStrs = preferenceValue.split("/");
			TCComponentItemRevision templateItemRevision = TcUtil.findItemRevision(splitStrs[0], splitStrs[1]);
			if (templateItemRevision == null) {
				MessageBox.post("无法找到ID为 " + splitStrs[0] + " , 版本为 " + splitStrs[1] + " 的模板对象！" , "错误", 1);
				return Status.CANCEL_STATUS;
			}
			TCComponentDataset templateDataset = getRelatedDataset(templateItemRevision);
			if (templateDataset == null) {
				MessageBox.post("模板对象 " + templateItemRevision + " 下未找到模板数据集！" , "错误", 1);
				return Status.CANCEL_STATUS;
			}
			
			// ECO
			TCComponent relatedComponent = this.dcnItemRev.getRelatedComponent("Z9_RelatedECO");
			if (relatedComponent == null) {
				MessageBox.post("所选DCN版本对象未关联ECO对象！", "提示", 2);
				return Status.CANCEL_STATUS;
			}
			String ecoItemID = relatedComponent.getProperty("item_id");
			
			// 更改前零组件
			TCComponent[] hasImpactedItems = this.dcnItemRev.getRelatedComponents("CMHasImpactedItem");
			if (hasImpactedItems == null || hasImpactedItems.length == 0) {
				MessageBox.post("所选DCN版本对象没有更改前零组件！", "提示", 2);
				return Status.CANCEL_STATUS;
			}
			for (TCComponent hasImpactedItem : hasImpactedItems) 
			{
				String itemID = hasImpactedItem.getProperty("item_id");
				this.hasImpactedItemMap.put(itemID, hasImpactedItem);
				
				String type = hasImpactedItem.getType();
				this.itemIDToTypeMap.put(itemID, type);
			}
			
			// 更改后零组件
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
				MessageBox.post(message, "提示", 2);
				return Status.CANCEL_STATUS;
			}
			
			tempDirectory = FileUtil.createTempDirectory("GenerateChangeNotice");
			File[] files = templateDataset.getFiles("word", tempDirectory);
			if (files == null || files.length == 0) {
				MessageBox.post("模板数据集 " + templateDataset + " 下未找到模板文件！" , "错误", 1);
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
	 * 获取版本对象下规格关系的word数据集
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
	 * 检查更改前零组件是否在数据库中有记录, 返回没有数据的数量
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
					stringBuffer.append(itemID + " 零组件不在 " + ecoItemID + " 变更\n\n");
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
	 * 生成报表文件
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
		
		// 处理页眉，填写【编号】
		ReportUtil.updateHeader(document, reportData.dcnItemID);
		
		List<XWPFTable> tables = document.getTables();
		if (tables != null && tables.size() > 0) 
		{
			XWPFTable table = tables.get(0);
			
			int minus = 0;
			if (allPartInfoVector.size() > 0) // 模板需写数据的行原有1行，超过1条数据先插入行
			{
				minus = allPartInfoVector.size() - 1;
				
				XWPFTableRow fourthRow = table.getRow(4);
				for (int i = 0; i < minus; i++) {
					table.addRow(fourthRow, 5); // 复制第5行，并插入到第5行之前
				}
				
				int techFileStartRowNum = 11 + minus;
				XWPFTableRow techFileStartRow = table.getRow(techFileStartRowNum);
				for (int i = 0; i < allTechFileInfoVector.size() - 1; i++) {
					table.addRow(techFileStartRow, techFileStartRowNum + 1); // 复制第12行，并插入到第12行之前
				}
			}
			
			// 拷贝行后保存一次临时文件，不然后面无法获取复制出的行
			fileInputStream.close();
			//写到目标文件
			String tempPath = tempDirectory + reportData.dcnItemID + "_设计变更通知单_temp" + suffix;
			OutputStream outputStream = new FileOutputStream(tempPath);
			document.write(outputStream);
			outputStream.close();
			
			File tempFile = new File(tempPath);
			fileInputStream = new FileInputStream(tempFile);
			document = new CustomXWPFDocument(fileInputStream);
			table = document.getTables().get(0);
			
			XWPFTableRow firstRow = table.getRow(0); // 第一行
			// 第一行第四列，填写【支持文件编号】
			firstRow.getCell(3).setText(reportData.supportFileNumber);
			
			XWPFTableRow secondRow = table.getRow(1); // 第二行
			// 第二行第一列，填写【来源/变更原因】
			XWPFParagraph paragraph_1 = secondRow.getCell(0).getParagraphs().get(0);
			XWPFRun run_1 = paragraph_1.createRun();
			run_1.addBreak(); // 换行
			run_1.setText(reportData.reasonsChange + "/" + reportData.changeSource); 
			
			// 第二行第二列，填写【变更措施】
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
			
			// 技术文件
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
				if (techFileInfo.preChangeImage != null && techFileInfo.preChangeImage.length > 3) // 存在图片才处理图片插入，下同
				{
					String preChangeImagePath = tempDirectory + techFileInfo.itemID + "_preChangeImage." + techFileInfo.preChangeImageType;
					FileUtil.decodeBytes2File(techFileInfo.preChangeImage, preChangeImagePath);
					// 更改前图片像素限宽253
					ReportUtil.insertPicture(preChangeCell, preChangeImagePath, document, techFileInfo.preChangeImageType, 253);
				}
				
				XWPFTableCell postChangeCell = techFileRow.getCell(5);
				postChangeCell.setText(techFileInfo.postChangeDescription);
				if (techFileInfo.postChangeImage != null && techFileInfo.postChangeImage.length > 3) 
				{
					String postChangeImagePath = tempDirectory + techFileInfo.itemID + "_postChangeImage." + techFileInfo.postChangeImageType;
					FileUtil.decodeBytes2File(techFileInfo.postChangeImage, postChangeImagePath);
					// 更改后图片像素限宽381
					ReportUtil.insertPicture(postChangeCell, postChangeImagePath, document, techFileInfo.postChangeImageType, 381);
				}
			}
		}
		
		fileInputStream.close();
		//写到目标文件
		String reportFilePath = tempDirectory + reportData.dcnItemID + "_设计变更通知单" + suffix;
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