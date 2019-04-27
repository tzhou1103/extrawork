package com.dayun.report.utils;

import com.dayun.report.editproblem.DatasetBean;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCUserService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

@SuppressWarnings("deprecation")
public class TcUtil
{
  public static TCSession getTcSession()
  {
    return (TCSession)AIFUtility.getCurrentApplication().getSession();
  }
  
  public static File getTemplateFile(String preferenceName, String directory)
    throws Exception
  {
    String templateID = getSitePreferenceStringValue(preferenceName);
    if ((templateID == null) || (templateID.isEmpty())) {
      throw new Exception("首选项 " + preferenceName + " 配置有误，请联系管理员 ！");
    }
    TCComponentItem templateItem = findItem(templateID);
    if (templateItem == null) {
      throw new Exception("无法找到ID为 " + templateID + " 的报表模板零组件，请联系管理员 ！");
    }
    TCComponentItemRevision templateItemRev = templateItem.getLatestItemRevision();
    TCComponent relatedComponent = getRelatedComponent(templateItemRev, "IMAN_specification", "MSExcelX");
    if (relatedComponent == null) {
      throw new Exception("模板零组件最新版本 " + templateItemRev + " 的规格关系下没有 MSExcelX 类型的报表模板数据集，请联系管理员 ！");
    }
    TCComponentDataset templateDataset = (TCComponentDataset)relatedComponent;
    File templateFile = getTemplateFile(templateDataset, directory, "excel");
    if ((templateFile == null) || (!templateFile.exists())) {
      throw new Exception("数据集 " + templateDataset + " 下没有报表模板文件，请联系管理员 ！");
    }
    return templateFile;
  }
  
  public static String getSitePreferenceStringValue(String preferenceName)
  {
    TCPreferenceService preferenceService = getTcSession().getPreferenceService();
    if (preferenceService.isDefinitionExistForPreference(preferenceName))
    {
      String preferenceValue = preferenceService.getStringValueAtLocation(preferenceName, TCPreferenceService.TCPreferenceLocation.OVERLAY_LOCATION);
      return preferenceValue;
    }
    return null;
  }
  
  public static String[] getSitePreferenceStringValueArray(String preferenceName)
  {
    TCPreferenceService preferenceService = getTcSession().getPreferenceService();
    if (preferenceService.isDefinitionExistForPreference(preferenceName))
    {
      String[] preferenceValues = preferenceService.getStringValuesAtLocation(preferenceName, TCPreferenceService.TCPreferenceLocation.OVERLAY_LOCATION);
      return preferenceValues;
    }
    return null;
  }
  
  public static TCComponentItem findItem(String itemId)
    throws TCException
  {
    TCComponentItemType itemType = (TCComponentItemType)getTcSession().getTypeComponent("Item");
    TCComponentItem[] items = itemType.findItems(itemId);
    if ((items != null) && (items.length > 0)) {
      return items[0];
    }
    return null;
  }
  
  public static TCComponentDataset getTemplateDataset(TCComponentItem paramItem, String relation, String type)
    throws TCException
  {
    TCComponentItemRevision itemRev = paramItem.getLatestItemRevision();
    TCComponent relatedComponent = getRelatedComponent(itemRev, relation, type);
    if ((relatedComponent instanceof TCComponentDataset)) {
      return (TCComponentDataset)relatedComponent;
    }
    return null;
  }
  
  public static TCComponent getRelatedComponent(TCComponentItemRevision paramItemRev, String relation, String type)
    throws TCException
  {
    TCComponent[] relatedComponents = paramItemRev.getRelatedComponents(relation);
    if ((relatedComponents != null) && (relatedComponents.length > 0)) {
      for (TCComponent tcComponent : relatedComponents)
      {
        String objectType = tcComponent.getType();
        if (objectType.equals(type)) {
          return tcComponent;
        }
      }
    }
    return null;
  }
  
  public static File getTemplateFile(TCComponentDataset dataset, String dirctory, String refType)
    throws TCException
  {
    File workingDir = new File(dirctory);
    if (!workingDir.exists()) {
      workingDir.mkdirs();
    }
    File[] files = dataset.getFiles(refType, dirctory);
    if ((files != null) && (files.length > 0)) {
      return files[0];
    }
    return null;
  }
  
  public static String getTempPath()
  {
    String tempPath = System.getProperty("java.io.tmpdir");
    if ((!tempPath.endsWith("/")) && (!tempPath.endsWith("\\"))) {
      tempPath = tempPath + File.separator;
    }
    return tempPath;
  }
  
