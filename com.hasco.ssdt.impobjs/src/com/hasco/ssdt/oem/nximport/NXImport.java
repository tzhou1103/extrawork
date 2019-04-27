package com.hasco.ssdt.oem.nximport;
//NXImport.java/NXImportDlg.java/NXImportHandler.java����޹�
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
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class NXImport implements Runnable {

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

	public static final String NX_MappingFile_Name = "NX_import_mapping_file.txt";

	WaitingDialog wdlg = null;
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
	Map<String,TCComponentItemRevision> needToReviseItemRevs = new HashMap<String,TCComponentItemRevision>();
	String defaultServerURL = "";
	ArrayList<TCComponent> refreshObjs = new ArrayList<TCComponent>();

	public NXImport(String selectedPath, String nxBaseDir,String defaultServerURL) {

		desktop = AIFUtility.getCurrentApplication().getDesktop();
		this.app = AIFUtility.getCurrentApplication();
		this.session = (TCSession) app.getSession();
		this.nxBaseDir = nxBaseDir;
		this.userPwd = session.getCredentials().getPassword();
		this.defaultServerURL = defaultServerURL;

		try {
			this.userName = session.getUserName();
			this.userGroup = session.getCurrentGroup().getFullName();

			genAssemblyPath(selectedPath);
			nxlog = CommonUtils.mkLog(NXImportDir.getCanonicalPath(),
					"Import_log_record.txt");
			
			
			AIFComponentContext[] selectedObjects = selectedContexts = app.getTargetContexts();

		    if (selectedObjects != null) {
		      if (selectedObjects.length > 1) {
		        (new MessageBox("��ѡ�񵥸�������в���", "����", 4)).setVisible(true);
		      }
		      if (!(selectedObjects[0].getComponent() instanceof TCComponentFolder)) {
		        (new MessageBox("����ѡ��һ���ļ���", "����", 4)).setVisible(true);
		      }
		      pasterFolder = (TCComponentFolder)selectedObjects[0].getComponent();
		      
		    } else {
		    	
		    	pasterFolder = session.getUser().getNewStuffFolder();
		    }
		    
		    pasteFolderName = pasterFolder.getProperty("object_name");
		    
		    

		} catch (IOException ioe) {
			MessageBox.post(ioe);
		} catch (TCException e) {
			MessageBox.post(e);
		}

	}

	private void genAssemblyPath(String directoryPath) throws IOException {

		nxAssemblyFile = new File(directoryPath);

		NXImportDir = nxAssemblyFile.getParentFile();
		
		NXImportDirPath =NXImportDir.getCanonicalPath();

	}

	@Override
	public void run() {

		try {	
			if(hasImported()){
					
					nxlog.println("################װ��"+nxAssemblyFile.getName()+":�Ѿ����뵽Teamcenter������Ҫ�ظ�����!###########");
					nxlog.println();
					throw new CustException("װ���ظ����룬������鿴Import_log_record��־");
					
			}
			wdlg = new WaitingDialog(desktop, "ִ����", "   ����NXװ�� ......");
			wdlg.setVisible(true);
			((TCSession) app.getSession()).setStatus("��������ִ����...");

			nxlog.println("#######################׼������#########################################");
			nxlog.println("");

			prepareToExport();

			nxlog.println("#######################׼������#########################################");
			nxlog.println("");

			nxlog.println("#######################��ʼ����#########################################");
			nxlog.println("");

			startExportOperation();
			
			
			for(int k=0;k<refreshObjs.size();k++)
			{
				if(refreshObjs.get(k) instanceof TCComponentItem)
				{
					TCComponentItem tempItem = (TCComponentItem) refreshObjs.get(k);
					tempItem.refresh();
					tempItem.getLatestItemRevision().refresh();
					for(int j=0;j<tempItem.getLatestItemRevision().getChildren().length;j++)
					{
						TCComponent tempComponent =(TCComponent) tempItem.getLatestItemRevision().getChildren()[j].getComponent();
						refreshObjs.add(tempComponent);
					}
					
				}
				if(refreshObjs.get(k) instanceof TCComponentItemRevision)
				{
					TCComponentItemRevision tempItemRevision = (TCComponentItemRevision) refreshObjs.get(k);
					tempItemRevision.refresh();
					for(int j=0;j<tempItemRevision.getChildren().length;j++)
					{
						TCComponent tempComponent =(TCComponent) tempItemRevision.getChildren()[j].getComponent();
						refreshObjs.add(tempComponent);
					}
                    
				}
			}
			
			TCComponentType.refresh(refreshObjs);

			nxlog.println("#######################�������#########################################");

			if (wdlg != null)
				wdlg.setVisible(false);
			MsgBox.showM("NX����װ�����,�������ϸ����ο�import.log�ļ���", "���", MessageBox.INFORMATION);
		
			CommonUtils.ShowLog(NXImportDirPath,"import.log");
			
		} catch (CustException e) {
			e.printStackTrace();
			(new MessageBox(e.getMessage(), "����", MessageBox.ERROR)).setVisible(true);
			CommonUtils.ShowLog(NXImportDirPath,"Import_log_record.txt");

		} catch (TCException e) {
			e.printStackTrace();
			(new MessageBox(e.getMessage(), "����", MessageBox.ERROR)).setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
			(new MessageBox(e.getMessage(), "����", MessageBox.ERROR)).setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			(new MessageBox(e.getMessage(), "����", MessageBox.ERROR)).setVisible(true);

		} finally {

			if (((TCSession) app.getSession()).getStatus() != null) {
				((TCSession) app.getSession()).setReadyStatus();
			}

			if (wdlg != null)
				wdlg.setVisible(false);

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
		if (!parseDir(bomInfoMap)) {
			throw new CustException(
					"����Ĳ����������⣬�����Import_log_record��־���м��");
		}

		nxlog.println("#######################��ʼ����Mapping�ļ�##############################");
		nxlog.println("");

		buildMappingFile(bomInfoMap);

		nxlog.println("#######################����Mapping�ļ�����##############################");
		nxlog.println("");
		
		nxlog.println("#######################��ʼ������������Item##############################");
		nxlog.println("");
		
		createOrReviseItem(bomInfoMap);
		
		nxlog.println("#######################������������Item����##############################");
		nxlog.println("");

	}

	private void startExportOperation() throws IOException, TCException, InterruptedException, CustException {

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

	private TCComponentItemRevision qryItemRev(String partFileName)
			throws TCException, CustException {

		TCComponentItemRevision itemRev = null;
		String[] entry_name = new String[1];
		String[] entry_value = new String[1];

		TCComponentQueryType queryType = (TCComponentQueryType) this.session
				.getTypeComponent("ImanQuery");
		TCComponentQuery query = (TCComponentQuery) queryType
				.find("__SSDT_ItemID");
		
		if(query ==null){
			nxlog.println("#######################��ѯû���ҵ�#####################################");
			nxlog.println("");
			throw new CustException("����Ĳ����������⣬�����Import_log_record��־���м��");
		}

		entry_name[0] = "�����";
		entry_value[0] = partFileName;

		TCComponent[] results = query.execute(entry_name, entry_value);
		itemRev = getItemRevByMinID(results);

		return itemRev;

	}

	private TCComponentItemRevision getItemRevByMinID(TCComponent[] tc)
			throws TCException {

		TCComponentItemRevision outPutItemRev = null;
		if (tc.length != 0) {
			outPutItemRev = (TCComponentItemRevision) tc[0];
			String itemID = outPutItemRev.getProperty("item_id");

			for (int j = 1; j < tc.length; j++) {

				TCComponentItemRevision tempItemRev = (TCComponentItemRevision) tc[j];
				String tempItemID = tempItemRev.getProperty("item_id");
				if (itemID.compareTo(tempItemID) > 0) {
					outPutItemRev = tempItemRev;
					itemID = tempItemID;
					tempItemID = null;
				}
			}
		}
		return outPutItemRev;

	}

	private boolean parseDir(Map map) throws TCException, IOException, CustException {

        boolean isPartOk = true;
        String itemOperation = null;
		
		TCComponentItemType itemType = (TCComponentItemType) session
				.getTypeComponent("H5_OEMPart");
		
		File[] importFiles = NXImportDir.listFiles();

		for (int j = 0; j < importFiles.length; j++) {

			if (importFiles[j].getName().endsWith(".prt") && !importFiles[j].getName().contains("drawing")) {

				String partName = null;
				String qryFileName = null;
				String qryFileName2 = null;

				String itemID = null;
				String revID = null;
				String itemName = null;
				String existingData = null;

				String partFullName = importFiles[j].getName();
				partName = partFullName.substring(0, partFullName.length() - 4);

				if (!isCorrectFormat(partName)) {
					isPartOk = false;
					nxlog.println("���󣺼��������벿��:" + partFullName
							+ "####���Ʋ�����   �����.���.�汾��.prt ģʽ,���а汾��λ������6λ");
					nxlog.println();
					continue;
				}
				qryFileName = generateSearchStr(partName);

				TCComponentItemRevision itemRev = qryItemRev(qryFileName);

				if (itemRev == null) {

					qryFileName2 = partName.substring(0,
							partName.indexOf(".")) + "*";

					itemRev = qryItemRev(qryFileName2);

					if (itemRev == null) {
						itemID = itemType.getNewID();
						revID = "001";
						itemName = itemID;
						
						existingData = "$OVERWRITE_EXISTING";
						itemOperation = "create";
						
					} else {
						itemID = itemRev.getItem().getProperty("item_id");

						//update 2014.8.1
						TCComponent[] releasedList = itemRev.getItem().getLatestItemRevision()
								.getReferenceListProperty("release_status_list");
						if (releasedList.length == 0) {
							isPartOk = false;
							nxlog.println("���󣺼�����" + itemID + "####���浼��NX��ģ"
									+ partFullName + ",���ֵ�ǰ�汾��δ������##");
							nxlog.println();
							continue;
						}
						
						revID = itemRev.getItem().getNewRev();
						itemName = itemRev.getProperty("object_name");
						existingData = "$OVERWRITE_EXISTING";
						
						itemOperation = "revise";
						needToReviseItemRevs.put(itemID + "@" + revID, itemRev);
					}
				} else {
					itemID = itemRev.getItem().getProperty("item_id");
					revID = itemRev.getProperty("item_revision_id");
					itemName = itemRev.getProperty("object_name");
					existingData = "$USE_EXISTING";
					itemOperation = "none";
				}
				if (!map.containsKey(itemID + "@" + revID)) {
					Map itemMap = new HashMap();
					itemMap.put("ItemId", itemID);
					itemMap.put("RevId", revID);
					itemMap.put("ItemName", itemName);
					itemMap.put("PartId", partFullName);
					itemMap.put("ExistingData", existingData);
					itemMap.put("ItemOperation", itemOperation);
					map.put(itemID + "@" + revID, itemMap);
				}
			}
		}
		
		return isPartOk;

	}

	private void buildMappingFile(Map map) throws IOException {

		NXMappingFile = createMappingFile(NXImportDir);
		PrintWriter pw3d = new PrintWriter(new FileOutputStream(NXMappingFile));
		pw3d.println("[Defaults]");
		pw3d.println("import_folder="+pasteFolderName+"");
		pw3d.println("assoc_files=no");
		pw3d.println();

		printMappingContent(map, pw3d);
		pw3d.close();

	}

	private File createMappingFile(File directory) throws IOException {

		File mappingFile = new File(directory.getCanonicalFile(),
				NX_MappingFile_Name);

		if (mappingFile.exists())
			mappingFile.delete();
		mappingFile.createNewFile();

		return mappingFile;
	}

	private void printMappingContent(Map map, PrintWriter pw) {

		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Map content = (Map) map.get(key);

			String itemId = (String) content.get("ItemId");
			String revId = (String) content.get("RevId");
			String itemName = (String) content.get("ItemName");
			String partId = (String) content.get("PartId");
			String existingData = (String) content.get("ExistingData");

			pw.println("[" + partId + "]");
			pw.println("db_part_id=\"" + itemId + "\"");
			pw.println("db_part_rev=\"" + revId + "\"");
			pw.println("db_part_type=\"H5_OEMPart\"");
			//pw.println("db_part_name=\"" + itemName + "\"");
			pw.println("existing_data=\"" + existingData + "\"");
			pw.println("");

		}
	}
	
	private void createOrReviseItem(Map map) throws TCException
	{
		TCComponentItemType itemType = (TCComponentItemType) session
				.getTypeComponent("H5_OEMPart");
		
		refreshObjs.clear();
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Map content = (Map) map.get(key);

			String itemId = (String) content.get("ItemId");
			String revId = (String) content.get("RevId");
			String itemName = (String) content.get("ItemName");
            String itemOperation = (String) content.get("ItemOperation");
			
		    if(itemOperation.equals("revise")){
		    	
		    	//TCComponentItemRevision itemRev = needToReviseItemRevs.get(itemId + "@" + revId).getItem().revise(revId, itemName, "");
		    	TCComponentItemRevision itemRev = needToReviseItemRevs
						.get(itemId + "@" + revId).saveAs(revId, itemName, "",true, null);
		    	//copyFormPropties(needToReviseItemRevs.get(itemId + "@" + revId),itemRev);
		    	pasterFolder.add("contents", itemRev);
		    	refreshObjs.add(itemRev);
		    	
		    	itemRev.unlock();
				itemRev.refresh();
		    	nxlog.println("#######################����:"+itemId+"��"+revId+"�汾##################");
		    	nxlog.println();
		    	
		    	
		    }else if(itemOperation.equals("create")){
		    	
		    	TCComponentItem createItem=itemType.create(itemId,revId,"H5_OEMPart",itemName,"",null);
		    	pasterFolder.add("contents", createItem);
		    	refreshObjs.add(createItem);
		    	createItem.unlock();
		    	createItem.refresh();
		    
		    	nxlog.println("#######################����:"+itemId+"/"+revId+"########################");
		    	nxlog.println();
		    	
		    }
		}
	
	}

	private void importUGMasterDatasets() throws IOException, TCException,
			InterruptedException, CustException {

		for (int i = 0; i < ugMaster.size(); i++) {

			Map content = (Map) ugMaster.get(i);
			String partId = (String) content.get("PartId");

			if (partId.length() < 1)
				continue;
			
			
			nxlog.println("#######################YZ#####################################");

			nxlog.println("#######################����UGMaster#####################################");
			nxlog.println();

			importUGMaster(partId, NXImportDir.getCanonicalPath(), nxlog);

			nxlog.println("#######################����UGMaster����#################################");
			nxlog.println();

		}

	}

	private String generateSearchStr(String beforeStr){
		
		String afterStr = null;
		String splitPartNumber = null;
		String splitMark = null;
		String splitRevision = null;
		
		String[] splitStr =beforeStr.split("\\.");
		
		splitPartNumber = splitStr[0];
		splitMark = splitStr[1];
		splitRevision = splitStr[2];

		if (beforeStr.endsWith("999")) {

			afterStr = splitPartNumber + "."
					+ splitRevision.substring(0, 3) + "."
					+ splitMark.toUpperCase();

		} else {

			afterStr = splitPartNumber
					+ "."
					+ splitRevision.substring(0, 3)
					+ ".0"
					+ splitRevision.substring(3,6)+"."
					+ splitMark.toUpperCase();

		}
		
		return afterStr;
	}
	
	private void importUGMaster(String partName, String filepath,
			PrintWriter uglog) throws IOException, TCException,
			InterruptedException {

		File commandFile = new File(filepath, "UG_Master_import.bat");
		if (commandFile.exists())
			commandFile.delete();
		commandFile.createNewFile();
		PrintWriter pw = new PrintWriter(new FileOutputStream(commandFile));
		StringBuffer buff = new StringBuffer();
		String imanRoot = session.getServerConfigInfo()[3];
		String imandata = session.getServerConfigInfo()[2];
		
		pw.println("cd /d " + NXImportDir.getCanonicalPath() + "\\");
		//pw.println("set TC_ROOT=" + imanRoot);
		//pw.println("set TC_DATA=" + imandata);
		pw.println("set TC_ROOT=" + System.getenv("TPR"));
		//pw.println("set TC_DATA=" + System.getenv("TC_DATA"));	
		//pw.println("call %TC_DATA%\\tc_profilevars");
		pw.println("set UGII_UGMGR_COMMUNICATION=HTTP");
		//pw.println("set UGII_UGMGR_HTTP_URL=http://10.98.82.240:9080/tc");
		pw.println("set UGII_UGMGR_HTTP_URL="+defaultServerURL);
		
		
		pw.println("set UGII_BASE_DIR=" + nxBaseDir);
		pw.println("set UGII_ROOT_DIR=" + nxBaseDir + "\\UGII\\");

		buff.append("\"%UGII_BASE_DIR%\\ugmanager\\ug_import\"");

		buff.append(" -part=\"" + partName + "\"");
		buff.append(" -mapping=\"" + NXMappingFile.getCanonicalPath() + "\"");
		buff.append(" -u=" + userName);
		buff.append(" -p=" + userPwd);
		buff.append(" -g=" + userGroup);
		pw.print(buff.toString());
		pw.close();

		NXShell shell = new NXShell(commandFile.getCanonicalPath(), uglog);
		shell.run();

		if (shell.returnCode != 0) {
			(new MessageBox("��ִ�нű�ʱ����������:", buff.toString(), "����", MessageBox.ERROR)).setVisible(true);

			nxlog.println("#######################ִ��UG_Master_import.bat�ű����� ################");
			nxlog.println();
		} else {

			nxlog.println("#######################ִ��UG_Master_import.bat�ű�����#################");
			nxlog.println();
			
			
		}

	}

	public boolean isCorrectFormat(String value) {

		Pattern pattern = Pattern
				.compile("([\\S\\s]*)[.]([\\S\\s]*)[.]([\\S\\s]{6,})");

		Matcher mat = pattern.matcher(value);
		if (mat.find()) {
			return true;
		} else {
			return false;
		}

	}
	
	private boolean hasImported() throws TCException, CustException
	{
		
		String partId =nxAssemblyFile.getName();
		String partName = partId.substring(0, partId.length() - 4);
		
		String qryFileName = generateSearchStr(partName);

		TCComponentItemRevision itemRev = qryItemRev(qryFileName);
		
		if(itemRev == null){		
			return false;
		}
		return true;
		
	}
	
}
