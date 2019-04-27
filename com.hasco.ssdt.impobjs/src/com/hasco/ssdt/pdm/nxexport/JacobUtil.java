package com.hasco.ssdt.pdm.nxexport;

import java.io.File;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * Jacob����Excel���÷���������
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
	 * ��Excel�ļ�����ȡExcel������
	 * 
	 * @param paramFile Excel�ļ�
	 * @return Excel������
	 * @throws Exception ����Excel�ļ����׳��쳣
	 */
	public static Dispatch getWorkBook(ActiveXComponent excelApp, String paramFilePath) throws Exception
	{
		if (!paramFilePath.endsWith(".xls") && !paramFilePath.endsWith(".xlsx")) {
			throw new Exception("[ " + paramFilePath + " ]����Excel�ļ�����ȷ�ϣ�");
		}
		
		Dispatch workbooks = excelApp.getProperty("Workbooks").toDispatch();
		Dispatch workBook = Dispatch.invoke(workbooks, "Open", Dispatch.Method, 
				new Object[] { paramFilePath, new Variant(false), new Variant(false) }, new int[1]).toDispatch();
		
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
		Dispatch sheets = Dispatch.get(workBook, "sheets").toDispatch();
		return sheets;
	}
	
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
	 * ���ݵ�Ԫ�����ƻ�ȡ����
	 * <p> �����֣��ص�ĩβ��.0
	 * 
	 * @param sheet ��Ҫ������Sheet
	 * @param cellName ��Ԫ������
	 * @throws Exception ����������δ�ҵ���Ԫ�����׳��쳣
	 */
	public static String getCellData(Dispatch sheet, String cellName) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("ģ����δ�ҵ�����Ϊ[" + cellName + "]�ĵ�Ԫ������ϵ����Ա��");
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
	 * �ر�Excel�ĵ�������Excel����
	 * 
	 * @param excelApp Excel����
	 * @param workBook Excel������
	 * @param needSave �Ƿ���Ҫ����
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
