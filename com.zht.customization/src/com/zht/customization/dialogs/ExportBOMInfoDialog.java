package com.zht.customization.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.zht.customization.listeners.OKListener;
import com.zht.customization.listeners.PathListener;
import com.zht.customization.utils.BOMUtil;

public class ExportBOMInfoDialog extends Dialog {

	protected Object result;
	protected Shell shlbom;
	private Table tb_sos;
	private Text txt_path;
	private Button btn_path;
	private Button btn_ok;
	@SuppressWarnings("unused")
	private Label label_1;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ExportBOMInfoDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlbom.open();
		shlbom.layout();
		Display display = getParent().getDisplay();
		while (!shlbom.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlbom = new Shell(getParent(), getStyle());
		shlbom.setSize(545, 318);
		shlbom.setText("\u5BFC\u51FABOM\u660E\u7EC6\u8868");
		shlbom.setLayout(new GridLayout(3, false));
		new Label(shlbom, SWT.NONE);
		new Label(shlbom, SWT.NONE);
		new Label(shlbom, SWT.NONE);
		new Label(shlbom, SWT.NONE);

		tb_sos = new Table(shlbom, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tb_sos.setLinesVisible(true);
		tb_sos.setHeaderVisible(true);
		GridData gd_tb_sos = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tb_sos.widthHint = 395;
		tb_sos.setLayoutData(gd_tb_sos);

		TableColumn tableColumn = new TableColumn(tb_sos, SWT.NONE);
		tableColumn.setWidth(376);
		tableColumn
				.setText("\u8BF7\u9009\u62E9\u4E0B\u5217\u914D\u7F6E\u4E2D\u76841-5\u4E2A\uFF0C\u82E5\u4E0D\u9009\u5219\u5BFC\u51FA\u5F53\u524DBOM\u660E\u7EC6\u8868");

		setTableItems(tb_sos);

		btn_ok = new Button(shlbom, SWT.NONE);
		OKListener okListener = new OKListener();
		okListener.setTable(tb_sos);

		btn_ok.addSelectionListener(okListener);
		GridData gd_btn_ok = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_btn_ok.widthHint = 63;
		btn_ok.setLayoutData(gd_btn_ok);
		btn_ok.setText("\u786E\u5B9A");

		Label lb_path = new Label(shlbom, SWT.NONE);
		lb_path.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lb_path.setText("\u5BFC\u51FA\u8DEF\u5F84\uFF1A");

		txt_path = new Text(shlbom, SWT.BORDER);
		txt_path.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		btn_path = new Button(shlbom, SWT.NONE);
		PathListener pathListener = new PathListener();
		pathListener.setTxt_filePath(txt_path);
		okListener.setTxt_filePath(txt_path);
		btn_path.addSelectionListener(pathListener);
		btn_path.setText("\u9009\u62E9\u8DEF\u5F84");
		new Label(shlbom, SWT.NONE);

		label_1 = new Label(shlbom, SWT.NONE);
		new Label(shlbom, SWT.NONE);
	}

	public void setTableItems(Table table) {
		TCComponent[] getSOSList = BOMUtil.GetSOSList();
		for (TCComponent comp : getSOSList) {
			String name = ((TCComponentSavedVariantRule) comp).getName();
			new TableItem(table, SWT.NONE).setText(name);
		}
	}

	public void close() {
		Display.getCurrent().getDefault().syncExec(new Runnable() {
			public void run() {
				shlbom.close();
				shlbom.dispose();
			}
		});
	}

	public static void main(String[] args) {
		Shell shell2 = new Shell(new Display());
		ExportBOMInfoDialog exportBOMInfoDialog = new ExportBOMInfoDialog(
				shell2, 64);
		exportBOMInfoDialog.open();
		exportBOMInfoDialog.getParent().close();
	}
}
