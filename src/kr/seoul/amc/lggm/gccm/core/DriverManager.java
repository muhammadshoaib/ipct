package kr.seoul.amc.lggm.gccm.core;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

public class DriverManager {
	
	public static String dataSourceJNDIName = "java:/myctrpdb"; 
	
	public static Connection getConnection(ServletContext context) {
		
		DataSource ds;
		try {
			InitialContext initContext = new InitialContext();
			ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			return ds.getConnection();
		} catch (NamingException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
		
		
	}
}
