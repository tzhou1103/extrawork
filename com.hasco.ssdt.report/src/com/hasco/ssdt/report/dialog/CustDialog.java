package com.hasco.ssdt.report.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public abstract class CustDialog extends Dialog 
{
	public static final int CUST_DIALOG_STYLE = SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER| SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation();
	
	public CustDialog(Shell parentShell) 
	{
		super(parentShell);
	}
	
	public CustDialog(Shell parentShell, int style) 
	{
		super(parentShell);
		setShellStyle(style);
	}
	
	protected Control createButtonBar(Composite paramComposite) 
	{
		Control control = super.createButtonBar(paramComposite);
		Label label = new Label(paramComposite, SWT.SEPARATOR|SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.moveAbove(control);
		return control;
	}
	
	protected Control createDialogArea(Composite paramComposite) 
	{
		Composite composite = new Composite(paramComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(paramComposite.getFont());
		Label seperatorlabel = new Label(paramComposite, SWT.SEPARATOR|SWT.HORIZONTAL);
		seperatorlabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		seperatorlabel.moveAbove(composite);
		return composite;
	}
	
	protected Label createRequiredLabel(Composite paramComposite)
	{
		Label requiredLabel = new Label(paramComposite, SWT.NONE);
	    requiredLabel.setText("*");
	    requiredLabel.setForeground(requiredLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
	    requiredLabel.setToolTipText("±ØÌîÏî");
	    return requiredLabel;
	}
}


