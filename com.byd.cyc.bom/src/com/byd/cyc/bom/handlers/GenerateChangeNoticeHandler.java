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
 * 生成更改通知单
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
				MessageBox.post("请选择DCN版本对象！", "提示", 2);
				return null;
			}
			
			TCComponentItemRevision itemRev = (TCComponentItemRevision)targetComponent;
			if (!itemRev.isTypeOf("Z9_DCNRevision")) {
				MessageBox.post("请选择DCN版本对象！", "提示", 2);
				return null;
			}
			
//			final Shell activeShell = HandlerUtil.getActiveShell(event);
			
			GenerateChangeNoticeJob generateChangeNoticeJob = new GenerateChangeNoticeJob("进度", itemRev);
			generateChangeNoticeJob.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(IJobChangeEvent event) 
				{
					GenerateChangeNoticeJob generateChangeNoticeJob = (GenerateChangeNoticeJob) event.getJob();
					if (generateChangeNoticeJob.isCompleted()) 
					{
						// 提示完成，不打开报表
						MessageBox.post("设计变更通知单生成完成！", "提示", 2);
						
						// 提示完成，点击确定后打开报表
						/*final TCComponentDataset reportDataset = generateChangeNoticeJob.getReportDataset();
						activeShell.getDisplay().asyncExec(new Runnable() 
						{							
							@Override
							public void run() 
							{
								MessageDialog.openInformation(activeShell, "提示", "设计变更通知单生成完成！");
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
