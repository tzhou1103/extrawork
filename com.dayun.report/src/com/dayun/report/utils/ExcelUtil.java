package com.dayun.report.utils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import java.io.File;
import java.util.Vector;

public class ExcelUtil
{
  public static ActiveXComponent openExcelApp()
  {
    ComThread.InitSTA();
    ActiveXComponent excelApp = new ActiveXComponent("Excel.Application");
    excelApp.setProperty("Visible", new Variant(false));
    
    return excelApp;
  }
  
  public static Dispatch getWorkBook(ActiveXComponent excelApp, File paramFile)
    throws Exception
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
  
  public static void setSheetName(Dispatch sheet, String sheetName)
  {
    Dispatch.call(sheet, "Select");
    Dispatch.put(sheet, "Name", sheetName);
  }
  
  public static void writeCellData(Dispatch sheet, String cellName, Object cellValue)
    throws Exception
  {
    Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
    if (cell == null) {
      throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
    }
    Dispatch.put(cell, "Value", cellValue);
  }
  
  public static void setCellColor(Dispatch sheet, String cellName, Object cellValue)
    throws Exception
  {
    Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
    if (cell == null) {
      throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
    }
    Dispatch interior = Dispatch.get(cell, "Interior").toDispatch();
    Dispatch.put(interior, "Color", cellValue);
  }
  
  public static void insertPicture(Dispatch sheet, String cellName, String imagePath)
  {
    Dispatch range = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
    Variant left = Dispatch.get(range, "Left");
    Variant top = Dispatch.get(range, "Top");
    Dispatch.call(range, "Select");
    Dispatch shapes = Dispatch.call(sheet, "Shapes").toDispatch();
    Dispatch.call(shapes, "AddPicture", new Object[] { imagePath, Boolean.valueOf(false), Boolean.valueOf(true), left, top, Integer.valueOf(-1), Integer.valueOf(-1) }).toDispatch();
  }
  
  public static Dispatch mergeCell(Dispatch sheet, String rangeArea)
    throws Exception
  {
    Dispatch range = Dispatch.invoke(sheet, "Range", 2, new Object[] { rangeArea }, new int[1]).toDispatch();
    if (range == null) {
      throw new Exception("模板中未找到名称为[" + rangeArea + "]的区域，请联系管理员！");
    }
    Dispatch.call(range, "Merge");
    
    return range;
  }
  
  public static void copySheet(Dispatch sheet, int pageNum)
  {
    for (int i = 0; i < pageNum; i++) {
      Dispatch.call(sheet, "Copy", new Object[] { sheet });
    }
  }
  
  public static void copyRow(Dispatch sheet, int rowNo, int insertRows)
  {
    String rowArea = rowNo + ":" + rowNo;
    Dispatch row = Dispatch.invoke(sheet, "Rows", 2, new Object[] { rowArea }, new int[1]).toDispatch();
    for (int i = 0; i < insertRows; i++)
    {
      Dispatch.call(row, "Copy");
      Dispatch.call(row, "Insert");
    }
  }
  
  public static void setAllBorders(Dispatch sheet, String rangeName)
  {
    Dispatch range = Dispatch.invoke(sheet, "Range", 2, new Object[] { rangeName }, new int[1]).toDispatch();
    
    Dispatch borders = Dispatch.get(range, "Borders").toDispatch();
    

    Dispatch edgeLeft = Dispatch.call(borders, "Item", new Object[] { new Variant(1) }).toDispatch();
    Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
    
    edgeLeft = Dispatch.call(borders, "Item", new Object[] { new Variant(2) }).toDispatch();
    Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
    
    edgeLeft = Dispatch.call(borders, "Item", new Object[] { new Variant(3) }).toDispatch();
    Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
    
    edgeLeft = Dispatch.call(borders, "Item", new Object[] { new Variant(4) }).toDispatch();
    Dispatch.put(edgeLeft, "LineStyle", new Variant(1));
  }
  
