package com.sokon.bopreport.customization.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import com.jacob.com.Dispatch;
import com.sokon.bopreport.customization.messages.ReportMessages;
import com.sokon.bopreport.customization.datamodels.TargetBOP;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPWorkarea;
import com.teamcenter.rac.commands.open.OpenCommand;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCClassificationService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentBOPWindowType;
import com.teamcenter.rac.kernel.TCComponentCCObject;
import com.teamcenter.rac.kernel.TCComponentCfg0ConfiguratorPerspective;
import com.teamcenter.rac.kernel.TCComponentCfg0ProductItem;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentStructureContext;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;

public class TcUtil
{	
	/**
	 * @return 获取当前会话
	 */
	public static TCSession getTcSession()
	{
		return (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	/**
	 * 获取站点单值首选项
	 * 
	 * @param prefName
	 * @return
	 */
	public static final String getPrefStringValue(String prefName)
	{
		TCPreferenceService preferenceService = getTcSession().getPreferenceService();
		if (preferenceService.isDefinitionExistForPreference(prefName)) {
			String prefValue = preferenceService.getStringValueAtLocation(prefName, TCPreferenceLocation.OVERLAY_LOCATION);
			return prefValue;
		}
		return null;
	}
	
	/**
	 * 获取站点多值首选项
	 * 
	 * @param prefName
	 * @return
	 */
	public static final String[] getPrefStringValues(String prefName)
	{
		TCPreferenceService preferenceService = getTcSession().getPreferenceService();
		if (preferenceService.isDefinitionExistForPreference(prefName)) {
			String[] prefValues = preferenceService.getStringValuesAtLocation(prefName, TCPreferenceLocation.OVERLAY_LOCATION);
			return prefValues;
		}
		return null;
	}
	
	/**
	 * 获取指定版本对象下特定类型,含有特定属性的对象
	 * 
	 * @param revision
	 * @param relation
	 * @param type
	 * @param documentType
	 * @throws TCException
	 */
	public static TCComponentItemRevision getRelatedDocumentRevision(TCComponentItemRevision revision, String relation, String type, String documentType) throws TCException 
	{
		TCComponent[] relatedComponents = revision.getRelatedComponents(relation);
		if (relatedComponents != null && relatedComponents.length > 0) 
		{
			for (TCComponent relatedComponent : relatedComponents)
			{
				if (relatedComponent instanceof TCComponentItem) 
				{
					TCComponentItem item = (TCComponentItem) relatedComponent;
					if (item.getType().equals(type)) 
					{
						TCComponentItemRevision latestRevision = item.getLatestItemRevision();
//						String at_DocumentType = latestRevision.getProperty("s4_AT_DocumentType");
						String at_DocumentType = latestRevision.getStringProperty("s4_AT_DocumentType");
						if (at_DocumentType.equals(documentType)) {
							return latestRevision;
						}
					}
				} 
			}
		}
		
		return null;
	}
	
	/**
	 * 获取指定版本对象下的指定类型的数据集
	 * 
	 * @param revision
	 * @param relation
	 * @param type
	 * @return
	 * @throws TCException
	 */
	public static TCComponentDataset getRelatedDataset(TCComponentItemRevision revision, String relation, String type) throws TCException 
	{
		TCComponent[] relatedComponents = revision.getRelatedComponents(relation);
		if (relatedComponents != null && relatedComponents.length > 0) 
		{
			for (TCComponent relatedComponent : relatedComponents)
			{
				if (relatedComponent instanceof TCComponentDataset) 
				{
					TCComponentDataset dataset = (TCComponentDataset) relatedComponent;
					if (dataset.getType().equals(type)) {
						return dataset;
					}
				} 
			}
		}
		
		return null;
	}
	
	/**
	 * 判断对象是否发布
	 * 
	 * @param component
	 * @return
	 * @throws TCException
	 */
	public static boolean isComponentReleased(TCComponent component) throws TCException
	{
		TCComponent[] releaseStatusList = component.getReferenceListProperty("release_status_list");
		if (releaseStatusList != null && releaseStatusList.length > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取模板数据集
	 * 
	 * @param dsName 数据集名称
	 * @param dsType 数据集类型
	 * @return
	 * @throws TCException
	 */
	public static final TCComponentDataset findTemplateDataset(String dsName, String dsType) throws TCException 
	{
		TCComponentDatasetType datasetType = (TCComponentDatasetType) getTcSession().getTypeComponent("Dataset");
		TCComponentDataset dataset = datasetType.find(dsName);
		if (dataset != null && dataset.getType().equals(dsType)) {
			return dataset;
		}
		return null;
	}
	
	/**
	 * 下载模板数据集文件到本地Temp下的新建文件夹下
	 * 
	 * @param dataset
	 * @return
	 * @throws TCException
	 */
	public static File getTemplateFile(TCComponentDataset dataset) throws TCException
	{
		String workingDirPath = System.getenv("Temp") + File.separator + System.currentTimeMillis();
		File workingDir = new File(workingDirPath);
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}
		File[] files = dataset.getFiles("excel", workingDirPath);
		if (files != null && files.length > 0) {
			return files[0];
		}
		return null;
	}
	
	/**
	 * 删除文件夹及其下所有文件
	 * 
	 * @param folderPath
	 */
	public static void deleteFolder(String folderPath) 
	{
		File folder = new File(folderPath);

		if ((folder.exists()) && (folder.isDirectory())) 
		{
			if (folder.listFiles().length == 0) {
				folder.delete();
			} else {
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
	
	/**
	 * 根据对象类型获取专业
	 * 
	 * @param type
	 * @return
	 */
	public static String getProfession(String type) 
	{
		String[] professions = TcUtil.getPrefStringValues("S4CUST_ProcessReport_Profession");
		if (professions != null && professions.length > 0)
		{
			for (String profession : professions) 
			{
				if (profession.startsWith(type)) {
					return profession.substring(profession.indexOf("=") + 1);
				}
			}
		}
		return "";
	}
	
	/**
	 * @return 获取当前日期，输出格式2019/09/11
	 */
	public static String getCurrentDate()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		return simpleDateFormat.format(new Date());
	}
	
	/**
	 * 创建Item对象
	 * 
	 * @param type
	 * @param name
	 * @param desc
	 * @return
	 * @throws TCException
	 */
	public static TCComponentItem createItem(String type, String name, String desc) throws TCException
	{
		TCComponentItemType itemType = (TCComponentItemType) getTcSession().getTypeComponent(type);
		TCComponentItem item = itemType.create(itemType.getNewID(), itemType.getNewRev(null), type, name, desc, null);
		return item;
	}
	
	/**
	 * 创建或更新报表数据集
	 * 
	 * @param targetBOP
	 * @param reportFile
	 * @param objectName
	 * @return
	 * @throws TCException 
	 */
	public static TCComponentDataset createOrUpdateReportDataset(TargetBOP targetBOP, File reportFile, String objectName) throws TCException
	{
		String[] filePathNames = { reportFile.getAbsolutePath() };
		String[] namedRefs = { "excel" };
		
		if (targetBOP.documentRevision != null) 
		{
			String newRevId = targetBOP.documentRevision.getItem().getNewRev();
			if (isComponentReleased(targetBOP.documentRevision)) {
				targetBOP.documentRevision = targetBOP.documentRevision.saveAs(newRevId);
			}
			
			TCComponentDataset dataset = getRelatedDataset(targetBOP.documentRevision, "IMAN_specification", "MSExcelX");
			if (dataset != null) {
				dataset.removeNamedReference("excel");
			} else {
				dataset = createDataset(objectName, "", "MSExcelX");
			}
			
			dataset.setFiles(filePathNames, namedRefs);
			dataset.refresh();
			
			return dataset;
		} else {
			TCComponentItem documentItem = TcUtil.createItem("S4_IT_ProcessDoc", objectName, "");
			if (documentItem != null) 
			{
				targetBOP.itemRevision.add("IMAN_reference", documentItem);
				targetBOP.itemRevision.refresh();
				
				TCComponentItemRevision documentRevision = documentItem.getLatestItemRevision();
				targetBOP.documentRevision = documentRevision;
				
				TCComponentDataset dataset = createDataset(objectName, "", "MSExcelX");
				dataset.setFiles(filePathNames, namedRefs);
				documentRevision.add("IMAN_specification", dataset);
				
				return dataset;
			}
		}
		
		return null;
	}
	
	/**
	 * 打开工艺报表
	 * 
	 * @param shell
	 * @param reportDataset
	 */
	public static void openReportDataset(final Shell shell, final TCComponentDataset reportDataset)
	{
		shell.getDisplay().asyncExec(new Runnable() 
		{								
			@Override
			public void run() 
			{
				MessageDialog.openInformation(shell, ReportMessages.getString("hint.Title"), ReportMessages.getString("generateProcessReportSucceed.Msg"));
			
				OpenCommand openCommand = new OpenCommand(AIFDesktop.getActiveDesktop(), reportDataset);
				try {
					openCommand.executeModal();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 设置Shell居中
	 * @param paramShell
	 */
	public static void centerShell(Shell paramShell) 
	{
		if (paramShell == null) {
			return;
		}
		Display display = paramShell.getDisplay();
		Rectangle rectangle = display.getClientArea();
		Monitor[] arrayOfMonitor = display.getMonitors();
		Monitor primaryMonitor = display.getPrimaryMonitor();
		if ((primaryMonitor != null) && (arrayOfMonitor != null)
				&& (arrayOfMonitor.length > 1)) {
			rectangle = primaryMonitor.getClientArea();
		}
		Point point = paramShell.getSize();
		paramShell.setLocation((rectangle.width - point.x) / 2, (rectangle.height - point.y) / 2);
	}
	
	/**
	 * 获取语言选项
	 * 
	 * @param shell
	 * @return
	 */
	public static int getLanguageSelction(Shell shell)
	{
		SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(shell);
		if (selectLanguageDialog.open() == Dialog.OK) {
			return selectLanguageDialog.getLanguageSelection();
		}
		return -1;
	}
	
	/**
	 * 获取用法数量
	 * 
	 * @param bopLine
	 * @return
	 * @throws TCException
	 */
	public static double getUsageQuantity(TCComponentBOPLine bopLine) throws TCException
	{
		String usage_Quantity = bopLine.getProperty("Usage_Quantity");
		if (!usage_Quantity.equals("")) {
			return Double.parseDouble(usage_Quantity);
		} else {
			return 1;
		}
	}
	
	/**
	 * 根据工序获取工位ID
	 * 
	 * @param opBopLine
	 * @return
	 * @throws TCException
	 */
	public static String getWorkAreaId(TCComponentBOPLine opBopLine) throws TCException
	{
		TCComponentMfgBvrBOPWorkarea workarea = null;
		
		// 当顶层为冲压BOP时，获取工序下的工位
		TCComponentItemRevision bopRevision = opBopLine.window().getTopBOMLine().getItemRevision();
		if (bopRevision.getType().equals("S4_IT_StampBOPRevision")) 
		{
			TCComponent[] relatedComponents = opBopLine.getRelatedComponents("Mfg0assigned_workarea");
			if (relatedComponents != null && relatedComponents.length > 0) {
				workarea = (TCComponentMfgBvrBOPWorkarea) relatedComponents[0];
			}
		} else {
			// 其他BOP类型，工位和工序同级别
			TCComponent[] relatedComponents = opBopLine.parent().getRelatedComponents("Mfg0assigned_workarea");
			if (relatedComponents != null && relatedComponents.length > 0) {
				workarea = (TCComponentMfgBvrBOPWorkarea) relatedComponents[0];
			}
		}
		
		if (workarea != null) {
			return workarea.getProperty("bl_child_id");
		}
		return "";
	}
	
	/**
	 * 获取工位地址
	 * <p> 用于工具清单
	 * 
	 * @param opBopLine
	 * @return
	 * @throws TCException
	 */
	public static String getStationAddress(TCComponentBOPLine opBopLine) throws TCException
	{
		String workAreaId = getWorkAreaId(opBopLine);
		
		String area = "0";
		TCComponentItemRevision bopRevision = opBopLine.window().getTopBOMLine().getItemRevision();
		if (bopRevision.isTypeOf("S4_IT_PaintBOPRevision")) {
//			String meStationArea = opBopLine.parent().getStringProperty("s4_BAT_MEStationArea");
			String meStationArea = opBopLine.parent().getItemRevision().getStringProperty("s4_AT_MEStationArea");
			area = meStationArea.equals("") ? "0" : meStationArea;
		} else {
			TCComponent relatedComponent = opBopLine.getRelatedComponent("Mfg0processResource");
			if (relatedComponent != null && relatedComponent.getStringProperty("bl_item_object_type").equals("S4_IT_Worker")) 
			{
				String procResArea = relatedComponent.getStringProperty("s4_BAT_ProcResArea");
				area = procResArea.equals("") ? "0" : procResArea;
			}
		}
		
		return workAreaId + area;
	}
	
	/**
	 * 获取拼接后的备注
	 * 
	 * @param remark
	 * @param bopLine
	 * @return
	 * @throws TCException
	 */
	public static String getAppendRemark(String remark, TCComponentBOPLine bopLine) throws TCException
	{
		String remarks = bopLine.getProperty("S4_NT_Remarks");
		if (!remarks.equals(""))
		{
			if (remark.equals("")) 
				return remarks;
			else 
				return remark + "," + remarks;
		}
		return remark;
	}
	
	/**
	 * 获取工位地址
	 * <p> 用于设备、焊钳清单
	 * 
	 * @param stationBopLine
	 * @return
	 * @throws TCException
	 */
	/*public static String getLcationAddress(TCComponentBOPLine stationBopLine) throws TCException
	{
		String stationId = stationBopLine.getStringProperty("bl_item_item_id");
		String meStationArea = stationBopLine.getStringProperty("s4_BAT_MEStationArea");
		String stationArea = meStationArea.equals("") ? "0" : meStationArea;
		String lcationAddress = stationId + stationArea;
		return lcationAddress;
	}*/
	
	/**
	 * 根据ID和版本号查找版本对象
	 * 
	 * @param itemId
	 * @param revId
	 * @return
	 * @throws TCException
	 */
	public static TCComponentItemRevision findItemRevision(String itemId, String revId) throws TCException
	{
		TCComponentItemRevisionType revisionType = (TCComponentItemRevisionType) getTcSession().getTypeComponent("ItemRevision");
		TCComponentItemRevision[] itemRevisions = revisionType.findRevisions(itemId, revId);
		if (itemRevisions != null && itemRevisions.length > 0) {
			return itemRevisions[0];
		}
		
		return null;
	}
	
	/**
	 * 获取CC包下指定类型的BOP版本
	 * 
	 * @param ccObject
	 * @param bopTypes
	 * @return
	 * @throws TCException
	 */
	public static Vector<TCComponentItemRevision> getBopRevisionsByCCObject(TCComponentCCObject ccObject, String[] bopTypes) throws TCException
	{
		Vector<TCComponentItemRevision> bopRevisionVector = new Vector<TCComponentItemRevision>();
		
		TCComponent[] ccContexts = ccObject.getRelatedComponents("IMAN_CCContext");
		if (ccContexts != null && ccContexts.length > 0)
		{
			for (TCComponent ccContext : ccContexts) 
			{
				if (ccContext.isTypeOf("MEProcessContext")) 
				{
					TCComponentStructureContext structureContext = (TCComponentStructureContext) ccContext;
					TCComponent[] relatedComponents = structureContext.getRelatedComponents("contents");
					if (relatedComponents != null && relatedComponents.length > 0) 
					{
						for (TCComponent relatedComponent : relatedComponents) 
						{
							if (relatedComponent.isTypeOf("BOMView Revision")) 
							{
								TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) relatedComponent;
								String bomViewName = bomViewRevision.getProperty("object_name");
								String itemId = bomViewName.substring(0, bomViewName.indexOf("/"));
								String revId = bomViewName.substring(bomViewName.indexOf("/") + 1, bomViewName.lastIndexOf("-"));
								TCComponentItemRevision itemRevision = findItemRevision(itemId, revId);
								if (itemRevision != null && itemRevision.isTypeOf(bopTypes)) {
									bopRevisionVector.add(itemRevision);
								}
							}
						}
					}
				}
			}
		}
		
		return bopRevisionVector;
	}
	
	/**
	 * 构建BOPWindow
	 * 
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOPWindow createBopWindow() throws TCException
	{
		TCComponentBOPWindowType bopWindowType = (TCComponentBOPWindowType) getTcSession().getTypeComponent("BOPWindow");
		TCComponentBOPWindow bopWindow = bopWindowType.createBOPWindow(null);
		return bopWindow;
	}
	
	/**
	 * 根据版本对象构建BOPWindow的顶层行
	 * 
	 * @param bopRevision
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOPLine getTopBopLine(TCComponentItemRevision bopRevision) throws TCException
	{
		TCComponentBOPWindow bopWindow = createBopWindow();
		TCComponentBOPLine topBopLine = (TCComponentBOPLine) bopWindow.setWindowTopLine(bopRevision.getItem(), bopRevision, null, null);
		return topBopLine;
	}
	
	/**
	 * 构建BOMWindow
	 * 
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOMWindow createBomWindow() throws TCException
	{
		TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) getTcSession().getTypeComponent("BOMWindow");
		TCComponentBOMWindow bomWindow = bomWindowType.create(null);
		return bomWindow;
	}
	
	/**
	 * 根据版本对象构建BOPWindow的顶层行
	 * 
	 * @param bopRevision
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOMLine getTopBomLine(TCComponentItemRevision itemRevision) throws TCException
	{
		TCComponentBOMWindow bomWindow = createBomWindow();
		TCComponentBOMLine topBomLine = bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, null);
		return topBomLine;
	}
	
	/**
	 * 创建或更新报表数据集
	 * 
	 * @param targetBOP
	 * @param reportFile
	 * @param objectName
	 * @param documentType
	 * @return
	 * @throws TCException 
	 */
	public static TCComponentDataset createOrUpdateReportDataset(TCComponentItemRevision processDocRevision, File reportFile, String objectName, String documentType) throws TCException
	{
		String[] filePathNames = { reportFile.getAbsolutePath() };
		String[] namedRefs = { "excel" };
		
		if (processDocRevision != null) 
		{
			if (TcUtil.isComponentReleased(processDocRevision)) {
				processDocRevision = processDocRevision.saveAs(processDocRevision.getItem().getNewRev());
			}
			
			TCComponentDataset dataset = getRelatedDataset(processDocRevision, "IMAN_specification", "MSExcelX");
			if (dataset != null) {
				dataset.removeNamedReference("excel");
			} else {
				dataset = createDataset(objectName, "", "MSExcelX");
			}
			
			dataset.setFiles(filePathNames, namedRefs);
			dataset.refresh();
			
			return dataset;
		} else {
			TCComponentItem documentItem = createItem("S4_IT_ProcessDoc", objectName, "");
			if (documentItem != null) 
			{
				TCComponentFolder newStuffFolder = getTcSession().getUser().getNewStuffFolder();
				newStuffFolder.add("contents", documentItem);
				newStuffFolder.refresh();
				
				TCComponentItemRevision documentRevision = documentItem.getLatestItemRevision();
				TCComponentDataset dataset = createDataset(objectName, "", "MSExcelX");
				dataset.setFiles(filePathNames, namedRefs);
				documentRevision.add("IMAN_specification", dataset);
				documentRevision.refresh();
				documentRevision.setProperty("s4_AT_DocumentType", documentType);
				return dataset;
			}
		}
		
		return null;
	}
	
	/**
	 * 根据语言选项获取值
	 * <p> 英文值在上，中文值在下
	 * 
	 * @param languageSelection
	 * @param value
	 * @param cnValue
	 * @return
	 */
	public static String getValueByLanguageSelection(int languageSelection, String value, String cnValue)
	{
		if (languageSelection == SelectLanguageDialog.SLECTION_EN_US) {
			return value;
		} else if (languageSelection == SelectLanguageDialog.SLECTION_CH_ZN) {
			return cnValue;
		} else if (languageSelection == SelectLanguageDialog.SLECTION_BOTH) {
			if (!cnValue.equals("") && !value.equals(cnValue)) {
				return value + "\n" + cnValue;
			}
			return value;
		}
		return "";
	}
	
	/**
	 * 获取配置
	 * 
	 * @param bopWindow
	 * @return
	 * @throws TCException 
	 */
	public static String getConfiguration(TargetBOP targetBOP) throws TCException
	{
		if (targetBOP.variantModel != null) {
			String desc = targetBOP.variantModel.getStringProperty("object_desc");
			String cnDesc = targetBOP.variantModel.getProperty("object_desc");
			return getValueByLanguageSelection(targetBOP.languageSelection, desc, cnDesc);
		}
		
		return "";
	}
	
	/**
	 * 获取当前BOP的目标MBOM窗口
	 * 
	 * @return
	 * @throws TCException 
	 */
	public static TCComponentBOMWindow getMBOMWindow(TCComponentBOPLine targetBopLine) throws TCException
	{
		TCComponentBOMWindow bomWindow = null;
		
		MFGLegacyApplication mfgLegacyApplication = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
		AbstractViewableTreeTable[] viewableTreeTables = mfgLegacyApplication.getViewableTreeTables();
		if (viewableTreeTables != null && viewableTreeTables.length > 0) 
		{
			for (AbstractViewableTreeTable abstractViewableTreeTable : viewableTreeTables) 
			{
				TCComponentBOMLine topBomLine = abstractViewableTreeTable.getBOMRoot();
				TCComponent relatedComponent = targetBopLine.getItemRevision().getRelatedComponent("IMAN_METarget");
				TCComponentItemRevision itemRevision = topBomLine.getItemRevision();
				if (topBomLine != null && itemRevision.isTypeOf("S4_IT_MBOMTopRevision") && relatedComponent == itemRevision) {
					bomWindow = topBomLine.window();
					break;
				}
			}
		}
		
		return bomWindow;
	}
	
	/**
	 * 获取模型族
	 * 
	 * @param mbomWinbow
	 * @return
	 * @throws TCException
	 */
	public static Vector<TCComponent> getVariantModels(TCComponentBOMWindow mbomWinbow) throws TCException
	{
		Vector<TCComponent> variantModels = new Vector<TCComponent>();
		
		TCComponent configuratorContext = mbomWinbow.getCurrentConfiguratorContext();
		if (configuratorContext != null && configuratorContext instanceof TCComponentCfg0ProductItem) 
		{
			TCComponentCfg0ProductItem productItem = (TCComponentCfg0ProductItem) configuratorContext;
			TCComponentCfg0ConfiguratorPerspective configuratorPerspective = getConfiguratorPerspective(productItem);
			if (configuratorPerspective != null) 
			{
				TCComponent[] models = configuratorPerspective.getReferenceListProperty("cfg0Models");
				for (TCComponent model : models) {
					variantModels.add(model);
				}
			}
		}
		
		return variantModels;
	}
	
	/**
	 * 获取配置器透视图
	 * 
	 * @param item
	 * @return
	 * @throws TCException
	 */
	public static final TCComponentCfg0ConfiguratorPerspective getConfiguratorPerspective(TCComponentCfg0ProductItem cfg0ProductItem) throws TCException
	{
		TCComponent configPerspective = cfg0ProductItem.getReferenceProperty("cfg0ConfigPerspective");
		if (configPerspective instanceof TCComponentCfg0ConfiguratorPerspective) 
		{
			return (TCComponentCfg0ConfiguratorPerspective) configPerspective;
		}
		return null;
	}
	
	/**
	 * 获取数量
	 * 
	 * @param bopLine
	 * @return
	 * @throws TCException
	 */
	public static int getQuantity(TCComponentBOPLine bopLine) throws TCException
	{
		String usage_Quantity = bopLine.getProperty("bl_quantity");
		if (!usage_Quantity.equals("")) {
			return Integer.valueOf(usage_Quantity);
		} else {
			return 1;
		}
	}
	
	/**
	 * 获取工序下关联的S4_IT_WorkerRevision对象的s4_AT_ProcResArea真实值
	 * 
	 * @param meopBOMLine
	 * @return
	 * @throws TCException 
	 */
	public static String getProcResArea(TCComponentBOMLine opBOMLine) throws TCException
	{
		String procResArea = "";
		TCComponent relatedComponent = opBOMLine.getRelatedComponent("Mfg0processResource");
		if (relatedComponent != null) 
		{
			TCComponentBOMLine childBOMLine = (TCComponentBOMLine) relatedComponent;
			TCComponentItemRevision itemRevision = childBOMLine.getItemRevision();
			if (itemRevision.isTypeOf("S4_IT_WorkerRevision")) {
				procResArea = itemRevision.getStringProperty("s4_AT_ProcResArea");
			}
		}
		return procResArea.isEmpty() ? "0" : procResArea;
	}
	
	
	/**
	 * 截取字符串的后7位
	 * 
	 * @param srcString
	 * @return
	 */
	public static String getLast7String(String srcString)
	{
		int srcLength = srcString.length();
		if (srcLength > 7) {
			return srcString.substring(srcLength - 7);
		}
		
		return srcString;
	}
	
	/**
	 * 文件重命名
	 * 
	 * @param file
	 * @param newFileName 新的文件名称，不带后缀
	 * @return
	 */
	public static File renameFile(File file, String newFileName)
	{
		if (!getPrefix(file.getName()).equals(newFileName)) 
		{
			File newFile  = new File(file.getParent(), newFileName + getSuffix(file.getName()));
			if (!newFile.exists()) {
				file.renameTo(newFile);
			}
			return newFile;
		}
		
		return file;
	}
	
	/**
	 * 获取分类属性值
	 * 
	 * @param itemRev
	 * @param attributeName
	 * @return
	 * @throws TCException
	 */
	public static String getClassificationAttributeValue(TCComponentItemRevision itemRev, String attributeName) throws TCException
	{
		String value = "";
		TCClassificationService classificationService = itemRev.getSession().getClassificationService();
		if (classificationService.isObjectClassified(itemRev)) 
		{
			Map<String, String> classificationAttributes = itemRev.getClassificationAttributes();
			if (classificationAttributes != null && classificationAttributes.size() >0) {
				value = classificationAttributes.get(attributeName);
			}
		}
		
		return value == null ? "" : value;
	}
	
	
	// ******************* 分界线，main方法以上作者zhoutong，main方法以下作者chenyanhua *******************
	public static void main(String[] args) {
		SelectLanguageDialog dialog = new SelectLanguageDialog(new Shell());
		dialog.open();
	}
	
	public static TCComponent getRelatedComponentByType(TCComponent parentComponent, String relationType, String compType)
	{
		if (parentComponent == null)
			return null;
		
		try 
		{
			TCComponent[] atccomponent = parentComponent.getRelatedComponents(relationType);
			for (int i = 0; i < atccomponent.length; i++)
			{
				if (atccomponent[i].getType().equals(compType))
				{
					return atccomponent[i];
				}
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static TCComponent getRelatedComponentByTypeName(TCComponent parentComponent, String relationType, String compType, String name)
	{
		if (parentComponent == null)
			return null;
		
		try 
		{
			TCComponent[] atccomponent = parentComponent.getRelatedComponents(relationType);
			for (int i = 0; i < atccomponent.length; i++)
			{
				if (atccomponent[i].isTypeOf(compType) && atccomponent[i].getStringProperty("object_name").equals(name))
				{
					return atccomponent[i];
				}
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static final TCComponent[] queryComponents(TCSession session, String queryName, String[] keys,
			String[] values) throws TCException {
		TCComponent[] results = new TCComponent[0];
		TCComponentQueryType querytype = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
		TCComponentQuery query = (TCComponentQuery) querytype.find(queryName);
		querytype.clearCache();
		if (query == null)
			throw new RuntimeException("未定义的查询[" + queryName + "]");
		
		TCTextService textService = session.getTextService();
		String[] keytexts = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			keytexts[i] = textService.getTextValue(keys[i]);
		}
		String[] valuetexts = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			valuetexts[i] = textService.getTextValue(values[i]);
		}
		results = query.execute(keytexts, valuetexts);
		query.clearCache();
		return results;
	}
	
	public static final TCComponentItem findItem(String itemId)
	{
		try 
		{
			TCComponentItemType itemType = (TCComponentItemType)getTcSession().getTypeComponent("Item");
//			return itemType.find(itemId);
			TCComponentItem[] items = itemType.findItems(itemId);
			if (items != null && items.length > 0) {
				return items[0];
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeData(Dispatch sheet, String cellName, String cellValue) throws Exception
	{
		System.out.println("Cell Name:" + cellName);
		Dispatch cell = Dispatch.invoke(sheet,
				"Range", Dispatch.Get, new Object[]{cellName}, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		Dispatch.put(cell, "Value", cellValue);
	}
	
	public static final Dispatch getShapeDispatch(Dispatch sheet, String shapeName)
	{
		Dispatch shapesDispatch = Dispatch.get(sheet, "Shapes").getDispatch();
		
		int count = Dispatch.get(shapesDispatch, "Count").getInt();
		
		for (int i = 0; i < count; i++) 
		{
			Dispatch rangeDispatch = Dispatch.invoke(shapesDispatch, "Range", Dispatch.Get, new Object[]{i + 1},new int[1]).toDispatch();
			String str = Dispatch.get(rangeDispatch, "name").getString();
			if (str.equals(shapeName))
			{
				return rangeDispatch;
			}
		}
		return null;
	}
	
	public static void createSheet(Dispatch sheets,int pageNum)
	{
		
		Dispatch firstSheet = Dispatch.invoke(sheets,
				"Item",
				Dispatch.Get,
				new Object[]{1},
				new int[1]).toDispatch();
		for(int i = 1; i < pageNum; i++)
		{
			Dispatch sheet = Dispatch.invoke(sheets,
					"Item",
					Dispatch.Get,
					new Object[]{i + 1},
					new int[1]).toDispatch();
			Dispatch.call(firstSheet,"Copy",sheet);
		}
	}
	
//	public static final void insertPicture(Dispatch sheet, String cellName, String imagePath)
//	{
//		Dispatch d = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{cellName}, new int[1]).toDispatch();
//		Dispatch.call(d, "Select"); //在工作表中，定位需要插入图片的具体位置
//		Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
//		Dispatch pic = Dispatch.call(select, "Insert", imagePath).toDispatch();
//	}
	
	public static final void insertPicture(Dispatch sheet, String cellName, String imagePath) throws Exception
	{ 
		Dispatch cell = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[] { cellName }, new int[1]).toDispatch();
		if (cell == null)
			throw new Exception("模板中未找到名称为[" + cellName + "]的单元格，请联系管理员！");
		
		Dispatch.call(cell, "Select"); //在工作表中，定位需要插入图片的具体位置
		Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
		Dispatch.call(select, "Insert", imagePath).toDispatch();
	}
	
//	public static final void insertPicture(Dispatch sheet, String cellName, String imagePath, boolean isOffice2007)
//	{
//		Dispatch cell = getCell(sheet, cellName);
//		double d1 = Double.parseDouble(Dispatch.get(cell, "Width").toString());
//		double d2 = Double.parseDouble(Dispatch.get(cell, "Height").toString());
////		
////		
////		System.out.println("info:" + Dispatch.get(cell, "Height").toString());
//		
//		if (isOffice2007)
//		{
//			Dispatch d = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{cellName}, new int[1]).toDispatch();
//			Dispatch.call(d, "Select"); //在工作表中，定位需要插入图片的具体位置
//			Dispatch select = Dispatch.call(sheet, "Shapes").toDispatch();
//			Variant left = Dispatch.get(d, "Left");
//			Variant top = Dispatch.get(d, "Top");
//			Dispatch pic = Dispatch.call(select, "AddPicture", new Object[] {imagePath, false, true, left, top, -1, -1}).toDispatch();
//			
//			double d3 = Double.parseDouble(Dispatch.get(pic, "Width")
//					.toString());
//			double d4 = Double.parseDouble(Dispatch.get(pic, "Height")
//					.toString());
//			double d5 = d1 > d2 ? d1 : d2;
//			double d6 = d3 > d4 ? d3 : d4;
//			double d7;
//			if (d6 < d5)
//				d7 = 1.0D;
//			else
//				d7 = d5 / d6;
//			
//			if (d7 != 1.0D)
//			{
//				int i = (int)(d3 * d7);
//		        int j = (int)(d4 * d7);
//		        
//		        Dispatch.put(pic, "Width", i);
//		        Dispatch.put(pic, "Height", j);
//			}
//			
//		}else
//		{
//			Dispatch d = Dispatch.invoke(sheet, "Range", Dispatch.Get, new Object[]{cellName}, new int[1]).toDispatch();
//			Dispatch.call(d, "Select"); //在工作表中，定位需要插入图片的具体位置
//			Dispatch select = Dispatch.call(sheet, "Pictures").toDispatch();
//			Dispatch pic = Dispatch.call(select, "Insert", imagePath).toDispatch();
//			
//			double d3 = Double.parseDouble(Dispatch.get(pic, "Width")
//					.toString());
//			double d4 = Double.parseDouble(Dispatch.get(pic, "Height")
//					.toString());
//			double d5 = d1 > d2 ? d1 : d2;
//			double d6 = d3 > d4 ? d3 : d4;
//			double d7;
//			if (d6 < d5)
//				d7 = 1.0D;
//			else
//				d7 = d5 / d6;
//			
//			if (d7 != 1.0D)
//			{
//				int i = (int)(d3 * d7);
//		        int j = (int)(d4 * d7);
//		        
//		        Dispatch.put(pic, "Width", i);
//		        Dispatch.put(pic, "Height", j);
//			}
//		}
//	}
	
	public static final int getRangeCount(Dispatch sheet)
	{
		Dispatch shapesDispatch = Dispatch.get(sheet, "Shapes").getDispatch();
		
		Dispatch rangeDispatch = Dispatch.invoke(shapesDispatch, "Range", Dispatch.Get, new Object[]{1},new int[1]).toDispatch();
		
		int count = Dispatch.get(rangeDispatch, "Count").getInt();
		
		return count;
	}
	
	public static final Dispatch getCell(Dispatch sheet, String cellName)
	{
		Dispatch cell = Dispatch.invoke(sheet,
				"Range", Dispatch.Get, new Object[]{cellName}, new int[1]).toDispatch();
		return cell;
	}
	
	public static int getRowIndex(Dispatch sheet, String cellName)
	{
		Dispatch xuhaoCell = getCell(sheet, cellName);
		
		Dispatch rows = Dispatch.call(xuhaoCell, "Rows").toDispatch();
		Dispatch row = Dispatch.invoke(rows, "Item", Dispatch.Get, new Object[]{1},new int[1]).toDispatch();
		int rowIndex = Dispatch.get(row,"row").getInt();
		
		return rowIndex;
	}
	
	public static final void copyRow(Dispatch sheet, int rowNo, int insertRows)
	{
		String rowArea = rowNo + ":" + rowNo;
		Dispatch row = Dispatch.invoke(sheet, "Rows", Dispatch.Get, new Object[] { rowArea }, new int[1]).toDispatch();
		for (int i = 0; i < insertRows; i++)
		{
			Dispatch.call(row, "Copy");
			Dispatch.call(row, "Insert");
		}
	}
	
	public static final String getSuffix(String fileName) 
	{
		int index = fileName.lastIndexOf('.');
		if (index != -1)
			return fileName.substring(index);
		return "";
	}

	public static final String getPrefix(String fileName) 
	{
		int index = fileName.lastIndexOf('.');
		if (index != -1)
			return fileName.substring(0, index);
		return fileName;
	}
	
	public static final void importFileToDataset(TCComponentDataset dataset, File file, String fileType, String refType) throws TCException 
	{
		String[] as1 = {file.getPath()};
		String[] as2 = {fileType};
		String[] as3 = {"Plain"};
		String[] as4 = {refType};
		dataset.setFiles(as1, as2, as3, as4);
	}
	
	public static final TCComponentDataset createDataset(String name, String description, String type) throws TCException
	{
		TCComponentDataset dataset = null;
		TCComponentDatasetType datasetType = (TCComponentDatasetType) getTcSession().getTypeComponent(type);
		if (datasetType == null)
			throw new TCException("无法获取名为" + type + "的数据集类型！");
		dataset = datasetType.create(name, description, type);
		return dataset;
	}
	
	public static final TCComponentItem createItem(String itemID, String revID, String type, String name, String description, TCComponent unitOfMeasure) throws TCException
	{
		TCComponentItem newItem = null;
		TCComponentItemType itemType = (TCComponentItemType) getTcSession().getTypeComponent(type);
		if (itemType == null)
			throw new TCException("无法获取名为" + type + "的Item类型");
		
		String newItemId = itemID;
		String newRevId = revID;
		
		if (newItemId.equals("")) 
		{
			newItemId = itemType.getNewID();
		}

		if (newRevId.equals("")) 
		{
			newRevId = itemType.getNewRev(null);
		}
		
		if (name.equals(""))
		{
			name = newItemId;
		}
		
		newItem = itemType.create(newItemId, newRevId, type, name, description, unitOfMeasure);
		return newItem;
	}
	
	public static final void removeFilesFromDataset(TCComponentDataset dataset, String namedReference) throws TCException 
	{
		if (dataset == null)
			return;
		
		NamedReferenceContext[] contexts = dataset.getDatasetDefinitionComponent().getNamedReferenceContexts();
		for (int i = 0; i < contexts.length; i++)
		{
			NamedReferenceContext context = contexts[i];
			String reference = context.getNamedReference();
			if (reference.equals(namedReference))
				dataset.removeNamedReference(reference);
		}
	}
	
	public static final void removeAllFilesFromDataset(TCComponentDataset dataset) throws TCException 
	{
		if (dataset == null)
			return;
		
		NamedReferenceContext[] contexts = dataset.getDatasetDefinitionComponent().getNamedReferenceContexts();
		for (int i = 0; i < contexts.length; i++)
		{
			NamedReferenceContext context = contexts[i];
			String reference = context.getNamedReference();
			dataset.removeNamedReference(reference);
		}
	}
	
	public static final void willExpand(TCComponentBOMLine[] bomLineArray) throws Exception
	{
		if (bomLineArray == null || bomLineArray.length <= 0)
			return;
		TcBOMService.expandOneLevel(getTcSession(), bomLineArray);
		Vector<TCComponentBOMLine> chidlrenVector = new Vector<TCComponentBOMLine>();
		for (int i = 0; i < bomLineArray.length; i++)
		{
			TCComponentBOMLine bomLine = bomLineArray[i];
			AIFComponentContext[] children = bomLine.getChildren();
			for (int j = 0; j < children.length; j++)
			{
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine)children[j].getComponent();
				chidlrenVector.add(childBOMLine);
			}
		}
		willExpand(chidlrenVector.toArray(new TCComponentBOMLine[chidlrenVector.size()]));
	}
	
	public static final void clearContents(Dispatch sheet, String rangeName)
	{
		Dispatch rangeDispatch = getCell(sheet, rangeName);
		Dispatch.call(rangeDispatch, "Select");
		Dispatch.call(rangeDispatch, "ClearContents");
	}
	
//	public static final Dispatch getCell(Dispatch sheet, String cellName)
//	{
//		Dispatch cell = Dispatch.invoke(sheet,
//				"Range", Dispatch.Get, new Object[]{cellName}, new int[1]).toDispatch();
//		return cell;
//	}

}
