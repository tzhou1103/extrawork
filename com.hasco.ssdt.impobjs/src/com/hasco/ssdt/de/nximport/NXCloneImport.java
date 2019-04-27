package com.hasco.ssdt.de.nximport;
//NX数模导入：1.遍历数模所在的整个文件夹，生成map文件
/**
 * 2.生成bat文件
 * 3.执行bat文件，实现导入
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hasco.ssdt.util.CommonUtils;
import com.hasco.ssdt.util.CustException;
import com.hasco.ssdt.util.MsgBox;
import com.hasco.ssdt.util.NXShell;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class NXCloneImport implements Runnable {

	TCSession session;

	AIFDesktop desktop;
	AbstractAIFUIApplication app = null;
	AIFComponentContext[] selectedContexts = null;

	File NXImportDir;
	String NXImportDirPath = null;

	Vector ugMaster;

	Vector ugPart;

	Map noRevisionConfigured;

	String tempDir = System.getenv("temp");

	String autoType;
	PrintWriter recordnotfoundlog;

	File NXMappingFile;

	public static final String NX_MappingFile_Name = "NX_clone_mapping_file.clone";
	public static String DWG_MARK= "drawing";
	ArrayList<String> fileNameList = new ArrayList<String>();

	ProgressDialog pdlg = null;
	boolean errFlag = false;
	String userName = null;
	String userGroup = null;
	PrintWriter nxlog;
	String selectedPath = null;
	String nxBaseDir = null;
	String userPwd = null;
	File nxAssemblyFile = null;

	Map bomInfoMap;
	ArrayList<String> exportType = new ArrayList<String>();
	TCComponentFolder pasterFolder = null;
	String pasteFolderName = null;
	String defaultServerURL="";
	ArrayList<TCComponent> refreshObjs = new ArrayList<TCComponent>();
	private BufferedReader br;
	
	public NXCloneImport(String selectedPath, String nxBaseDir,String defaultServerURL) {

		desktop = AIFUtility.getCurrentApplication().getDesktop();
		this.app = AIFUtility.getCurrentApplication();
		this.session = (TCSession) app.getSession();
		this.nxBaseDir = nxBaseDir;
		this.userPwd = session.getCredentials().getPassword();
		this.defaultServerURL= defaultServerURL;
		
		try {
			this.userName = session.getUserName();
			this.userGroup = session.getCurrentGroup().getFullName();
		
			genAssemblyPath(selectedPath);
			nxlog = CommonUtils.mkLog(NXImportDir.getCanonicalPath(),
					"Import_log_record.txt");

			if(!isExistsFolder(NXImportDir.getName()))
			{
			   TCComponentFolderType folderType = (TCComponentFolderType) session.getTypeComponent("Folder");
			   pasterFolder = folderType.create(NXImportDir.getName(), "nx导入文件夹", "Folder");
			   TCComponentFolder home = this.session.getUser().getHomeFolder();
			   home.add("contents", pasterFolder);
			   home.refresh();
			}
			
			pasteFolderName = session.getUser().getProperty("user_id")+":"+NXImportDir.getName();
			

		} catch (IOException ioe) {
			MessageBox.post(ioe);
		} catch (TCException e) {
			MessageBox.post(e);
		}

	}

	private void genAssemblyPath(String directoryPath) throws IOException {

		nxAssemblyFile = new File(directoryPath);

		NXImportDir = nxAssemblyFile.getParentFile();

		NXImportDirPath = NXImportDir.getCanonicalPath();

	}

	@Override
	public void run() {

		try {
		
			pdlg = new ProgressDialog(desktop, "导入NX装配...");
			pdlg.setVisible(true);
			((TCSession) app.getSession()).setStatus("程序正在执行中...");

			nxlog.println("#######################准备导入#########################################");
			nxlog.println("");

			prepareToExport();

			nxlog.println("#######################准备结束#########################################");
			nxlog.println("");

			nxlog.println("#######################开始导入#########################################");
			nxlog.println("");

			startExportOperation();
			
			if(pasterFolder!= null){
				pasterFolder.refresh();
			}
			
			for(int k=0;k<pasterFolder.getChildrenCount();k++)
			{
				if(pasterFolder.getChildren()[k].getComponent() instanceof TCComponentItem)
				{
					TCComponentItem tempItem = (TCComponentItem) pasterFolder.getChildren()[k].getComponent();
					tempItem.refresh();
				}
			}
									
			nxlog.println("#######################导入结束#########################################");

			if (pdlg != null)
				pdlg.setVisible(false);
			MsgBox.showM("NX导入装配结束,导入具体细节请参考import.clone文件！", "完成",
					MessageBox.INFORMATION);
			
			CommonUtils.ShowLog(NXImportDirPath, "Import_log_record.txt");

		} catch (CustException e) {
			e.printStackTrace();
			MsgBox.showM(e.getMessage(), "错误", MessageBox.ERROR);	
			CommonUtils.ShowLog(NXImportDirPath, "Import_log_record.txt");

		} catch (TCException e) {
			e.printStackTrace();
			MsgBox.showM(e.getMessage(), "错误", MessageBox.ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			MsgBox.showM(e.getMessage(), "错误", MessageBox.ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			MsgBox.showM(e.getMessage(), "错误", MessageBox.ERROR);

		} finally {

			if (((TCSession) app.getSession()).getStatus() != null) {
				((TCSession) app.getSession()).setReadyStatus();
			}

			if (pdlg != null)
				pdlg.setVisible(false);

			if (nxlog != null) {
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

		addUGMaster();
		
		//将导入目录所有文件名称去除
		File[] importFiles = NXImportDir.listFiles();
		fileNameList.clear();
		for (int j = 0; j < importFiles.length; j++) {
			fileNameList.add(importFiles[j].getName());
		}
		
		if (!parseDir(bomInfoMap)) {
			throw new CustException("导入的部件存在问题，请根据Import_log_record日志进行检查");
		}

		nxlog.println("#######################开始创建Mapping文件##############################");
		nxlog.println("");

		buildMappingFile(bomInfoMap);

		nxlog.println("#######################创建Mapping文件结束##############################");
		nxlog.println("");

	}

	private void startExportOperation() throws IOException, TCException,
			InterruptedException, CustException {

		if (ugMaster.size() > 0) {
			importUGMasterDatasets();
		}

	}

	private void addUGMaster() throws TCException {

		String partId = nxAssemblyFile.getName();

		Map ugmasterMap = new HashMap();
		ugmasterMap.put("PartId", partId);

		ugMaster.add(ugmasterMap);
		
	}


	private boolean parseDir(Map map) throws TCException, IOException,
			CustException {

		boolean isPartOk = true;
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent("H5_HascoPart");

		for (int j = 0; j < fileNameList.size(); j++) {
			
			System.out.println("Count:"+fileNameList.size()+"fileNameList"+j+":"+fileNameList.get(j));
			
			if (fileNameList.get(j).endsWith(".prt")
					&& !fileNameList.get(j).contains("drawing")&& !fileNameList.get(j).contains("simulat")) {

				String partName = null;
				String itemID = null;
				String revID = null;
				String itemName = null;
	
				String partFullName = fileNameList.get(j);
				partName = partFullName.substring(0, partFullName.length()- 4);
				/*if (!isFileNameCorrectFormat(partName)) {
					isPartOk = false;
					nxlog.println("错误：检索到导入部件:" + partFullName
							+ "####名称不符合   零件号.标记.版本号.prt 模式,其中版本号位数至少6位");
					nxlog.println();
					continue;
				}*/

				itemName = partName;
				itemID = itemType.getNewID();
				revID = "001";	
				if (!map.containsKey(itemID + "@" + revID)) {
					Map itemMap = new HashMap();
					itemMap.put("ItemId", itemID);
					itemMap.put("RevId", revID);
					itemMap.put("ItemName", itemName);
					itemMap.put("PartId", partFullName);
					map.put(itemID + "@" + revID, itemMap);
				}
			}
		 }

		return isPartOk;

	}
	//生成map文件头部共同信息
	private void buildMappingFile(Map map) throws IOException {

		NXMappingFile = createMappingFile(NXImportDir);
		PrintWriter pw3d = new PrintWriter(new FileOutputStream(NXMappingFile));
		pw3d.println("Assembly Cloning Log File");
		pw3d.println("&LOG Operation_Type: IMPORT_OPERATION");
		pw3d.println("&LOG Default_Cloning_Action: USE_EXISTING"); 
		pw3d.println("&LOG Default_Cloning_Action: OVERWRITE");
		pw3d.println("&LOG Default_Container: "+pasteFolderName);
		pw3d.println("&LOG Default_Part_Name: \" \"");
		pw3d.println("&LOG Default_Part_Type: H5_HascoPart");
		pw3d.println("&LOG Default_Naming_Technique: USER_NAME");
		pw3d.println("&LOG Default_Copy_Associated_Files: No"); 
		pw3d.println("&LOG Default_Non_Master_Copy: specification Yes"); 
		pw3d.println("&LOG Default_Non_Master_Copy: manifestation Yes"); 
		pw3d.println("&LOG Default_Non_Master_Copy: altrep Yes"); 
		pw3d.println("&LOG Default_Non_Master_Copy: scenario Yes"); 
		pw3d.println("&LOG");
		printMappingContent(map, pw3d);
		pw3d.close();

	}
    //创建map文件各数模信息	
	private File createMappingFile(File directory) throws IOException {

		File mappingFile = new File(directory.getCanonicalFile(),
				NX_MappingFile_Name);

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

			String itemId = (String) content.get("ItemId");
			String revId = (String) content.get("RevId");
			String itemName = (String) content.get("ItemName");

			String partId = (String) content.get("PartId");
			pw.println("&LOG Part: \""+NXImportDir.getCanonicalPath()+"\\"+partId+"\"");
			pw.println("&LOG Cloning_Action: DEFAULT_DISP Naming_Technique: DEFAULT_NAMING Clone_Name: @DB/" + itemId + "/"+revId);
			pw.println("&LOG Part_Type:H5_HascoPart");
			pw.println("&LOG Container: "+pasteFolderName);
			pw.println("&LOG Part_Name: \"" + itemName+"\"");
		    pw.println("&LOG Part_Description: \" \"");
			pw.println("&LOG ");
			
		/*	ArrayList<String> dwgList = findDrawingFromDir(partId);
			if(dwgList.size()!=0)
			{
				for(int i=0;i<dwgList.size();i++)
				{
					pw.println("&LOG Part: \""+NXImportDir.getCanonicalPath()+"\\"+dwgList.get(i)+"\"");
					pw.println("&LOG Cloning_Action: DEFAULT_DISP Naming_Technique: DEFAULT_NAMING Clone_Name: @DB/" + itemId + "/"+revId+"/"+"specification"+"/"+dwgList.get(i).substring(0, dwgList.get(i).length() - 4));
					pw.println("&LOG Part_Type:H5_HascoPart");
					pw.println("&LOG Container: "+pasteFolderName);
					//文件名称作为Item的名称					pw.println("&LOG Part_Name: \"" +itemName+"\"");
					//db_Part_name作为描述
				    pw.println("&LOG Part_Description: \" \"");
					pw.println("&LOG ");
				}
			}*/

		}
	}

	private void importUGMasterDatasets() throws IOException, TCException,
			InterruptedException, CustException {

		for (int i = 0; i < ugMaster.size(); i++) {

			Map content = (Map) ugMaster.get(i);
			String partId = (String) content.get("PartId");

			if (partId.length() < 1)
				continue;

			nxlog.println("#######################导入UGMaster#####################################");
			nxlog.println();

			importUGMaster(partId, NXImportDir.getCanonicalPath());

			nxlog.println("#######################导入UGMaster结束#################################");
			nxlog.println();

		}

	}
	
