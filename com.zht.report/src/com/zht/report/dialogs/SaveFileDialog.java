package com.zht.report.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;
import com.zht.report.jobs.GenerateProductCategoryJob;
import com.zht.report.jobs.GenerateVehicleDetailReportJob;
//import com.zht.report.log.StringViewerCommand;

public class SaveFileDialog extends AbstractAIFDialog
{
	private static final long serialVersionUID = 1L;
	
	private String title;
	private TCComponentBOMLine targetBOMLine;
	private JTextField filePathField;
	
	public SaveFileDialog(Frame paramFrame, String paramTitle, TCComponentBOMLine paramBOMLine)
	{
		super(paramFrame, true);
		this.title = paramTitle;
		this.targetBOMLine = paramBOMLine;
		initUI();
	}

	private void initUI()
	{
		setTitle(this.title);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(450, 150));
		
		JPanel mainPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		
		JLabel selectFileLabel = new JLabel(ZHTConstants.SELECTFILE);
		
		JPanel selectFilePanel = new JPanel(new HorizontalLayout(5, 5, 5, 5, 5));
		
		this.filePathField = new JTextField();
		this.filePathField.setEditable(false);
		JButton browserButton = new JButton(ZHTConstants.BROWSER);
		browserButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();  	      
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(ZHTConstants.EXCEL_FILEFLITERNAMES, "xlsx");  
			    chooser.setFileFilter(filter);  
			    int option = chooser.showSaveDialog(null);  
			    if(option == JFileChooser.APPROVE_OPTION)
			    {    
			        File file = chooser.getSelectedFile();  
			        String filename = chooser.getName(file);
					if (filename.indexOf(".xlsx") == -1) {
						file = new File(chooser.getCurrentDirectory(), filename + ".xlsx");
					}
			        SaveFileDialog.this.filePathField.setText(file.getAbsolutePath());
			    }
			}
		});
		
		selectFilePanel.add("unbound.bind", this.filePathField);
		selectFilePanel.add("right.bind", browserButton);
		
		JPanel buttonPanel = new JPanel(new ButtonLayout(ButtonLayout.RIGHT));
		
		JButton executeButton = new JButton(ZHTConstants.EXECUTE);
		executeButton.addActionListener(new ActionListener()
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String filePath =  SaveFileDialog.this.filePathField.getText();
				if (filePath == null || filePath.equals("")) {
					JOptionPane.showMessageDialog(SaveFileDialog.this, ZHTConstants.NOTSELCTEDFILEPATH_MSG, ZHTConstants.ERROR, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				disposeDialog();
				
				if (SaveFileDialog.this.title.equals(ZHTConstants.PRODUCTCATEGORY)) 
				{
					GenerateProductCategoryJob generateCategoryJob = new GenerateProductCategoryJob(ZHTConstants.JOB_TITLE, SaveFileDialog.this.targetBOMLine, filePath);
					generateCategoryJob.addJobChangeListener(new JobChangeAdapter()
					{
						@Override
						public void done(IJobChangeEvent event) 
						{
							GenerateProductCategoryJob generateCategoryJob = (GenerateProductCategoryJob) event.getJob();
							if (generateCategoryJob.isCompleted()) {
								MessageBox.post(ZHTConstants.PRODUCTCATEGORY_COMPLTED_MSG, ZHTConstants.HINT, 2);
							}
						}
					});
					generateCategoryJob.setPriority(10);
					generateCategoryJob.setUser(true);
					generateCategoryJob.schedule();
				} 
				else if (SaveFileDialog.this.title.equals(ZHTConstants.VEHICLEDTAILRPORT)) 
				{
					GenerateVehicleDetailReportJob generateReportJob = new GenerateVehicleDetailReportJob(ZHTConstants.JOB_TITLE, SaveFileDialog.this.targetBOMLine, filePath);
					generateReportJob.addJobChangeListener(new JobChangeAdapter()
					{
						@Override
						public void done(IJobChangeEvent event) 
						{
							GenerateVehicleDetailReportJob generateReportJob = (GenerateVehicleDetailReportJob) event.getJob();
							if (generateReportJob.isCompleted()) {
								MessageBox.post(ZHTConstants.VEHICLEDTAILRPORT_COMPLTED_MSG, ZHTConstants.HINT, 2);
							} else {
								String logPath = generateReportJob.getLogFilePath();
								if ((logPath != null) && (!logPath.equals("")))
								{
									final File logFile = new File(logPath);
									if ((logFile.isFile()) && (logFile.canRead()))
									{
										SwingUtilities.invokeLater(new Runnable() 
										{
											public void run() {
												/*
												try {
													StringViewerCommand stringViewerCommand = new StringViewerCommand(logFile);
													stringViewerCommand.executeModal();
												} catch (Exception e) {
													e.printStackTrace();
													MessageBox.post(e);
												}
												*/
												Runtime runtime = Runtime.getRuntime();
												String cmd = "rundll32 url.dll FileProtocolHandler file://" + logFile.getAbsolutePath();
												try {
													runtime.exec(cmd);
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
										});
									}
					            }
					            new File(logPath).deleteOnExit();
							}
						}
					});
					generateReportJob.setPriority(10);
					generateReportJob.setUser(true);
					generateReportJob.schedule();
				}
			}
		});
		
		JButton closeButton = new JButton(ZHTConstants.CLOSE);
		closeButton.addActionListener(new AbstractAIFDialog.IC_DisposeActionListener());
		
		buttonPanel.add(executeButton);
		buttonPanel.add(closeButton);
		
		mainPanel.add("top.bind.left.top", selectFileLabel);
		mainPanel.add("top.bind", selectFilePanel);
		
		mainPanel.add("bottom.nobind.right.bottom", buttonPanel);
		mainPanel.add("bottom.bind", new Separator());
		
		add("Center", mainPanel);
		pack();
		centerToScreen();
	}
	
}
