package com.zht.customization.listeners;

import java.io.File;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

public class PathListener extends SelectionAdapter {
	private Text txt_filePath;

	public void setTxt_filePath(Text txt_filePath) {
		this.txt_filePath = txt_filePath;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		DirectoryDialog directoryDialog = new DirectoryDialog(e.display.getActiveShell());
		directoryDialog.open();
		txt_filePath.setText(directoryDialog.getFilterPath() + File.separator);
		txt_filePath.setEnabled(false);
		System.out.println(directoryDialog.getFilterPath() + File.separator);
	}
}