  public static void setOutBorders(Dispatch sheet, String rangeName)
  {
    Dispatch range = Dispatch.invoke(sheet, "Range", 2, new Object[] { rangeName }, new int[1]).toDispatch();
    
    Dispatch borders = Dispatch.get(range, "Borders").toDispatch();
    

    Dispatch edge = Dispatch.call(borders, "Item", new Object[] { new Variant(7) }).toDispatch();
    Dispatch.put(edge, "LineStyle", new Variant(1));
    
    edge = Dispatch.call(borders, "Item", new Object[] { new Variant(8) }).toDispatch();
    Dispatch.put(edge, "LineStyle", new Variant(1));
    
    edge = Dispatch.call(borders, "Item", new Object[] { new Variant(9) }).toDispatch();
    Dispatch.put(edge, "LineStyle", new Variant(1));
    
    edge = Dispatch.call(borders, "Item", new Object[] { new Variant(10) }).toDispatch();
    Dispatch.put(edge, "LineStyle", new Variant(1));
  }
  
  public static void insertHistogram(Dispatch sheet, Dispatch workBook, String rangeName, String cellName, String title, boolean showLabel, Vector<String> dataNameVector, int deleteLegendIndex)
  {
    Dispatch.call(sheet, "Activate");
    Dispatch range = Dispatch.invoke(sheet, "Range", 2, new Object[] { rangeName }, new int[1]).toDispatch();
    Dispatch.call(range, "Select");
    
    Dispatch cell = Dispatch.invoke(sheet, "Range", 2, new Object[] { cellName }, new int[1]).toDispatch();
    Dispatch.call(cell, "Activate");
    

    Dispatch shapes = Dispatch.get(sheet, "Shapes").toDispatch();
    Dispatch chart = Dispatch.call(shapes, "AddChart2", new Object[] { new Variant(201), new Variant(51) }).toDispatch();
    Dispatch.call(chart, "Select");
    

    Dispatch activeChart = Dispatch.get(workBook, "ActiveChart").toDispatch();
    Dispatch.call(activeChart, "SetSourceData", new Object[] { range });
    

    Dispatch chartTitle = Dispatch.get(activeChart, "ChartTitle").toDispatch();
    if ((title == null) || (title.isEmpty()))
    {
      Dispatch.call(chartTitle, "Select");
      Dispatch.call(chartTitle, "Delete");
    }
    else
    {
      Dispatch.put(chartTitle, "Text", title);
    }
    if (showLabel) {
      Dispatch.call(activeChart, "SetElement", new Object[] { new Variant(205) });
    }
    if (dataNameVector.size() < 2) {
      return;
    }
    Dispatch legend = Dispatch.get(activeChart, "Legend").toDispatch();
    Dispatch.call(legend, "Select");
    Dispatch legendEntry = Dispatch.call(legend, "LegendEntries", new Object[] { new Variant(1) }).toDispatch();
    Dispatch.call(legendEntry, "Select");
    

    Dispatch seriesCollection = Dispatch.call(activeChart, "FullSeriesCollection", new Object[] { new Variant(1) }).toDispatch();
    String seriesName = Dispatch.get(seriesCollection, "Name").getString();
    if (dataNameVector.contains(seriesName)) {
      Dispatch.call(activeChart, "SetSourceData", new Object[] { range, new Variant(2) });
    }
    if (deleteLegendIndex > 0)
    {
      seriesCollection = Dispatch.call(activeChart, "FullSeriesCollection", new Object[] { new Variant(deleteLegendIndex) }).toDispatch();
      Dispatch.call(seriesCollection, "Select");
      Dispatch.call(seriesCollection, "Delete");
    }
  }
  
  public static void closeExcelApp(ActiveXComponent excelApp, Dispatch workBook)
  {
    Dispatch.call(workBook, "Save");
    Dispatch.call(workBook, "Close", new Object[] { new Variant(false) });
    if (excelApp != null) {
      excelApp.invoke("Quit", new Variant[0]);
    }
    ComThread.Release();
  }
}
