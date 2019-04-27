package com.dayun.report.massissuereport;

import com.dayun.report.utils.TcUtil;
import java.util.Vector;

public class QualityProblemStatistics
{
  public String responsibleDepartment = "";
  public int redCount = 0;
  public int yellowCount = 0;
  public int greenCount = 0;
  public int grayCount = 0;
  public int totalCount = 0;
  public String closingRate = "";
  
  public QualityProblemStatistics(String groupName, Vector<QualityProblem> problemVector)
  {
    this.responsibleDepartment = groupName;
    this.totalCount = problemVector.size();
    for (QualityProblem problem : problemVector)
    {
      String str;
      switch ((str = problem.probleamStatus).hashCode())
      {
      case 28784: 
        if (str.equals("»Ò")) {
        	 this.grayCount += 1;
        }
        break;
      case 32418: 
        if (str.equals("ºì")) {
        	this.redCount += 1;
        }
        break;
      case 32511: 
        if (str.equals("ÂÌ")) {
        	 this.greenCount += 1;
        }
        break;
      case 40644: 
        if (!str.equals("»Æ"))
        {
        	this.yellowCount += 1;
        }
        break;
      }
    }
    this.closingRate = TcUtil.calculatePercent(this.greenCount, this.totalCount);
  }
  
  public QualityProblemStatistics(String responsibleDepartment, int redCount, int yellowCount, int greenCount, int grayCount, int totalCount)
  {
    this.responsibleDepartment = responsibleDepartment;
    this.redCount = redCount;
    this.yellowCount = yellowCount;
    this.greenCount = greenCount;
    this.grayCount = grayCount;
    this.totalCount = totalCount;
    
    this.closingRate = TcUtil.calculatePercent(this.greenCount, totalCount);
  }
}
