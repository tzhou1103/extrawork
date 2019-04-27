package com.zht.customization.listeners;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentSavedVariantRule;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.zht.customization.dialogs.ExportBOMInfoDialog;
import com.zht.customization.dialogs.ProgressDialog;
import com.zht.customization.exception.StopMsgException;
import com.zht.customization.manager.ExcelManager;
import com.zht.customization.manager.ModelManager;
import com.zht.customization.model.BOMNode;
import com.zht.customization.utils.BOMUtil;
import com.zht.customization.utils.DatasetFinder;
import com.zht.customization.utils.SessionUtil;

public class OKListener extends SelectionAdapter {
	private Text txt_filePath;
	private Table table;
	private ProgressDialog progressDialog = null;
	public static ExportBOMInfoDialog dialog = null;

	private String templateName = "BOM明细表模板";
	private static String tempDir = System.getenv("temp");
	public static TCComponentSavedVariantRule[] rules = null;
	public static TCComponentSavedVariantRule currentRule = null;

	public void setTxt_filePath(Text txt_filePath) {
		this.txt_filePath = txt_filePath;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				ModelManager.modelHList.clear();
				ModelManager.modelLList.clear();
				ModelManager.modelList.clear();
				ModelManager.quantityList.clear();
				File file = new File(txt_filePath.getText());
				if (!file.exists()) {
					MessageBox.post("路径不存在", "警告", MessageBox.WARNING);
					return;
				}

				if (table.getSelectionCount() > 5) {
					MessageBox.post("选择的配置不能大于五个", "警告", MessageBox.WARNING);
					return;
				}

				int[] selectionIndices = table.getSelectionIndices();
				rules = new TCComponentSavedVariantRule[selectionIndices.length];
				int index = 0;
				for (int i : selectionIndices) {
					rules[index] = (TCComponentSavedVariantRule) BOMUtil.GetSOSList()[i];
					index++;
				}
				progressDialog = new ProgressDialog(-1, "正在导出BOM明细表");
				progressDialog.show();
				BOMUtil.invalidItem.clear();

				DatasetFinder dsFinder = DatasetFinder.GetDataSetFinder(SessionUtil.GetSession());
				TCComponentDataset ds = dsFinder.FindDatasetByName(templateName, "infodba");
				if (ds == null) {
					MessageBox.post("没有找到模板文件，请联系系统管理员！", "警告", MessageBox.WARNING);
					dialog.close();
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
					Display.getCurrent().asyncExec(new Runnable() {

						@Override
						public void run() {
							progressDialog.disposeDialog();
						}
					});
					return;
				}
				TCComponentBOMLine[] targetBOMLines = SessionUtil.GetTargetBOMLines();
				TCComponentBOMWindow bomWindow = BOMUtil.GetBOMWindow();
				TCComponentVariantRule vRule = null, origin = null;
				String objectString = "";
				try {
					BOMNode root = new BOMNode();
					ModelManager.root = root;
					ModelManager.ruleName = "";
					if (rules.length != 1) {
						TCComponentRevisionRule revisionRule = bomWindow.getRevisionRule();
						TCComponentBOMWindowType typeComponent = (TCComponentBOMWindowType) SessionUtil.GetSession()
								.getTypeComponent("BOMWindow");
						TCComponentBOMWindow newBomWindow = typeComponent.create(revisionRule);
						TCComponentBOMLine topline = newBomWindow.setWindowTopLine(bomWindow.getTopBOMLine().getItem(),
								bomWindow.getTopBOMLine().getItemRevision(), null, null);
						newBomWindow.showUnconfiguredChanges(true);
//						System.out.println("sos:" + ModelManager.ruleName);
						try {
							BOMUtil.loopBOM(topline, root);
						} catch (StopMsgException e1) {
							newBomWindow.close();
							Display.getCurrent().asyncExec(new Runnable() {

								@Override
								public void run() {
									progressDialog.disposeDialog();
								}
							});
							MessageBox.post("该节点无读取权限，请联系所有者！明细表输出失败！", "失败", MessageBox.WARNING);					
						}
						newBomWindow.close();
						if(rules.length==0){
							if (!BOMUtil.invalidItem.isEmpty()) {
								StringBuilder sb = new StringBuilder();
//								sb.append(ModelManager.ruleName + "\r\n");
								for (String itemString : BOMUtil.invalidItem)
									sb.append(itemString + "\r\n");
								Display.getCurrent().asyncExec(new Runnable() {

									@Override
									public void run() {
										progressDialog.disposeDialog();
									}
								});
								dialog.close();
								MessageBox.post(sb.toString(), "警告", MessageBox.INFORMATION);
								return;
							}
						}
					}

					System.out.println("BOMUtil.invalidItem:" + BOMUtil.invalidItem.size());
					BOMUtil.invalidItem.clear();
					vRule = bomWindow.askVariantRule().copy();
					for (TCComponentSavedVariantRule sos : rules) {
//						bomWindow.clearCache();
//						vRule.clearCache();
						bomWindow.getTopBOMLine().clearCache();
						System.out.println(sos.getName());
						vRule.applyFullVRule(sos);
						vRule.apply(bomWindow);
//						vRule.destroy();
						bomWindow.refresh();
						ModelManager.ruleName = sos.getName();
//						System.out.println("sos:" + ModelManager.ruleName);
						boolean error = false;
						for (TCComponentBOMLine bomLine : targetBOMLines) {
							try {
								if (rules.length == 1)
									BOMUtil.loopBOM2(bomWindow.getTopBOMLine(), root);
								else
									BOMUtil.LoopBOM2(bomWindow.getTopBOMLine(), root.children);
							} catch (StopMsgException e1) {
								error = true;
								break;
							}
						}
						if (error) {
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
							sb.append(ModelManager.ruleName + "\r\n");
							for (String itemString : BOMUtil.invalidItem)
								sb.append(itemString + "\r\n");
							Display.getCurrent().asyncExec(new Runnable() {

								@Override
								public void run() {
									progressDialog.disposeDialog();
								}
							});
							dialog.close();
							MessageBox.post(sb.toString(), "警告", MessageBox.INFORMATION);
							return;
						}
						ModelManager.ruleName = "";
					}
					// bomWindow.clearCache();
					// vRule.clearCache();
					// vRule.applyFullVRule(origin);
					// vRule.refresh();
					// bomWindow.refresh();
					String[] properties = bomWindow.getTopBOMLine().getItem()
							.getProperties(new String[] { "item_id", "object_String" });
					objectString = properties[0] + "_" + properties[1];
				} catch (TCException e1) {
					e1.printStackTrace();
					Display.getCurrent().asyncExec(new Runnable() {

						@Override
						public void run() {
							progressDialog.disposeDialog();
						}
					});
				}
				try {
					ExcelManager excelManager = new ExcelManager(templateFile);
					excelManager.write2Excel();
					excelManager.writeSOSNames();
					excelManager.saveExcel(txt_filePath.getText() + File.separator + objectString
							+ templateName.substring(0, templateName.length() - 2) + ".xlsx");
				} catch (IOException e1) {
					e1.printStackTrace();
					MessageBox.post("报表导出失败，请联系系统管理员", "失败", MessageBox.WARNING);
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
				MessageBox.post("报表成功导出至" + txt_filePath.getText(), "成功", MessageBox.INFORMATION);
				dialog.close();
			}
		});
		
			
	}
}
