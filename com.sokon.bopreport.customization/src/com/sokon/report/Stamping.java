package com.sokon.report;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

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

import com.sokon.report.data.sokonCommon;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrResource;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.zy.common.ExcelCommon;
import com.zy.common.ReportCommon;
import com.zy.common.SOACommon;

public class Stamping extends AbstractHandler implements IHandler {

	TCComponentDataset TemplateDatset = null;
	int Lang = -1;
	String Error = "";
	String TargetType = null;

	TCComponentMfgBvrProcess StampBOP = null;
	TCComponentItem ProcessDoc = null;

	ArrayList SheetMetalArray = new ArrayList();
	ArrayList METargetArray = new ArrayList();
	ArrayList StampOPArray = new ArrayList();
	ArrayList AUXArray = new ArrayList();
	ArrayList MEResourceArray = new ArrayList();

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
			Shell shell = AIFUtility.getActiveDesktop().getShell();

			try {
				TCComponentMfgBvrProcess TempMfg = (TCComponentMfgBvrProcess) Comp;
				TargetType = TempMfg.getItemRevision().getType();
				if (TargetType.equals("S4_IT_StampBOPRevision")) {
					StampBOP = TempMfg;
					ProcessDoc = sokonCommon.GetProcessDoc(StampBOP, "冲压工艺卡", "Stamp Process Card");
					if (ProcessDoc != null) {
						boolean b = MessageDialog.openConfirm(shell, Messages.LangDlg_Infomation, Messages.UpdateConfirm);
						if (!b) {
							return null;
						}
					}
				} else {
					MessageBox.post(Messages.InvalidType, Messages.LangDlg_Infomation, 2);
					return null;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			if (ProcessDoc != null) {
				TemplateDatset = (TCComponentDataset) ReportCommon.GetRelationComp(ProcessDoc.getLatestItemRevision(), "IMAN_specification", "MSExcelX");
				if (TemplateDatset == null) {
					MessageBox.post("No Excel found under ProcessDoc", Messages.LangDlg_Infomation, 2);
					return null;
				}
			} else {
				TemplateDatset = ReportCommon.GetTemplateDataset("SOKON_300_冲压工艺卡模板");
			}
			if (TemplateDatset == null)
				return null;

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

						GenerateReport(TemplateDatset, Lang);
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

	boolean GenerateReport(TCComponentDataset TemplateDatset, int Lang) {
		try {
			String DateStr = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String NewFileName = "StampReport_" + DateStr + ".xlsx";

			File TempDir = new File("C:\\temp\\");
			if (!TempDir.exists()) {
				TempDir.mkdir();
			}
			File TemplateFile = ReportCommon.DownloadReportTemplate(TemplateDatset, NewFileName, TempDir.getAbsolutePath());
			if (TemplateFile != null) {
				if (FileData2Excel(TemplateFile, StampBOP, TempDir)) {
					String DocItemName = "Kartu Teknikal Stamping";
					TCComponentItemRevision DocItemRev = null;
					if (ProcessDoc == null) {
						TCComponentItem DocItem = SOACommon.createItem("S4_IT_ProcessDoc", "", DocItemName, "", "A");
						DocItemRev = DocItem.getLatestItemRevision();
						StampBOP.getItemRevision().add("IMAN_reference", DocItemRev.getItem());
						try {
							DocItemRev.setProperty("s4_AT_DocumentType", "StampProcessCard");
							if (ReportCommon.IsEnglisth()) {
								// DocItemRev.setProperty("s4_AT_DocumentType", "Stamp Process Card");
							} else {
								// DocItemRev.setProperty("s4_AT_DocumentType", "冲压工艺卡");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						DocItemRev = ProcessDoc.getLatestItemRevision();
						if (SOACommon.IsReleased(DocItemRev)) {
							DocItemRev = DocItemRev.saveAs("");
						}
					}
					if (DocItemRev != null) {
						String ExcelName = StampBOP.getItemRevision().getProperty("object_name");
						ReportCommon.createOrUpdateDataset(TemplateFile.getAbsolutePath(), "MSExcelX", "excel", ExcelName, DocItemRev, "IMAN_specification", false);
						// TemplateFile.delete();
					}
					return true;
				} else {
					Error = Error + "Report created failed\n";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	String GetMergeItemID() {
		String ItemID = "";
		for (int i = 0; i < METargetArray.size(); i++) {
			try {
				TCComponentMfgBvrBOPLine Temp = (TCComponentMfgBvrBOPLine) METargetArray.get(i);
				if (i == 0)
					ItemID = Temp.getItem().getProperty("item_id");
				else
					ItemID = ItemID + "/" + Temp.getItem().getProperty("item_id");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ItemID;
	}

	String GetMergeItemName() {
		String ItemName = "";
		for (int i = 0; i < METargetArray.size(); i++) {
			try {
				TCComponentMfgBvrBOPLine Temp = (TCComponentMfgBvrBOPLine) METargetArray.get(i);
				String Name = sokonCommon.GetProperty(Temp, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
				if (i == 0)
					ItemName = Name;
				else
					ItemName = ItemName + "/" + Name;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ItemName;
	}

	void ClearTemplate(XSSFWorkbook wb) {
		ArrayList FirstClear = ReportCommon.LoadProp("StampClear.properties", "First");
		ArrayList SecondClear = ReportCommon.LoadProp("StampClear.properties", "Second");
		int sheetNum = wb.getNumberOfSheets();
		for (int i = 0; i < sheetNum; i++) {
			XSSFSheet sheet = wb.getSheetAt(i);
			if (i == 0) {
				for (int m = 0; m < FirstClear.size(); m++) {
					ExcelCommon.SetCellValue(sheet, FirstClear.get(m).toString(), "");
				}
			} else {
				for (int m = 0; m < SecondClear.size(); m++) {
					ExcelCommon.SetCellValue(sheet, SecondClear.get(m).toString(), "");
				}
			}
		}
	}

	boolean FileData2Excel(File TemplateFile, TCComponentMfgBvrProcess StampBOP, File TempDir) {
		try {
			FileInputStream fileInputStream = new FileInputStream(TemplateFile);
			XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream(fileInputStream));
			XSSFSheet sheet = wb.getSheetAt(0);

			if (ProcessDoc != null) {
				ClearTemplate(wb);
			}
			// Init All data
			SheetMetalArray.clear();
			METargetArray.clear();
			StampOPArray.clear();
			GetStampOP(StampBOP);
			GetMETarget(StampBOP);
			GetSheetMetal(StampBOP);

			// Variant
			String Variant = StampBOP.getItemRevision().getProperty("s4_AT_EngineeringModel");
			ExcelCommon.SetCellValue(sheet, "F1", Variant);
			// PROSES
			ExcelCommon.SetCellValue(sheet, "AF1", String.valueOf(StampOPArray.size()));
			// NO. PART
			ExcelCommon.SetCellValue(sheet, "W1", GetMergeItemID());
			// NAMA PART
			ExcelCommon.SetCellValue(sheet, "W2", GetMergeItemName());
			// 岗位定编JOB SET
			String PersonneQuota = StampBOP.getItemRevision().getProperty("s4_AT_PersonneQuota");
			ExcelCommon.SetCellValue(sheet, "AO2", PersonneQuota);
			ExcelCommon.SetCellValue(sheet, "AT18", PersonneQuota);
			// STASIUN
			TCComponent Line = GetLine(StampBOP);
			if (Line != null) {
				String LineName = sokonCommon.GetProperty(Line, "s4_CAT_ChineseName", "object_name", Lang);
				ExcelCommon.SetCellValue(sheet, "AF2", LineName);
			}

			// 材料明细表
			ExportMaterial(5, SheetMetalArray, sheet);
			ExportOPList(18, StampOPArray, sheet);

			// Copy sheet and set SheetName
			int SheetNum = wb.getNumberOfSheets();
			for (int i = 0; i < StampOPArray.size(); i++) {
				try {
					int SheetIndex = i + 1;
					XSSFSheet sheet1 = null;
					if (SheetIndex > SheetNum - 1) {
						XSSFSheet oriSheet = wb.getSheetAt(1);
						sheet1 = wb.cloneSheet(1);
						try {
							wb.setPrintArea(wb.getSheetIndex(sheet1), 0, 47, 0, 57);

							sheet1.setMargin(XSSFSheet.TopMargin, oriSheet.getMargin(XSSFSheet.TopMargin));// 页边距（上）
							sheet1.setMargin(XSSFSheet.BottomMargin, oriSheet.getMargin(XSSFSheet.BottomMargin));// 页边距（下）
							sheet1.setMargin(XSSFSheet.LeftMargin, oriSheet.getMargin(XSSFSheet.LeftMargin));// 页边距（左）
							sheet1.setMargin(XSSFSheet.RightMargin, oriSheet.getMargin(XSSFSheet.RightMargin));// 页边距（右

							sheet1.getPrintSetup().setLandscape(oriSheet.getPrintSetup().getLandscape());
							sheet1.getPrintSetup().setOrientation(oriSheet.getPrintSetup().getOrientation());
							sheet1.getPrintSetup().setFooterMargin(oriSheet.getPrintSetup().getFooterMargin());
							sheet1.getPrintSetup().setHeaderMargin(oriSheet.getPrintSetup().getHeaderMargin());
							sheet1.getPrintSetup().setLeftToRight(oriSheet.getPrintSetup().getLeftToRight());
							sheet1.getPrintSetup().setScale(oriSheet.getPrintSetup().getScale());
							sheet1.getPrintSetup().setPaperSize(oriSheet.getPrintSetup().getPaperSize());
							sheet1.getPrintSetup().setPageStart(oriSheet.getPrintSetup().getPageStart());
							sheet1.getPrintSetup().setPageOrder(oriSheet.getPrintSetup().getPageOrder());
							sheet1.getPrintSetup().setVResolution(oriSheet.getPrintSetup().getVResolution());
							sheet1.getPrintSetup().setHResolution(oriSheet.getPrintSetup().getHResolution());
							sheet1.setHorizontallyCenter(oriSheet.getHorizontallyCenter());// 设置打印页面为水平居中
							sheet1.setVerticallyCenter(oriSheet.getVerticallyCenter());//

							// sheet1.getPrintSetup().setFitHeight(oriSheet.getPrintSetup().getFitHeight());
							// sheet1.getPrintSetup().setFitWidth(oriSheet.getPrintSetup().getFitWidth());
							// sheet1.getPrintSetup().setNoOrientation(oriSheet.getPrintSetup().getNoOrientation());
							// sheet1.getPrintSetup().setUsePage(oriSheet.getPrintSetup().getUsePage());
							// sheet1.getPrintSetup().setNotes(oriSheet.getPrintSetup().getNotes());
							// sheet1.getPrintSetup().setValidSettings(oriSheet.getPrintSetup().getValidSettings());
							// sheet1.getPrintSetup().setDraft(oriSheet.getPrintSetup().getDraft());
							// sheet1.setAutobreaks(oriSheet.getAutobreaks());
							// sheet1.setFitToPage(oriSheet.getFitToPage());
							// sheet1.setRepeatingColumns(oriSheet.getRepeatingColumns());
							// sheet1.setRepeatingRows(oriSheet.getRepeatingRows());
							// sheet1.setFitToPage(oriSheet.getFitToPage());
						} catch (Exception e3) {
							e3.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// Fill Sheet
			for (int i = 0; i < StampOPArray.size(); i++) {
				try {
					// 表头
					TCComponentMfgBvrBOPLine OP = (TCComponentMfgBvrBOPLine) StampOPArray.get(i);
					sheet = wb.getSheetAt(i + 1);
					// Variant
					ExcelCommon.SetCellValue(sheet, "F1", Variant);
					// NO. PART
					ExcelCommon.SetCellValue(sheet, "W1", GetMergeItemID());
					// NAMA PART
					ExcelCommon.SetCellValue(sheet, "W2", GetMergeItemName());
					// 工序
					String OperationNumber = OP.getProperty("s4_BAT_OperationNumber");
					ExcelCommon.SetCellValue(sheet, "AG1", OperationNumber);
					ExcelCommon.SetSheetName(sheet, OperationNumber);
					// Station NO

					// 工位地址 STASIUN 非首页
					TCComponentMfgBvrBOPLine Station = sokonCommon.GetSource(OP, "MEWorkArea", "S4_IT_Station");

					String StationID = "";
					if (Station != null) {
						StationID = Station.getItem().getProperty("item_id");
					}
					// Not sure how to get Mfg0processResource
					TCComponent Mfg0processResource = OP.getReferenceProperty("Mfg0processResource");
					if (Mfg0processResource != null) {
						String Type = Mfg0processResource.getType();
						if (Type.equals("S4_IT_WorkerRevision")) {
							Object ProcResArea = Mfg0processResource.getTCProperty("s4_AT_ProcResArea").getPropertyValue();
							String ProcResArea_Val = "";
							if (ProcResArea != null) {
								ProcResArea_Val = ProcResArea.toString();
							}
							if (ProcResArea_Val.length() == 0) {
								ProcResArea_Val = "0";
							}
							StationID = StationID + ProcResArea_Val;
						}
					}
					if (StationID.length() > 7) {
						StationID = StationID.substring(StationID.length() - 7);
					}
					ExcelCommon.SetCellValue(sheet, "AG2", StationID);
					// 工艺参数
					String DieSetHeight = OP.getProperty("s4_BAT_DieSetHeight");
					ExcelCommon.SetCellValue(sheet, "H5", DieSetHeight);

					String TopBarPressure = OP.getProperty("s4_BAT_TopBarPressure");
					ExcelCommon.SetCellValue(sheet, "H7", TopBarPressure);
					// 顶杆行程
					String LiftRodStroke = OP.getProperty("s4_BAT_LiftRodStroke");
					ExcelCommon.SetCellValue(sheet, "H6", LiftRodStroke);

					// 设备、工装、工具
					MEResourceArray.clear();
					GetMEResrouces(OP);
					ExportResource(5, MEResourceArray, sheet);
					// 辅料
					AUXArray.clear();
					GetAUX(OP);
					ExportAUX(16, AUXArray, sheet);
					// 技术要求
					String TechnicalRequirement = OP.getProperty("s4_BAT_TechnicalRequirement");
					ExcelCommon.SetCellValue(sheet, "T15", TechnicalRequirement);
					// Picture

					ArrayList SnapShot = ReportCommon.Get3DSnapShot(OP);
					ExcelCommon.ExportPicture(28, SnapShot, sheet, TempDir);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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

	void ExportAUX(int StartLine, ArrayList AUXArray, XSSFSheet sheet) {
		int CurrentLine = StartLine;
		for (int i = 0; i < AUXArray.size(); i++) {
			try {
				TCComponentMfgBvrBOPLine AUX = (TCComponentMfgBvrBOPLine) AUXArray.get(i);
				String Name = sokonCommon.GetProperty(AUX, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
				ExcelCommon.SetCellValue(sheet, "C" + String.valueOf(CurrentLine), Name);
				// 规格型号
				ExcelCommon.SetCellValue(sheet, "H" + String.valueOf(CurrentLine), AUX.getProperty("s4_BAT_SpecificationModel"));
				// S4_NT_Remark
				String S4_NT_Remark = AUX.getProperty("S4_NT_Remarks");
				ExcelCommon.SetCellValue(sheet, "Q" + String.valueOf(CurrentLine), S4_NT_Remark);
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void ExportResource(int StartLine, ArrayList MEResourcceArray, XSSFSheet sheet) {
		int CurrentLine = StartLine;
		for (int i = 0; i < MEResourcceArray.size(); i++) {
			try {
				TCComponentMfgBvrBOPLine OPBOP = (TCComponentMfgBvrBOPLine) MEResourcceArray.get(i);
				String Name = sokonCommon.GetProperty(OPBOP, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
				ExcelCommon.SetCellValue(sheet, "Z" + String.valueOf(CurrentLine), Name);
				// 规格型号
				String Model = OPBOP.getProperty("s4_BAT_SpecificationModel");
				ExcelCommon.SetCellValue(sheet, "AF" + String.valueOf(CurrentLine), Model);
				// Usage_Quantity
				String Usage_Quantity = OPBOP.getProperty("Usage_Quantity");
				if (Usage_Quantity.length() == 0) {
					Usage_Quantity = "1";
				}
				ExcelCommon.SetCellValue(sheet, "AP" + String.valueOf(CurrentLine), Usage_Quantity);
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void ExportOPList(int StartLine, ArrayList StampOPArray, XSSFSheet sheet) {
		int CurrentLine = StartLine;
		for (int i = 0; i < StampOPArray.size(); i++) {
			try {
				TCComponentMfgBvrBOPLine OPBOP = (TCComponentMfgBvrBOPLine) StampOPArray.get(i);
				String OperationNumber = OPBOP.getProperty("s4_BAT_OperationNumber");
				ExcelCommon.SetCellValue(sheet, "AD" + String.valueOf(CurrentLine), OperationNumber);
				String Name = sokonCommon.GetProperty(OPBOP, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
				ExcelCommon.SetCellValue(sheet, "AF" + String.valueOf(CurrentLine), Name);
				// 1. 设备名称NAMA MESIN S4_IT_PressMachRevision s4_AT_SpecificationModel
				TCComponentMfgBvrBOPLine SheBei = sokonCommon.GetSourceLevel2(OPBOP, "S4_IT_PressMach");
				if (SheBei != null) {
					String SpecificationModel = SheBei.getItemRevision().getProperty("s4_AT_SpecificationModel");
					ExcelCommon.SetCellValue(sheet, "AK" + String.valueOf(CurrentLine), SpecificationModel);
				}
				// 模具名称NAMA DIES S4_IT_DieRevision bl_rev_object_name bl_rev_s4_CAT_ChineseName
				TCComponent Die = sokonCommon.GetSourceLevel2(OPBOP, "S4_IT_Die");
				if (Die != null) {
					String DieName = sokonCommon.GetProperty(Die, "bl_rev_s4_CAT_ChineseName", "bl_rev_object_name", Lang);
					ExcelCommon.SetCellValue(sheet, "AO" + String.valueOf(CurrentLine), DieName);
				}
				CurrentLine++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void ExportMaterial(int StartLine, ArrayList SheetMetalArray, XSSFSheet sheet) {
		// Attachments
		int CurrentLine = StartLine;
		for (int i = 0; i < SheetMetalArray.size(); i++) {
			try {
				// 第一列
				ExcelCommon.SetCellValue(sheet, "AD" + String.valueOf(CurrentLine), String.valueOf(i * 3 + 1));
				ExcelCommon.SetCellValue(sheet, "AD" + String.valueOf(CurrentLine + 1), String.valueOf(i * 3 + 2));
				ExcelCommon.SetCellValue(sheet, "AD" + String.valueOf(CurrentLine + 2), String.valueOf(i * 3 + 3));

				// 第二列
				ExcelCommon.SetCellValue(sheet, "AF" + String.valueOf(CurrentLine), "材料牌号\nKODE MATERIAL");
				ExcelCommon.SetCellValue(sheet, "AF" + String.valueOf(CurrentLine + 1), "材料规格尺寸（mm）\nUKURAN MATERIAL");
				ExcelCommon.SetCellValue(sheet, "AF" + String.valueOf(CurrentLine + 2), "每张可制件数\nHASIL PRODUKSI TIAP LEMBAR");

				// 第三列
				TCComponentMfgBvrBOPLine Material = (TCComponentMfgBvrBOPLine) SheetMetalArray.get(i);
				TCComponent ItemRev = Material.getItemRevision();
				ExcelCommon.SetCellValue(sheet, "AO" + String.valueOf(CurrentLine), ItemRev.getProperty("s4_AT_Material"));

				String Length = ItemRev.getProperty("s4_AT_Length");
				String Width = ItemRev.getProperty("s4_AT_Width");
				String Thickness = ItemRev.getProperty("s4_AT_Thickness");
				ExcelCommon.SetCellValue(sheet, "AO" + String.valueOf(CurrentLine + 1), Length + "X" + Width + "X" + Thickness);

				String JianShu = "1件";
				if (SheetMetalArray.size() == 1 && METargetArray.size() == 2) {
					JianShu = "2件";
				}
				ExcelCommon.SetCellValue(sheet, "AO" + String.valueOf(CurrentLine + 2), JianShu);

				CurrentLine = CurrentLine + 3;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void GetSheetMetal(TCComponentMfgBvrBOPLine BOPLine) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String Type = SubBOPLine.getItem().getType();
				if (Type.equals("S4_IT_SheetMetal")) {
					SheetMetalArray.add(SubBOPLine);
					continue;
				}
				GetSheetMetal(SubBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	void GetMETarget(TCComponentMfgBvrBOPLine BOPLine) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String bl_occ_type = SubBOPLine.getProperty("bl_occ_type");
				if (bl_occ_type.equals("METarget")) {
					String Type = SubBOPLine.getItem().getType();
					if (Type.equals("S4_IT_Part")) {
						METargetArray.add(SubBOPLine);
					}
				}
				GetMETarget(SubBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	TCComponent GetLine(TCComponentMfgBvrBOPLine BOPLine) {
		try {
			TCComponent[] MEWorkArea = BOPLine.getItemRevision().getRelatedComponents("IMAN_MEWorkArea");
			for (int m = 0; m < MEWorkArea.length; m++) {
				String Type = MEWorkArea[m].getType();
				if (Type.equals("S4_IT_LineRevision")) {
					return MEWorkArea[m];
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	// MEResouceArray
	void GetMEResrouces(TCComponentMfgBvrBOPLine BOPLine) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				// String bl_occ_type = SubBOPLine.getProperty("bl_occ_type");
				// if (bl_occ_type.equals("MEWorkArea")) {
				if (SubBOPLine instanceof TCComponentMfgBvrResource) {
					String Type = SubBOPLine.getItem().getType();
					if (!Type.equals("S4_IT_ProcessAux")) {
						MEResourceArray.add(SubBOPLine);
					}
				}
				// }
				GetMEResrouces(SubBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	// AUXArray
	void GetAUX(TCComponentMfgBvrBOPLine BOPLine) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String Type = SubBOPLine.getItem().getType();
				if (Type.equals("S4_IT_ProcessAux")) { // S4_IT_AuxPart
					AUXArray.add(SubBOPLine);
				}
				GetAUX(SubBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	void GetStampOP(TCComponentMfgBvrBOPLine BOPLine) {
		try {
			AIFComponentContext[] Context = BOPLine.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponentMfgBvrBOPLine SubBOPLine = (TCComponentMfgBvrBOPLine) Context[i].getComponent();
				String Type = SubBOPLine.getItem().getType();
				if (Type.equals("S4_IT_StampOP")) {
					StampOPArray.add(SubBOPLine);
				}
				GetStampOP(SubBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

}