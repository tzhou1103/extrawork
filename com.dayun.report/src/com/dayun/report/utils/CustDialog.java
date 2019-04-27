package com.dayun.report.utils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public abstract class CustDialog
  extends Dialog
{
  public static final int CUST_DIALOG_STYLE = 0x10C70 | getDefaultOrientation();
  
  public CustDialog(Shell parentShell)
  {
    super(parentShell);
  }
  
  public CustDialog(Shell parentShell, int style)
  {
    super(parentShell);
    setShellStyle(style);
  }
  
  protected Control createButtonBar(Composite paramComposite)
  {
    Control control = super.createButtonBar(paramComposite);
    Label label = new Label(paramComposite, 258);
    label.setLayoutData(new GridData(768));
    label.moveAbove(control);
    return control;
  }
  
  protected Control createDialogArea(Composite paramComposite)
  {
    Composite composite = new Composite(paramComposite, 0);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.verticalSpacing = 0;
    gridLayout.horizontalSpacing = 0;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(1808));
    composite.setFont(paramComposite.getFont());
    Label seperatorlabel = new Label(paramComposite, 258);
    seperatorlabel.setLayoutData(new GridData(768));
    seperatorlabel.moveAbove(composite);
    return composite;
  }
  
  protected Label createRequiredLabel(Composite paramComposite)
  {
    Label requiredLabel = new Label(paramComposite, 0);
    requiredLabel.setText("*");
    requiredLabel.setForeground(requiredLabel.getDisplay().getSystemColor(3));
    requiredLabel.setToolTipText("±ØÌîÏî");
    return requiredLabel;
  }
  
  protected void centerShell(Shell paramShell)
  {
    if (paramShell == null) {
      return;
    }
    Display display = paramShell.getDisplay();
    Rectangle rectangle = display.getClientArea();
    Monitor[] arrayOfMonitor = display.getMonitors();
    Monitor primaryMonitor = display.getPrimaryMonitor();
    if ((primaryMonitor != null) && (arrayOfMonitor != null) && 
      (arrayOfMonitor.length > 1)) {
      rectangle = primaryMonitor.getClientArea();
    }
    Point point = paramShell.getSize();
    paramShell.setLocation((rectangle.width - point.x) / 2, (rectangle.height - point.y) / 2);
  }
}
