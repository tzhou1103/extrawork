package com.dayun.report.deliveryreport;

import java.util.Date;

import com.dayun.report.utils.Constants;
import com.dayun.report.utils.TcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentScheduleTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

public class Delivery 
{	
	public String deliveryName = "";
	public String status = "";
	public String workSource = "";
	public String responsibleDepartment = "";
	public String responsiblePerson = "";
	public String startDate = "";
	public String actualStartDate = "";
	public String finishDate = "";
	public String actualFinishDate = "";
	public String progressDescription = "";
	
	public Delivery(TCComponentItemRevision itemRev, TCComponentScheduleTask workTask) 
	{
		this(itemRev, workTask, null);
	}
	
	public Delivery(TCComponentItemRevision itemRev, TCComponentScheduleTask workTask, TCComponentScheduleTask masterPlan) 
	{
		try {
			this.deliveryName = itemRev.getProperty("object_name");
			this.status = workTask.getTCProperty("fnd0status").getDisplayValue();
			if (this.status.equals("已放弃")) {
				this.status = "暂停";
			}
			
			if (masterPlan != null) {
				this.workSource = masterPlan.getProperty("object_name");
			}
			
			TCComponent groupComponent = itemRev.getReferenceProperty("owning_group");
			TCComponentGroup owningGroup = (TCComponentGroup) groupComponent;
			this.responsibleDepartment = owningGroup.getGroupName();
			
			TCComponent userComponent = itemRev.getReferenceProperty("owning_user");
			TCComponentUser owningUser = (TCComponentUser) userComponent;
			this.responsiblePerson = owningUser.toDisplayString();
			
			Date releaseDate = null;
			if (TcUtil.isObjectReleased(itemRev)) {
				releaseDate = itemRev.getDateProperty("date_released");
			}
			Date finishDate = workTask.getDateProperty("finish_date");
			
			if (!this.status.equals("暂停")) 
			{			
				if (releaseDate != null) {
					if (finishDate != null && (releaseDate.before(finishDate) || TcUtil.isSameDate(finishDate, releaseDate))) {
						this.status = Constants.STATUS_ONSCHEDULE;
					} else if (finishDate != null && releaseDate.after(finishDate)) {
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
			// 修改实际完成时间为交付物发布时间，2019-03-07
//			Date actualFinishDate = workTask.getDateProperty("actual_finish_date");
//			if (actualFinishDate != null) {
			if (releaseDate != null) {
//				this.actualFinishDate = Constants.DATE_FORMAT.format(actualFinishDate);
				this.actualFinishDate = Constants.DATE_FORMAT.format(releaseDate);
			}
			
			this.progressDescription = itemRev.getProperty("e9_ProgressDescription");
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
}