  public static final String getSuffix(String fileName)
  {
    int index = fileName.lastIndexOf('.');
    if (index != -1) {
      return fileName.substring(index);
    }
    return "";
  }
  
  public static String getPrefix(String fileName)
  {
    int index = fileName.lastIndexOf('.');
    if (index != -1) {
      return fileName.substring(0, index);
    }
    return fileName;
  }
  
  public static File renameFile(File file, String newFileName)
  {
    if (!getPrefix(file.getName()).equals(newFileName))
    {
      File newFile = new File(file.getParent(), newFileName + getSuffix(file.getName()));
      if (newFile.exists()) {
        newFile.delete();
      }
      file.renameTo(newFile);
      return newFile;
    }
    return file;
  }
  
  public static void deleteFolder(String folderPath)
  {
    if ((folderPath == null) || (folderPath.isEmpty())) {
      return;
    }
    File folder = new File(folderPath);
    if ((folder.exists()) && (folder.isDirectory())) {
      if (folder.listFiles().length == 0)
      {
        folder.delete();
      }
      else
      {
        File[] subFiles = folder.listFiles();
        for (File subFile : subFiles) {
          if (subFile.isDirectory()) {
            deleteFolder(subFile.getAbsolutePath());
          } else {
            subFile.delete();
          }
        }
        folder.delete();
      }
    }
  }
  
  public static String getTimeStamp(String pattern)
  {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    return simpleDateFormat.format(new Date());
  }
  
  public static final TCComponentDataset createDataset(String name, String description, String type)
    throws TCException
  {
    TCComponentDataset dataset = null;
    TCComponentDatasetType datasetType = (TCComponentDatasetType)getTcSession().getTypeComponent(type);
    if (datasetType == null) {
      throw new TCException("无法获取名为" + type + "的数据集类型！");
    }
    dataset = datasetType.create(name, description, type);
    return dataset;
  }
  
  public static final void importFileToDataset(TCComponentDataset dataset, File file, String fileType, String refType)
    throws TCException
  {
    String[] as1 = { file.getPath() };
    String[] as2 = { fileType };
    String[] as3 = { "Plain" };
    String[] as4 = { refType };
    dataset.setFiles(as1, as2, as3, as4);
  }
  
  public static boolean isObjectInProcess(TCComponent component)
    throws TCException
  {
    TCComponent[] processStageList = component.getReferenceListProperty("process_stage_list");
    if ((processStageList != null) && (processStageList.length > 0)) {
      return true;
    }
    return false;
  }
  
  public static boolean isObjectReleased(TCComponent component)
    throws TCException
  {
    TCComponent[] releaseStatusList = component.getReferenceListProperty("release_status_list");
    if ((releaseStatusList != null) && (releaseStatusList.length > 0)) {
      return true;
    }
    return false;
  }
  
  public static String calculatePercent(int number1, int number2)
  {
    if ((number1 == 0) || (number2 == 0)) {
      return "/";
    }
    float result = number1 / number2;
    String formatValue = Constants.NUMBER_FORMAT.format(result);
    return formatValue;
  }
  
  public static boolean isSameDate(Date date_1, Date date_2)
  {
    if ((date_1 == null) || (date_2 == null)) {
      return false;
    }
    Calendar calendar_1 = Calendar.getInstance();
    calendar_1.setTime(date_1);
    Calendar calendar_2 = Calendar.getInstance();
    calendar_2.setTime(date_2);
    return (calendar_1.get(0) == calendar_2.get(0)) && (calendar_1.get(1) == calendar_2.get(1)) && (calendar_1.get(6) == calendar_2.get(6));
  }
  
  public static Vector<DatasetBean> getAllDatasetBeans(String preferenceName)
  {
    Vector<DatasetBean> vector = new Vector<DatasetBean>();
    
    String[] valueArray = getSitePreferenceStringValueArray(preferenceName);
    if ((valueArray != null) && (valueArray.length > 0)) {
      for (String value : valueArray)
      {
        String[] splitStrs = value.split(":");
        if (splitStrs.length == 3)
        {
          DatasetBean datasetBean = new DatasetBean(splitStrs[0], splitStrs[1], splitStrs[2]);
          vector.add(datasetBean);
        }
      }
    }
    return vector;
  }
  
  public static void setByPass(String flag)
    throws TCException
  {
    TCUserService userService = getTcSession().getUserService();
    userService.call("bypass", new String[] { flag });
  }
}
