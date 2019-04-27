package com.zht.customization.listeners;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.MessageBox;
import com.zht.customization.dialogs.GenerateBOMInfoDialog;
import com.zht.customization.dialogs.ProgressDialog;
import com.zht.customization.exception.StopMsgException;
import com.zht.customization.manager.ExcelManager;
import com.zht.customization.manager.ModelManager;
import com.zht.customization.model.ECRModel;
import com.zht.customization.utils.BOMUtil;
import com.zht.customization.utils.DatasetFinder;
import com.zht.customization.utils.SessionUtil;

public class BT_OKMouseListener extends MouseAdapter {

	private Text txt_filePath;
	private String objectString;
	private static String tempDir = System.getenv("temp");
	public static String templateName = "";
	public static String ProgressDialogTitle = "";
	private ProgressDialog progressDialog = null;
	public static GenerateBOMInfoDialog dialog = null;

	public void setTxt_filePath(Text txt_filePath) {
		this.txt_filePath = txt_filePath;
	}

	@Override
	public void mouseDown(MouseEvent e) {
		progressDialog = new ProgressDialog(-1, ProgressDialogTitle);
		progressDialog.show();
		File file = new File(txt_filePath.getText());
		if (!file.exists()) {
			MessageBox.post("路径不存在", "警告", MessageBox.WARNING);
			return;
		}

		DatasetFinder dsFinder = DatasetFinder.GetDataSetFinder(SessionUtil.GetSession());
		TCComponentDataset ds = dsFinder.FindDatasetByName(templateName, "infodba");
		if (ds == null) {
			MessageBox.post("没有找到模板文件，请联系系统管理员！", "警告", MessageBox.WARNING);
			dialog.close();
			dialog = null;
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					progressDialog.disposeDialog();
				}
			});
			return;
		}

		File templateFile = dsFinder.ExpertFileToDir(ds, "excel", templateName + ".xlsx", tempDir);

		if (templateFile == null || !templateFile.exists()) {
			MessageBox.post("模板文件导出失败，请联系系统管理员！", "警告", MessageBox.WARNING);
			dialog.close();
			dialog = null;
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					progressDialog.disposeDialog();
				}
			});
			return;
		}
		// int colNum = BOMUtil.GetSOSList().length - 4;
		// if (colNum > 0)
		// new VBUtil(templateFile.getAbsolutePath(), "车身BOM表", colNum);
		BOMUtil.invalidItem.clear();
		if (SessionUtil.GetCommand().equals("生成BOM明细表")) {
			boolean bomData = this.getBOMData();
			if (!bomData) {
				Display.getCurrent().asyncExec(new Runnable() {

					@Override
					public void run() {
						progressDialog.disposeDialog();
					}
				});
				MessageBox.post("该节点无读取权限，请联系所有者！明细表输出失败！", "失败", MessageBox.WARNING);
				return;
			}

			if (!BOMUtil.invalidItem.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (String itemString : BOMUtil.invalidItem)
					sb.append(itemString + "\r\n");
				Display.getCurrent().asyncExec(new Runnable() {

					@Override
					public void run() {
						progressDialog.disposeDialog();
					}
				});
				dialog.close();
				MessageBox.post("以下数据未发放\r\n" + sb.toString(), "警告", MessageBox.INFORMATION);
				return;
			}
		} else if (SessionUtil.GetCommand().equals("向ERP系统传递新增信息")) {
			this.getECRAddData();
			if (BOMUtil.invalidItem.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (String itemString : BOMUtil.invalidItem)
					sb.append(itemString + "\r\n");
				Display.getCurrent().asyncExec(new Runnable() {

					@Override
					public void run() {
						progressDialog.disposeDialog();
					}
				});
				dialog.close();
				MessageBox.post("以下数据未发放\r\n" + sb.toString(), "警告", MessageBox.INFORMATION);
				BOMUtil.invalidItem.clear();
				return;
			}
		} else if (SessionUtil.GetCommand().equals("向ERP系统传递更改信息")) {
			this.getECRChangeData();
		}
		try {
			ExcelManager excelManager = new ExcelManager(templateFile);
			if (SessionUtil.GetCommand().equals("生成BOM明细表")) {
				excelManager.write2Excel();
			} else
				excelManager.writeECRData2Excel();
			excelManager.saveExcel(this.txt_filePath.getText() + File.separator + objectString
					+ templateName.substring(0, templateName.length() - 2) + ".xlsx");
		} catch (IOException e1) {
			e1.printStackTrace();
			MessageBox.post("报表导出失败，请联系系统管理员", "失败", MessageBox.WARNING);
			progressDialog.disposeDialog();
			return;
		} finally {
			ModelManager.modelHList.clear();
			ModelManager.modelLList.clear();
			ModelManager.modelList.clear();
			ModelManager.quantityList.clear();
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					progressDialog.disposeDialog();
				}
			});
		}
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		MessageBox.post("报表成功导出至" + this.txt_filePath.getText(), "成功", MessageBox.INFORMATION);
		dialog.close();
		dialog = null;
	}

	@SuppressWarnings("static-access")
	private void getECRChangeData() {
		try {
			if (this.dialog.itemRevH != null && this.dialog.itemRevL != null) {
				TCProperty tcPropertyH = this.dialog.itemRevH.getTCProperty("structure_revisions");
				TCProperty tcPropertyL = this.dialog.itemRevL.getTCProperty("structure_revisions");
				if (tcPropertyH.getModelObjectArrayValue().length != 0
						&& tcPropertyL.getModelObjectArrayValue().length != 0) {
					TCComponentBOMWindow bomWindow = BOMUtil.CreateNewBOMWindow();
					// H
					TCComponentBOMLine topBOMLineH = bomWindow.setWindowTopLine(this.dialog.itemRevH.getItem(),
							this.dialog.itemRevH, null, null);
					BOMUtil.getECRBOMData(topBOMLineH, ModelManager.modelHList);
					// L
					TCComponentBOMLine topBOMLineL = bomWindow.setWindowTopLine(this.dialog.itemRevL.getItem(),
							this.dialog.itemRevL, null, null);
					BOMUtil.getECRBOMData(topBOMLineL, ModelManager.modelLList);
					bomWindow.close();
					bomWindow = null;
					ModelManager.CompareBOM();
				} else {
					ECRModel instanceL = ECRModel.GetInstance(this.dialog.itemRevL);
					instanceL.excuteData();
					instanceL.setAdd("");
					ECRModel instanceH = ECRModel.GetInstance(this.dialog.itemRevH);
					instanceH.excuteData();
					instanceH.setAdd("");
					ModelManager.modelList.add(instanceH);
					ModelManager.modelList.add(instanceL);
				}
			}
			String[] properties = this.dialog.itemRevH.getProperties(new String[] { "item_id", "item_revision_id" });
			this.objectString = properties[0] + "_" + properties[1];
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	private void getECRAddData() {
		AbstractAIFUIApplication application = SessionUtil.GetApplication();
		InterfaceAIFComponent targetComponent = application.getTargetComponent();
		TCComponentItemRevision itemRev = (TCComponentItemRevision) targetComponent;
		TCComponentBOMWindow bomWindow = BOMUtil.CreateNewBOMWindow();
		try {
			TCComponentBOMLine topBOMLine = bomWindow.setWindowTopLine(itemRev.getItem(), itemRev, null, null);
			BOMUtil.getECRBOMData(topBOMLine, ModelManager.modelList);
			bomWindow.close();
			bomWindow = null;
			String[] properties = itemRev.getProperties(new String[] { "item_id", "item_revision_id" });
			this.objectString = properties[0] + "_" + properties[1];
		} catch (TCException e) {
			System.out.println(e.getError());
			e.printStackTrace();
		}
	}

	private boolean getBOMData() {
		TCComponentBOMLine[] targetBOMLines = SessionUtil.GetTargetBOMLines();
		TCComponentBOMWindow bomWindow = BOMUtil.GetBOMWindow();
		TCComponentVariantRule vRule = null;
		try {
			vRule = bomWindow.askVariantRule().copy();
			vRule.apply(bomWindow);
			for (TCComponent sos : BOMUtil.GetSOSList()) {
				vRule.applyFullVRule(sos);
				vRule.refresh();
				ModelManager.ruleName = ((TCComponentSavedVariantRule) sos).getName();
				for (TCComponentBOMLine bomLine : targetBOMLines) {
					try {
						BOMUtil.LoopBOM(bomLine);
					} catch (StopMsgException e1) {
						return false;
					}
				}
			}
			vRule = null;
			String[] properties = bomWindow.getTopBOMLine().getItem()
					.getProperties(new String[] { "item_id", "object_String" });
			this.objectString = properties[0] + "_" + properties[1];
		} catch (TCException e1) {
			e1.printStackTrace();
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					progressDialog.disposeDialog();
				}
			});
		}
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
