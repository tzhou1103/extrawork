package com.byd.cyc.bom.generatechangenotice;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

import com.byd.cyc.bom.utils.*;
import com.teamcenter.rac.kernel.*;

/**
 * @author zhoutong
 * @version 2019-03-06：“新增”或“删除”的3D数据，也输出到更改单中
 */
public class ReportData
{
	public static final String[] TYPES_3D = new String[] { "Z9_DesignPartRevision", "Z9_StandardPartRevision" };
	
	private Map<String, TCComponent> hasImpactedItemMap = new HashMap<String, TCComponent>();
	private Map<String, TCComponent> hasSolutionItemMap = new HashMap<String, TCComponent>();
	private Map<String, String> pItemIDMap = new HashMap<String, String>();
	
	private Vector<String> addOrDeleteItemIDVector = new Vector<String>();
	
	public String dcnItemID = "";
	
	// Z9_PBYDECO
	public String supportFileNumber = "";
	public String reasonsChange = "";
	public String changeSource = "";
	public String changeMeasures = "";
	
	public Map<String, PartInfo> partInfoMap = new HashMap<String, PartInfo>();
	public Vector<TechFileInfo> techFileInfos = new Vector<TechFileInfo>();
	
	public String projectStage = "";
	
	public static Map<String, Integer> SEQUENCEMAP = new HashMap<String, Integer>();
	
	static {
		SEQUENCEMAP.put("Z9_DesignPartRevision", 1);
		SEQUENCEMAP.put("Z9_StandardPartRevision", 1);
		SEQUENCEMAP.put("Z9_DrawingRevision", 2);
		SEQUENCEMAP.put("Z9_DocumentRevision", 3);
		
		SEQUENCEMAP.put("3D", 1);
		SEQUENCEMAP.put("2D", 2);
		SEQUENCEMAP.put("TRS", 3);
	}
	
	// 针对2019-03-06的需求调整，新增
	public Vector<PartInfo> addOrDeletePartInfoVector = new Vector<PartInfo>();
	public Vector<TechFileInfo> addOrDeleteTechFileInfoVector = new Vector<TechFileInfo>();
	
	public ReportData(TCComponentItemRevision dcnItemRev, String ecoItemID, String projectStage, 
			Map<String, TCComponent> hasImpactedItemMap, Map<String, TCComponent> hasSolutionItemMap, 
			Vector<String> itemIDVector, Map<String, String> pItemIDMap) 
	{
		try {
			this.dcnItemID = dcnItemRev.getProperty("item_id");
			this.projectStage = projectStage;
			
			this.hasImpactedItemMap = hasImpactedItemMap;
			this.hasSolutionItemMap = hasSolutionItemMap;
			this.pItemIDMap = pItemIDMap;
			
			this.addOrDeleteItemIDVector.addAll(itemIDVector);
			
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		DBCon dbCon = new DBCon();
		String sql = "select * from Z9_PBYDECO where PECOID = ?";
	    Connection connection = null;
		try {
			connection = dbCon.getCon();
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, ecoItemID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet != null && resultSet.next()) 
			{
				this.supportFileNumber = resultSet.getString("PSUPPORTFILENUMBER");
				this.reasonsChange = resultSet.getString("PREASONSCHANGE");
				this.changeSource = resultSet.getString("PCHANGESOURCE");
				this.changeMeasures = resultSet.getString("PCHANGEMEASURES");
			}
			dbCon.close(resultSet, preparedStatement, null);
			
			// 变更
			Iterator<Entry<String, String>> iterator = this.pItemIDMap.entrySet().iterator();
			while (iterator.hasNext()) 
			{
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
				String itemID = entry.getKey();
				this.addOrDeleteItemIDVector.remove(itemID);
				String pITEMID = entry.getValue();
				PartInfo partInfo = this.partInfoMap.get(pITEMID);
				if (partInfo == null) {
					partInfo = new PartInfo(pITEMID, dbCon, ecoItemID, projectStage);
				}
				partInfo.changeStaus = "变更";
				updatePartInfo(partInfo, itemID);
				this.partInfoMap.put(pITEMID, partInfo);
				
				TCComponent itemRev = this.hasImpactedItemMap.get(itemID);
				if (itemRev == null) {
					itemRev = this.hasSolutionItemMap.get(itemID);
				}
				TechFileInfo techFileInfo = new TechFileInfo(itemRev, dbCon, ecoItemID, pITEMID, partInfo.objectName, itemID);
				this.techFileInfos.add(techFileInfo);
			}
			
			for (String itemID : this.addOrDeleteItemIDVector) 
			{
				String changeStatus = "删除";
				TCComponent itemRev = this.hasImpactedItemMap.get(itemID);
				if (itemRev == null) {
					changeStatus = "新增";
					itemRev = this.hasSolutionItemMap.get(itemID);
				}
				
				if (itemRev.isTypeOf(TYPES_3D)) 
				{
					PartInfo addOrDeletePartInfo = new PartInfo(itemRev, changeStatus);
					if (!this.addOrDeletePartInfoVector.contains(addOrDeletePartInfo)) {
						this.addOrDeletePartInfoVector.add(addOrDeletePartInfo);
					}
					
					String subPartInfo = addOrDeletePartInfo.itemID + "/" + addOrDeletePartInfo.objectName;
					TechFileInfo addOrDeleteTechFileInfo = new TechFileInfo(subPartInfo, "3D");
					if (!this.addOrDeleteTechFileInfoVector.contains(addOrDeleteTechFileInfo)) {
						this.addOrDeleteTechFileInfoVector.add(addOrDeleteTechFileInfo);
					}
				}
			}
		} catch (SQLException | TCException e) {
			e.printStackTrace();
		} finally {
			dbCon.close(null, null, connection);
		}
	}
	
