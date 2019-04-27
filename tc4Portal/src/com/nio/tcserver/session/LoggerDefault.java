package com.nio.tcserver.session;

import org.apache.log4j.Logger;

public class LoggerDefault {

	public static void logInfo(String str) {
		// System.out.println(str);
		Logger.getLogger("default").info(str);
	}

	public static void logError(String str) {
		// System.err.println(str);
		Logger.getLogger("default").error(str);
	}

	public static void logWarn(String str) {
		// System.out.println(str);
		Logger.getLogger("default").warn(str);
	}
}
