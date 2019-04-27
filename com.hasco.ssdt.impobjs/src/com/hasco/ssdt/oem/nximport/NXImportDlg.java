package com.hasco.ssdt.oem.nximport;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

public class NXImportDlg extends AbstractAIFDialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AbstractAIFUIApplication app = null;
	AIFComponentContext[] selectedContexts = null;
	TCSession session = null;
	iTextField exportAssemlyPathField ;
	iTextField ugBaseDirField ;
	JFileChooser exportAssemlyPathChooser ;
	JFileChooser nxBaseDirChooser ;
	
	String userGroup = null;
	String nxBaseDirStr = null;
	
	JButton confirmButton;
	JButton cancelButton;
	JButton selectExportPathButton;
	JButton selectNXBaseDirButton;
	
   ArrayList<String> selectList = new ArrayList<String>();
	
	WaitingDialog  wDialog ;
	protected Registry registry;
	String defaultServerURL="";

	public NXImportDlg(Frame frame, String title){
		super(frame,title);
		app = AIFUtility.getCurrentApplication();
		session = (TCSession) app.getSession();
		nxBaseDirStr = System.getenv("UGII_BASE_DIR");
		
		registry = Registry.getRegistry(this);
		this.defaultServerURL = registry.getString("DEFAULT_SERVER.URI");
		
		try {
			initUI();
			addActionListener();

		} catch (TCException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
public void initUI()throws TCException,Exception{
				
	    JPanel mainPanel = new JPanel(new PropertyLayout(5, 5, 15, 15, 15, 15));
	    
		JPanel topPanel = new JPanel(new PropertyLayout(8, 8, 8, 8, 8, 8));
		exportAssemlyPathField = new iTextField();
		exportAssemlyPathField.setPreferredSize(new Dimension(160, 24));
		exportAssemlyPathField.setEditable(false);
						
		ugBaseDirField = new iTextField();
		ugBaseDirField.setPreferredSize(new Dimension(160, 24));
		ugBaseDirField.setText(nxBaseDirStr);
		ugBaseDirField.setEditable(false);
	
		exportAssemlyPathChooser = new JFileChooser();
		nxBaseDirChooser= new JFileChooser();
		
		selectExportPathButton = new JButton("选择...");
		selectExportPathButton.setPreferredSize(new Dimension(80, 24));
		
		selectNXBaseDirButton = new JButton("选择...");
		selectNXBaseDirButton.setPreferredSize(new Dimension(80, 24));
			
	    topPanel.add("1.1.left.top.preferred.preferred", new JLabel("选择导入总装配:"));
	    topPanel.add("1.2.center.top.preferred.preferred", exportAssemlyPathField);
	    topPanel.add("1.3.right.top.preferred.preferred", selectExportPathButton);
	    
	    topPanel.add("2.1.left.top.preferred.preferred", new JLabel("选择NX安装路径:"));
	    topPanel.add("2.2.center.top.preferred.preferred", ugBaseDirField);
	    topPanel.add("2.3.right.top.preferred.preferred", selectNXBaseDirButton);

		confirmButton = new JButton("确定");
		confirmButton.setPreferredSize(new Dimension(80, 24));
		
	    cancelButton = new JButton("取消");
	    cancelButton.setPreferredSize(new Dimension(80, 24));
	    JPanel buttonPanel = new JPanel(new PropertyLayout(28, 8, 8, 8, 8, 8));
	    buttonPanel.add("1.1.center.top.preferred.preferred", confirmButton);
	    buttonPanel.add("1.2.center.top.preferred.preferred", cancelButton);
		
	    mainPanel.add("1.1.center.top.preferred.preferred",  topPanel);
	    mainPanel.add("2.1.center.top.preferred.preferred", buttonPanel);
	    
		add(mainPanel);
		centerToScreen();
		setResizable(false);
		pack();
				
	}

    
    public void addActionListener()throws TCException,Exception{
    	
		confirmButton.addActionListener(new ActionListener() {              
            @Override  
            public void actionPerformed(ActionEvent e) {
            	            	
            	if(exportAssemlyPathField.getText().equals("")){
            		MsgBox.showM("请选择导入装配", "提示",
							MessageBox.INFORMATION);
            		return;
            	} else if(ugBaseDirField.getText().equals("")){
            		MsgBox.showM("请选择NX安装路径", "提示",
							MessageBox.INFORMATION);
            		return;
            	}else if (CommonUtils.checkBlank(exportAssemlyPathField.getText())){
            		MsgBox.showM("导入装配路径不能包含空格", "提示",
							MessageBox.INFORMATION);
            		return;
            	}else if (CommonUtils.isContainChinese(exportAssemlyPathField.getText())){
            		MsgBox.showM("导入装配路径不能包含中文", "提示",
							MessageBox.INFORMATION);
            		return;
            	}
            	
            	File file=new File(exportAssemlyPathField.getText().toString());    
                if(!file.exists() && file.isFile())    
                {    
                	MsgBox.showM("选择的导入装配不存在，请重新选择", "提示",
							MessageBox.INFORMATION);
                }    
                
            	dispose();
            	    
            	NXImport nxImport = new NXImport(exportAssemlyPathField.getText().toString(),ugBaseDirField.getText().toString(),defaultServerURL);
            	Thread exportThread=new Thread(nxImport);
            	exportThread.start();   	
            }  
        }); 
		
		selectExportPathButton.addActionListener(new ActionListener() {              
            @Override  
            public void actionPerformed(ActionEvent e) {  
         
            	exportAssemlyPathChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int intRetVal = exportAssemlyPathChooser.showOpenDialog(NXImportDlg.this);
            	if( intRetVal == JFileChooser.APPROVE_OPTION){
            		exportAssemlyPathField.setText(exportAssemlyPathChooser.getSelectedFile().getPath());
                }
            		    
            }  
        }); 
		
		selectNXBaseDirButton.addActionListener(new ActionListener() {              
            @Override  
            public void actionPerformed(ActionEvent e) {  
         
            	nxBaseDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int intRetVal = nxBaseDirChooser.showOpenDialog(NXImportDlg.this);
            	if( intRetVal == JFileChooser.APPROVE_OPTION){
            		ugBaseDirField.setText(nxBaseDirChooser.getSelectedFile().getPath());
                }
            		    
            }  
        }); 
		
		cancelButton.addActionListener(new ActionListener() {              
            @Override  
            public void actionPerformed(ActionEvent e) {  
            	dispose();
            }  
        }); 
	}
    
 

	
	
}
