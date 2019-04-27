package com.hasco.ssdt.report.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.hasco.ssdt.report.utils.SWTUtil;

import swthelper.layout.gridforms.GridForms;

public class OutputReportDialog extends CustDialog 
{
	private ListViewer listViewer;
	private Button inDatasetButton;
	private Button exportToLocalButton;
	private Text directoryText;
	private Button browseButton;
	private Button openOnGenerateButton;	// 生成后打开
	
	private java.util.List<String> reportNameList = new ArrayList<String>();
	
	private String reportName = "";
	private boolean inDataset = false;
	private boolean exportToLocal = false;
	private boolean openOnGenerate = false;
	private String dirctoryPath;
	
	public OutputReportDialog(Shell parentShell, java.util.List<String> reportNameList) {
		super(parentShell, CUST_DIALOG_STYLE);
		this.reportNameList = reportNameList;
	}

	@Override
	public Control createContents(Composite paramComposite)
	{
		Composite localComposite = (Composite)super.createContents(paramComposite);
		Shell localShell = paramComposite.getShell();
		localShell.setText("报表输出");
		localShell.setMinimumSize(500, 400);
	    localShell.pack(true);
	    SWTUtil.centerShell(localShell);
	    return localComposite;
	}
	
	@Override
	protected Control createDialogArea(Composite paramComposite) 
	{
		Composite parentComposite = (Composite) super.createDialogArea(paramComposite);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginLeft = 5;
		gridLayout.marginRight = 5;
		gridLayout.verticalSpacing = 10;
		parentComposite.setLayout(gridLayout);
		
		TabFolder tabFolder = new TabFolder(parentComposite, 0);
		tabFolder.setBackground(parentComposite.getBackground());
		tabFolder.setLayoutData(new GridData(1808));
		
		createTemplateTabItem(tabFolder);
		createConfigTabItem(tabFolder);
		
		this.openOnGenerateButton = new Button(parentComposite, SWT.CHECK);
		this.openOnGenerateButton.setText("生成报表后打开");
		this.openOnGenerateButton.setSelection(true);
		this.openOnGenerateButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		return parentComposite;
	}
	
	private TabItem createTemplateTabItem(TabFolder tabFolder)
	{
		TabItem templateTabItem = new TabItem(tabFolder, 0);
		templateTabItem.setText("模板选择");
		Composite templateComposite = new Composite(tabFolder, 0);
		templateComposite.setBackground(tabFolder.getBackground());
		templateComposite.setLayout(new GridLayout());
		this.listViewer = new ListViewer(templateComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		this.listViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});
		this.listViewer.setContentProvider(new ArrayContentProvider());
		this.listViewer.setInput(this.reportNameList);
		List list = this.listViewer.getList();
		list.setLayoutData(new GridData(1808));
		list.setBackground(templateComposite.getBackground());
		list.select(0);
		templateTabItem.setControl(templateComposite);
		return templateTabItem;
	}

	private TabItem createConfigTabItem(TabFolder tabFolder)
	{
		TabItem configTabItem = new TabItem(tabFolder, 0);
		configTabItem.setText("配置");
		Group configGroup = new Group(tabFolder, 0);
		configGroup.setText("输出方式");
		configGroup.setBackground(tabFolder.getBackground());
		
		GridForms configGridForms = new GridForms(configGroup, "pref, fill:pref:grow, 60", "55, pref, pref, pref");
		configGridForms.setBorderWidth(10);
		
		this.inDatasetButton = new Button(configGroup, SWT.RADIO);
		this.inDatasetButton.setText("在系统中作为数据集");
		this.inDatasetButton.setSelection(true); // 默认选择
		this.inDataset = true;
		this.inDatasetButton.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (OutputReportDialog.this.inDatasetButton.getSelection()) {
					OutputReportDialog.this.inDataset = true;
					OutputReportDialog.this.directoryText.setText("");
				} else {
					OutputReportDialog.this.inDataset = false;
				}
			}
		});
		
		this.exportToLocalButton = new Button(configGroup, SWT.RADIO);
		this.exportToLocalButton.setText("导出到本地目录");
		this.exportToLocalButton.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (OutputReportDialog.this.exportToLocalButton.getSelection()) {
					OutputReportDialog.this.exportToLocal = true;
					OutputReportDialog.this.directoryText.setEnabled(true);
					OutputReportDialog.this.browseButton.setEnabled(true);
				} else {
					OutputReportDialog.this.exportToLocal = false;
					OutputReportDialog.this.directoryText.setEnabled(false);
					OutputReportDialog.this.browseButton.setEnabled(false);
				}
			}
		});
		
		this.directoryText = new Text(configGroup, SWT.BORDER | SWT.READ_ONLY);
		this.browseButton = new Button(configGroup, SWT.PUSH);
		this.browseButton.setText("...");
		this.browseButton.setToolTipText("选择导出目录");
		this.browseButton.setEnabled(false);
		this.browseButton.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell(), SWT.OPEN);
				String directoryPath = directoryDialog.open();
				if (directoryPath != null) {
					OutputReportDialog.this.directoryText.setText(directoryPath);
				}
			}
		});
		configGridForms.setComponentAt(new Label(configGroup, 0), 1, 1, 3, 1);
		configGridForms.setComponentAt(this.inDatasetButton, 1, 2, 1, 1);
		configGridForms.setComponentAt(this.exportToLocalButton, 1, 3, 1, 1);
		configGridForms.setComponentAt(this.directoryText, 1, 4, 2, 1);
		configGridForms.setComponentAt(this.browseButton, 3, 4, 1, 1);
		configGridForms.pack();
		
		configTabItem.setControl(configGroup);
		return configTabItem;
	}
	
	@Override
	protected void okPressed() 
	{
		ISelection selection = this.listViewer.getSelection();
		if (selection != null && selection instanceof IStructuredSelection) 
		{
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement == null || firstElement.toString().equals("")) {
				MessageDialog.openInformation(getShell(), "提示", "未选择模板。");
				return;
			} 
			
			this.reportName = firstElement.toString();
		}
		
		this.dirctoryPath = this.directoryText.getText();
		if (this.exportToLocal && this.dirctoryPath.equals("")) {
			MessageDialog.openInformation(getShell(), "提示", "未选择导出目录。");
			return;
		}
		
		this.openOnGenerate = this.openOnGenerateButton.getSelection();
		
		super.okPressed();
	}
	
	public String getReportName() {
		return reportName;
	}

	public boolean isInDataset() {
		return inDataset;
	}

	public boolean isExportToLocal() {
		return exportToLocal;
	}
	
	public String getDirctoryPath() {
		return dirctoryPath;
	}

	public boolean isOpenOnGenerate() {
		return openOnGenerate;
	}


	// test
	public static void main(String[] args) {
		OutputReportDialog dialog = new OutputReportDialog(new Shell(), null);
		dialog.open();
	}
	
}
