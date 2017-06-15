package kr.seoul.amc.lggm.gccm.core;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AtlasExpression
{
  private Connection conn;
  public Set<String> associated_genes = null;
 public String total_rows;
 public String filtered_rows;
  
  public AtlasExpression(Connection conn)
  {
    this.conn = conn;
  }
  
  public AtlasExpression(Connection conn, Set<String> associated_genes)
  {
    this.conn = conn;
    this.associated_genes = associated_genes;
  }
  
  public List<AtlasGeneInterface> GetAtlasTable(Collection<String> geneIds)
    throws SQLException
  {
    List<AtlasGeneInterface> atlasExpressionInterface = new ArrayList();
    String genes_list_string = String.join(",", geneIds);
    
    String sql = "SELECT DISTINCT gene_ensamble_id, gene_symbol, log_fold_change, p_value, expariment_detail FROM  (SELECT * FROM `expariments_results` WHERE `gene_id` IN (" + 
      genes_list_string + " ) " + 
      "AND (`log_fold_change` > 1 OR log_fold_change < -1 )) AS A " + 
      "INNER JOIN  `meta_genes` USING(gene_id) " + 
      "INNER JOIN (SELECT DISTINCT `expr_condition_id` FROM `exparimental_conditions` " + 
      "WHERE `property_name` LIKE 'disease' AND `property_value` LIKE '%cancer%') AS B " + 
      "USING(`expr_condition_id`) " + 
      "INNER JOIN `exparimental_configs` USING(expr_condition_id)" + 
      "ORDER BY log_fold_change DESC LIMIT 100000";
    
    Statement stm = this.conn.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    while (rs.next()) {
      atlasExpressionInterface.add(new AtlasGeneInterface(
        rs.getString("gene_symbol"), rs.getString("expariment_detail"), 
        rs.getString("expariment_detail"), 
        rs.getFloat("log_fold_change"), rs.getFloat("p_value")));
    }
    rs.close();
    stm.close();
    return atlasExpressionInterface;
  }
  
  public List<AtlasGeneInterface> GetAtlasTableByDrugNames(String drugString, String graphType, String drugSensitivity, String string, String string2)
    throws SQLException
  {
    System.out.println("Drug is " + drugString);
    if (this.associated_genes == null)
    {
      DrugSearchFactory dsf = new DrugSearchFactory(null, this.conn, drugString);
      List<String> drugiDs = dsf.GetDrugIDs(drugString);
      this.associated_genes = dsf.GetAssocatedGenesFreq(graphType, drugSensitivity, 0.2D, false).keySet();
    }
    return GetAtlasTable(this.associated_genes);
  }
}
