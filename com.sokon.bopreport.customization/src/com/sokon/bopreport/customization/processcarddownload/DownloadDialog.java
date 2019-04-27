package com.sokon.bopreport.customization.processcarddownload;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import swthelper.layout.gridforms.GridForms;

import com.sokon.bopreport.customization.util.TcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * ÏÂÔØ¶Ô»°¿ò
 * 
 * @author zhoutong
 *
 */
public class DownloadDialog extends Dialog 
{
	private Registry registry;
	private Text pathText;
	private Button okButton;
	private String path;
	
	public DownloadDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(1264);
		this.registry = Registry.getRegistry(this);
	}

	@Override
	protected void configureShell(Shell newShell) 
	{
		super.configureShell(newShell);
		newShell.setSize(500, 220);
		newShell.setMinimumSize(440, 220);
		newShell.setText(this.registry.getString("download.Text"));
		TcUtil.centerShell(newShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{		
		Composite mainComposite = (Composite) super.createDialogArea(parent);
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(1808));
		
		Composite downloadComposite = new Composite(mainComposite, 0);
		downloadComposite.setLayoutData(new GridData(1808));
		
		GridForms gridForms = new GridForms(downloadComposite, "pref, fill:pref:grow, pref", "pref");
		gridForms.setBorderWidth(5);
		gridForms.setHorizontalSpacing(5);
		
		Label pathLabel = new Label(downloadComposite, 0);
		pathLabel.setText(this.registry.getString("downloadPath.Label"));
		
		this.pathText = new Text(downloadComposite, SWT.BORDER | SWT.READ_ONLY);
		this.pathText.addModifyListener(new ModifyListener() 
		{			
			@Override
			public void modifyText(ModifyEvent modifyEvent) 
			{
				String pathText = DownloadDialog.this.pathText.getText().trim();
				if (!pathText.equals("")) {
					DownloadDialog.this.okButton.setEnabled(true);
				} else {
					DownloadDialog.this.okButton.setEnabled(false);
				}
			}
		});
		
		Button browseButton = new Button(downloadComposite, SWT.PUSH);
		browseButton.setText(this.registry.getString("browse.Text"));
		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell(), SWT.OPEN);
				String directoryPath = directoryDialog.open();
				if (directoryPath != null) {
					DownloadDialog.this.pathText.setText(directoryPath);
				}
			}
		});
		
		gridForms.setComponentAt(pathLabel, 1, 1, 1, 1);
		gridForms.setComponentVerticalAlignment(pathLabel, 1);
		gridForms.setComponentAt(this.pathText, 2, 1, 1, 1);
		gridForms.setComponentAt(browseButton, 3, 1, 1, 1);
		
		gridForms.pack();
		
		Label separatorLabel = new Label(mainComposite, 258);
		separatorLabel.setLayoutData(new GridData(768));
		separatorLabel.moveBelow(mainComposite);
		
		return mainComposite;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		super.createButtonsForButtonBar(parent);
		this.okButton = getButton(IDialogConstants.OK_ID);
		this.okButton.setEnabled(false);
	}

	@Override
	protected void okPressed() 
	{
		this.path = this.pathText.getText();
		super.okPressed();
	}

	public String getPath() {
		return path;
	}
	
}
