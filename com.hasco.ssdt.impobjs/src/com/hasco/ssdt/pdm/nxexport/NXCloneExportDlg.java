package com.hasco.ssdt.pdm.nxexport;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hasco.ssdt.util.CommonUtils;
import com.hasco.ssdt.util.MsgBox;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.iTextField;

public class NXCloneExportDlg extends AbstractAIFDialog
{
	private static final long serialVersionUID = 1L;
	
	AbstractAIFUIApplication app = null;
	AIFComponentContext[] selectedContexts = null;
	TCSession session = null;
	iTextField fileField ;
	iTextField exportPathField ;
	iTextField ugBaseDirField ;
	
	JFileChooser selectFileChooser ;
	JFileChooser exportPathChooser ;
	JFileChooser nxBaseDirChooser ;
	
	String userGroup = null;
	String nxBaseDirStr = null;
	
	JButton confirmButton;
	JButton cancelButton;
	JButton selectFileButton;
	JButton selectExportPathButton;
	JButton selectNXBaseDirButton;
	
    JCheckBox ugMasterCheckBox;
    JCheckBox ugPartCheckBox;
    JCheckBox imageCheckBox;
    JCheckBox wordCheckBox;
    JCheckBox excelCheckBox;
    JCheckBox pdfCheckBox;
    JCheckBox rarCheckBox;
    
    ArrayList<String> selectList = new ArrayList<String>();
		
	protected Registry registry;
	String defaultServerURL = "";

	public NXCloneExportDlg(Frame frame, String title)
	{
		super(frame, title);
		app = AIFUtility.getCurrentApplication();
		session = (TCSession) app.getSession();
		nxBaseDirStr = System.getenv("UGII_BASE_DIR");
		registry = Registry.getRegistry(this);
		this.defaultServerURL = registry.getString("DEFAULT_SERVER.URI");
		
		try {
			initUI();
			addActionListener();
		} catch (Exception e) {
			MsgBox.showM(e.toString(),"",MessageBox.ERROR);
			e.printStackTrace();
		}
	}
	
	public void initUI() throws TCException, Exception 
	{				
	    JPanel mainPanel = new JPanel(new PropertyLayout(5, 5, 15, 15, 15, 15));
	    
		JPanel topPanel = new JPanel(new PropertyLayout(8, 8, 8, 8, 8, 8));
		fileField = new iTextField();
		fileField.setPreferredSize(new Dimension(160, 24));
		fileField.setEditable(false);
		
		exportPathField = new iTextField();
		exportPathField.setPreferredSize(new Dimension(160, 24));
		exportPathField.setEditable(false);
						
		ugBaseDirField = new iTextField();
		ugBaseDirField.setPreferredSize(new Dimension(160, 24));
		ugBaseDirField.setText(nxBaseDirStr);
		ugBaseDirField.setEditable(false);
	
		selectFileChooser = new JFileChooser();
		exportPathChooser = new JFileChooser();
		 if(!getLastPath().equals("")){
			 exportPathChooser.setCurrentDirectory(new File(getLastPath()));
	     }
		nxBaseDirChooser= new JFileChooser();
		
		selectFileButton = new JButton("选择...");
		selectFileButton.setPreferredSize(new Dimension(80, 24));
		
		selectExportPathButton = new JButton("选择...");
		selectExportPathButton.setPreferredSize(new Dimension(80, 24));
		
		selectNXBaseDirButton = new JButton("选择...");
		selectNXBaseDirButton.setPreferredSize(new Dimension(80, 24));

		ugMasterCheckBox = new JCheckBox("数模",true);
		ugPartCheckBox   = new JCheckBox("图纸",true);
		imageCheckBox    = new JCheckBox("图像",true);
		wordCheckBox     = new JCheckBox("Word",true);
		excelCheckBox    = new JCheckBox("Excel",true);
		pdfCheckBox      = new JCheckBox("PDF",true);
		rarCheckBox      = new JCheckBox("压缩文件",true);
		
		JPanel centerPanel = new JPanel(new PropertyLayout(16, 8, 8, 8, 8, 8));
		centerPanel.setBorder (new TitledBorder("选择导出数据集 "));
		
		centerPanel.add("1.1.left.top.preferred.preferred", ugMasterCheckBox);
		centerPanel.add("1.2.center.top.preferred.preferred", ugPartCheckBox);
		centerPanel.add("2.1.center.top.preferred.preferred", imageCheckBox);
		centerPanel.add("2.2.center.top.preferred.preferred", wordCheckBox);
		centerPanel.add("2.3.center.top.preferred.preferred", excelCheckBox);
		centerPanel.add("2.4.center.top.preferred.preferred", rarCheckBox);
		centerPanel.add("2.5.left.top.preferred.preferred", pdfCheckBox);
		
		topPanel.add("1.1.left.top.preferred.preferred", new JLabel("文件选择:"));
	    topPanel.add("1.2.center.top.preferred.preferred", fileField);
	    topPanel.add("1.3.right.top.preferred.preferred", selectFileButton);
				
	    topPanel.add("2.1.left.top.preferred.preferred", new JLabel("选择导出路径:"));
	    topPanel.add("2.2.center.top.preferred.preferred", exportPathField);
	    topPanel.add("2.3.right.top.preferred.preferred", selectExportPathButton);
	    
	    topPanel.add("3.1.left.top.preferred.preferred", new JLabel("选择NX安装路径:"));
	    topPanel.add("3.2.center.top.preferred.preferred", ugBaseDirField);
	    topPanel.add("3.3.right.top.preferred.preferred", selectNXBaseDirButton);
	    	    
		confirmButton = new JButton("确定");
		confirmButton.setPreferredSize(new Dimension(80, 24));
		
	    cancelButton = new JButton("取消");
	    cancelButton.setPreferredSize(new Dimension(80, 24));
	    JPanel buttonPanel = new JPanel(new PropertyLayout(38, 8, 8, 8, 8, 8));
	    buttonPanel.add("1.1.center.top.preferred.preferred", confirmButton);
	    buttonPanel.add("1.2.center.top.preferred.preferred", cancelButton);
		
	    mainPanel.add("1.1.center.top.preferred.preferred",  topPanel);
	    mainPanel.add("2.1.center.top.preferred.preferred", centerPanel); 
	    mainPanel.add("3.1.center.top.preferred.preferred", buttonPanel);
	    
		add(mainPanel);
		centerToScreen();
		setResizable(false);
		pack();
	}
    
