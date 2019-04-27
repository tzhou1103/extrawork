package com.byd.cyc.bom.generatechangenotice;

import java.sql.*;

import com.byd.cyc.bom.utils.DBCon;
import com.teamcenter.rac.kernel.*;

/**
 * @author zhoutong
 *
 */
public class TechFileInfo 
{
	public String partInfo = "";
	public String changeType = ""; 	
	public String itemID = "";
	public String objectName = "";
	
	public String preChangeDescription = "";
	public byte[] preChangeImage = new byte[0];
	public String preChangeImageType = "";
	public String postChangeDescription = "";
	public byte[] postChangeImage = new byte[0];
	public String postChangeImageType = "";
	
	public TechFileInfo(TCComponent itemRev, DBCon dbCon, String ecoItemID, String pITEMID, String pOBJECTNAME, String itemID) 
	{
		try {
			this.partInfo = pITEMID + "/" + pOBJECTNAME;
			this.itemID = itemID;
			this.objectName =  itemRev.getProperty("object_name");
			
			if (itemRev.isTypeOf(ReportData.TYPES_3D)) {
				this.changeType = "3D";
				this.objectName = pOBJECTNAME;
			} else if (itemRev.isTypeOf("Z9_DrawingRevision")) {
				this.changeType = "2D";
			} else if (itemRev.isTypeOf("Z9_DocumentRevision")) {
				this.changeType = "TRS";
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		
		// 获取更改前的图片信息
		String sql = "select PPRECHNAGEIMAGE,PRECHANGEIMAGETYPE,PPRECHANGEDESCRIPTION,PPOSTCHANGEIMAGE,POSTCHANGEIMAGETYPE,PPOSTCHNAGEDESCRIPTION from Z9_PBYDPARTCHANGE where PECOID = ? and PITEMID = ? and PCHANGETYPE = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = dbCon.getCon().prepareStatement(sql);
			preparedStatement.setString(1, ecoItemID);
			preparedStatement.setString(2, pITEMID);
			preparedStatement.setString(3, this.changeType);
			resultSet = preparedStatement.executeQuery();
			if (resultSet != null && resultSet.next()) 
			{
				this.preChangeImage = resultSet.getBytes("PPRECHNAGEIMAGE");
				this.preChangeImageType = resultSet.getString("PRECHANGEIMAGETYPE");
				this.preChangeDescription = resultSet.getString("PPRECHANGEDESCRIPTION");
				
				this.postChangeImage = resultSet.getBytes("PPOSTCHANGEIMAGE");
				this.postChangeImageType = resultSet.getString("POSTCHANGEIMAGETYPE");
				this.postChangeDescription = resultSet.getString("PPOSTCHNAGEDESCRIPTION");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			dbCon.close(resultSet, preparedStatement, null);
		}
	}

	// 2019-03-06，以下，新增
	public TechFileInfo(String partInfo, String changeType)
	{
		this.partInfo = partInfo;
		this.changeType = changeType;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changeType == null) ? 0 : changeType.hashCode());
		result = prime * result + ((partInfo == null) ? 0 : partInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TechFileInfo other = (TechFileInfo) obj;
		if (changeType == null) {
			if (other.changeType != null)
				return false;
		} else if (!changeType.equals(other.changeType))
			return false;
		if (partInfo == null) {
			if (other.partInfo != null)
				return false;
		} else if (!partInfo.equals(other.partInfo))
			return false;
		return true;
	}
	
}
