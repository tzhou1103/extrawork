package com.zy.common;

import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.BorderFactory;

import com.teamcenter.rac.util.iTextField;

public class YLabTextField extends iTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public YLabTextField(int i, int j, boolean flag) {
		super(null, null, i, j, flag, null);

		Color BKColor = SystemColor.control;

		setBorder(BorderFactory.createLineBorder(BKColor));
		setBackground(BKColor);

		setHorizontalAlignment(iTextField.CENTER);
		setFocusable(false);
		setOpaque(true);

	}

	public YLabTextField(String Text, int Len) {
		this(Len, 100, false);
		setText(Text);
	}

	public YLabTextField(String Text, int Len, int Alignment) {
		this(Len, 100, false);
		setText(Text);
		setHorizontalAlignment(Alignment);
	}
}
