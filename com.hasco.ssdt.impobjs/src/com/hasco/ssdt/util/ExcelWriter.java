package com.hasco.ssdt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import jxl.CellType;
import jxl.CellView;
import jxl.NumberCell;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.WritableFont.FontName;
import jxl.write.biff.RowsExceededException;

public class ExcelWriter {
	public final static int LEFT = 0;
	public final static int CENTER = 1;
	public final static int RIGHT = 2;

	public final static int RED = 10;
	public final static int YELLOW = 11;
	public final static int BLUE = 12;
	public final static int GREEN = 13;
	public final static int WHITE = 14;
	public final static int GRAY = 15;
	public final static int BLACK = 16;

	public final static int FONT_ARIAL = 20;
	public final static int FONT_COURIER = 21;
	public final static int FONT_TAHOMA = 22;
	public final static int FONT_TIMES = 23;

	public final static int BORDER_ALL = 30;
	public final static int BORDER_TOP = 31;
	public final static int BORDER_BOTTOM = 32;
	public final static int BORDER_LEFT = 33;
	public final static int BORDER_RIGHT = 34;
	public final static int BORDER_NONE = 35;

	public final static int BORDERLINE_DOUBLE = 40;
	public final static int BORDERLINE_THIN = 41;
	public final static int BORDERLINE_THICK = 42;
	public final static int BORDERLINE_NONE = 43;
	
	public final static int DEFAULT_CHAR_SIZE = 10;

	public FontName fontName = WritableFont.TAHOMA;

	public WritableWorkbook workbook;

	public WritableSheet workSheet;

	public OutputStream file;

	public boolean createNew;

	public String filePath;

	public int currentWorkSheetNo = 0;

	public ExcelWriter() throws IOException {
		filePath = System.getProperty("java.io.tmpdir") + "\\report.xls";

		createNew = true;

		file = new FileOutputStream(filePath);
		workbook = Workbook.createWorkbook(file);
		workSheet = workbook.createSheet("report", currentWorkSheetNo);
	}
	
	public ExcelWriter(String path) throws BiffException, IOException {
		filePath = path;

		File tempFile = new File(path); //根据path获得文件

		if(tempFile.exists()) { //如果文件已经存在则读取该文件并准备写入
			createNew = false;

			Workbook wb = Workbook.getWorkbook(tempFile);
			workbook = Workbook.createWorkbook(tempFile, wb);
			
			currentWorkSheetNo = 0;
			workSheet = workbook.getSheet(0);

		} else { //不存在该文件则新建文件和sheet
			createNew = true;

			file = new FileOutputStream(path);
			workbook = Workbook.createWorkbook(file);
			currentWorkSheetNo = 0;
			workSheet = workbook.createSheet("report", currentWorkSheetNo);
		}
	}

	public ExcelWriter(String path, String sheetName) throws BiffException, IOException {
		filePath = path;

		File tempFile = new File(path); //根据path获得文件

		if(tempFile.exists()) { //如果文件已经存在则读取该文件并准备写入
			createNew = false;

			Workbook wb = Workbook.getWorkbook(tempFile);
			workbook = Workbook.createWorkbook(tempFile, wb);

			boolean createNewSheet = true;
			String[] sheetNames = workbook.getSheetNames(); //得到所有的sheet名

			for (int i = 0; i < sheetNames.length; i++) {
				if(sheetNames[i].equals(sheetName)) { //如果有与sheetName同名的sheet则读取该sheet
					currentWorkSheetNo = i;
					workSheet = workbook.getSheet(i);
					createNewSheet = false;
					break;
				}
			}

			if(createNewSheet) { //如果没有与sheetName同名的sheet则新建名为sheetName的sheet
				currentWorkSheetNo = 0;
				workSheet = workbook.createSheet(sheetName, currentWorkSheetNo);
			}
		} else { //不存在该文件则新建文件和sheet
			createNew = true;

			file = new FileOutputStream(path);
			workbook = Workbook.createWorkbook(file);
			currentWorkSheetNo = 0;
			workSheet = workbook.createSheet(sheetName, currentWorkSheetNo);
		}
	}
	
