package com.sokon.bopreport.customization.datamodels;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

/**
 * �ؼ����� ���ݶ���
 * 
 * @author zhoutong
 * 
 */
public class CrucialProcess 
{
	public String station = "";					// ��λ
	public String processName = "";				// ��������
	public String controlEssentials = "";		// ���Ʒ���
	public String CCSC = "";					// �������Է���
	public String standard = "";				// �淶/����
	public String remark = "";					// ��ע
	
	public String processChinessName = "";		// ������������

	public CrucialProcess(TCComponentBOPLine opBopLine, int languageSelection) 
	{
		try {
			String[] propNames = { "bl_rev_object_name", "bl_rev_s4_CAT_ChineseName", "s4_BAT_ControlEssentials", "s4_BAT_CCSC", "s4_BAT_Standard", "S4_NT_Remarks" };
			String[] propValues = opBopLine.getProperties(propNames);
			if (propValues != null && propValues.length == 6) 
			{
				this.processName = TcUtil.getValueByLanguageSelection(languageSelection, propValues[0], propValues[1]);
				
				this.controlEssentials = propValues[2];
				this.CCSC = propValues[3];
				this.standard = propValues[4];
				this.remark = propValues[5];
			}
			
//			this.station = TcUtil.getWorkAreaId(opBopLine);
			// 2018-11-08, �޸Ĺ�λȡֵ
			String tempStation = TcUtil.getWorkAreaId(opBopLine) + TcUtil.getProcResArea(opBopLine);
			this.station = TcUtil.getLast7String(tempStation);
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
}
