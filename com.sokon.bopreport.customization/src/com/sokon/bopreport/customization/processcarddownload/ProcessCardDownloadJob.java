package com.sokon.bopreport.customization.processcarddownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ProcessCardDownloadJob extends Job 
{
	public Boolean completed = false;
	public Integer fileCount = 0;
	private Vector<TCComponentItemRevision> bopRevVector = new Vector<TCComponentItemRevision>();
	private String directoryPath;
	
	private Registry registry;
	
	// BOP版本类型:BOP结构中会关联工艺卡的对象类型
	private Map<String, String> hasDocTypeMap = new HashMap<String, String>();
	// BOP结构中会关联工艺卡的对象类型:工艺卡对象类型
	private Map<String, String> docTypeMap = new HashMap<String, String>();
	// 工艺类型类型：工艺卡报表类型
	private Map<String, String> proceessDocumentTypeMap = new HashMap<String, String>();
	// 工艺卡数据集类型
	private String[] datasetTypes;
	
	private Vector<TCComponentItemRevision> processCardNotRelease = new Vector<TCComponentItemRevision>();
	private Vector<TCComponentItemRevision> noProcessCard = new Vector<TCComponentItemRevision>();

	public ProcessCardDownloadJob(String name, Vector<TCComponentItemRevision> paramItemRevVector, String paramPath) {
		super(name);
		this.bopRevVector = paramItemRevVector;
		this.directoryPath = paramPath;
		this.registry = Registry.getRegistry(this);
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{		
		progressMonitor.beginTask(ReportMessages.getString("workingAndWait.Msg"), -1);
		
		try {
			String prefName = "S4CUST_ProcessCardDownload_DocType";
			String[] docTypes = TcUtil.getPrefStringValues(prefName);
			if (docTypes == null || docTypes.length == 0) {
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return Status.CANCEL_STATUS;
			}
			
			phaseDocTypesToMap(docTypes);
			
			if (this.hasDocTypeMap.size() != this.docTypeMap.size()
					|| this.hasDocTypeMap.size() != this.proceessDocumentTypeMap.size()
					|| this.docTypeMap.size() != this.proceessDocumentTypeMap.size()) {
				System.out.println(">>> 首选项 " + prefName + " 每个值需三个:");
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return Status.CANCEL_STATUS;
			}
			
			prefName = "S4CUST_ProcessCardDownload_DatasetType";
			this.datasetTypes = TcUtil.getPrefStringValues(prefName);
			if (this.datasetTypes == null || this.datasetTypes.length == 0) {
				MessageBox.post(ReportMessages.getString("invalidPrefConfiguration.Msg", prefName), ReportMessages.getString("hint.Title"), 2);
				return Status.CANCEL_STATUS;
			}
			
			for (TCComponentItemRevision bopItemRev : this.bopRevVector) 
			{
				TCComponentBOPLine bopLine = TcUtil.getTopBopLine(bopItemRev);
				String bopType = bopItemRev.getType(); 
				String targetType = this.hasDocTypeMap.get(bopType);
				if (targetType == null) {
					throw new Exception(MessageFormat.format(this.registry.getString("noMatchValueInPreefernce.Msg"),
							new Object[] { "S4CUST_ProcessCardDownload_DocType", bopType }));
				}
				Vector<TCComponentBOMLine> vector = new Vector<TCComponentBOMLine>();
				
				if (bopType.equals(targetType) && !vector.contains(bopLine)) {
					vector.add(bopLine);
				}
				
				traverseBOM(bopLine, targetType, vector);
				
				downloadDocFile(bopType, bopItemRev, vector);
				
				bopLine.window().close();
			}
			
			if (this.noProcessCard.size() > 0 || this.processCardNotRelease.size() > 0) {
				generateNotDownloadedProcessListFile();
				this.fileCount++;
			}
			
			this.completed = true;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			progressMonitor.done();
		}
		
		return Status.OK_STATUS;
	}
	
	private void phaseDocTypesToMap(String[] docTypes)
	{
		for (String docType : docTypes) 
		{
			String[] splitStrs = docType.split(":");
			if(splitStrs != null && splitStrs.length == 4)
			{
				this.hasDocTypeMap.put(splitStrs[0], splitStrs[1]);
				this.docTypeMap.put(splitStrs[1], splitStrs[2]);
				this.proceessDocumentTypeMap.put(splitStrs[1], splitStrs[3]);
			}
		}
	}

	private void traverseBOM(TCComponentBOMLine paramBOMLine, String targetType, Vector<TCComponentBOMLine> vector) throws TCException
	{
		AIFComponentContext[] contexts = paramBOMLine.getChildren();
		if (contexts != null && contexts.length > 0) 
		{
			for (AIFComponentContext context : contexts)
			{
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine) context.getComponent();
				if (childBOMLine.getItemRevision().isTypeOf(targetType) && !vector.contains(childBOMLine)) {
					vector.add(childBOMLine);
				}
				
				traverseBOM(childBOMLine, targetType, vector);
			}
		}
	}
	
	private void downloadDocFile(String bopType, TCComponentItemRevision bopItemRev, Vector<TCComponentBOMLine> vector) throws TCException
	{
		for (TCComponentBOMLine hasDocBOMLine : vector) 
		{
			Vector<TCComponentDataset> docDatasets = getDocDatasets(hasDocBOMLine);
			if (docDatasets.size() > 0) 
			{
				for (TCComponentDataset dataset : docDatasets) 
				{
					String namedReferenceType = this.registry.getString(dataset.getType());
					String fileName = getFileNameByBOPType(bopType, bopItemRev, hasDocBOMLine);
					File[] files = dataset.getFiles(namedReferenceType, this.directoryPath);
					if (files != null && files.length > 0) {
						TcUtil.renameFile(files[0], fileName);
						this.fileCount++;
					}
				}
			}
		}
	}
	
	private String getFileNameByBOPType(String bopType, TCComponentItemRevision bopItemRev, TCComponentBOMLine hasDocBOMLine) throws TCException
	{
		switch (bopType) {
		case "S4_IT_GAPlantBOPRevision":
			return getGAPaintingFileName(hasDocBOMLine);
		case "S4_IT_PaintBOPRevision":
			return getPaintingFileName(hasDocBOMLine);
		case "S4_IT_BAPlantBOPRevision":
			return getBAPaintingFileName(hasDocBOMLine);
		case "S4_IT_StampBOPRevision":
			return bopItemRev.getProperty("object_name");
		default:
			return bopItemRev.getProperty("object_name");
		}
	}
	
	private String getGAPaintingFileName(TCComponentBOMLine opBOMLine) throws TCException
	{
		String gaPaintingFileName = opBOMLine.getProperty("bl_item_object_name");
		String procResArea = TcUtil.getProcResArea(opBOMLine);
		TCComponent[] relatedComponents = opBOMLine.parent().getRelatedComponents("Mfg0assigned_workarea");
		if (relatedComponents != null && relatedComponents.length > 0) {			
			String workAreaId = relatedComponents[0].getProperty("bl_child_id");
			
			gaPaintingFileName = TcUtil.getLast7String(workAreaId + procResArea) + " " + gaPaintingFileName;
		}
		return gaPaintingFileName;
	}
	
	private String getPaintingFileName(TCComponentBOMLine stationBOMLine) throws TCException
	{
		String paintingFileName = stationBOMLine.getItemRevision().getStringProperty("s4_AT_MEStationArea");
		if (paintingFileName == null || paintingFileName.isEmpty()) {
			paintingFileName = "0";
		}
		TCComponent[] relatedComponents = stationBOMLine.getRelatedComponents("Mfg0assigned_workarea");
		if (relatedComponents != null && relatedComponents.length > 0) {
			
			String workAreaId = relatedComponents[0].getProperty("bl_child_id");
			String workAreaName = ((TCComponentBOMLine)relatedComponents[0]).getItem().getProperty("object_name");
			
			paintingFileName = TcUtil.getLast7String(workAreaId + paintingFileName) + " " + workAreaName;
		}
		return paintingFileName;
	}
	
	private String getBAPaintingFileName(TCComponentBOMLine stationBOMLine) throws TCException
	{
		String baPaintingFileName = "";
		TCComponent[] relatedComponents = stationBOMLine.getRelatedComponents("Mfg0assigned_workarea");
		if (relatedComponents != null && relatedComponents.length > 0) 
		{			
			String workAreaId = relatedComponents[0].getProperty("bl_child_id") + "0";
			String workAreaName = ((TCComponentBOMLine)relatedComponents[0]).getItem().getProperty("object_name");
			
			baPaintingFileName = TcUtil.getLast7String(workAreaId) + " " + workAreaName;
		}
		return baPaintingFileName;
	}
	
	private Vector<TCComponentDataset> getDocDatasets(TCComponentBOMLine targetBOMLine) throws TCException
	{
		Vector<TCComponentDataset> datasetVector = new Vector<TCComponentDataset>();
		
		TCComponentItemRevision itemRevision = targetBOMLine.getItemRevision();
		String targetType = itemRevision.getType();
		String docType = this.docTypeMap.get(targetType);
		if (docType == null) {
			String prefName = "S4CUST_ProcessCardDownload_DocType";
			throw new TCException(MessageFormat.format(this.registry.getString("noMatchValueInPreefernce.Msg"),
					new Object[] { prefName, targetType }));
		}
		String documentType = this.proceessDocumentTypeMap.get(targetType);
		
		boolean hasDoc = false;
		TCComponent[] referenceComponents = targetBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
		if (referenceComponents != null && referenceComponents.length > 0) 
		{
			for (TCComponent referenceComponent : referenceComponents) 
			{
				if (referenceComponent.isTypeOf(docType)) 
				{
					TCComponentItem docItem = (TCComponentItem) referenceComponent;
					if (docItem.getLatestItemRevision().getStringProperty("s4_AT_DocumentType").equals(documentType)) 
					{
						hasDoc = true;
						
						TCComponentItemRevision[] releasedItemRevs = docItem.getReleasedItemRevisions();
						if (releasedItemRevs != null && releasedItemRevs.length > 0) 
						{
							TCComponent[] specificationComponents = releasedItemRevs[0].getRelatedComponents("IMAN_specification");
							if (specificationComponents != null && specificationComponents.length > 0)
							{
								for (TCComponent specificationComponent : specificationComponents) {
									if (specificationComponent.isTypeOf(this.datasetTypes)) {
										datasetVector.add((TCComponentDataset) specificationComponent);
									}
								}
							}
						} else {
							// Process Card Not Release
							this.processCardNotRelease.add(itemRevision);
						}
					}
				}
			}
		} 
		
		// No Process Card
		if (!hasDoc) {
			this.noProcessCard.add(itemRevision);
		}
		
		return datasetVector;
	}
	
	private void generateNotDownloadedProcessListFile() throws IOException, TCException
	{
		String fileName = "Not Downloaded Process List.txt";
		File notDownloadedProcessListFile = new File(this.directoryPath, fileName);
		if (notDownloadedProcessListFile.exists()) {
			notDownloadedProcessListFile.delete();
		}
		notDownloadedProcessListFile.createNewFile();
		
		PrintWriter printWriter = new PrintWriter(new FileOutputStream(notDownloadedProcessListFile));
		
		if (this.noProcessCard.size() > 0) {
			for (TCComponentItemRevision itemRev : this.noProcessCard) {
				String message = itemRev.getProperty("object_string") + "  " + "No Process Card";
				printWriter.println(message);
			}
			
			printWriter.println("");
		}
		
		if (this.processCardNotRelease.size() > 0) {
			for (TCComponentItemRevision itemRev : this.processCardNotRelease) {
				String message = itemRev.getProperty("object_string") + "  " + "Process Card Not Release";
				printWriter.println(message);
			}
		}
		
		if (printWriter != null) {
			printWriter.flush();
			printWriter.close();
		}
	}
	
}
