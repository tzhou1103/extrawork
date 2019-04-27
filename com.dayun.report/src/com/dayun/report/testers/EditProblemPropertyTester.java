package com.dayun.report.testers;

import com.dayun.report.utils.TcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import org.eclipse.core.expressions.PropertyTester;

public class EditProblemPropertyTester
  extends PropertyTester
{
  public boolean test(Object targetObject, String property, Object[] args, Object expectValue)
  {
    InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
    if ((targetComponent != null) && ((targetComponent instanceof TCComponentItemRevision)))
    {
      TCComponentItemRevision itemRev = (TCComponentItemRevision)targetComponent;
      return validComponent(itemRev);
    }
    return false;
  }
  
  private boolean validComponent(TCComponentItemRevision itemRev)
  {
    try
    {
      if ((TcUtil.isObjectInProcess(itemRev)) && (itemRev.isTypeOf("E9_TY_IssueRepRevision")))
      {
        String responsibleDepartment = itemRev.getProperty("e9_ResponsiblerDepartment");
        
        int leftIndex = responsibleDepartment.indexOf("(");
        int rightIndex = responsibleDepartment.indexOf(")");
        if ((leftIndex > 0) && (rightIndex > 0))
        {
          String userID = responsibleDepartment.substring(leftIndex + 1, rightIndex);
          if (userID.equals(itemRev.getSession().getUser().getUserId())) {
            return true;
          }
        }
      }
    }
    catch (TCException e)
    {
      e.printStackTrace();
    }
    return false;
  }
}
