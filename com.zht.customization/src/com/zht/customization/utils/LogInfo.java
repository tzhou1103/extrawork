package com.zht.customization.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LogInfo {
	public static void writeLog(String args) {
		// ��1����ʹ��File���ҵ�һ���ļ�
		File f = new File("C:" + File.separator + "BOM��ϸ����־.txt"); // ����File����
		Writer out = null; // ׼����һ������Ķ���

		try {
			// ��2����ͨ������ʵ�����������
			if (!f.exists())
				f.createNewFile();

			out = new FileWriter(f, true); // ͨ�������̬�ԣ�����ʵ����
			// ��3��������д����
			// String str = "LIXINGHUA\r\nHello World!!!";// ׼��һ���ַ���
			out.write(args);// ����������������ļ�
			// ��4�����ر������
			
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
		File f = new File("C:" + File.separator + "BOM��ϸ����־.txt"); // ����File����
		if (f.exists())
			f.delete();
	}
	
}
