package com.sokon.bopreport.customization.messages;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * ���ձ�����Ӣ�ı��ػ�
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
	 * ����key��ȡֵ
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
	 * ����key��ȡֵ����תΪ����
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
	 * ����key��ȡֵ����ƴ�Ӳ���
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
