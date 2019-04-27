package com.zy.common;

import java.awt.Graphics;

import com.teamcenter.rac.util.Painter;
import com.teamcenter.rac.util.combobox.iComboBox;

public class iComboBoxM extends iComboBox {
	public void paint(Graphics g) {
		super.paint(g);
		Painter.paintIsRequired(this, g);
	}
}
