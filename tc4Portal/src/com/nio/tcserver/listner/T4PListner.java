package com.nio.tcserver.listner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;

import com.nio.tcserver.session.SessionPoolManager;
import com.nio.tcserver.session.T4PContext;

public class T4PListner implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (!T4PContext.isDebug)
			SessionPoolManager.logoutAll();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();
		T4PContext.initWebValue(sc);

		System.setProperty("serv.context", sc.getServletContextName());

		PropertyConfigurator.configure(sc.getRealPath("/") + "WEB-INF\\classes\\log4j.xml");
	}

}
