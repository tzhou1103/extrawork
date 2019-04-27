package com.zy.common;

import java.util.HashMap;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.pse.PSEApplicationPanel;
import com.teamcenter.rac.pse.pca.views.PSEVariantFormulaExpressionEditorView;

public class PreferredPerspectivePartListener implements IPerspectiveListener {
	HashMap ColNameMap = new HashMap();
	PSEApplicationPanel PSEPane = null;

	public void perspectiveActivated(IWorkbenchPage iworkbenchpage, IPerspectiveDescriptor iperspectivedescriptor) {
		System.out.println(iperspectivedescriptor.getId());
		IViewPart view[] = iworkbenchpage.getViews();
		for (int i = 0; i < view.length; i++) {
			if (view[i] instanceof PSEVariantFormulaExpressionEditorView) {
				try {
					PSEVariantFormulaExpressionEditorView view1 = (PSEVariantFormulaExpressionEditorView) view[i];

					AIFComponentContext[] context = view1.getConfigPerspective().getRelated();

					int m = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// com.teamcenter.rac.pse.pca.PSEVariantFormulaExpressionEditorView
	}

	public void perspectiveChanged(IWorkbenchPage iworkbenchpage, IPerspectiveDescriptor iperspectivedescriptor, String s) {
		// System.out.println(iperspectivedescriptor.getId());
		// System.out.println(iperspectivedescriptor.getId());
	}
}
