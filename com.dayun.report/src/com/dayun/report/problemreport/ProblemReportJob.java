package com.dayun.report.problemreport;

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

public class ProblemReportJob
  extends Job
{
  private TCComponentFolder targetFolder;
  private Vector<Problem> problemVector = new Vector<Problem>();
  private LinkedHashMap<String, Vector<Problem>> groupToProblemMap = new LinkedHashMap<String, Vector<Problem>>();
  private Vector<String> groupNameVector = new Vector<String>();
  private boolean completed = false;
  
  public ProblemReportJob(String name, TCComponentFolder paramFolder)
  {
    super(name);
    this.targetFolder = paramFolder;
  }
  
  protected IStatus run(IProgressMonitor progressMonitor)
  {
    String tempDirectory = null;
    try
    {
      progressMonitor.beginTask("正在导出问题情况报表，请耐心等待...", -1);
      
      tempDirectory = TcUtil.getTempPath() + System.currentTimeMillis();
      File workingDir = new File(tempDirectory);
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
      String preferenceName = "dy_issue_report_template";
      File templateFile = TcUtil.getTemplateFile(preferenceName, tempDirectory);
      
      getProblemVector();
      if (this.problemVector.size() > 0)
      {
        File reportFile = outputReportFile(templateFile);
        
        String datasetName = getProjectName() + "项目问题情况报表" + TcUtil.getTimeStamp("yyyyMMddHHmmss");
        TCComponentDataset dataset = TcUtil.createDataset(datasetName, "", "MSExcelX");
        TcUtil.importFileToDataset(dataset, reportFile, "MSExcelX", "excel");
        TCComponentFolder newStuffFolder = dataset.getSession().getUser().getNewStuffFolder();
        newStuffFolder.add("contents", dataset);
      }
      else
      {
        MessageBox.post("没有可导出的问题信息！", "提示", 2);
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
        if (tcComponent.isTypeOf("E9_TY_IssueRep"))
        {
          TCComponentItem item = (TCComponentItem)tcComponent;
          Problem problem = new Problem(item);
          this.problemVector.add(problem);
          
          String groupName = problem.responsibleDepartment;
          Vector<Problem> problemVector = (Vector<Problem>)this.groupToProblemMap.get(groupName);
          if (problemVector == null) {
            problemVector = new Vector<Problem>();
          }
          problemVector.add(problem);
          this.groupToProblemMap.put(groupName, problemVector);
        }
      }
    }
  }
  
  private Vector<ProblemStatistics> getProblemStatisticsVector()
  {
    Vector<ProblemStatistics> problemStatisticsVector = new Vector<ProblemStatistics>();
    
    int taskTotalCount = 0;
    int onScheduleCount = 0;
    int expectedCount = 0;
    int deferredCount = 0;
    int unexpiredCount = 0;
    
    Iterator<Map.Entry<String, Vector<Problem>>> iterator = this.groupToProblemMap.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry<String, Vector<Problem>> entry = (Map.Entry<String, Vector<Problem>>)iterator.next();
      String groupName = (String)entry.getKey();
      Vector<Problem> deliveryVector = (Vector<Problem>)entry.getValue();
      ProblemStatistics problemStatistics = new ProblemStatistics(groupName, deliveryVector);
      problemStatisticsVector.add(problemStatistics);
      
      taskTotalCount += problemStatistics.problemTotalCount;
      onScheduleCount += problemStatistics.onScheduleCount;
      expectedCount += problemStatistics.expectedCount;
      deferredCount += problemStatistics.deferredCount;
      unexpiredCount += problemStatistics.unexpiredCount;
      
      this.groupNameVector.add(groupName);
    }
    ProblemStatistics problemStatistics = new ProblemStatistics(
      "总计", taskTotalCount, onScheduleCount, expectedCount, 
      deferredCount, unexpiredCount);
    problemStatisticsVector.add(problemStatistics);
    
    return problemStatisticsVector;
  }
  
  private File outputReportFile(File templateFile)
    throws Exception
  {
    Vector<ProblemStatistics> problemStatisticsVector = getProblemStatisticsVector();
    
    ActiveXComponent excelApp = ExcelUtil.openExcelApp();
    Dispatch workBook = null;
    try
    {
      workBook = ExcelUtil.getWorkBook(excelApp, templateFile);
      Dispatch sheets = ExcelUtil.getSheets(workBook);
      
      Dispatch problemSheet = ExcelUtil.getSheet(sheets, Integer.valueOf(1));
      int problemNum = this.problemVector.size();
      if (problemNum > 7)
      {
        int minus = problemNum - 7;
        ExcelUtil.copyRow(problemSheet, 4, minus);
      }
      for (int i = 0; i < problemNum; i++)
      {
        Problem problem = (Problem)this.problemVector.get(i);
        
        ExcelUtil.writeCellData(problemSheet, "A" + (4 + i), Integer.valueOf(i + 1));
        ExcelUtil.writeCellData(problemSheet, "B" + (4 + i), problem.problemName);
        ExcelUtil.writeCellData(problemSheet, "C" + (4 + i), problem.problemDescription);
        ExcelUtil.writeCellData(problemSheet, "D" + (4 + i), problem.status);
        ExcelUtil.writeCellData(problemSheet, "E" + (4 + i), problem.sourcesWork);
        ExcelUtil.writeCellData(problemSheet, "F" + (4 + i), problem.proposedDepartments);
        ExcelUtil.writeCellData(problemSheet, "G" + (4 + i), problem.introducer);
        ExcelUtil.writeCellData(problemSheet, "H" + (4 + i), problem.responsibleDepartment);
        ExcelUtil.writeCellData(problemSheet, "I" + (4 + i), problem.responsiblePerson);
        ExcelUtil.writeCellData(problemSheet, "J" + (4 + i), problem.dataPlan);
        ExcelUtil.writeCellData(problemSheet, "K" + (4 + i), problem.estimatedTime);
        ExcelUtil.writeCellData(problemSheet, "L" + (4 + i), problem.dateReleased);
        ExcelUtil.writeCellData(problemSheet, "M" + (4 + i), problem.progressDescription);
      }
      ExcelUtil.setAllBorders(problemSheet, "A4:M" + (problemNum + 3));
      
      Dispatch problemStatisticsSheet = ExcelUtil.getSheet(sheets, Integer.valueOf(2));
      int problemStatisticsNum = problemStatisticsVector.size();
      if (problemStatisticsNum > 21)
      {
        int minus = problemStatisticsNum - 21;
        ExcelUtil.copyRow(problemStatisticsSheet, 4, minus);
      }
      for (int i = 0; i < problemStatisticsNum; i++)
      {
        ProblemStatistics problemStatistics = (ProblemStatistics)problemStatisticsVector.get(i);
        
        ExcelUtil.writeCellData(problemStatisticsSheet, "B" + (4 + i), Integer.valueOf(i + 1));
        ExcelUtil.writeCellData(problemStatisticsSheet, "C" + (4 + i), problemStatistics.modelName);
        ExcelUtil.writeCellData(problemStatisticsSheet, "D" + (4 + i), Integer.valueOf(problemStatistics.problemTotalCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "E" + (4 + i), Integer.valueOf(problemStatistics.onScheduleCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "F" + (4 + i), Integer.valueOf(problemStatistics.expectedCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "G" + (4 + i), Integer.valueOf(problemStatistics.deferredCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "H" + (4 + i), Integer.valueOf(problemStatistics.unexpiredCount));
        ExcelUtil.writeCellData(problemStatisticsSheet, "I" + (4 + i), problemStatistics.onSchedulePercent);
        ExcelUtil.writeCellData(problemStatisticsSheet, "J" + (4 + i), problemStatistics.expectedPercent);
        ExcelUtil.writeCellData(problemStatisticsSheet, "K" + (4 + i), problemStatistics.totalPercent);
      }
      ExcelUtil.setAllBorders(problemStatisticsSheet, "B4:K" + (problemStatisticsNum + 3));
      if (problemStatisticsNum > 1)
      {
        int lastRowNum = 4 + problemStatisticsNum - 2;
        ExcelUtil.insertHistogram(problemStatisticsSheet, workBook, "C3:H" + lastRowNum, "H" + lastRowNum, "", true, this.groupNameVector, 1);
      }
    }
    finally
    {
      ExcelUtil.closeExcelApp(excelApp, workBook);
    }
    File reportFile = TcUtil.renameFile(templateFile, "项目问题情况报表");
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
