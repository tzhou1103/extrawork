package com.dayun.report.problemreport;

import java.util.Vector;

import com.dayun.report.utils.Constants;
import com.dayun.report.utils.TcUtil;

public class ProblemStatistics 
{	
	public String modelName = "";
	public int problemTotalCount = 0;		
	public int onScheduleCount = 0;		// 按期完成
	public int expectedCount = 0;		// 预计完成
	public int deferredCount = 0;		// 延期中
	public int unexpiredCount = 0;		// 未到期
	
	public String onSchedulePercent = "";
	public String expectedPercent = "";
	public String totalPercent = "";
	
	public ProblemStatistics(String groupName, Vector<Problem> problemVector) 
	{
		this.modelName = groupName;
		this.problemTotalCount = problemVector.size();
		
		for (Problem problem : problemVector) 
		{
			switch (problem.status) 
			{
			case Constants.STATUS_ONSCHEDULE:
				this.onScheduleCount++;
				break;
			case Constants.STATUS_EXPECTED:
				this.expectedCount++;
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

	public ProblemStatistics(String modelName, int taskTotalCount,
			int onScheduleCount, int expectedCount, int deferredCount, int unexpiredCount) {
		this.modelName = modelName;
		this.problemTotalCount = taskTotalCount;
		this.onScheduleCount = onScheduleCount;
		this.expectedCount = expectedCount;
		this.deferredCount = deferredCount;
		this.unexpiredCount = unexpiredCount;
		
		calculatePercent();
	}
	
	private void calculatePercent()
	{
		int expireCount = this.onScheduleCount + this.expectedCount + this.deferredCount;
		
		this.onSchedulePercent = TcUtil.calculatePercent(this.onScheduleCount, expireCount);
		
		this.expectedPercent = TcUtil.calculatePercent(this.expectedCount, expireCount);
		
		int completedCount = this.onScheduleCount + this.expectedCount;
		
		this.totalPercent = TcUtil.calculatePercent(completedCount, expireCount);
	}
	
}
