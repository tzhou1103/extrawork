package com.hasco.ssdt.pdm.nxexport;

import java.io.File;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * Jacob操作Excel常用方法工具类
 * 
 * @author zhoutong
 */
public class JacobUtil 
{	
	/**
	 * 打开Excel应用程序
	 * 
	 * @return Excel应用
	 */
	public static ActiveXComponent openExcelApp()
	{
		ComThread.InitSTA();
		ActiveXComponent excelApp = new ActiveXComponent("Excel.Application");
		excelApp.setProperty("Visible", new Variant(false));
		
		return excelApp;
	}
	
	/**
	 * 打开Excel文件，获取Excel工作簿
	 * 
	 * @param paramFile Excel文件
	 * @return Excel工作簿
	 * @throws Exception 不是Excel文件，抛出异常
	 */
	public static Dispatch getWorkBook(ActiveXComponent excelApp, File paramFile) throws Exception
	{
		String filePath = paramFile.getAbsolutePath();
		if (!filePath.endsWith(".xls") && !filePath.endsWith(".xlsx")) {
			throw new Exception("[ " + paramFile + " ]不是Excel文件，请确认！");
		}
		
		Dispatch workbooks = excelApp.getProperty("Workbooks").toDispatch();
		Dispatch workBook = Dispatch.invoke(workbooks, "Open", Dispatch.Method, 
				new Object[] { paramFile.getAbsolutePath(), new Variant(false), new Variant(false) }, new int[1]).toDispatch();
		
		return workBook;
	}
	
	/**
	 * 打开Excel文件，获取Excel工作簿
	 * 
	 * @param paramFile Excel文件
	 * @return Excel工作簿
	 * @throws Exception 不是Excel文件，抛出异常
	 */
	public static Dispatch getWorkBook(ActiveXComponent excelApp, String paramFilePath) throws Exception
	{
		if (!paramFilePath.endsWith(".xls") && !paramFilePath.endsWith(".xlsx")) {
			throw new Exception("[ " + paramFilePath + " ]不是Excel文件，请确认！");
		}
		
		Dispatch workbooks = excelApp.getProperty("Workbooks").toDispatch();
		Dispatch workBook = Dispatch.invoke(workbooks, "Open", Dispatch.Method, 
				new Object[] { paramFilePath, new Variant(false), new Variant(false) }, new int[1]).toDispatch();
		
		return workBook;
	}
	
	/**
	 * 获取Excel中所有sheet对象
	 * 
	 * @param workBook Excel工作簿
	 * @return sheets集合
	 */
	public static Dispatch getSheets(Dispatch workBook)
	{
		Dispatch sheets = Dispatch.get(workBook, "sheets").toDispatch();
		return sheets;
	}
	
	/**
	 * 获取Excel中指定位置或指定名称的sheet页
	 * 
	 * @param sheets sheets集合
	 * @param paramObject 指定位置或名称
	 * @return 指定的Sheet
	 */
	public static Dispatch getSheet(Dispatch sheets, Object paramObject)
	{
		Dispatch sheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[] { paramObject }, new int[1]).toDispatch();
		return sheet;
	}
	
	/**
	 * 根据单元格名称写入数据
	 * 
	 * @param sheet 需要操作的Sheet
	 * @param cellName 单元格名称
	 * @param cellValue 需要设置的值
	 * @throws Exception 若根据名称未找到单元格，则抛出异常
	 */
	public static void writeCellData(Dispatch sheet, String cellName, Object cellValue) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		Dispatch.put(cell, "Value", cellValue);
	}
	
	/**
	 * 根据单元格名称获取数据
	 * <p> 纯数字，截掉末尾的.0
	 * 
	 * @param sheet 需要操作的Sheet
	 * @param cellName 单元格名称
	 * @throws Exception 若根据名称未找到单元格，则抛出异常
	 */
	public static String getCellData(Dispatch sheet, String cellName) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		String value = Dispatch.get(cell, "Value").toString();
		if (value != null && !value.equals("null")) 
		{
			if (value.endsWith(".0")) {
				return value.substring(0, value.lastIndexOf("."));
			}
			return value;
		}
		return "";
	}
	
	/**
	 * 关闭Excel文档，结束Excel进程
	 * 
	 * @param excelApp Excel进程
	 * @param workBook Excel工作簿
	 * @param needSave 是否需要保存
	 */
	public static void closeExcelApp(ActiveXComponent excelApp, Dispatch workBook, boolean needSave)
	{
		if (needSave) {
			Dispatch.call(workBook, "Save");
		}
		
		Dispatch.call(workBook, "Close", new Variant(false));
		if (excelApp != null)
			excelApp.invoke("Quit", new Variant[]{});
		ComThread.Release();
	}
	
}
