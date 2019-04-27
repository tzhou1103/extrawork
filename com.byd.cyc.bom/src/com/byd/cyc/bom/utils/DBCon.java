package com.byd.cyc.bom.utils;

import java.io.*;
import java.sql.*;
import java.util.Properties;

import com.encrypt.util.EncryptUtil;
import com.teamcenter.rac.util.MessageBox;

/**
 * @author zhoutong
 *
 */
public class DBCon 
{
    public static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private Connection con = null;
    
    public DBCon()
    {
    	InputStream inputStream = null;
        try 
        {
        	Properties properties = new Properties();
        	inputStream = DBCon.class.getClassLoader().getResourceAsStream("jdbc.properties");
        	properties.load(inputStream);
        	String URL = properties.getProperty("URL");
        	String USER = properties.getProperty("UserName");
        	String PWD = EncryptUtil.getDecryptString(properties.getProperty("Password"));
        	if (!paramValidate(URL, USER, PWD)) {
        		MessageBox.post("数据库连接配置有误，请检查jdbc.properties！", "提示", 2);
        		return;
			}
        	
        	Class.forName(DRIVER);
            this.con = DriverManager.getConnection(URL, USER, PWD);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
	/**
	 * 检查jdbc.properties文件中有关数据库连接的参数配置是否正确
	 * 
	 * @param URL
	 * @param USER
	 * @param PWD
	 * @return
	 */
	private boolean paramValidate(String URL, String USER, String PWD) 
	{
		if (URL == null || URL.isEmpty()) {
			return false;
		}
		if (USER == null || USER.isEmpty()) {
			return false;
		}
		if (PWD == null || PWD.isEmpty()) {
			return false;
		}
		return true;
	}
    
	public Connection getCon() {
		return this.con;
	}
    
    public void close(ResultSet rs, PreparedStatement ps, Connection conn)
    {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}

			if (ps != null) {
				ps.close();
				ps = null;
			}

			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public void close(ResultSet rs, Statement statement, Connection conn)
    {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}

			if (statement != null) {
				statement.close();
				statement = null;
			}

			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) {
		new DBCon();
	}
}
