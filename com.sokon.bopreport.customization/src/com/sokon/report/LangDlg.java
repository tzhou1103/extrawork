package com.sokon.report;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.util.MessageBox;

public class LangDlg extends Dialog {

	ExecutionEvent executionevent = null;
	public int Lang = -1;
	String TitleName = null;
	Button chinese;
	Button english;

	protected LangDlg(Shell parentShell) {
		super(parentShell);
	}

	public LangDlg(Shell parentShell, TCComponentType ItemTypeComp, ExecutionEvent executionevent) {
		this(parentShell);
		this.executionevent = executionevent;
	}

	public LangDlg(Shell parentShell, String TitleName) {
		this(parentShell);
		this.TitleName = TitleName;
	}

	protected Control createDialogArea(Composite parent) {
		Shell DlgShell = getShell();
		DlgShell.setText(TitleName);

		Composite topComp = new Composite(parent, SWT.NONE);
		topComp.setLayout(new RowLayout(SWT.VERTICAL));

		Label lab = new Label(topComp, SWT.NONE);
		lab.setText(Messages.LangDlg_SelLang);

		Composite CheckPanel = new Composite(topComp, SWT.NONE);

		GridLayout ParentGrid = new GridLayout(2, false);
		ParentGrid.marginTop = 15;
		ParentGrid.marginLeft = 50;
		ParentGrid.horizontalSpacing = 20;
		CheckPanel.setLayout(ParentGrid);

		chinese = new Button(CheckPanel, SWT.CHECK);
		chinese.setText(Messages.LangDlg_Chinese);
		english = new Button(CheckPanel, SWT.CHECK);
		english.setText(Messages.LangDlg_English);

		return topComp;

	}

	protected void okPressed() {
		boolean ch = chinese.getSelection();
		boolean en = english.getSelection();
		if (ch == false && en == false) {
			MessageBox.post(Messages.LangDlg_SelLang2, Messages.LangDlg_Infomation, 2);
			return;
		}
		if (ch && en) {
			Lang = 4;
		} else if (ch) {
			Lang = 2;
		} else if (en) {
			Lang = 1;
		}
		setReturnCode(0);
		close();
	}

	protected void cancelPressed() {
		setReturnCode(1);
		close();
	}

	protected void initializeBounds() {
		super.initializeBounds();
		Rectangle bounds = Display.getDefault().getPrimaryMonitor().getBounds();
		Rectangle rect = getShell().getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		getShell().setLocation(x, y);
	}

	protected int getShellStyle() {
		return SWT.BORDER | SWT.CLOSE | SWT.MODELESS;
	}
}
