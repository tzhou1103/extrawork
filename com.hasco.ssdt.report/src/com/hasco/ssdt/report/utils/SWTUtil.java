package com.hasco.ssdt.report.utils;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class SWTUtil 
{	
	/**
	 * ÉèÖÃShell¾ÓÖÐ
	 * 
	 * @param paramShell
	 */
	public static void centerShell(Shell paramShell) 
	{
		if (paramShell == null) {
			return;
		}
		Display display = paramShell.getDisplay();
		Rectangle rectangle = display.getClientArea();
		Monitor[] arrayOfMonitor = display.getMonitors();
		Monitor primaryMonitor = display.getPrimaryMonitor();
		if ((primaryMonitor != null) && (arrayOfMonitor != null)
				&& (arrayOfMonitor.length > 1)) {
			rectangle = primaryMonitor.getClientArea();
		}
		Point point = paramShell.getSize();
		paramShell.setLocation((rectangle.width - point.x) / 2, (rectangle.height - point.y) / 2);
	}
	
}
