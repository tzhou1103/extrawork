package com.sokon.bopreport.customization.weldingprocedurespecification;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.sokon.bopreport.customization.generalassemblyprocessdoc.FileDataset;
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

public class NewWeldingProcedureSpecificationJob extends Job 
{

	private Registry appReg;
	private TCComponentBOMLine baStationBOMLine;
	private int languageType;
	private String chexingVariant;
	
	private boolean flag = false;
	
	public NewWeldingProcedureSpecificationJob(String name, TCComponentBOMLine baStationBOMLine, int languageType, String chexingVariant) 
	{
		super(name);
		this.appReg = Registry.getRegistry(this);
		this.baStationBOMLine = baStationBOMLine;
		this.languageType = languageType;
		this.chexingVariant = chexingVariant;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		File workingDirFile = null;
		
		try 
		{
			progressMonitor.beginTask(this.appReg.getString("WeldingProcedureSpecification.Message"), -1);
			
			workingDirFile = FileUtility.createTempDirectory("WeldingProcedureSpecification");
			String workingDir = workingDirFile.getAbsolutePath();
			
			File templateFile = null;
			boolean updateStatus = false;
			TCComponentItem weldingProcedureItem = findWeldingProcedureSpecification(this.baStationBOMLine);
			if (weldingProcedureItem != null)
			{
				TCComponentItemRevision weldingProcedureRev = weldingProcedureItem.getLatestItemRevision();
				
				TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(weldingProcedureRev, "IMAN_specification", "MSExcelX", "Welding Procedure Specification");
				if (ds != null)
				{
					File[] files = ds.getFiles("excel", workingDir);
					if (files != null && files.length > 0)
					{
						templateFile = files[0];
						updateStatus = true;
					}
				}
			}
			
			templateFile = getTemplateFile(workingDir);
			if (templateFile == null)
				throw new Exception(this.appReg.getString("WeldingProcedureSpecification.TemplateNotFound.Message"));
			
			String bastationID = this.baStationBOMLine.getProperty("bl_item_item_id");
			
			TCComponentBOMLine stationBOMLine = null;
			int workerCount = 0;
			Vector<TCComponentBOMLine> opBOMLineVector = new Vector<TCComponentBOMLine>();
			AIFComponentContext[] aaifContext = this.baStationBOMLine.getChildren();
			for (int i = 0; i < aaifContext.length; i++) 
			{
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine)aaifContext[i].getComponent();
				if (childBOMLine.getItem().isTypeOf("S4_IT_Station") && childBOMLine.getStringProperty("bl_occ_type").equals("MEWorkArea"))
				{
					stationBOMLine = childBOMLine;
				}else if (childBOMLine.getItem().isTypeOf("S4_IT_Worker"))
				{
					int count = 0;
					String quantityStr = childBOMLine.getProperty("bl_quantity");
					if ("".equals(quantityStr))
						count = 1;
					else
					{
						try 
						{
							count = Integer.parseInt(quantityStr);
						} catch (Exception e) 
						{
							count = 1;
						}
					}
					
					workerCount = workerCount + count;
				}else if (childBOMLine.getItem() instanceof TCComponentMEOP)
				{
					opBOMLineVector.add(childBOMLine);
				}
			}
			
			Vector<TCComponentBOMLine> jzsVector = new Vector<TCComponentBOMLine>();
			HashMap<TCComponentItem, BigDecimal> jzsMap = new HashMap<TCComponentItem, BigDecimal>();
			
			Vector<TCComponentBOMLine> avxVector = new Vector<TCComponentBOMLine>();
			HashMap<TCComponentItem, BigDecimal> avxMap = new HashMap<TCComponentItem, BigDecimal>();
			
			Vector<TCComponentBOMLine> stdPartVector = new Vector<TCComponentBOMLine>();
			HashMap<TCComponentItem, BigDecimal> stdPartMap = new HashMap<TCComponentItem, BigDecimal>();
			
			Vector<TCComponentBOMLine> pinchWeldVector = new Vector<TCComponentBOMLine>();
			Vector<TCComponentBOMLine> weldPointVector = new Vector<TCComponentBOMLine>();
			
			
			for (int i = 0; i < opBOMLineVector.size(); i++) 
			{
				TCComponentBOMLine opBOMLine = opBOMLineVector.get(i);
				aaifContext = opBOMLine.getChildren();
				for (int j = 0; j < aaifContext.length; j++) 
				{
					TCComponentBOMLine bomLine = (TCComponentBOMLine) aaifContext[j].getComponent();
					
					if (bomLine.getItem().isTypeOf("S4_IT_StdPart") && bomLine.getStringProperty("bl_occ_type").equals("MEConsumed"))
					{
						if (stdPartMap.containsKey(bomLine.getItem()))
						{
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							BigDecimal countBigDecimal = stdPartMap.get(bomLine.getItem()).add(new BigDecimal(quantity));
							stdPartMap.put(bomLine.getItem(), countBigDecimal);
						}else
						{
							stdPartVector.add(bomLine);
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							stdPartMap.put(bomLine.getItem(), new BigDecimal(quantity));
						}
					}else if (bomLine.getItem().isTypeOf("S4_IT_Part") && bomLine.getStringProperty("bl_occ_type").equals("MEConsumed"))
					{
						TCComponentItemRevision tempCompRev = (TCComponentItemRevision)TcUtil.getRelatedComponentByType(bomLine.getItemRevision(), "S4_REL_AuxiliaryPart", "S4_IT_AuxPartRevision");
						if (tempCompRev == null)
						{
							String isColorPart = bomLine.getItemRevision().getStringProperty("s4_AT_IsColorPart");
							if ("COL/SURF".equals(isColorPart))
							{
								continue;
							}
							
							if (stdPartMap.containsKey(bomLine.getItem()))
							{
								String quantity = bomLine.getProperty("Usage_Quantity");
								if ("".equals(quantity))
									quantity = "1";
								BigDecimal countBigDecimal = stdPartMap.get(bomLine.getItem()).add(new BigDecimal(quantity));
								stdPartMap.put(bomLine.getItem(), countBigDecimal);
							}else
							{
								stdPartVector.add(bomLine);
								String quantity = bomLine.getProperty("Usage_Quantity");
								if ("".equals(quantity))
									quantity = "1";
								stdPartMap.put(bomLine.getItem(), new BigDecimal(quantity));
							}
						}else
						{
							if (avxMap.containsKey(tempCompRev.getItem()))
							{
								String quantity = bomLine.getProperty("Usage_Quantity");
								if ("".equals(quantity))
									quantity = "1";
								BigDecimal countBigDecimal = avxMap.get(tempCompRev.getItem()).add(new BigDecimal(quantity));
								avxMap.put(tempCompRev.getItem(), countBigDecimal);
								
							}else
							{
								avxVector.add(bomLine);
								String quantity = bomLine.getProperty("Usage_Quantity");
								if ("".equals(quantity))
									quantity = "1";
								avxMap.put(tempCompRev.getItem(), new BigDecimal(quantity));
							}
						}
					}else if (Utilities.contains(bomLine.getItem().getType(), new String[] {"S4_IT_Tool", "S4_IT_Equipment", 
							"S4_IT_Fixture", "S4_IT_OtherTool", "S4_IT_PinchWeld"}))
					{
						if (jzsMap.containsKey(bomLine.getItem()))
						{
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							BigDecimal countBigDecimal = jzsMap.get(bomLine.getItem()).add(new BigDecimal(quantity));
							jzsMap.put(bomLine.getItem(), countBigDecimal);
						}else
						{
							jzsVector.add(bomLine);
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							jzsMap.put(bomLine.getItem(), new BigDecimal(quantity));
						}
						
						/**
						 * S4_IT_PinchWeld对象，将下面逻辑移动到此处
						 */
						if (bomLine.getItem().isTypeOf("S4_IT_PinchWeld"))
						{
							pinchWeldVector.add(bomLine);
						}
						
					}else if (bomLine.getItem().isTypeOf("S4_IT_AuxPart") && bomLine.getStringProperty("bl_occ_type").equals("MEConsumed"))
					{
						if (avxMap.containsKey(bomLine.getItem()))
						{
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							BigDecimal countBigDecimal = avxMap.get(bomLine.getItem()).add(new BigDecimal(quantity));
							avxMap.put(bomLine.getItem(), countBigDecimal);
						}else
						{
							avxVector.add(bomLine);
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							avxMap.put(bomLine.getItem(), new BigDecimal(quantity));
						}
					}else if (bomLine.getItem().isTypeOf("S4_IT_ProcessAux"))
					{
						if (avxMap.containsKey(bomLine.getItem()))
						{
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							BigDecimal countBigDecimal = avxMap.get(bomLine.getItem()).add(new BigDecimal(quantity));
							avxMap.put(bomLine.getItem(), countBigDecimal);
						}else
						{
							avxVector.add(bomLine);
							String quantity = bomLine.getProperty("Usage_Quantity");
							if ("".equals(quantity))
								quantity = "1";
							avxMap.put(bomLine.getItem(), new BigDecimal(quantity));
						}
					}
//					else if (bomLine.getItem().isTypeOf("S4_IT_PinchWeld"))
//					{
//						pinchWeldVector.add(bomLine);
//					}
					else if (bomLine.getItem().isTypeOf("WeldPoint"))
					{
						weldPointVector.add(bomLine);
					}
				}
			}
			
			Vector<FileDataset> imageFileVector = new Vector<FileDataset>();
			aaifContext = this.baStationBOMLine.getItemRevision().getChildren();
			for (int i = 0; i < aaifContext.length; i++) 
			{
				TCComponent comp = (TCComponent)aaifContext[i].getComponent();
				if ((comp instanceof TCComponentDataset) && comp.isTypeOf("S4_DA_FirstLevImage"))
				{
					TCComponentDataset ds = (TCComponentDataset)comp;
					File[] imageFiles = ds.getFiles("S4_Image", workingDir);
					if (imageFiles != null && imageFiles.length > 0)
					{
						imageFileVector.add(new FileDataset(ds, imageFiles[0]));
					}
				}
			}
			
			for (int k = 0; k < opBOMLineVector.size(); k++) 
			{
				aaifContext = opBOMLineVector.get(k).getItemRevision().getChildren();
				for (int i = 0; i < aaifContext.length; i++) 
				{
					TCComponent comp = (TCComponent)aaifContext[i].getComponent();
					if ((comp instanceof TCComponentDataset) && comp.isTypeOf("SnapShotViewData"))
					{
						TCComponentDataset ds = (TCComponentDataset)comp;
						File[] imageFiles = ds.getFiles("Image", workingDir);
						if (imageFiles != null && imageFiles.length > 0)
						{
							imageFileVector.add(new FileDataset(ds, imageFiles[0]));
						}
					}else if ((comp instanceof TCComponentDataset) && comp.isTypeOf("S4_DA_FirstLevImage"))
					{
						TCComponentDataset ds = (TCComponentDataset)comp;
						File[] imageFiles = ds.getFiles("S4_Image", workingDir);
						if (imageFiles != null && imageFiles.length > 0)
						{
							imageFileVector.add(new FileDataset(ds, imageFiles[0]));
						}
					}
				}
			}
			
			ActiveXComponent excel = null;
			Dispatch workbook = null;
			File saveFile = null;
			
			try 
			{
				ComThread.InitSTA();
				excel = new ActiveXComponent("Excel.Application");
				excel.setProperty("Visible", new Variant(false));
				Dispatch workbooks = excel.getProperty("Workbooks").toDispatch();
				workbook = Dispatch.invoke(workbooks, "Open", Dispatch.Method,
						new Object[] { templateFile.getAbsolutePath(), new Variant(false), new Variant(false) },
						new int[1]).toDispatch();
				Dispatch sheets = Dispatch.get(workbook, "sheets").toDispatch();
				
				Dispatch sheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[]{1},new int[1]).toDispatch();
				
				Dispatch.put(sheet, "name", bastationID);
				
				TcUtil.writeData(sheet, "variant1", this.chexingVariant);
				TcUtil.writeData(sheet, "variant2", this.chexingVariant);
				TcUtil.writeData(sheet, "variant3", this.chexingVariant);
				
				if (stationBOMLine != null)
				{
//					TcUtil.writeData(sheet, "postaddress1", this.baStationBOMLine.getProperty("bl_item_item_id"));
//					TcUtil.writeData(sheet, "workernumber1", String.valueOf(workerCount));
//					
//					TcUtil.writeData(sheet, "postaddress2", this.baStationBOMLine.getProperty("bl_item_item_id"));
//					TcUtil.writeData(sheet, "workernumber2", String.valueOf(workerCount));
//					
//					TcUtil.writeData(sheet, "postaddress3", this.baStationBOMLine.getProperty("bl_item_item_id"));
//					TcUtil.writeData(sheet, "workernumber3", String.valueOf(workerCount));
					
					// modified by zhoutong, 2018-10-18
					String postAddress = getPostAddress(this.baStationBOMLine);
					
					postAddress = postAddress + "0";
					
					if (postAddress.length() > 7)
					{
						postAddress = postAddress.substring(postAddress.length() - 7, postAddress.length());
					}
					
					TcUtil.writeData(sheet, "postaddress1", postAddress);
					TcUtil.writeData(sheet, "workernumber1", String.valueOf(workerCount));
					
					TcUtil.writeData(sheet, "postaddress2", postAddress);
					TcUtil.writeData(sheet, "workernumber2", String.valueOf(workerCount));
					
					TcUtil.writeData(sheet, "postaddress3", postAddress);
					TcUtil.writeData(sheet, "workernumber3", String.valueOf(workerCount));
				}
				
				//零部件Parts
				int rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start1");
				int rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end1");
				int rowCount = rowEndIndex - rowStartIndex + 1;
				int addRowCount = stdPartVector.size() - rowCount;
				if (addRowCount > 0)
				{
					TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
				}
				int rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end1");
				String rangeName = "A" + rowStartIndex + ":AU" + rowEndIndex1;
				TcUtil.clearContents(sheet, rangeName);
				
				int startRow = rowStartIndex;
				
				for (int j = 0; j < stdPartVector.size(); j++) 
				{
					TCComponentBOMLine stdPartBOMLine = stdPartVector.get(j);
					
					TcUtil.writeData(sheet, "A" + (startRow + j), String.valueOf(j + 1));
					
					String stdPartName = "";
					if (this.languageType == 0)
					{
						stdPartName = stdPartBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
						
					}else if (this.languageType == 1)
					{
						stdPartName = stdPartBOMLine.getProperty("bl_rev_object_name");
						
					}else if (this.languageType == 2)
					{
						stdPartName = stdPartBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + stdPartBOMLine.getProperty("bl_rev_object_name");
					}
					TcUtil.writeData(sheet, "G" + (startRow + j), stdPartName);
					
					TcUtil.writeData(sheet, "AK" + (startRow + j), stdPartBOMLine.getProperty("bl_item_item_id"));
					
					String countStr = stdPartMap.get(stdPartBOMLine.getItem()).toString();
					TcUtil.writeData(sheet, "AR" + (startRow + j), countStr);
					
				}
				
				//工艺设备及工具Technology Equipments and Tools
				rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start2");
				rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end2");
				rowCount = rowEndIndex - rowStartIndex + 1;
				addRowCount = jzsVector.size() - rowCount;
				if (addRowCount > 0)
				{
					TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
				}
				rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end2");
				rangeName = "A" + rowStartIndex + ":AU" + rowEndIndex1;
				TcUtil.clearContents(sheet, rangeName);
				
				startRow = rowStartIndex;
				
				for (int j = 0; j < jzsVector.size(); j++) 
				{
					TCComponentBOMLine jzsBOMLine = jzsVector.get(j);
					
					TcUtil.writeData(sheet, "A" + (startRow + j), String.valueOf(j + 1));
					
					String jzsName = "";
					if (this.languageType == 0)
					{
						jzsName = jzsBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
						
					}else if (this.languageType == 1)
					{
						jzsName = jzsBOMLine.getProperty("bl_rev_object_name");
					}else if (this.languageType == 2)
					{
						jzsName = jzsBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + jzsBOMLine.getProperty("bl_rev_object_name");
					}
					
					TcUtil.writeData(sheet, "G" + (startRow + j), jzsName);
					
					TcUtil.writeData(sheet, "AK" + (startRow + j), jzsBOMLine.getItemRevision().getProperty("s4_AT_SpecificationModel"));
					
					String countStr = jzsMap.get(jzsBOMLine.getItem()).toString();
					TcUtil.writeData(sheet, "AR" + (startRow + j), countStr);
				}
				
				//工艺辅料Process Auxiliary
				rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start3");
				rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end3");
				rowCount = rowEndIndex - rowStartIndex + 1;
				addRowCount = avxVector.size() - rowCount;
				if (addRowCount > 0)
				{
					TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
				}
				rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end3");
				rangeName = "A" + rowStartIndex + ":AU" + rowEndIndex1;
				TcUtil.clearContents(sheet, rangeName);
				
				startRow = rowStartIndex;
				
				for (int j = 0; j < avxVector.size(); j++) 
				{
					TCComponentBOMLine avxBOMLine = avxVector.get(j);
					
					TCComponentItemRevision partRev = null;
					if (avxBOMLine.getItem().isTypeOf("S4_IT_Part"))
					{
						partRev = (TCComponentItemRevision)TcUtil.getRelatedComponentByType(avxBOMLine.getItemRevision(), "S4_REL_AuxiliaryPart", "S4_IT_AuxPartRevision");
					}
					
					TcUtil.writeData(sheet, "A" + (startRow + j), String.valueOf(j + 1));
					
					String avxName = "";
					if (this.languageType == 0)
					{
						if (partRev != null)
							avxName = partRev.getProperty("s4_CAT_ChineseName");
						else
							avxName = avxBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
						
					}else if (this.languageType == 1)
					{
						if (partRev != null)
							avxName = partRev.getProperty("object_name");
						else
							avxName = avxBOMLine.getProperty("bl_rev_object_name");
					}else if (this.languageType == 2)
					{
						
						if (partRev != null)
							avxName = partRev.getProperty("s4_CAT_ChineseName") + "\n" + partRev.getProperty("object_name");
						else
							avxName = avxBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + avxBOMLine.getProperty("bl_rev_object_name");
					}
					
					TcUtil.writeData(sheet, "G" + (startRow + j), avxName);
					
					String xinghao = "";
					if (partRev != null)
					{
						xinghao = partRev.getProperty("s4_AT_AuxiliarySpec");
					}
					else if (avxBOMLine.getItem().isTypeOf("S4_IT_ProcessAux"))
					{
						xinghao = avxBOMLine.getProperty("s4_BAT_SpecificationModel");
					}else
						xinghao = avxBOMLine.getProperty("s4_BAT_AuxiliarySpec");
					
					TcUtil.writeData(sheet, "AK" + (startRow + j), xinghao);
					
					String countStr = "";
					if (partRev != null)
						countStr = avxMap.get(partRev.getItem()).toString();
					else
					{
						countStr = avxMap.get(avxBOMLine.getItem()).toString();
					}
					TcUtil.writeData(sheet, "AR" + (startRow + j), countStr);
				}
				
				//
				rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start4");
				rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end4");
				rowCount = rowEndIndex - rowStartIndex + 1;
				int addRowCount1 = pinchWeldVector.size() - rowCount;
				int addRowCount2 = weldPointVector.size() - rowCount;
				addRowCount = Math.max(addRowCount1, addRowCount2);
				if (addRowCount > 0)
				{
					TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
				}
				rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end4");
				rangeName = "A" + rowStartIndex + ":AK" + rowEndIndex1;
				TcUtil.clearContents(sheet, rangeName);
				
				int maxHangshu = Math.max(pinchWeldVector.size(), weldPointVector.size());
				
				startRow = rowStartIndex;
				
				for (int j = 0; j < maxHangshu; j++) 
				{
					if (j < pinchWeldVector.size())
					{
						TCComponentBOMLine pinchWeldBOMLine = pinchWeldVector.get(j);
						
						TcUtil.writeData(sheet, "A" + (startRow + j), pinchWeldBOMLine.getProperty("s4_BAT_ToolNumber"));
					}
					
					if (j < weldPointVector.size())
					{
						TCComponentBOMLine weldPointBOMLine = weldPointVector.get(j);
						TcUtil.writeData(sheet, "J" + (startRow + j), weldPointBOMLine.getProperty("bl_item_item_id"));
					}
				}
				
				for (int k = 0; k < imageFileVector.size(); k++) 
				{
					FileDataset fileDataset = imageFileVector.get(k);
					TCComponentDataset ds = fileDataset.getDs();
					File imageFile = fileDataset.getImageFile();
					String dsName = ds.getProperty("object_name");
					
					Dispatch shapeDispatch = TcUtil.getShapeDispatch(sheet, dsName);
					if (shapeDispatch == null)
					{
						Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { "tupian" }, new int[1]).toDispatch();
						if (cell == null)
							throw new Exception("模板中未找到名称为[" + "tupian" + "]的单元格，请联系管理员！");
						Dispatch.call(cell, "Select"); //在工作表中，定位需要插入图片的具体位置
						Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
						Dispatch pic = Dispatch.call(select, "Insert", imageFile.getAbsolutePath()).toDispatch();
						Dispatch.put(pic, "name", dsName);
					}else
					{
						Variant height = Dispatch.get(shapeDispatch, "Height");
						Variant width = Dispatch.get(shapeDispatch, "Width");
						
						Variant left = Dispatch.get(shapeDispatch, "Left");
						Variant top = Dispatch.get(shapeDispatch, "Top");
						
						Dispatch.call(shapeDispatch, "Delete");
						
						Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { "tupian" }, new int[1]).toDispatch();
						if (cell == null)
							throw new Exception("模板中未找到名称为[" + "tupian" + "]的单元格，请联系管理员！");
						Dispatch.call(cell, "Select"); //在工作表中，定位需要插入图片的具体位置
						Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
						Dispatch pic = Dispatch.call(select, "Insert", imageFile.getAbsolutePath()).toDispatch();
						Dispatch.put(pic, "name", dsName);
						
						Dispatch.put(pic, "Top", top);
						Dispatch.put(pic, "Left", left);
						
						Dispatch.put(pic, "Width", width);
				        Dispatch.put(pic, "Height", height);
					}
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
				if (weldingProcedureItem == null)
				{
					File resultFile = new File(saveFile.getParent(), "WeldingProcedureSpecification" + TcUtil.getSuffix(saveFile.getName()));

					templateFile.renameTo(resultFile);
					
					weldingProcedureItem = TcUtil.createItem("", "", "S4_IT_ProcessDoc", "Welding Procedure Specification", "", null);
					try 
					{
						weldingProcedureItem.getLatestItemRevision().lock();
//						weldingProcedureItem.getLatestItemRevision().setProperty("s4_AT_DocumentType", "焊装_工艺卡");
						weldingProcedureItem.getLatestItemRevision().setProperty("s4_AT_DocumentType", "BAProcessCard");
						weldingProcedureItem.getLatestItemRevision().save();
					} finally 
					{
						weldingProcedureItem.getLatestItemRevision().unlock();
					}
					
					this.baStationBOMLine.getItemRevision().add("IMAN_reference", weldingProcedureItem);
					
					TCComponentDataset ds = TcUtil.createDataset("Welding Procedure Specification", "", "MSExcelX");
					
					TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
					
					weldingProcedureItem.getLatestItemRevision().add("IMAN_specification", ds);
					
				}else
				{
					TCComponentItemRevision weldingProcedureRev = weldingProcedureItem.getLatestItemRevision();
					TCComponent[] status = weldingProcedureRev.getReferenceListProperty("release_status_list");
					
					if ((status != null) && (status.length > 0))
					{
						TCComponentItemRevision lastestWeldingProcedureRev = weldingProcedureRev.saveAs(weldingProcedureRev.getItem().getNewRev(), "Welding Procedure Specification", "", false, null);
						
						TCComponentDataset ds = TcUtil.createDataset("Welding Procedure Specification", "", "MSExcelX");
						
						if (updateStatus)
						{
							TcUtil.importFileToDataset(ds, saveFile, "MSExcelX", "excel");
						}else
						{
							File resultFile = new File(saveFile.getParent(), "WeldingProcedureSpecification" + TcUtil.getSuffix(saveFile.getName()));

							saveFile.renameTo(resultFile);
							TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
						}
						
						lastestWeldingProcedureRev.add("IMAN_specification", ds);
					}else
					{
						TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(weldingProcedureRev, "IMAN_specification", "MSExcelX", "Welding Procedure Specification");
						
						if (ds == null)
						{
							ds = TcUtil.createDataset("Welding Procedure Specification", "", "MSExcelX");
							
							weldingProcedureRev.add("IMAN_specification", ds);
						}
						TcUtil.removeAllFilesFromDataset(ds);
						
						File resultFile = new File(saveFile.getParent(), "WeldingProcedureSpecification" + TcUtil.getSuffix(saveFile.getName()));

						saveFile.renameTo(resultFile);
						
						TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
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
	
	private TCComponentItem findWeldingProcedureSpecification(TCComponentBOMLine opBOMLine)
	{
		TCComponentItem weldingProcedureSpecification = null;
		try 
		{
			TCComponent[] comps = opBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < comps.length; i++) 
			{
//				if (comps[i].isTypeOf("S4_IT_ProcessDoc") && (((TCComponentItem)comps[i]).getLatestItemRevision().getProperty("s4_AT_DocumentType")).equals("焊装_工艺卡"))
				if (comps[i].isTypeOf("S4_IT_ProcessDoc") 
						&& (((TCComponentItem)comps[i]).getLatestItemRevision().getStringProperty("s4_AT_DocumentType")).equals("BAProcessCard"))
				{
					TCComponentItem item = (TCComponentItem)comps[i];
					
					weldingProcedureSpecification = item;
				}
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return weldingProcedureSpecification;
	}
	
	private File getTemplateFile(String workingDir)
	{
		File file = null;
		
		try {
//			TCComponentItem item = TcUtil.findItem("WeldingProcedureSpecification.Template");
//			if (item != null)
//			{
//				AIFComponentContext[] aaifContext = item.getLatestItemRevision().getChildren();
//				for (int i = 0; i < aaifContext.length; i++) 
//				{
//					TCComponent component = (TCComponent) aaifContext[i].getComponent();
//					if (component instanceof TCComponentDataset && component.isTypeOf("MSExcelX"))
//					{
//						TCComponentDataset ds = (TCComponentDataset) component;
//						File[] files = ds.getFiles("excel", workingDir);
//						if ((files != null) && (files.length > 0))
//							file = files[0];
//					}
//				}
//			}
			
			// 修改模板获取方式，modified by zhoutong, 2018-09-18
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(this.appReg.getString("WeldingProcedureSpecification.Template"), "MSExcelX");
			if (templateDataset != null) {
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

	
	private String getPostAddress(TCComponentBOMLine baStationBOMLine) throws TCException
	{
		TCComponent[] relatedComponents = baStationBOMLine.getRelatedComponents("Mfg0assigned_workarea");
		if (relatedComponents != null && relatedComponents.length > 0) {
			return relatedComponents[0].getProperty("bl_child_id");
		}
		return "";
	}
	
}
