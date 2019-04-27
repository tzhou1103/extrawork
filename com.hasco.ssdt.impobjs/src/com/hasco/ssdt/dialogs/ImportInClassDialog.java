package com.hasco.ssdt.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import jxl.read.biff.BiffException;

import com.hasco.ssdt.util.ExcelWriter;
import com.hasco.ssdt.util.LogAppend;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.classification.common.G4MUserAppContext;
import com.teamcenter.rac.classification.icm.ClassificationService;
import com.teamcenter.rac.kernel.TCClassificationService;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.ics.ICSAdminClass;
import com.teamcenter.rac.kernel.ics.ICSAdminClassAttribute;
import com.teamcenter.rac.kernel.ics.ICSApplicationObject;
import com.teamcenter.rac.kernel.ics.ICSProperty;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;

/**
 * 
 * @XiaYang
 * 
 */
@SuppressWarnings("serial")
public class ImportInClassDialog extends AbstractAIFDialog {

	public final static int HEAD_CHAR_SIZE = 10;
	public final static int BODY_CHAR_SIZE = 10;

	LogAppend logAppend;

	int item_attr_start = 0;
	int item_attr_end = 0;
	int inlcass_attr_start = 0;
	int inlcass_attr_end = 0;

	TCComponentFolder pasteFolder;

	String xls_path;
	String rootFolderPath;

	//	iTextField xlsPathTextField;
	//	JButton browseButton;

	DefaultTableModel templateTableModel;
	JTable templateInfoTable;	
	JButton addTemplateButton;
	JButton removeTemplateButton;	
	JScrollPane templateInfoTablePane;

	ArrayList<String> templateList;
	ArrayList<String> rootFolderList;

	JButton okButton;
	JButton cancelButton;

	TCSession session;

	Registry registry;

	TCComponentQuery generalQuery;
	TCComponentQuery itemIDQuery;

	TCClassificationService classificationService = null;
	ICSAdminClass adminClass = null;
	AbstractAIFUIApplication calssificationApplication = null;

	HashMap<String, String> inclassPropMap;

	//	int totalSuccCount = 0;
	//	int totalFailedCount = 0;

	public ImportInClassDialog(AIFDesktop aifDesktop, TCSession session, TCComponentFolder targetFolder) {
		super(aifDesktop, "");
		pasteFolder = targetFolder;

		classificationService = session.getClassificationService();

		calssificationApplication = aifDesktop.getApplicationManager().askApplication("com.teamcenter.rac.classification.icm.ICMApplication");
		this.session = session;
		registry = Registry.getRegistry(this);

		//		xls_path = "";

		logAppend = new LogAppend();

		templateList = new ArrayList<String>();
		rootFolderList = new ArrayList<String>();
		inclassPropMap = new HashMap<String, String>();

		try {

			TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			generalQuery = (TCComponentQuery) (queryType.find("General..."));
			if (generalQuery == null) {
				generalQuery = (TCComponentQuery) (queryType.find("常规..."));
			}
			itemIDQuery = (TCComponentQuery) (queryType.find("Item ID"));

		} catch (TCException e) {
			e.printStackTrace();
			disposeDialog();
		}

		initUI();
		showDialog();
		addActionListeners();
	}

