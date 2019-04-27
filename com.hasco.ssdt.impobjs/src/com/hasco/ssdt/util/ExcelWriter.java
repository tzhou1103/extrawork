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

		File tempFile = new File(path); //����path����ļ�

		if(tempFile.exists()) { //����ļ��Ѿ��������ȡ���ļ���׼��д��
			createNew = false;

			Workbook wb = Workbook.getWorkbook(tempFile);
			workbook = Workbook.createWorkbook(tempFile, wb);
			
			currentWorkSheetNo = 0;
			workSheet = workbook.getSheet(0);

		} else { //�����ڸ��ļ����½��ļ���sheet
			createNew = true;

			file = new FileOutputStream(path);
			workbook = Workbook.createWorkbook(file);
			currentWorkSheetNo = 0;
			workSheet = workbook.createSheet("report", currentWorkSheetNo);
		}
	}

	public ExcelWriter(String path, String sheetName) throws BiffException, IOException {
		filePath = path;

		File tempFile = new File(path); //����path����ļ�

		if(tempFile.exists()) { //����ļ��Ѿ��������ȡ���ļ���׼��д��
			createNew = false;

			Workbook wb = Workbook.getWorkbook(tempFile);
			workbook = Workbook.createWorkbook(tempFile, wb);

			boolean createNewSheet = true;
			String[] sheetNames = workbook.getSheetNames(); //�õ����е�sheet��

			for (int i = 0; i < sheetNames.length; i++) {
				if(sheetNames[i].equals(sheetName)) { //�������sheetNameͬ����sheet���ȡ��sheet
					currentWorkSheetNo = i;
					workSheet = workbook.getSheet(i);
					createNewSheet = false;
					break;
				}
			}

			if(createNewSheet) { //���û����sheetNameͬ����sheet���½���ΪsheetName��sheet
				currentWorkSheetNo = 0;
				workSheet = workbook.createSheet(sheetName, currentWorkSheetNo);
			}
		} else { //�����ڸ��ļ����½��ļ���sheet
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
	 * �ڵ�ǰ�Ĺ������ָ����ǰ����һ��
	 * @param row ָ���к�
	 * @return void
	 */
	public void insertRow(int row) {
		workSheet.insertRow(row);
	}
	
	/**
	 * �õ�Excel�ĵ�Ԫ�������
	 * @param column ����
	 * @param row ����
	 * @return ָ����Ԫ�������
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
	 * �õ�EXCEL�ļ�������
	 * @return
	 */
	public int getRownumber() {
		return workSheet.getRows();
	}
	
	/**
	 * �õ�Excel��Ԫ�����ע����
	 * @param column
	 * @param row
	 * @return ָ����Ԫ����ע����
	 */
	public String getComment(int column, int row) {
		WritableCellFeatures writableCellFeatures = workSheet.getWritableCell(column, row).getWritableCellFeatures();
		return writableCellFeatures.getComment();
	}
	
	/**
	 * ɾ��Excel��Ԫ�����ע����
	 * @param column
	 * @param row
	 */
	public void removeComment(int column, int row) {
		WritableCellFeatures writableCellFeatures = workSheet.getWritableCell(column, row).getWritableCellFeatures();
		writableCellFeatures.removeComment();
	}

	/**
	 * ��Excel�ĵ�Ԫ����д���ַ��������ı�ԭ��Ԫ��ĸ�ʽ��
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
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
	 * ��Excel�ĵ�Ԫ����д�����֣����ı�ԭ��Ԫ��ĸ�ʽ��
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
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
	 * ��Excel�ĵ�Ԫ����д�����֣����ı�ԭ��Ԫ��ĸ�ʽ��
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
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
	 * �ϲ�ָ���ķ�Χ�ĵ�Ԫ�񣬲�д������
	 * @param startCell_column �ϲ���Ԫ�����ʼ����
	 * @param startCell_row �ϲ���Ԫ�����ʼ����
	 * @param endCell_column �ϲ���Ԫ��Ľ�������
	 * @param endCell_row �ϲ���Ԫ��Ľ�������
	 * @param bgColor ������ɫ
	 * @param fontColor ������ɫ
	 * @param info д���ֵ
	 * @param fontSize �����С
	 * @param isBold �Ƿ�Ӵ�
	 * @param alignment �����ʽ
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
	 * ����Excel�ĵ�Ԫ��ı���ɫ
	 * @param column ����
	 * @param row ����
	 * @param bgColor ����ɫ
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
	 * ����Excel�ĵ�Ԫ��ı߿�
	 * @param column ����
	 * @param row ����
	 * @param borderType �߿��λ��
	 * @param borderStyle �߿�ķ��
	 * @param lineColor �߿����ɫ
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
	 * ��Excel�ĵ�Ԫ����д������
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
	 * @param alignment �����ʽ
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
	 * ��Excel�ĵ�Ԫ����д������
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
	 * @param alignment �����ʽ
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
	 * ��Excel�ĵ�Ԫ����д���ַ���
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
	 * @param fontSize �����С
	 * @param isBold �Ƿ�Ӵ�
	 * @return void
	 */
	public void addStringCell(int column, int row, String info, int fontSize, boolean isBold) {
		addStringCell(column, row, info, fontSize, isBold, ExcelWriter.LEFT);
	}

	/**
	 * ��Excel�ĵ�Ԫ����д���ַ���
	 * @param column ����
	 * @param row ����
	 * @param info д���ֵ
	 * @param fontSize �����С
	 * @param isBold �Ƿ�Ӵ�
	 * @param alignment �����ʽ
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
	 * ��Excel�ĵ�Ԫ����д���ַ���
	 * @param column ����
	 * @param row ����
	 * @param bgColor ������ɫ
	 * @param info д���ֵ
	 * @param fontSize �����С
	 * @param isBold �Ƿ�Ӵ�
	 * @return void
	 */
	public void addStringCell(int column, int row, int bgColor, String info, int fontSize, boolean isBold) {
		addStringCell(column, row, bgColor, ExcelWriter.BLACK, info, fontSize, isBold, ExcelWriter.LEFT);
	}

	/**
	 * ��Excel�ĵ�Ԫ����д���ַ���
	 * @param column ����
	 * @param row ����
	 * @param bgColor ������ɫ
	 * @param info д���ֵ
	 * @param fontSize �����С
	 * @param isBold �Ƿ�Ӵ�
	 * @param alignment �����ʽ
	 * @return void
	 */
	public void addStringCell(int column, int row, int bgColor, String info, int fontSize, boolean isBold, int alignment) {
		addStringCell(column, row, bgColor, ExcelWriter.BLACK, info, fontSize, isBold, alignment);
	}

	/**
	 * ��Excel�ĵ�Ԫ����д���ַ���
	 * @param column ����
	 * @param row ����
	 * @param bgColor ������ɫ
	 * @param fontColor ������ɫ
	 * @param info д���ֵ
	 * @param fontSize �����С
	 * @param isBold �Ƿ�Ӵ�
	 * @param alignment �����ʽ
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
	 * �����п�
	 * @param column ����
	 * @param width �п��
	 * @return void
	 */
	public void setColumnWidth(int column, int width) {
		CellView cellView = new CellView();
		cellView.setSize(width);
		workSheet.setColumnView(column, cellView);
	}

	/**
	 * ��������
	 * @param font ����
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
	 * ��ȡ����������
	 * @param 
	 * @return ����������
	 */
	public int getSheetCount() {
		return workbook.getNumberOfSheets();
	}
	
	/**
	 * �����µĹ������л��ɵ�ǰ�Ĺ�����
	 * @param sheetName �¹����������
	 * @return void
	 */
	public void createNewSheet(String sheetName) {
		createNewSheet(sheetName, 0);
	}

	/**
	 * �����µĹ������л��ɵ�ǰ�Ĺ�����
	 * @param sheetName �¹����������
	 * @param position �¹������λ��
	 * @return void
	 */
	public void createNewSheet(String sheetName, int position) {
		currentWorkSheetNo = position;
		workSheet = workbook.createSheet(sheetName, currentWorkSheetNo);
	}
	
	/**
	 * ɾ��ָ���Ĺ�����
	 * @param sheetName �����������
	 * @return true: ɾ���ɹ�  false: ɾ��ʧ��
	 */
	public boolean removeSheet(String sheetName) {
		String[] sheetNames = workbook.getSheetNames(); //�õ����е�sheet��

		for (int i = 0; i < sheetNames.length; i++) {
			if(sheetNames[i].equals(sheetName)) { //�������sheetNameͬ����sheet��ɾ����sheet
				workbook.removeSheet(i);
				switchSheet(0);
				return true;
			}
		}

		return false;
	}

	/**
	 * ɾ��ָ���Ĺ�����
	 * @param position �������λ��
	 * @return true: ɾ���ɹ�  false: ɾ��ʧ��
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
	 * �л���ǰ������
	 * @param sheetName �����������
	 * @return true: �л��ɹ�  false: �л�ʧ��
	 */
	public boolean switchSheet(String sheetName) {
		String[] sheetNames = workbook.getSheetNames(); //�õ����е�sheet��

		for (int i = 0; i < sheetNames.length; i++) {
			if(sheetNames[i].equals(sheetName)) { //�������sheetNameͬ����sheet���ȡ��sheet
				currentWorkSheetNo = i;
				workSheet = workbook.getSheet(i);
				return true;
			}
		}

		return false;
	}

	/**
	 * �л���ǰ������
	 * @param position �������λ��
	 * @return true: �л��ɹ�  false: �л�ʧ��
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
	 * �رչ�������ļ�
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
	 * ��ʾExcel����
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
