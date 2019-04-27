package com.hasco.ssdt.oem.nxexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hasco.ssdt.de.nximport.ProgressDialog;
import com.hasco.ssdt.util.CommonUtils;
import com.hasco.ssdt.util.CustException;
import com.hasco.ssdt.util.MsgBox;
import com.hasco.ssdt.util.NXShell;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class NXCloneExport implements Runnable {

	TCSession session;

	AIFDesktop desktop;
	AbstractAIFUIApplication app = null;
	AIFComponentContext[] selectedContexts = null;

	TCComponentBOMLine selectedBOMLine;

	File NXExportDir;

	Vector ugMaster;

	Vector ugPart;

	Map noRevisionConfigured;

	String tempDir = System.getenv("temp");

	PrintWriter recordnotfoundlog;

	File NXMappingFile;

	public static final String NX_MappingFile_Name = "NX_export_mapping_file.clone";
	HashMap<String,String> nxPartToPath = new HashMap<String,String>  ();

	ProgressDialog pdlg = null;
	
	boolean errFlag = false;
	String userName = null;
	String userGroup = null;
	PrintWriter nxlog;
	String selectedPath = null;
	String nxBaseDir = null;
	String userPwd = null;

	Map bomInfoMap;
	ArrayList<String> exportType = new ArrayList<String>();
	String defaultServerURL="";

	public NXCloneExport(String selectedPath, String nxBaseDir,ArrayList<String> exportType,String defaultServerURL) {

		desktop = AIFUtility.getCurrentApplication().getDesktop();
		this.app = AIFUtility.getCurrentApplication();
		this.session = (TCSession) app.getSession();
		this.nxBaseDir = nxBaseDir;
		this.userPwd = session.getCredentials().getPassword();
		this.exportType = exportType;
		this.defaultServerURL= defaultServerURL;
	
		try {
			selectedContexts = app.getTargetContexts();
			if (selectedContexts.length == 0 || selectedContexts.length > 1) {
				MsgBox.showM("请选择BOM根节点！", "警告", 4);
				return;
			}
			selectedBOMLine = (TCComponentBOMLine) selectedContexts[0]
					.getComponent();
			this.userName = session.getUserName();
			this.userGroup = session.getCurrentGroup().getFullName();	
			
			buildExportPath(selectedPath);
			nxlog = CommonUtils.mkLog(NXExportDir.getAbsolutePath(),
					"Export_log_record.txt");

		} catch (IOException ioe) {
			MessageBox.post(ioe);
		} catch (TCException e) {
			MessageBox.post(e);
		}

	}

    private void buildExportPath(String directoryPath) throws IOException, TCException {
		
    	TCComponentItemRevision rev = selectedBOMLine.getItemRevision();
		String newExportDir = directoryPath+"\\"+getTopPartNumber(rev).toLowerCase();
		System.out.println("newExportDir:"+newExportDir);
		NXExportDir = new File(newExportDir);
		if (!(NXExportDir.exists() && NXExportDir.isDirectory()))
			NXExportDir.mkdir();

	}
    
   public String getTopPartNumber(TCComponentItemRevision rev) throws TCException
   {
	   String partNumber = "";
	   TCComponent[] relateComps = rev.getRelatedComponents("IMAN_specification");
		for (int i = 0; i < relateComps.length; i++) {
		
			System.out.println("relateComps[i].getType():"+relateComps[i].getType());
			if (relateComps[i].getType().equals("H5_SSDTOEMForm")) {
				TCComponentForm form = (TCComponentForm) relateComps[i];
				TCProperty[] properties = form.getFormTCProperties();
				for(int j=0;j<properties.length;j++){
					System.out.println("properties:"+j+":"+properties[j].getPropertyName());
					System.out.println("properties type:"+j+":"+properties[j].getPropertyType());
				}
				partNumber = form.getProperty("h5partnumber");
			}
		}
		
		if(partNumber.equals("")){
			
			partNumber = rev.getProperty("item_id");
		}
		return partNumber;
   }
	

	@Override
	public void run() {

		try {		
			pdlg = new ProgressDialog(desktop, "导出NX装配...");
			pdlg.setVisible(true);
			
			prepareToExport();

			if (noRevisionConfigured.size() > 0) {
				StringBuffer buff = new StringBuffer();
				Object[] keys = noRevisionConfigured.keySet().toArray();
				for (int i = 0; i < keys.length; i++) {
					buff.append(noRevisionConfigured.get(keys[i]) + "\n");
				}
				MsgBox.showM( "不存在Item版本:"+buff.toString(), "错误",MessageBox.ERROR);
				return;
			}

			startExportOperation();
			if (pdlg != null)
				pdlg.setVisible(false);		
			MsgBox.showM("NX导出装配结束！", "成功", MessageBox.INFORMATION);
			CommonUtils.ShowLog(NXExportDir.getAbsolutePath(),"Export_log_record.txt");

		} catch (TCException e) {
			e.printStackTrace();
			MsgBox.showM(e.toString(), "错误", MessageBox.ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			MsgBox.showM(e.toString(), "错误", MessageBox.ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			MsgBox.showM(e.toString(), "错误", MessageBox.ERROR);

		} finally{
			
			if (pdlg != null)
				pdlg.setVisible(false);

			if (nxlog != null) {
				nxlog.println("####################################################################");
				
				nxlog.println("导出结束，如果导出的是装配,导出过程可以参考当前目录下的export.clone文件,");
				nxlog.println("它是Clone_Export运行时自动产生的Log日志");

				nxlog.println("###################################################################");
				nxlog.close();
				nxlog = null;
			}
		}
	}

	private void prepareToExport() throws IOException, TCException,
			CustException {

		bomInfoMap = new HashMap();
		
		if (ugMaster != null)
			ugMaster.clear();
		else
			ugMaster = new Vector();
		if (ugPart != null)
			ugPart.clear();
		else
			ugPart = new Vector();

		if (noRevisionConfigured != null)
			noRevisionConfigured.clear();
		else
			noRevisionConfigured = new Hashtable();

		
		if(exportType.indexOf("UGMaster")!=-1 || exportType.indexOf("UGPart")!=-1)
		{
			if(exportType.indexOf("UGMaster")!=-1 && exportType.indexOf("UGPart")!=-1 && exportType.size()>2)
			{
				exportDataset(selectedBOMLine,new ArrayList<String>());
			}
			if(exportType.indexOf("UGMaster")!=-1 && exportType.indexOf("UGPart")==-1 && exportType.size()>1)
			{
				exportDataset(selectedBOMLine,new ArrayList<String>());
			}
			if(exportType.indexOf("UGMaster")==-1 && exportType.indexOf("UGPart")!=-1 && exportType.size()>1)
			{
				exportDataset(selectedBOMLine,new ArrayList<String>());
			}
			
			addToUGExportQueue(selectedBOMLine);		
			parseBOM(bomInfoMap, selectedBOMLine, "");
			buildMappingFile(bomInfoMap);
			
		}else{
			
			exportDataset(selectedBOMLine,new ArrayList<String>());
			
		}

	}

	private void startExportOperation() {

		if (ugMaster.size() > 0) {
			try {
				exportUGMasterDatasets();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void addToUGExportQueue(TCComponentBOMLine line)
			throws TCException, CustException {
		
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

		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof TCComponentDataset) {
				TCComponentDataset ds = (TCComponentDataset) comps[i];
				String datasetType = ds.getType();

				if (datasetType.equals("UGMASTER")) {
					File[] fs = ds.getFiles("UGPART");
					if (fs != null && fs.length > 0) {
						addUGMaster(itemid, revid, nxBaseDir);
						hasUGMaster = true;
						return;
					} else {
						MessageBox.post("零组件: " + itemid + "/" + revid
								+ "有UGMaster对象" + ds.getProperty("object_name")
								+ ",但UGMaster没有数模!", "错误", MessageBox.ERROR);
						return;
					}
				}
			}
		}
		
		if(!hasUGMaster){
			throw new CustException("零组件: " + itemid + "/" + revid+ "没有UGMaster对象");
		}

	}

	private void addUGMaster(String itemid, String revid, String baseString)
			throws TCException {
		Map ugmasterMap = new HashMap();
		ugmasterMap.put("Id", itemid);
		ugmasterMap.put("RevId", revid);
		ugmasterMap.put("Path", baseString);
		ugMaster.add(ugmasterMap);
	}
	
	private void exportDataset(TCComponentBOMLine line,ArrayList<String> lastTransferPath) throws TCException, IOException, CustException
	{
		ArrayList<String> currentTransferPath = new ArrayList<String>();
		currentTransferPath.clear();
		TCComponentItemRevision rev = line.getItemRevision();				
	    DSExport.exportDS(rev, NXExportDir.getCanonicalPath(),exportType);
		//过滤掉非H5_OEMPart
		//String viewType = line.getProperty("bl_view_type");
		AIFComponentContext[] context = line.getChildren();
		for (int i = 0; i < context.length; i++) {
		     TCComponentBOMLine child = (TCComponentBOMLine) context[i]
						.getComponent();
		     exportDataset(child,currentTransferPath);
		}
	} 

	private void parseBOM(Map map, TCComponentBOMLine line,
			String type) throws TCException, IOException, CustException {
				
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
			String partName = "";
			
			TCComponent[] relateComps = rev.getRelatedComponents("IMAN_specification");
			for (int i = 0; i < relateComps.length; i++) {
				if (relateComps[i].getType().equals("UGPART")) {

					TCComponentDataset dataSetObject = (TCComponentDataset) relateComps[i];
					dsName = dataSetObject.getProperty("object_name");
				}
				if (relateComps[i].getType().equals("H5_SSDTOEMForm")) {
					TCComponentForm form = (TCComponentForm) relateComps[i];
					partName = form.getProperty("h5partnumber");
				}
			}

			if (!map.containsKey(itemid + "@" + revid)) {
				Map itemMap = new HashMap();
				itemMap.put("Id", itemid);
				itemMap.put("RevId", revid);

				String export3D = genFileName(rev, partName,"3D");
				String export2D = genFileName(rev, partName,"2D");
				System.out.println("export3D:"+export3D);
				System.out.println("export2D:"+export2D);

				export3D = CommonUtils.replaceSpaceChar(export3D);
				export2D = CommonUtils.replaceSpaceChar(export2D);

				itemMap.put("Export3D", export3D);
				itemMap.put("Export2D", export2D);
				itemMap.put("Type", "NX");
				itemMap.put("DsName", dsName);
				itemMap.put("PartName", partName);
				itemMap.put("RevName", revname);
				itemMap.put("RevDesc", revdesc);			
					
				map.put(itemid + "@" + revid, itemMap);
				
			}
					
			AIFComponentContext[] context = line.getChildren();
			if (context != null && context.length > 0) {
				for (int i = 0; i < context.length; i++) {
					TCComponentBOMLine child = (TCComponentBOMLine) context[i]
							.getComponent();
					parseBOM(map, child, type);
				}
			}
									
		}
	}

	private void buildMappingFile(Map map) throws IOException {

		NXMappingFile = createMappingFile(NXExportDir);
		PrintWriter pw3d = new PrintWriter(new FileOutputStream(NXMappingFile));
		pw3d.println("Assembly Cloning Log File");
		pw3d.println("&LOG Operation_Type: EXPORT_OPERATION"); 
		pw3d.println("&LOG Default_Cloning_Action: OVERWRITE"); 
		pw3d.println("&LOG Default_Container: \"\"");
		pw3d.println("&LOG Default_Part_Name: \"\"");
		pw3d.println("&LOG Default_Part_Type: H5_OEMPart");
		pw3d.println("&LOG Default_Naming_Technique: USER_NAME");
		pw3d.println("&LOG Naming_Rule_Type: RENAME Rename_String: temp");
		//pw3d.println("&LOG Naming_Rule_Type: APPEND_PREFIX Append_String: temp");
		pw3d.println("&LOG Default_Copy_Associated_Files: No"); 
		if( exportType.indexOf("UGPart")!=-1)
		{
			pw3d.println("&LOG Default_Non_Master_Copy: specification Yes"); 
			
		}else{
			pw3d.println("&LOG Default_Non_Master_Copy: specification No"); 
		}
		
		pw3d.println("&LOG Default_Non_Master_Copy: manifestation No"); 
		pw3d.println("&LOG Default_Non_Master_Copy: altrep No"); 
		pw3d.println("&LOG Default_Non_Master_Copy: scenario No"); 
		pw3d.println("&LOG Default_Associated_Files_Directory: \"\"");
		pw3d.println("&LOG");

		printMappingContent(map, pw3d);
		pw3d.close();

	}

	private File createMappingFile(File directory) throws IOException {

		File mappingFile = new File(directory.getCanonicalFile(),NX_MappingFile_Name);

		if (mappingFile.exists())
			mappingFile.delete();
		mappingFile.createNewFile();

		return mappingFile;
	}

	private void printMappingContent(Map map, PrintWriter pw) throws IOException {
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Map content = (Map) map.get(key);

			String id = (String) content.get("Id");
			String revid = (String) content.get("RevId");
			String partfile = (String) content.get("Export3D");
			String dsName = (String) content.get("DsName");
			String partName = (String) content.get("PartName");
			String dsPartFile = (String) content.get("Export2D");
			String revdesc =  (String) content.get("RevDesc");
			String revname=  (String) content.get("RevName");
			
			pw.println("&LOG Part:@DB/" + id + "/"+revid);
			pw.println("&LOG Cloning_Action: DEFAULT_DISP Naming_Technique: DEFAULT_NAMING");
			pw.println("&LOG Clone_Name: \""+ NXExportDir.getCanonicalPath()+"\\"+partfile+".prt\"");
			pw.println("&LOG Part_Type:H5_OEMPart");
			pw.println("&LOG Container: \"\"");
			if(!partName.equals("")){
				pw.println("&LOG Part_Name: \"" + partName+"\"");
			}else{
				pw.println("&LOG Part_Name: \"" + revname+"\"");
			}
			
		    pw.println("&LOG Part_Description: \"" + revdesc+"\"");
		    pw.println("&LOG Associated_Files_Directory: \"\"");
			pw.println("&LOG ");

			if (!dsName.equals("") && dsName != null && exportType.indexOf("UGPart")!=-1) {
				pw.println("&LOG Part:@DB@" + id + "@"+revid+"@specification"+"@"+dsName);
				pw.println("&LOG Cloning_Action: DEFAULT_DISP Naming_Technique: DEFAULT_NAMING");
				pw.println("&LOG Clone_Name:  \""+ NXExportDir.getCanonicalPath()+"\\"+dsPartFile+".prt\"");
				pw.println("&LOG Part_Type:H5_OEMPart");
				pw.println("&LOG Container: \"\"");
				if(!partName.equals("")){
					pw.println("&LOG Part_Name: \"" + partName+"\"");
				}else{
					pw.println("&LOG Part_Name: \"" + revname+"\"");
				}
			    pw.println("&LOG Part_Description: \"" + revdesc+"\"");
			    pw.println("&LOG Associated_Files_Directory: \"\"");
				pw.println("&LOG ");
			}
		}
	}


	private void exportUGMasterDatasets() throws IOException, TCException, InterruptedException, CustException 
	{

			for (int i = 0; i < ugMaster.size(); i++) {

				Map content = (Map) ugMaster.get(i);
				String id = (String) content.get("Id");
				String revid = (String) content.get("RevId");
				String path = (String) content.get("Path");
				if (id.length() < 1)
					continue;
								
				if(exportType.indexOf("UGMaster")!=-1 ||exportType.indexOf("UGPart")!=-1)
				{
					nxlog.println("=========================开始导出数模=========================");
					nxlog.println();
					nxlog.println("#Row" + (i + 1) + ":" + "ItemRevision(" + id
							+ "/" + revid + ")导出UGMaster");
					exportUGMaster(id, revid, NXExportDir.getCanonicalPath(),
							nxlog, path);
					nxlog.println();
					nxlog.println("=========================导出数模结束=========================");
				}
			}
	}

	private void exportUGMaster(String id, String revid, String filepath,
			PrintWriter uglog, String path) throws IOException, TCException,
			InterruptedException {

		File commandFile = new File(filepath, "Clone_Export.bat");
		if (commandFile.exists())
			commandFile.delete();
		commandFile.createNewFile();
		PrintWriter pw = new PrintWriter(new FileOutputStream(commandFile));
		StringBuffer buff = new StringBuffer();	
		pw.println("cd /d " + NXExportDir.getCanonicalPath() + "\\");
				 
		pw.println("set TC_ROOT=" + System.getenv("TPR"));
		pw.println("set UGII_UGMGR_COMMUNICATION=HTTP");
		pw.println("set UGII_UGMGR_HTTP_URL="+defaultServerURL);

		pw.println("set UGII_BASE_DIR=" + path);
		pw.println("set UGII_ROOT_DIR=" + path + "\\UGII\\");
		
		buff.append("\"%UGII_BASE_DIR%\\ugmanager\\ugmanager_clone.bat\"");
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
	
	
	
	private String genFileName(TCComponentItemRevision IR, String partNumber,
			String genType) throws TCException {

		String fileName = null;
		// partNumber
		String itemID = IR.getProperty("item_id");
		if (partNumber.equals("") && genType.equals("3D")) {
			
			fileName = itemID;
		} else if (!partNumber.equals("") && genType.equals("3D")) {

			//如果"."存在
			if(partNumber.indexOf(".")>0){
				String[] splitStr = partNumber.toLowerCase().split("\\.");
				
				//字符串按照"."分割，如果分割后的字符数为3
				if(splitStr.length == 3){
					
					fileName = splitStr[0]+"."+splitStr[2]+"."+splitStr[1]+"999";
					
				//字符串按照"."分割，如果分割后的字符数为4
				}else if(splitStr.length == 4){
					
					fileName = splitStr[0]+"."+splitStr[3]+"."+splitStr[1]+splitStr[2];
				
				 //字符串按照"."分割，如果分割后的字符数不是3，4
				}else{
					
					fileName = partNumber;
				}
			//如果"."不存在
			}else{
				System.out.println("不存在.  mathdataID:"+partNumber);
				fileName = partNumber;
			}
			
		} else if (partNumber.equals("") && genType.equals("2D")) {

			//fileName = itemID + "_dwg";
			fileName = itemID + ".drawing001";
		} else if (!partNumber.equals("") && genType.equals("2D")) {
			
			//如果"."存在
			if(partNumber.indexOf(".")>0){
				String[] splitStr = partNumber.toLowerCase().split("\\.");
				//字符串按照"."分割，如果分割后的字符数为3
				if(splitStr.length == 3){
					
					fileName = splitStr[0]+".drawing001"+"."+splitStr[1]+"999";
				//字符串按照"."分割，如果分割后的字符数为4
				}else if(partNumber.split(".").length == 4){
					
					fileName = splitStr[0]+".drawing001"+"."+splitStr[1]+splitStr[2];
				 //字符串按照"."分割，如果分割后的字符数不是3，4
				}else{
					
					fileName = partNumber + ".drawing001";
				}
			//如果"."不存在
			}else{
				//fileName = mathdataID + "_dwg";
				fileName = partNumber + ".drawing001";
			}
		}

		fileName = fileName
				.replaceAll(System.getProperty("line.separator"), "");
		fileName = fileName.replaceAll("\r\n", "");
		fileName = fileName.replaceAll("\n", "");

		return CommonUtils.handleSpecialChar(fileName);
	}
	
	
	

	public boolean isContainsSpec(String name)
	{
		Matcher matcher = Pattern.compile("[\\u005C/:\\u002A\\u003F\"<>\'\\u007C’‘“”：？]").matcher(name);

	    while ( matcher.find() )
	    {
	        return true;
	    }
	    
	    return false;
	}
	

}
