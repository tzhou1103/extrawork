package com.sokon.bopreport.customization.messages;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * 工艺报表中英文本地化
 * 
 * @author zhoutong
 *
 */
public class ReportMessages 
{
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("com.sokon.bopreport.customization.messages.messages_locale");

	static {
		NLS.initializeMessages("com.sokon.bopreport.customization.messages.messages_locale", ReportMessages.class);
	}

	/**
	 * 根据key获取值
	 * 
	 * @param key key
	 * @return
	 */
	public static String getString(String key) 
	{
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException localMissingResourceException) {
			
		}
		return '!' + key + '!';
	}
	
	/**
	 * 根据key获取值，并转为整数
	 * 
	 * @param key key
	 * @return
	 */
	public static int getIntValue(String key) 
	{
		try {
			return Integer.valueOf(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException localMissingResourceException) {
			
		}
		return -1;
	}

	/**
	 * 根据key获取值，并拼接参数
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public static String getString(String key, Object... values)
	{
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key), values);
		} catch (MissingResourceException localMissingResourceException) {
			
		}
		return '!' + key + '!';
	}

	public static ResourceBundle getBundle() {
		return RESOURCE_BUNDLE;
	}
}
