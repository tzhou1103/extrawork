package com.sokon.bopreport.customization.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import swthelper.layout.gridforms.GridForms;

import com.sokon.bopreport.customization.messages.ReportMessages;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;

/**
 * 模具、原材料选择语言及工艺文档对话框
 * 
 * @author zhoutong
 *
 */
public class ProcessReportDialog extends Dialog 
{
	/** 中文 */
	public static int SLECTION_CH_ZN = 0;
	
	/** 英文 */
	public static int SLECTION_EN_US = 1;
	
	/** 中英文 */
	public static int SLECTION_BOTH = 2;
	
	private int languageSelection = -1;
	public static TCComponentItem reportItem;
	
	private Button zh_CN_Button;
	private Button en_US_Button;
	
	public static Text text;
	private Button browseButton;
	
	public ProcessReportDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM);
		ProcessReportDialog.reportItem = null;
	}

	@Override
	protected void configureShell(Shell newShell) 
	{
		super.configureShell(newShell);
		newShell.setSize(520, 240);
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
		selectionComposite.setLayout(new GridLayout());
		selectionComposite.setLayoutData(new GridData(1808));
		
		GridForms gridForms = new GridForms(selectionComposite, "pref, pref, pref, pref, fill:pref:grow, pref", "pref, pref");
		gridForms.setBorderWidth(10);
		gridForms.setHorizontalSpacing(10);
		gridForms.setVerticalSpacing(35);
		
		Label selectLanguageLabel = new Label(selectionComposite, 0);
		selectLanguageLabel.setText(ReportMessages.getString("selectLanguage.Msg"));
		
		this.zh_CN_Button = new Button(selectionComposite, SWT.CHECK);
		this.zh_CN_Button.setText(ReportMessages.getString("Chinese.Title"));
		this.zh_CN_Button.setSelection(true);
		
		this.en_US_Button = new Button(selectionComposite, SWT.CHECK);
		this.en_US_Button.setText(ReportMessages.getString("English.Title"));
		this.en_US_Button.setSelection(true);
		
		Label updateExistingDocumentLabel = new Label(selectionComposite, 0);
		updateExistingDocumentLabel.setText(ReportMessages.getString("updatingExistingDocument.Msg"));
		
		ProcessReportDialog.text = new Text(selectionComposite, SWT.BORDER | SWT.READ_ONLY);
		
		this.browseButton = new Button(selectionComposite, SWT.PUSH);
		this.browseButton.setText(ReportMessages.getString("browse.Title"));
		this.browseButton.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				browsePressed();
			}
		});
		
		Label blankLabel_1 = new Label(selectionComposite, 0);
		blankLabel_1.setText("    ");
		
		Label blankLabel_2 = new Label(selectionComposite, 0);
		blankLabel_2.setText("            ");
		
		gridForms.setComponentAt(selectLanguageLabel, 1, 1, 1, 1);
		gridForms.setComponentAt(this.zh_CN_Button, 2, 1, 1, 1);
		gridForms.setComponentAt(blankLabel_1, 3, 1, 1, 1);
		gridForms.setComponentAt(this.en_US_Button, 4, 1, 1, 1);
		gridForms.setComponentAt(blankLabel_2, 5, 1, 1, 1);
		
		gridForms.setComponentAt(updateExistingDocumentLabel, 1, 2, 1, 1);
		gridForms.setComponentAt(ProcessReportDialog.text, 2, 2, 4, 1);
		gridForms.setComponentAt(this.browseButton, 3, 2, 1, 1);
		
		gridForms.pack();
		selectionComposite.pack();
		
		Label separatorLabel = new Label(mainComposite, 258);
		separatorLabel.setLayoutData(new GridData(768));
		separatorLabel.moveBelow(mainComposite);
		
		return mainComposite;
	}
	
	private void browsePressed()
	{
		ProcessReportDialog.reportItem = null;
		ProcessReportDialog.text.setText("");
		
		try {
			TCComponentFolder homeFolder = TcUtil.getTcSession().getUser().getHomeFolder();
			SelectDocumentCommand selectDocumentCommand = new SelectDocumentCommand(homeFolder);
			selectDocumentCommand.executeModal();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		super.okPressed();
	}

	public int getLanguageSelection() {
		return languageSelection;
	}

	public TCComponentItem getReportItem() {
		return reportItem;
	}
	
}
