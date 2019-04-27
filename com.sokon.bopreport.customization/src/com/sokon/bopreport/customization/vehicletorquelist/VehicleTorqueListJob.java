package com.sokon.bopreport.customization.vehicletorquelist;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.sokon.bopreport.customization.util.FileUtility;
import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOP;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;

public class VehicleTorqueListJob extends Job
{
	private TCComponentBOMLine targetBOMLine;
	private int languageType;
	private Registry appReg;
	private boolean flag = false;
	private SimpleDateFormat dateFormat;
	
	private TCComponentDataset reportDataset; // added by zhoutong, 2018-09-28

	public VehicleTorqueListJob(String name, TCComponentBOMLine targetBOMLine, int languageType) 
	{
		super(name);
		this.targetBOMLine = targetBOMLine;
		this.languageType = languageType;
		this.appReg = Registry.getRegistry(this);
		this.dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		File workingDirFile = null;
		try 
		{
			progressMonitor.beginTask(this.appReg.getString("VehicleTorqueList.Message"), -1);
			
			workingDirFile = FileUtility.createTempDirectory("VehicleTorqueList");
			String workingDir = workingDirFile.getAbsolutePath();
			
			TcUtil.willExpand(new TCComponentBOMLine[] {this.targetBOMLine});
			
			Vector<RowInfo> partRowInfoVector = new Vector<RowInfo>();
			
			String[] partTypes = TcUtil.getPrefStringValues("S4CUST_VehicleTorqueList_PartType");
			
			traverseBOMLine(this.targetBOMLine, partRowInfoVector, partTypes);
			
			File templateFile = null;
//			boolean updateStatus = false;
			TCComponentItem vehicleTorqueList = findVehicleTorqueList(this.targetBOMLine);
			/*if (vehicleTorqueList != null)
			{
				TCComponentItemRevision vehicleTorqueListRev = vehicleTorqueList.getLatestItemRevision();
				
				TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(vehicleTorqueListRev, "IMAN_specification", "MSExcelX", "Vehicle Torque List");
				if (ds != null)
				{
					File[] files = ds.getFiles("excel", workingDir);
					if (files != null && files.length > 0)
					{
						templateFile = files[0];
						updateStatus = true;
					}
				}
			}*/
			
			if (templateFile == null)
				templateFile = getTemplateFile(workingDir);
			
			if (templateFile == null)
				throw new Exception(this.appReg.getString("VehicleTorqueList.TemplateNotFound.Message"));
			
			// added by zhoutong, 2018-09-27
			int rowCount = Integer.valueOf(this.appReg.getString("VehicleTorqueList.Template.RowCount"));
			if (rowCount < 1) {
				throw new Exception(MessageFormat.format(this.appReg.getString("InvalidRowCountConfigurationForTemplate.Msg"), this.appReg.getString("VehicleTorqueList.Template")));
			}
			
			String professionType = TcUtil.getProfession(this.targetBOMLine.getItemRevision().getType());
			
			String chexingVariant = this.targetBOMLine.getItemRevision().getProperty("s4_AT_EngineeringModel");
			
			
			ActiveXComponent excel = null;
			Dispatch workbook = null;
			File saveFile = null;
			
			try 
			{
				ComThread.InitSTA();
				excel = new ActiveXComponent("Excel.Application");
				excel.setProperty("Visible", new Variant(false));
				Dispatch workbooks = excel.getProperty("Workbooks").toDispatch();
				workbook = Dispatch.invoke(workbooks, 
											"Open", Dispatch.Method,
											new Object[]{templateFile.getAbsolutePath(), new Variant(false),
														new Variant(false)}, 
											new int[1]).toDispatch();
				
				Dispatch sheets = Dispatch.get(workbook, "sheets").toDispatch();
				
				Dispatch sheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[]{1},new int[1]).toDispatch();
				
				TcUtil.writeData(sheet, "D2", chexingVariant);
				
				TcUtil.writeData(sheet, "J2", professionType);
				
				TcUtil.writeData(sheet, "J3", this.dateFormat.format(new Date()));
				
//				int rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start1");
//				int rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end1");
//				int rowCount = rowEndIndex - rowStartIndex + 1;
				int addRowCount = partRowInfoVector.size() - rowCount;
				if (addRowCount > 0)
				{
					TcUtil.copyRow(sheet, 5, addRowCount);
				}
//				int rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end1");
//				String rangeName = "A" + rowStartIndex + ":K" + rowEndIndex1;
//				TcUtil.clearContents(sheet, rangeName);
				
				int startRow = 5;
				for (int i = 0; i < partRowInfoVector.size(); i++) 
				{
					RowInfo partRowInfo = partRowInfoVector.get(i);
					TCComponentBOMLine partBOMLine = partRowInfo.getPartBOMLine();
					TCComponentBOMLine opBOMLine = partRowInfo.getOpBOMLine();
					TCComponentBOMLine workAreaBOMLine = partRowInfo.getWorkAreaBOMLine();
					
					TcUtil.writeData(sheet, "A" + (startRow + i), String.valueOf(i + 1));
					
					if (workAreaBOMLine != null)
					{
//						TcUtil.writeData(sheet, "C" + (startRow + i), workAreaBOMLine.getProperty("bl_item_item_id"));
						// 修改工位的取值， modofied by tzhou, 2018-11-08
						String workAreaId = workAreaBOMLine.getProperty("bl_item_item_id");
						String station = TcUtil.getLast7String(workAreaId + TcUtil.getProcResArea(opBOMLine));
						TcUtil.writeData(sheet, "C" + (startRow + i), station);
					}
					
					if (opBOMLine != null)
					{
						String opName = "";
						if (this.languageType == 0)
						{
							opName = opBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
							
						}else if (this.languageType == 1)
						{
							opName = opBOMLine.getProperty("bl_rev_object_name");
						}else if (this.languageType == 2)
						{
							opName = opBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + opBOMLine.getProperty("bl_rev_object_name");
						}
						
						TcUtil.writeData(sheet, "D" + (startRow + i), opName);
						
					}
					
					if (partBOMLine != null)
					{
						TcUtil.writeData(sheet, "I" + (startRow + i), partBOMLine.getProperty("bl_item_item_id"));
						TcUtil.writeData(sheet, "J" + (startRow + i), partBOMLine.getProperty("S4_NT_Torque"));
						TcUtil.writeData(sheet, "K" + (startRow + i), partBOMLine.getProperty("S4_NT_TorqueRemarks"));
						
						String keyTorque = "";
//						String key = partBOMLine.getProperty("S4_NT_KeyTorque");
						String key = partBOMLine.getStringProperty("S4_NT_KeyTorque"); // modified by zhoutong, 2018-09-28
						if (key.equals("Y"))
							keyTorque = "Yes";
						TcUtil.writeData(sheet, "B" + (startRow + i), keyTorque);
					}
					
//					startRow++;
				}
				
				Dispatch.call(workbook, "Save");
				
				saveFile = templateFile;
				
			} finally 
			{
				if (workbook != null)
					Dispatch.call(workbook, "Close",new Variant(false));
				if (excel != null)
					excel.invoke("Quit",new Variant[]{});
				ComThread.Release();
			}
			
			if (saveFile != null)
			{
				if (vehicleTorqueList == null)
				{
					File resultFile = new File(saveFile.getParent(), "VehicleTorqueList" + TcUtil.getSuffix(saveFile.getName()));

					templateFile.renameTo(resultFile);
					
					vehicleTorqueList = TcUtil.createItem("", "", "S4_IT_ProcessDoc", "Vehicle Torque List", "", null);
					try 
					{
						vehicleTorqueList.getLatestItemRevision().lock();
//						vehicleTorqueList.getLatestItemRevision().setProperty("s4_AT_DocumentType", "整车力矩表");
						vehicleTorqueList.getLatestItemRevision().setProperty("s4_AT_DocumentType", "VehicleTorqueList");
						vehicleTorqueList.getLatestItemRevision().save();
					} finally 
					{
						vehicleTorqueList.getLatestItemRevision().unlock();
					}
					
					this.targetBOMLine.getItemRevision().add("IMAN_reference", vehicleTorqueList);
					
					TCComponentDataset ds = TcUtil.createDataset("Vehicle Torque List", "", "MSExcelX");
					
					TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
					
					vehicleTorqueList.getLatestItemRevision().add("IMAN_specification", ds);
					
					this.reportDataset = ds;
				}else
				{
					TCComponentItemRevision vehicleTorqueListRev = vehicleTorqueList.getLatestItemRevision();
					TCComponent[] status = vehicleTorqueListRev.getReferenceListProperty("release_status_list");
					
					if ((status != null) && (status.length > 0))
					{
						TCComponentItemRevision lastestvehicleTorqueListRev = vehicleTorqueListRev.saveAs(vehicleTorqueListRev.getItem().getNewRev(), "Vehicle Torque List", "", false, null);
						
//						TCComponentDataset ds = TcUtil.createDataset("Vehicle Torque List", "", "MSExcelX");
						TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(lastestvehicleTorqueListRev, "IMAN_specification", "MSExcelX", "Vehicle Torque List");
						if (ds == null) {
							ds = TcUtil.createDataset("Vehicle Torque List", "", "MSExcelX");
						} else {
							TcUtil.removeAllFilesFromDataset(ds);
						}
//						if (updateStatus)
//						{
//							TcUtil.importFileToDataset(ds, saveFile, "MSExcelX", "excel");
//						}else
//						{
							File resultFile = new File(saveFile.getParent(), "VehicleTorqueList" + TcUtil.getSuffix(saveFile.getName()));

							saveFile.renameTo(resultFile);
							TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
//						}
						
						lastestvehicleTorqueListRev.add("IMAN_specification", ds);
						
						this.reportDataset = ds;
					}else
					{
						TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(vehicleTorqueListRev, "IMAN_specification", "MSExcelX", "Vehicle Torque List");
						
						if (ds == null)
						{
							ds = TcUtil.createDataset("Vehicle Torque List", "", "MSExcelX");
							
							vehicleTorqueListRev.add("IMAN_specification", ds);
						}
						TcUtil.removeAllFilesFromDataset(ds);
						
						File resultFile = new File(saveFile.getParent(), "VehicleTorqueList" + TcUtil.getSuffix(saveFile.getName()));

						saveFile.renameTo(resultFile);
						
						TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
						
						this.reportDataset = ds;
					}
				}
			}
			
			this.flag = true;
			
		} catch (Exception e) 
		{
			MessageBox.post(e);
		} finally
		{
			progressMonitor.done();
		}
		return Status.OK_STATUS;
	}
	
	public boolean isFlag()
	{
		return this.flag;
	}
	
	private void traverseBOMLine(TCComponentBOMLine paramBOMLine, Vector<RowInfo> partRowInfoVector, String[] partTypes) throws Exception
	{
		AIFComponentContext[] aaifContext = paramBOMLine.getChildren();
		for (int i = 0; i < aaifContext.length; i++) 
		{
			TCComponentBOMLine opBOMLine = (TCComponentBOMLine) aaifContext[i].getComponent();
			if (opBOMLine.getItem() instanceof TCComponentMEOP)
			{
				AIFComponentContext[] meopContexts = opBOMLine.getChildren();
				for (int j = 0; j < meopContexts.length; j++) 
				{
					TCComponentBOMLine partBOMLine = (TCComponentBOMLine)meopContexts[j].getComponent();
					TCComponentItemRevision partRev = partBOMLine.getItemRevision();
//					if (Utilities.contains(partRev.getType(), partTypes) && (!partRev.getProperty("S4_NT_Torque").equals("")))
					if (Utilities.contains(partRev.getType(), partTypes) && (!partBOMLine.getProperty("S4_NT_Torque").equals(""))) // modified by zhoutong, 2018-09-28
					{
						// 过滤掉对象类型为S4_IT_PartRevision且其s4_AT_IsColorPart属性值为“COL/SURF”（LOV真实值）的对象，added by zhoutong, 2018-11-08
						if (partRev.isTypeOf("S4_IT_PartRevision") && partRev.getStringProperty("s4_AT_IsColorPart").equals("COL/SURF")) {
							continue;
						}
						
						TCComponentBOMLine workAreaBOMLine = null;
						AIFComponentContext[] childContexts = opBOMLine.parent().getChildren();
						for (int k = 0; k < childContexts.length; k++) 
						{
							TCComponentBOMLine childBOMLine = (TCComponentBOMLine)childContexts[k].getComponent();
							if (childBOMLine.getStringProperty("bl_occ_type").equals("MEWorkArea"))
							{
								workAreaBOMLine = childBOMLine;
							}
						}
						
						RowInfo rowInfo = new RowInfo(partBOMLine, opBOMLine, workAreaBOMLine);
						
						partRowInfoVector.add(rowInfo);
					}
				}
			}else
			{
				traverseBOMLine(opBOMLine, partRowInfoVector, partTypes);
			}
		}
	}
	
	private File getTemplateFile(String workingDir)
	{
		File file = null;
		
		try 
		{
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(this.appReg.getString("VehicleTorqueList.Template"), "MSExcelX");
			if (templateDataset != null) 
			{
				File[] files = templateDataset.getFiles("excel", workingDir);
				if ((files != null) && (files.length > 0))
					file = files[0];
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		
		return file;
	}
	
	private TCComponentItem findVehicleTorqueList(TCComponentBOMLine opBOMLine)
	{
		TCComponentItem vehicleTorqueList = null;
		try 
		{
			TCComponent[] comps = opBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < comps.length; i++) 
			{
//				if (comps[i].isTypeOf("S4_IT_ProcessDoc") && (((TCComponentItem)comps[i]).getLatestItemRevision().getProperty("s4_AT_DocumentType")).equals("整车力矩表"))
				if (comps[i].isTypeOf("S4_IT_ProcessDoc") 
						&& (((TCComponentItem)comps[i]).getLatestItemRevision().getStringProperty("s4_AT_DocumentType")).equals("VehicleTorqueList"))
				{
					TCComponentItem item = (TCComponentItem)comps[i];
					
					vehicleTorqueList = item;
				}
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return vehicleTorqueList;
	}

	public TCComponentDataset getReportDataset() {
		return reportDataset;
	}
	
}