    public void addActionListener()throws Exception
    {    	
		confirmButton.addActionListener(new ActionListener() 
		{              
            @Override  
            public void actionPerformed(ActionEvent e) 
            { 
            	if (fileField.getText().equals("")) {
            		MsgBox.showM("请选择Excel文件", "提示", 2);
            		return;
				} else if (exportPathField.getText().equals("")) {
					MsgBox.showM("请选择导出路径", "提示", 2);
            		return;
            	} else if(ugBaseDirField.getText().equals("")){
            		MsgBox.showM("请选择NX安装路径", "提示", 2);
            		return;
            	}else if (CommonUtils.checkBlank(exportPathField.getText())){
            		MsgBox.showM("路径不能包含空格", "提示", 2);
            		return;
            	}else if (CommonUtils.isContainChinese(exportPathField.getText())){
            		MsgBox.showM("路径不能包含中文", "提示", 2);
            		return;
            	}
            	
            	if(ugMasterCheckBox.isSelected()){
            		selectList.add("UGMaster");
            	}
            	if(ugPartCheckBox.isSelected()){
            		selectList.add("UGPart");
            	}
            	if(imageCheckBox.isSelected()){
            		selectList.add("Image");
            	}
            	if(wordCheckBox.isSelected()){
            		selectList.add("MSWord");
            		selectList.add("MSWordX");
            	}
            	if(excelCheckBox.isSelected()){
            		selectList.add("MSExcel");
            		selectList.add("MSExcelX");
            	}
            	if(pdfCheckBox.isSelected()){
            		selectList.add("PDF");
            	}
            	if(rarCheckBox.isSelected()){
            		selectList.add("H5Winrar");
            		selectList.add("Zip");
            	}
				if (selectList.size() == 0) {
					MsgBox.showM("请选择至少一种导出类型", "提示", 2);
					return;
				}
                       	
				File file = new File(exportPathField.getText().toString());
				if (!file.exists()) {
					file.mkdir();
				}   
                
            	dispose();
            	   
            	NXCloneExport nxExport = new NXCloneExport(fileField.getText(), exportPathField.getText().toString(),ugBaseDirField.getText().toString(),selectList,defaultServerURL);
				Thread exportThread = new Thread(nxExport);
				exportThread.start();
            }  
        }); 
		
		selectFileButton.addActionListener(new ActionListener() 
		{              
            @Override  
            public void actionPerformed(ActionEvent e) 
            {         
            	selectFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            	selectFileChooser.setFileFilter(new FileNameExtensionFilter("Excel 工作簿", "xlsx", "xls"));
                int intRetVal = selectFileChooser.showOpenDialog(NXCloneExportDlg.this);
            	if( intRetVal == JFileChooser.APPROVE_OPTION){
            		fileField.setText(selectFileChooser.getSelectedFile().getPath());
                }
            }  
        }); 
		
		selectExportPathButton.addActionListener(new ActionListener() 
		{              
            @Override  
            public void actionPerformed(ActionEvent e) 
            {         
            	exportPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int intRetVal = exportPathChooser.showOpenDialog(NXCloneExportDlg.this);
            	if( intRetVal == JFileChooser.APPROVE_OPTION){
            		exportPathField.setText(exportPathChooser.getSelectedFile().getPath());
            		setLastPath(exportPathChooser.getSelectedFile().getAbsolutePath());
                }
            }  
        }); 
		
		selectNXBaseDirButton.addActionListener(new ActionListener() 
		{              
            @Override  
            public void actionPerformed(ActionEvent e) 
            {         
            	nxBaseDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int intRetVal = nxBaseDirChooser.showOpenDialog(NXCloneExportDlg.this);
            	if( intRetVal == JFileChooser.APPROVE_OPTION){
            		ugBaseDirField.setText(nxBaseDirChooser.getSelectedFile().getPath());
                }
            }  
        }); 
		
		cancelButton.addActionListener(new IC_DisposeActionListener()); 
	}
    
	public String getLastPath()
	{
		String lastPath = "";
		Preferences pref = Preferences.userRoot().node(this.getClass().getName()); 
		lastPath = pref.get("exportlastPath", ""); 
		
		return lastPath;
	}
	
	public void setLastPath(String path)
	{		
		Preferences pref = Preferences.userRoot().node(this.getClass().getName()); 
		pref.put("exportlastPath", path); 
	}
    
 }
