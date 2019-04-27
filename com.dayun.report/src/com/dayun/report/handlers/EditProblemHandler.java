package com.dayun.report.handlers;

import com.dayun.report.editproblem.DatasetBean;
import com.dayun.report.editproblem.EditMassIssueDialog;
import com.dayun.report.editproblem.EditProblemDialog;
import com.dayun.report.utils.TcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class EditProblemHandler
  extends AbstractHandler
{
  public Object execute(ExecutionEvent event)
    throws ExecutionException
  {
    try
    {
      Shell activeShell = HandlerUtil.getActiveShell(event);
      InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
      TCComponentItemRevision itemRev = (TCComponentItemRevision)targetComponent;
      
      Vector<DatasetBean> vector = TcUtil.getAllDatasetBeans("dy_datasetinfo_config");
      if (vector.size() > 0)
      {
        if (itemRev.isTypeOf("E9_TY_IssueRepRevision"))
        {
          EditProblemDialog editProblemDialog = new EditProblemDialog(activeShell, itemRev, vector);
          editProblemDialog.open();
        }
        else
        {
          EditMassIssueDialog editMassIssueDialog = new EditMassIssueDialog(activeShell, itemRev, vector);
          editMassIssueDialog.open();
        }
      }
      else
      {
        MessageBox.post("首选项 dy_datasetinfo_config 配置有误，请联系管理员！", "错误", 1);
        return null;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      MessageBox.post(e);
    }
    return null;
  }
}
