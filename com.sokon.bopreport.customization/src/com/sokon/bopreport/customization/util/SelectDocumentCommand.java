package com.sokon.bopreport.customization.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sokon.bopreport.customization.messages.ReportMessages;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.TCTree;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * 选择工艺文档对话框
 * 
 * @author zhoutong
 *
 */
public class SelectDocumentCommand extends AbstractAIFCommand 
{
	public SelectDocumentCommand(TCComponentFolder homeFolder) {
		SelectDocumentDialog selectDocumentDialog = new SelectDocumentDialog(AIFDesktop.getActiveDesktop(), homeFolder);
		setRunnable(selectDocumentDialog);
	}
}

class SelectDocumentDialog extends AbstractAIFDialog
{
	private static final long serialVersionUID = 1L;
	
	private TCTree tcTree;
	private TCComponentFolder homeFolder;

	public SelectDocumentDialog(Frame frame, TCComponentFolder homeFolder) {
		super(frame,true);
		this.homeFolder = homeFolder;
		initUI();
	}
	
	private void initUI()
	{
		setTitle(ReportMessages.getString("selectProcessDoc.Title"));
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(350, 550));
		
		JPanel mainPanel = new JPanel(new VerticalLayout(10, 10, 10, 10, 10)); 
		
		this.tcTree = new TCTree();
		this.tcTree.setRoot(this.homeFolder);
		
		JPanel buttonPanel = new JPanel(new ButtonLayout());
		JButton okButton = new JButton(ReportMessages.getString("ok.Title"));
		okButton.addActionListener(new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				okPressed();
			}
		});
		JButton cancelButton = new JButton(ReportMessages.getString("cancel.Title"));
		cancelButton.addActionListener(new IC_DisposeActionListener());
		
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		mainPanel.add("unbound.bind", new JScrollPane(this.tcTree));
		mainPanel.add("bottom.nobind.right.bottom", buttonPanel);
		mainPanel.add("bottom.bind", new Separator());
		
		add("Center", mainPanel);
		pack();
		centerToScreen();
	}

	protected void okPressed() 
	{
		InterfaceAIFComponent selectedComponent = this.tcTree.getSelectedComponent();
		if (selectedComponent == null || !selectedComponent.getType().equals("S4_IT_ProcessDoc")) 
		{
			MessageBox.post(ReportMessages.getString("noProcessDocSelected.Msg"), ReportMessages.getString("hint.Title"), 2);
			return;
		}
		
		final TCComponentItem selectedItem = (TCComponentItem) selectedComponent;
		
		AIFDesktop.getActiveDesktop().getShell().getDisplay().syncExec(new Runnable() 
		{			
			@Override
			public void run() {
				ProcessReportDialog.reportItem = selectedItem;
				ProcessReportDialog.text.setText(selectedItem.toDisplayString());
			}
		});
		
		SelectDocumentDialog.this.disposeDialog();
	}
	
}
