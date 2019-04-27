package com.zy.common;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCQueryClause;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTypeService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.soa.internal.client.model.LovValueImpl;

public class ReportCommon {

	public static ArrayList Get3DSnapShot(TCComponentMfgBvrBOPLine PaintStat) {
		ArrayList SnapShots = new ArrayList();
		try {
			TCComponent[] Context = PaintStat.getItemRevision().getRelatedComponents("IMAN_3D_snap_shot");
			for (int i = 0; i < Context.length; i++) {
				String Type = Context[i].getType();
				if (Type.equals("SnapShotViewData")) {
					SnapShots.add(Context[i]);
				}
			}
			AIFComponentContext[] Context2 = PaintStat.getItemRevision().getChildren();
			for (int i = 0; i < Context2.length; i++) {
				TCComponent comp = (TCComponent) Context2[i].getComponent();
				if (comp instanceof TCComponentDataset) {
					String Type = comp.getType();
					if (Type.equals("S4_DA_FirstLevImage")) {
						SnapShots.add(comp);
					}
				}
			}
			// 工位工艺版本下S4_DA_FirstLevImage类型的数据集
		} catch (TCException e) {
			e.printStackTrace();
		}
		return SnapShots;
	}

	public static ArrayList LoadProp(String Propfile, String Attribute) {
		ArrayList FirstSheet = new ArrayList();
		try {
			Properties prop = new Properties();
			InputStream stream = ReportCommon.class.getClassLoader().getResourceAsStream(Propfile); //$NON-NLS-1$
			if (stream != null) {
				prop.load(stream);
				String StampClear_First = prop.getProperty(Attribute);
				String Split[] = StampClear_First.split(",");
				for (int i = 0; i < Split.length; i++) {
					FirstSheet.add(Split[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FirstSheet;
	}

	public static TCComponentDataset GetTemplateDataset(String TemplateName) {
		TCComponentFolder TemplateFolder = ReportCommon.GetReportTemplateFolder("BOP Report Template");
		if (TemplateFolder == null) {
			MessageBox.post("Report Template Folder not found.", "Infomation", 2);
			return null;
		}

		TCComponentDataset TemplateDatset = ReportCommon.GetSubDataset(TemplateFolder, "MSExcelX", TemplateName); //$NON-NLS-1$
		if (TemplateDatset == null) {
			MessageBox.post("Report Template dataset not found.", "Infomation", 2);
			return null;
		}
		return TemplateDatset;
	}

	public static File DownloadReportTemplate(TCComponentDataset TemplateDatset, String NewFileName, String localDirName) {
		File TempFile = DatasetFileToLocalDir(TemplateDatset, localDirName);
		if (TempFile.exists()) {
			try {
				File NewTempFile = new File(localDirName, NewFileName);
				TempFile.renameTo(NewTempFile);
				TempFile = NewTempFile;
			} catch (Exception e) {
			}
			return TempFile;
		} else {
			System.out.println("报表模板下载失败!");
			// MessageBox.post("报表模板下载失败!", "提示", MessageBox.WARNING);
		}
		return null;
	}

	public static String[] GetLOVValue(String LOVName) {
		try {
			TCComponentListOfValues List = ReportCommon.findLOVByName(LOVName);
			ListOfValuesInfo Info = List.getListOfValues();
			List values = Info.getValues();
			String Value[] = new String[values.size()];
			for (int i = 0; i < values.size(); i++) {
				LovValueImpl Imp = (LovValueImpl) values.get(i);
				Value[i] = Imp.getDisplayValue();
			}
			return Value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	public static TCComponentListOfValues findLOVByName(String LOVName) {
		TCComponentListOfValues tccomponentlistofvalues = null;
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentListOfValuesType tccomponentlistofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
			TCComponentListOfValues atccomponentlistofvalues[] = tccomponentlistofvaluestype.find(LOVName);
			if (atccomponentlistofvalues != null && atccomponentlistofvalues.length > 0)
				tccomponentlistofvalues = atccomponentlistofvalues[0];
			else
				tccomponentlistofvalues = null;
		} catch (TCException tcexception) {
			tccomponentlistofvalues = null;
		}
		return tccomponentlistofvalues;
	}

	/**
	 * 下载数据集到本地
	 * 
	 * @param componentDataset
	 * @param datasetType
	 * @param namedRefName
	 * @param localFileName
	 * @param localDirName
	 * @return
	 */
	public static File DatasetFileToLocalDir(TCComponentDataset componentDataset, String localDirName) {
		// File fileObject;
		File dirObject = new File(localDirName);
		if (!dirObject.exists())
			dirObject.mkdirs();
		if (componentDataset == null)
			return null;
		try {
			componentDataset = componentDataset.latest();
			TCComponentDatasetDefinition Defin = componentDataset.getDatasetDefinitionComponent();
			String namedRefName = Defin.getNamedReferences()[0];
			String namedRefFileName[] = componentDataset.getFileNames(namedRefName);
			if ((namedRefFileName == null) || (namedRefFileName.length == 0)) {
				// MessageBox.post("没有对应的报表模板!", "系统配置错误", MessageBox.ERROR);
				System.out.println("没有对应的报表模板!");
				return null;
			}
			// Delete old file
			File tempFileObject = new File(localDirName, namedRefFileName[0]);
			if (tempFileObject.exists())
				tempFileObject.delete();
			File tempFile = componentDataset.getFile(namedRefName, namedRefFileName[0], localDirName);
			return tempFile;
		} catch (TCException e) {
			MessageBox.post("Download failed!\n" + e.getDetailsMessage(), "Error", MessageBox.ERROR);
			return null;
		}
	}

	/**
	 * Export dataset file to local directory, dataset file from the first named reference of the first dataset object found via dataset name
	 * 
	 * @param datasetType
	 *            dataset type
	 * @param datasetName
	 *            dataset name
	 * @param namedRefName
	 *            named reference name
	 * @param localFileName
	 *            local file name
	 * @param localDirName
	 *            local directory
	 * @return true(OK)/false(fail)
	 */
	public synchronized static File datasetFileToLocalDir(TCComponentDataset componentDataset, String namedRefName, String localDirName) {
		File fileObject = null;
		File tempFileObject;
		File dirObject = new File(localDirName);
		if (!dirObject.exists())
			dirObject.mkdirs();

		try {
			componentDataset = componentDataset.latest();
			// TCComponentDatasetDefinition Defin =
			// componentDataset.getDatasetDefinitionComponent();
			// String namedRefName1 = Defin.getNamedReferences()[0];

			String namedRefFileName[] = componentDataset.getFileNames(namedRefName);
			if ((namedRefFileName == null) || (namedRefFileName.length == 0)) {
				// MessageBox.post("数据集<" + datasetName + ">没有对应的命名引用!", "系统配置错误", MessageBox.ERROR);
				return null;
			}
			// Delete old file
			tempFileObject = new File(localDirName, namedRefFileName[0]);
			if (tempFileObject.exists())
				tempFileObject.delete();
			fileObject = componentDataset.getFile(namedRefName, namedRefFileName[0], localDirName);
			return fileObject;
		} catch (TCException e) {
			// System.out.print("数据集<" + datasetName + ">配置错误!\n");

		}

		return null;
	}

	public static TCComponentDataset createOrUpdateDataset(String localFile, String datasetType, String datasetNamedRef, String datasetName, TCComponent itemRevision, String relationType, boolean replaceAlert) {
		TCComponentDataset datasetComponent = null;
		try {
			TCSession TCSession = (TCSession) AIFUtility.getDefaultSession();
			TCTypeService typeService = TCSession.getTypeService();
			TCComponentDatasetType TCDatasetType = (TCComponentDatasetType) typeService.getTypeComponent(datasetType);
			TCComponent TCComponent[] = itemRevision.getRelatedComponents(relationType);
			if ((TCComponent != null) && (TCComponent.length > 0)) // Find
			{
				for (int i = 0; i < TCComponent.length; i++)
					if ((TCComponent[i].getType().equals(datasetType)) && (TCComponent[i].getProperty("object_name").equals(datasetName))) {
						datasetComponent = (TCComponentDataset) TCComponent[i];
						break;
					}
			}
			String filePathNames[] = { localFile };
			String namedRefs[] = { datasetNamedRef };
			if (datasetComponent == null) {
				datasetComponent = TCDatasetType.setFiles(datasetName, "", datasetType, filePathNames, namedRefs);
				TCComponent imanfile = datasetComponent.getNamedRefComponent(datasetNamedRef);
				if (imanfile != null) {
					try {
						imanfile.setProperty("original_file_name", datasetName + ".xlsx");
					} catch (Exception e1) {
						//
					}
				}
				itemRevision.add(relationType, datasetComponent);
			} else {
				boolean confirm = true;
				if (replaceAlert)
					confirm = MessageDialog.openQuestion(null, "确认", datasetName + "已经存在，是否覆盖？");
				if (confirm) {
					TCDatasetType.setFiles(datasetComponent, filePathNames, namedRefs);
				}
				return datasetComponent;
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
			return null;
		}
		return datasetComponent;
	}

	public static boolean CheckValidTemplate(TCComponentDataset Dataset) {
		try {
			AIFComponentContext[] Context = Dataset.whereReferenced();
			for (int i = 0; i < Context.length; i++) {
				TCComponent comp = (TCComponent) Context[i].getComponent();
				if (comp instanceof TCComponentFolder) {
					if (comp.getProperty("owning_user").indexOf("infodba") >= 0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Find the first dataset object by dataset type and dataset name
	 * 
	 * @param datasetType
	 *            dataset type
	 * @param datasetName
	 *            dataset name
	 * @return first dataset object found
	 */
	public static TCComponentDataset findDatasetByName(TCSession imanSession, String datasetType, String datasetName) {
		// TCComponentDataset imanComponentDataset = null;
		try {
			if (datasetName != null) {
				TCTypeService typeService = imanSession.getTypeService();
				TCComponentDatasetType imanDatasetType = (TCComponentDatasetType) typeService.getTypeComponent(datasetType);
				// imanComponentDataset = imanDatasetType.find(datasetName);
				TCComponentDataset imanComponentAllDataset[] = imanDatasetType.findAll(datasetName);
				for (int i = 0; i < imanComponentAllDataset.length; i++) {
					if (imanComponentAllDataset[i].getTCProperty("object_type").getStringValue().equals(datasetType)) {
						if ((imanComponentAllDataset[i].getProperty("owning_user").indexOf("infodba") >= 0) && ((imanComponentAllDataset[i].getProperty("object_string").compareTo(datasetName) == 0))) {
							// 检查是否在文件夹报表模版目录下
							if (CheckValidTemplate(imanComponentAllDataset[i])) {
								return imanComponentAllDataset[i];
							}
						}
					}
				}
			}
			return null;
		} catch (TCException e) {
			System.out.print("数据集类型<" + datasetType + ">不存在!");
			return null;
		}
		// if (imanComponentDataset == null)
		// MessageBox.post("数据集对象<" + datasetName + ">不存在!", "系统配置错误",
		// MessageBox.ERROR);
		// return imanComponentDataset;
	}

	public static TCComponentDataset GetSubDataset(TCComponent Folder, String FolderType, String FolderName) {
		try {
			AIFComponentContext Context[] = Folder.getChildren();
			for (int i = 0; i < Context.length; i++) {
				TCComponent Temp = (TCComponent) Context[i].getComponent();
				String Type = Temp.getTCProperty("object_type").getStringValue();
				String Name = Temp.getProperty("object_name");
				if (Type.equals(FolderType) && Name.equals(FolderName)) {
					return (TCComponentDataset) Temp;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static TCComponent[] GetWorkflowTemplate() {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery itemQuery = (TCComponentQuery) queryType.find("__Process_Templates");
			if (itemQuery == null) {
				itemQuery = (TCComponentQuery) queryType.find("__Process_Templates");
			}
			String[] names = new String[] { "模板分类" };
			String[] values = new String[] { "0" };
			TCComponent items[] = ExecQuery(itemQuery, names, values);
			return items;
		} catch (Exception e) {
			return null;
		}
	}

	public static TCComponent[] GetGroups() {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery itemQuery = (TCComponentQuery) queryType.find("__RB_Group_By_Name");
			if (itemQuery == null) {
				itemQuery = (TCComponentQuery) queryType.find("__RB_Group_By_Name");
			}
			String[] names = new String[] { "__Name" };
			String[] values = new String[] { "*" };
			TCComponent items[] = ExecQuery(itemQuery, names, values);
			return items;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean IsEnglisth() {
		Locale locale = Locale.getDefault();
		// System.out.println(locale.getLanguage());
		// System.out.println(locale.getCountry());
		if (locale.toString().equals("en_US") || locale.toString().equals("en")) {
			return true;
		} else {
			return false;
		}
	}

	public static TCComponentQuery GetGeneralQuery() {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery itemQuery = null;
			if (IsEnglisth()) {
				itemQuery = (TCComponentQuery) queryType.find("General...");

			} else {
				itemQuery = (TCComponentQuery) queryType.find("常规...");
			}
			return itemQuery;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponentFolder GetLibFolder(String TypeName) {
		try {
			TCComponentQuery itemQuery = GetGeneralQuery();
			String[] names = new String[] { "Type", "OwningUser" };
			String[] values = new String[] { TypeName, "*infodba*" };
			TCComponent items[] = ExecQuery(itemQuery, names, values);
			return (TCComponentFolder) items[0];
		} catch (Exception e) {
			return null;
		}
	}

	public static TCComponentFolder GetReportTemplateFolder(String TemplateFolderName) {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery itemQuery = (TCComponentQuery) queryType.find("__WEB_find_user");
			if (itemQuery != null) {
				TCComponentUser infodbaUser = null;

				String[] names = null;
				String[] values = new String[] { "infodba" };
				if (ReportCommon.IsEnglisth()) {
					names = new String[] { "User ID" };
				} else {
					names = new String[] { "用户 ID" };
				}
				TCComponent user[] = ExecQuery(itemQuery, names, values);
				infodbaUser = (TCComponentUser) user[0];

				TCComponentFolder HomeFolder = infodbaUser.getHomeFolder();
				TCComponent TemplateFolder = GetRelationComp(HomeFolder, "contents", "Folder", TemplateFolderName);
				return (TCComponentFolder) TemplateFolder;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponent GetRelationComp(TCComponent PrimaryComp, String RelationName, String SecondTypeName, String SecondName) {
		try {
			TCComponent Second[] = PrimaryComp.getRelatedComponents(RelationName);
			for (int i = 0; i < Second.length; i++) {
				String Type = Second[i].getTCProperty("object_type").getStringValue();
				if (Type.equals(SecondTypeName)) {
					String Name = Second[i].getProperty("object_name");
					if (Name.equals(SecondName)) {
						return Second[i];
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponent GetRelationComp(TCComponent PrimaryComp, String RelationName, String SecondTypeName) {
		try {
			TCComponent Second[] = PrimaryComp.getRelatedComponents(RelationName);
			for (int i = 0; i < Second.length; i++) {
				String Type = Second[i].getTCProperty("object_type").getStringValue();
				if (Type.equals(SecondTypeName)) {
					return Second[i];
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TCComponentFolder GetReportTemplateFolder_bak(String TemplateFolderName) {
		try {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery itemQuery = (TCComponentQuery) queryType.find("General...");
			if (itemQuery == null) {
				itemQuery = (TCComponentQuery) queryType.find("常规...");

			}
			String[] names = new String[] { "Name", "OwningUser" };
			String[] values = new String[] { TemplateFolderName, "*infodba*" };
			TCComponent items[] = ExecQuery(itemQuery, names, values);
			return (TCComponentFolder) items[0];
		} catch (Exception e) {
			return null;
		}
	}

	public static TCComponent[] ExecQuery(TCComponentQuery itemQuery, String[] names, String[] values) {
		try {
			TCQueryClause Clause[] = itemQuery.describe();
			// TransLate names to UserEntryNameDisplay
			for (int i = 0; i < names.length; i++) {
				for (int m = 0; m < Clause.length; m++) {
					if (names[i].equals(Clause[m].getUserEntryName())) {
						names[i] = Clause[m].getUserEntryNameDisplay();
						break;
					}
				}
			}
			TCComponent Comp2[] = itemQuery.execute(names, values);
			return Comp2;
		} catch (Exception e) {
			return null;
		}
	}

	// UDSExportDB 导出功能数据连结
	public static String[] GetPreferences(String PreferenceName) {

		try {
			String PreferencesValues[] = new String[0];
			// 配置文件首选项
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCPreferenceService PreferService = session.getPreferenceService();
			PreferencesValues = PreferService.getStringArray(TCPreferenceService.TC_preference_site, PreferenceName);
			if ((PreferencesValues == null) || (PreferencesValues.length == 0)) {
				System.out.println("Not found preference:" + PreferenceName);
				return new String[0];
			}
			return PreferencesValues;
		} catch (Exception e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	/**
	 * 判断一个字符串是否是数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		return Pattern.compile("^\\-?\\d+\\.?\\d*$").matcher(str).find();
	}

	public static boolean isAllNumber(String str) {
		if (str.length() == 0) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char temp = str.charAt(i);
			if (temp > '9' || temp < '0') {
				return false;
			}
		}
		return true;
	}

	public static String RemoveZero(String str) {
		String Temp = str;
		// 判断是否数字
		for (int i = 0; i < Temp.length(); i++) {
			if ((Temp.charAt(i) > '9') || (Temp.charAt(i) < '0')) {
				if (Temp.charAt(i) != '.') {
					return str;
				}
			}
		}
		// 去掉末尾的零
		if (Temp.indexOf(".") > 0) {
			while (Temp.endsWith("0")) {
				Temp = Temp.substring(0, Temp.length() - 1);
			}
			if (Temp.endsWith(".")) {
				Temp = Temp.substring(0, Temp.length() - 1);
			}
		}
		return Temp;
	}

}
