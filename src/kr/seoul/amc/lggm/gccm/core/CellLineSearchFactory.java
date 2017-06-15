package kr.seoul.amc.lggm.gccm.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.text.WordUtils;

public class CellLineSearchFactory {
	ServletContext context = null; 
	public String ServerIPAddress = ""; 
	public String dataSourceJNDIName = "java:/myctrpdb";  
	private Connection conn;
	public CellLineSearchFactory(ServletContext context, Connection conn) { 
		this.context = context; 
		this.conn = conn; 
	}


	public ArrayList<String> GetCellLinesIDs(String cellLineNames) throws SQLException {
		ArrayList<String> cellLineIds = new ArrayList<>(); 
		
		String sql = "SELECT master_ccl_id, ccl_name "
				+ "FROM meta_per_cell_line WHERE MATCH(ccl_name) AGAINST( '" + cellLineNames +  "' ) ";
		
		System.out.println(sql);
		java.sql.Statement stm = this.conn.createStatement(); 
		ResultSet rs = stm.executeQuery(sql); 

		while(rs.next()) { 
			cellLineIds.add(rs.getString("master_ccl_id")); 
		}
		
		rs.close();
		stm.close(); 
		
		return cellLineIds; 
		
	}
	public String GetCellLineGraph(String cellLineName, String graphType, String databases,
			String drugSensitivity, String output, String pathwayType, String genesFilter ) throws SQLException { 

		System.out.println(cellLineName); 

		cellLineName = cellLineName.replace(";", " ");
		Context initContext;

		HashMap<String, GraphObject> drugs = new HashMap<>(); 
		HashMap<String, String> cellLineNames = new HashMap<>(); 

		Connection conn = null ; 
		try {

			GraphMLWriter gmlw = new GraphMLWriter(); 
			gmlw.AddKey(new Key("l0", "all", "label", "string")); 
			gmlw.AddKey(new Key("dbName", "node", "Database Name", "string"));
			gmlw.AddKey(new Key("fullName", "node", "Full Name", "string")); 
			gmlw.AddKey(new Key("url", "node", "URL", "string"));
			gmlw.AddKey(new Key("details", "node", "Details", "string"));
			gmlw.AddKey(new Key("sensitivity", "node", "Sensitivity", "string"));
			initContext = new InitialContext();

			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			conn = ds.getConnection();
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement(); 

			String sql = "SELECT master_ccl_id, ccl_name "
					+ "FROM meta_per_cell_line WHERE MATCH(ccl_name) AGAINST( '" + cellLineName +  "' ) ";

			System.out.println(sql);
			ResultSet rs = stm.executeQuery(sql); 
			String cellLinesIDsString = ""; 

			while(rs.next()) { 
				Node drug_node = new Node("CEL" + rs.getString("master_ccl_id")); 
				drug_node.AddData("dbName", "Cell Line");
				drug_node.AddData("fullName", rs.getString("ccl_name").toUpperCase());
				drug_node.AddData("l0", rs.getString("ccl_name").toLowerCase());
				gmlw.AddNode(drug_node); 
				cellLinesIDsString = cellLinesIDsString + "," + rs.getString("master_ccl_id"); 
				cellLineNames.put(rs.getString("master_ccl_id"), rs.getString("ccl_name").toUpperCase()); 
			}
			rs.close();
			cellLinesIDsString = cellLinesIDsString.replaceFirst(",", ""); 


			sql = "SELECT C.*, meta_per_compound.`cpd_name` FROM (SELECT A.*, B.master_ccl_id FROM "
					+ "(SELECT experiment_id, master_cpd_id, residuals FROM `data_curves_post_qc_res` "
					+ "WHERE `residuals` < " + drugSensitivity + ")"
					+ "AS A INNER JOIN (SELECT experiment_id, master_ccl_id FROM `meta_per_experiment` "
					+ "WHERE master_ccl_id IN (" + cellLinesIDsString + ")  ) AS B USING (experiment_id)) AS C "
					+ "INNER JOIN `meta_per_compound` USING (`master_cpd_id`)";
			System.out.println(sql);
			rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				if(graphType.equals("summerized")) {
					String drug_key = "DRG" + rs.getString("master_cpd_id"); 
					if (drugs.containsKey(drug_key) == false) { 
						drugs.put(drug_key, new GraphObject(drug_key, 
								rs.getString("cpd_name").toLowerCase(), 
								"CEL" + rs.getString("master_ccl_id"), "None")); 
						drugs.get(drug_key).AddData("fullName", WordUtils.capitalize(rs.getString("cpd_name")));
						drugs.get(drug_key).AddData("sensitivity", 
								cellLineNames.get(rs.getString("master_ccl_id")) + 
								": " + rs.getString("residuals").substring(0, 5));
					}
					else {
						drugs.get(drug_key).AddConnection("CEL" + rs.getString("master_ccl_id"));
						drugs.get(drug_key).UpdateData("sensitivity", 
								cellLineNames.get(rs.getString("master_ccl_id")) + 
								": " + rs.getString("residuals").substring(0, 5));
					}
					drug_key = null;
					continue; 
				}
				Node drugNode = new Node("DRG" + rs.getString("master_cpd_id")); 
				drugNode.AddData("dbName", "Drug");
				drugNode.AddData("fullName", rs.getString("cpd_name").toUpperCase());
				drugNode.AddData("l0", rs.getString("cpd_name").toLowerCase());

				drugNode.AddData("sensitivity", 
						cellLineNames.get(rs.getString("master_ccl_id")) + 
						": " + rs.getString("residuals").substring(0, 5));
				
				if (gmlw.AddNode(drugNode) == false) { 
					gmlw.nodes.get("DRG" + rs.getString("master_cpd_id"))
					.UpdateData("sensitivity", 
							cellLineNames.get(rs.getString("master_ccl_id")) + 
							": " + rs.getString("residuals").substring(0, 5));
				}

				Edge drugCellLineEdge = new Edge("CEL" + rs.getString("master_ccl_id"), 
						"DRG" + rs.getString("master_cpd_id")); 
				gmlw.AddEdge(drugCellLineEdge); 
			}
			rs.close();

			String max_celllines_limit = "0"; 
			if(graphType.equals("summerized")) {
				max_celllines_limit = "1"; 
			}
			
			String where_clause_filter = ""; 
			if (genesFilter.equals("cancerGenes")) { 
				where_clause_filter = " AND is_cancer_genes = TRUE "; 
			}else if (genesFilter.equals("excludeFrequentMut")) { 
				where_clause_filter = " AND is_common = 'N'  "; 
			}
			
			sql = "SELECT gene_id, gene_symbol, GROUP_CONCAT(DISTINCT master_ccl_id) AS cell_lines, "
					+ "COUNT(master_ccl_id) "
					+ "FROM `gene_mutations_res`  WHERE master_ccl_id IN (" + cellLinesIDsString + ") "
					+ "GROUP BY gene_symbol HAVING COUNT(DISTINCT master_ccl_id) > 1"; 
			
			sql = "SELECT DISTINCT gene_id, meta_genes.gene_symbol, cell_lines, "
					+ "mutations, alterations, up_regulated, down_regulated, total_cell_lines  "
					+ "FROM ("
					+ "SELECT gene_id, gene_symbol, GROUP_CONCAT(DISTINCT master_ccl_id) AS cell_lines, "
					+ "COUNT(DISTINCT master_ccl_id) AS total_cell_lines "
					+ "FROM `gene_mutations_res`  WHERE master_ccl_id IN (" + cellLinesIDsString + ") "
					+ where_clause_filter
					+ "GROUP BY gene_id HAVING COUNT(*) > " + max_celllines_limit
					+ ") A INNER JOIN `cbio_genes_summery` USING(gene_id)"
					+ "INNER JOIN `meta_genes` USING (gene_id)"
					+ "ORDER BY total_cell_lines DESC, ((`mutations` + alterations)) DESC LIMIT 50"	; 
			System.out.println(sql);
			ArrayList<String> genes_list = new ArrayList<>(); 
			String genes_list_string = ""; 
			
			rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				Node geneNode = new Node("GN" + rs.getString("gene_id")); 
				geneNode.AddData("dbName", "Gene");
				geneNode.AddData("fullName", rs.getString("gene_symbol").toUpperCase());
				geneNode.AddData("l0", rs.getString("gene_symbol").toUpperCase());
				gmlw.AddNode(geneNode); 

				if (genes_list.contains(rs.getString("gene_id")) == false) {
					genes_list.add(rs.getString("gene_id")); 
					genes_list_string = genes_list_string + "," + rs.getString("gene_id"); 
				}
				
				String[] connectedCellLines = rs.getString("cell_lines").split(","); 
				for (int i = 0; i<connectedCellLines.length; i++) {
					Edge drugCellLineEdge = new Edge("CEL" + connectedCellLines[i].trim(), 
							"GN" + rs.getString("gene_id")); 
					gmlw.AddEdge(drugCellLineEdge);
				}
			}

			
			if(graphType.equals("summerized")) {

				Iterator it = drugs.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				//System.out.println(reactoms.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject rec = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ObjectID + " " + rec.Genes.toString()); 
					if(rec.HasMultiRelations()) { 
						//System.out.println()
						Node reactomeNode = new Node(rec.ObjectID); 
						reactomeNode.AddData("l0", rec.ObjectLabel);
						reactomeNode.AddData("fullName", rec.GetData("fullName"));
						reactomeNode.AddData("sensitivity", rec.GetData("sensitivity"));
						reactomeNode.AddData("dbName", "Drug");
						gmlw.AddNode(reactomeNode); 

						for(int i = 0; i<rec.ObjectConntions.size(); i++) { 
							Edge reactomeEdge = new Edge(rec.ObjectID, rec.ObjectConntions.get(i)); 
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}
			}
			
			if(databases.contains("pathways") && databases.contains("mutGenes") && genes_list_string.length() > 1) {
				genes_list_string = genes_list_string.replaceFirst(",", ""); 
				genes_list_string = genes_list_string.replace(",null", ""); 
				genes_list = null; 
				System.out.println(genes_list_string);

				sql = "SELECT gene_symbol, reactome_id, pathway_name FROM "
						+ "(SELECT * FROM `reactome_pathways_simple` WHERE `gene_id` IN ("+ genes_list_string + " ) ) AS A "
						+ "INNER JOIN "
						+ "(SELECT DISTINCT gene_id, gene_symbol FROM `meta_genes` WHERE `gene_id` IN ("+ genes_list_string + " )) AS B "
						+ "USING (gene_id)"; 
				System.out.println(sql);
				rs = stm.executeQuery(sql); 
				while(rs.next()) { 
					Node reactome_node = new Node("RTM" + rs.getString("reactome_id")); 
					reactome_node.AddData("dbName", "Pathway");
					reactome_node.AddData("fullName", rs.getString("pathway_name").toUpperCase());
					reactome_node.AddData("l0", rs.getString("pathway_name").toLowerCase());
					gmlw.AddNode(reactome_node); 

					Edge reactomeGeneEdge = new Edge("GN" + rs.getString("gene_symbol"), 
							"RTM" + rs.getString("reactome_id")); 
					gmlw.AddEdge(reactomeGeneEdge); 
				}
				rs.close();
			}
			stm.close();
			conn.close();
			return gmlw.GetGraphML(); 


		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();

		}
		return ""; 
	}
}
