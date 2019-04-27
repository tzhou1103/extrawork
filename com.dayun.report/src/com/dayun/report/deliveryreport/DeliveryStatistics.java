package com.dayun.report.deliveryreport;

import java.util.Vector;

import com.dayun.report.utils.Constants;
import com.dayun.report.utils.TcUtil;

public class DeliveryStatistics 
{	
	public String modelName = "";
	public int taskTotalCount = 0;		
	public int onScheduleCount = 0;		// 按期完成
	public int delayedCount = 0;		// 延期完成
	public int deferredCount = 0;		// 延期中
	public int unexpiredCount = 0;		// 未到期
	
	public String onSchedulePercent = "";
	public String delayedPercent = "";
	public String totalPercent = "";
	
	public DeliveryStatistics(String groupName, Vector<Delivery> deliveryVector) 
	{
		this.modelName = groupName;
		this.taskTotalCount = deliveryVector.size();
		
		for (Delivery delivery : deliveryVector) 
		{
			switch (delivery.status) 
			{
			case Constants.STATUS_ONSCHEDULE:
				this.onScheduleCount++;
				break;
			case Constants.STATUS_DELAYED:
				this.delayedCount++;
				break;
			case Constants.STATUS_DEFERRED:
				this.deferredCount++;
				break;
			case Constants.STATUS_UNEXPIRED:
				this.unexpiredCount++;
				break;
			default:
				break;
			}
		}
		
		calculatePercent();
	}

	public DeliveryStatistics(String modelName, int taskTotalCount,
			int onScheduleCount, int delayedCount, int deferredCount, int unexpiredCount) {
		this.modelName = modelName;
		this.taskTotalCount = taskTotalCount;
		this.onScheduleCount = onScheduleCount;
		this.delayedCount = delayedCount;
		this.deferredCount = deferredCount;
		this.unexpiredCount = unexpiredCount;
		
		calculatePercent();
	}
	
	private void calculatePercent()
	{
		int expireCount = this.onScheduleCount + this.delayedCount + this.deferredCount;
		
		this.onSchedulePercent = TcUtil.calculatePercent(this.onScheduleCount, expireCount);
		
		this.delayedPercent = TcUtil.calculatePercent(this.delayedCount, expireCount);
		
		int completedCount = this.onScheduleCount + this.delayedCount;
		
		this.totalPercent = TcUtil.calculatePercent(completedCount, expireCount);
	}
	
}