	public ExcelWriter(String path, String sheetName, boolean overwrite) throws BiffException, IOException {
		filePath = path;
		
		createNew = true;

		file = new FileOutputStream(path);
		workbook = Workbook.createWorkbook(file);
		currentWorkSheetNo = 0;
		workSheet = workbook.createSheet(sheetName, currentWorkSheetNo);
	}
	
	/**
	 * 在当前的工作表的指定行前插入一行
	 * @param row 指定行号
	 * @return void
	 */
	public void insertRow(int row) {
		workSheet.insertRow(row);
	}
	
	/**
	 * 得到Excel的单元格的内容
	 * @param column 列数
	 * @param row 行数
	 * @return 指定单元格的内容
	 */
	public String getCellContent2(int column, int row) {
		return workSheet.getCell(column, row).getContents();
	}
	
	public String getCellContent(int column, int row) {
		
		if(workSheet.getCell(column, row).getType() == CellType.NUMBER)
		{
			NumberCell number00 = (NumberCell) workSheet.getCell(column, row);
            double strc00 = number00.getValue();
            return String.valueOf(strc00);
		}else{
			return workSheet.getCell(column, row).getContents();
		}
		
	}
	/**
	 * 得到EXCEL文件的行数
	 * @return
	 */
	public int getRownumber() {
		return workSheet.getRows();
	}
	
	/**
	 * 得到Excel单元格的批注内容
	 * @param column
	 * @param row
	 * @return 指定单元格批注内容
	 */
	public String getComment(int column, int row) {
		WritableCellFeatures writableCellFeatures = workSheet.getWritableCell(column, row).getWritableCellFeatures();
		return writableCellFeatures.getComment();
	}
	
	/**
	 * 删除Excel单元格的批注内容
	 * @param column
	 * @param row
	 */
	public void removeComment(int column, int row) {
		WritableCellFeatures writableCellFeatures = workSheet.getWritableCell(column, row).getWritableCellFeatures();
		writableCellFeatures.removeComment();
	}

