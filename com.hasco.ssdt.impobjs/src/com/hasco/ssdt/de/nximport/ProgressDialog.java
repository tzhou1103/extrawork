package com.hasco.ssdt.de.nximport;
//导入过程进度条
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.util.HorizontalLayout;

public class ProgressDialog extends AbstractAIFDialog{
	
	private static final long serialVersionUID = 3370147161721715283L;
	public JProgressBar progressBar=null;

	public ProgressDialog(Frame frame, String s) {
		super(frame, s);
		initialize_UI(true);
	}

	public ProgressDialog(Dialog frame, String s) {
		super(frame);
		setTitle(s);
		initialize_UI(true);
	
	}

	public ProgressDialog(Dialog dialog, String s, int minValue, int maxValue) {
		super(dialog, s);
		initialize_UI(minValue,maxValue,true);
		setBoundary(minValue, maxValue);
		setValue(0);
		
	}

	public ProgressDialog(Frame frame, String s, int minValue, int maxValue, boolean info2) {
		super(frame, s);
		initialize_UI(info2);
		setBoundary(minValue, maxValue);
		setValue(0);
	
	}

	public void initialize_UI(boolean info2) {

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setString("");
		
		this.getContentPane().setLayout(new HorizontalLayout(5,5,5,5,5));
		this.getContentPane().add("unbound", progressBar);
		centerToScreen();
		pack();
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// do nothing
			}
		});
		setModal(false);
	}
	public void initialize_UI(int minValue,int maxValue,boolean info2) {
		
		progressBar = new JProgressBar(minValue,maxValue);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setString("");

		this.getContentPane().setLayout(new HorizontalLayout(5,5,5,5,5));
		this.getContentPane().add("unbound", progressBar);
		centerToScreen();
		pack();
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// do nothing
			}
		});
		setModal(false);
	}
	public void setBoundary(int minValue, int maxValue) {
		progressBar.setMinimum(minValue);
		progressBar.setMaximum(maxValue);
	}

	public void setValue() {
		progressBar.setValue(progressBar.getValue() + 1);
	}

	public void setValue(int value) {
		progressBar.setValue(value);
		refresh();
	}

	public void setValue(String value) {
		progressBar.setString("");
		refresh();
	}

	public int getValue() {
		return progressBar.getValue();
	}

	public void refresh() {
		update(getGraphics());
	}


}
