package com.byd.cyc.bom.generatechangenotice;

import java.sql.*;

import com.byd.cyc.bom.utils.DBCon;
import com.byd.cyc.bom.utils.TcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

/**
 * @author zhoutong
 *
 */
public class PartInfo
{
	public String itemID = "";
	public String objectName = "";
	public String partVersionNum = "";
	public String firstClassSupply = "";
	public String weightChange = "";
	public String interChangeability = "";
	public String changesAffect = "";
	public String affectCertification = "";
	public String deliveryTime = "";
	public String submitDate = "";
	public String counterSignDrawing = "";
	public String z9memo = "";
	
	public String changeStaus = "";	// 新增、删除、变革
	
	public String designPartPreStatus = "";
	public String designPartPreVersion = "";
	public String designPartPostVersion = "";
	public String drawingPreVersion = "";
	public String drawingPostVersion = "";
	public String trsPreVersion = "";
	public String trsPostVersion = "";
	
	// 更改后需要取值
	public String internalTrial = "";
	public String suggestMethod = "";
	public String switchingTime = "";
	public String expectedSwitching = "";
	
	public PartInfo(String pITEMID, DBCon dbCon, String ecoItemID, String projectStage) 
	{
		this.itemID = pITEMID;
		
		String sql = "select * from Z9_PBYDPART where PECOID = ? and PITEMID = ?";
		try {
			PreparedStatement preparedStatement = dbCon.getCon().prepareStatement(sql);
			preparedStatement.setString(1, ecoItemID);
			preparedStatement.setString(2, pITEMID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet != null && resultSet.next()) 
			{
//				this.objectName = resultSet.getString("POBJECTNAME");
				this.objectName = resultSet.getString("PNEWOBJECTNAME"); // modified by tzhou, 2019-03-06
				this.partVersionNum = resultSet.getString("PPARTVERSIONNUM");
				this.firstClassSupply = resultSet.getString("PFIRSTCLASSSUPPLY");
				this.firstClassSupply = this.firstClassSupply.equals("0") ? "否" : "是";
				this.weightChange = resultSet.getString("PWEIGHTCHANGE");
				this.interChangeability = resultSet.getString("PINTERCHANGEABILITY");
				this.interChangeability = this.interChangeability.equals("0") ? "否" : "是";
				this.changesAffect = resultSet.getString("PCHANGEAFFECT");
				this.affectCertification = resultSet.getString("PAFFECTCERTIFICATION");
				this.affectCertification = this.affectCertification.equals("0") ? "否" : "是";
				this.deliveryTime = resultSet.getString("PDELIVERYTIME");
				this.submitDate = resultSet.getString("PSUBMITDATE");
				this.counterSignDrawing = resultSet.getString("PCOUNTSIGNDRAWING");
				this.z9memo = resultSet.getString("PZ9MEMO");
			}
			dbCon.close(resultSet, preparedStatement, null);
			
			if (projectStage.equals("afterMP")) 
			{
				sql = "select PINTERNALTRIAL,PSUGGESTMETHOD,PSWITCHINGTIME,PEXPECTEDSWITCHING from Z9_PBYDPART where PECOID = ?";
				preparedStatement = dbCon.getCon().prepareStatement(sql);
				preparedStatement.setString(1, ecoItemID);
				resultSet = preparedStatement.executeQuery();
				if (resultSet != null && resultSet.next()) 
				{
					this.internalTrial = resultSet.getString(1);
					this.suggestMethod = resultSet.getString(2);
					this.switchingTime = resultSet.getString(3);
					this.expectedSwitching = resultSet.getString(4);
				}
				dbCon.close(resultSet, preparedStatement, null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	// 2019-03-06，以下，新增
	public PartInfo(TCComponent itemRev, String changeStatus) throws TCException
	{
		String[] propNames = { "item_id", "object_name", "item_revision_id" };
		String[] propValues = itemRev.getProperties(propNames);
		if (propValues != null && propValues.length == 3) 
		{
			this.itemID = propValues[0];
			this.objectName = propValues[1];
			if (changeStatus.equals("新增")) {
				this.designPartPostVersion = propValues[2];
			} else {
				this.designPartPreVersion = propValues[2];
			}
		}
		
		this.designPartPreStatus = TcUtil.getObjectStatus(itemRev);
		this.changeStaus = changeStatus;
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changeStaus == null) ? 0 : changeStaus.hashCode());
		result = prime * result + ((designPartPostVersion == null) ? 0 : designPartPostVersion.hashCode());
		result = prime * result + ((designPartPreStatus == null) ? 0 : designPartPreStatus.hashCode());
		result = prime * result + ((itemID == null) ? 0 : itemID.hashCode());
		result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
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
		PartInfo other = (PartInfo) obj;
		if (changeStaus == null) {
			if (other.changeStaus != null)
				return false;
		} else if (!changeStaus.equals(other.changeStaus))
			return false;
		if (designPartPostVersion == null) {
			if (other.designPartPostVersion != null)
				return false;
		} else if (!designPartPostVersion.equals(other.designPartPostVersion))
			return false;
		if (designPartPreStatus == null) {
			if (other.designPartPreStatus != null)
				return false;
		} else if (!designPartPreStatus.equals(other.designPartPreStatus))
			return false;
		if (itemID == null) {
			if (other.itemID != null)
				return false;
		} else if (!itemID.equals(other.itemID))
			return false;
		if (objectName == null) {
			if (other.objectName != null)
				return false;
		} else if (!objectName.equals(other.objectName))
			return false;
		return true;
	}
	
}
