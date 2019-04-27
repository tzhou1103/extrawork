package com.sokon.report;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;

import com.sokon.report.data.OPStructLine;
import com.sokon.report.data.OneTool;
import com.sokon.report.data.PaintStatStruct;
import com.sokon.report.data.PicInfo;
import com.sokon.report.data.sokonCommon;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentCfg0ConfiguratorPerspective;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductModel;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.zy.common.ExcelCommon;
import com.zy.common.ReportCommon;
import com.zy.common.SOACommon;

public class PaintingProcess extends AbstractHandler implements IHandler {

	TCComponentDataset TemplateDatset = null;
	int Lang = -1;
	String Error = "";
	String TargetType = null;
	ArrayList PSStructArray = new ArrayList();

	HashMap AttachmentType = new HashMap();
	ArrayList AllToolsArray = new ArrayList();
	ArrayList AllOPStructArray = new ArrayList();

	ArrayList replacePic = new ArrayList();
	String StationNO = "";

	HashMap GetAttachmentType() {
		String ArraPre[] = ReportCommon.GetPreferences("S4CUST_PaintingInstruction_AttachmentType");
		// AttachmentType
		HashMap AttachmentType = new HashMap();
		for (int i = 0; i < ArraPre.length; i++) {
			String Split[] = ArraPre[i].split("=");
			if (Split.length == 2) {
				AttachmentType.put(Split[0], Split[1]);
			}
		}
		return AttachmentType;
	}

