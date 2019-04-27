package com.dayun.report.taskreport;

import java.util.Date;

import com.dayun.report.utils.Constants;
import com.dayun.report.utils.TcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentScheduleTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

public class Task 
{	
	public String taskName = "";
	public String status = "";
	public String workSource = "";
	public String responsibleDepartment = "";
	public String responsiblePerson = "";
	public String startDate = "";
	public String actualStartDate = "";
	public String finishDate = "";
	public String actualFinishDate = "";
	public String progressDescription = "";
	
	public Task(TCComponentScheduleTask workTask) 
	{
		this(workTask, null);
	}
	
	public Task(TCComponentScheduleTask workTask, TCComponentScheduleTask masterPlan) 
	{
		try {
			this.taskName = workTask.getProperty("object_name");
			this.status = workTask.getTCProperty("fnd0status").getDisplayValue();
			if (this.status.equals("ÒÑ·ÅÆú")) {
				this.status = "ÔÝÍ£";
			}
			
			if (masterPlan != null) {
				this.workSource = masterPlan.getProperty("object_name");
			}
			
			TCComponent[] relatedComponents = workTask.getRelatedComponents("ResourceAssignment");
			if (relatedComponents != null && relatedComponents.length > 0) 
			{
				TCComponent tcComponent = relatedComponents[0];
				if (tcComponent instanceof TCComponentUser) 
				{
					TCComponentUser user = (TCComponentUser) tcComponent;
					this.responsiblePerson = user.toDisplayString();
					TCComponentGroup defaultGroup = (TCComponentGroup) user.getReferenceProperty("default_group");
					this.responsibleDepartment = defaultGroup.getGroupName();
				}
			}
			
			Date finishDate = workTask.getDateProperty("finish_date");
			Date actualFinishDate = workTask.getDateProperty("actual_finish_date");
			if (!this.status.equals("ÔÝÍ£")) 
			{
				if (actualFinishDate != null) {
					if (finishDate != null && (actualFinishDate.before(finishDate) || TcUtil.isSameDate(finishDate, actualFinishDate))) {
						this.status = Constants.STATUS_ONSCHEDULE;
					} else if (finishDate != null && actualFinishDate.after(finishDate)) {
						this.status = Constants.STATUS_DELAYED;
					}
				} else {
					if (finishDate != null && new Date().after(finishDate)) {
						this.status = Constants.STATUS_DEFERRED;
					} else {
						this.status = Constants.STATUS_UNEXPIRED;
					}
				}
			}
			
			Date startDate = workTask.getDateProperty("start_date");
			if (startDate != null) {
				this.startDate = Constants.DATE_FORMAT.format(startDate);
			}
			
			Date actualStartDate = workTask.getDateProperty("actual_start_date");
			if (actualStartDate != null) {
				this.actualStartDate = Constants.DATE_FORMAT.format(actualStartDate);
			}
			
			if (finishDate != null) {
				this.finishDate = Constants.DATE_FORMAT.format(finishDate);
			}
			if (actualFinishDate != null) {
				this.actualFinishDate = Constants.DATE_FORMAT.format(actualFinishDate);
			}
			
			this.progressDescription = workTask.getProperty("e9_ProgressDescription");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
}
