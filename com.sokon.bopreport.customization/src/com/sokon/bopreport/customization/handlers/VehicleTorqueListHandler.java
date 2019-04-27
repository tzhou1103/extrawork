package com.sokon.bopreport.customization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import com.sokon.bopreport.customization.util.SelectLanguageDialog;
import com.sokon.bopreport.customization.util.TcUtil;
import com.sokon.bopreport.customization.vehicletorquelist.VehicleTorqueListJob;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;

/**
 * 整车力矩表
 * 
 * @author chenyanhua
 *
 */
public class VehicleTorqueListHandler extends AbstractHandler 
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try 
		{
			final Registry appReg = Registry.getRegistry("com.sokon.bopreport.customization.handlers.common");
			InterfaceAIFComponent aifComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (aifComponent == null)
			{
				MessageBox.post(appReg.getString("VehicleTorqueList.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			if (!(aifComponent instanceof TCComponentBOMLine))
			{
				MessageBox.post(appReg.getString("VehicleTorqueList.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			TCComponentBOMLine targetBOMLine = (TCComponentBOMLine)aifComponent;
			
			String[] types = TcUtil.getPrefStringValues("S4CUST_VehicleTorqueList_Type");
			
			if (!Utilities.contains(targetBOMLine.getItemRevision().getType(), types))
			{
				MessageBox.post(appReg.getString("VehicleTorqueList.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			final Shell parentShell = AIFDesktop.getActiveDesktop().getShell();
			
			TCComponentItem vehicleTorqueList = findVehicleTorqueList(targetBOMLine);
			if (vehicleTorqueList == null)
			{
				SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(AIFDesktop.getActiveDesktop().getShell());
				if (Dialog.OK == selectLanguageDialog.open())
				{
					int languageType = selectLanguageDialog.getLanguageSelection();
					
					VehicleTorqueListJob vehicleTorqueListJob = new VehicleTorqueListJob(appReg.getString("Info.Title"), targetBOMLine, languageType);
					vehicleTorqueListJob.addJobChangeListener(new JobChangeAdapter() 
					{

						@Override
						public void done(IJobChangeEvent event) 
						{
							VehicleTorqueListJob vehicleTorqueListJob = (VehicleTorqueListJob)event.getJob();
							if (vehicleTorqueListJob.isFlag())
							{
//								MessageBox.post(parentShell, appReg.getString("VehicleTorqueList.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
								TcUtil.openReportDataset(parentShell, vehicleTorqueListJob.getReportDataset()); // added by zhoutong, 2018-09-28
							}
						}
						
					});
					
					vehicleTorqueListJob.setPriority(10);
					vehicleTorqueListJob.setUser(true);
					vehicleTorqueListJob.schedule();
					
				}
			}else
			{
				boolean response = MessageDialog.openConfirm(parentShell, appReg.getString("Confirm.Title"), appReg.getString("VehicleTorqueList.ConfirmUpdate.Message"));
				if (!response)
					return null;
				
				SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(parentShell);
				if (Dialog.OK == selectLanguageDialog.open())
				{
					int languageType = selectLanguageDialog.getLanguageSelection();
					
					VehicleTorqueListJob vehicleTorqueListJob = new VehicleTorqueListJob(appReg.getString("Info.Title"), targetBOMLine, languageType);
					vehicleTorqueListJob.addJobChangeListener(new JobChangeAdapter() 
					{

						@Override
						public void done(IJobChangeEvent event) 
						{
							VehicleTorqueListJob vehicleTorqueListJob = (VehicleTorqueListJob)event.getJob();
							if (vehicleTorqueListJob.isFlag())
							{
//								MessageBox.post(parentShell, appReg.getString("VehicleTorqueList.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
								TcUtil.openReportDataset(parentShell, vehicleTorqueListJob.getReportDataset()); // added by zhoutong, 2018-09-28
							}
						}
						
					});
					
					vehicleTorqueListJob.setPriority(10);
					vehicleTorqueListJob.setUser(true);
					vehicleTorqueListJob.schedule();
				}
			}
			
		} catch (Exception e) 
		{
			MessageBox.post(e);
		}
		return null;
	}
	
	private TCComponentItem findVehicleTorqueList(TCComponentBOMLine opBOMLine)
	{
		TCComponentItem vehicleTorqueList = null;
		try 
		{
			TCComponent[] comps = opBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < comps.length; i++) 
			{
//				if (comps[i].isTypeOf("S4_IT_ProcessDoc") && (((TCComponentItem)comps[i]).getLatestItemRevision().getProperty("s4_AT_DocumentType")).equals("整车力矩表"))
				if (comps[i].isTypeOf("S4_IT_ProcessDoc") 
						&& (((TCComponentItem)comps[i]).getLatestItemRevision().getStringProperty("s4_AT_DocumentType")).equals("VehicleTorqueList"))
				{
					TCComponentItem item = (TCComponentItem)comps[i];
					
					vehicleTorqueList = item;
				}
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return vehicleTorqueList;
	}
}
