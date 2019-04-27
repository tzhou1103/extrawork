package com.hasco.ssdt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.StringViewerDialog;


/**
 * @author Zhuang Jia
 *
 *@purpose 用于生成并显示log
 *
 *@UpdateHistory
 * 2009-3-31 根据代码检视的要求增加注释
 * 2009-9-8  增加一个直接显示log的函数
 * 
 */
public class LogAppend {
	File logFile = null;

	File filepath;

	private PrintWriter pw = null;

	public String filename;
	
	public boolean hasContent;

	public LogAppend() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		String logtime = format.format(new Date());

		filepath = new File(System.getProperty("java.io.tmpdir") + "\\logfiles");
		if (!(filepath.exists() && filepath.isDirectory()))
			filepath.mkdir();
		filename = filepath.toString();
		if (!filename.endsWith("\\")) {
			filename += "\\";
		}
		filename += "log" + logtime + ".txt";
		logFile = new File(filename);
		if (logFile.exists())
			logFile.delete();
		try {
			logFile.createNewFile();
			pw = new PrintWriter(new FileOutputStream(logFile));
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		hasContent = false;
	}

	public void AppendLog(String message) {
		if (pw != null) {
			try {
				pw.println(message);
				hasContent = true;
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {				
			}
		}
	}
	
	public void Flush() {
		if (pw != null) {
			try {
				pw.flush();
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox.post(e);
			} finally {				
			}
		}
	}
	
	public void ShowLog(AIFDesktop desktop, String title) {
		free();
		
		int width = desktop.getCurrentApplication().getApplicationPanel().getWidth() / 3 * 2;
		int height = desktop.getCurrentApplication().getApplicationPanel().getHeight() / 3 * 2;
		
		StringViewerDialog dialog = new StringViewerDialog(desktop, logFile);
		dialog.setSize(width, height);
		dialog.setTitle(title);
		dialog.setVisible(true);
	}
	
	public void ShowLog(AIFDesktop desktop, String title, String info) {
		free();
		
		int response = JOptionPane.showConfirmDialog(desktop, info, "", JOptionPane.YES_NO_OPTION);
		if(response==JOptionPane.YES_OPTION) {
			
			int width = desktop.getCurrentApplication().getApplicationPanel().getWidth() / 3 * 2;
			int height = desktop.getCurrentApplication().getApplicationPanel().getHeight() / 3 * 2;
			
			StringViewerDialog dialog = new StringViewerDialog(desktop, logFile);
			dialog.setSize(width, height);
			dialog.setTitle(title);
			dialog.setVisible(true);
		}
	}
	
	public void ShowLog() {
		free();
		try {
			String[] cmd = new String[]{"cmd.exe", "/c", "start", filename};
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void free() {
		if (pw != null) {
			pw.close();
		}
	}
	
	public void deleteLog() {
		if (logFile.exists()) {
			free();
			logFile.delete();
		}		
	}
	
}
