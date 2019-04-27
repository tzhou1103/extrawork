package com.zht.customization.dialogs;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.util.PropertyLayout;

public class ProgressDialog implements Runnable {
	
	ProgressingDialog progressingDialog;
	
	int totalCount = 0;
	String info = "";
	String detail_info = "";
	
	Frame frame = null ;
	
	int currentCount = 0;
	
	
	public ProgressDialog(String s) {
		totalCount = -1;
		info = s;
	}

	
	public ProgressDialog(int count, String s) {
		totalCount = count;
		info = s;
	}
	
	public ProgressDialog(int count, String s1, String s2) {
		totalCount = count;
		info = s1;
		detail_info = s2;
	}
	
	public ProgressDialog(Frame frame ,int count, String s) {
		this.frame = frame ;
		totalCount = count;
		info = s;
	}
	
	
	public void show() {
		new Thread(this).run();
	}
	
	
	public void setInfo(String s) {
		progressingDialog.setInfo(s);
	}
	
	
	public void setDetailedInfo(String s) {
		progressingDialog.setDetailedInfo(s);
	}
	
	
	public void addValue() {
		progressingDialog.setValue(currentCount);
		currentCount++;
	}
	
	
	public void addValue(int count) {
		progressingDialog.setValue(currentCount);
		currentCount = currentCount + count;
	}
	
	
	public void setValue(int count) {
		currentCount = count;
		progressingDialog.setValue(currentCount);
	}
	
	
	public void disposeDialog() {
		
		if (totalCount == 0) {
			return;
		}
		
		if (totalCount == -1) {
			progressingDialog.disposeDialog();
		}
		else {
			progressingDialog.setValue(totalCount + 1);
		}
	}
	public void run() {
		if (frame!=null) {
			progressingDialog = new ProgressingDialog(frame ,totalCount);
		} else {
			progressingDialog = new ProgressingDialog(totalCount);
		}
		
		progressingDialog.showDialog();
	}

	
	class ProgressingDialog extends AbstractAIFDialog {

		public JPanel panel;
		public JLabel infoLabel;
		public JLabel detailedInfoLabel;
		public JProgressBar progressBar;
		int totalCount;


		public ProgressingDialog(int count) {
			super(AIFDesktop.getActiveDesktop(), "");
			
			totalCount = count;
			//setAlwaysOnTop(true);
			initUI();
			
			setInfo(info);
		}

		public ProgressingDialog(Frame frame ,int count) {
			super(frame , false);
			
			totalCount = count;
			//setAlwaysOnTop(true);
			initUI();
			
			setInfo(info);
		}
		public void initUI() {
			setTitle("进度信息");
			panel = new JPanel(new PropertyLayout(10, 10, 10, 10, 15, 15));
			panel.setPreferredSize(new Dimension(590, 100));
			infoLabel = new JLabel(info);
			detailedInfoLabel = new JLabel(detail_info);
			
			progressBar = new JProgressBar();
			progressBar.setBorderPainted(true);
			
			if (totalCount == -1) {
				progressBar.setIndeterminate(true);
				progressBar.setPreferredSize(new Dimension(550, 30));
			}
			else {
				progressBar.setMinimum(1);
				progressBar.setMaximum(550);
				
				progressBar.setStringPainted(true);
			}

			panel.add("1.1.left.center.preferred.preferred", infoLabel);
			panel.add("2.1.center.center.preferred.preferred", detailedInfoLabel);
			panel.add("3.1.center.center.preferred.preferred", progressBar);
			
			setResizable(false);			
			getContentPane().add(panel);
			centerToScreen();
			//pack();
			
		}


		public void setValue(int currentCount) {
			
			if (totalCount == 0) {
				setVisible(false);
				return;
			}
			
			progressBar.setValue((currentCount + 1) * 100 / totalCount);
			update(getGraphics());
			
			if ((currentCount + 1) >= totalCount) {
				setVisible(false);
			}
		}


		public void setInfo(String info) {
			infoLabel.setText(info);
			update(getGraphics());
		}
		
		
		public void setDetailedInfo(String info) {
			detailedInfoLabel.setText(info);
			update(getGraphics());
		}
	}
}
