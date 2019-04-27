package com.sokon.bopreport.customization.util;

import java.io.File;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * Jacob���÷���������
 * 
 * @author zhoutong
 */
public class JacobUtil 
{	
	/**
	 * ��ExcelӦ�ó���
	 * 
	 * @return ExcelӦ��
	 */
	public static ActiveXComponent openExcelApp()
	{
		ComThread.InitSTA();
		ActiveXComponent excelApp = new ActiveXComponent("Excel.Application");
		excelApp.setProperty("Visible", new Variant(false));
		
		return excelApp;
	}
	
	/**
	 * ��Excel�ļ�����ȡExcel������
	 * 
	 * @param paramFile Excel�ļ�
	 * @return Excel������
	 * @throws Exception ����Excel�ļ����׳��쳣
	 */
	public static Dispatch getWorkBook(ActiveXComponent excelApp, File paramFile) throws Exception
	{
		String filePath = paramFile.getAbsolutePath();
		if (!filePath.endsWith(".xls") && !filePath.endsWith(".xlsx")) {
			throw new Exception("[ " + paramFile + " ]����Excel�ļ�����ȷ�ϣ�");
		}
		
		Dispatch workbooks = excelApp.getProperty("Workbooks").toDispatch();
		Dispatch workBook = Dispatch.invoke(workbooks, "Open", Dispatch.Method, 
				new Object[] { paramFile.getAbsolutePath(), new Variant(false), new Variant(false) }, new int[1]).toDispatch();
		
		return workBook;
	}
	
	/**
	 * ��ȡExcel������sheet����
	 * 
	 * @param workBook Excel������
	 * @return sheets����
	 */
	public static Dispatch getSheets(Dispatch workBook)
	{
//		Dispatch sheets = Dispatch.get(workBook, "sheets").toDispatch();
		Dispatch sheets = Dispatch.get(workBook, "Worksheets").toDispatch();
		return sheets;
	}
	/**
	 * ��ȡSheetҳ����
	 * @return
	 */
	/* private static int getSheetCount(Dispatch workBook) {
        int count = Dispatch.get(getSheets(workBook), "count").toInt();  
        return count;  
	 }*/  
	
	/**
	 * ��ȡExcel��ָ��λ�û�ָ�����Ƶ�sheetҳ
	 * 
	 * @param sheets sheets����
	 * @param paramObject ָ��λ�û�����
	 * @return ָ����Sheet
	 */
	public static Dispatch getSheet(Dispatch sheets, Object paramObject)
	{
		Dispatch sheet = Dispatch.invoke(sheets, "Item", Dispatch.Get, new Object[] { paramObject }, new int[1]).toDispatch();
		return sheet;
	}
	
	/**
	 * ���ݵ�Ԫ������д������
	 * 
	 * @param sheet ��Ҫ������Sheet
	 * @param cellName ��Ԫ������
	 * @param cellValue ��Ҫ���õ�ֵ
	 * @throws Exception ����������δ�ҵ���Ԫ�����׳��쳣
	 */
	public static void writeCellData(Dispatch sheet, String cellName, Object cellValue) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("ģ����δ�ҵ�����Ϊ[" + cellName + "]�ĵ�Ԫ������ϵ����Ա��");
		Dispatch.put(cell, "Value", cellValue);
	}
	
	/**
	 * ���õ�Ԫ����ɫ
	 * 
	 * @param sheet ��Ҫ������Sheet
	 * @param cellName ��Ԫ������
	 * @param cellValue ��Ҫ���õ���ɫ
	 * @throws Exception
	 */
	public static void setCellColor(Dispatch sheet, String cellName, Object cellValue) throws Exception
	{
		Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null) {
			throw new Exception("ģ����δ�ҵ�����Ϊ[" + cellName + "]�ĵ�Ԫ������ϵ����Ա��");
		}
		Dispatch Interior = Dispatch.get(cell, "Interior").toDispatch();
		Dispatch.put(Interior, "Color", cellValue);
	}
	
	/**
	 * ���ݵ�Ԫ�����Ʋ���ͼƬ
	 * 
	 * @param sheet ��Ҫ������Sheet
	 * @param cellName ��Ԫ������
	 * @param imageFile ͼƬ�ļ�
	 * @throws Exception ����������δ�ҵ���Ԫ�����׳��쳣
	 */
	public static void insertPicture(Dispatch sheet, String cellName, File imageFile) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("ģ����δ�ҵ�����Ϊ[" + cellName + "]�ĵ�Ԫ������ϵ����Ա��");
		
		Dispatch.call(cell, "Select"); //�ڹ������У���λ��Ҫ����ͼƬ�ľ���λ��
		Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
		Dispatch.call(select, "Insert", imageFile.getAbsolutePath()).toDispatch();
	}
	
	/**
	 * ����ָ����Χ�ϲ���Ԫ��
	 * 
	 * @param sheet ��Ҫ������Sheet
	 * @param rangeArea �ϲ�������A1:C2
	 * @return ���غϲ���ĵ�Ԫ��
	 * @throws Exception ����������δ�ҵ���Ԫ���������׳��쳣
	 */
	public static Dispatch mergeCell(Dispatch sheet, String rangeArea) throws Exception
	{
		Dispatch rangeCell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { rangeArea }, new int[1]).toDispatch();
		if (rangeCell == null) {
			throw new Exception("ģ����δ�ҵ�����Ϊ[" + rangeArea + "]����������ϵ����Ա��");
		}
		Dispatch.call(rangeCell, "Merge");
		
		return rangeCell;
	}
	
	/**
	 * ��ͬһ��Excel�и���sheetҳ
	 * 
	 * @param sheets Excel������Sheet
	 * @param pageNum ��Ҫ���Ƶ�ҳ��
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
	 * ��sheetҳ�н�ָ���и��Ƴ�����
	 * 
	 * @param sheet
	 * @param rowNo ָ����
	 * @param insertRows ��Ҫ���Ƶ�����
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
	 * ��ָ�������������п���
	 * 
	 * @param sheet
	 * @param rangeName
	 */
	public static void setAllBorders(Dispatch sheet, String rangeName)
	{
		Dispatch range = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{rangeName}, new int[1]).toDispatch();
		
		Dispatch borders = Dispatch.get(range, "Borders").toDispatch();
		
		// 1��2��3��4 ���ζ�Ӧ ���ҡ��ϡ���
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
	 * ��ָ����������������
	 * 
	 * @param sheet
	 * @param rangeName
	 */
	public static void setOutBorders(Dispatch sheet, String rangeName)
	{
		Dispatch range = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{rangeName}, new int[1]).toDispatch();
		
		Dispatch borders = Dispatch.get(range, "Borders").toDispatch();
		
		// 7��8��9��10 ���ζ�Ӧ ���ϡ��¡���
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
	 * �޸�����Sheetҳ���ƣ���ǰ׺��_ҳ�����
	 * 
	 * @param sheets Excel�����е�Sheet
	 * @param prefix sheetҳ����ǰ׺
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
	 * ���沢�ر�Excel�ĵ�������Excel����
	 * 
	 * @param excelApp Excel����
	 * @param workBooks Excel������
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
