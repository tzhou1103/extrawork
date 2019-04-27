package com.hasco.ssdt.oem.nximport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 通用的等待对话框，提示用户程序正在执行
 * @author Administrator
 *
 */
public class WaitingDialog extends com.teamcenter.rac.aif.AbstractAIFDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JPanel panel ;
	
	public WaitingDialog(Frame arg0, String arg1, String message)
	{
		super(arg0, arg1);
		this.setModal(false);
		
		panel = new JPanel(new BorderLayout());
		
		JLabel label = new JLabel(message);
		
		panel.add(BorderLayout.CENTER, label);
		
		panel.setPreferredSize(new Dimension(350,60));
		
		this.getContentPane().add(panel);
		
		centerToScreen();

		pack();
		
	}
}
