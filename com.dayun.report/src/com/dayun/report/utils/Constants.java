package com.dayun.report.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class Constants
{
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  public static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
  public static final String STATUS_ONSCHEDULE = "按期完成";
  public static final String STATUS_DELAYED = "延期完成";
  public static final String STATUS_DEFERRED = "延期中";
  public static final String STATUS_UNEXPIRED = "未到期";
  public static final String STATUS_SUSPEND = "暂停";
  
  static
  {
    NUMBER_FORMAT.setMaximumFractionDigits(2);
  }
  
  public static final String[] DELIVERY_TYPES = { "E9_TY_Doc", "E9_TY_ProDoc" };
  public static final String[] DELIVERY_REV_TYPES = { "E9_TY_DocRevision", "E9_TY_ProDocRevision" };
  public static final String PROJECT_PROBLEM_MGMT = "项目问题管理";
  public static final String STATUS_EXPECTED = "预计完成";
  public static final String[] PROBLEM_REV_TYPES = { "E9_TY_IssueRepRevision", "E9_TY_MassIssueRevision" };
  public static final String REL_PROSOLUPICTURE = "E9_REL_ProSolutionPicture";
  public static final String PROJECT_QUALITY_PROBLEM_MGMT = "项目质量问题管理";
  public static final String STATUS_COLOR_RED = "红";
  public static final String STATUS_COLOR_YELLOW = "黄";
  public static final String STATUS_COLOR_GREEN = "绿";
  public static final String STATUS_COLOR_GRAY = "灰";
}
