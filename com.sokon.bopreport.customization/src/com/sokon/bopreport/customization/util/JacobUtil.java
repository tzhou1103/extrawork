package com.sokon.bopreport.customization.util;

import java.io.File;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * Jacob常用方法工具类
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
	 * 获取Excel中所有sheet对象
	 * 
	 * @param workBook Excel工作簿
	 * @return sheets集合
	 */
	public static Dispatch getSheets(Dispatch workBook)
	{
//		Dispatch sheets = Dispatch.get(workBook, "sheets").toDispatch();
		Dispatch sheets = Dispatch.get(workBook, "Worksheets").toDispatch();
		return sheets;
	}
	/**
	 * 获取Sheet页总数
	 * @return
	 */
	/* private static int getSheetCount(Dispatch workBook) {
        int count = Dispatch.get(getSheets(workBook), "count").toInt();  
        return count;  
	 }*/  
	
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
	 * 设置单元格颜色
	 * 
	 * @param sheet 需要操作的Sheet
	 * @param cellName 单元格名称
	 * @param cellValue 需要设置的颜色
	 * @throws Exception
	 */
	public static void setCellColor(Dispatch sheet, String cellName, Object cellValue) throws Exception
	{
		Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null) {
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		}
		Dispatch Interior = Dispatch.get(cell, "Interior").toDispatch();
		Dispatch.put(Interior, "Color", cellValue);
	}
	
	/**
	 * 根据单元格名称插入图片
	 * 
	 * @param sheet 需要操作的Sheet
	 * @param cellName 单元格名称
	 * @param imageFile 图片文件
	 * @throws Exception 若根据名称未找到单元格，则抛出异常
	 */
	public static void insertPicture(Dispatch sheet, String cellName, File imageFile) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		
		Dispatch.call(cell, "Select"); //在工作表中，定位需要插入图片的具体位置
		Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
		Dispatch.call(select, "Insert", imageFile.getAbsolutePath()).toDispatch();
	}
	
	/**
	 * 根据指定范围合并单元格
	 * 
	 * @param sheet 需要操作的Sheet
	 * @param rangeArea 合并区域，如A1:C2
	 * @return 返回合并后的单元格
	 * @throws Exception 若根据名称未找到单元格区域，则抛出异常
	 */
	public static Dispatch mergeCell(Dispatch sheet, String rangeArea) throws Exception
	{
		Dispatch rangeCell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { rangeArea }, new int[1]).toDispatch();
		if (rangeCell == null) {
			throw new Exception("模板中未找到名称为[" + rangeArea + "]的区域，请联系管理员！");
		}
		Dispatch.call(rangeCell, "Merge");
		
		return rangeCell;
	}
	
	/**
	 * 在同一个Excel中复制sheet页
	 * 
	 * @param sheets Excel中所有Sheet
	 * @param pageNum 需要复制的页数
	 */
	public static void copySheet(Dispatch sheets, int pageNum)
	{
		Dispatch firstSheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[] { 1 }, new int[1]).toDispatch();
		for (int i = 1; i < pageNum; i++)
		{
			Dispatch sheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[] { i }, new int[1]).toDispatch();
			Dispatch.call(firstSheet, "Copy", sheet);
		}
	}
	
	/**
	 * 在sheet页中将指定行复制出多行
	 * 
	 * @param sheet
	 * @param rowNo 指定行
	 * @param insertRows 需要复制的行数
	 */
	public static void copyRow(Dispatch sheet, int rowNo, int insertRows)
	{
		String rowArea = rowNo + ":" + rowNo;
		Dispatch row = Dispatch.invoke(sheet, "Rows", Dispatch.Get, new Object[] { rowArea }, new int[1]).toDispatch();
		for (int i = 0; i < insertRows; i++)
		{
			Dispatch.call(row, "Copy");
			Dispatch.call(row, "Insert");
		}
	}
	
	/**
	 * 给指定区域设置所有框线
	 * 
	 * @param sheet
	 * @param rangeName
	 */
	public static void setAllBorders(Dispatch sheet, String rangeName)
	{
		Dispatch range = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{rangeName}, new int[1]).toDispatch();
		
		Dispatch borders = Dispatch.get(range, "Borders").toDispatch();
		
		// 1、2、3、4 依次对应 左、右、上、下
		Dispatch edgeLeft = Dispatch.call(borders, "Item", new Variant(1)).toDispatch();
		Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
		
		edgeLeft = Dispatch.call(borders, "Item", new Variant(2)).toDispatch();
		Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
		
		edgeLeft = Dispatch.call(borders, "Item", new Variant(3)).toDispatch();
		Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
		
		edgeLeft = Dispatch.call(borders, "Item", new Variant(4)).toDispatch();
		Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
	}
	
	/**
	 * 给指定区域设置外侧框线
	 * 
	 * @param sheet
	 * @param rangeName
	 */
	public static void setOutBorders(Dispatch sheet, String rangeName)
	{
		Dispatch range = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{rangeName}, new int[1]).toDispatch();
		
		Dispatch borders = Dispatch.get(range, "Borders").toDispatch();
		
		// 7、8、9、10 依次对应 左、上、下、右
		Dispatch edge = Dispatch.call(borders, "Item", new Variant(7)).toDispatch();
		Dispatch.put(edge, "LineStyle", new Variant(1));
		
		edge = Dispatch.call(borders, "Item", new Variant(8)).toDispatch();
		Dispatch.put(edge, "LineStyle", new Variant(1));
		
		edge = Dispatch.call(borders, "Item", new Variant(9)).toDispatch();
		Dispatch.put(edge, "LineStyle", new Variant(1));
		
		edge = Dispatch.call(borders, "Item", new Variant(10)).toDispatch();
		Dispatch.put(edge, "LineStyle", new Variant(1));
	}
	
	/**
	 * 修改所有Sheet页名称，以前缀加_页码序号
	 * 
	 * @param sheets Excel中所有的Sheet
	 * @param prefix sheet页名称前缀
	 */
	public static void reNameAllSheet(Dispatch sheets, String prefix)
	{
		int sheetCount = Dispatch.call(sheets, "Count").getInt();
		if (sheetCount < 2) {
			return;
		}
		
		for (int i = 1; i <= sheetCount; i++) 
		{
			Dispatch sheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[] { i }, new int[1]).toDispatch();
			String newName = prefix + "_" + String.valueOf(i);
			Dispatch.put(sheet, "Name", newName);
		}
	}
	
	/**
	 * 保存并关闭Excel文档，结束Excel进程
	 * 
	 * @param excelApp Excel进程
	 * @param workBooks Excel工作簿
	 */
	public static void closeExcelApp(ActiveXComponent excelApp, Dispatch workBook)
	{
		Dispatch.call(workBook, "Save");
		Dispatch.call(workBook, "Close", new Variant(false));
		if (excelApp != null)
			excelApp.invoke("Quit", new Variant[]{});
		ComThread.Release();
	}
	
}
