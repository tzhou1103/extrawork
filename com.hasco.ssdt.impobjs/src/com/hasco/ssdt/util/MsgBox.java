package com.hasco.ssdt.util;

import com.teamcenter.rac.util.MessageBox;

public class MsgBox {
	
	public static void show(String content, String title, int stat){
		MessageBox mbx = new MessageBox(content, title, stat);
		mbx.setVisible(true);
	}
	
	public static void showM(String content, String title, int stat){
		MessageBox mbx = new MessageBox(content, title, stat);
		mbx.setModal(true);
		mbx.setVisible(true);
	}

}
