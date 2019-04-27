package com.dayun.report.massissuereport;

import com.dayun.report.utils.ExcelUtil;
import com.dayun.report.utils.TcUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class MassIssueReportJob
  extends Job
{
  private TCComponentFolder targetFolder;
  private Vector<QualityProblem> problemVector = new Vector<QualityProblem>();
  private LinkedHashMap<String, Vector<QualityProblem>> groupToProblemMap = new LinkedHashMap<String, Vector<QualityProblem>>();
  private Vector<String> groupNameVector = new Vector<String>();
  private boolean completed = false;
  
  public MassIssueReportJob(String name, TCComponentFolder paramFolder)
  {
    super(name);
    this.targetFolder = paramFolder;
  }
  
  protected IStatus run(IProgressMonitor progressMonitor)
  {
    String tempDirectory = null;
    try
    {
      progressMonitor.beginTask("正在导出质量问题情况报表，请耐心等待...", -1);
      
      tempDirectory = TcUtil.getTempPath() + System.currentTimeMillis();
      File workingDir = new File(tempDirectory);
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
      String preferenceName = "dy_massissue_report_template";
      File templateFile = TcUtil.getTemplateFile(preferenceName, tempDirectory);
      
      getProblemVector();
      if (this.problemVector.size() > 0)
      {
        File reportFile = outputReportFile(templateFile);
        
        String datasetName = getProjectName() + "项目质量问题情况报表" + TcUtil.getTimeStamp("yyyyMMddHHmmss");
        TCComponentDataset dataset = TcUtil.createDataset(datasetName, "", "MSExcelX");
        TcUtil.importFileToDataset(dataset, reportFile, "MSExcelX", "excel");
        TCComponentFolder newStuffFolder = dataset.getSession().getUser().getNewStuffFolder();
        newStuffFolder.add("contents", dataset);
      }
      else
      {
        MessageBox.post("没有可导出的质量问题信息！", "提示", 2);
        IStatus localIStatus = Status.CANCEL_STATUS;return localIStatus;
      }
      this.completed = true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      MessageBox.post(e);
    }
    finally
    {
      TcUtil.deleteFolder(tempDirectory);
      progressMonitor.done();
    }
    return Status.OK_STATUS;
  }
  
  private void getProblemVector()
    throws TCException
  {
    TCComponent[] relatedComponents = this.targetFolder.getRelatedComponents("contents");
    if ((relatedComponents != null) && (relatedComponents.length > 0)) {
      for (TCComponent tcComponent : relatedComponents) {
        if (tcComponent.isTypeOf("E9_TY_MassIssue"))
        {
          TCComponentItem item = (TCComponentItem)tcComponent;
          QualityProblem qualityProblem = new QualityProblem(item);
          this.problemVector.add(qualityProblem);
          
          String groupName = qualityProblem.responsibleDepartment;
          Vector<QualityProblem> problemVector = (Vector<QualityProblem>)this.groupToProblemMap.get(groupName);
          if (problemVector == null) {
            problemVector = new Vector<QualityProblem>();
          }
          problemVector.add(qualityProblem);
          this.groupToProblemMap.put(groupName, problemVector);
        }
      }
    }
  }
  
  private Vector<QualityProblemStatistics> getQualityProblemStatisticsVector()
  {
    Vector<QualityProblemStatistics> vector = new Vector<QualityProblemStatistics>();
    
    int totalCount = 0;
    int redCount = 0;
    int yellowCount = 0;
    int greenCount = 0;
    int grayCount = 0;
    
    Iterator<Map.Entry<String, Vector<QualityProblem>>> iterator = this.groupToProblemMap.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry<String, Vector<QualityProblem>> entry = (Map.Entry<String, Vector<QualityProblem>>)iterator.next();
      String groupName = (String)entry.getKey();
      Vector<QualityProblem> qualityProblemVector = (Vector<QualityProblem>)entry.getValue();
      QualityProblemStatistics problemStatistics = new QualityProblemStatistics(groupName, qualityProblemVector);
      vector.add(problemStatistics);
      
      totalCount += problemStatistics.totalCount;
      redCount += problemStatistics.redCount;
      yellowCount += problemStatistics.yellowCount;
      greenCount += problemStatistics.greenCount;
      grayCount += problemStatistics.grayCount;
      
      this.groupNameVector.add(groupName);
    }
    QualityProblemStatistics problemStatistics = new QualityProblemStatistics(
      "总计", totalCount, redCount, yellowCount, 
      greenCount, grayCount);
    vector.add(problemStatistics);
    
    return vector;
  }
  
  private File outputReportFile(File templateFile)
    throws Exception
  {
    Vector<QualityProblemStatistics> problemStatisticsVector = getQualityProblemStatisticsVector();
    
    ActiveXComponent excelApp = ExcelUtil.openExcelApp();
    Dispatch workBook = null;
    try
    {
      workBook = ExcelUtil.getWorkBook(excelApp, templateFile);
      Dispatch sheets = ExcelUtil.getSheets(workBook);
      
      Dispatch problemSheet = ExcelUtil.getSheet(sheets, Integer.valueOf(1));
      int problemNum = this.problemVector.size();
      for (int i = 0; i < problemNum; i++)
      {
        QualityProblem problem = (QualityProblem)this.problemVector.get(i);
        
        ExcelUtil.writeCellData(problemSheet, "A" + (3 + i), Integer.valueOf(i + 1));
        ExcelUtil.writeCellData(problemSheet, "B" + (3 + i), problem.itemID);
        ExcelUtil.writeCellData(problemSheet, "C" + (3 + i), problem.objectName);
        ExcelUtil.writeCellData(problemSheet, "D" + (3 + i), problem.modular);
        ExcelUtil.writeCellData(problemSheet, "E" + (3 + i), problem.sourcesIssue);
        ExcelUtil.writeCellData(problemSheet, "F" + (3 + i), problem.proposeDate);
        ExcelUtil.writeCellData(problemSheet, "G" + (3 + i), problem.proposerDepartment);
        ExcelUtil.writeCellData(problemSheet, "H" + (3 + i), problem.issueLevel);
        ExcelUtil.writeCellData(problemSheet, "I" + (3 + i), problem.responsiblerDepartment);
        ExcelUtil.writeCellData(problemSheet, "J" + (3 + i), problem.probleamStatus);
        ExcelUtil.writeCellData(problemSheet, "K" + (3 + i), problem.issueDescription);
        ExcelUtil.writeCellData(problemSheet, "L" + (3 + i), problem.causeAnalysis);
        ExcelUtil.writeCellData(problemSheet, "M" + (3 + i), problem.interimMeasures);
        ExcelUtil.writeCellData(problemSheet, "N" + (3 + i), problem.ultimateMeasures);
        ExcelUtil.writeCellData(problemSheet, "O" + (3 + i), problem.rectificationProgress);
        ExcelUtil.writeCellData(problemSheet, "P" + (3 + i), problem.dataPlan);
        ExcelUtil.writeCellData(problemSheet, "Q" + (3 + i), problem.estimatedTime);
        ExcelUtil.writeCellData(problemSheet, "R" + (3 + i), problem.dateReleased);
        ExcelUtil.writeCellData(problemSheet, "R" + (3 + i), problem.validateTruckNO);
        ExcelUtil.writeCellData(problemSheet, "R" + (3 + i), problem.remark);
      }
      ExcelUtil.setAllBorders(problemSheet, "A3:R" + (problemNum + 2));
      
      Dispatch problemStatisticsSheet = ExcelUtil.getSheet(sheets, Integer.valueOf(2));
      int problemStatisticsNum = problemStatisticsVector.size();
      for (int i = 0; i < problemStatisticsNum; i++)
      {
        QualityProblemStatistics problemStatistics = (QualityProblemStatistics)problemStatisticsVector.get(i);
        
        ExcelUtil.writeCellData(problemStatisticsSheet, "B" + (4 + i), Integer.valueOf(i + 1));
        ExcelUtil.writeCellData(problemStatisticsSheet, "C" + (4 + i), problemStatistics.responsibleDepartment);
        ExcelUtil.writeCellData(problemStatisticsSheet, "D" + (4 + i), Integer.valueOf(problemStatistics.redCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "E" + (4 + i), Integer.valueOf(problemStatistics.yellowCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "F" + (4 + i), Integer.valueOf(problemStatistics.greenCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "G" + (4 + i), Integer.valueOf(problemStatistics.grayCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "H" + (4 + i), problemStatistics.closingRate);
      }
      ExcelUtil.setAllBorders(problemStatisticsSheet, "B4:H" + (problemStatisticsNum + 3));
      if (problemStatisticsNum > 1)
      {
        int lastRowNum = 4 + problemStatisticsNum - 2;
        ExcelUtil.insertHistogram(problemStatisticsSheet, workBook, "C3:F" + lastRowNum, "F" + lastRowNum, "", true, this.groupNameVector, -1);
      }
    }
    finally
    {
      ExcelUtil.closeExcelApp(excelApp, workBook);
    }
    File reportFile = TcUtil.renameFile(templateFile, "项目质量问题情况报表");
    return reportFile;
  }
  
  private String getProjectName()
    throws TCException
  {
    TCComponent[] tcComponents = this.targetFolder.getReferenceListProperty("project_list");
    if ((tcComponents != null) && (tcComponents.length > 0))
    {
      TCComponentProject tcProject = (TCComponentProject)tcComponents[0];
      return tcProject.getProjectName();
    }
    return "";
  }
  
  public boolean isCompleted()
  {
    return this.completed;
  }
}
