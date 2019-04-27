package com.dayun.report.problemreport;

import java.util.Date;

import com.dayun.report.utils.Constants;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class Problem 
{
	public String problemName = "";
	public String problemDescription = "";
	public String sourcesWork = "";
	public String responsibleDepartment = "";
	public String responsiblePerson = "";
	public String proposedDepartments = "";
	public String introducer = "";
	public String dataPlan = "";
	public String estimatedTime = "";
	public String dateReleased = "";
	public String progressDescription = "";
	
	public String status = "";
	
	public Problem(TCComponentItem problemItem) throws TCException
	{
		this.problemName = problemItem.getProperty("object_name");
		TCComponent[] revision_list = problemItem.getReferenceListProperty("revision_list");
		if (revision_list != null && revision_list.length > 0) 
		{
			TCComponentItemRevision itemRev = problemItem.getLatestItemRevision();
			String[] propNames = { "e9_ProblemDescription", "e9_SourcesWork",
					"e9_ResponsibleDepartment", "e9_ResponsiblePerson",
					"e9_ProposedDepartments", "e9_Introducer",
					"e9_ProgressDescription" };
			String[] propValues = itemRev.getProperties(propNames);
			if (propValues != null && propValues.length == 7) 
			{
				this.problemDescription = propValues[0];
				this.sourcesWork = propValues[1];
				this.responsibleDepartment = propValues[2];
				this.responsiblePerson = propValues[3];
				this.proposedDepartments = propValues[4];
				this.introducer = propValues[5];
				this.progressDescription = propValues[6];
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
				if (planDate != null && releasedDate.before(planDate)) {
					this.status = Constants.STATUS_ONSCHEDULE;
				} else if (estimatedDate != null && releasedDate.before(estimatedDate)) {
					this.status = Constants.STATUS_EXPECTED;
				}
			} else {
				if (planDate != null && new Date().after(planDate)) {
					this.status = Constants.STATUS_DEFERRED;
				} else {
					this.status = Constants.STATUS_UNEXPIRED;
				}
			}
		}
		
	} 
	
}
