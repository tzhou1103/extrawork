package com.zht.customization.utils;

import java.io.File;
import java.io.IOException;

public class VBUtil {
	private String fileName = "";
	private String sheetName = "";
	private int insertColumnNum = 0;

	public VBUtil(String fileName, String sheetName, int insertColumnNum) {
		this.fileName = fileName;
		this.sheetName = sheetName;
		this.insertColumnNum = insertColumnNum;
	}

	public void doInsert() {
		if (!new File(fileName).exists())
			return;
		String tcRootPath = System.getenv("FMS_HOME").replace("tccs", "");
		String cmdString = "cmd /c " + tcRootPath + "bin\\insertColumn.exe "
				+ fileName + " " + sheetName + " " + insertColumnNum;
		System.out.println(cmdString);
		Runtime rt = Runtime.getRuntime();
		try {
			Process exec = rt.exec(cmdString);
			exec.waitFor();
		} catch (InterruptedException | IOException e) {
		}
		System.out.println("Complete insert column");
	}
}
