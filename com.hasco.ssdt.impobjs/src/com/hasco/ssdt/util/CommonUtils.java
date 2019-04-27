package com.hasco.ssdt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * 
 * @author xiaolei
 * 
 */

public class CommonUtils {

	/**
	 * 判断revision是否为最新版本
	 * 
	 * @param rev
	 * @return true or false
	 */
	public static boolean isLastRevision(TCComponentItemRevision rev) {
		try {
			TCComponentItem item = rev.getItem();
			if (item.getLatestItemRevision().equals(rev))
				return true;
		} catch (TCException e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
	
	public static String replaceSpaceChar(String s) {
		if (s == null || s.trim().length() < 1)
			return "";
		StringBuffer buff = new StringBuffer();
		boolean toreplace = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == ' ') {
				if (!toreplace) {
					buff.append("_");
					toreplace = true;
				}
			} else {
				buff.append(c);
				toreplace = false;
			}
		}
		return buff.toString();
	}
	
	
	public static String handleSpecialChar(String s) {
		if (s == null)
			return "";

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '/' || c == ':' || c == '?' || c == '*' || c == '\t'
					|| c == '<' || c == '>' || c == '|' || c == '\\'
					|| c == '\"' || c == '$') { 
				buff.append("_");
			} else {
				buff.append(c);
			}
		}

		return buff.toString();
	}

	public static PrintWriter mkLog(String filePath,String logName) throws IOException
	{
		File ugexportlogfile = new File(filePath,logName);
		if (ugexportlogfile.exists())
			ugexportlogfile.delete();
		
		ugexportlogfile.createNewFile();
		PrintWriter uglog = new PrintWriter(new FileOutputStream(ugexportlogfile));
		
		return uglog;
		
	}
	public static boolean checkBlank(String str){      
        Pattern pattern = Pattern.compile("[\\s]+");
        Matcher matcher = pattern.matcher(str);
        boolean flag = false;
        while (matcher.find()) {
            flag = true;
        }
        return flag;
   }
	
   public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
	
	
	  public static void ShowLog(String filePath,String fileName) {
		    try { 
		    	File logFile = new File(filePath,fileName);
		    	System.out.println("AbsolutePath:"+logFile.getAbsolutePath());
		    	System.out.println("CanonicalPath:"+logFile.getCanonicalPath());

		      String[] cmd = { "cmd.exe", "/c", "start", logFile.getAbsolutePath() };
		      Runtime.getRuntime().exec(cmd);
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		  }
	  
	   public static String removeSpecilChar(String str){
	    	String result = "";
	    	if(null != str){
	    	Pattern pat = Pattern.compile("\\r|\\n|\\t");
	    	Matcher mat = pat.matcher(str);
	    	result = mat.replaceAll("");
	    	}
	    	return result;
	    } 
	/**
	 * 判断版本是否发放
	 * 
	 * @param rev
	 * @return true or false
	 */
	public static boolean isReleasedRevision(TCComponentItemRevision rev) {
		try {
			String release_status = rev.getProperty("release_status_list");
			if (release_status != null && !release_status.equals(""))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * 判断是否为最新发放版本
	 * 
	 * @param rev
	 * @return true or false
	 */
	public static boolean isLastReleasedRevision(TCComponentItemRevision rev) {
		return isLastRevision(rev) && isReleasedRevision(rev);
	}

	public static String getLOVDescByValue(TCSession session, String lovName,
			String value) {
		try {
			TCComponentListOfValues lov = TCComponentListOfValuesType
					.findLOVByName(session, lovName);

			if (lov != null) {
				Object[] values = lov.getListOfValues().getListOfValues();

				for (int i = 0; i < values.length; i++) {
					if (values[i].equals(value)) {
						return lov.getListOfValues().getDescriptions()[i];
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return "";
	}
	
	/**
	 * 判断字符串是否全为数字
	 * 
	 * @param str
	 * @return true or false
	 */
	public static boolean isNumeric(String str){
		for (int i = 0; i < str.length(); i++){
			if (!Character.isDigit(str.charAt(i)) && str.charAt(i) != '.'){
				return false;
			}
		}
		return true;
	}
	

	/**
	 * 判断字符串是否全为字母
	 * 
	 * @param str
	 * @return true or false
	 */
	public static boolean hasLetter(String str){
		for (int i = 0; i < str.length(); i++){
			if (!Character.isLetter(str.charAt(i))){
				return false;
			}
		}
		return true;
	}
	/**
	 * 判断字符串是否能够转化为数字
	 * 
	 * @param str
	 * @return true or false
	 */
	public static boolean isNumber(String str){
  
	   // return str.matches("-*\\d+\\.?\\d*");
		return str.matches("^[-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?[dD]?$");

	}
	 /* 连接字符
	 * 新增@ZhangMengLong
	 * 2013.1.7
	 */
	public static String combString(String objectString, String tdm9_frequency,
			String tdm9_time, String orderNumber, String object_desc,
			String tdm9_characteristic_value, String string_value,
			String charUnit, String testCondition, String comments) {
		
		String transValue = "tdm9_name=" + objectString + "@&@tdm9_frequency="
				+ tdm9_frequency + "@&@tdm9_time=" + tdm9_time
				+ "@&@tdm9_order=" + orderNumber + "@&@tdm9_desc="
				+ object_desc + "@&@tdm9_characteristic_value="
				+ tdm9_characteristic_value + "@&@tdm9_charactervalues="
				+ string_value + "@&@tdm9_CharUnit=" + charUnit
				+ "@&@tdm9_testcondition=" + testCondition
				+ "@&@tdm9_comments=" + comments;
		
		return transValue;
	}
}
