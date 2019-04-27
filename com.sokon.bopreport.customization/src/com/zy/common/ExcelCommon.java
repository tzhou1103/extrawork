package com.zy.common;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;

import com.sokon.report.data.PicInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;

public class ExcelCommon {

	public static void SetPicName(XSSFPicture PIC, String PicName) {
		try {
			PIC.getCTPicture().getNvPicPr().getCNvPr().setName(PicName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ExportPicture(int StartLine, ArrayList SnapShot, XSSFSheet sheet, File TempDir) {
		XSSFWorkbook wb = sheet.getWorkbook();
		for (int i = 0; i < SnapShot.size(); i++) {
			try {
				TCComponent Dataset = (TCComponent) SnapShot.get(i);
				String DatasetName = Dataset.getProperty("object_name");
				String Type = Dataset.getType();
				String PicName = DatasetName;

				String referenceName = "Image";
				if (Type.equals("S4_DA_FirstLevImage")) {
					referenceName = "S4_Image";
				}
				File ImgFile = ReportCommon.datasetFileToLocalDir((TCComponentDataset) Dataset, referenceName, TempDir.getAbsolutePath());
				if (ImgFile != null) {
					System.out.println(ImgFile.getAbsolutePath());
					try {
						int Col = 2;
						int Row = StartLine;

						String PicType = "";
						int POIType = -1;
						if (ImgFile.getAbsolutePath().toLowerCase().endsWith(".jpg")) {
							PicType = "jpg";
							POIType = XSSFWorkbook.PICTURE_TYPE_JPEG;
						} else if (ImgFile.getAbsolutePath().toLowerCase().endsWith(".jpeg")) {
							PicType = "jpeg";
							POIType = XSSFWorkbook.PICTURE_TYPE_JPEG;
						} else if (ImgFile.getAbsolutePath().toLowerCase().endsWith(".png")) {
							PicType = "png";
							POIType = XSSFWorkbook.PICTURE_TYPE_PNG;
						} else if (ImgFile.getAbsolutePath().toLowerCase().endsWith(".bmp")) {
							PicType = "bmp";
							POIType = XSSFWorkbook.PICTURE_TYPE_BMP;
						} else {
							System.out.println("Unsupport Image File");
							continue;
						}

						ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
						BufferedImage bufferImg = ImageIO.read(ImgFile);
						boolean ret = ImageIO.write(bufferImg, PicType, byteArrayOut);

						PicInfo picinfo = DeleteOldPic(sheet, PicName);
						if (picinfo != null) {
							if ((picinfo.Exsit) && (picinfo.row > 0)) {
								Row = picinfo.row;
								Col = picinfo.col;
							}
						}
						XSSFDrawing patriarch = sheet.createDrawingPatriarch();
						XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) Col, Row, (short) Col, Row);
						XSSFPicture PIC = patriarch.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), POIType));
						PIC.resize();
						ExcelCommon.SetPicName(PIC, PicName);

						ImgFile.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void SetSheetName(XSSFSheet sheet, String SheetName) {
		if (SheetName.length() > 0) {
			try {
				sheet.getWorkbook().setSheetName(sheet.getWorkbook().getSheetIndex(sheet), SheetName);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static PicInfo DeleteOldPic(XSSFSheet sheet, String PicName) {
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

	public static String GetPicName(XSSFPicture PIC) {
		try {
			return PIC.getCTPicture().getNvPicPr().getCNvPr().getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static XSSFPicture GetPicture(XSSFSheet sheet, String Name) {

		List drlist = sheet.getRelations();
		for (int i = 0; i < drlist.size(); i++) {
			if (drlist.get(i) instanceof XSSFDrawing) {
				XSSFDrawing drawing = (XSSFDrawing) drlist.get(i);
				List shapes = drawing.getShapes();
				for (int m = 0; m < shapes.size(); m++) {
					XSSFPicture pic = (XSSFPicture) shapes.get(m);
					String Name2 = GetPicName(pic);
					if (Name2.equals(Name)) {
						return pic;
					}
				}
			}
		}
		return null;
	}

	// 创建一行 样式按上行
	public static XSSFRow CreateRowAndCell(XSSFWorkbook wb, XSSFSheet sheet, int RowNum, String StrStandRowNum) {
		XSSFRow Row = sheet.getRow(RowNum);
		int StandRowNum = 5;
		try {
			StandRowNum = Integer.parseInt(StrStandRowNum);
		} catch (Exception e) {

		}
		XSSFRow StandRow = sheet.getRow(StandRowNum);
		if ((Row == null) && (StandRow != null)) {
			Row = sheet.createRow(RowNum);
			for (int i = 0; i < StandRow.getLastCellNum(); i++) {
				XSSFCell StandCell = StandRow.getCell(i);
				if (StandCell != null) {
					XSSFCell Cell = Row.createCell(i);
					if (Cell != null) {
						XSSFCellStyle StandStyle = StandCell.getCellStyle();
						XSSFCellStyle Style = wb.createCellStyle();
						Style.setBorderBottom(StandStyle.getBorderBottom());
						Style.setBorderLeft(StandStyle.getBorderLeft());
						Style.setBorderRight(StandStyle.getBorderRight());
						Style.setBorderTop(StandStyle.getBorderTop());
						XSSFFont oldfont = wb.getFontAt(StandStyle.getFontIndex());
						Style.setFont(oldfont);
						Style.setVerticalAlignment(StandStyle.getVerticalAlignment());
						Style.setAlignment(StandStyle.getAlignment());
						Style.setWrapText(StandStyle.getWrapText());
						Style.setDataFormat(StandStyle.getDataFormat());
						Style.setHidden(StandStyle.getHidden());
						Style.setIndention(StandStyle.getIndention());
						Style.setRotation(StandStyle.getRotation());
						Style.setFillBackgroundColor(StandStyle.getFillBackgroundColor());
						Style.setFillForegroundColor(StandStyle.getFillForegroundColor());
						Style.setFillPattern(StandStyle.getFillPattern());
						// 设置
						Cell.setCellStyle(Style);
					}
				}
			}
		}
		return Row;
	}

	public static void setStringCellValue(XSSFRow row, String CellColName, String cellString) {
		try {
			int cellNum = GetCol(CellColName);
			if (cellString != null) {
				XSSFCell cell = row.getCell((short) cellNum);
				if (cell == null) {
					cell = row.createCell((short) cellNum);
				}
				// cell.setEncoding(HSSFCell.ENCODING_UTF_16);
				cell.setCellValue(cellString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setNumberCellValue(XSSFRow row, String CellColName, int cellString) {
		try {
			int cellNum = GetCol(CellColName);
			XSSFCell cell = row.getCell((short) cellNum);
			if (cell == null) {
				cell = row.createCell((short) cellNum);
			}
			cell.setCellValue(cellString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 填写值到cell
	 * 
	 * @param wb
	 * @param cell
	 * @param str
	 */
	public static void WbFillCell(XSSFSheet DestSheet, XSSFCell cell, String str) {
		try {
			if (str.compareTo("") == 0) {
				// return;
			}
			if (cell != null) {
				// cell.setEncoding(XSSFCell.ENCODING_UTF_16);
				cell.setCellValue(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// POS =A3 具体格子坐标
	public static XSSFCell GetCell(XSSFSheet sheet0, int Row, int Col) {
		XSSFRow Sheet0Row = sheet0.getRow(Row);
		if (Sheet0Row == null) {
			Sheet0Row = sheet0.createRow(Row);
		}
		XSSFCell Sheet0Cell = Sheet0Row.getCell(Col);
		{
			if (Sheet0Cell == null) {
				Sheet0Cell = Sheet0Row.createCell(Col);
			}
		}
		return Sheet0Cell;
	}

	// POS =A3 具体格子坐标
	public static XSSFCell GetCell(XSSFSheet sheet0, String POS) {
		try {
			int Col = GetCol(POS);
			int Row = Integer.parseInt(GetNum(POS)) - 1;
			XSSFRow Sheet0Row = sheet0.getRow(Row);
			if (Sheet0Row == null) {
				Sheet0Row = sheet0.createRow(Row);
			}
			XSSFCell Sheet0Cell = Sheet0Row.getCell(Col);
			{
				if (Sheet0Cell == null) {
					Sheet0Cell = Sheet0Row.createCell(Col);
				}
			}
			return Sheet0Cell;
		} catch (Exception e) {
			//
		}
		return null;
	}

	public static void copyCell(XSSFCell srcCell, XSSFCell distCell) {
		distCell.setCellStyle(srcCell.getCellStyle());
		if (srcCell.getCellComment() != null) {
			distCell.setCellComment(srcCell.getCellComment());
		}
		int srcCellType = srcCell.getCellType();
		distCell.setCellType(srcCellType);
		if (srcCellType == XSSFCell.CELL_TYPE_NUMERIC) {
			// if (XSSFDateUtil.isCellDateFormatted(srcCell)) {
			// distCell.setCellValue(srcCell.getDateCellValue());
			// } else {
			distCell.setCellValue(srcCell.getNumericCellValue());
			// }
		} else if (srcCellType == XSSFCell.CELL_TYPE_STRING) {
			distCell.setCellValue(srcCell.getRichStringCellValue());
		} else if (srcCellType == XSSFCell.CELL_TYPE_BLANK) {
			// nothing
		} else if (srcCellType == XSSFCell.CELL_TYPE_BOOLEAN) {
			distCell.setCellValue(srcCell.getBooleanCellValue());
		} else if (srcCellType == XSSFCell.CELL_TYPE_ERROR) {
			distCell.setCellErrorValue(srcCell.getErrorCellValue());
		} else if (srcCellType == XSSFCell.CELL_TYPE_FORMULA) {
			distCell.setCellFormula(srcCell.getCellFormula());
		} else {
			// nothing
		}
	}

	public static void copyCellStyle(XSSFCellStyle fromStyle, XSSFCellStyle toStyle) {
		toStyle.setAlignment(fromStyle.getAlignment());
		// 边框和边框颜色
		toStyle.setBorderBottom(fromStyle.getBorderBottom());
		toStyle.setBorderLeft(fromStyle.getBorderLeft());
		toStyle.setBorderRight(fromStyle.getBorderRight());
		toStyle.setBorderTop(fromStyle.getBorderTop());
		toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
		toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
		toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
		toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());

		// 背景和前景
		toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
		toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());

		toStyle.setDataFormat(fromStyle.getDataFormat());
		toStyle.setFillPattern(fromStyle.getFillPattern());
		// toStyle.setFont(fromStyle.getFont(null));
		toStyle.setHidden(fromStyle.getHidden());
		toStyle.setIndention(fromStyle.getIndention());// 首行缩进
		toStyle.setLocked(fromStyle.getLocked());
		toStyle.setRotation(fromStyle.getRotation());// 旋转
		toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());
		toStyle.setWrapText(fromStyle.getWrapText());

	}

	public static void copyTemplate(String exTemplateFilePath, String targetFilePath) throws Exception {
		FileInputStream fileInputStream = new FileInputStream(exTemplateFilePath);
		XSSFWorkbook wb = new XSSFWorkbook(new BufferedInputStream(fileInputStream));
		XSSFSheet sheet = wb.getSheetAt(0);
		int lastRow = sheet.getLastRowNum() + 1;

		copyRows(sheet, wb.getSheetAt(0), 1, lastRow, 20);

		FileOutputStream fileOut = new FileOutputStream(targetFilePath);
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}

	public static boolean DeleteMergeRegin(XSSFSheet sourceSheet, int StartRow, int EndRow) {
		int mergedc1 = sourceSheet.getNumMergedRegions();
		for (int i = 0; i < mergedc1; i++) {
			CellRangeAddress region = sourceSheet.getMergedRegion(i);// .getMergedRegionAt(i);
			int rf = region.getFirstRow();// .getRowFrom();
			int rt = region.getLastRow();// .getRowTo();
			if ((rf >= StartRow) && (rt <= EndRow)) {
				sourceSheet.removeMergedRegion(i);
				return true;
			}
		}
		return false;
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

	public static void copyRows(XSSFSheet sourceSheet, XSSFSheet targetSheet, int startRow, int endRow, int targetPosition) {
		int pStartRow = startRow - 1;
		int pEndRow = endRow - 1;
		int pPosition = targetPosition - 1;
		XSSFRow sourceRow = null, targetRow = null;
		XSSFCell sourceCell = null;
		XSSFCell targetCell = null;
		CellRangeAddress region = null;
		int cType, i, j, targetRowFrom, targetRowTo;

		if (pStartRow < 0 || pEndRow < 0 || pStartRow > pEndRow) {
			return;
		}

		// 获取合并行单元格的数目,并设定目标单元格合并属性
		int mergedc = sourceSheet.getNumMergedRegions();
		for (i = 0; i < mergedc; i++) {
			region = sourceSheet.getMergedRegion(i);// .getMergedRegionAt(i);
			int rf = region.getFirstRow();// .getRowFrom();
			int rt = region.getLastRow();// .getRowTo();
			if ((rf >= pStartRow) && (rt <= pEndRow)) {
				targetRowFrom = rf - pStartRow + pPosition;
				targetRowTo = rt - pStartRow + pPosition;
				region.setFirstRow(targetRowFrom);
				region.setLastRow(targetRowTo);
				targetSheet.addMergedRegion(region);
			}
		}
		// 设定个单元格的列宽
		for (i = pStartRow; i <= pEndRow; i++) {
			sourceRow = sourceSheet.getRow(i);
			if (sourceRow != null) {
				int firstC = sourceRow.getFirstCellNum();
				for (j = sourceRow.getLastCellNum(); j > firstC; j--) {
					targetSheet.setColumnWidth(j, sourceSheet.getColumnWidth(j));
					targetSheet.setColumnHidden(j, false);
				}
				break;
			}
		}
		// 填充数据
		for (; i <= pEndRow; i++) {
			sourceRow = sourceSheet.getRow(i);
			if (sourceRow == null) {
				continue;
			}

			targetRow = targetSheet.createRow(i - pStartRow + pPosition);
			targetRow.setHeight(sourceRow.getHeight());
			// int psy = sourceRow.getPhysicalNumberOfCells();
			int psy = sourceRow.getLastCellNum();
			for (j = sourceRow.getFirstCellNum(); j < psy; j++) {
				try {
					sourceCell = sourceRow.getCell(j);
					if (sourceCell == null) {
						continue;
					}
					targetCell = targetRow.createCell(j);
					XSSFCellStyle style = sourceCell.getCellStyle();
					targetCell.setCellStyle(style);

					cType = sourceCell.getCellType();
					targetCell.setCellType(cType);

					switch (cType) {
					case HSSFCell.CELL_TYPE_BOOLEAN:
						targetCell.setCellValue(sourceCell.getBooleanCellValue());
						break;
					case HSSFCell.CELL_TYPE_ERROR:
						targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						// targetCell.setCellFormula(parseFormula(sourceCell.getCellFormula()));
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						targetCell.setCellValue(sourceCell.getNumericCellValue());
						break;
					case HSSFCell.CELL_TYPE_STRING:
						targetCell.setCellValue(sourceCell.getRichStringCellValue());
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void InsertRow(XSSFSheet sheet, int startRow, int rows) {
		try {
			if (rows == 0)
				return;
			sheet.shiftRows(startRow, sheet.getLastRowNum(), rows, true, true);
			rows = 0;
			for (int i = 0; i < rows; i++) {
				try {
					XSSFCell sourceCell = null;
					XSSFCell targetCell = null;
					XSSFRow sourceRow = sheet.createRow(startRow);
					XSSFRow targetRow = sheet.getRow(startRow + rows);

					sourceRow.setHeight(targetRow.getHeight());
					for (int m = targetRow.getFirstCellNum(); m < targetRow.getPhysicalNumberOfCells(); m++) {
						sourceCell = sourceRow.createCell(m);
						targetCell = targetRow.getCell(m);
						sourceCell.setCellStyle(targetCell.getCellStyle());
						sourceCell.setCellType(targetCell.getCellType());
					}

					startRow++;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class ClientAnchorInfo {
		public XSSFSheet sheet;
		public EscherClientAnchorRecord clientAnchorRecord;

		public ClientAnchorInfo(XSSFSheet sheet, EscherClientAnchorRecord clientAnchorRecord) {
			super();
			this.sheet = sheet;
			this.clientAnchorRecord = clientAnchorRecord;
		}
	}

	/**
	 * 返回Col
	 * 
	 * @param Pos
	 * @return
	 */
	public static int GetCol(String Pos) {
		// BB AA B
		int Col = 0;
		String TempPos = GetStr(Pos);
		int len = TempPos.length();
		for (int j = 1; j <= len; j++) {
			Col = (TempPos.charAt(j - 1) - 'A' + 1) * (int) Math.pow(26, len - j) + Col;
		}
		Col = Col - 1;
		return Col;
	}

	public static String GetCellValue(XSSFSheet sheet0, String POS) {
		XSSFCell cell = GetCell(sheet0, POS);
		if (cell != null) {
			return GetCellValue(cell);
		}
		return "";
	}

	public static void SetCellBool(XSSFSheet sheet0, String POS, boolean BoolVal) {
		XSSFCell cell = GetCell(sheet0, POS);
		if (cell != null) {
			cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
			cell.setCellValue(BoolVal);
		}
	}

	public static void SetCellValue(XSSFSheet sheet0, String POS, String Value) {
		XSSFCell cell = GetCell(sheet0, POS);
		if (cell != null) {
			WbFillCell(sheet0, cell, Value);
		}
	}

	/**
	 * 得到Excel一个单元格里面的值
	 * 
	 * @param Cell
	 * @return
	 */
	public static String GetCellValue(XSSFCell Cell) {
		try {
			if (Cell.getCellType() == XSSFCell.CELL_TYPE_STRING)// CELL_TYPE_STRING
			{
				return Cell.getStringCellValue().trim();
			} else if (Cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
				String temp = String.valueOf(Cell.getNumericCellValue());
				temp = RemoveZero(temp);
				return temp;
			} else if (Cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA) {
				String temp = Cell.getStringCellValue().trim();
				if (temp.equals("")) {
					temp = String.valueOf(Cell.getNumericCellValue());
				}
				temp = RemoveZero(temp);
				return temp;
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 得到字符串 比如"AA33" 返回AA 注意：只能前面是字母 后面是数字 而且要保证连续
	 * 
	 * @param str
	 * @return
	 */
	public static String GetStr(String str) {
		str = str.toUpperCase();
		int index = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) - 'A' >= 0) {
				index = i;
				// break;
			}
		}
		return str.substring(0, index + 1);
	}

	/**
	 * 得到数字 比如"AA33" 返回33 注意：只能前面是字母 后面是数字 而且要保证连续
	 * 
	 * @param str
	 * @return
	 */
	public static String GetNum(String str) {
		str = str.toUpperCase();
		int index = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) - 'A' >= 0) {
				index = i;
				// break;
			}
		}
		return str.substring(index + 1, str.length());
	}

	/**
	 * 去掉末尾的零
	 * 
	 * @param str
	 * @return
	 */
	public static String RemoveZero(String str) {
		String Temp = str;
		try {
			// 判断是否数字
			for (int i = 0; i < Temp.length(); i++) {
				if ((Temp.charAt(i) > '9') || (Temp.charAt(i) < '0')) {
					if (Temp.charAt(i) != '.') {
						return str;
					}
				}
			}
			// 去掉末尾的零
			if (Temp.indexOf(".") > 0) {
				while (Temp.endsWith("0")) {
					Temp = Temp.substring(0, Temp.length() - 1);
				}
				if (Temp.endsWith(".")) {
					Temp = Temp.substring(0, Temp.length() - 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Temp;
	}

}
