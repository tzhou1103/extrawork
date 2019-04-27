package com.sokon.bopreport.customization.util;

import java.io.File;

public class FileUtility 
{
	public static final String getTempDir() 
	{
		String tempPath = System.getProperty("java.io.tmpdir");
		return tempPath;
	}
	
	public static final synchronized File createTempDirectory(String folderName) 
	{
		File dirFile = null;
		int index = 0;
		do 
		{
			StringBuffer stringBuffer = new StringBuffer(getTempDir());
			stringBuffer.append(File.separator);
			stringBuffer.append(folderName.trim());
			stringBuffer.append("_");
			stringBuffer.append(index++);
			stringBuffer.append(Long.toString(System.currentTimeMillis(), 36));
			dirFile = new File(stringBuffer.toString());
		} while (dirFile.exists());
		dirFile.mkdirs();
		return dirFile;
	}
	
	public static final boolean deleteDirectoryTree(File parentFile) 
	{
		if ((parentFile != null) && (parentFile.isDirectory())) 
		{
			File[] arrayOfFile = parentFile.listFiles();
			int index = 1;
			for (int j = 0; j < arrayOfFile.length; j++) 
			{
				File file = arrayOfFile[j];
				boolean bool;
				if (file.isDirectory())
					bool = deleteDirectoryTree(file);
				else
					bool = file.delete();
				index = (index != 0) && (bool) ? 1 : 0;
			}
			if (index != 0)
				return parentFile.delete();
			return false;
		}
		return false;
	}
}