	public void initUI() {
		//setTitle("Import InClass...");
		setTitle("导入分类数据...");
		
		JPanel mainPanel = new JPanel(new PropertyLayout(5, 10, 5, 5, 5, 5));

		JPanel panel = new JPanel();
		//panel.setBorder(BorderFactory.createTitledBorder("Select import file"));
		panel.setBorder(BorderFactory.createTitledBorder("选择导入文件"));
		
		//xlsPathTextField = new iTextField(50);
		//browseButton = new JButton("Browse...");
		//panel.add("1.1.left.center.preferred.preferred", xlsPathTextField);
		//panel.add("1.2.left.center.preferred.preferred", browseButton);

		//String[] tableHeader1 = { "Import Template Path" };
		String[] tableHeader1 = { "导入模板路径" };
		templateTableModel = new DefaultTableModel(tableHeader1, 0);
		templateInfoTable = new JTable(templateTableModel);

		templateInfoTablePane = new JScrollPane(templateInfoTable);
		templateInfoTablePane.setBackground(Color.white);
		templateInfoTablePane.setPreferredSize(new Dimension(500, 100));

		JPanel tableButtonPanel = new JPanel(new PropertyLayout(2, 2, 2, 2, 2, 2));
		addTemplateButton = new JButton(registry.getImageIcon("addButton.ICON"));
		addTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		removeTemplateButton = new JButton(registry.getImageIcon("removeButton.ICON"));
		removeTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		tableButtonPanel.add("1.1.center.top.preferred.preferred", addTemplateButton);
		tableButtonPanel.add("2.1.center.top.preferred.preferred", removeTemplateButton);

		panel.add("left.bind.left.top", templateInfoTablePane);
		panel.add("right.nobind.left.top", tableButtonPanel);		

		mainPanel.add("1.1.center.top.preferred.preferred", panel);

		//okButton = new JButton("Import");
		//cancelButton = new JButton("Cancel");
		okButton = new JButton("导入");
	    cancelButton = new JButton("取消");
		JPanel buttonPanel = new JPanel(new ButtonLayout(ButtonLayout.HORIZONTAL, ButtonLayout.CENTER, 20));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		okButton.setEnabled(false);

		mainPanel.add("2.1.center.top.preferred.preferred", buttonPanel);

		setResizable(false);
		getContentPane().add(mainPanel);
		pack();
		centerToScreen();

	}

