package com.hasco.ssdt.report.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtility 
{
	public static Properties properties;
	
	/**
	 * 根据key获取配置文件config.properties中的值
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) 
	{
		String value = "";
		InputStream inputStream = null;
		try {
			if (properties == null) {
				properties = System.getProperties();
				inputStream = FileUtility.class.getClassLoader().getResourceAsStream("config.properties");
				properties.load(inputStream);
			}
			value = properties.getProperty(key);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}
	
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
	
    public static void deleteFolder(String folderPath)
    {
    	if (folderPath == null || folderPath.isEmpty()) {
			return;
		}
    	
        File folder = new File(folderPath);
        
        if ((folder.exists()) && (folder.isDirectory()))
        {
			if (folder.listFiles().length == 0) {
				folder.delete();
			} else {
                File[] subFiles = folder.listFiles();
                for (File subFile : subFiles)
                {
					if (subFile.isDirectory()) {
						deleteFolder(subFile.getAbsolutePath());
					} else {
						subFile.delete();
					}
                }
                folder.delete();
            }
        }
    }
	
	public static final String getSuffix(String fileName) 
	{
		int index = fileName.lastIndexOf('.');
		if (index != -1)
			return fileName.substring(index);
		return "";
	}

	public static final String getPrefix(String fileName) 
	{
		int index = fileName.lastIndexOf('.');
		if (index != -1)
			return fileName.substring(0, index);
		return fileName;
	}
	
	public static File renameFile(String directory, File file, String newFileName)
	{
		if (!getPrefix(file.getName()).equals(newFileName)) 
		{
			if (directory == null || directory.isEmpty()) {
				directory = file.getParent();
			}
			
			File newFile  = new File(directory, newFileName + getSuffix(file.getName()));
			if (newFile.exists()) {
				newFile.delete();
			}
			file.renameTo(newFile);
			return newFile;
		}
		
		return file;
	}
	
	public static void openFile(String filePath, String fileName) 
	{
		try {
			File file = new File(filePath, fileName);
			String[] cmd = { "cmd.exe", "/c", "start", file.getAbsolutePath() };
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void openFile(File file) 
	{
		try {
			String[] cmd = { "cmd.exe", "/c", "start", file.getAbsolutePath() };
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
