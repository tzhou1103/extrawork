package com.dayun.report.handlers;

import com.dayun.report.massissuereport.MassIssueReportJob;
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

public class MassIssueReportHandler
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
        MessageBox.post("请选择 项目质量问题管理 文件夹！", "提示", 2);
        return null;
      }
      TCComponentFolder targetFolder = (TCComponentFolder)targetComponent;
      String folderName = targetFolder.getProperty("object_name");
      if (!folderName.equals("项目质量问题管理"))
      {
        MessageBox.post("请选择 项目质量问题管理 文件夹！", "提示", 2);
        return null;
      }
      MassIssueReportJob qualityProblemJob = new MassIssueReportJob("进度", targetFolder);
      qualityProblemJob.addJobChangeListener(new JobChangeAdapter()
      {
        public void done(IJobChangeEvent event)
        {
          MassIssueReportJob qualityProblemJob = (MassIssueReportJob)event.getJob();
          if (qualityProblemJob.isCompleted()) {
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
      qualityProblemJob.setPriority(10);
      qualityProblemJob.setUser(true);
      qualityProblemJob.schedule();
    }
    catch (TCException e)
    {
      e.printStackTrace();
      MessageBox.post(e);
    }
    return null;
  }
}