	public void addActionListeners() {

		//		browseButton.addActionListener(new ActionListener() {
		//
		//			public void actionPerformed(java.awt.event.ActionEvent e) {
		//
		//				if (excelWriter != null) {
		//					excelWriter.closeExcel();
		//				}
		//				JFileChooser fileChooser = new JFileChooser();
		//				fileChooser.setAcceptAllFileFilterUsed(false);
		//
		//				fileChooser.addChoosableFileFilter(new FileFilter() {
		//
		//					public boolean accept(File f) {
		//
		//						if (f.isDirectory() || f.getName().toLowerCase().endsWith(".xls")) {
		//							return true;
		//						}
		//						return false;
		//					}
		//
		//					public String getDescription() {
		//						return "*.xls";
		//					}
		//				});
		//
		//				int result = fileChooser.showOpenDialog(AIFDesktop.getActiveDesktop());
		//
		//				if (result == JFileChooser.APPROVE_OPTION) {
		//					xlsPathTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		//					okButton.setEnabled(true);
		//					xls_path = xlsPathTextField.getText();
		//					rootFolderPath = fileChooser.getCurrentDirectory().getAbsolutePath();
		//					try {
		//						excelWriter = new ExcelWriter(xls_path);
		//					} catch (BiffException e1) {
		//						e1.printStackTrace();
		//					} catch (IOException e1) {
		//						e1.printStackTrace();
		//					}
		//					System.out.println("xls path is: " + xls_path);
		//				}
		//			}
		//
		//		});

		addTemplateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setAcceptAllFileFilterUsed(false);

				fileChooser.addChoosableFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {

						if (f.isDirectory() || f.getName().toLowerCase().endsWith(".xls") ) {
							return true;
						}
						return false;
					}

					@Override
					public String getDescription() {
						return "*.xls";
					}
				});

				int result = fileChooser.showOpenDialog(AIFDesktop.getActiveDesktop());

				if (result == JFileChooser.APPROVE_OPTION) {
					File[] selectedFiles = fileChooser.getSelectedFiles();
					int tempSeq = templateInfoTable.getSelectedRow();
					for (int i = 0; i < selectedFiles.length; i++) {
						templateList.add(selectedFiles[i].getAbsolutePath());
						rootFolderList.add(fileChooser.getCurrentDirectory().getAbsolutePath());
						if (tempSeq == -1) {
							templateTableModel.addRow(new String[] {selectedFiles[i].getAbsolutePath()});
						}
						//有选中行，在选中行后添加
						else {
							templateTableModel.insertRow(tempSeq+1, new String[] {selectedFiles[i].getAbsolutePath()});						
						}
					}
					okButton.setEnabled(true);
					System.out.println("Load " + selectedFiles.length + " templates");
				}
			}

		});

		removeTemplateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int[] selectedRows = templateInfoTable.getSelectedRows();
				for (int i = selectedRows.length - 1; i >= 0 ; i--) {
					int selectedRow = selectedRows[i];
					templateList.remove(selectedRow);
					rootFolderList.remove(selectedRow);
					templateTableModel.removeRow(selectedRow);
				}				
			}
		});

		okButton.addActionListener(new ActionListener() {

			
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				//				totalSuccCount = 0;
				//				totalFailedCount = 0;
				for (int index = 0; index < templateList.size(); index++) {
					xls_path = templateList.get(index);
					rootFolderPath = rootFolderList.get(index);
					ExcelWriter excelWriter = null;
					try {
					   // session.setStatus("Opening template file" + String.valueOf(index + 1) + " ...");
						session.setStatus("打开模板文件 " + String.valueOf(index + 1) + " ...");
						logAppend.AppendLog("[------------Information-----------]Opening template file:" + xls_path);
						excelWriter = new ExcelWriter(xls_path);
					} catch (BiffException e1) {
						e1.printStackTrace();
						if (excelWriter != null) {
							excelWriter.closeExcel();
							excelWriter= null;
							logAppend.AppendLog("[++ERROR]Opening template file FAILED!");
							continue;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						if (excelWriter != null) {
							excelWriter.closeExcel();
							excelWriter= null;
							logAppend.AppendLog("[++ERROR]Opening template file FAILED!");
							continue;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						if (excelWriter != null) {
							excelWriter.closeExcel();
							excelWriter= null;
							logAppend.AppendLog("[++ERROR]Opening template file FAILED!");
							continue;
						}
					}
					//session.setStatus("Checking data...");
					session.setStatus("正在检查数据...");
					
					int sheetCount = excelWriter.getSheetCount();

					//遍历所有的sheet
					for (int i = 0; i < sheetCount; i++) {

						excelWriter.switchSheet(i);
						logAppend.AppendLog("[Information]Switch on Sheet: " + excelWriter.workSheet.getName());
						//System.out.println("[Information]Switch on Sheet: " + excelWriter.workSheet.getName());
						inclassPropMap.clear();

						if (!excelWriter.getCellContent2(0, 0).toLowerCase().equals("class type")) {
							logAppend.AppendLog("[++Warning]Sheet skipped, not a data sheet! The value should be 'class type' in cell 'A1' in a data sheet.");
							continue;
						}

						//获取参数
						inlcass_attr_start = 8;
						inlcass_attr_end = 8;
						while (!excelWriter.getCellContent2(inlcass_attr_end+1, 0).equals("")) {
							inlcass_attr_end++;
						}
						//							String columnParm = excelWriter.getCellContent2(0, 1);
						//							if (columnParm.equals("")) {
						//								logAppend.AppendLog("[----Error----]Sheet skipped, not found Column Parameters. Sheet = " + excelWriter.workSheet.getName());
						//								continue;
						//							}
						//							inlcass_attr_start = Integer.parseInt(columnParm.split(",")[0]);
						//							inlcass_attr_end = Integer.parseInt(columnParm.split(",")[1]);
						//							logAppend.AppendLog("[Information]Get column parameters. Sheet = " + excelWriter.workSheet.getName() + ", parameter = " + columnParm);

						//String inClassID = excelWriter.getCellContent2(1, 1);
						//String itemTypeString = excelWriter.getCellContent2(2, 1);
						String inClassID = excelWriter.getCellContent2(1, 2);
						String itemTypeString = excelWriter.getCellContent2(2, 2);
						//检查
						TCComponentItemType itemType = null;
						try {
							itemType = (TCComponentItemType) session.getTypeComponent(itemTypeString);
							if (itemType == null) {
								logAppend.AppendLog("[++ERROR]Sheet skipped, invalid Item Type. Sheet = " + excelWriter.workSheet.getName() + ", Item Type = " + itemTypeString);
								continue;
							}
							//logAppend.AppendLog("[Information] Item Type is ok. Item Type = " + itemTypeString);
							System.out.println("[Information] Item Type is ok. Item Type = " + itemTypeString);

						} catch (TCException e2) {
							e2.printStackTrace();
							continue;
						}

						ICSAdminClassAttribute[] attributes;
						try {
							//检查
							ICSAdminClass adminClass = classificationService.newICSAdminClass();
							adminClass.load(inClassID);
							attributes = adminClass.getAttributes();
							if (attributes.length == 0) {
								logAppend.AppendLog("[++Error]Sheet skipped, invalid Class ID. Sheet = " + excelWriter.workSheet.getName() + ", Class ID = " + inClassID);
								continue;
							}
							//logAppend.AppendLog("[Information] Class ID is ok. Class ID = " + inClassID);
							System.out.println("[Information] Class ID is ok. Class ID = " + inClassID);
						} catch (Exception e2) {
							e2.printStackTrace();
							continue;
						}

						//遍历行数据
						//modified by lou xiang 
						//int rowCount =1;
						int rowCount = 2;
						//end modification
						while (!excelWriter.getCellContent2(5, rowCount).equals("")) {
							String errMsg = "";
							String itemID = excelWriter.getCellContent2(3, rowCount);
							String itemRevID = excelWriter.getCellContent2(4, rowCount);
							String itemName = excelWriter.getCellContent2(5, rowCount);
							String datasetTypeString = excelWriter.getCellContent2(6, rowCount);
							String filePathString = excelWriter.getCellContent2(7, rowCount);

							boolean createDataset = false;

							//检查
							String[] datasetTypes = null;
							String[] filePaths = null;
							if (!datasetTypeString.equals("") && !filePathString.equals("")) {
								datasetTypes = datasetTypeString.split(":");
								filePaths = filePathString.split(":");
								if (datasetTypes.length != filePaths.length) {
									errMsg = errMsg + "'DatasetType'与'File Path'不匹配；";
									excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
									excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
									logAppend.AppendLog("[----Warning----]'DatasetType' not match with 'File Path', dataset will not be created. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount));
									rowCount++;
									continue;
									//createDataset = false;
								}else {
									createDataset = true;
									//检查
									boolean isValidDatasetType = true;
									String datasetType = null;
									for (int j = 0; j < datasetTypes.length; j++) {
										datasetType = datasetTypes[j];
										TCComponentDatasetType tcComponentDatasetType = null;
										try {
											tcComponentDatasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
										} catch (TCException e1) {
											e1.printStackTrace();
										}

										if (tcComponentDatasetType == null) {
											isValidDatasetType = false;
											break;
										}
									}
									if (!isValidDatasetType) {
										errMsg = errMsg + "不正确的'DatasetType'；";
										excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
										excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
										logAppend.AppendLog("[----Warning----]Invalid DatasetType, dataset will not be created. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", Invalid Type = " + datasetType);
										rowCount++;
										continue;
										//createDataset = false;
									}

									//检查
									boolean isValidFilePath = true;
									String filePath = null;
									for (int j = 0; j < filePaths.length; j++) {
										filePath = rootFolderPath + "\\" + filePaths[j];
										File file = new File(filePath);
										if (!file.exists()) {
											isValidFilePath = false;
										}else {
											filePaths[j] = filePath;
										}
									}
									if (!isValidFilePath) {
										errMsg = errMsg + "'File Path'定义的文件不存在；";
										excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
										excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
										logAppend.AppendLog("[----Warning----]Invalid File Path, dataset will not be created. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", Invalid Path = " + filePath);
										rowCount++;
										continue;
										//createDataset = false;
									}
								}
							}else if (datasetTypeString.equals("") && filePathString.equals("")) {
								createDataset = false;
							}else {
								errMsg = errMsg + "'DatasetType'与'File Path'不匹配；";
								excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
								excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
								logAppend.AppendLog("[----Warning----]'DatasetType' not match with 'File Path', dataset will not be created. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount));
								rowCount++;
								continue;
								//createDataset = false;
							}

							TCComponentItemRevision relatedRevision = null;
							//是否新建
							boolean isValidRow = checkValidInclassAttr(rowCount, excelWriter);
							if (isValidRow) {
								if (itemID.equals("")) {
									try {
										//session.setStatus("Creating new item revision...");
										session.setStatus("创建 零件版本...");
									
										String newItemId = itemType.getNewID();
									
										
										String newRevId = itemType.getNewRev(null);
										TCComponentItem newItem = itemType.create(newItemId, newRevId, itemTypeString, itemName,  "", null);
										relatedRevision = newItem.getLatestItemRevision();
									
										pasteFolder.add("contents", newItem);
										logAppend.AppendLog("[Information] New Item created. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", ItemID = " + newItemId + ", ItemRev = " + newRevId);
										//System.out.println("[Information] New Item created. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", ItemID = " + newItemId + ", ItemRev = " + newRevId);

										excelWriter.addStringCell(3, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, newItemId, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
										excelWriter.setCellBorder(3, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
										excelWriter.addStringCell(4, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, newRevId, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
										excelWriter.setCellBorder(4, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
										//logAppend.AppendLog("[Information] New Item information has written back to Excel.");
										System.out.println("[Information] New Item information has written back to Excel.");
									} catch (TCException e2) {
										e2.printStackTrace();
										errMsg = errMsg + "系统新建ItemRevision过程中出现异常；";
										excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
										excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
										rowCount++;
										logAppend.AppendLog("[++ERROR] Row skipped. TCException occured during creating new item revision. Sheet = " + excelWriter.workSheet.getName() + ", Row= " + String.valueOf(rowCount));
										continue;
									}		
									try {
										if (createDataset) {
											TCComponentDatasetType tcComponentDatasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
											for (int j = 0; j < filePaths.length; j++) {
												System.out.println("file path is " + filePaths[j]);
												String[] tmpStrs = filePaths[j].split("\\\\");
												String fileName = tmpStrs[tmpStrs.length - 1];
												System.out.println("file name is " + fileName);
												int position = fileName.lastIndexOf(".");
												String datasetName = fileName.substring(0, position);
												TCComponentDataset newDatset = tcComponentDatasetType.create(datasetName, "", datasetTypes[j]);
												relatedRevision.add("IMAN_specification", newDatset);
												
												//modify by Lou Xiang 20131211
												//String namedRef = newDatset.getDatasetDefinitionComponent().getNamedReferences()[0];
												//newDatset.setFiles(new String[] { filePaths[j] }, new String[] { namedRef });
												if(datasetTypes[j].equalsIgnoreCase("CATPart"))
												{
													newDatset.setFiles(new String[] { filePaths[j] }, new String[] { "catpart" });
												}
												else if(datasetTypes[j].equalsIgnoreCase("UGMASTER"))
												{
													newDatset.setFiles(new String[] { filePaths[j] }, new String[] { "UGPART" });
												}
												else
												{
													String namedRef = newDatset.getDatasetDefinitionComponent().getNamedReferences()[0];
													newDatset.setFiles(new String[] { filePaths[j] }, new String[] { namedRef });
												}
												//end modification
												
											}
											//logAppend.AppendLog("[Information] Dataset created.");
											System.out.println("[Information] Dataset created.");
										}
									} catch (Exception e2) {
										e2.printStackTrace();
										logAppend.AppendLog("[++ERROR] TCException occured during creating dataset. Sheet = " + excelWriter.workSheet.getName() + ", Row= " + String.valueOf(rowCount));
									}

								}else {
									try {
										TCComponentItemRevisionType revisionType = (TCComponentItemRevisionType) session.getTypeComponent(itemTypeString + "Revision");
										if (itemRevID.equals("")) {
											relatedRevision = revisionType.findRevision(itemID, "001");
											if (relatedRevision == null) {
												errMsg = errMsg + "系统中未找到该ID和版本的ItemRevsion；";
												excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
												excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
												logAppend.AppendLog("[----Error----]Row skipped, item revision not found in Teamcenter. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", ItemID = " + itemID + ", ItemRev = " + itemRevID);
												rowCount++;
												continue;
											}
											excelWriter.addStringCell(4, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, "001", BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
											excelWriter.setCellBorder(4, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
											//logAppend.AppendLog("[Information] Item revision found in Teamcenter. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", ItemID = " + itemID + ", ItemRev = " + "A");
										}else {
											relatedRevision = revisionType.findRevision(itemID, itemRevID);
											if (relatedRevision == null) {
												errMsg = errMsg + "系统中未找到该ID和版本的ItemRevsion；";
												excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
												excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
												logAppend.AppendLog("[----Error----]Row skipped, item revision not found in Teamcenter. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", ItemID = " + itemID + ", ItemRev = " + itemRevID);
												rowCount++;
												continue;
											}
											//logAppend.AppendLog("[Information] Item revision found in Teamcenter. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount) + ", ItemID = " + itemID + ", ItemRev = " + itemRevID);
										}
									} catch (TCException e2) {
										e2.printStackTrace();
									}
								}
							}else {
								errMsg = errMsg + "该行分类属性全部为空，跳过；";
								excelWriter.addStringCell(inlcass_attr_end + 1, rowCount, ExcelWriter.WHITE, ExcelWriter.BLACK, errMsg, BODY_CHAR_SIZE, false, ExcelWriter.LEFT);
								excelWriter.setCellBorder(inlcass_attr_end + 1, rowCount, ExcelWriter.BORDER_ALL, ExcelWriter.BORDERLINE_THIN,	ExcelWriter.BLACK);
								logAppend.AppendLog("[++Error]Row skipped, no InClassification attribute set. Sheet = " + excelWriter.workSheet.getName() + ", Row = " + String.valueOf(rowCount));
								rowCount++;
								continue;
							}

							//设置
							for (int j = inlcass_attr_start; j < inlcass_attr_end + 1; j++) {
								String attrName = excelWriter.getCellContent2(j, 0);
								String value = null;
								try {
									if (excelWriter.getComment(j, 0).toUpperCase().equals("LOV")) {
										value = excelWriter.getCellContent2(j, rowCount).split(":")[0];
									}else if (excelWriter.getComment(j, 0).toUpperCase().equals("LOV_BOOL")) {
										if (!excelWriter.getCellContent2(j, rowCount).equals("")) {
											value = excelWriter.getCellContent2(j, rowCount).split(":")[0];
										}else {
											value = "2";
										}
									}else {
										value = excelWriter.getCellContent2(j, rowCount);
									}
								} catch (NullPointerException e2) {
									value = excelWriter.getCellContent2(j, rowCount);
								}
								
								if (!value.equals("") || value != null) {
									inclassPropMap.put(attrName, value);
								}								
							}
							
							//Add by Lou Xiang 20131211
							//设置 属性 
							if(relatedRevision != null)
							{
								try {
									TCComponentForm spform  =(TCComponentForm)relatedRevision.getRelatedComponent("IMAN_specification");
									spform.setProperties(inclassPropMap);
								} catch (TCException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							
							ICSProperty[] properties = new ICSProperty[attributes.length];
							for (int j = 0; j < properties.length; j++) {
								properties[j] = new ICSProperty(attributes[j].getAttributeId());
								String propName = attributes[j].getName();									
								if (inclassPropMap.containsKey(propName)) {
									System.out.println("inclassPropMap.get(propName):"+j+":"+inclassPropMap.get(propName));
									properties[j].setValue(inclassPropMap.get(propName));
								}
								else {
									properties[j].setValue("");
								}
							}

							ICSApplicationObject appObject = null;
							try {
								G4MUserAppContext m_context = new G4MUserAppContext(calssificationApplication, "ICM");
								appObject = m_context.getICSApplicationObject();
							} catch (Exception e2) {
								e2.printStackTrace();
							}


							int objFound = 0;
							try {
								objFound = appObject.searchById("", relatedRevision.getUid());
							} catch (TCException e1) {
								e1.printStackTrace();
							}
							if (objFound > 0) {
								//logAppend.AppendLog("[Information] ICO already exist.");
								System.out.println("[Information] ICO already exist.");
							}else {	
								//session.setStatus("Create ICO and set attributes...");
								session.setStatus("创建分类对象和设置属性...");
								logAppend.AppendLog("[Information] Create ICO and set attributes...");

								try {
									appObject.create(relatedRevision.getProperty("item_id") + "/" + relatedRevision.getProperty("item_revision_id"), relatedRevision.getUid());
									appObject.edit();
									appObject.setView(inClassID);
									appObject.setProperties(properties);
									appObject.save();
								} catch (Exception e2) {
									e2.printStackTrace();
									logAppend.AppendLog("[++Warning] Save properties error, maybe format error");
								}

								logAppend.AppendLog("[Information] New ico created and classification attribute set ok.");
								//System.out.println("[Information] New ico created and classification attribute set ok.");
							}	

							rowCount++;
							logAppend.Flush();
						}
					}
					excelWriter.closeExcel();
					excelWriter = null;

				}
				session.setReadyStatus();
				showLog("Importing completed");
				logAppend.free();
			}
		});

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				disposeDialog();
			}

		});
	}

	public boolean checkValidInclassAttr(int row, ExcelWriter excelWriter) {
		boolean isValid = false;
		for (int i = inlcass_attr_start; i < inlcass_attr_end; i++) {
			String valueString = excelWriter.getCellContent2(i, row);
			if (!valueString.equals("")) {
				return true;
			}
		}
		return isValid;
	}

	private void showLog(String info) {
		int response = JOptionPane.showConfirmDialog(AIFDesktop.getActiveDesktop(), info + "View log?", "Message",
				JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION) {
			Runtime rt = Runtime.getRuntime();
			String[] open = { "write", logAppend.filename };
			try {
				rt.exec(open);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
