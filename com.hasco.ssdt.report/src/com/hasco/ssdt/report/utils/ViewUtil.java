package com.hasco.ssdt.report.utils;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.util.PlatformHelper;

public class ViewUtil
{
	@SuppressWarnings("unchecked")
	public static <T_VIEW extends IViewPart> T_VIEW findView(String paramString1)
	{
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (workbenchPage == null)
			return null;
		
		IViewPart findView = workbenchPage.findView(paramString1);
		if (findView != null) 
			return (T_VIEW) findView;
		
		// findView有时无法找到已打开的视图，故采用遍历的方式再次查找
		IViewReference[] viewReferences = workbenchPage.getViewReferences();
		if (viewReferences != null && viewReferences.length > 0) 
		{
			for (int i = viewReferences.length - 1; i >= 0; i--) {
				if (viewReferences[i].getId().equals(paramString1)) {
					return  (T_VIEW) viewReferences[i].getView(true);
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T_VIEW extends IViewPart> T_VIEW findView(String primaryID, String secondaryID) 
	{
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewReference viewReference = workbenchPage.findViewReference(primaryID, secondaryID);
		if (viewReference != null)
		{
			T_VIEW view = (T_VIEW)viewReference.getView(true);
			workbenchPage.activate(view);
			return view;
		}
		return null;
	}
	
	public static <T_VIEW extends IViewPart> T_VIEW openView(String paramString)
	{
		return openView(paramString, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T_VIEW extends IViewPart> T_VIEW openView(String paramString1, String paramString2)
	{
		try {
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (workbenchPage == null)
				return null;
			if (paramString2 == null)
				return (T_VIEW) workbenchPage.showView(paramString1);
			T_VIEW view = (T_VIEW) workbenchPage.showView(paramString1, paramString2, 3);
			workbenchPage.activate(view);
			return view;
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static IWorkbenchPart getActivePart()
	{
		IWorkbenchWindow workbenchWindow = PlatformHelper.getCurrentWorkbenchWindow();
	    if (workbenchWindow == null)
	    	return null;
	    IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
	    if (workbenchPage == null)
	    	return null;
	    return workbenchPage.getActivePart();
	}
	
}
