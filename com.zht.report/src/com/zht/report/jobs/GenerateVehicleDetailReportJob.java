package com.zht.report.jobs;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;
import com.zht.report.datamodels.VehicleDetailInfo;
import com.zht.report.dialogs.ZHTConstants;
import com.zht.report.log.LogAppend;
import com.zht.report.utils.JacobUtil;
import com.zht.report.utils.ReportUtil;
import com.zht.report.utils.TcUtil;


public class GenerateVehicleDetailReportJob extends Job
{
	private boolean completed = false;
	
	private TCComponentBOMLine targetBOMLine;
	private String filePath;

	private StringBuilder stringBuilder = new StringBuilder();
	
	private LogAppend logAppend;
	private String logFilePath;
	
	public GenerateVehicleDetailReportJob(String name, TCComponentBOMLine paramBOMLine, String paramFilePath) {
		super(name);
		this.targetBOMLine = paramBOMLine;
		this.filePath = paramFilePath;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		progressMonitor.beginTask(ZHTConstants.JOB_BEGINTASK_MSG, -1);
		
		try {
			this.logAppend = new LogAppend(System.getProperty("java.io.tmpdir"));
			
			Vector<VehicleDetailInfo> infoVector = new Vector<VehicleDetailInfo>();
			Map<String, VehicleDetailInfo> map = new HashMap<String, VehicleDetailInfo>();
			
			TCComponentBOMWindow bomWindow = this.targetBOMLine.window();
			bomWindow.clearCache();
//			bomWindow.unlock();
//			bomWindow.refresh();// 20180704，出现拒绝访问的问题，猜测是其他操作导致bomWindow被锁定
			
			TcUtil.willExpand(new TCComponentBOMLine[] { this.targetBOMLine });
			
			traverseBOM(this.targetBOMLine, map);
			
			if (this.stringBuilder.toString() == null || this.stringBuilder.toString().equals("")) 
			{
				for (Entry<String, VehicleDetailInfo> entry : map.entrySet()) 
				{
					VehicleDetailInfo detailInfo = entry.getValue();
					if (!detailInfo.getQuantity().equals("0")) {
						infoVector.add(detailInfo);
					}
				}
				
				Collections.sort(infoVector, new Comparator<VehicleDetailInfo>() 
				{
					@Override
					public int compare(VehicleDetailInfo o1, VehicleDetailInfo o2) {
						return o1.nodeId.compareTo(o2.nodeId);
					}
				});
				
				VehicleDetailInfo detailInfo = getVehicleDetailInfo(this.targetBOMLine);
				infoVector.insertElementAt(detailInfo, 0);
				generateVehicleDetailReport(infoVector);
				this.completed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			if (this.logAppend != null) {
				this.logAppend.close();
				this.logFilePath = this.logAppend.getLogFilePath();
			}
			progressMonitor.done();
		}
		
		return Status.OK_STATUS;
	}
	
	
	private boolean traverseBOM(TCComponentBOMLine paramBOMLine, Map<String, VehicleDetailInfo> paramMap) throws Exception
	{
		AIFComponentContext[] contexts = paramBOMLine.getChildren();
		for (AIFComponentContext context : contexts)
		{
			TCComponentBOMLine childBOMLine = (TCComponentBOMLine) context.getComponent();
			String structureFeature = childBOMLine.getProperty("Z9_Structure_Feature");
			if (!structureFeature.equals("N")) 
			{
				if (childBOMLine.getItemRevision() == null) {
					this.stringBuilder.append("节点 " + paramBOMLine + " 下存在无法读取节点.\n");
					this.logAppend.messageLog(MessageFormat.format(ZHTConstants.CANNOTREAD_MSG, paramBOMLine.toDisplayString()));
					continue;
				}
				
				String materialStatus = childBOMLine.getItemRevision().getProperty("z9_IR_Materialstatus");
				if (materialStatus != null && !materialStatus.equals("4")) 
				{
					if (isAbandonOrNotReleased(childBOMLine)) {
						continue;
					}
					
					String itemId = childBOMLine.getItem().getStringProperty("item_id");
					if (paramMap.containsKey(itemId)) {
						VehicleDetailInfo detailInfo = paramMap.get(itemId);
						int quantity = TcUtil.getTotalQuantity(childBOMLine);
						int totalQuantity = quantity + Integer.valueOf(detailInfo.quantity);
						detailInfo.setQuantity(totalQuantity + "");
					} else {
						VehicleDetailInfo detailInfo = getVehicleDetailInfo(childBOMLine);
						paramMap.put(itemId, detailInfo);
					}
					
					traverseBOM(childBOMLine, paramMap);
				}
			}
		}
		return true;
	}
	
	private boolean isAbandonOrNotReleased(TCComponentBOMLine paramBOMLine) throws TCException
	{
		if (paramBOMLine.getItemRevision() == null) {
			this.stringBuilder.append("节点 " + paramBOMLine.parent() + " 下存在无法读取节点.\n");
			this.logAppend.messageLog(MessageFormat.format(ZHTConstants.CANNOTREAD_MSG, paramBOMLine.parent().toDisplayString()));
			return true;
		}
		
		TCComponentItemRevision itemRevision = paramBOMLine.getItemRevision();
		String itemType = itemRevision.getItem().getType();
		String[] notCheckTypes = TcUtil.getPrefStringValues("Z9_N_Release_type");
		if (!Utilities.contains(itemType, notCheckTypes)) 
		{
			TCComponent[] statuscComponents = itemRevision.getReferenceListProperty("release_status_list");
			if (statuscComponents != null && statuscComponents.length > 0)
			{
				for (TCComponent tcComponent : statuscComponents) 
				{
					String statusName = tcComponent.getProperty("object_name");
					if (statusName.equals("Z9_Discard") || statusName.equals("D"))
					{
						this.stringBuilder.append("节点 " + paramBOMLine + " 已废弃.\n");
						this.logAppend.messageLog(MessageFormat.format(ZHTConstants.ABANDON_MSG, paramBOMLine.toDisplayString()));
						return true;
					}
				}
			} else {
				this.stringBuilder.append("节点 " + paramBOMLine + " 未发布.\n");
				this.logAppend.messageLog(MessageFormat.format(ZHTConstants.NOTRELEASED_MSG, paramBOMLine.toDisplayString()));
				return true;
			}
		}
		
		return false;
	}
	
	
	private VehicleDetailInfo getVehicleDetailInfo(TCComponentBOMLine paramBOMLine) throws Exception
	{
		TCComponentItemRevision itemRevision = paramBOMLine.getItemRevision();
		String drawingNo = ReportUtil.getDrawingNo(itemRevision);

		TCComponentUser user = (TCComponentUser) itemRevision.getTCProperty("owning_user").getReferenceValue();
		TCComponentGroup group = (TCComponentGroup) itemRevision.getTCProperty("owning_group").getReferenceValue();
		
		VehicleDetailInfo detailInfo = new VehicleDetailInfo();
		
//		String nodeId = itemRevision.getStringProperty("item_id");
		// 2018-11-06优化属性取值，减少访问数据库次数
		String[] propNames = { "item_id", "z9_IR_Techcode", "object_name", "z9_IR_Unit", "z9_IR_Car_status",
				"z9_IR_Inventory", "z9_IR_Materialstatus", "z9_IR_Material", "z9_IR_Weight", "object_desc",
				"item_revision_id" };
		String[] propValues = itemRevision.getProperties(propNames);
		if (propValues != null && propValues.length == 11) {
			detailInfo.setNodeId(propValues[0]);
			detailInfo.setTechCode(propValues[1]);
			detailInfo.setNodeName(propValues[2]);
			detailInfo.setUnit(propValues[3]);
			detailInfo.setCarStatus(propValues[4]);
			detailInfo.setInventory(propValues[5]);
			detailInfo.setMaterialstatus(propValues[6]);
			detailInfo.setMaterial(propValues[7]);
			detailInfo.setWeight(propValues[8]);
			detailInfo.setRemark(propValues[9]);
			detailInfo.setNodeRevId(propValues[10]);
		}
		
//		detailInfo.setNodeId(nodeId);
//		detailInfo.setTechCode(itemRevision.getProperty("z9_IR_Techcode"));
//		detailInfo.setNodeName(itemRevision.getStringProperty("object_name"));
		detailInfo.setResponsible(user.getTCProperty("person").getReferenceValue().getProperty("PA6"));
		detailInfo.setUserName(user.getProperty("person"));
		detailInfo.setGroupDesc(group.getProperty("description"));
		
		detailInfo.setQuantity(TcUtil.getTotalQuantity(paramBOMLine) + "");
//		detailInfo.setUnit(itemRevision.getProperty("z9_IR_Unit"));
		
		detailInfo.setReplacePartId(ReportUtil.getReplacePartId(paramBOMLine));
		detailInfo.setStructureFeature(paramBOMLine.getProperty("Z9_Structure_Feature"));
		
		String itemRevType = itemRevision.getType();
		detailInfo.setCarStructure(ReportUtil.getCarStructure(paramBOMLine, itemRevType));
		detailInfo.setCarType(ReportUtil.getCarType(group));
		
//		detailInfo.setCarStatus(itemRevision.getProperty("z9_IR_Car_status"));
//		detailInfo.setInventory(itemRevision.getProperty("z9_IR_Inventory"));
//		detailInfo.setMaterialstatus(itemRevision.getProperty("z9_IR_Materialstatus"));
//		detailInfo.setMaterial(itemRevision.getProperty("z9_IR_Material"));
//		detailInfo.setWeight(itemRevision.getProperty("z9_IR_Weight"));
		
		detailInfo.setDrawingNo(drawingNo);
		detailInfo.setModelName(ReportUtil.getModelName(itemRevision));
		
//		detailInfo.setRemark(itemRevision.getProperty("object_desc"));
//		detailInfo.setNodeRevId(itemRevision.getStringProperty("item_revision_id"));
		
		detailInfo.setTotalMap(getNamedRefString(itemRevision, "Z9_Total_map")); // 共图件
		detailInfo.setAboutRel(getNamedRefString(itemRevision, "Z9_about_rel"));
		
		return detailInfo;
	}
	
	
	// 2018-07-12 added
	private String getNamedRefString(TCComponentItemRevision itemRevision, String relation) throws TCException
	{
		StringBuilder stringBuilder = new StringBuilder("");
		TCComponent[] components = itemRevision.getRelatedComponents(relation);
		if (components != null && components.length > 0) 
		{
			for (int i = 0; i < components.length; i++) 
			{
				if (components[i] instanceof TCComponentItemRevision) 
				{
					TCComponentItemRevision partRevision = (TCComponentItemRevision) components[i];
					
					TCComponent[] statuscComponents = partRevision.getReferenceListProperty("release_status_list");
					if (statuscComponents != null && statuscComponents.length > 0)
					{
						boolean isAbandon= false;
						for (TCComponent tcComponent : statuscComponents) {
							String statusName = tcComponent.getProperty("object_name");
							if (statusName.equals("Z9_Discard") || statusName.equals("D")) {
								isAbandon = true;
							}
						}
						
						if (!isAbandon) 
						{
							if (relation.equals("Z9_about_rel")) {
								stringBuilder.append(ReportUtil.getModelName(partRevision));
								if (i < components.length - 1) {
									stringBuilder.append(";");
								}
							} else if (relation.equals("Z9_Total_map")) {
								stringBuilder.append(getDrawingNo(partRevision));
								if (i < components.length - 1) {
									stringBuilder.append(";");
								}
							}
						}
					}
				} 
				else if (components[i] instanceof TCComponentItem)
				{
					TCComponentItem part = (TCComponentItem) components[i];
					TCComponentItemRevision[] releasedItemRevisions = part.getReleasedItemRevisions();
					if (releasedItemRevisions != null && releasedItemRevisions.length > 0) 
					{
						TCComponentItemRevision latestReleasedItemRevision = releasedItemRevisions[0];
						if (!ReportUtil.isAbandon(latestReleasedItemRevision)) 
						{
							if (relation.equals("Z9_about_rel")) {
								stringBuilder.append(ReportUtil.getModelName(latestReleasedItemRevision));
								if (i < components.length - 1) {
									stringBuilder.append(";");
								}
							} else if (relation.equals("Z9_Total_map")) {
								stringBuilder.append(getDrawingNo(latestReleasedItemRevision));
								if (i < components.length - 1) {
									stringBuilder.append(";");
								}
							}
						}
					}
				}
			}		
		}
		
		String resultStr = stringBuilder.toString();
		if (resultStr.startsWith(";")) {
			resultStr = resultStr.substring(1);
		}
		
		return resultStr;
	}
	
	private String getDrawingNo(TCComponentItemRevision itemRevision) throws TCException
	{
		StringBuilder stringBuilder = new StringBuilder();
		TCComponent[] components = itemRevision.getRelatedComponents();
		for (int i = 0; i < components.length; i++) 
		{
			if (components[i] instanceof TCComponentDataset)
			{
				TCComponentDataset dataset = (TCComponentDataset) components[i];
				TCComponentTcFile[] tcFiles = dataset.getTcFiles();
				if (tcFiles != null && tcFiles.length > 0) 
				{
					for (TCComponentTcFile tcFile : tcFiles) {
						String originalFileName = tcFile.getProperty("original_file_name");
						stringBuilder.append(originalFileName).append(";");
					}
				}
			}
		}
		
		String drawingNo = stringBuilder.toString();
		if (drawingNo.endsWith(";")) {
			drawingNo = drawingNo.substring(0, drawingNo.lastIndexOf(";"));
		}
		
		return drawingNo;
	}
	
	private void generateVehicleDetailReport(Vector<VehicleDetailInfo> infoVector) throws Exception
	{
		TCComponentDataset templateDataset = TcUtil.findTemplateDataset("MSExcelX", ZHTConstants.VEHICLEDTAILRPORT_DSNAME);
		if (templateDataset == null) {
			throw new Exception(MessageFormat.format(ZHTConstants.DATASETNOTFOUND_MSG, ZHTConstants.VEHICLEDTAILRPORT_DSNAME));
		}
		
		TCComponentTcFile[] tcFiles = templateDataset.getTcFiles();
		if (tcFiles == null || tcFiles.length < 1) {
			throw new Exception(MessageFormat.format(ZHTConstants.NOFMSFILE_MSG, ZHTConstants.VEHICLEDTAILRPORT_DSNAME));
		}
		File templateFile = tcFiles[0].getFile(System.getenv("Temp"));
		
		ComThread.InitSTA();
		ActiveXComponent excelApp = JacobUtil.getExcelApp();
		Dispatch workBook = null;
		
		try {
			workBook = JacobUtil.getWorkBook(excelApp, templateFile);
			Dispatch sheets = JacobUtil.getSheets(workBook);
			Dispatch firstSheet = JacobUtil.getSheet(sheets, Integer.valueOf(1));
			
			for (int i = 0; i < infoVector.size(); i++) 
			{
				VehicleDetailInfo detailInfo = infoVector.get(i);
				
				JacobUtil.writeCellData(firstSheet, "A" + (i+2), i + 1);
				JacobUtil.writeCellData(firstSheet, "B" + (i+2), detailInfo.nodeId);
				JacobUtil.writeCellData(firstSheet, "C" + (i+2), detailInfo.techCode);
				JacobUtil.writeCellData(firstSheet, "D" + (i+2), detailInfo.nodeName);
				JacobUtil.writeCellData(firstSheet, "E" + (i+2), detailInfo.responsible);
				JacobUtil.writeCellData(firstSheet, "F" + (i+2), detailInfo.userName);
				JacobUtil.writeCellData(firstSheet, "G" + (i+2), detailInfo.groupDesc);
				
				JacobUtil.writeCellData(firstSheet, "H" + (i+2), detailInfo.quantity);
				JacobUtil.writeCellData(firstSheet, "I" + (i+2), detailInfo.unit);
				JacobUtil.writeCellData(firstSheet, "J" + (i+2), detailInfo.replacePartId);
				JacobUtil.writeCellData(firstSheet, "L" + (i+2), detailInfo.structureFeature);
				JacobUtil.writeCellData(firstSheet, "N" + (i+2), detailInfo.carStructure);
				JacobUtil.writeCellData(firstSheet, "O" + (i+2), detailInfo.carType);
				JacobUtil.writeCellData(firstSheet, "P" + (i+2), detailInfo.carStatus);
				
				JacobUtil.writeCellData(firstSheet, "Q" + (i+2), detailInfo.inventory);
				JacobUtil.writeCellData(firstSheet, "R" + (i+2), detailInfo.materialstatus);
				JacobUtil.writeCellData(firstSheet, "S" + (i+2), detailInfo.material);
				JacobUtil.writeCellData(firstSheet, "T" + (i+2), detailInfo.weight);
				JacobUtil.writeCellData(firstSheet, "W" + (i+2), detailInfo.drawingNo);
				JacobUtil.writeCellData(firstSheet, "X" + (i+2), detailInfo.modelName);
				
				if (i > 0 && detailInfo.materialstatus.equals("")) {
					JacobUtil.setCellColor(firstSheet, "R" + (i+2), 255);
				}
				
				JacobUtil.writeCellData(firstSheet, "AB" + (i+2), detailInfo.remark);
				JacobUtil.writeCellData(firstSheet, "AC" + (i+2), detailInfo.nodeRevId);
				
				JacobUtil.writeCellData(firstSheet, "AJ" + (i+2), detailInfo.totalMap);
				JacobUtil.writeCellData(firstSheet, "AK" + (i+2), detailInfo.aboutRel);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		} finally {
			JacobUtil.closeExcelApp(excelApp, workBook);
			
			File file = new File(this.filePath);
			if (file.exists()) {
				file.delete();
			}
			templateFile.renameTo(file);
		}
	}

	public boolean isCompleted() {
		return completed;
	}

	public String getLogFilePath() {
		return logFilePath;
	}
	
}