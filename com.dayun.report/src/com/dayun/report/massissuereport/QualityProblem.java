package com.dayun.report.massissuereport;

import com.dayun.report.utils.Constants;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import java.util.Date;

public class QualityProblem
{
  public String itemID = "";
  public String objectName = "";
  public String modular = "";
  public String sourcesIssue = "";
  public String proposeDate = "";
  public String proposerDepartment = "";
  public String issueLevel = "";
  public String responsiblerDepartment = "";
  public String responsibleDepartment;
  public String probleamStatus = "";
  public String issueDescription = "";
  public String causeAnalysis = "";
  public String interimMeasures = "";
  public String ultimateMeasures;
  public String rectificationProgress = "";
  public String dataPlan = "";
  public String estimatedTime = "";
  public String dateReleased = "";
  public String validateTruckNO = "";
  public String remark = "";
  
  public QualityProblem(TCComponentItem massIssueItem)
    throws TCException
  {
    TCComponent[] revision_list = massIssueItem.getReferenceListProperty("revision_list");
    if ((revision_list != null) && (revision_list.length > 0))
    {
      TCComponentItemRevision itemRev = massIssueItem.getLatestItemRevision();
      String[] propNames = { "item_id", "object_name", "e9_Modular", 
        "e9_SourcesIssue", "e9_ProposerDepartment", 
        "e9_IssueLevel", "e9_ResponsiblerDepartment", 
        "e9_IssueDescription", "e9_CauseAnalysis", 
        "e9_InterimMeasures", "e9_UltimateMeasures", 
        "e9_RectificationProgress", "e9_ValidateTruckNO", 
        "e9_Remark" };
      String[] propValues = itemRev.getProperties(propNames);
      if ((propValues != null) && (propValues.length == 14))
      {
        this.itemID = propValues[0];
        this.objectName = propValues[1];
        this.modular = propValues[2];
        this.sourcesIssue = propValues[3];
        this.proposerDepartment = getSplice(propValues[4]);
        this.issueLevel = propValues[5];
        this.responsiblerDepartment = getSplice(propValues[6]);
        this.responsibleDepartment = propValues[6].substring(propValues[6].indexOf(")") + 1);
        this.issueDescription = propValues[7];
        this.causeAnalysis = propValues[8];
        this.interimMeasures = propValues[9];
        this.ultimateMeasures = propValues[10];
        this.rectificationProgress = propValues[11];
        this.validateTruckNO = propValues[12];
        this.remark = propValues[13];
      }
      Date proposeDate = itemRev.getDateProperty("e9_ProposeDate");
      if (proposeDate != null) {
        this.proposeDate = Constants.DATE_FORMAT.format(proposeDate);
      }
      Date planDate = itemRev.getDateProperty("e9_DataPlan");
      if (planDate != null) {
        this.dataPlan = Constants.DATE_FORMAT.format(planDate);
      }
      Date estimatedDate = itemRev.getDateProperty("e9_EstimatedTime");
      if (estimatedDate != null) {
        this.estimatedTime = Constants.DATE_FORMAT.format(estimatedDate);
      }
      Date releasedDate = itemRev.getDateProperty("date_released");
      if (releasedDate != null) {
        this.dateReleased = Constants.DATE_FORMAT.format(releasedDate);
      }
    }
  }
  
  private String getSplice(String sourceStr)
  {
    String department = sourceStr.substring(sourceStr.indexOf(")") + 1);
    String personName = sourceStr.substring(0, sourceStr.indexOf("("));
    
    return department + "/" + personName;
  }
}
