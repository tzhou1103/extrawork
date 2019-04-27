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
import com.sokon.bopreport.customization.weldingprocedurespecification.NewMultipleWeldingProcedureSpecificationJob;
import com.sokon.bopreport.customization.weldingprocedurespecification.NewWeldingProcedureSpecificationJob;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class WeldingProcedureSpecificationHandler extends AbstractHandler 
{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException 
	{
		try {
			final Registry appReg = Registry.getRegistry("com.sokon.bopreport.customization.handlers.common");
			InterfaceAIFComponent aifComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (aifComponent == null)
			{
				MessageBox.post(appReg.getString("WeldingProcedureSpecification.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			if (!(aifComponent instanceof TCComponentBOMLine))
			{
				MessageBox.post(appReg.getString("WeldingProcedureSpecification.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			TCComponentBOMLine targetBOMLine = (TCComponentBOMLine)aifComponent;
			
			TCComponentBOMLine topBOMLine = targetBOMLine.window().getTopBOMLine();
			
			if (!topBOMLine.getItem().isTypeOf("S4_IT_BAPlantBOP"))
			{
				MessageBox.post(appReg.getString("WeldingProcedureSpecification.openError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
			String chexingVariant = topBOMLine.getItemRevision().getProperty("s4_AT_EngineeringModel");
			
			final Shell parentShell = AIFDesktop.getActiveDesktop().getShell();
			if (targetBOMLine.getItem().isTypeOf("S4_IT_BAStation"))
			{
				TCComponentItem wpsItem = findWeldingProcedureSpecification(targetBOMLine);
				if (wpsItem != null)
				{
					boolean response = MessageDialog.openConfirm(parentShell, appReg.getString("Confirm.Title"), appReg.getString("WeldingProcedureSpecification.ConfirmUpdate.Message"));
					if (!response)
						return null;
					
					SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(parentShell);
					if (Dialog.OK == selectLanguageDialog.open())
					{
						int languageType = selectLanguageDialog.getLanguageSelection();
						
						NewWeldingProcedureSpecificationJob weldingProcedureSpecificationJob = new NewWeldingProcedureSpecificationJob(appReg.getString("Info.Title"), targetBOMLine, languageType, chexingVariant);
						weldingProcedureSpecificationJob.addJobChangeListener(new JobChangeAdapter() 
						{

							@Override
							public void done(IJobChangeEvent event) 
							{
								NewWeldingProcedureSpecificationJob weldingProcedureSpecificationJob = (NewWeldingProcedureSpecificationJob)event.getJob();
								if (weldingProcedureSpecificationJob.isFlag())
								{
									MessageBox.post(parentShell, appReg.getString("WeldingProcedureSpecification.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
								}
							}
							
						});
						
						weldingProcedureSpecificationJob.setPriority(10);
						weldingProcedureSpecificationJob.setUser(true);
						weldingProcedureSpecificationJob.schedule();
					}
					
				}else
				{
					SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(AIFDesktop.getActiveDesktop().getShell());
					if (Dialog.OK == selectLanguageDialog.open())
					{
						int languageType = selectLanguageDialog.getLanguageSelection();
						
						NewWeldingProcedureSpecificationJob weldingProcedureSpecificationJob = new NewWeldingProcedureSpecificationJob(appReg.getString("Info.Title"), targetBOMLine, languageType, chexingVariant);
						weldingProcedureSpecificationJob.addJobChangeListener(new JobChangeAdapter() 
						{

							@Override
							public void done(IJobChangeEvent event) 
							{
								NewWeldingProcedureSpecificationJob weldingProcedureSpecificationJob = (NewWeldingProcedureSpecificationJob)event.getJob();
								if (weldingProcedureSpecificationJob.isFlag())
								{
									MessageBox.post(parentShell, appReg.getString("WeldingProcedureSpecification.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
								}
							}
							
						});
						
						weldingProcedureSpecificationJob.setPriority(10);
						weldingProcedureSpecificationJob.setUser(true);
						weldingProcedureSpecificationJob.schedule();
					}
				}
				
				
			}else if (targetBOMLine.getItem().isTypeOf("S4_IT_BAPlantBOP"))
			{
				
				SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(AIFDesktop.getActiveDesktop().getShell());
				if (Dialog.OK == selectLanguageDialog.open())
				{
					int languageType = selectLanguageDialog.getLanguageSelection();
					
					NewMultipleWeldingProcedureSpecificationJob newMultipleWeldingProcedureSpecificationJob = new NewMultipleWeldingProcedureSpecificationJob(appReg.getString("Info.Title"), targetBOMLine, languageType, chexingVariant);
					newMultipleWeldingProcedureSpecificationJob.addJobChangeListener(new JobChangeAdapter() 
					{

						@Override
						public void done(IJobChangeEvent event) 
						{
							NewMultipleWeldingProcedureSpecificationJob newMultipleWeldingProcedureSpecificationJob = (NewMultipleWeldingProcedureSpecificationJob)event.getJob();
							if (newMultipleWeldingProcedureSpecificationJob.isFlag())
							{
								MessageBox.post(parentShell, appReg.getString("WeldingProcedureSpecification.Success.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
							}
						}
						
					});
					newMultipleWeldingProcedureSpecificationJob.setPriority(10);
					newMultipleWeldingProcedureSpecificationJob.setUser(true);
					newMultipleWeldingProcedureSpecificationJob.schedule();
					
				}
				
			}
			else 
			{
				MessageBox.post(appReg.getString("WeldingProcedureSpecification.SelectError.Message"), appReg.getString("Info.Title"), MessageBox.INFORMATION);
				return null;
			}
			
		} catch (Exception e) 
		{
			MessageBox.post(e);
		}
		
		return null;
	}
	
	private TCComponentItem findWeldingProcedureSpecification(TCComponentBOMLine opBOMLine)
	{
		TCComponentItem weldingProcedureSpecification = null;
		try 
		{
			TCComponent[] comps = opBOMLine.getItemRevision().getRelatedComponents("IMAN_reference");
			for (int i = 0; i < comps.length; i++) 
			{
//				if (comps[i].isTypeOf("S4_IT_ProcessDoc") && (((TCComponentItem)comps[i]).getLatestItemRevision().getProperty("s4_AT_DocumentType")).equals("º¸×°_¹¤ÒÕ¿¨"))
				if (comps[i].isTypeOf("S4_IT_ProcessDoc") 
						&& (((TCComponentItem)comps[i]).getLatestItemRevision().getStringProperty("s4_AT_DocumentType")).equals("BAProcessCard"))
				{
					TCComponentItem item = (TCComponentItem)comps[i];
					
					weldingProcedureSpecification = item;
				}
			}
		} catch (TCException e) 
		{
			e.printStackTrace();
		}
		return weldingProcedureSpecification;
	}

}
