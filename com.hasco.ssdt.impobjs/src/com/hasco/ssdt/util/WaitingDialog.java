package com.hasco.ssdt.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.teamcenter.rac.aif.AbstractAIFDialog;

@SuppressWarnings("serial")
public class WaitingDialog extends AbstractAIFDialog
{
    public JPanel panel;

    JLabel label;
    
    public WaitingDialog(Frame arg0, String arg1, String message)
    {
	super(arg0, arg1);
	this.setModal(false);

	panel = new JPanel(new BorderLayout());

	label = new JLabel(message);

	panel.add(BorderLayout.CENTER, label);

	panel.setPreferredSize(new Dimension(250, 32));

	this.getContentPane().add(panel);

	centerToScreen();

	pack();

    }
    
    public void setMessage(String message) {
		label.setText(message);
		this.repaint();
	}
}
