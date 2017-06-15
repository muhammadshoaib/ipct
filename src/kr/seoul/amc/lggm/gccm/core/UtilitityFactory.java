package kr.seoul.amc.lggm.gccm.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

public class UtilitityFactory
{
  ServletContext context = null;
  public String ServerIPAddress = "";
  public String dataSourceJNDIName = "java:/myctrpdb";
  
  public UtilitityFactory(ServletContext context) { this.context = context; }
  
  public ArrayList<String> ReturnAllObjectsList(String GeneMolecule, String term)
  {
    Connection conn = null;
    ArrayList<String> requiredList = new ArrayList();
    try
    {
      Context initContext = new javax.naming.InitialContext();
      DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
      conn = ds.getConnection();
      
      Statement stm = conn.createStatement();
      String sql = null;
      if (GeneMolecule.equals("gene")) {
        sql = 
          "SELECT DISTINCT gene_symbol AS NAME FROM meta_genes WHERE gene_symbol LIKE '" + term + "%' LIMIT 0, 20";
      } else if (GeneMolecule.equals("cellLine")) {
        sql = 
          "SELECT CONCAT(ccl_name, ': ', ccle_primary_site) AS NAME FROM `meta_per_cell_line`  "
          + "WHERE `ccl_name` LIKE '" + term + "%' OR `ccle_primary_site` LIKE '" + term + "%' "
          		+ "LIMIT 0, 20";
      }
      else if (GeneMolecule.equals("molecule")) {
        sql = 
          "SELECT cpd_name AS NAME FROM `meta_per_compound`  WHERE `cpd_name` LIKE '" + term + "%' LIMIT 0, 20";
      }
      
      System.out.println(sql);
      ResultSet rs = stm.executeQuery(sql);
      while (rs.next()) {
        requiredList.add(rs.getString("NAME").trim());
      }
      rs.close();
      stm.close();
      conn.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return requiredList;
    }
    catch (NamingException e) {
      e.printStackTrace();
    }
    return requiredList;
  }
  
  public ArrayList<String> ReturnOrgansList() throws SQLException {
    ArrayList<String> requiredList = new ArrayList();
    Connection conn = null;
    try
    {
      Context initContext = new javax.naming.InitialContext();
      DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
      conn = ds.getConnection();
      Statement stm = conn.createStatement();
      String sql = "SELECT DISTINCT ccle_primary_site FROM meta_per_cell_line ORDER BY ccle_primary_site";
      ResultSet rs = stm.executeQuery(sql);
      while (rs.next()) {
        requiredList.add(rs.getString("ccle_primary_site"));
      }
      
      rs.close();
      stm.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    catch (NamingException e) {
      e.printStackTrace();
    }
    finally {
      conn.close();
    }
    return requiredList;
  }
}