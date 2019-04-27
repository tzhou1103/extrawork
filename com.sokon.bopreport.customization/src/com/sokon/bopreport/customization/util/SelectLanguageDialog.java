package com.sokon.bopreport.customization.util;

import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import swthelper.layout.gridforms.GridForms;

import com.sokon.bopreport.customization.messages.ReportMessages;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

/**
 * 语言选择对话框
 * 
 * @author zhoutong
 *
 */
public class SelectLanguageDialog extends Dialog 
{
	/** 中文 */
	public static int SLECTION_CH_ZN = 0;
	
	/** 英文 */
	public static int SLECTION_EN_US = 1;
	
	/** 中英文 */
	public static int SLECTION_BOTH = 2;
	
	private int languageSelection = -1;
	
	private Button zh_CN_Button;
	private Button en_US_Button;
	
	public SelectLanguageDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM);
	}
	
	// added by zhoutong, 2018-11-28
//	private Vector<TCComponent> variantModels = null;
	private Vector<TCComponent> variantModels = new Vector<TCComponent>();
	private boolean needVariant = false;
	private ComboViewer variantModelComboViewer;
	private TCComponent variantModel;
	
	public SelectLanguageDialog(Shell parentShell, Vector<TCComponent> variantModels) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM);
		this.variantModels = variantModels;
		this.needVariant = true;
	}

	@Override
	protected void configureShell(Shell newShell) 
	{
		super.configureShell(newShell);
		newShell.setSize(360, 240);
		if (this.needVariant) {
			newShell.setSize(360, 300);
		}
		newShell.setText(ReportMessages.getString("processReport.Title"));
		TcUtil.centerShell(newShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{		
		Composite mainComposite = (Composite) super.createDialogArea(parent);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(1808));
		
		Composite selectionComposite = new Composite(mainComposite, 0);
		selectionComposite.setLayoutData(new GridData(1808));
		
		GridForms gridForms = new GridForms(selectionComposite, "pref, pref, pref, pref", "pref, pref, pref");
		gridForms.setBorderWidth(10);
		gridForms.setHorizontalSpacing(50);
		gridForms.setVerticalSpacing(30);
		
		Label selectLanguageLabel = new Label(selectionComposite, 0);
		selectLanguageLabel.setText(ReportMessages.getString("selectLanguage.Msg"));
		
		this.zh_CN_Button = new Button(selectionComposite, SWT.CHECK);
		this.zh_CN_Button.setText(ReportMessages.getString("Chinese.Title"));
		this.zh_CN_Button.setSelection(true);
		
		this.en_US_Button = new Button(selectionComposite, SWT.CHECK);
		this.en_US_Button.setText(ReportMessages.getString("English.Title"));
		this.en_US_Button.setSelection(true);
		
		if (this.needVariant) {
			Composite variantModelComposite = createVariantModelComposite(selectionComposite, gridForms);
			gridForms.setComponentAt(variantModelComposite, 1, 3, 4, 1);
		} 
		
		gridForms.setComponentAt(selectLanguageLabel, 1, 1, 2, 1);
		gridForms.setComponentAt(this.zh_CN_Button, 2, 2, 1, 1);
		gridForms.setComponentAt(this.en_US_Button, 3, 2, 1, 1);
		gridForms.pack();
		
		Label separatorLabel = new Label(mainComposite, 258);
		separatorLabel.setLayoutData(new GridData(768));
		separatorLabel.moveBelow(mainComposite);
		
		return mainComposite;
	}
	
	private Composite createVariantModelComposite(Composite selectionComposite, GridForms gridForms)
	{
		Composite configurationComposite = new Composite(selectionComposite, 0);
		configurationComposite.setLayout(new GridLayout(2, false));
		
		Label configurationLabel = new Label(configurationComposite, 0);
		configurationLabel.setText(ReportMessages.getString("configuration.Label"));
		configurationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		this.variantModelComboViewer = new ComboViewer(configurationComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.variantModelComboViewer.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				if (element instanceof TCComponent)
				{
					TCComponent variantModel = (TCComponent)element;
					try {
						String desc = variantModel.getStringProperty("object_desc");
						String cnDesc = variantModel.getProperty("object_desc");
						if (!cnDesc.isEmpty() && !desc.equals(cnDesc)) {
							return desc + "/" + cnDesc;
						}
						return desc;
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
				return super.getText(element);
			}
		});
		this.variantModelComboViewer.setContentProvider(new IStructuredContentProvider() 
		{
			
			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
				
			}
			
			@Override
			public void dispose() {
				
			}
			
			@Override
			public Object[] getElements(Object input) 
			{
				if (input instanceof Vector<?>) {
					return ((Vector<?>) input).toArray();
				}
				return new Object[0];
			}
		});
		this.variantModelComboViewer.setInput(this.variantModels);
		this.variantModelComboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		return configurationComposite;
	}

	@Override
	protected void okPressed() 
	{
		if (!this.zh_CN_Button.getSelection()
				&& !this.en_US_Button.getSelection()) {
			MessageDialog.openInformation(getShell(), ReportMessages.getString("hint.Title"), ReportMessages.getString("atLeastSelectOneLanguage.Msg"));
			return;
		} else if (this.zh_CN_Button.getSelection()
				&& !this.en_US_Button.getSelection()) {
			this.languageSelection = SLECTION_CH_ZN;
		} else if (!this.zh_CN_Button.getSelection()
				&& this.en_US_Button.getSelection()) {
			this.languageSelection = SLECTION_EN_US;
		} else if (this.zh_CN_Button.getSelection()
				&& this.en_US_Button.getSelection()) {
			this.languageSelection = SLECTION_BOTH;
		}
		
		if (this.needVariant)
		{
			ISelection selection = this.variantModelComboViewer.getSelection();
			if (selection != null && selection instanceof IStructuredSelection) 
			{
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement != null && firstElement instanceof TCComponent) {
					this.variantModel = (TCComponent) firstElement;
				}
			}
			
			if (this.variantModel == null) {
				MessageDialog.openInformation(getShell(), ReportMessages.getString("hint.Title"), ReportMessages.getString("noVariantModelSelected.Msg"));
				return;
			}
		}
		
		super.okPressed();
	}

	public int getLanguageSelection() {
		return languageSelection;
	}
	
	public TCComponent getVariantModel() {
		return variantModel;
	}

	// test
	public static void main(String[] args) {
//		SelectLanguageDialog dialog = new SelectLanguageDialog(new Shell());
		SelectLanguageDialog dialog = new SelectLanguageDialog(new Shell(), null);
		dialog.open();
	}
	
}
