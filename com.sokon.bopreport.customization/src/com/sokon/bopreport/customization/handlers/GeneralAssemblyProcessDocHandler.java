package com.sokon.bopreport.customization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.sokon.bopreport.customization.generalassemblyprocessdoc.NewGeneralAssemblyProcessDocJob;
import com.sokon.bopreport.customization.generalassemblyprocessdoc.NewMultipleGeneralAssemblyProcessDocJob;
import com.sokon.bopreport.customization.util.SelectLanguageDialog;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class GeneralAssemblyProcessDocHandler extends AbstractHandler 
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
				MessageBox.post(appReg.getString("GeneralAssemblyProcessDoc.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			if (!(aifComponent instanceof TCComponentBOMLine))
			{
				MessageBox.post(appReg.getString("GeneralAssemblyProcessDoc.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			TCComponentBOMLine targetBOMLine = (TCComponentBOMLine)aifComponent;
			
			TCComponentBOMLine topBOMLine = targetBOMLine.window().getTopBOMLine();
			
			if (!topBOMLine.getItem().isTypeOf("S4_IT_GAPlantBOP"))
			{
				MessageBox.post(appReg.getString("GeneralAssemblyProcessDoc.openError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			String chexingVariant = topBOMLine.getItemRevision().getProperty("s4_AT_EngineeringModel");
			
			final Shell parentShell = AIFDesktop.getActiveDesktop().getShell();
			if (targetBOMLine.getItem().isTypeOf("S4_IT_GAOP"))
			{
				TCComponentItem assemblyProcessCard = findAssemblyProcessDoc(targetBOMLine);
				if (assemblyProcessCard != null)
				{
					//是否更新总装工艺卡？
					boolean response = MessageDialog.openConfirm(parentShell, appReg.getString("Confirm.Title"), appReg.getString("GeneralAssemblyProcessDoc.ConfirmUpdate.Message"));
					if (!response)
						return null;

					SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(parentShell);
					if (Dialog.OK == selectLanguageDialog.open())
					{
						int languageType = selectLanguageDialog.getLanguageSelection();
						
						NewGeneralAssemblyProcessDocJob newGeneralAssemblyProcessDocJob = new NewGeneralAssemblyProcessDocJob(appReg.getString("Info.Title"), targetBOMLine, languageType, chexingVariant);
						newGeneralAssemblyProcessDocJob.addJobChangeListener(new JobChangeAdapter() 
						{

							@Override
							public void done(IJobChangeEvent event) 
							{
								NewGeneralAssemblyProcessDocJob newGeneralAssemblyProcessDocJob = (NewGeneralAssemblyProcessDocJob)event.getJob();
								if (newGeneralAssemblyProcessDocJob.isFlag())
								{
									MessageBox.post(parentShell, appReg.getString("GeneralAssemblyProcessDoc.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
								}
							}
							
						});
						
						newGeneralAssemblyProcessDocJob.setPriority(10);
						newGeneralAssemblyProcessDocJob.setUser(true);
						newGeneralAssemblyProcessDocJob.schedule();
					}
					
				}else
				{

					SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(AIFDesktop.getActiveDesktop().getShell());
					if (Dialog.OK == selectLanguageDialog.open())
					{
						int languageType = selectLanguageDialog.getLanguageSelection();
						
						NewGeneralAssemblyProcessDocJob newGeneralAssemblyProcessDocJob = new NewGeneralAssemblyProcessDocJob(appReg.getString("Info.Title"), targetBOMLine, languageType, chexingVariant);
						newGeneralAssemblyProcessDocJob.addJobChangeListener(new JobChangeAdapter() 
						{

							@Override
							public void done(IJobChangeEvent event) 
							{
								NewGeneralAssemblyProcessDocJob newGeneralAssemblyProcessDocJob = (NewGeneralAssemblyProcessDocJob)event.getJob();
								if (newGeneralAssemblyProcessDocJob.isFlag())
								{
									MessageBox.post(parentShell, appReg.getString("GeneralAssemblyProcessDoc.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
								}
							}
							
						});
						newGeneralAssemblyProcessDocJob.setPriority(10);
						newGeneralAssemblyProcessDocJob.setUser(true);
						newGeneralAssemblyProcessDocJob.schedule();
						
					}
				}
			}else if (targetBOMLine.getItem().isTypeOf("S4_IT_GAPlantBOP")) 
			{
				SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(AIFDesktop.getActiveDesktop().getShell());
				if (Dialog.OK == selectLanguageDialog.open())
				{
					int languageType = selectLanguageDialog.getLanguageSelection();
					
					NewMultipleGeneralAssemblyProcessDocJob newMultipleGeneralAssemblyProcessDocJob = new NewMultipleGeneralAssemblyProcessDocJob(appReg.getString("Info.Title"), targetBOMLine, languageType, chexingVariant);
					newMultipleGeneralAssemblyProcessDocJob.addJobChangeListener(new JobChangeAdapter() 
					{

						@Override
						public void done(IJobChangeEvent event) 
						{
							NewMultipleGeneralAssemblyProcessDocJob newMultipleGeneralAssemblyProcessDocJob = (NewMultipleGeneralAssemblyProcessDocJob)event.getJob();
							if (newMultipleGeneralAssemblyProcessDocJob.isFlag())
							{
								MessageBox.post(parentShell, appReg.getString("GeneralAssemblyProcessDoc.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
							}
						}
						
					});
					newMultipleGeneralAssemblyProcessDocJob.setPriority(10);
					newMultipleGeneralAssemblyProcessDocJob.setUser(true);
					newMultipleGeneralAssemblyProcessDocJob.schedule();
					
				}
				
				//NewMultipleGeneralAssemblyProcessDocJob(String name, TCComponentBOMLine gaplantOpBOMLine, int languageType, String chexingVariant)
			}else
			{
				MessageBox.post(appReg.getString("GeneralAssemblyProcessDoc.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
		} catch (Exception e) 
		{
			MessageBox.post(e);
		}
		
		return null;
	}
	
	private TCComponentItem findAssemblyProcessDoc(TCComponentBOMLine opBOMLine)
	{
		TCComponentItem assemblyProcessCard = null;
		try 
		{
			TCComponent[] comps = opBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < comps.length; i++) 
			{
//				if (comps[i].isTypeOf("S4_IT_ProcessDoc") && (((TCComponentItem)comps[i]).getLatestItemRevision().getProperty("s4_AT_DocumentType")).equals("总装_工艺卡"))
				if (comps[i].isTypeOf("S4_IT_ProcessDoc") 
						&& (((TCComponentItem)comps[i]).getLatestItemRevision().getStringProperty("s4_AT_DocumentType")).equals("GAProcessCard"))
				{
					TCComponentItem item = (TCComponentItem)comps[i];
					
					assemblyProcessCard = item;
				}
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return assemblyProcessCard;
	}

}
