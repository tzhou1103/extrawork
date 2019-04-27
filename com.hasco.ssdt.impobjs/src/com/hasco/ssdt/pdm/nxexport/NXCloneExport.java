package com.hasco.ssdt.pdm.nxexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hasco.ssdt.util.CommonUtils;
import com.hasco.ssdt.util.CustException;
import com.hasco.ssdt.util.MsgBox;
import com.hasco.ssdt.util.NXShell;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class NXCloneExport implements Runnable 
{
	TCSession session;

	AIFDesktop desktop;
	AbstractAIFUIApplication app = null;
	AIFComponentContext[] selectedContexts = null;

	TCComponentBOMLine selectedBOMLine;

	File NXExportDir;

	Vector<Map<String, String>> ugMaster;

	Map<String, String> noRevisionConfigured;

	String tempDir = System.getenv("temp");

	PrintWriter recordnotfoundlog;

	File NXMappingFile;

	public static final String NX_MappingFile_Name = "NX_export_mapping_file.clone";

	ProgressDialog pdlg = null;
	
	boolean errFlag = false;
	String userName = null;
	String userGroup = null;
	PrintWriter nxlog;
	String selectedPath = null;
	String nxBaseDir = null;
	String userPwd = null;

	Map<String, Map<String, String>> bomInfoMap;
	ArrayList<String> exportType = new ArrayList<String>();
	String defaultServerURL = "";
	
	Map<String, Boolean> objectNameMap = new HashMap<String, Boolean>();

	public NXCloneExport(String filePath, String exportPath, String nxBaseDir,ArrayList<String> exportType,String defaultServerURL) 
	{
		desktop = AIFUtility.getCurrentApplication().getDesktop();
		this.app = AIFUtility.getCurrentApplication();
		this.session = (TCSession) app.getSession();
		this.nxBaseDir = nxBaseDir;
		this.userPwd = session.getCredentials().getPassword();
		this.exportType = exportType;
		this.defaultServerURL = defaultServerURL;
	
		try {
			selectedContexts = app.getTargetContexts();
			if (selectedContexts.length == 0 || selectedContexts.length > 1) {
				MsgBox.showM("请选择BOM根节点！", "警告", 4);
				return;
			}
			
			selectedBOMLine = (TCComponentBOMLine) selectedContexts[0].getComponent();
			
			this.userName = session.getUserName();
			this.userGroup = session.getCurrentGroup().getFullName();	
			
			readExcel(filePath);
			
			buildExportPath(exportPath);
			nxlog = CommonUtils.mkLog(NXExportDir.getAbsolutePath(), "Export_log_record.txt");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			MessageBox.post(ioe);
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
	
	private void readExcel(String filePath) throws Exception
	{
		ActiveXComponent excelApp = JacobUtil.openExcelApp();
		Dispatch workBook = null;
		
		try {
			workBook = JacobUtil.getWorkBook(excelApp, filePath);
			Dispatch sheets = JacobUtil.getSheets(workBook);
			Dispatch sheet = JacobUtil.getSheet(sheets, 1);
			
			int index = 2;
			while (!JacobUtil.getCellData(sheet, "B" + index).isEmpty() ) 
			{
				String objectName = JacobUtil.getCellData(sheet, "B" + index);
				if (!objectNameMap.containsKey(objectName)) {
					objectNameMap.put(objectName, false);
				}
				index++;
			}
		} finally {
			JacobUtil.closeExcelApp(excelApp, workBook, false);
		} 
	}
	
    private void buildExportPath(String directoryPath) throws IOException, TCException 
    {		
    	TCComponentItemRevision rev = selectedBOMLine.getItemRevision();
		String newExportDir = directoryPath + "\\" + rev.getProperty("object_name");
		NXExportDir = new File(newExportDir);
		if (!(NXExportDir.exists() && NXExportDir.isDirectory()))
			NXExportDir.mkdir();
	}

	@Override
	public void run() 
	{
		try {
			pdlg = new ProgressDialog(desktop, "导出NX装配...");
			pdlg.setVisible(true);
			
			Vector<TCComponentBOMLine> vector = new Vector<TCComponentBOMLine>();
			String objectName = selectedBOMLine.getItemRevision().getProperty("object_name");
			if (objectNameMap.containsKey(objectName)) {
				objectNameMap.put(objectName, true);
				vector.add(selectedBOMLine);
			}
			getBOMLines(selectedBOMLine, vector);
			
			for (int i = 0; i < vector.size(); i++) 
			{
				TCComponentBOMLine bomLine = vector.get(i);
				objectName = bomLine.getItemRevision().getProperty("object_name");
				String path = NXExportDir.getCanonicalPath() + File.separator + objectName;
				File directory = new File(path);
				if (!directory.exists()) {
					directory.mkdir();
				}
				
				prepareToExport(bomLine, directory);
				
				if (noRevisionConfigured.size() > 0) 
				{
					StringBuffer buff = new StringBuffer();
					Object[] keys = noRevisionConfigured.keySet().toArray();
					for (int j = 0; j < keys.length; j++) {
						buff.append(noRevisionConfigured.get(keys[j]) + "\n");
					}
					MsgBox.showM("不存在Item版本:" + buff.toString(), "错误", MessageBox.ERROR);
					return;
				}

				startExportOperation(directory);
			}
			
			if (pdlg != null)
				pdlg.setVisible(false);		
			MsgBox.showM("NX导出装配结束！", "成功", 2);
			CommonUtils.ShowLog(NXExportDir.getAbsolutePath(), "Export_log_record.txt");

		} catch (Exception e) {
			e.printStackTrace();
			MsgBox.showM(e.toString(), "错误", 1);

		} finally {			
			if (pdlg != null)
				pdlg.setVisible(false);

			if (nxlog != null) 
			{				
				for (Entry<String, Boolean> entry : objectNameMap.entrySet()) {
					if (!entry.getValue()) {
						nxlog.println("BOM中未找到名称为 " + entry.getKey() + " 的数据");
					}
				}
				
				nxlog.println("####################################################################");
				
				nxlog.println("导出结束，如果导出的是装配,导出过程可以参考当前目录下的export.clone文件,");
				nxlog.println("它是Clone_Export运行时自动产生的Log日志");

				nxlog.println("###################################################################");
				nxlog.close();
				nxlog = null;
			}
		}
	}
	
	private void getBOMLines(TCComponentBOMLine bomLine, Vector<TCComponentBOMLine> vector) throws TCException
	{
		AIFComponentContext[] contexts = bomLine.getChildren();
		if (contexts != null && contexts.length > 0) 
		{
			for (AIFComponentContext context : contexts) 
			{
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine) context.getComponent();
				String objectName = childBOMLine.getItemRevision().getStringProperty("object_name");
				if (objectNameMap.containsKey(objectName)) {
					objectNameMap.put(objectName, true);
					vector.add(childBOMLine);
				}
				
				getBOMLines(childBOMLine, vector);
			}
		}
	}

	private void prepareToExport(TCComponentBOMLine bomLine, File directory) throws IOException, TCException, CustException 
	{
		bomInfoMap = new HashMap<String, Map<String, String>>();
		
		if (ugMaster != null)
			ugMaster.clear();
		else
			ugMaster = new Vector<Map<String, String>>();

		if (noRevisionConfigured != null)
			noRevisionConfigured.clear();
		else
			noRevisionConfigured = new Hashtable<String, String>();

		if (exportType.indexOf("UGMaster") != -1 || exportType.indexOf("UGPart") != -1)	
		{
			exportDataset(bomLine, directory.getCanonicalPath());
			
			addToUGExportQueue(bomLine);		
			parseBOM(bomInfoMap, bomLine, "");
			buildMappingFile(bomInfoMap, directory);
		} else {
			exportDataset(bomLine, directory.getCanonicalPath());
		}
	}

	private void startExportOperation(File directory) 
	{
		if (ugMaster.size() > 0) 
		{
			try {
				exportUGMasterDatasets(directory);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addToUGExportQueue(TCComponentBOMLine line) throws TCException, CustException 
	{		
		boolean hasUGMaster = false;
		if (line == null)
			return;

		if (line.getItemRevision() == null)
			return;

		TCComponentItemRevision rev = line.getItemRevision();
		String itemid = rev.getItem().getProperty("item_id");
		String revid = rev.getProperty("item_revision_id");

		if (nxBaseDir.equals("")) {
			throw new CustException("没有找到NX安装路径!");
		}
		TCComponent[] comps = rev.getRelatedComponents();

		for (int i = 0; i < comps.length; i++) 
		{
			if (comps[i] instanceof TCComponentDataset) 
			{
				TCComponentDataset ds = (TCComponentDataset) comps[i];
				String datasetType = ds.getType();

				if (datasetType.equals("UGMASTER")) 
				{
					File[] fs = ds.getFiles("UGPART");
					if (fs != null && fs.length > 0) {
						addUGMaster(itemid, revid, nxBaseDir);
						hasUGMaster = true;
						return;
					} else {
						MessageBox.post("零组件: " + itemid + "/" + revid + "有UGMaster对象" + ds.getProperty("object_name") + ",但UGMaster没有数模!", "错误", MessageBox.ERROR);
						return;
					}
				}
			}
		}
		
		if(!hasUGMaster){
			throw new CustException("零组件: " + itemid + "/" + revid+ "没有UGMaster对象");
		}
	}

	private void addUGMaster(String itemid, String revid, String baseString) throws TCException 
	{
		Map<String, String> ugmasterMap = new HashMap<String, String>();
		ugmasterMap.put("Id", itemid);
		ugmasterMap.put("RevId", revid);
		ugmasterMap.put("Path", baseString);
		ugMaster.add(ugmasterMap);
	}
	
	//导出数据集
	private void exportDataset(TCComponentBOMLine line, String path) throws TCException, IOException, CustException	
	{
		TCComponentItemRevision rev = line.getItemRevision();
		//实现导出数据集的方法
//	    DSExport.exportDS(rev, NXExportDir.getCanonicalPath(), exportType);
		DSExport.exportDS(rev, path, exportType);
		//过滤掉非H5_HascoPart
		//String viewType = line.getProperty("bl_view_type");
		
		AIFComponentContext[] context = line.getChildren();
		for (int i = 0; i < context.length; i++) {
		     TCComponentBOMLine child = (TCComponentBOMLine) context[i].getComponent();
		     exportDataset(child, path);
		}
	} 

	private void parseBOM(Map<String, Map<String, String>> map, TCComponentBOMLine line, String type) throws TCException, IOException, CustException 
	{
		if (line.getItemRevision() == null) {
			String id = line.getItem().getProperty("item_id");
			if (!noRevisionConfigured.containsKey(line.getItem().getProperty("item_id"))) {
				noRevisionConfigured.put(id, line.getItem().toString());	
			}
		} else {
			TCComponentItemRevision rev = line.getItemRevision();	
			String itemid = rev.getItem().getProperty("item_id");
			String revid = rev.getProperty("item_revision_id");
	/*		String revname=  CommonUtils.removeSpecilChar(rev.getProperty("object_name").trim()).replace("\"", "");
			String revdesc=  CommonUtils.removeSpecilChar(rev.getProperty("object_desc").trim()).replace("\"", "");*/
			
			String revname=  rev.getProperty("object_name");
			String revdesc= rev.getProperty("object_desc");
					
			String dsName = "";
			
			TCComponent[] relateComps = rev.getRelatedComponents("IMAN_specification");
			for (int i = 0; i < relateComps.length; i++) 
			{
				if (relateComps[i].getType().equals("UGPART")) {
					TCComponentDataset dataSetObject = (TCComponentDataset) relateComps[i];
					dsName = dataSetObject.getProperty("object_name");
				}
			}

			if (!map.containsKey(itemid + "@" + revid)) 
			{
				Map<String, String> itemMap = new HashMap<String, String>();
				itemMap.put("Id", itemid);
				itemMap.put("RevId", revid);

				String export3D = genFileName(rev, "3D");
				String export2D = genFileName(rev, "2D");
				System.out.println("export3D:"+export3D);
				System.out.println("export2D:"+export2D);

				export3D = CommonUtils.replaceSpaceChar(export3D);
				export2D = CommonUtils.replaceSpaceChar(export2D);

				itemMap.put("Export3D", export3D);
				itemMap.put("Export2D", export2D);
				itemMap.put("Type", "NX");
				itemMap.put("DsName", dsName);
				itemMap.put("RevName", revname);
				itemMap.put("RevDesc", revdesc);			
					
				map.put(itemid + "@" + revid, itemMap);
			}
			
//			String viewType = line.getProperty("bl_view_type");
		
			AIFComponentContext[] context = line.getChildren();
			if (context != null && context.length > 0)
			{
				for (int i = 0; i < context.length; i++) {
					TCComponentBOMLine child = (TCComponentBOMLine) context[i].getComponent();
					parseBOM(map, child, type);
				}
			}
		}
	}
	
	//生成map文件头部信息
	private void buildMappingFile(Map<String, Map<String, String>> map, File directory) throws IOException 
	{
//		NXMappingFile = createMappingFile(NXExportDir);
		NXMappingFile = createMappingFile(directory);
		PrintWriter pw3d = new PrintWriter(new FileOutputStream(NXMappingFile));
		pw3d.println("Assembly Cloning Log File");
		pw3d.println("&LOG Operation_Type: EXPORT_OPERATION"); 
		pw3d.println("&LOG Default_Cloning_Action: OVERWRITE"); 
		pw3d.println("&LOG Default_Container: \"\"");
		pw3d.println("&LOG Default_Part_Name: \"\"");
		pw3d.println("&LOG Default_Part_Type: H5_HascoPart");
		pw3d.println("&LOG Default_Naming_Technique: USER_NAME");
		pw3d.println("&LOG Naming_Rule_Type: RENAME Rename_String: temp");
		//pw3d.println("&LOG Naming_Rule_Type: APPEND_PREFIX Append_String: temp");
		pw3d.println("&LOG Default_Copy_Associated_Files: No"); 
		if (exportType.indexOf("UGPart") != -1) {
			pw3d.println("&LOG Default_Non_Master_Copy: specification Yes"); 
		} else {
			pw3d.println("&LOG Default_Non_Master_Copy: specification No");
		}
		
		pw3d.println("&LOG Default_Non_Master_Copy: manifestation No"); 
		pw3d.println("&LOG Default_Non_Master_Copy: altrep No"); 
		pw3d.println("&LOG Default_Non_Master_Copy: scenario No"); 
		pw3d.println("&LOG Default_Associated_Files_Directory: \"\"");
		pw3d.println("&LOG");

		printMappingContent(map, pw3d, directory);
		pw3d.close();
	}
	
    //生成map文件
	private File createMappingFile(File directory) throws IOException 
	{
		File mappingFile = new File(directory.getCanonicalFile(), NX_MappingFile_Name);

		if (mappingFile.exists())
			mappingFile.delete();
		mappingFile.createNewFile();

		return mappingFile;
	}

	private void printMappingContent(Map<String, Map<String, String>> map, PrintWriter pw, File directory) throws IOException 
	{
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) 
		{
			String key = (String) iter.next();
			Map<String, String> content = map.get(key);

			String id = (String) content.get("Id");
			String revid = (String) content.get("RevId");
			String partfile = (String) content.get("Export3D");
			String dsName = (String) content.get("DsName");
			String dsPartFile = (String) content.get("Export2D");
			String revdesc =  (String) content.get("RevDesc");
			String revname=  (String) content.get("RevName");
			
			pw.println("&LOG Part:@DB/" + id + "/"+revid);
			pw.println("&LOG Cloning_Action: DEFAULT_DISP Naming_Technique: DEFAULT_NAMING");
//			pw.println("&LOG Clone_Name: \""+ NXExportDir.getCanonicalPath()+"\\"+partfile+".prt\"");
			pw.println("&LOG Clone_Name: \""+ directory.getCanonicalPath()+"\\"+partfile+".prt\"");
			
			pw.println("&LOG Part_Type:H5_HascoPart");
			pw.println("&LOG Container: \"\"");
			pw.println("&LOG Part_Name: \"" + revname+"\"");
		    pw.println("&LOG Part_Description: \"" + revdesc+"\"");
		    pw.println("&LOG Associated_Files_Directory: \"\"");
			pw.println("&LOG ");

			if (!dsName.equals("") && dsName != null && exportType.indexOf("UGPart")!=-1) 
			{
				pw.println("&LOG Part:@DB@" + id + "@"+revid+"@specification"+"@"+dsName);
				pw.println("&LOG Cloning_Action: DEFAULT_DISP Naming_Technique: DEFAULT_NAMING");
//				pw.println("&LOG Clone_Name:  \""+ NXExportDir.getCanonicalPath()+"\\"+dsPartFile+".prt\"");
				pw.println("&LOG Clone_Name:  \""+ directory.getCanonicalPath()+"\\"+dsPartFile+".prt\"");
				pw.println("&LOG Part_Type:H5_HascoPart");
				pw.println("&LOG Container: \"\"");
				pw.println("&LOG Part_Name: \"" + revname+"\"");
			    pw.println("&LOG Part_Description: \"" + revdesc+"\"");
			    pw.println("&LOG Associated_Files_Directory: \"\"");
				pw.println("&LOG ");
			}
		}
	}

	private void exportUGMasterDatasets(File directory) throws IOException, TCException, InterruptedException, CustException 
	{
		for (int i = 0; i < ugMaster.size(); i++) 
		{
			Map<String, String> content = ugMaster.get(i);
			String id = content.get("Id");
			String revid = content.get("RevId");
			String path = content.get("Path");
			if (id.length() < 1)
				continue;
			if (exportType.indexOf("UGMaster") != -1 || exportType.indexOf("UGPart") != -1) 
			{
				nxlog.println("=========================开始导出数模=========================");
				nxlog.println();
				nxlog.println("#Row" + (i + 1) + ":" + "ItemRevision(" + id + "/" + revid + ")导出UGMaster");
//				exportUGMaster(id, revid, NXExportDir.getCanonicalPath(), nxlog, path);
				exportUGMaster(id, revid, directory.getCanonicalPath(), nxlog, path);
				nxlog.println();
				nxlog.println("=========================导出数模结束=========================");
			}
		}
	}

	private void exportUGMaster(String id, String revid, String filepath, PrintWriter uglog, String path) throws IOException, TCException, InterruptedException 
	{
		File commandFile = new File(filepath, "Clone_Export.bat");
		if (commandFile.exists())
			commandFile.delete();
		commandFile.createNewFile();
		PrintWriter pw = new PrintWriter(new FileOutputStream(commandFile));
		StringBuffer buff = new StringBuffer();	
//		pw.println("cd /d " + NXExportDir.getCanonicalPath() + "\\");
		pw.println("cd /d " + filepath + "\\");
				 
		pw.println("set TC_ROOT=" + System.getenv("TPR"));
		pw.println("set UGII_UGMGR_COMMUNICATION=HTTP");
		pw.println("set UGII_UGMGR_HTTP_URL="+defaultServerURL);

		pw.println("set UGII_BASE_DIR=" + path);
		pw.println("set UGII_ROOT_DIR=" + path + "\\UGII\\");
		
		buff.append("\"%UGII_BASE_DIR%\\ugmanager\\ugmanager_clone\"");
		buff.append(" -u=" + userName);
		buff.append(" -p=" + userPwd);
		buff.append(" -g=" + userGroup);
		buff.append(" -pim=Yes");
		buff.append(" -o=export");
		buff.append(" -l=\"" + NXMappingFile.getCanonicalPath() + "\"");
		buff.append(" -default_n=autotranslate");
		pw.print(buff.toString());
		pw.close();
	
		NXShell shell = new NXShell(commandFile.getCanonicalPath(), uglog);
		shell.runCmd();

		if (shell.returnCode != 0) {
			MsgBox.showM("Failed while export the UGMaster in"+buff.toString(), "Error", MessageBox.ERROR);
			nxlog.println();
			nxlog.println("=========================执行Clone_Export.bat脚本错误 =========================");
			return;
		} else {
			nxlog.println();
			nxlog.println("=========================执行Clone_Export.bat脚本结束========================= ");
		}
	}

	private String genFileName(TCComponentItemRevision IR, String genType) throws TCException 
	{
		String fileName = null;
		//boolean recordnotfoundflag = false;

		// mathdata数模ID
		String itemRevName = IR.getProperty("object_name");
		if (genType.equals("3D")) {
			fileName = itemRevName;
		} else if (genType.equals("2D")) {
			//fileName = itemID + "_dwg";
			fileName = itemRevName + ".drawing001";
		}

		fileName = fileName.replaceAll(System.getProperty("line.separator"), "");
		fileName = fileName.replaceAll("\r\n", "");
		fileName = fileName.replaceAll("\n", "");

		return CommonUtils.handleSpecialChar(fileName);
	}

	public boolean isContainsSpec(String name)
	{
		Matcher matcher = Pattern.compile("[\\u005C/:\\u002A\\u003F\"<>\'\\u007C’‘“”：？]").matcher(name);
		while (matcher.find()) {
			return true;
		}
	    
	    return false;
	}

}
