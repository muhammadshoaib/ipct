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

public class CBioDataSummery
{
  private Connection conn;
  public Set<String> associated_genes = null;
 public String total_rows;
 public String filtered_rows;
  
  public CBioDataSummery(Connection conn)
  {
    this.conn = conn;
  }
  
  public CBioDataSummery(Connection conn, Set<String> associated_genes)
  {
    this.conn = conn;
    this.associated_genes = associated_genes;
  }
  
  public List<CBioGeneInterface> GetGeneCBIOTable(Collection<String> cellLineIds)
    throws SQLException
  {
    List<CBioGeneInterface> cbiogenessummery = new ArrayList<>();
    String cell_list_string = String.join(",", cellLineIds);
    
    String sql = "SELECT DISTINCT gene_id, meta_genes.gene_symbol, cell_lines, "
    		+ "mutations, alterations, up_regulated, down_regulated, total_cell_lines  "
    		+ "FROM ("
    		+ "SELECT gene_id, gene_symbol, GROUP_CONCAT(DISTINCT master_ccl_id) AS cell_lines, "
    		+ "COUNT(DISTINCT master_ccl_id) AS total_cell_lines "
    		+ "FROM `gene_mutations_res`  WHERE master_ccl_id IN (" + cell_list_string +  ") "
    		+ "GROUP BY gene_id "
    		+ ") A INNER JOIN `cbio_genes_summery` USING(gene_id)"
    		+ "INNER JOIN `meta_genes` USING (gene_id)"
    		+ "ORDER BY total_cell_lines DESC, ((`mutations` + alterations)) DESC "; 
    		//+ "ORDER BY (ABS(up_regulated - down_regulated) / (up_regulated + down_regulated)) DESC";
    
    System.out.println(sql);
    
    Statement stm = this.conn.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    while (rs.next()) {
    	cbiogenessummery.add(new CBioGeneInterface(rs.getString("gene_symbol"), 
    			rs.getFloat("mutations"),
    			rs.getFloat("alterations"),
    			rs.getFloat("up_regulated"),
    			rs.getFloat("down_regulated"),
    			rs.getInt("total_cell_lines")));
    }
    rs.close();
    stm.close();
    
	return cbiogenessummery;
  }
  
  public List<CBioGeneInterface> GetAtlasTableByDrugNames(String cellLInesString, String graphType, String drugSensitivity, String string, String string2)
    throws SQLException
  {
    System.out.println("Drug is " + cellLInesString);
    if (this.associated_genes == null)
    {
      DrugSearchFactory dsf = new DrugSearchFactory(null, this.conn, cellLInesString);
      List<String> drugiDs = dsf.GetDrugIDs(cellLInesString);
      this.associated_genes = dsf.GetAssocatedGenesFreq(graphType, drugSensitivity, 0.2D, false).keySet();
    }
    return GetGeneCBIOTable(this.associated_genes);
  }
}

