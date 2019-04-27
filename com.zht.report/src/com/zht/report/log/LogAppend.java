package com.zht.report.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.teamcenter.rac.util.MessageBox;

public class LogAppend 
{
	private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
	private String logPath;
	private File logFile;
	private PrintWriter pw;
	private String logName;
	public LogAppend(String logPath) throws Exception
	{
		this.logPath = logPath;
		File logDir = new File(this.logPath);
		if ((!logDir.exists()) || (!logDir.isDirectory()))
			logDir.mkdir();
		if ((!this.logPath.endsWith("\\")) || (!this.logPath.endsWith("/")))
		{
			this.logPath = this.logPath + File.separatorChar;
		}
		this.logName = this.logPath + "Log_VehicleDetailReport_" + format.format(new Date()) + ".log";
		this.logFile = new File(this.logName);
		if (this.logFile.exists())
			this.logFile.delete();
		this.logFile.createNewFile();
		this.pw = new PrintWriter(new FileOutputStream(this.logFile));
	}
	
	public LogAppend(String logPath, String logFileName) throws Exception
	{
		this.logPath = logPath;
		File logDir = new File(this.logPath);
		if ((!logDir.exists()) || (!logDir.isDirectory()))
			logDir.mkdir();
		if ((!this.logPath.endsWith("\\")) || (!this.logPath.endsWith("/")))
		{
			this.logPath = this.logPath + File.separatorChar;
		}
		this.logName = this.logPath + logFileName + format.format(new Date()) + ".log";
		this.logFile = new File(this.logName);
		if (this.logFile.exists())
			this.logFile.delete();
		this.logFile.createNewFile();
		this.pw = new PrintWriter(new FileOutputStream(this.logFile));
	}
	
	public String getLogFilePath()
	{
		return this.logName;
	}
	
	public void messageLog(String message)
	{
		if (this.pw != null)
		{
			this.pw.println(message);
		}
	}
	
	public void exceptionLog(Exception e)
	{
		if (this.pw != null)
		{
			e.printStackTrace(this.pw);
			this.pw.println("");
		}
	}
	
	public void close() 
	{
		if (this.pw != null)
		{
			this.pw.flush();
			this.pw.close();
		}
	}
	
	public void displayLog()
	{
		if (logFile.isFile() && logFile.canRead())
		{
			SwingUtilities.invokeLater(new Runnable() 
			{
				@Override
				public void run() 
				{
					try 
					{
						StringViewerCommand stringViewerCommand = new StringViewerCommand(logFile);
						stringViewerCommand.executeModal();
					} catch (Exception e) 
					{
						e.printStackTrace();
						MessageBox.post(e);
					}
				}
			});
		}
	}
	
}
