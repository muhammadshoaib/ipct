package org.researchaware.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnector {
	@SuppressWarnings("finally")
	public static Connection ConnectToServer(String server, 
			String username, 
			String password, 
			String database) {
		String url = "jdbc:mysql://" + server + "/" + database; 
		Connection conn = null; 
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			try {
				conn = DriverManager.getConnection (url, username, password);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally { 
			return conn; 
		}
	}
	
	public static Connection ConnectToAMCLGGMServer() { 
		return MySQLConnector.ConnectToServer("172.25.53.172", "cbio", "cbio", "GENEDB"); 
	}
	public String cleanSQLString(String sql) {
		return sql.replace("'", "\'"); 
	}
}
