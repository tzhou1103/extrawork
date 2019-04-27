package com.byd.cyc.bom.utils;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

/**
 * @author zhoutong
 *
 */
public class FileUtil 
{
	public static final String getTempDir() 
	{
		String tempPath = System.getProperty("java.io.tmpdir");
		return tempPath;
	}
	
	public static final String createTempDirectory(String folderName)
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
		return dirFile.getAbsolutePath();
	}
	
	/**
	 * Base64字节转为图片文件
	 * 
	 * @param bytes
	 * @param filePath
	 */
	public static void decodeBytes2File(byte[] bytes, String filePath)
	{
		try {
			// Base64解码
			byte[] decodeBytes = Base64.decodeBase64(bytes);
			for (int i = 0; i < decodeBytes.length; ++i) {
				if (decodeBytes[i] < 0) {// 调整异常数据
					decodeBytes[i] += 256;
				}
			}
			
			//生成文件
			OutputStream outputStream = new FileOutputStream(filePath);    
			outputStream.write(decodeBytes);
			outputStream.flush();
			outputStream.close();
		} catch (Exception e)  {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取图片像素【宽度×高度】
	 * 
	 * @param imageFile
	 * @return
	 * @throws IOException
	 */
	public static int[] getImagePixel(File imageFile) throws IOException
	{
		BufferedImage bufferedImage = ImageIO.read(imageFile);
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		return new int[] { width, height };
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
	
	public static void openFile(String directory, String fileName) 
	{
		try {
			File file = new File(directory, fileName);
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
