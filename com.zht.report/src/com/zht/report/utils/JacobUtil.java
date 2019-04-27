package com.zht.report.utils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import java.io.File;

public class JacobUtil
{
	public static ActiveXComponent getExcelApp() 
	{
		ComThread.InitSTA();
		ActiveXComponent excelApp = new ActiveXComponent("Excel.Application");
		excelApp.setProperty("Visible", new Variant(false));

		return excelApp;
	}

	public static Dispatch getWorkBook(ActiveXComponent excelApp, File paramFile) throws Exception
	{
		String filePath = paramFile.getAbsolutePath();
		if ((!filePath.endsWith(".xls")) && (!filePath.endsWith(".xlsx"))) {
			throw new Exception("[ " + paramFile + " ]不是Excel文件，请确认！");
		}
		Dispatch workbooks = excelApp.getProperty("Workbooks").toDispatch();
		Dispatch workBook = Dispatch.invoke(workbooks, "Open", 1,
				new Object[] { paramFile.getAbsolutePath(), new Variant(false), new Variant(false) }, new int[1]).toDispatch();

		return workBook;
	}

	public static Dispatch getSheets(Dispatch workBook)
	{
		Dispatch sheets = Dispatch.get(workBook, "sheets").toDispatch();
		return sheets;
	}

	public static Dispatch getSheet(Dispatch sheets, Object paramObject) 
	{
		Dispatch sheet = Dispatch.invoke(sheets, "Item", 2, new Object[] { paramObject }, new int[1]).toDispatch();
		return sheet;
	}

	public static void writeCellData(Dispatch sheet, String cellName, Object cellValue) throws Exception 
	{
		Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null) {
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		}
		Dispatch.put(cell, "Value", cellValue);
	}
	
	
	public static void setCellColor(Dispatch sheet, String cellName, Object cellValue) throws Exception
	{
		Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null) {
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		}
		Dispatch Interior = Dispatch.get(cell, "Interior").toDispatch();
		Dispatch.put(Interior, "Color", cellValue);
	}


	public static void closeExcelApp(ActiveXComponent excelApp, Dispatch workBook) 
	{
		if (workBook != null) {
			Dispatch.call(workBook, "Save");
			Dispatch.call(workBook, "Close", new Object[] { new Variant(false) });
		}
		if (excelApp != null) {
			excelApp.invoke("Quit", new Variant[0]);
		}
		ComThread.Release();
	}

}
