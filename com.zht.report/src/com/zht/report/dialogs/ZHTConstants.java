package com.zht.report.dialogs;

import java.io.IOException;
import java.util.Properties;

public class ZHTConstants 
{	
	public static Properties properties;
	
	public static String NOBOMLINE_SELCTD_MSG = getValue("NoBOMLineSelected.Msg"); 
	
	public static String INVALID_SELCTION_MSG = getValue("InvalidSelection.Msg"); 
	
	public static String PRODUCTCATEGORY = getValue("ProductCatgory.Title");
	public static String VEHICLEDTAILRPORT = getValue("VehicleDetailReeport.Title");
	
	public static String SELECTFILE = getValue("SelectFile.Text");
	public static String SELECTFILEMSG = getValue("SelectFile.Msg");
	public static String BROWSER = getValue("Browser.Text");
	public static String EXECUTE = getValue("Execute.Text");
	public static String CLOSE = getValue("Close.Text");
	
	public static String HINT = getValue("Hint.Title");
	public static String WARNING = getValue("Warning.Title");
	public static String ERROR = getValue("Error.Title");
	
	public static String SAVE = getValue("Save.Name");
	public static String EXCEL_FILEFLITERNAMES = getValue("ExcelFileFilter.Names");
	
	public static String INVALIDFILEEXTENSION_MSG = getValue("InvalidFileExtension.Msg");
	public static String NOTSELCTEDFILEPATH_MSG = getValue("NotSelectedFilePath.Msg");
	
	public static String JOB_TITLE = getValue("Job.Title");
	public static String JOB_BEGINTASK_MSG = getValue("Job_BeginTask.Msg");
	
	public static String FIRSTFOLDER_NAME = getValue("FirstFolder.Name");
	public static String SECONDFOLDER_NAME = getValue("SecondFolder.Name");
	public static String THIRDOLDER_NAME = getValue("ThirdFolder.Name");
	
	public static String INVALIDPREFCONFIGURATION_MSG = getValue("InvalidPrefConfiguration.Msg");
	
	public static String PRODUCTCATEGORY_DSNAME = getValue("ProductCatgory.dsName");
	public static String VEHICLEDTAILRPORT_DSNAME = getValue("VehicleDetailReeport.dsName");
	
	public static String DATASETNOTFOUND_MSG = getValue("DatasetNotFound.Msg");
	
	public static String ABANDON = getValue("Abandon.Name");
	
	public static String DISSATISFIED_MSG = getValue("DisSatisfied.Msg");
	public static String CANNOTREAD_MSG = getValue("CannotRead.Msg");
	public static String ABANDON_MSG = getValue("Abandon.Msg");
	public static String NOTRELEASED_MSG = getValue("NotReleased.Msg");
	
	public static String PRODUCTCATEGORY_COMPLTED_MSG = getValue("ProductCatgory_Completed.Msg");
	public static String VEHICLEDTAILRPORT_COMPLTED_MSG = getValue("VehicleDetailReeport_Completed.Msg");
	public static String NOFMSFILE_MSG = getValue("NoFmsFile.Msg");
	
	public static String ONLYDBAALLOWED_MSG = getValue("OnlyDBAAllowed.Msg");
	public static String OPEN = getValue("Open.Name");
	public static String TEXT_FILEFLITERNAMES = getValue("TextFileFilter.Names");
	public static String INVALIDFILEFORMAT_MSG = getValue("InvalidFileFormat.Msg");
	public static String UPDATEPROPERTYJOB_BEGINTASK_MSG = getValue("ERPToPLMUpdatePropertyJob_BeginTask.Msg");
	public static String UPDATEPROPERTYJOB_SUBTASK_MSG = getValue("ERPToPLMUpdatePropertyJob_SubTask.Msg");
	public static String UPDATEPROPERTY_DONE_MSG = getValue("UpdateProperty_Done.Msg");
	
	public static String getValue(String key) 
	{
		String value = "";
		try {
			if (properties == null) {
				properties = System.getProperties();
				properties.load(ZHTConstants.class.getResourceAsStream("report_zh_CN.properties"));
			}
			value = properties.getProperty(key);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}
}
