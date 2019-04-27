package com.hasco.ssdt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.teamcenter.rac.util.Shell;

public class NXShell {

	public int returnCode = 0;
	String cmdFilePath = "";
	PrintWriter uglog;
	PrintWriter ugerrorlog;
	String m_commands[] = null;
	
	public NXShell(String cmdFilePath,PrintWriter uglog)
	{
	   this.cmdFilePath = cmdFilePath;
	   this.uglog = uglog;							
	}
	public NXShell(String cmdFilePath)
	{
	   this.cmdFilePath = cmdFilePath;						
	}
	
	public void run()
	{	
		
	   Shell s = new Shell(cmdFilePath);
	   s.run();		
	   returnCode = s.getState();
		
		
	}
	
	public void runCmd()
	{	
		try
	    {
	      Process localProcess = Runtime.getRuntime().exec(cmdFilePath);
	      new IC_StreamManagementThread().run(localProcess.getInputStream());
	      new IC_StreamManagementThread().run(localProcess.getErrorStream());
	      localProcess.waitFor();
	      returnCode = localProcess.exitValue();
	    }
	    catch (IOException localIOException)
	    {
	       uglog.println(localIOException.getLocalizedMessage());
	    } catch (InterruptedException e) {
			
	       uglog.println(e.getLocalizedMessage());
		}
		
		
	}
	
    public class IC_StreamManagementThread extends Thread
	{
	  
	    public void run(InputStream arg4)
	    {
	      BufferedReader m_reader = new BufferedReader(new InputStreamReader(arg4));
		  start();
	      StringBuilder localStringBuilder = new StringBuilder();
	      try
	      {
	        String str = null;
	        while ((str = m_reader.readLine()) != null)
	          localStringBuilder.append(str).append('\n');
	      }
	      catch (Exception localException)
	      {
	    	 uglog.println(localException.getLocalizedMessage());
	      }
	      try
	      {
	         m_reader.close();
	      }
	      catch (IOException localIOException)
	      {
	    	  uglog.println(localIOException.getLocalizedMessage());
	      }
	      if (localStringBuilder.length() > 0)
	      {
	    	  uglog.println(localStringBuilder.toString());
	      }
	    	 
	        
	    }
	  }

}
