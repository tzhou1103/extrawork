package com.sokon.report;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.sokon.report.messages"; //$NON-NLS-1$
	public static String LangDlg_Chinese;
	public static String LangDlg_English;
	public static String LangDlg_Infomation;
	public static String LangDlg_SelLang;
	public static String LangDlg_SelLang2;

	public static String ProcessReport;
	public static String InvalidType;
	public static String Done;
	public static String TaskInProcess;

	public static String UpdateConfirm;
	public static String IsOverwrite;

	public static String NoPaintStatRevision;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
