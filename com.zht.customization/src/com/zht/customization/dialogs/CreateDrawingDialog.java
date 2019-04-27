package com.zht.customization.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ExtendedAttributes;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.zht.customization.utils.SessionUtil;

public class CreateDrawingDialog extends Dialog {
	private Text text;
	private Text text_1;
	private TCComponentItemRevision itemRevision;
	private String drawing_item_id = "";

	private CreateDrawingDialog dialog;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public CreateDrawingDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setItemRevision(TCComponentItemRevision itemRevision) {
		this.itemRevision = itemRevision;
		dialog = this;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 4;
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label lblId = new Label(container, SWT.NONE);
		lblId.setAlignment(SWT.CENTER);
		GridData gd_lblId = new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1);
		gd_lblId.widthHint = 29;
		lblId.setLayoutData(gd_lblId);
		lblId.setText("ID");

		text = new Text(container, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 239;
		text.setLayoutData(gd_text);
		text.setEnabled(false);

		Button button = new Button(container, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO
				String[] properties = null;
				try {
					properties = itemRevision.getProperties(new String[] {
							"item_id", "item_revision_id" });
				} catch (TCException e1) {
					e1.printStackTrace();
				}
				String item_id = properties[0];
				String item_revision_id = properties[1];
				drawing_item_id = item_id + "_" + item_revision_id + "-DWG";
				text.setText(drawing_item_id);
			}
		});
		GridData gd_button = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_button.widthHint = 66;
		button.setLayoutData(gd_button);
		button.setText("\u6307\u6D3E");
		new Label(container, SWT.NONE);

		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblNewLabel.setText("\u540D\u79F0");

		text_1 = new Text(container, SWT.BORDER);
		GridData gd_text_1 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1,
				1);
		gd_text_1.widthHint = 239;
		text_1.setLayoutData(gd_text_1);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		label.setAlignment(SWT.CENTER);
		label.setText("\u63CF\u8FF0");

		final StyledText styledText = new StyledText(container, SWT.BORDER);
		GridData gd_styledText = new GridData(SWT.LEFT, SWT.FILL, true, true,
				1, 1);
		gd_styledText.widthHint = 241;
		styledText.setLayoutData(gd_styledText);
		// new Label(container, SWT.NONE);

		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					TCComponentItemType typeComponent = (TCComponentItemType) SessionUtil
							.GetSession().getTypeComponent("Item");
					TCComponentItem[] findItems = typeComponent
							.findItems(drawing_item_id);
					if (findItems.length > 0) {
						MessageBox.post(drawing_item_id + "已存在", "警告",
								MessageBox.WARNING);
						return;
					}
				} catch (TCException e2) {
					e2.printStackTrace();
				}
				if(text_1.getText().equals("")){
					MessageBox.post("请填写名称", "警告",
							MessageBox.WARNING);
					return;
				}
				if (drawing_item_id != null && !drawing_item_id.equals("")) {
					// Create item
					try {
						HashMap<String, String> hashMap = new HashMap<String, String>();
						System.out.println( styledText.getText());
						hashMap.put("object_desc", styledText.getText());
						TCComponentItem item = createItemsInFolder(SessionUtil.GetSession(),
								"Z9_Drawing", drawing_item_id,
								text_1.getText(), "001",
								"Z9_DrawingRevision");
						TCComponentFolder homeFolder = SessionUtil.GetSession().getUser().getHomeFolder();
						homeFolder.add("contents", item);
						item.setProperty("object_desc", styledText.getText());
					} catch (TCException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					dialog.close();
				} else {
					MessageBox.post("请填写ID", "警告",
							MessageBox.WARNING);
					return;
				}
			}
		});
		GridData gd_btnNewButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_btnNewButton.widthHint = 62;
		gd_btnNewButton.heightHint = 29;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("\u521B\u5EFA");
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	public static TCComponentItem createItemsInFolder(TCSession session, String itemType,
			String item_id, String itemName, String rev_id,String rev_obj_type) throws Exception {
		DataManagementService dmService = DataManagementService
				.getService(session);
		ItemProperties[] itemProps = new ItemProperties[1];
		ItemProperties itemProperty = new ItemProperties();
		ExtendedAttributes[] extpros = new ExtendedAttributes[1];
		extpros[0] = new ExtendedAttributes();
		extpros[0].objectType = rev_obj_type;
		itemProperty.itemId = item_id;
		itemProperty.name = itemName;
		itemProperty.type = itemType;
		itemProperty.revId = rev_id;
		itemProperty.extendedAttributes = extpros;
		itemProps[0] = itemProperty;
		CreateItemsResponse response = dmService.createItems(itemProps,
				null, "");
		if (response.serviceData.sizeOfPartialErrors() > 0) {
			throw new Exception(response.serviceData.getPartialError(0)
					.getMessages()[0]);
		}

		CreateItemsOutput[] out = response.output;
		TCComponentItem item = out[0].item;
		return item;
	}

}