/*	private ArrayList<String> findDrawingFromDir(String nxPartName) {

		String splitPartNumber = null;
		String splitRevision = null;
		ArrayList<String> dwgList = new ArrayList<String>();

		String[] splitStr = nxPartName.split("\\.");
		splitPartNumber = splitStr[0];
		splitRevision = splitStr[2];
		
		for(int i=0;i<fileNameList.size();i++){			
			
            Pattern p = Pattern.compile(splitPartNumber+"\\."+DWG_MARK+"([\\s\\S]*)"+splitRevision);
            Matcher m = p.matcher(fileNameList.get(i));
            if (m.find()) { 
            	 System.out.println("after match:"+fileNameList.get(i));
            	 dwgList.add(fileNameList.get(i));
            }
            
		}
		
		return dwgList;
	}*/

	private void importUGMaster(String partName, String filepath) throws IOException {
       //生成.bat文件
		File commandFile = new File(filepath, "UG_Master_clone.bat");
		if (commandFile.exists())
			commandFile.delete();
		commandFile.createNewFile();
		PrintWriter pw = new PrintWriter(new FileOutputStream(commandFile));
		StringBuffer buff = new StringBuffer();

		pw.println("cd /d " + NXImportDir.getCanonicalPath() + "\\");
		pw.println("set TC_ROOT=" + System.getenv("TPR"));
		pw.println("set UGII_UGMGR_COMMUNICATION=HTTP");
		pw.println("set UGII_UGMGR_HTTP_URL="+defaultServerURL);
		pw.println("set UGII_BASE_DIR=" + nxBaseDir);
		pw.println("set UGII_ROOT_DIR=" + nxBaseDir + "\\UGII\\");
		buff.append("\"%UGII_BASE_DIR%\\ugmanager\\ugmanager_clone\"");
		buff.append(" -u=" + userName);
		buff.append(" -p=" + userPwd);
		buff.append(" -g=" + userGroup);
		buff.append(" -o=import");
		buff.append(" -l=\"" + NXMappingFile.getCanonicalPath() + "\"");
		buff.append(" -default_n=autogen");
		pw.print(buff.toString());
		pw.close();

		
		
		
		
		NXShell shell = new NXShell(commandFile.getCanonicalPath(), nxlog);
		shell.runCmd();//执行bat文件

		nxlog.println("#######################执行UG_Master_clone.bat脚本结束#################");


	}

	public boolean isFileNameCorrectFormat(String value) {

		Pattern pattern = Pattern
				.compile("([\\S\\s]*)[.]([\\S\\s]*)[.]([\\S\\s]{6,})");

		Matcher mat = pattern.matcher(value);
		if (mat.find()) {
			return true;
		} else {
			return false;
		}

	}


	
	//获取当前用户的home文件夹，判断需要创建的文件夹是否存在
	 public  boolean isExistsFolder(String foldername)
	 {
	    int i = 0;
	    TCComponent compfolder = null;
	    try {
	      TCComponentFolder home = this.session.getUser().getHomeFolder();
	      AIFComponentContext[] children = home.getChildren();
	      for (i = 0; i < children.length; i++)
	      {
	        if (!children[i].toString().equalsIgnoreCase(foldername)){
	        	continue;
	        }
	        compfolder = (TCComponent)children[i].getComponent();
	        if (compfolder instanceof TCComponentFolder) {
	        	 if (compfolder instanceof TCComponentFolder) {
	                 this.pasterFolder = ((TCComponentFolder)compfolder);
	             }
	        	break;
	         }
	       }      
	      return i != children.length;
	    }
	    catch (TCException e)
	    {
	      e.printStackTrace();
	    }
	    return false;
	  }



}
