package com.zht.customization.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class POIUtil {

	private String excelPath;
	private Sheet sheet = null;
	private Workbook wb = null;

	private POIUtil(String excelPath) {
		this.excelPath = excelPath;
		getWorkBook(this.excelPath);
	}

	/**
	 * ���POI session֮ǰ�ж�·���Ƿ����
	 * 
	 * @param excelPath
	 * @return POIUtil
	 */
	public static POIUtil GetUtil(String excelPath) {
		File file = new File(excelPath);
		POIUtil util = null;
		if (file.exists()) {
			util = new POIUtil(excelPath);
		}
		return util;
	}

	/**
	 * ���湤����
	 * 
	 * @param wb
	 */
	public void saveExcel() {
		FileOutputStream fileOut;
		try {
			System.out.println("saving...");
			if (wb != null) {
				fileOut = new FileOutputStream(excelPath);
				wb.write(fileOut);
				fileOut.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �õ�һ�����еĹ�������POI����
	 * 
	 * @return
	 */
	private Workbook getWorkBook(String excelPath) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(excelPath);
			if (fis != null)
				wb = new XSSFWorkbook(fis);
		} catch (Exception e) {
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return wb;
	}

	public void getSheet(String sheetName) {
		if (wb == null) {
			System.out.println("wb is null");
			return;
		}
		sheet = wb.getSheet(sheetName);
	}

	public Cell getCell(int row, int col) {
		if (sheet == null) {
			System.out.println("sheet is null");
			return null;
		}
		Row row2 = sheet.getRow(row);
		if (row2 == null)
			return null;
		return row2.getCell(col);
	}

	public void setCellValue(Cell cell, String value) {
		if (cell != null) {
			cell.setCellValue(value == null ? "" : value);
		}
	}

	public static void main(String[] args) {
		POIUtil poi = POIUtil.GetUtil("E:\\1.xlsx");
		poi.getSheet("Sheet1");
		Cell cell = poi.getCell(0, 0);
		poi.setCellValue(cell, null);
		poi.saveExcel();
	}
}
