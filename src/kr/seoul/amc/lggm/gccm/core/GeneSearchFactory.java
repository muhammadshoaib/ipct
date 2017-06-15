package kr.seoul.amc.lggm.gccm.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.text.WordUtils;
import org.researchaware.core.DataExtracter;
import org.researchaware.core.MySQLConnector;
import org.xml.sax.InputSource;



public class GeneSearchFactory {
	ServletContext context = null; 
	private Connection conn = null; 
	public GeneSearchFactory(ServletContext context, Connection conn) { 
		this.context = context; 
		this.conn = conn; 
	}
	public String GetGeneGraphML(String inputType, String geneNames, 
			String graphType,  String drugSensitivity, Map<String, String[]> perms) throws SQLException { 


		HashMap<String, GraphObject> cellLines = new HashMap<>(); 

		System.out.println(graphType); 
		java.sql.Statement stm = conn.createStatement(); 
		String sql = null ; 

		GraphMLWriter gmlw = new GraphMLWriter(); 
		gmlw.AddKey(new Key("l0", "all", "label", "string")); 
		gmlw.AddKey(new Key("dbName", "node", "Database Name", "string"));
		gmlw.AddKey(new Key("fullName", "node", "Full Name", "string")); 
		gmlw.AddKey(new Key("url", "node", "URL", "string"));
		gmlw.AddKey(new Key("details", "node", "Details", "string"));

		String geneIDs = ""; 

		sql = "SELECT * FROM `meta_genes` WHERE MATCH (`gene_symbol`) AGAINST ('" + geneNames + "')"; 
		System.out.println(sql);
		ResultSet rs = stm.executeQuery(sql); 
		while(rs.next()) {
			Node geneNode = new Node("GN" + rs.getString("gene_id")); 
			geneNode.AddData("fullName", rs.getString("gene_symbol"));
			geneNode.AddData("l0",  rs.getString("gene_symbol"));
			geneNode.AddData("dbName", "Gene");
			gmlw.AddNode(geneNode); 
			geneIDs = geneIDs +"," +  rs.getString("gene_id");  
		}
		geneIDs = geneIDs.replaceFirst(",", ""); 

		String cellLinesIDsString = ""; 
		sql = "SELECT * FROM "
				+ "(SELECT gene_id, master_ccl_id FROM `gene_mutations_mut` "
				+ "WHERE gene_id IN (" + geneIDs + ")) AS A"
				+ "INNER JOIN `meta_per_cell_line` USING(master_ccl_id)"; 
		rs = stm.executeQuery(sql); 
		while(rs.next()) {


			if(graphType.equals("summerized")) {
				String cellline_id = "CEL" + rs.getString("master_ccl_id"); 
				if (cellLines.containsKey(cellline_id) == false) { 
					GraphObject cell_line_obj = new GraphObject(cellline_id, 
							rs.getString("ccl_name").toUpperCase(), 
							"GN" + rs.getString("gene_id"), "None"); 


					cell_line_obj.props.put("ccle_primary_site", rs.getString("ccle_primary_site")); 
					cellLines.put(cellline_id, cell_line_obj); 
				}
				else {
					cellLines.get(cellline_id).AddConnection("GN" + rs.getString("gene_id"));
				}
				cellline_id = null;
				continue; 
			}



			Node cellLineNode = new Node("CEL" + rs.getString("master_ccl_id")); 
			cellLineNode.AddData("dbName", "Cell Line");
			cellLineNode.AddData("fullName", rs.getString("ccl_name").toUpperCase());
			cellLineNode.AddData("l0", rs.getString("ccl_name").toUpperCase());
			//cellLineNode.AddData("sensitivity", rs.getString("residuals"));
			gmlw.AddNode(cellLineNode); 

			String organNodeId  = "ORG" + rs.getString("ccle_primary_site"); 
			if (gmlw.ContainNode(organNodeId) == false) {
				Node primOrganNode = new Node(organNodeId); 
				primOrganNode.AddData("dbName", "Organ");
				primOrganNode.AddData("fullName", WordUtils.capitalize(
						rs.getString("ccle_primary_site").replaceAll("_", " ")));
				primOrganNode.AddData("l0", rs.getString("ccle_primary_site"));
				gmlw.AddNode(primOrganNode); 
			}

			Edge geneCellLineEdge = new Edge( "GN" + rs.getString("gene_id"), 
					"CEL" + rs.getString("master_ccl_id")); 

			gmlw.AddEdge(geneCellLineEdge); 

			Edge cellLineOrgansimEdge = new Edge( "CEL" + rs.getString("master_ccl_id"), 
					organNodeId); 
			gmlw.AddEdge(cellLineOrgansimEdge); 

			cellLinesIDsString = cellLinesIDsString +"," +  rs.getString("master_ccl_id");  
		}


		if(graphType.equals("summerized") == true) { 

			Iterator it = cellLines.entrySet().iterator(); 
			while(it.hasNext()) { 
				Map.Entry pair =  (Map.Entry) it.next(); 
				GraphObject rec = (GraphObject) pair.getValue(); 
				if(rec.HasMultiRelations()) { 
					Node cellLineNode = new Node(rec.ObjectID); 
					cellLineNode.AddData("dbName", "Cell Line");
					cellLineNode.AddData("fullName", rec.ObjectLabel.toUpperCase());
					cellLineNode.AddData("l0", rec.ObjectLabel.toUpperCase());
					gmlw.AddNode(cellLineNode); 

					String organNodeId  = "ORG" + rec.props.get("ccle_primary_site"); 
					if (gmlw.ContainNode(organNodeId) == false) {
						Node primOrganNode = new Node(organNodeId); 
						primOrganNode.AddData("dbName", "Organ");
						primOrganNode.AddData("fullName", rec.props.get("ccle_primary_site").toUpperCase());
						primOrganNode.AddData("l0", rec.props.get("ccle_primary_site"));
						gmlw.AddNode(primOrganNode); 
					}
					Edge cellLineOrganEdge = new Edge(rec.ObjectID, organNodeId); 
					gmlw.AddEdge(cellLineOrganEdge);

					for(int j = 0; j<rec.ObjectConntions.size(); j++) { 
						Edge reactomeEdge = new Edge(rec.ObjectID, rec.ObjectConntions.get(j)); 
						gmlw.AddEdge(reactomeEdge); 
					}

					cellLinesIDsString = cellLinesIDsString +"," + rec.ObjectID.replace("CEL", "");  
				}
			}
		}


		cellLinesIDsString = cellLinesIDsString.replaceFirst(",", "");
		
		if (cellLinesIDsString.length() > 0) { 
			sql = "SELECT master_cpd_id, meta_per_compound.`cpd_name`, GROUP_CONCAT(master_ccl_id) AS cell_lines "
					+ "FROM (SELECT A.*, B.master_ccl_id FROM "
					+ "(SELECT experiment_id, master_cpd_id, residuals FROM `data_curves_post_qc_res` "
					+ "WHERE `residuals` < " + drugSensitivity + " * 2)"
					+ "AS A INNER JOIN (SELECT experiment_id, master_ccl_id FROM `meta_per_experiment` "
					+ "WHERE master_ccl_id IN (" + cellLinesIDsString + ")  ) AS B USING (experiment_id)) AS C "
					+ "INNER JOIN `meta_per_compound` USING (`master_cpd_id`) "
					+ "GROUP BY (master_cpd_id) HAVING COUNT(master_ccl_id) > 1";
			System.out.println(sql);

			rs = stm.executeQuery(sql); 
			while(rs.next()) { 


				Node drug_node = new Node("DRG" + rs.getString("master_cpd_id")); 
				drug_node.AddData("dbName", "Drug");
				drug_node.AddData("fullName", rs.getString("cpd_name").toUpperCase());
				drug_node.AddData("l0", rs.getString("cpd_name").toLowerCase());
				gmlw.AddNode(drug_node); 

				String[] celllines = rs.getString("cell_lines").split(","); 
				for (String cellline : celllines) {
					Edge drugCellLineEdge = new Edge("DRG" + rs.getString("master_cpd_id"), 
							"CEL" + cellline); 
					gmlw.AddEdge(drugCellLineEdge); 
				}


			}

		}
		
		rs.close();
		stm.close();

		return gmlw.GetGraphML(); 
	}
}

