package com.zht.customization.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.zht.customization.listeners.BT_OKMouseListener;
import com.zht.customization.listeners.BT_ViewMouseListener;

public class GenerateBOMInfoDialog extends Dialog {
	public TCComponentItemRevision itemRevL;
	public TCComponentItemRevision itemRevH;
	private Text txt_filePath;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public GenerateBOMInfoDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(3, false));

		Label lb_save = new Label(container, SWT.NONE);
		lb_save.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lb_save.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12,
				SWT.NORMAL));
		lb_save.setText("存储位置：");

		txt_filePath = new Text(container, SWT.BORDER);
		txt_filePath.setFont(SWTResourceManager.getFont("Microsoft YaHei UI",
				12, SWT.NORMAL));
		GridData gd_filePath = new GridData(SWT.FILL, SWT.FILL, true, false, 1,
				1);
		gd_filePath.widthHint = 436;
		txt_filePath.setLayoutData(gd_filePath);

		Button bt_view = new Button(container, SWT.NONE);
		BT_ViewMouseListener bt_ViewMouseListener = new BT_ViewMouseListener();
		bt_ViewMouseListener.setTxt_filePath(txt_filePath);
		bt_view.addMouseListener(bt_ViewMouseListener);
		bt_view.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12,
				SWT.NORMAL));
		bt_view.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		bt_view.setText("浏览");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button bt_ok = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, false);
		bt_ok.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12,
				SWT.NORMAL));
		bt_ok.setText("确定");
		BT_OKMouseListener bt_OKMouseListener = new BT_OKMouseListener();
		bt_OKMouseListener.setTxt_filePath(txt_filePath);
		bt_ok.addMouseListener(bt_OKMouseListener);
		Button bt_cancel = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		bt_cancel.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12,
				SWT.NORMAL));
		bt_cancel.setText("取消");
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(655, 199);
	}

	public static void main(String[] args) {
		new GenerateBOMInfoDialog(Display.getDefault().getActiveShell()).open();
	}
}