	/**
	 * 更新partInfo数据，获取变更对象版本号信息获取
	 * 
	 * @param pITEMID
	 * @param partInfo
	 * @param dbCon
	 * @param ecoItemID
	 * @throws TCException
	 */
	private void updatePartInfo(PartInfo partInfo, String itemID) throws TCException
	{
		// 更改前版本号
		TCComponent itemRev = this.hasImpactedItemMap.get(itemID);
		if (itemRev != null) 
		{
			String itemRevisionID = itemRev.getProperty("item_revision_id");
			if (itemRev.isTypeOf(TYPES_3D)) {
				partInfo.designPartPreVersion = itemRevisionID;
				partInfo.designPartPreStatus = TcUtil.getObjectStatus(itemRev);
			} else if (itemRev.isTypeOf("Z9_DrawingRevision")) {
				partInfo.drawingPreVersion = itemRevisionID;
			} else if (itemRev.isTypeOf("Z9_DocumentRevision")) {
				partInfo.trsPreVersion = itemRevisionID;
			}
		}
		
		// 更改后版本号
		itemRev = this.hasSolutionItemMap.get(itemID);
		if (itemRev != null) 
		{
			String itemRevisionID = itemRev.getProperty("item_revision_id");
			if (itemRev.isTypeOf(TYPES_3D)) {
				partInfo.designPartPostVersion = itemRevisionID;
			} else if (itemRev.isTypeOf("Z9_DrawingRevision")) {
				partInfo.drawingPostVersion = itemRevisionID;
			} else if (itemRev.isTypeOf("Z9_DocumentRevision")) {
				partInfo.trsPostVersion = itemRevisionID;
			}
		}
	}
	

	/**
	 * @return 汇总零件信息，added by tzhou, 2019-03-06
	 */
	public Vector<PartInfo> getAllPartInfoVector()
	{
		Vector<PartInfo> vector = new Vector<PartInfo>();
		Iterator<Entry<String, PartInfo>> iterator = this.partInfoMap.entrySet().iterator();
		while (iterator.hasNext()) 
		{
			Map.Entry<String, PartInfo> entry = (Map.Entry<String, PartInfo>) iterator.next();
			PartInfo partInfo = entry.getValue();
			vector.add(partInfo);
		}
		
		vector.addAll(this.addOrDeletePartInfoVector);
		
		Collections.sort(vector, new Comparator<PartInfo>() 
		{
			@Override
			public int compare(PartInfo o1, PartInfo o2) 
			{
				if (o1.itemID.equals(o2.itemID)) {
					return o1.designPartPostVersion.compareTo(o2.designPartPostVersion);
				}
				return o1.itemID.compareTo(o2.itemID);
			}
		});
		
		return vector;
	}
	
	/**
	 * @return 汇总技术文件信息，added by tzhou, 2019-03-06
	 */
	public Vector<TechFileInfo> getAllTechFileInfoVector()
	{
		Vector<TechFileInfo> vector = new Vector<TechFileInfo>();
		vector.addAll(this.techFileInfos);
		vector.addAll(this.addOrDeleteTechFileInfoVector);
		Collections.sort(vector, new Comparator<TechFileInfo>() 
		{
			@Override
			public int compare(TechFileInfo o1, TechFileInfo o2) 
			{
				if (o1.partInfo.equals(o2.partInfo)) {
					int int_01 = SEQUENCEMAP.get(o1.changeType);
					int int_02 = SEQUENCEMAP.get(o2.changeType);
					return int_01 - int_02;
				} else {
					return o1.partInfo.compareTo(o2.partInfo);
				}
			}
		});
		
		return vector;
	}

}
