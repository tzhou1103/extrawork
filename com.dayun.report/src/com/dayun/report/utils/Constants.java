package com.dayun.report.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class Constants
{
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  public static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
  public static final String STATUS_ONSCHEDULE = "�������";
  public static final String STATUS_DELAYED = "�������";
  public static final String STATUS_DEFERRED = "������";
  public static final String STATUS_UNEXPIRED = "δ����";
  public static final String STATUS_SUSPEND = "��ͣ";
  
  static
  {
    NUMBER_FORMAT.setMaximumFractionDigits(2);
  }
  
  public static final String[] DELIVERY_TYPES = { "E9_TY_Doc", "E9_TY_ProDoc" };
  public static final String[] DELIVERY_REV_TYPES = { "E9_TY_DocRevision", "E9_TY_ProDocRevision" };
  public static final String PROJECT_PROBLEM_MGMT = "��Ŀ�������";
  public static final String STATUS_EXPECTED = "Ԥ�����";
  public static final String[] PROBLEM_REV_TYPES = { "E9_TY_IssueRepRevision", "E9_TY_MassIssueRevision" };
  public static final String REL_PROSOLUPICTURE = "E9_REL_ProSolutionPicture";
  public static final String PROJECT_QUALITY_PROBLEM_MGMT = "��Ŀ�����������";
  public static final String STATUS_COLOR_RED = "��";
  public static final String STATUS_COLOR_YELLOW = "��";
  public static final String STATUS_COLOR_GREEN = "��";
  public static final String STATUS_COLOR_GRAY = "��";
}
