package com.dayun.report.taskreport;

import com.dayun.report.utils.ExcelUtil;
import com.dayun.report.utils.TcUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentScheduleTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class TaskReportJob
  extends Job
{
  private TCComponentScheduleTask scheduleTask;
  private Vector<TCComponentScheduleTask> workTaskVector = new Vector<TCComponentScheduleTask>();
  private Map<TCComponentScheduleTask, TCComponentScheduleTask> workToPlanMap = new HashMap<TCComponentScheduleTask, TCComponentScheduleTask>();
  private LinkedHashMap<String, Vector<Task>> groupToTaskMap = new LinkedHashMap<String, Vector<Task>>();
  private Vector<String> groupNameVector = new Vector<String>();
  private boolean completed = false;
  
  public TaskReportJob(String name, TCComponentScheduleTask paramScheduleTask)
  {
    super(name);
    this.scheduleTask = paramScheduleTask;
  }
  
  protected IStatus run(IProgressMonitor progressMonitor)
  {
    String tempDirectory = null;
    try
    {
      progressMonitor.beginTask("正在导出项目任务情况报表，请耐心等待...", -1);
      
      tempDirectory = TcUtil.getTempPath() + System.currentTimeMillis();
      File workingDir = new File(tempDirectory);
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
      String preferenceName = "dy_Task_report_template";
      File templateFile = TcUtil.getTemplateFile(preferenceName, tempDirectory);
      
      traverseScheduleTask(this.scheduleTask);
      if (this.workTaskVector.size() > 0)
      {
        File reportFile = outputReportFile(templateFile);
        
        String scheduleName = this.scheduleTask.getProperty("object_name");
        String datasetName = scheduleName + "项目任务情况报表" + TcUtil.getTimeStamp("yyyyMMddHHmmss");
        TCComponentDataset dataset = TcUtil.createDataset(datasetName, "", "MSExcelX");
        TcUtil.importFileToDataset(dataset, reportFile, "MSExcelX", "excel");
        TCComponentFolder newStuffFolder = dataset.getSession().getUser().getNewStuffFolder();
        newStuffFolder.add("contents", dataset);
      }
      else
      {
        MessageBox.post("没有可导出的项目工作任务信息！", "提示", 2);
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
  
  private void traverseScheduleTask(TCComponentScheduleTask scheduleTask)
    throws TCException
  {
    AIFComponentContext[] contexts = scheduleTask.getChildren();
    if ((contexts != null) && (contexts.length > 0)) {
      for (AIFComponentContext context : contexts) {
        if ((context.getComponent() instanceof TCComponentScheduleTask))
        {
          TCComponentScheduleTask childScheduleTask = (TCComponentScheduleTask)context.getComponent();
          String taskType = childScheduleTask.getType();
          if (taskType.equals("E9_TY_WorkTask"))
          {
            this.workTaskVector.add(childScheduleTask);
            

            String parentTaskType = scheduleTask.getType();
            if (parentTaskType.equals("E9_TY_MasterPlan")) {
              this.workToPlanMap.put(childScheduleTask, scheduleTask);
            }
          }
          traverseScheduleTask(childScheduleTask);
        }
      }
    }
  }
  
  private Vector<Task> getTaskVector()
  {
    Vector<Task> projectTaskVector = new Vector<Task>();
    for (TCComponentScheduleTask workTask : this.workTaskVector)
    {
      TCComponentScheduleTask masterPlan = (TCComponentScheduleTask)this.workToPlanMap.get(workTask);
      Task projectTask = new Task(workTask, masterPlan);
      projectTaskVector.add(projectTask);
      
      String groupName = projectTask.responsibleDepartment;
      Vector<Task> taskVector = (Vector<Task>)this.groupToTaskMap.get(groupName);
      if (taskVector == null) {
        taskVector = new Vector<Task>();
      }
      taskVector.add(projectTask);
      this.groupToTaskMap.put(groupName, taskVector);
    }
    return projectTaskVector;
  }
  
  private Vector<TaskStatistics> getTaskStatisticsVector()
  {
    Vector<TaskStatistics> taskStatisticVector = new Vector<TaskStatistics>();
    
    int taskTotalCount = 0;
    int onScheduleCount = 0;
    int delayedCount = 0;
    int deferredCount = 0;
    int unexpiredCount = 0;
    
    Iterator<Map.Entry<String, Vector<Task>>> iterator = this.groupToTaskMap.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry<String, Vector<Task>> entry = (Map.Entry<String, Vector<Task>>)iterator.next();
      String groupName = (String)entry.getKey();
      Vector<Task> taskVector = (Vector<Task>)entry.getValue();
      TaskStatistics taskStatistics = new TaskStatistics(groupName, taskVector);
      taskStatisticVector.add(taskStatistics);
      
      taskTotalCount += taskStatistics.taskTotalCount;
      onScheduleCount += taskStatistics.onScheduleCount;
      delayedCount += taskStatistics.delayedCount;
      deferredCount += taskStatistics.deferredCount;
      unexpiredCount += taskStatistics.unexpiredCount;
      
      this.groupNameVector.add(groupName);
    }
    TaskStatistics taskStatistics = new TaskStatistics(
      "总计", taskTotalCount, onScheduleCount, delayedCount, 
      deferredCount, unexpiredCount);
    taskStatisticVector.add(taskStatistics);
    
    return taskStatisticVector;
  }
  
  private File outputReportFile(File templateFile)
    throws Exception
  {
    Vector<Task> taskVector = getTaskVector();
    Vector<TaskStatistics> taskStatisticsVector = getTaskStatisticsVector();
    
    ActiveXComponent excelApp = ExcelUtil.openExcelApp();
    Dispatch workBook = null;
    try
    {
      workBook = ExcelUtil.getWorkBook(excelApp, templateFile);
      Dispatch sheets = ExcelUtil.getSheets(workBook);
      
      Dispatch taskSheet = ExcelUtil.getSheet(sheets, Integer.valueOf(1));
      int taskNum = taskVector.size();
      if (taskNum > 7)
      {
        int minus = taskNum - 7;
        ExcelUtil.copyRow(taskSheet, 4, minus);
      }
      for (int i = 0; i < taskNum; i++)
      {
        Task task = (Task)taskVector.get(i);
        
        ExcelUtil.writeCellData(taskSheet, "A" + (4 + i), Integer.valueOf(i + 1));
        ExcelUtil.writeCellData(taskSheet, "B" + (4 + i), task.taskName);
        ExcelUtil.writeCellData(taskSheet, "C" + (4 + i), task.status);
        ExcelUtil.writeCellData(taskSheet, "D" + (4 + i), task.workSource);
        ExcelUtil.writeCellData(taskSheet, "E" + (4 + i), task.responsibleDepartment);
        ExcelUtil.writeCellData(taskSheet, "F" + (4 + i), task.responsiblePerson);
        ExcelUtil.writeCellData(taskSheet, "G" + (4 + i), task.startDate);
        ExcelUtil.writeCellData(taskSheet, "H" + (4 + i), task.actualStartDate);
        ExcelUtil.writeCellData(taskSheet, "I" + (4 + i), task.finishDate);
        ExcelUtil.writeCellData(taskSheet, "J" + (4 + i), task.actualFinishDate);
        ExcelUtil.writeCellData(taskSheet, "K" + (4 + i), task.progressDescription);
      }
      ExcelUtil.setAllBorders(taskSheet, "A4:K" + (taskNum + 3));
      
      Dispatch taskStatisticsSheet = ExcelUtil.getSheet(sheets, Integer.valueOf(2));
      int taskStatisticsNum = taskStatisticsVector.size();
      if (taskStatisticsNum > 21)
      {
        int minus = taskStatisticsNum - 21;
        ExcelUtil.copyRow(taskStatisticsSheet, 4, minus);
      }
      for (int i = 0; i < taskStatisticsNum; i++)
      {
        TaskStatistics taskStatistics = (TaskStatistics)taskStatisticsVector.get(i);
        
        ExcelUtil.writeCellData(taskStatisticsSheet, "B" + (4 + i), Integer.valueOf(i + 1));
        ExcelUtil.writeCellData(taskStatisticsSheet, "C" + (4 + i), taskStatistics.modelName);
        ExcelUtil.writeCellData(taskStatisticsSheet, "D" + (4 + i), Integer.valueOf(taskStatistics.taskTotalCount));
        ExcelUtil.writeCellData(taskStatisticsSheet, "E" + (4 + i), Integer.valueOf(taskStatistics.onScheduleCount));
        ExcelUtil.writeCellData(taskStatisticsSheet, "F" + (4 + i), Integer.valueOf(taskStatistics.delayedCount));
        ExcelUtil.writeCellData(taskStatisticsSheet, "G" + (4 + i), Integer.valueOf(taskStatistics.deferredCount));
        ExcelUtil.writeCellData(taskStatisticsSheet, "H" + (4 + i), Integer.valueOf(taskStatistics.unexpiredCount));
        ExcelUtil.writeCellData(taskStatisticsSheet, "I" + (4 + i), taskStatistics.onSchedulePercent);
        ExcelUtil.writeCellData(taskStatisticsSheet, "J" + (4 + i), taskStatistics.delayedPercent);
        ExcelUtil.writeCellData(taskStatisticsSheet, "K" + (4 + i), taskStatistics.totalPercent);
      }
      ExcelUtil.setAllBorders(taskStatisticsSheet, "B4:K" + (taskStatisticsNum + 3));
      if (taskStatisticsNum > 1)
      {
        int lastRowNum = 4 + taskStatisticsNum - 2;
        ExcelUtil.insertHistogram(taskStatisticsSheet, workBook, "C3:H" + lastRowNum, "H" + lastRowNum, "", true, this.groupNameVector, 1);
      }
    }
    finally
    {
      ExcelUtil.closeExcelApp(excelApp, workBook);
    }
    File reportFile = TcUtil.renameFile(templateFile, "项目任务情况报表");
    return reportFile;
  }
  
  public boolean isCompleted()
  {
    return this.completed;
  }
}
