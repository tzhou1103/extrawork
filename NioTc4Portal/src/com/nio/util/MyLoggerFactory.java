package com.nio.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MyLoggerFactory 
{
	public static Logger getLogger(Class<?> my_class) 
	{
		Logger log = Logger.getLogger(my_class);
		Properties properties = new Properties();
		try 
		{
			properties.load(MyLoggerFactory.class.getClassLoader().getResourceAsStream("log4j.properties"));
			if (properties != null)
				PropertyConfigurator.configure(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return log;
	}
}
