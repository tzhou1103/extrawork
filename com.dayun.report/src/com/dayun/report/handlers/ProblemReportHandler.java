package com.dayun.report.handlers;

import com.dayun.report.problemreport.ProblemReportJob;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentFolder;
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

public class ProblemReportHandler
  extends AbstractHandler
{
  public Object execute(ExecutionEvent event)
    throws ExecutionException
  {
    try
    {
      final Shell activeShell = HandlerUtil.getActiveShell(event);
      
      InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
      if ((targetComponent == null) || (!(targetComponent instanceof TCComponentFolder)))
      {
        MessageBox.post("请选择 项目问题管理 文件夹！", "提示", 2);
        return null;
      }
      TCComponentFolder targetFolder = (TCComponentFolder)targetComponent;
      String folderName = targetFolder.getProperty("object_name");
      if (!folderName.equals("项目问题管理"))
      {
        MessageBox.post("请选择 项目问题管理 文件夹！", "提示", 2);
        return null;
      }
      ProblemReportJob problemReportJob = new ProblemReportJob("进度", targetFolder);
      problemReportJob.addJobChangeListener(new JobChangeAdapter()
      {
        public void done(IJobChangeEvent event)
        {
          ProblemReportJob problemReportJob = (ProblemReportJob)event.getJob();
          if (problemReportJob.isCompleted()) {
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
      problemReportJob.setPriority(10);
      problemReportJob.setUser(true);
      problemReportJob.schedule();
    }
    catch (TCException e)
    {
      e.printStackTrace();
      MessageBox.post(e);
    }
    return null;
  }
}
