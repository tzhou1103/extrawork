package com.sokon.bopreport.customization.generalassemblyprocessdoc;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;

public class NewMultipleGeneralAssemblyProcessDocJob extends Job 
{

	private TCComponentBOMLine gaplantOpBOMLine;
	private int languageType;
	private Registry appReg;
	private String chexingVariant;
	private boolean flag = false;
	public NewMultipleGeneralAssemblyProcessDocJob(String name, TCComponentBOMLine gaplantOpBOMLine, int languageType, String chexingVariant) 
	{
		super(name);
		this.gaplantOpBOMLine = gaplantOpBOMLine;
		this.languageType = languageType;
		this.chexingVariant = chexingVariant;
		this.appReg = Registry.getRegistry(this);
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) 
	{
		File workingDirFile = null;
		try 
		{
			progressMonitor.beginTask(this.appReg.getString("GeneralAssemblyProcessDocInfo.Message"), -1);
			
			workingDirFile = FileUtility.createTempDirectory("GeneralAssemblyProcessDoc");
			
			
			TcUtil.willExpand(new TCComponentBOMLine[] {this.gaplantOpBOMLine});
			
			Vector<TCComponentBOMLine> gaOpBOMLineVector = new Vector<TCComponentBOMLine>();
			
			traverseBOMLine(this.gaplantOpBOMLine, gaOpBOMLineVector);
			
			for (int i = 0; i < gaOpBOMLineVector.size(); i++) 
			{
				TCComponentBOMLine gaOpBOMLine = gaOpBOMLineVector.get(i);
				
				writeExcel(gaOpBOMLine, workingDirFile);
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
	
	private void traverseBOMLine(TCComponentBOMLine paramBOMLine, Vector<TCComponentBOMLine> gaOpBOMLineVector) throws Exception
	{
		AIFComponentContext[] aaifContext = paramBOMLine.getChildren();
		for (int i = 0; i < aaifContext.length; i++) 
		{
			TCComponentBOMLine bomLine = (TCComponentBOMLine) aaifContext[i].getComponent();
			
			if (bomLine.getItem().isTypeOf("S4_IT_GAOP"))
			{
				TCComponentItem assemblyProcessCard = findAssemblyProcessDoc(bomLine);
				if (assemblyProcessCard != null)
				{
					TCComponent[] status = assemblyProcessCard.getLatestItemRevision().getRelatedComponents("release_status_list");
					if ((status == null) || (status.length <= 0))
					{
						gaOpBOMLineVector.add(bomLine);
					}
				}else
				{
					gaOpBOMLineVector.add(bomLine);
				}
			}else
			{
				traverseBOMLine(bomLine, gaOpBOMLineVector);
			}
		}
	}
	
	private void writeExcel(TCComponentBOMLine gaOpBOMLine, File workingDirFile) throws Exception
	{
		String workingDir = workingDirFile.getAbsolutePath();
		
		File templateFile = null;
		boolean updateStatus = false;
		TCComponentItem assemblyProcessCard = findAssemblyProcessDoc(gaOpBOMLine);
		if (assemblyProcessCard != null)
		{
			TCComponentItemRevision assemblyProcessCardRev = assemblyProcessCard.getLatestItemRevision();
			
			TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(assemblyProcessCardRev, "IMAN_specification", "MSExcelX", "General Assembly Process Documents");
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
		
		if (templateFile == null)
			templateFile = getTemplateFile(workingDir);
		
		if (templateFile == null)
			throw new Exception(this.appReg.getString("GeneralAssemblyProcessDocInfo.TemplateNotFound.Message"));
		
		TCComponentBOMLine stationBOMLine = null;
		AIFComponentContext[] aaifContext = gaOpBOMLine.parent().getChildren();
		for (int i = 0; i < aaifContext.length; i++) 
		{
			TCComponentBOMLine bomLine = (TCComponentBOMLine) aaifContext[i].getComponent();
			
			System.out.println("info:" + bomLine.getItem().getType());
			
			if (bomLine.getItem().isTypeOf("S4_IT_Station"))
			{
				stationBOMLine = bomLine;
			}
		}
		
		Vector<TCComponentBOMLine> jzsVector = new Vector<TCComponentBOMLine>();
		HashMap<TCComponentItem, BigDecimal> jzsMap = new HashMap<TCComponentItem, BigDecimal>();
		HashMap<TCComponentItem, StringBuffer> jzsRemarkMap = new HashMap<TCComponentItem, StringBuffer>();
		
		Vector<TCComponentBOMLine> avxVector = new Vector<TCComponentBOMLine>();
		HashMap<TCComponentItem, BigDecimal> avxMap = new HashMap<TCComponentItem, BigDecimal>();
		HashMap<TCComponentItem, StringBuffer> avxRemarkMap = new HashMap<TCComponentItem, StringBuffer>();
		
		Vector<TCComponentBOMLine> stdPartVector = new Vector<TCComponentBOMLine>();
		HashMap<String, BigDecimal> stdPartMap = new HashMap<String, BigDecimal>();
		
		aaifContext = gaOpBOMLine.getChildren();
		for (int i = 0; i < aaifContext.length; i++) 
		{
			TCComponentBOMLine bomLine = (TCComponentBOMLine) aaifContext[i].getComponent();
			TCComponentItem item = bomLine.getItem(); // added by zhoutong, 2018-09-28
			if (Utilities.contains(item.getType(), new String[] {"S4_IT_Tool", "S4_IT_OtherTool", "S4_IT_Equipment"}))
			{
				if (jzsMap.containsKey(item))
				{
					String quantity = bomLine.getProperty("Usage_Quantity");
					if ("".equals(quantity))
						quantity = "1";
					BigDecimal countBigDecimal = jzsMap.get(item).add(new BigDecimal(quantity));
					jzsMap.put(item, countBigDecimal);
					
					String remark = bomLine.getProperty("S4_NT_Remarks");
					if (!"".equals(remark))
					{
						StringBuffer strBuffer = jzsRemarkMap.get(item);
						if (strBuffer.toString().equals(""))
						{
							strBuffer.append(remark);
						}else
						{
							strBuffer.append(",");
							strBuffer.append(remark);
						}
					}
					
				}else
				{
					jzsVector.add(bomLine);
					String quantity = bomLine.getProperty("Usage_Quantity");
					if ("".equals(quantity))
						quantity = "1";
					BigDecimal quantityBigDecimal = new BigDecimal(quantity);
					jzsMap.put(item, quantityBigDecimal);
					
					String remark = bomLine.getProperty("S4_NT_Remarks");
					jzsRemarkMap.put(item, new StringBuffer(remark));
				}
			}else if (item.isTypeOf("S4_IT_AuxPart") && bomLine.getStringProperty("bl_occ_type").equals("MEConsumed"))
			{
				if (avxMap.containsKey(item))
				{
					String quantity = bomLine.getProperty("Usage_Quantity");
					if ("".equals(quantity))
						quantity = "1";
					BigDecimal countBigDecimal = avxMap.get(item).add(new BigDecimal(quantity));
					avxMap.put(item, countBigDecimal);
					
					String remark = bomLine.getProperty("S4_NT_Remarks");
					
					if (!"".equals(remark))
					{
						StringBuffer strBuffer = avxRemarkMap.get(item);

						if (strBuffer.toString().equals(""))
						{
							strBuffer.append(remark);
						}else
						{
							strBuffer.append(",");
							strBuffer.append(remark);
						}
					}
					
				}else
				{
					avxVector.add(bomLine);
					String quantity = bomLine.getProperty("Usage_Quantity");
					if ("".equals(quantity))
						quantity = "1";
					avxMap.put(item, new BigDecimal(quantity));
					String remark = bomLine.getProperty("S4_NT_Remarks");
					avxRemarkMap.put(item, new StringBuffer(remark));
				}
				
			}else if (item.isTypeOf("S4_IT_StdPart") && bomLine.getStringProperty("bl_occ_type").equals("MEConsumed"))
			{
				String itemID = bomLine.getProperty("bl_item_item_id");
				String formula = bomLine.getProperty("bl_formula");
				String torque = bomLine.getProperty("S4_NT_Torque");
				String key = itemID + "&" + formula + "&" + torque;
				if (stdPartMap.containsKey(key))
				{
					String quantity = bomLine.getProperty("Usage_Quantity");
					if ("".equals(quantity))
						quantity = "1";
					BigDecimal countBigDecimal = stdPartMap.get(key).add(new BigDecimal(quantity));
					stdPartMap.put(key, countBigDecimal);
				}else
				{
					stdPartVector.add(bomLine);
					String quantity = bomLine.getProperty("Usage_Quantity");
					if ("".equals(quantity))
						quantity = "1";
					stdPartMap.put(key, new BigDecimal(quantity));
				}
			}else if (item.isTypeOf("S4_IT_Part") && bomLine.getStringProperty("bl_occ_type").equals("MEConsumed"))
			{
				TCComponentItemRevision tempCompRev = (TCComponentItemRevision)TcUtil.getRelatedComponentByType(bomLine.getItemRevision(), "S4_REL_AuxiliaryPart", "S4_IT_AuxPartRevision");
				if (tempCompRev == null)
				{
					String isColorPart = bomLine.getItemRevision().getStringProperty("s4_AT_IsColorPart");
					if ("COL/SURF".equals(isColorPart))
					{
						continue;
					}
					
					String itemID = bomLine.getProperty("bl_item_item_id");
					String formula = bomLine.getProperty("bl_formula");
					String torque = bomLine.getProperty("S4_NT_Torque");
					String key = itemID + "&" + formula + "&" + torque;
					if (stdPartMap.containsKey(key))
					{
						String quantity = bomLine.getProperty("Usage_Quantity");
						if ("".equals(quantity))
							quantity = "1";
						BigDecimal countBigDecimal = stdPartMap.get(key).add(new BigDecimal(quantity));
						stdPartMap.put(key, countBigDecimal);
					}else
					{
						stdPartVector.add(bomLine);
						String quantity = bomLine.getProperty("Usage_Quantity");
						if ("".equals(quantity))
							quantity = "1";
						stdPartMap.put(key, new BigDecimal(quantity));
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
						
						String remark = bomLine.getProperty("S4_NT_Remarks");
						
						if (!"".equals(remark))
						{
							StringBuffer strBuffer = avxRemarkMap.get(tempCompRev.getItem());

							if (strBuffer.toString().equals(""))
							{
								strBuffer.append(remark);
							}else
							{
								strBuffer.append(",");
								strBuffer.append(remark);
							}
						}
						
					}else
					{
						avxVector.add(bomLine);
						String quantity = bomLine.getProperty("Usage_Quantity");
						if ("".equals(quantity))
							quantity = "1";
						avxMap.put(tempCompRev.getItem(), new BigDecimal(quantity));
						
						String remark = bomLine.getProperty("S4_NT_Remarks");
						avxRemarkMap.put(tempCompRev.getItem(), new StringBuffer(remark));
					}
				}
			}
		}
		
		Vector<FileDataset> imageFileVector = new Vector<FileDataset>();
		aaifContext = gaOpBOMLine.getItemRevision().getChildren();
		for (int i = 0; i < aaifContext.length; i++) 
		{
			TCComponent comp = (TCComponent)aaifContext[i].getComponent();
			if ((comp instanceof TCComponentDataset) && comp.isTypeOf("SnapShotViewData"))
			{
				TCComponentDataset ds = (TCComponentDataset)comp;
//				File[] imageFiles = ds.getFiles("ThumbnailImage", workingDir);
				File[] imageFiles = ds.getFiles("Image", workingDir); // modified by zhoutong, 2018-09-28
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
			
			//Dispatch.put(sheet, "name", gapName);
			
			TcUtil.writeData(sheet, "AN1", this.chexingVariant);
			
			TcUtil.writeData(sheet, "A7", gaOpBOMLine.getProperty("bl_item_item_id"));
			
			String gxName = "";
			if (this.languageType == 0)
			{
				gxName = gaOpBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
				
			}else if (this.languageType == 1)
			{
				gxName = gaOpBOMLine.getProperty("bl_rev_object_name");
			}else if (this.languageType == 2)
			{
				gxName = gaOpBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + gaOpBOMLine.getProperty("bl_rev_object_name");
			}
			
			TcUtil.writeData(sheet, "E7", gxName);
			
			TcUtil.writeData(sheet, "AB7", gaOpBOMLine.getProperty("bl_rev_item_revision_id"));
			
			if (stationBOMLine != null)
			{
				String stationName = "";
				if (this.languageType == 0)
				{
					stationName = stationBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
					
				}else if (this.languageType == 1)
				{
					stationName = stationBOMLine.getProperty("bl_rev_object_name");
				}else if (this.languageType == 2)
				{
					stationName = stationBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + stationBOMLine.getProperty("bl_rev_object_name");
				}
				TcUtil.writeData(sheet, "AD7", stationName);
				
				
				String procResArea = "";
				TCComponentBOMLine processResourceBOMLine = (TCComponentBOMLine)gaOpBOMLine.getRelatedComponent("Mfg0processResource");
				if ((processResourceBOMLine != null) && processResourceBOMLine.getItem().isTypeOf("S4_IT_Worker"))
				{
					procResArea = processResourceBOMLine.getItemRevision().getStringProperty("s4_AT_ProcResArea");
				}
				
				if (procResArea.equals(""))
					procResArea = "0";
				
				String temp = stationBOMLine.getProperty("bl_item_item_id") + procResArea;
				
				String stationAddress = "";
				if (temp.length() > 7)
				{
					stationAddress = temp.substring(temp.length() - 7, temp.length());
				}else
				{
					stationAddress = temp;
				}
				
				TcUtil.writeData(sheet, "AO7", stationAddress);
				
				if (!"".equals(stationAddress))
					Dispatch.put(sheet, "name", stationAddress);
			}
			
			TcUtil.writeData(sheet, "AV7", gaOpBOMLine.getProperty("Mfg0allocated_time"));
			
			
			//工具/工装/设备tool/frock/equipment 增加行
			int rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start1");
			int rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end1");
			int rowCount = rowEndIndex - rowStartIndex + 1;
			int addRowCount = jzsVector.size() - rowCount;
			if (addRowCount > 0)
			{
				TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
			}
			int rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end1");
			String rangeName = "A" + rowStartIndex + ":AP" + rowEndIndex1;
			TcUtil.clearContents(sheet, rangeName);
			
			int startRow = rowStartIndex;
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
				
				TcUtil.writeData(sheet, "C" + (startRow + j), jzsName);
				
				TcUtil.writeData(sheet, "X" + (startRow + j), jzsBOMLine.getProperty("s4_BAT_SpecificationModel"));
				
				String countStr = jzsMap.get(jzsBOMLine.getItem()).toString();
				TcUtil.writeData(sheet, "AF" + (startRow + j), countStr);
				
				TcUtil.writeData(sheet, "AK" + (startRow + j), jzsBOMLine.getProperty("S4_NT_Remarks"));
			}
			
			
			//辅助材料Accessories materials 增加行
			rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start2");
			rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end2");
			rowCount = rowEndIndex - rowStartIndex + 1;
			addRowCount = avxVector.size() - rowCount;
			if (addRowCount > 0)
			{
				TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
			}
			rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end2");
			rangeName = "A" + rowStartIndex + ":AP" + rowEndIndex1;
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
				
				TcUtil.writeData(sheet, "C" + (startRow + j), avxName);
				
				String xinghao = "";
				if (partRev != null)
					xinghao = partRev.getProperty("s4_AT_AuxiliarySpec");
				else
					xinghao = avxBOMLine.getProperty("s4_AT_AuxiliarySpec");
				
				TcUtil.writeData(sheet, "X" + (startRow + j), xinghao);
				
				String countStr = "";
				if (partRev != null)
					countStr = avxMap.get(partRev.getItem()).toString();
				else
				{
					System.out.println("info:" + avxBOMLine.toDisplayString());
					countStr = avxMap.get(avxBOMLine.getItem()).toString();
				}
				TcUtil.writeData(sheet, "AF" + (startRow + j), countStr);
				
				String remark = "";
				if (partRev != null)
					remark = avxRemarkMap.get(partRev.getItem()).toString();
				else 
					remark = avxRemarkMap.get(avxBOMLine.getItem()).toString();
				TcUtil.writeData(sheet, "AK" + (startRow + j), remark);
			}
			
			//零件清单Part List 增加行
			rowStartIndex = TcUtil.getRowIndex(sheet, "xuhao.start3");
			rowEndIndex = TcUtil.getRowIndex(sheet, "xuhao.end3");
			rowCount = rowEndIndex - rowStartIndex + 1;
			addRowCount = stdPartVector.size() - rowCount;
			if (addRowCount > 0)
			{
				TcUtil.copyRow(sheet, rowEndIndex, addRowCount);
			}
			rowEndIndex1 = TcUtil.getRowIndex(sheet, "xuhao.end3");
			rangeName = "A" + rowStartIndex + ":AP" + rowEndIndex1;
			TcUtil.clearContents(sheet, rangeName);
			
			startRow = rowStartIndex;
			
			for (int j = 0; j < stdPartVector.size(); j++) 
			{
				TCComponentBOMLine partBOMLine = stdPartVector.get(j);
				
				TcUtil.writeData(sheet, "A" + (startRow + j), String.valueOf(j + 1));
				
				String stdPartName = "";
				if (this.languageType == 0)
				{
					stdPartName = partBOMLine.getProperty("bl_rev_s4_CAT_ChineseName");
					
				}else if (this.languageType == 1)
				{
					stdPartName = partBOMLine.getProperty("bl_rev_object_name");
					
				}else if (this.languageType == 2)
				{
					stdPartName = partBOMLine.getProperty("bl_rev_s4_CAT_ChineseName") + "\n" + partBOMLine.getProperty("bl_rev_object_name");
				}
				
				TcUtil.writeData(sheet, "C" + (startRow + j), stdPartName);
				
				String itemID = partBOMLine.getProperty("bl_item_item_id");
				String formula = partBOMLine.getProperty("bl_formula");
				String torque = partBOMLine.getProperty("S4_NT_Torque");
				String key = itemID + "&" + formula + "&" + torque;
				
				String countStr = stdPartMap.get(key).toString();
				TcUtil.writeData(sheet, "X" + (startRow + j), countStr);
				
				TcUtil.writeData(sheet, "AA" + (startRow + j), partBOMLine.getProperty("bl_item_item_id"));
				
//				String formulaStr = "";
//				if (!formula.equals(""))
//				{
//					String rgex = "=(.*?)[)]";
//					List<String> list = getSubUtil(formula, rgex);
//					StringBuffer strBuffer = new StringBuffer();
//					for (int i = 0; i < list.size(); i++) 
//					{
//						String str = list.get(i); 
//						if (i != 0)
//							strBuffer.append(",");
//						strBuffer.append(str);
//					}
//					
//					formulaStr = strBuffer.toString();
//				}
				
//				String formulaStr = getFormulaStr(formula);
				
				TcUtil.writeData(sheet, "AF" + (startRow + j), formula);
				
				TcUtil.writeData(sheet, "AM" + (startRow + j), partBOMLine.getProperty("S4_NT_Torque"));
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
			if (assemblyProcessCard == null)
			{
				File resultFile = new File(saveFile.getParent(), "GeneralAssemblyProcessDocuments_" + System.currentTimeMillis() + TcUtil.getSuffix(saveFile.getName()));

				templateFile.renameTo(resultFile);
				
				assemblyProcessCard = TcUtil.createItem("", "", "S4_IT_ProcessDoc", "General Assembly Process Documents", "", null);
				try 
				{
					assemblyProcessCard.getLatestItemRevision().lock();
//					assemblyProcessCard.getLatestItemRevision().setProperty("s4_AT_DocumentType", "总装_工艺卡");
					assemblyProcessCard.getLatestItemRevision().setProperty("s4_AT_DocumentType", "GAProcessCard");
					assemblyProcessCard.getLatestItemRevision().save();
				} finally 
				{
					assemblyProcessCard.getLatestItemRevision().unlock();
				}
				
				gaOpBOMLine.getItemRevision().add("IMAN_reference", assemblyProcessCard);
				
				TCComponentDataset ds = TcUtil.createDataset("General Assembly Process Documents", "", "MSExcelX");
				
				TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
				
				assemblyProcessCard.getLatestItemRevision().add("IMAN_specification", ds);
				
			}else
			{
				TCComponentItemRevision assemblyProcessCardRev = assemblyProcessCard.getLatestItemRevision();
				TCComponent[] status = assemblyProcessCardRev.getReferenceListProperty("release_status_list");
				
				if ((status != null) && (status.length > 0))
				{
					TCComponentItemRevision lastestAssemblyProcessCardRev = assemblyProcessCardRev.saveAs(assemblyProcessCardRev.getItem().getNewRev(), "General Assembly Process Documents", "", false, null);
					
					TCComponentDataset ds = TcUtil.createDataset("General Assembly Process Documents", "", "MSExcelX");
					
					if (updateStatus)
					{
						TcUtil.importFileToDataset(ds, saveFile, "MSExcelX", "excel");
					}else
					{
						File resultFile = new File(saveFile.getParent(), "GeneralAssemblyProcessDocuments_" + System.currentTimeMillis() + TcUtil.getSuffix(saveFile.getName()));

						saveFile.renameTo(resultFile);
						TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
					}
					
					lastestAssemblyProcessCardRev.add("IMAN_specification", ds);
				}else
				{
					TCComponentDataset ds = (TCComponentDataset)TcUtil.getRelatedComponentByTypeName(assemblyProcessCardRev, "IMAN_specification", "MSExcelX", "General Assembly Process Documents");
					
					if (ds == null)
					{
						ds = TcUtil.createDataset("General Assembly Process Documents", "", "MSExcelX");
						
						assemblyProcessCardRev.add("IMAN_specification", ds);
					}
					TcUtil.removeAllFilesFromDataset(ds);
					
					File resultFile = new File(saveFile.getParent(), "GeneralAssemblyProcessDocuments_" + System.currentTimeMillis() + TcUtil.getSuffix(saveFile.getName()));

					saveFile.renameTo(resultFile);
					
					TcUtil.importFileToDataset(ds, resultFile, "MSExcelX", "excel");
				}
			}
		}
	}
	
	private File getTemplateFile(String workingDir)
	{
		File file = null;
		
		try {
//			TCComponentItem item = TcUtil.findItem("GeneralAssemblyProcessDoc.Template");
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
			TCComponentDataset templateDataset = TcUtil.findTemplateDataset(this.appReg.getString("AssemblyProcessDoc.Template"), "MSExcelX");
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
	
	private TCComponentItem findAssemblyProcessDoc(TCComponentBOMLine opBOMLine)
	{
		TCComponentItem assemblyProcessCard = null;
		try 
		{
			TCComponent[] comps = opBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < comps.length; i++) 
			{
//				if (comps[i].isTypeOf("S4_IT_ProcessDoc") && (((TCComponentItem)comps[i]).getLatestItemRevision().getProperty("s4_AT_DocumentType")).equals("总装_工艺卡"))
				if (comps[i].isTypeOf("S4_IT_ProcessDoc") 
						&& (((TCComponentItem)comps[i]).getLatestItemRevision().getStringProperty("s4_AT_DocumentType")).equals("GAProcessCard"))
				{
					TCComponentItem item = (TCComponentItem)comps[i];
					
					assemblyProcessCard = item;
				}
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return assemblyProcessCard;
	}

	
	/**
	 * 根据语言获取Sheet页名称
	 * 
	 * @param opBomLine
	 * @param languageType
	 * @return
	 * @throws TCException
	 */
	public String getSheetName(TCComponentBOMLine opBomLine, int languageType) throws TCException
	{
		String objectName = opBomLine.getProperty("bl_rev_object_name");
		String chineseName = opBomLine.getProperty("bl_rev_s4_CAT_ChineseName");
		if (languageType == 0) {
			if (chineseName.equals(""))
				return objectName;
			return chineseName;
		} else if (languageType == 1 || languageType == 2) {
			return objectName;
		}  
		
		return objectName;
	}
	
	public List<String> getSubUtil(String soap,String rgex)
	{
		List<String> list = new ArrayList<String>();
		Pattern pattern = Pattern.compile(rgex);
		Matcher m = pattern.matcher(soap);
		while (m.find())
		{
			int i=1;
			list.add(m.group(i));
			i++;
		}
		
		return list;
	}
	
	public String getFormulaStr(String formula)
	{
		if (formula == null || formula.equals(""))
			return "";
		String rgex = "\\[(.*?)=";
		Pattern pattern = Pattern.compile(rgex);
		Matcher m = pattern.matcher(formula);
		String result = m.replaceAll("");
		
		return result;
	}
}
