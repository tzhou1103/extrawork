package com.byd.cyc.bom.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.handlers.HandlerUtil;

import com.byd.cyc.bom.generatechangenotice.GenerateChangeNoticeJob;
//import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.commands.open.OpenCommand;
//import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

/**
 * ���ɸ���֪ͨ��
 * 
 * @author tzhou
 * @since 2019-01-08
 */
public class GenerateChangeNoticeHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent == null || !(targetComponent instanceof TCComponentItemRevision)) {
				MessageBox.post("��ѡ��DCN�汾����", "��ʾ", 2);
				return null;
			}
			
			TCComponentItemRevision itemRev = (TCComponentItemRevision)targetComponent;
			if (!itemRev.isTypeOf("Z9_DCNRevision")) {
				MessageBox.post("��ѡ��DCN�汾����", "��ʾ", 2);
				return null;
			}
			
//			final Shell activeShell = HandlerUtil.getActiveShell(event);
			
			GenerateChangeNoticeJob generateChangeNoticeJob = new GenerateChangeNoticeJob("����", itemRev);
			generateChangeNoticeJob.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(IJobChangeEvent event) 
				{
					GenerateChangeNoticeJob generateChangeNoticeJob = (GenerateChangeNoticeJob) event.getJob();
					if (generateChangeNoticeJob.isCompleted()) 
					{
						// ��ʾ��ɣ����򿪱���
						MessageBox.post("��Ʊ��֪ͨ��������ɣ�", "��ʾ", 2);
						
						// ��ʾ��ɣ����ȷ����򿪱���
						/*final TCComponentDataset reportDataset = generateChangeNoticeJob.getReportDataset();
						activeShell.getDisplay().asyncExec(new Runnable() 
						{							
							@Override
							public void run() 
							{
								MessageDialog.openInformation(activeShell, "��ʾ", "��Ʊ��֪ͨ��������ɣ�");
								OpenCommand openCommand = new OpenCommand(AIFDesktop.getActiveDesktop(), reportDataset);
								try {
									openCommand.executeModal();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});*/
					}
				}
			});
			generateChangeNoticeJob.setPriority(10);
			generateChangeNoticeJob.setUser(true);
			generateChangeNoticeJob.schedule();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}

}
