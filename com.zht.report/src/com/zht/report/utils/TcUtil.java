package com.zht.report.utils;

import java.util.Vector;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.kernel.TCUserService;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.util.MessageBox;
import com.zht.report.dialogs.ZHTConstants;

@SuppressWarnings("deprecation")
public class TcUtil 
{
	public static TCSession getTcSession()
	{
		return (TCSession) AIFUtility.getCurrentApplication().getSession();
	}
	
	public static final TCComponentDataset findTemplateDataset(String dsType, String dsName) throws TCException 
	{
		TCComponentUserType userType = (TCComponentUserType) getTcSession().getTypeComponent("User");
		TCComponentUser user = userType.find("infodba");
		if (user == null) {
			return null;
		}
		TCComponentFolder homeFolder = user.getHomeFolder();
		if (homeFolder == null) {
			return null;
		}
		AIFComponentContext[] aContext = homeFolder.getChildren("contents");
		for (AIFComponentContext context : aContext)
		{
			if ((context.getComponent() instanceof TCComponentFolder))
			{
				TCComponentFolder templatesFolder = (TCComponentFolder) context.getComponent();
				if (ZHTConstants.FIRSTFOLDER_NAME.equals(templatesFolder.getProperty("object_name")))
				{
					AIFComponentContext[] aContext1 = templatesFolder.getChildren("contents");
					for (AIFComponentContext content1 : aContext1) 
					{
						if ((content1.getComponent() instanceof TCComponentFolder)) 
						{
							TCComponentFolder templatesFolder1 = (TCComponentFolder) content1.getComponent();
							if (ZHTConstants.SECONDFOLDER_NAME.equals(templatesFolder1.getProperty("object_name"))) 
							{
								AIFComponentContext[] aContext2 = templatesFolder1.getChildren("contents");
								for (AIFComponentContext aifContext : aContext2) 
								{
									TCComponentFolder templatesFolder2 = (TCComponentFolder) aifContext.getComponent();
									if (ZHTConstants.THIRDOLDER_NAME.equals(templatesFolder2.getProperty("object_name"))) 
									{
										AIFComponentContext[] aContext3 = templatesFolder2.getChildren("contents");
										for (AIFComponentContext aifComponentContext : aContext3) {
											if ((aifComponentContext.getComponent() instanceof TCComponentDataset)) {
												TCComponentDataset dataset = (TCComponentDataset) aifComponentContext.getComponent();
												if ((dataset.isTypeOf(dsType))
														&& (dataset.getProperty("object_name").equals(dsName))) {
													return dataset;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static TCComponentForm getRevForm(TCComponentItemRevision itemRevision) throws TCException
	{
		if(itemRevision == null){
			return null;
		}
		TCComponentForm irm = (TCComponentForm) itemRevision.getRelatedComponent("IMAN_master_form_rev");
		return irm;
	}
	
	public static TCComponentItem findItem(String itemId) throws TCException
	{
		TCComponentItemType itemType = (TCComponentItemType) getTcSession().getTypeComponent("Item");
		TCComponentItem[] items = itemType.findItems(itemId);
		if (items != null && items.length > 0) {
			return items[0];
		}
		return null;
	}

	
	public static TCComponent[] queryComponents(TCSession session, String queryName, String[] keys, String[] values) 
	{
		TCComponent[] results = new TCComponent[0];
		try {
			TCComponentQueryType querytype = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			TCComponentQuery query = (TCComponentQuery) querytype.find(queryName);
			querytype.clearCache();
			if (query == null) {
				throw new RuntimeException("未定义的查询[" + queryName + "]");
			}
			TCTextService textService = session.getTextService();
			String[] keytexts = new String[keys.length];
			for (int i = 0; i < keys.length; i++) 
			{
				keytexts[i] = textService.getTextValue(keys[i]);
				if ((keytexts[i] == null) || (keytexts[i].length() == 0)) {
					keytexts[i] = keys[i];
				}
			}
			String[] valuetexts = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				valuetexts[i] = textService.getTextValue(values[i]);
			}
			results = query.execute(keytexts, valuetexts);
			query.clearCache();
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post("通过查询构建器" + queryName + "查询发生错误.", e, "错误", 1);
		}
		return results;
	}
	
	
	public static final String[] getPrefStringValues(String prefName)
	{
		TCPreferenceService preferenceService = getTcSession().getPreferenceService();
		String[] prefValues = preferenceService.getStringValuesAtLocation(prefName, TCPreferenceLocation.OVERLAY_LOCATION);
		return prefValues;
	}
	
	
	public static int getQuantity(TCComponentBOMLine paramBOMLine) throws TCException 
	{
		String bl_quantity = paramBOMLine.getStringProperty("bl_quantity");
		
		int quantity = bl_quantity.equals("") ? 1 : Integer.valueOf(bl_quantity).intValue();
		
		String structureFeature = paramBOMLine.getProperty("Z9_Structure_Feature");
		if (structureFeature.equals("F")) {
			quantity  = 0;
		} else if (structureFeature.equals("-")) {
			quantity = quantity * -1;
		} 		
		return quantity;
	}

	public static int getTotalQuantity(TCComponentBOMLine paramBOMLine) throws TCException
	{
		int quantity = getQuantity(paramBOMLine);
				
		TCComponentBOMLine parentBOMLine = paramBOMLine;
		while (parentBOMLine.parent() != null) 
		{
			parentBOMLine = parentBOMLine.parent();
			int parentQuantity = getQuantity(parentBOMLine);
			quantity *= parentQuantity;
		}
		return quantity;
	}

	
	public static final void willExpand(TCComponentBOMLine[] bomLineArray) throws Exception 
	{
		if ((bomLineArray == null) || (bomLineArray.length <= 0)) {
			return;
		}
		TcBOMService.expandOneLevel(getTcSession(), bomLineArray);
		Vector<TCComponentBOMLine> chidlrenVector = new Vector<TCComponentBOMLine>();
		for (int i = 0; i < bomLineArray.length; i++) {
			TCComponentBOMLine bomLine = bomLineArray[i];
			AIFComponentContext[] children = bomLine.getChildren();
			for (int j = 0; j < children.length; j++) {
				TCComponentBOMLine childBOMLine = (TCComponentBOMLine) children[j].getComponent();
				chidlrenVector.add(childBOMLine);
			}
		}
		willExpand(chidlrenVector.toArray(new TCComponentBOMLine[chidlrenVector.size()]));
	}
	  
	
	public static void centerShell(Shell paramShell) 
	{
		if (paramShell == null) {
			return;
		}
		Display display = paramShell.getDisplay();
		Rectangle rectangle = display.getClientArea();
		Monitor[] arrayOfMonitor = display.getMonitors();
		Monitor primaryMonitor = display.getPrimaryMonitor();
		if ((primaryMonitor != null) && (arrayOfMonitor != null)
				&& (arrayOfMonitor.length > 1)) {
			rectangle = primaryMonitor.getClientArea();
		}
		Point point = paramShell.getSize();
		paramShell.setLocation((rectangle.width - point.x) / 2, (rectangle.height - point.y) / 2);
	}
	
	
    public static boolean openByPass()
    {
        TCUserService userService = getTcSession().getUserService();
		try {
			userService.call("openByPass", new Object[] { 1 });
			return true;
		} catch (TCException e) {
			e.printStackTrace();
		}
        return false;
    }
    
    public static void closeByPass()
    {
        TCUserService userService = getTcSession().getUserService();
		try {
			userService.call("closeByPass", new Object[] { 1 });
		} catch (TCException e) {
			e.printStackTrace();
		}
    }
    
}
