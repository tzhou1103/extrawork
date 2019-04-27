package com.zht.customization.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LogInfo {
	public static void writeLog(String args) {
		// 第1步、使用File类找到一个文件
		File f = new File("C:" + File.separator + "BOM明细表日志.txt"); // 声明File对象
		Writer out = null; // 准备好一个输出的对象

		try {
			// 第2步、通过子类实例化父类对象
			if (!f.exists())
				f.createNewFile();

			out = new FileWriter(f, true); // 通过对象多态性，进行实例化
			// 第3步、进行写操作
			// String str = "LIXINGHUA\r\nHello World!!!";// 准备一个字符串
			out.write(args);// 将内容输出，保存文件
			// 第4步、关闭输出流
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void deleteLogFile(){
		File f = new File("C:" + File.separator + "BOM明细表日志.txt"); // 声明File对象
		if (f.exists())
			f.delete();
	}
	
}