	/**
	 * 向Excel的单元格里写入字符串（不改变原单元格的格式）
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @return void
	 */
	public void writeStringCell(int column, int row, String info) {
		WritableCell cell = workSheet.getWritableCell(column, row);
		if(cell.getCellFormat() != null) {
			WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());
			
			Label infoLabel = new Label(column, row, info, cellFormat);

			try {
				workSheet.addCell(infoLabel);

			} catch (RowsExceededException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				e.printStackTrace();
			}
		} else {
			addStringCell(column, row, info, DEFAULT_CHAR_SIZE, false);
		}
		

	}

	/**
	 * 向Excel的单元格里写入数字（不改变原单元格的格式）
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @return void
	 */
	public void writeNumberCell(int column, int row, double info) {
		WritableCell cell = workSheet.getWritableCell(column, row);
		WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());

		jxl.write.Number nLabel = new jxl.write.Number(column, row, info, cellFormat);

		try {
			workSheet.addCell(nLabel);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 向Excel的单元格里写入数字（不改变原单元格的格式）
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @return void
	 */
	public void writeNumberCell(int column, int row, int info) {
		WritableCell cell = workSheet.getWritableCell(column, row);
		WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());

		jxl.write.Number nLabel = new jxl.write.Number(column, row, info, cellFormat);

		try {
			workSheet.addCell(nLabel);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 合并指定的范围的单元格，并写入内容
	 * @param startCell_column 合并单元格的起始列数
	 * @param startCell_row 合并单元格的起始行数
	 * @param endCell_column 合并单元格的结束列数
	 * @param endCell_row 合并单元格的结束行数
	 * @param bgColor 背景颜色
	 * @param fontColor 字体颜色
	 * @param info 写入的值
	 * @param fontSize 字体大小
	 * @param isBold 是否加粗
	 * @param alignment 对齐格式
	 * @return void
	 */	
	public void addMergeCell(int startCell_column, int startCell_row, int endCell_column, int endCell_row, int bgColor, int fontColor, String info, int fontSize, boolean isBold, int alignment) {
		try {
			workSheet.mergeCells(startCell_column, startCell_row, endCell_column, endCell_row);
			addStringCell(startCell_column, startCell_row, bgColor, fontColor, info, fontSize, isBold, alignment);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置Excel的单元格的背景色
	 * @param column 列数
	 * @param row 行数
	 * @param bgColor 背景色
	 * @return void
	 */	
	public void setCellBackgroundColor(int column, int row, int bgColor) {
		WritableCell cell = workSheet.getWritableCell(column, row);
		WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());

		try {
			switch (bgColor) {
			case ExcelWriter.RED:
				cellFormat.setBackground(Colour.RED);
				break;
			case ExcelWriter.YELLOW:
				cellFormat.setBackground(Colour.YELLOW);
				break;
			case ExcelWriter.BLUE:
				cellFormat.setBackground(Colour.BLUE);
				break;
			case ExcelWriter.GREEN:
				cellFormat.setBackground(Colour.GREEN);
				break;
			case ExcelWriter.WHITE:
				cellFormat.setBackground(Colour.WHITE);
				break;
			case ExcelWriter.GRAY:
				cellFormat.setBackground(Colour.GREY_40_PERCENT);
				break;
			case ExcelWriter.BLACK:
				cellFormat.setBackground(Colour.BLACK);
				break;
			default:
				break;
			}
		} catch (WriteException e) {
			e.printStackTrace();
		}

		cell.setCellFormat(cellFormat);
	}

	/**
	 * 设置Excel的单元格的边框
	 * @param column 列数
	 * @param row 行数
	 * @param borderType 边框的位置
	 * @param borderStyle 边框的风格
	 * @param lineColor 边框的颜色
	 * @return void
	 */
	public void setCellBorder(int column, int row, int borderType, int borderStyle, int lineColor) {
		WritableCell cell = workSheet.getWritableCell(column, row);
		WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());

		try {
			Border border;
			BorderLineStyle borderLineStyle;
			Colour borderLineColor;

			switch (borderType) {
			case ExcelWriter.BORDER_ALL:
				border = Border.ALL;
				break;
			case ExcelWriter.BORDER_TOP:
				border = Border.TOP;
				break;
			case ExcelWriter.BORDER_BOTTOM:
				border = Border.BOTTOM;
				break;
			case ExcelWriter.BORDER_LEFT:
				border = Border.LEFT;
				break;
			case ExcelWriter.BORDER_RIGHT:
				border = Border.RIGHT;
				break;
			default:
				border = Border.NONE;
			break;
			}

			switch (borderStyle) {
			case ExcelWriter.BORDERLINE_THIN:
				borderLineStyle = BorderLineStyle.THIN;
				break;
			case ExcelWriter.BORDERLINE_THICK:
				borderLineStyle = BorderLineStyle.THICK;
				break;
			case ExcelWriter.BORDERLINE_DOUBLE:
				borderLineStyle = BorderLineStyle.DOUBLE;
				break;
			default:
				borderLineStyle = BorderLineStyle.THIN;
			break;
			}

			switch (lineColor) {
			case ExcelWriter.RED:
				borderLineColor = Colour.RED;
				break;
			case ExcelWriter.YELLOW:
				borderLineColor = Colour.YELLOW;
				break;
			case ExcelWriter.BLUE:
				borderLineColor = Colour.BLUE;
				break;
			case ExcelWriter.GREEN:
				borderLineColor = Colour.GREEN;
				break;
			case ExcelWriter.WHITE:
				borderLineColor = Colour.WHITE;
				break;
			case ExcelWriter.GRAY:
				borderLineColor = Colour.GREY_40_PERCENT;
				break;
			case ExcelWriter.BLACK:
				borderLineColor = Colour.BLACK;
				break;
			default:
				borderLineColor = Colour.WHITE;
			break;
			}

			cellFormat.setBorder(border, borderLineStyle, borderLineColor);
		} catch (WriteException e) {
			e.printStackTrace();
		}

		cell.setCellFormat(cellFormat);
	}

	/**
	 * 向Excel的单元格里写入数字
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @param alignment 对齐格式
	 * @return void
	 */
	public void addNumberCell(int column, int row, int info, int alignment) {
		NumberFormat nfont = new NumberFormat("#");
		WritableCellFormat cellFormat = new WritableCellFormat(nfont);

		try {
			cellFormat.setWrap(true);
			switch (alignment) {
			case ExcelWriter.LEFT:
				cellFormat.setAlignment(Alignment.LEFT);
				break;
			case ExcelWriter.CENTER:
				cellFormat.setAlignment(Alignment.CENTRE);
				break;
			case ExcelWriter.RIGHT:
				cellFormat.setAlignment(Alignment.RIGHT);
				break;
			default:
				break;
			}
		} catch (WriteException e) {
			e.printStackTrace();
		}

		jxl.write.Number nLabel = new jxl.write.Number(column, row, info, cellFormat);

		try {
			workSheet.addCell(nLabel);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 向Excel的单元格里写入数字
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @param alignment 对齐格式
	 * @return void
	 */
	public void addNumberCell(int column, int row, double info, int alignment) {
		NumberFormat nfont = new NumberFormat("#");
		WritableCellFormat cellFormat = new WritableCellFormat(nfont);

		try {
			cellFormat.setWrap(true);
			switch (alignment) {
			case ExcelWriter.LEFT:
				cellFormat.setAlignment(Alignment.LEFT);
				break;
			case ExcelWriter.CENTER:
				cellFormat.setAlignment(Alignment.CENTRE);
				break;
			case ExcelWriter.RIGHT:
				cellFormat.setAlignment(Alignment.RIGHT);
				break;
			default:
				break;
			}
		} catch (WriteException e) {
			e.printStackTrace();
		}

		jxl.write.Number nLabel = new jxl.write.Number(column, row, info, cellFormat);

		try {
			workSheet.addCell(nLabel);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向Excel的单元格里写入字符串
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @param fontSize 字体大小
	 * @param isBold 是否加粗
	 * @return void
	 */
	public void addStringCell(int column, int row, String info, int fontSize, boolean isBold) {
		addStringCell(column, row, info, fontSize, isBold, ExcelWriter.LEFT);
	}

	/**
	 * 向Excel的单元格里写入字符串
	 * @param column 列数
	 * @param row 行数
	 * @param info 写入的值
	 * @param fontSize 字体大小
	 * @param isBold 是否加粗
	 * @param alignment 对齐格式
	 * @return void
	 */
	public void addStringCell(int column, int row, String info, int fontSize, boolean isBold, int alignment) {
		WritableFont font;
		if(isBold == true) {
			font = new WritableFont(fontName, fontSize, WritableFont.BOLD, false);
		} else {
			font = new WritableFont(fontName, fontSize, WritableFont.NO_BOLD, false);
		}
		WritableCellFormat cellFormat = new WritableCellFormat(font, NumberFormats.TEXT);
		try {
			cellFormat.setWrap(true);
			switch (alignment) {
			case ExcelWriter.LEFT:
				cellFormat.setAlignment(Alignment.LEFT);
				break;
			case ExcelWriter.CENTER:
				cellFormat.setAlignment(Alignment.CENTRE);
				break;
			case ExcelWriter.RIGHT:
				cellFormat.setAlignment(Alignment.RIGHT);
				break;
			default:
				break;
			}
		} catch (WriteException e) {
			e.printStackTrace();
		}

		Label infoLabel = new Label(column, row, info, cellFormat);

		try {
			workSheet.addCell(infoLabel);

		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向Excel的单元格里写入字符串
	 * @param column 列数
	 * @param row 行数
	 * @param bgColor 背景颜色
	 * @param info 写入的值
	 * @param fontSize 字体大小
	 * @param isBold 是否加粗
	 * @return void
	 */
	public void addStringCell(int column, int row, int bgColor, String info, int fontSize, boolean isBold) {
		addStringCell(column, row, bgColor, ExcelWriter.BLACK, info, fontSize, isBold, ExcelWriter.LEFT);
	}

	/**
	 * 向Excel的单元格里写入字符串
	 * @param column 列数
	 * @param row 行数
	 * @param bgColor 背景颜色
	 * @param info 写入的值
	 * @param fontSize 字体大小
	 * @param isBold 是否加粗
	 * @param alignment 对齐格式
	 * @return void
	 */
	public void addStringCell(int column, int row, int bgColor, String info, int fontSize, boolean isBold, int alignment) {
		addStringCell(column, row, bgColor, ExcelWriter.BLACK, info, fontSize, isBold, alignment);
	}

	/**
	 * 向Excel的单元格里写入字符串
	 * @param column 列数
	 * @param row 行数
	 * @param bgColor 背景颜色
	 * @param fontColor 字体颜色
	 * @param info 写入的值
	 * @param fontSize 字体大小
	 * @param isBold 是否加粗
	 * @param alignment 对齐格式
	 * @return void
	 */
	public void addStringCell(int column, int row, int bgColor, int fontColor, String info, int fontSize, boolean isBold, int alignment) {
		WritableFont font;
		Colour fColor;

		switch (fontColor) {
		case ExcelWriter.RED:
			fColor = Colour.RED;
			break;
		case ExcelWriter.YELLOW:
			fColor = Colour.YELLOW;
			break;
		case ExcelWriter.BLUE:
			fColor = Colour.BLUE;
			break;
		case ExcelWriter.GREEN:
			fColor = Colour.GREEN;
			break;
		case ExcelWriter.WHITE:
			fColor = Colour.WHITE;
			break;
		case ExcelWriter.GRAY:
			fColor = Colour.GREY_40_PERCENT;
			break;
		case ExcelWriter.BLACK:
			fColor = Colour.BLACK;
			break;
		default:
			fColor = Colour.WHITE;
		break;
		}

		if(isBold == true) {
			font = new WritableFont(fontName, fontSize, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, fColor);
		} else {
			font = new WritableFont(fontName, fontSize, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, fColor);
		}

		WritableCellFormat cellFormat = new WritableCellFormat(font, NumberFormats.TEXT);
		try {
			cellFormat.setWrap(true);
			switch (bgColor) {
			case ExcelWriter.RED:
				cellFormat.setBackground(Colour.RED);
				break;
			case ExcelWriter.YELLOW:
				cellFormat.setBackground(Colour.YELLOW);
				break;
			case ExcelWriter.BLUE:
				cellFormat.setBackground(Colour.BLUE);
				break;
			case ExcelWriter.GREEN:
				cellFormat.setBackground(Colour.GREEN);
				break;
			case ExcelWriter.WHITE:
				cellFormat.setBackground(Colour.WHITE);
				break;
			case ExcelWriter.GRAY:
				cellFormat.setBackground(Colour.GREY_40_PERCENT);
				break;
			case ExcelWriter.BLACK:
				cellFormat.setBackground(Colour.BLACK);
				break;
			default:
				break;
			}
			switch (alignment) {
			case ExcelWriter.LEFT:
				cellFormat.setAlignment(Alignment.LEFT);
				break;
			case ExcelWriter.CENTER:
				cellFormat.setAlignment(Alignment.CENTRE);
				break;
			case ExcelWriter.RIGHT:
				cellFormat.setAlignment(Alignment.RIGHT);
				break;
			default:
				break;
			}
		} catch (WriteException e) {
			e.printStackTrace();
		}

		Label infoLabel = new Label(column, row, info, cellFormat);

		try {
			workSheet.addCell(infoLabel);

		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置列宽
	 * @param column 列数
	 * @param width 列宽度
	 * @return void
	 */
	public void setColumnWidth(int column, int width) {
		CellView cellView = new CellView();
		cellView.setSize(width);
		workSheet.setColumnView(column, cellView);
	}

	/**
	 * 设置字体
	 * @param font 字体
	 * @return void
	 */
	public void setFont(int font) {
		switch (font) {
		case ExcelWriter.FONT_ARIAL:
			fontName = WritableFont.ARIAL;
			break;
		case ExcelWriter.FONT_COURIER:
			fontName = WritableFont.COURIER;
			break;
		case ExcelWriter.FONT_TAHOMA:
			fontName = WritableFont.TAHOMA;
			break;
		case ExcelWriter.FONT_TIMES:
			fontName = WritableFont.TIMES;
			break;
		default:
			break;
		}
	}
	
	public String getDataValidationList(int column, int row) {
		//ArrayList<String> validationList = new ArrayList<String>();
		WritableCellFeatures writableCellFeatures = workSheet.getWritableCell(column, row).getWritableCellFeatures();
		String list = writableCellFeatures.getDataValidationList();
		return list;
	}

	/**
	 * 获取工作表数量
	 * @param 
	 * @return 工作表数量
	 */
	public int getSheetCount() {
		return workbook.getNumberOfSheets();
	}
	
	/**
	 * 创建新的工作表并切换成当前的工作表
	 * @param sheetName 新工作表的名称
	 * @return void
	 */
	public void createNewSheet(String sheetName) {
		createNewSheet(sheetName, 0);
	}

	/**
	 * 创建新的工作表并切换成当前的工作表
	 * @param sheetName 新工作表的名称
	 * @param position 新工作表的位置
	 * @return void
	 */
	public void createNewSheet(String sheetName, int position) {
		currentWorkSheetNo = position;
		workSheet = workbook.createSheet(sheetName, currentWorkSheetNo);
	}
	
	/**
	 * 删除指定的工作表
	 * @param sheetName 工作表的名称
	 * @return true: 删除成功  false: 删除失败
	 */
	public boolean removeSheet(String sheetName) {
		String[] sheetNames = workbook.getSheetNames(); //得到所有的sheet名

		for (int i = 0; i < sheetNames.length; i++) {
			if(sheetNames[i].equals(sheetName)) { //如果有与sheetName同名的sheet则删除该sheet
				workbook.removeSheet(i);
				switchSheet(0);
				return true;
			}
		}

		return false;
	}

	/**
	 * 删除指定的工作表
	 * @param position 工作表的位置
	 * @return true: 删除成功  false: 删除失败
	 */
	public boolean removeSheet(int position) {
		if(workbook.getNumberOfSheets() > position) {
			workbook.removeSheet(position);
			switchSheet(0);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 切换当前工作表
	 * @param sheetName 工作表的名称
	 * @return true: 切换成功  false: 切换失败
	 */
	public boolean switchSheet(String sheetName) {
		String[] sheetNames = workbook.getSheetNames(); //得到所有的sheet名

		for (int i = 0; i < sheetNames.length; i++) {
			if(sheetNames[i].equals(sheetName)) { //如果有与sheetName同名的sheet则读取该sheet
				currentWorkSheetNo = i;
				workSheet = workbook.getSheet(i);
				return true;
			}
		}

		return false;
	}

	/**
	 * 切换当前工作表
	 * @param position 工作表的位置
	 * @return true: 切换成功  false: 切换失败
	 */
	public boolean switchSheet(int position) {
		if(workbook.getNumberOfSheets() > position) {
			currentWorkSheetNo = position;
			workSheet = workbook.getSheet(position);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 关闭工作表和文件
	 * @return void
	 */
	public void closeExcel() {
		try {
			workbook.write();
			workbook.close();
			if(createNew) {
				file.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 显示Excel报表
	 * @return void
	 */
	public void displayReport() {
		try {
			String[] cmd = { "cmd.exe", "/c", "start", "excel", "\"" + filePath + "\"" };

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
