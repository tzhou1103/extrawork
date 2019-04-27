package com.nio.tcserver.session;

import java.rmi.ConnectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.nio.tcserver.common.T4PUtils;
import com.teamcenter.clientx.AppXSession;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.User;

public class SessionPoolManager {

	public static HashMap<String, AppXSession> ssPool = new HashMap<String, AppXSession>();

	public static AppXSession getUserSession() throws ConnectException {
		return getUserSession(T4PContext.username, T4PContext.password);
	}

	public static AppXSession getDefaultSession() {

		String tmpMd5 = getMd5(T4PContext.username + "___" + T4PContext.password);
		AppXSession session = ssPool.get(tmpMd5);

		return session;
	}

	public static AppXSession getUserSession(String username, String password) throws ConnectException {

		String tmpMd5 = getMd5(username + "___" + password);
		AppXSession session = ssPool.get(tmpMd5);

		int count = 0;
		User user = null;
		String user_id = null;
		while (user == null || user_id == null) {
			count++;

			try {
				boolean flag = session == null || session.getConnection() == null;

				if (flag) {
					session = new AppXSession(T4PContext.tc_url);
					user = session.login(username, password);

					ssPool.put(tmpMd5, session);

					T4PUtils.setbypass(session, "1");

				} else {

//					try {
//						T4PUtils.setbypass(session, "1");
//
//						SessionService ssSrv = SessionService.getService(session.getConnection());
//						user = ssSrv.getTCSessionInfo().user;
//
//					} catch (Exception e) {
//						e.printStackTrace();

						user = session.login(username, password);
						T4PUtils.setbypass(session, "1");
//					}

				}
				
				try {
					DataManagementService dmService = DataManagementService.getService(session.getConnection());
					T4PUtils.serviceDataErrorCheck(dmService.refreshObjects2(new ModelObject[] { user }, false));
					LoggerDefault.logInfo("======Do service Data Error Check======");
				} catch (Exception e) {
					LoggerDefault.logError("User session is invalid!");
					e.printStackTrace();
				}
				user_id = user.get_user_id();

				if (flag) {
					LoggerDefault.logInfo("User login : " + user_id);
				} else {
					LoggerDefault.logInfo("User is already login : " + user_id);
				}

			} catch (Exception e) {
				e.printStackTrace();
				if (count > 2) {
					LoggerDefault.logError("Teamcenter login failed : " + e.getMessage());
					throw new ConnectException("Teamcenter login failed : " + e.getMessage());
				}
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e1) {
				}
				LoggerDefault.logError("Retry to login...... count : " + count);
			}
		}
		return session;
	}

	public static String getMd5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			return toHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static void logoutAll() {

		for (Entry<String, AppXSession> entry : ssPool.entrySet()) {
			AppXSession thissession = entry.getValue();
			try {
				if (thissession != null) {

					try {
						T4PUtils.setbypass(thissession, "0");
					} catch (Exception e) {
						e.printStackTrace();
					}

					thissession.logout();
				}
			} catch (Exception e) {
				//
			}
		}

	}
}
