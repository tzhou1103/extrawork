package com.dayun.report.handlers;

import com.dayun.report.deliveryreport.DeliveryReportJob;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentScheduleTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeliveryReportHandler
  extends AbstractHandler
{
  public Object execute(ExecutionEvent event)
    throws ExecutionException
  {
    try
    {
      final Shell activeShell = HandlerUtil.getActiveShell(event);
      
      InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
      if ((targetComponent == null) || (!(targetComponent instanceof TCComponentScheduleTask)))
      {
        MessageBox.post("请选择时间表顶层节点！", "提示", 2);
        return null;
      }
      TCComponentScheduleTask scheduleTask = (TCComponentScheduleTask)targetComponent;
      int taskType = scheduleTask.getIntProperty("task_type");
      if (taskType != 6)
      {
        MessageBox.post("请选择时间表顶层节点！", "提示", 2);
        return null;
      }
      DeliveryReportJob deliveryReportJob = new DeliveryReportJob("进度", scheduleTask);
      deliveryReportJob.addJobChangeListener(new JobChangeAdapter()
      {
        public void done(IJobChangeEvent event)
        {
          DeliveryReportJob deliveryReportJob = (DeliveryReportJob)event.getJob();
          if (deliveryReportJob.isCompleted()) {
            activeShell.getDisplay().asyncExec(new Runnable()
            {
              public void run()
              {
                MessageDialog.openInformation(activeShell, "提示", "导出完成！");
              }
            });
          }
        }
      });
      deliveryReportJob.setPriority(10);
      deliveryReportJob.setUser(true);
      deliveryReportJob.schedule();
    }
    catch (TCException e)
    {
      e.printStackTrace();
      MessageBox.post(e);
    }
    return null;
  }
}