	public Object execute(ExecutionEvent e) throws ExecutionException {
		try {
			InterfaceAIFComponent Comp = AIFUtility.getTargetComponent();
			if (Comp == null) {
				MessageBox.post(Messages.InvalidType, Messages.LangDlg_Infomation, 2);
				return null;
			}
			if (!(Comp instanceof TCComponentMfgBvrProcess)) {
				MessageBox.post(Messages.InvalidType, Messages.LangDlg_Infomation, 2);
				return null;
			}
			try {
				AttachmentType = GetAttachmentType();
				if (AttachmentType.size() == 0) {
					MessageBox.post("Invalid S4CUST_PaintingInstruction_AttachmentType", Messages.LangDlg_Infomation, 2);
					return null;
				}

				PSStructArray.clear();
				TCComponentMfgBvrProcess TempMfg = (TCComponentMfgBvrProcess) Comp;
				TargetType = TempMfg.getItemRevision().getType();
				if (TargetType.equals("S4_IT_PaintBOPRevision")) {
					TCComponentMfgBvrProcess PaintBOP = TempMfg;
					InitPSStruct(PaintBOP);
					if (PSStructArray.size() == 0) {
						MessageBox.post(Messages.NoPaintStatRevision, Messages.LangDlg_Infomation, 2);
						return null;
					}
				} else if (TargetType.equals("S4_IT_PaintStatRevision")) {
					TCComponentMfgBvrProcess PaintStat = TempMfg;
					TCComponentMfgBvrProcess PaintProc = (TCComponentMfgBvrProcess) PaintStat.parent();
					TCComponentMfgBvrProcess PaintBOP = (TCComponentMfgBvrProcess) PaintProc.parent();
					TCComponentItem ProcessDoc = sokonCommon.GetProcessDoc(PaintStat, "涂装工艺卡", "Paint Process Card");
					PaintStatStruct PSStruct = new PaintStatStruct(PaintBOP, PaintProc, PaintStat, ProcessDoc);
					PSStructArray.add(PSStruct);
				} else {
					MessageBox.post(Messages.InvalidType, Messages.LangDlg_Infomation, 2);
					return null;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			TemplateDatset = ReportCommon.GetTemplateDataset("SOKON_320_涂装工艺卡模板");
			if (TemplateDatset == null)
				return null;

			Shell shell = AIFUtility.getActiveDesktop().getShell();
			if (NeedAlert()) {
				boolean b = MessageDialog.openConfirm(shell, Messages.LangDlg_Infomation, Messages.IsOverwrite);
				if (!b) {
					return null;
				}
			}

			LangDlg Dlg = new LangDlg(shell, Messages.ProcessReport);
			Dlg.setBlockOnOpen(true);
			if (Dlg.open() == 0) {
				Lang = Dlg.Lang;
				String Error = "";
				ProgressMonitorDialog progress = new ProgressMonitorDialog(null);
				progress.setCancelable(false);
				progress.run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException {
						monitor.beginTask(Messages.TaskInProcess, IProgressMonitor.UNKNOWN);
						monitor.setTaskName(Messages.TaskInProcess);

						GenerateReport(TemplateDatset, PSStructArray, Lang);
					}
				});
				if (Error.length() > 0) {
					MessageBox.post(Error, Messages.LangDlg_Infomation, 2);
				} else {
					MessageBox.post(Messages.Done, Messages.LangDlg_Infomation, 2);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	boolean NeedAlert() {
		for (int i = 0; i < PSStructArray.size(); i++) {
			PaintStatStruct PSStruct = (PaintStatStruct) PSStructArray.get(i);
			if (PSStruct.ProcessDoc != null)
				return true;
		}
		return false;
	}

	boolean GenerateReport(TCComponentDataset TemplateDatset, ArrayList PSStructArray, int Lang) {
		try {
			for (int i = 0; i < PSStructArray.size(); i++) {
				TemplateDatset = ReportCommon.GetTemplateDataset("SOKON_320_涂装工艺卡模板");
				PaintStatStruct PSStruct = (PaintStatStruct) PSStructArray.get(i);
				String DateStr = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
				String NewFileName = "PaintReport_" + DateStr + ".xlsx";

				if (PSStruct.ProcessDoc != null) {
					TemplateDatset = (TCComponentDataset) ReportCommon.GetRelationComp(PSStruct.ProcessDoc.getLatestItemRevision(), "IMAN_specification", "MSExcelX");
					if (TemplateDatset == null) {
						Error = "No Excel found under ProcessDoc";
						return false;
					}
				}
				File TempDir = new File("C:\\temp\\");
				if (!TempDir.exists()) {
					TempDir.mkdir();
				}

				File TemplateFile = ReportCommon.DownloadReportTemplate(TemplateDatset, NewFileName, TempDir.getAbsolutePath());
				if (TemplateFile != null) {
					StationNO = "";
					if (FileData2Excel(TemplateFile, PSStruct, TempDir)) {
						// Update to Tc
						String DocItemName = "Painting Process Inspection Instruction";
						TCComponentItemRevision DocItemRev = null;
						if (PSStruct.ProcessDoc == null) {
							TCComponentItem DocItem = SOACommon.createItem("S4_IT_ProcessDoc", "", DocItemName, "", "A");
							DocItemRev = DocItem.getLatestItemRevision();
							PSStruct.PaintStat.getItemRevision().add("IMAN_reference", DocItemRev.getItem());

							try {
								DocItemRev.setProperty("s4_AT_DocumentType", "PaintProcessCard");
								if (ReportCommon.IsEnglisth()) {
									// DocItemRev.setProperty("s4_AT_DocumentType", "Paint Process Card");
								} else {
									// DocItemRev.setProperty("s4_AT_DocumentType", "涂装工艺卡");
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							DocItemRev = PSStruct.ProcessDoc.getLatestItemRevision();
							if (SOACommon.IsReleased(DocItemRev)) {
								DocItemRev = DocItemRev.saveAs("");
							}
						}
						if (DocItemRev != null) {
							String ExcelName = StationNO + " " + PSStruct.PaintStat.getItemRevision().getProperty("object_name");
							ReportCommon.createOrUpdateDataset(TemplateFile.getAbsolutePath(), "MSExcelX", "excel", ExcelName, DocItemRev, "IMAN_specification", false);
						}
					} else {
						Error = Error + "Report created failed\n";
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	void ClearTemplate(XSSFWorkbook wb) {
		ArrayList FirstClear = ReportCommon.LoadProp("PaintClear.properties", "First");
		XSSFSheet sheet = wb.getSheetAt(0);
		for (int m = 0; m < FirstClear.size(); m++) {
			ExcelCommon.SetCellValue(sheet, FirstClear.get(m).toString(), "");
		}
	}

	int GetNameLine(XSSFName OPERATION) {
		try {
			String refer = OPERATION.getRefersToFormula();
			String Split[] = refer.split("\\$");
			return Integer.parseInt(Split[Split.length - 1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	void deleteRow(XSSFSheet sheet, int startRow, int endRow) {
		try {
			if (startRow < endRow) {
				boolean Ret = ExcelCommon.DeleteMergeRegin(sheet, startRow, endRow);
				while (Ret) {
					Ret = ExcelCommon.DeleteMergeRegin(sheet, startRow, endRow);
				}

				int lastIndex = sheet.getLastRowNum();
				sheet.shiftRows(endRow, lastIndex, startRow - endRow);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void ClearCell(XSSFSheet sheet, int StartLine, int EndLine) {
		for (int i = StartLine; i < EndLine; i++) {
			try {
				XSSFRow row = sheet.getRow(i);
				if (row != null) {
					for (int m = 0; m < 34; m++) {
						XSSFCell cell = row.getCell(m);
						if (cell != null) {
							ExcelCommon.WbFillCell(sheet, cell, "");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	boolean FileData2Excel(File TemplateFile, PaintStatStruct PSStruct, File TempDir) {
		try {
			FileInputStream fileInputStream = new FileInputStream(TemplateFile);
			XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream(fileInputStream));
			XSSFSheet sheet = wb.getSheetAt(0);

			XSSFName OPERATION = wb.getName("OPERATION");
			XSSFName STRUCTURE = wb.getName("STRUCTURE");
			XSSFName TOOL = wb.getName("TOOL");
			XSSFName ATTACHMENT = wb.getName("ATTACHMENT");
			XSSFName DRAWING = wb.getName("DRAWING");

			if ((OPERATION == null) || (STRUCTURE == null) || (TOOL == null) || (ATTACHMENT == null) || (DRAWING == null)) {
				Error = "Invalid template";
				return false;
			}

			int OPERATION_Line = 0;
			int STRUCTURE_Line = 0;
			int TOOL_Line = 0;
			int DRAWING_Line = 0;

			// OPERATION_Line==>STRUCTURE_Line
			OPERATION_Line = GetNameLine(OPERATION);
			STRUCTURE_Line = GetNameLine(STRUCTURE);
			deleteRow(sheet, OPERATION_Line + 1, STRUCTURE_Line - 3);

			// OPERATION_Line==>TOOL_Line
			STRUCTURE_Line = GetNameLine(STRUCTURE);
			TOOL_Line = GetNameLine(TOOL);
			deleteRow(sheet, STRUCTURE_Line + 1, TOOL_Line - 3);

			// TOOL_Line==>ATTACHMENT_Line
			TOOL_Line = GetNameLine(TOOL);
			int ATTACHMENT_Line = GetNameLine(ATTACHMENT);
			deleteRow(sheet, TOOL_Line + 1, ATTACHMENT_Line - 11);

			// TOOL_Line==>ATTACHMENT_Line
			ATTACHMENT_Line = GetNameLine(ATTACHMENT);
			DRAWING_Line = GetNameLine(DRAWING);
			deleteRow(sheet, ATTACHMENT_Line + 1, DRAWING_Line - 4);

			if (PSStruct.ProcessDoc != null) {
				ClearTemplate(wb);
				ClearCell(sheet, OPERATION_Line - 1, OPERATION_Line + 1);
				ClearCell(sheet, STRUCTURE_Line - 1, STRUCTURE_Line + 1);
				ClearCell(sheet, TOOL_Line - 1, TOOL_Line + 2);
				ClearCell(sheet, ATTACHMENT_Line - 1, ATTACHMENT_Line + 1);
			}

			AllToolsArray.clear();
			AllOPStructArray.clear();

			// Process Title工艺名称
			String ProcessTitle = sokonCommon.GetProperty(PSStruct.PaintStat, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
			ExcelCommon.SetCellValue(sheet, "D8", ProcessTitle);

			// Line产线
			String Line = sokonCommon.GetProperty(PSStruct.PaintProc, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
			ExcelCommon.SetCellValue(sheet, "S7", Line);
			// Station NO工位号
			String StationArea = PSStruct.PaintStat.getProperty("s4_AT_MEStationArea");
			if (StationArea.length() == 0) {
				StationArea = "0";
			}
			TCComponentMfgBvrBOPLine Station = sokonCommon.GetSource(PSStruct.PaintStat, "MEWorkArea", "S4_IT_Station");

			if (Station != null) {
				StationNO = Station.getProperty("bl_item_item_id") + StationArea;
				if (StationNO.length() > 7) {
					StationNO = StationNO.substring(StationNO.length() - 7);
				}
				ExcelCommon.SetCellValue(sheet, "S8", StationNO);
			}
			// Mfg0allocated_time
			String Mfg0allocated_time = PSStruct.PaintStat.getProperty("Mfg0allocated_time");
			ExcelCommon.SetCellValue(sheet, "V8", ReportCommon.RemoveZero(Mfg0allocated_time));
			// Variant
			String Variant = PSStruct.PaintBOP.getItemRevision().getProperty("s4_AT_EngineeringModel");
			ExcelCommon.SetCellValue(sheet, "AD8", Variant);

			// Operation
			ArrayList PaintOPArray = GetPaintOP(PSStruct.PaintStat);
			STRUCTURE_Line = GetNameLine(OPERATION);
			ExportOPERATION(STRUCTURE_Line, PaintOPArray, sheet, StationNO, Variant);
			// OPStruct
			STRUCTURE_Line = GetNameLine(STRUCTURE);
			ExportOPStruct(STRUCTURE_Line, AllOPStructArray, sheet);
			// Tool
			TOOL_Line = GetNameLine(TOOL);
			ExportAllTool(TOOL_Line, AllToolsArray, sheet);
			// Attachments
			ATTACHMENT_Line = GetNameLine(ATTACHMENT);
			ExportAttachment(ATTACHMENT_Line, PSStruct.PaintStat, sheet);
			// Picture
			DRAWING_Line = GetNameLine(DRAWING);
			replacePic.clear();
			ArrayList SnapShot = ReportCommon.Get3DSnapShot(PSStruct.PaintStat);
			ExcelCommon.ExportPicture(DRAWING_Line + 2, SnapShot, sheet, TempDir);

			try {
				sheet.setActiveCell("A1");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			// 设置打印分页 RowBreak+插入行
			try {
				int Break[] = sheet.getRowBreaks();
				int Result_DRAWING_Line = GetNameLine(DRAWING);
				if (Break.length > 0) {
					sheet.removeRowBreak(Break[0]);
					int NewBreak = Result_DRAWING_Line - 4;
					sheet.setRowBreak(NewBreak);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// ch && en Lang = 4;
			// ch Lang = 2
			// en Lang = 1
			// Station Address+"-"+工序号
			String SheetName = "";
			if (Lang == 2) {
				SheetName = StationNO;
				// SheetName = PSStruct.PaintStat.getProperty("bl_rev_s4_CAT_ChineseName");
			} else if (Lang == 4 || Lang == 1) {
				SheetName = StationNO;
				// SheetName = PSStruct.PaintStat.getProperty("bl_rev_object_name");
			}
			ExcelCommon.SetSheetName(sheet, SheetName);

			// 保存文件 退出
			FileOutputStream out = new FileOutputStream(TemplateFile);
			wb.write(out);
			fileInputStream.close();
			out.close();
			return true;
		} catch (Exception e11) {
			e11.printStackTrace();
		}
		return false;
	}

	void RemoveAllPic(XSSFSheet sheet, XSSFPicture OldPic) {
		XSSFDrawing drawing = OldPic.getDrawing();
		List AllPic = sheet.getWorkbook().getAllPictures();
		for (int z = 0; z < AllPic.size(); z++) {
			if (AllPic.get(z) instanceof XSSFPictureData) {
				XSSFPictureData data = (XSSFPictureData) AllPic.get(z);
				PackagePart part = data.getPackagePart();
				part.setDeleted(true);
				drawing.getPackagePart().getPackage().removePart(part);
			}
		}

		CTTwoCellAnchor[] Anch = drawing.getCTDrawing().getTwoCellAnchorArray();
		for (int mm = Anch.length - 1; mm >= 0; mm--) {
			drawing.getCTDrawing().removeTwoCellAnchor(mm);
		}
		List relation = drawing.getRelations();
		for (int mm = 0; mm < relation.size(); mm++) {
			if (relation.get(mm) instanceof XSSFPictureData) {
				XSSFPictureData data = (XSSFPictureData) relation.get(mm);
				String RelID = data.getPackageRelationship().getId();
				drawing.getPackagePart().removeRelationship(RelID);
			}
		}
	}

	PicInfo DeleteOldPic(XSSFSheet sheet, String PicName) {
		try {
			boolean HavePic = false;
			int Row = -1;
			int Col = -1;
			List drlist = sheet.getRelations();
			XSSFDrawing drawing = null;
			for (int i = 0; i < drlist.size(); i++) {
				if (drlist.get(i) instanceof XSSFDrawing) {
					drawing = (XSSFDrawing) drlist.get(i);
					List shapes = drawing.getShapes();
					for (int m = 0; m < shapes.size(); m++) {
						try {
							XSSFPicture pic = (XSSFPicture) shapes.get(m);
							String Name = pic.getCTPicture().getNvPicPr().getCNvPr().getName();
							if (PicName.equals(Name)) {
								XSSFPictureData data = pic.getPictureData();
								HavePic = true;
								XSSFClientAnchor anchor = pic.getPreferredSize();
								CTMarker ctMarker = anchor.getFrom();
								Row = ctMarker.getRow();
								Col = ctMarker.getCol();

								String RelID = data.getPackageRelationship().getId();
								drawing.getPackagePart().removeRelationship(RelID);

								PackagePart part = data.getPackagePart();
								if (!part.isDeleted()) {
									part.setDeleted(true);
									drawing.getPackagePart().getPackage().removePart(part);
								}
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			CTTwoCellAnchor[] Anch = drawing.getCTDrawing().getTwoCellAnchorArray();
			for (int mm = Anch.length - 1; mm >= 0; mm--) {
				String Name = Anch[mm].getPic().getNvPicPr().getCNvPr().getName();
				if (Name.equals(PicName)) {
					drawing.getCTDrawing().removeTwoCellAnchor(mm);
				}
			}
			PicInfo picinfo = new PicInfo(HavePic, Row, Col);
			return picinfo;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	void ExportOPERATION(int StartLine, ArrayList PaintOPArray, XSSFSheet sheet, String StationNO, String Variant) {
		int CurrentLine = StartLine;
		ExcelCommon.InsertRow(sheet, StartLine, PaintOPArray.size());
		for (int m = 0; m < PaintOPArray.size(); m++) {
			ExcelCommon.copyRows(sheet, sheet, StartLine, StartLine, StartLine + m + 1);
		}

		for (int i = 0; i < PaintOPArray.size(); i++) {
			try {
				TCComponentMfgBvrBOPLine OPBOPLine = (TCComponentMfgBvrBOPLine) PaintOPArray.get(i);
				// OP NO操作序号
				String OPNO = OPBOPLine.getProperty("bl_sequence_no");
				ExcelCommon.SetCellValue(sheet, "B" + String.valueOf(CurrentLine), OPNO);

				// String value = OPBOPLine.getItemRevision().getProperty("s4_AT_OperationType");
				String OpEngName = GetProperty(OPBOPLine.getItemRevision(), "s4_AT_OperationType", "en_US");
				String OpChineseName = GetProperty(OPBOPLine.getItemRevision(), "s4_AT_OperationType", "zh_CN");
				String OperationType = "";// OPBOPLine.getProperty("s4_BAT_OperationType");
				if (Lang == 4) {
					OperationType = OpChineseName + OpEngName;
				} else if (Lang == 2) {
					OperationType = OpChineseName;
				} else if (Lang == 1) {
					OperationType = OpEngName;
				}
				ExcelCommon.SetCellValue(sheet, "C" + String.valueOf(CurrentLine), OperationType);

				String CCSC = OPBOPLine.getProperty("s4_BAT_CCSC");
				ExcelCommon.SetCellValue(sheet, "D" + String.valueOf(CurrentLine), CCSC);

				String RefNo = String.valueOf((char) ('A' + CurrentLine - StartLine));
				ExcelCommon.SetCellValue(sheet, "E" + String.valueOf(CurrentLine), RefNo);

				String OPDesc = sokonCommon.GetProperty(OPBOPLine, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
				ExcelCommon.SetCellValue(sheet, "F" + String.valueOf(CurrentLine), OPDesc);

				ExcelCommon.SetCellValue(sheet, "AA" + String.valueOf(CurrentLine), StationNO);

				String TMUTime = ReportCommon.RemoveZero(OPBOPLine.getProperty("Mfg0allocated_time"));
				ExcelCommon.SetCellValue(sheet, "AH" + String.valueOf(CurrentLine), TMUTime);

				String OPVariant = GetVariant(OPBOPLine);
				ExcelCommon.SetCellValue(sheet, "AC" + String.valueOf(CurrentLine), OPVariant);

				ArrayList OPStructArray = GetOPStruct(RefNo, OPVariant, OPBOPLine);
				for (int m = 0; m < OPStructArray.size(); m++) {
					AllOPStructArray.add(OPStructArray.get(m));
				}
				// Tool
				ArrayList OPToolArray = GetOPTool(RefNo, OPBOPLine);
				for (int m = 0; m < OPToolArray.size(); m++) {
					AllToolsArray.add(OPToolArray.get(m));
				}
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	String GetVariant(TCComponentMfgBvrBOPLine OPBOPLine) {
		String OPVariant = "";
		try {
			TCComponentCfg0ConfiguratorPerspective Perspective = (TCComponentCfg0ConfiguratorPerspective) OPBOPLine.window().getRelatedComponent("smc0ConfigPerspective");
			String formula = OPBOPLine.getProperty("bl_formula");
			if (formula.length() > 0) {
				HashMap ModelSet = new HashMap();
				if (Perspective != null) {
					try {
						TCComponent[] cfgModel = Perspective.getRelatedComponents("cfg0Models");
						for (int z = 0; z < cfgModel.length; z++) {
							TCComponentCfg0ProductModel ProdModel = (TCComponentCfg0ProductModel) cfgModel[z];
							String CurrentName = ProdModel.getProperty("object_name");
							String EngName = GetProperty(ProdModel, "object_desc", "en_US");
							String ChineseName = GetProperty(ProdModel, "object_desc", "zh_CN");
							String object_desc = EngName;
							if (Lang == 4) {
								object_desc = ChineseName + EngName;
							} else if (Lang == 2) {
								object_desc = ChineseName;
							} else if (Lang == 1) {
								object_desc = EngName;
							}
							ModelSet.put(CurrentName, ProdModel);
							if (formula.contains(CurrentName)) {
								if (OPVariant.length() == 0) {
									OPVariant = object_desc;
								} else {
									OPVariant = OPVariant + "/" + object_desc;
								}
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return OPVariant;
	}

	String GetProperty(TCComponent ProdModel, String PropName, String locale) {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			com.teamcenter.services.rac.core._2010_04.DataManagement.PropertyInfo apropertyinfo[] = new com.teamcenter.services.rac.core._2010_04.DataManagement.PropertyInfo[1];
			apropertyinfo[0] = new com.teamcenter.services.rac.core._2010_04.DataManagement.PropertyInfo();
			apropertyinfo[0].object = ProdModel;
			apropertyinfo[0].propsToget = new com.teamcenter.services.rac.core._2010_04.DataManagement.NameLocaleStruct[1];
			apropertyinfo[0].propsToget[0] = new com.teamcenter.services.rac.core._2010_04.DataManagement.NameLocaleStruct();
			apropertyinfo[0].propsToget[0].name = PropName;
			apropertyinfo[0].propsToget[0].locales = new String[] { locale };

			DataManagementService dmService = DataManagementService.getService(session);
			com.teamcenter.services.rac.core._2010_04.DataManagement.LocalizedPropertyValuesList m_response = dmService.getLocalizedProperties(apropertyinfo);
			if (m_response.output != null) {
				int j = m_response.output.length;
				if (j > 0) {
					String Value = m_response.output[0].propertyValues[0].values[0];
					return Value;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	void ExportOPStruct(int StartLine, ArrayList AllOPStructArray, XSSFSheet sheet) {
		// Attachments
		int CurrentLine = StartLine;

		ExcelCommon.InsertRow(sheet, StartLine, AllOPStructArray.size());
		for (int m = 0; m < AllOPStructArray.size(); m++) {
			ExcelCommon.copyRows(sheet, sheet, StartLine, StartLine, StartLine + m + 1);
		}

		for (int i = 0; i < AllOPStructArray.size(); i++) {
			try {
				OPStructLine StrLine = (OPStructLine) AllOPStructArray.get(i);
				ExcelCommon.SetCellValue(sheet, "B" + String.valueOf(CurrentLine), StrLine.RefNo);
				// Usage_Quantity
				String Usage_Quantity = StrLine.OPStructBOPLine.getProperty("Usage_Quantity");
				if (Usage_Quantity.equals("")) {
					Usage_Quantity = "1";
				}
				ExcelCommon.SetCellValue(sheet, "C" + String.valueOf(CurrentLine), Usage_Quantity);

				if (StrLine.OPStructBOPLine.getItem().getType().equals("S4_IT_OtherTool")) {
					ExcelCommon.SetCellValue(sheet, "D" + String.valueOf(CurrentLine), StrLine.OPStructBOPLine.getProperty("s4_BAT_ToolNumber"));
				} else {
					ExcelCommon.SetCellValue(sheet, "D" + String.valueOf(CurrentLine), StrLine.OPStructBOPLine.getItem().getProperty("item_id"));
				}
				ExcelCommon.SetCellValue(sheet, "F" + String.valueOf(CurrentLine), sokonCommon.GetProperty(StrLine.OPStructBOPLine, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang));
				ExcelCommon.SetCellValue(sheet, "AB" + String.valueOf(CurrentLine), StrLine.Variant);
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void ExportAllTool(int StartLine, ArrayList AllToolsArray, XSSFSheet sheet) {
		int CurrentLine = StartLine;

		ExcelCommon.InsertRow(sheet, StartLine, AllToolsArray.size());
		for (int m = 0; m < AllToolsArray.size(); m++) {
			ExcelCommon.copyRows(sheet, sheet, StartLine, StartLine, StartLine + m + 1);
		}

		for (int i = 0; i < AllToolsArray.size(); i++) {
			try {
				OneTool tool = (OneTool) AllToolsArray.get(i);
				ExcelCommon.SetCellValue(sheet, "F" + String.valueOf(CurrentLine), tool.RefNo);
				// Usage_Quantity
				if (tool.OPStructBOPLine.getItem().getType().equals("S4_IT_Tool")) {
					String ToolNumber = tool.OPStructBOPLine.getProperty("s4_BAT_ToolNumber");
					ExcelCommon.SetCellValue(sheet, "B" + String.valueOf(CurrentLine), ToolNumber);
				} else {
					ExcelCommon.SetCellValue(sheet, "B" + String.valueOf(CurrentLine), tool.OPStructBOPLine.getProperty("bl_item_item_id"));
				}
				String Name = sokonCommon.GetProperty(tool.OPStructBOPLine, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
				ExcelCommon.SetCellValue(sheet, "C" + String.valueOf(CurrentLine), Name);
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void ExportAttachment(int StartLine, TCComponentMfgBvrBOPLine PaintStat, XSSFSheet sheet) {
		// Attachments
		ArrayList Attachments = GetAttachments(PaintStat);
		int CurrentLine = StartLine;

		ExcelCommon.InsertRow(sheet, StartLine, Attachments.size());
		for (int m = 0; m < Attachments.size(); m++) {
			ExcelCommon.copyRows(sheet, sheet, StartLine, StartLine, StartLine + m + 1);
		}

		for (int i = 0; i < Attachments.size(); i++) {
			try {
				TCComponent Dataset = (TCComponent) Attachments.get(i);
				String Name = Dataset.getProperty("object_name");
				Date date = Dataset.getDateProperty("last_mod_date");
				String last_mod_date = new java.text.SimpleDateFormat("yyyyMMdd").format(date);
				String AttachmentType = GetAttachmentType(Dataset);
				ExcelCommon.SetCellValue(sheet, "B" + String.valueOf(CurrentLine), Name);
				ExcelCommon.SetCellValue(sheet, "R" + String.valueOf(CurrentLine), last_mod_date);
				ExcelCommon.SetCellValue(sheet, "Y" + String.valueOf(CurrentLine), AttachmentType);
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void InitPSStruct(TCComponentMfgBvrProcess PaintBOP) {
		try {
			AIFComponentContext[] Context = PaintBOP.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrProcess PaintProc = (TCComponentMfgBvrProcess) Context[i].getComponent();
				if (PaintProc.getItem().getType().equals("S4_IT_PaintProc")) {
					ArrayList PaintStatArray = GetPaintStat(PaintProc);
					for (int m = 0; m < PaintStatArray.size(); m++) {
						TCComponentMfgBvrBOPLine PaintStat = (TCComponentMfgBvrBOPLine) PaintStatArray.get(m);
						if (PaintStat != null) {
							TCComponentItem ProcessDoc = sokonCommon.GetProcessDoc(PaintStat, "涂装工艺卡", "Paint Process Card");
							if (ProcessDoc != null) {
								if (SOACommon.IsReleased(ProcessDoc.getLatestItemRevision())) {
									continue;
								}
							}
							PaintStatStruct PSStruct = new PaintStatStruct(PaintBOP, PaintProc, PaintStat, ProcessDoc);
							PSStructArray.add(PSStruct);
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	ArrayList GetPaintStat(TCComponentMfgBvrProcess PaintProc) {
		ArrayList PaintStatArray = new ArrayList();
		try {
			AIFComponentContext[] Context = PaintProc.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine PaintStat = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				if (PaintStat.getItem().getType().equals("S4_IT_PaintStat")) {
					PaintStatArray.add(PaintStat);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return PaintStatArray;
	}

	TCComponentMfgBvrBOPLine GetStation(TCComponentMfgBvrBOPLine PaintStat) {
		try {
			AIFComponentContext[] Context = PaintStat.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine Station = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				if (Station.getItem().getType().equals("S4_IT_Station")) {
					return Station;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	ArrayList GetOPTool(String RefNo, TCComponentMfgBvrBOPLine OPBOMLine) {
		ArrayList OPToolArray = new ArrayList();
		try {
			AIFComponentContext[] Context = OPBOMLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String Type = SubBOPLine.getItem().getType();
				if (Type.equals("S4_IT_ProcessAux")) {
					OneTool TempLine = new OneTool(RefNo, SubBOPLine);
					OPToolArray.add(TempLine);
					continue;
				}
				TCComponentMfgBvrBOPLine cc = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String bl_occ_type = cc.getProperty("bl_occ_type");
				if (bl_occ_type.equals("MEResource")) {
					OneTool TempLine = new OneTool(RefNo, SubBOPLine);
					OPToolArray.add(TempLine);
					continue;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return OPToolArray;
	}

	ArrayList GetPaintOP(TCComponentMfgBvrBOPLine PaintStat) {
		ArrayList Attachments = new ArrayList();
		try {
			AIFComponentContext[] Context = PaintStat.getChildren();
			for (int i = 0; i < Context.length; i++) {
				if (Context[i].getComponent() instanceof TCComponentMfgBvrBOPLine) {
					TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
					String Type = SubBOPLine.getItem().getType();
					if (Type.equals("S4_IT_PaintOP")) {
						Attachments.add(SubBOPLine);
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return Attachments;
	}

	ArrayList GetOPStruct(String RefNo, String Variant, TCComponentMfgBvrBOPLine OPBOMLine) {
		ArrayList Struct = new ArrayList();
		try {
			AIFComponentContext[] Context = OPBOMLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String Type = SubBOPLine.getItem().getType();

				// 过滤掉对象类型为S4_IT_PartRevision且其s4_AT_IsColorPart属性值为“COL/SURF”（LOV真实值）的对象
				if (Type.equals("S4_IT_Part")) {
					// s4_AT_IsColorPart
					// Get LOV BMIDE值 LOV真实值
					Object s4_AT_IsColorPart_Real = SubBOPLine.getItemRevision().getTCProperty("s4_AT_IsColorPart").getPropertyValue();
					String s4_AT_IsColorPart = SubBOPLine.getItemRevision().getProperty("s4_AT_IsColorPart");

					if (s4_AT_IsColorPart.equals("COL/SURF") || s4_AT_IsColorPart.equals("颜色件") || s4_AT_IsColorPart.equals("Color Parts")) {
						continue;
					}
				}
				if (Type.equals("S4_IT_OtherTool")) {
					OPStructLine TempLine = new OPStructLine(RefNo, Variant, SubBOPLine);
					Struct.add(TempLine);
					continue;
				}
				TCComponentMfgBvrBOPLine cc = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String bl_occ_type = cc.getProperty("bl_occ_type");
				if (bl_occ_type.equals("MEConsumed")) {
					OPStructLine TempLine = new OPStructLine(RefNo, Variant, SubBOPLine);
					Struct.add(TempLine);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return Struct;
	}

	ArrayList GetAttachments(TCComponentMfgBvrBOPLine PaintStat) {
		ArrayList Attachments = new ArrayList();
		try {
			AIFComponentContext[] Context = PaintStat.getItemRevision().getChildren();
			for (int i = 0; i < Context.length; i++) {
				String Type = Context[i].getComponent().getType();
				if (AttachmentType.containsKey(Type)) {
					Attachments.add(Context[i].getComponent());
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return Attachments;
	}

	String GetAttachmentType(TCComponent Dataset) {
		String Type = Dataset.getType();
		if (AttachmentType.containsKey(Type)) {
			return (String) AttachmentType.get(Type);
		}
		return "Unknown";
	}

	PicInfo DeleteOldPic_Del(XSSFSheet sheet, String PicName) {
		try {
			XSSFPicture OldPic = ExcelCommon.GetPicture(sheet, PicName);
			if (OldPic != null) {
				XSSFDrawing drawing = OldPic.getDrawing();
				XSSFPictureData data = OldPic.getPictureData();
				XSSFClientAnchor anchor = OldPic.getPreferredSize();
				CTMarker ctMarker = anchor.getFrom();
				int Row = ctMarker.getRow();
				int Col = ctMarker.getCol();

				String RelID = data.getPackageRelationship().getId();
				drawing.getPackagePart().removeRelationship(RelID);

				PackagePart part = data.getPackagePart();
				part.setDeleted(true);
				drawing.getPackagePart().getPackage().removePart(part);

				CTTwoCellAnchor[] Anch = drawing.getCTDrawing().getTwoCellAnchorArray();
				for (int mm = Anch.length - 1; mm >= 0; mm--) {
					String Name = Anch[mm].getPic().getNvPicPr().getCNvPr().getName();
					if (Name.equals(PicName)) {
						drawing.getCTDrawing().removeTwoCellAnchor(mm);
						break;
					}
				}
				PicInfo picinfo = new PicInfo(true, Row, Col);
				return picinfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}