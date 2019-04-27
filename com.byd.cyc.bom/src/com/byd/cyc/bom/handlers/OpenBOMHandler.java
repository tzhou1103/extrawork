package com.byd.cyc.bom.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenBOMHandler extends AbstractHandler 
{
	/**
	 * The constructor.
	 */
	public OpenBOMHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		try {
			String url = "";
			boolean getCNSelected = false;
			InterfaceAIFComponent targetComponent = AIFUtility.getCurrentApplication().getTargetComponent();
			if (targetComponent != null && targetComponent instanceof TCComponentItemRevision) 
			{
				TCComponentItemRevision itemRevision = (TCComponentItemRevision) targetComponent;
				if (itemRevision.isTypeOf("Z9_ECORevision")) {
					url = itemRevision.getProperty("z9_bomlink");
					getCNSelected = true;
				}
			}
			
			if (url != null && !url.isEmpty()) 
			{
				try {
//					Runtime.getRuntime().exec("explorer " + url);
					Runtime.getRuntime().exec("cmd /c start " + url);
				} catch (IOException e) {
					MessageBox.post("打开地址失败，错误信息为：" + e.getMessage(), "错误", 1);
				}
			} else {
				if (getCNSelected) {
					MessageBox.post("未在BOM系统中发现该变更单信息！", "提示", 2);
				}
				
				TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
				TCPreferenceService service = session.getPreferenceService();
//				url = service.getString(TCPreferenceService.TC_preference_site, "BYD_TC_BOM_URL");
				url = service.getStringValueAtLocation("BYD_TC_BOM_URL", TCPreferenceLocation.OVERLAY_LOCATION);
				if (url != null && !url.isEmpty()) 
				{
					try {
//						Runtime.getRuntime().exec("explorer " + url);
						Runtime.getRuntime().exec("cmd /c start " + url);
					} catch (IOException e) {
						MessageBox.post("打开地址失败，错误信息为：" + e.getMessage(), "错误", 1);
					}
				}  else {
					MessageBox.post("首选项 BYD_TC_BOM_URL 配置有误，请联系管理员！", "错误", 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
		return null;
	}
}
