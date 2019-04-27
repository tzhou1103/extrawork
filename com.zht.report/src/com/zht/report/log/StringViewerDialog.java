package com.zht.report.log;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.HTMLViewerPanel;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;

public class StringViewerDialog extends AbstractAIFDialog 
{
	private static final long serialVersionUID = 1L;
	
	private String str;
	private String htmlFile;
	private boolean htmlAtFirst = true;
	protected HTMLViewerPanel viewerPanel;
	protected Frame parentFrame;
	private boolean isXML;

	public StringViewerDialog(Frame paramFrame, File paramFile) {
		super(paramFrame, true);
		this.parentFrame = paramFrame;
		this.str = getStringFromFile(paramFile, null);
		this.htmlFile = writeHTMLFile(this.str);
		initializeDialog();
	}

	public void setEditable(boolean paramBoolean) {
		this.viewerPanel.setEditable(paramBoolean);
	}

	public void enableFind() {
		this.viewerPanel.enableFind();
	}

	private String getStringFromFile(File paramFile, String paramString)
	{
		BufferedReader localBufferedReader = null;
		try 
		{
			if (paramString != null)
				localBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(paramFile), paramString));
			else
				localBufferedReader = new BufferedReader(new FileReader(paramFile));
			StringBuilder localStringBuilder = new StringBuilder();
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				localStringBuilder.append(str1);
				localStringBuilder.append(System.getProperty("line.separator"));
			}
			this.str = localStringBuilder.toString();
		} catch (IOException localIOException2) 
		{
			MessageBox.post(localIOException2);
		} finally {
			if (localBufferedReader != null)
				try {
					localBufferedReader.close();
				} catch (IOException localIOException4) {
					localIOException4.printStackTrace();
				}
		}
		return this.str;
	}

	@SuppressWarnings("unused")
	private String getStringFromArray(String[] paramArrayOfString) 
	{
		StringBuilder localStringBuilder = new StringBuilder();
		for (String str1 : paramArrayOfString) {
			localStringBuilder.append(str1);
			localStringBuilder.append(System.getProperty("line.separator"));
		}
		return localStringBuilder.toString();
	}

	private void initializeDialog() {
		Registry localRegistry = Registry.getRegistry("com.teamcenter.rac.util.util");
		JLabel localJLabel = new JLabel(localRegistry.getImageIcon("viewer.ICON"), 0);
		setTitle(localRegistry.getString("viewer.TITLE"));
		JPanel localJPanel1 = new JPanel(new VerticalLayout(5, 2, 2, 2, 2));
		getContentPane().add(localJPanel1);
		JPanel localJPanel2 = new JPanel(new HorizontalLayout());
		JCheckBox localJCheckBox1 = new JCheckBox(localRegistry.getString("html.TITLE"), this.htmlAtFirst);
		localJCheckBox1.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				if (!StringViewerDialog.this.viewerPanel.isHTML())
					StringViewerDialog.this.loadViewerPanel(true);
			}
		});
		JCheckBox localJCheckBox2 = new JCheckBox(localRegistry.getString("text.TITLE"), !this.htmlAtFirst);
		localJCheckBox2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				if (StringViewerDialog.this.viewerPanel.isHTML())
					StringViewerDialog.this.loadViewerPanel(false);
			}
		});
		if (!this.isXML) {
			ButtonGroup localObject = new ButtonGroup();
			((ButtonGroup) localObject).add(localJCheckBox1);
			((ButtonGroup) localObject).add(localJCheckBox2);
			localJPanel2.add("right", localJCheckBox2);
			localJPanel2.add("right", localJCheckBox1);
		}
		this.viewerPanel = new HTMLViewerPanel();
		JPanel localObject = new JPanel(new ButtonLayout());
		JButton localJButton = new JButton(localRegistry.getString("close"));
		localJButton.setMnemonic(localRegistry.getString("close.MNEMONIC").charAt(0));
		localJButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
				StringViewerDialog.this.setVisible(false);
				StringViewerDialog.this.dispose();
			}
		});
		((JPanel) localObject).add(localJButton);
		localJPanel1.add("top.nobind.left", localJLabel);
		localJPanel1.add("top.bind.center.center", new Separator());
		localJPanel1.add("top.bind.center.center", localJPanel2);
		localJPanel1.add("unbound.bind.center.top", this.viewerPanel);
		localJPanel1.add("bottom.bind.center.top", localObject);
		localJPanel1.add("bottom.bind", new Separator());
		centerToScreen(1.5D, 1.0D, 0.5D, 0.45D);
		if ((this.htmlFile != null)
				&& (this.htmlFile.length() > HTMLViewerPanel.getHTMLDisplayLimit())) {
			if (HTMLViewerPanel.continuePrintHTML(this.parentFrame, this.htmlFile)) {
				localJCheckBox2.doClick();
				loadViewerPanel(false);
				localJCheckBox1.setEnabled(false);
			} else {
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run() {
						StringViewerDialog.this.setVisible(false);
						StringViewerDialog.this.dispose();
					}
				});
			}
		} else
			loadViewerPanel(this.htmlAtFirst);
	}

	protected String beginHTML(StringBuilder paramStringBuilder) {
		Registry localRegistry = Registry.getRegistry(this);
		String str1 = new StringBuilder().append("<HTML><TITLE>").append(
				localRegistry.getString("tcReport.TITLE")).append(
				"</TITLE><BODY>").toString();
		if (paramStringBuilder != null)
			paramStringBuilder.append(str1);
		return str1;
	}

	private String writeHTMLFile(String paramString) {
		String str1 = System.getProperty("line.separator");
		String str2 = new StringBuilder().append(beginHTML(null)).append(
				"<pre>").append(paramString).append(str1).append("</pre>")
				.append(HTMLViewerPanel.endHTML(null)).toString();
		return str2;
	}

	protected void loadViewerPanel(boolean paramBoolean) 
	{
		if (!paramBoolean)
			this.viewerPanel.setMonospaceFont(true);
		if (paramBoolean)
			this.viewerPanel.setText(this.htmlFile);
		else
			this.viewerPanel.setText(this.str);
	}

}
