package kr.seoul.amc.lggm.gccm.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.text.WordUtils;


public class DrugSearchFactory {
	ServletContext context = null; 
	public String ServerIPAddress = ""; 
	public String dataSourceJNDIName = "java:/myctrpdb"; 
	public Map<String, String> drugIds = new HashMap<>(); 

	private Connection conn;
	//private 

	public DrugSearchFactory(ServletContext context) { 
		this.context = context; 
	}

	public DrugSearchFactory(ServletContext context, Connection conn) { 
		this.conn = conn; 
		this.context = context; 
	}
	
	public DrugSearchFactory(ServletContext context, Connection conn, String drugString) throws SQLException { 
		this.conn = conn; 
		this.context = context;
		
		java.sql.Statement stm = conn.createStatement(); 
		//System.out.println(drugString);

		String sql = "SELECT DISTINCT master_cpd_id, `cpd_name` FROM `meta_per_compound` "
				+ "WHERE MATCH (`cpd_name`) AGAINST( '" + drugString.trim() + "') "; 
		//System.out.println(sql);
		ResultSet rs = stm.executeQuery(sql); 

		while(rs.next()) {
			this.drugIds.put(rs.getString("master_cpd_id"), rs.getString("master_cpd_id")); 
		}

		rs.close();
		stm.close();
		
	}


	public List<String> GetDrugIDs(String drugString) throws SQLException {


		List<String> drugIDs = new ArrayList<String>(); 
		Connection conn = null ; 
		try {

			conn = DriverManager.getConnection(this.context);
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement(); 
			//System.out.println(drugString);

			String sql = "SELECT master_cpd_id, `cpd_name` FROM `meta_per_compound` "
					+ "WHERE MATCH (`cpd_name`) AGAINST( '" + drugString.trim() + "') "; 
			//System.out.println(sql);
			ResultSet rs = stm.executeQuery(sql); 

			while(rs.next()) {
				drugIDs.add(rs.getString("master_cpd_id")); 
			}

			rs.close();
			stm.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();

		}
		return drugIDs; 

	}

	public String GetDrugGraph(String drugString,  String graphType, String databases, 
			String drugSensitivity, double mutationFrequency, String output, String pathwayType, String genesFilter) 
					throws SQLException { 

		mutationFrequency = mutationFrequency * 100; 
		System.out.println(drugString); 

		String[] drugNames = drugString.split(";");
		Context initContext;
		Connection conn = null ; 



		HashMap<String, GraphObject> cellLines = new HashMap<>(); 
		HashMap<String, GraphObject> mutated_genes = new HashMap<>(); 
		try {
			initContext = new InitialContext();

			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			conn = ds.getConnection();
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement(); 
			System.out.println(drugString);

			GraphMLWriter gmlw = new GraphMLWriter(); 
			gmlw.AddKey(new Key("l0", "all", "label", "string")); 
			gmlw.AddKey(new Key("dbName", "node", "Database Name", "string"));
			gmlw.AddKey(new Key("fullName", "node", "Full Name", "string")); 
			gmlw.AddKey(new Key("url", "node", "URL", "string"));
			gmlw.AddKey(new Key("details", "node", "Details", "string"));
			gmlw.AddKey(new Key("MutationPerct", "node", "Mutation Ratio", "string"));
			gmlw.AddKey(new Key("sensitivity", "node", "Sensitivity", "string"));

			// Finding Durg ID from Drug Name:
			for (int i = 0; i<drugNames.length; i++) {
				String drugName = drugNames[i]; 

				// lapatinib
				String sql = "SELECT master_cpd_id, `cpd_name` FROM `meta_per_compound` "
						+ "WHERE `cpd_name` LIKE '" + drugName.trim() + "' "; 
				System.out.println(sql);
				ResultSet rs = stm.executeQuery(sql); 

				String drugIDsString = ""; 



				while(rs.next()) { 
					Node drug_node = new Node("DRG" + rs.getString("master_cpd_id")); 
					drug_node.AddData("dbName", "Drug");
					drug_node.AddData("fullName", rs.getString("cpd_name").toUpperCase());
					drug_node.AddData("l0", rs.getString("cpd_name").toLowerCase());
					gmlw.AddNode(drug_node); 
					drugIDsString = drugIDsString + "," + rs.getString("master_cpd_id"); 
				}
				rs.close();
				drugIDsString = drugIDsString.replaceFirst(",", ""); 

				int total_sensitive_cell_lines = 0; 
				sql = "SELECT COUNT(experiment_id) AS TOTAL FROM `data_curves_post_qc_res` "
						+ "WHERE `residuals` < " + drugSensitivity 
						+ " AND `master_cpd_id` IN( "+ drugIDsString + ")"; 
				System.out.println(sql);
				rs = stm.executeQuery(sql); 
				rs.next(); 
				total_sensitive_cell_lines = rs.getInt("TOTAL"); 

				sql = "SELECT * FROM (SELECT experiment_id, master_cpd_id, residuals FROM `data_curves_post_qc_res` "
						+ "WHERE `residuals` < " +  drugSensitivity + " "
						+ "AND `master_cpd_id` IN( "+ drugIDsString + ")) A "
						+ "INNER JOIN ( SELECT experiment_id, ccl_name, master_ccl_id, `ccle_primary_site` FROM `meta_per_experiment` "
						+ "INNER JOIN `meta_per_cell_line` USING (`master_ccl_id`)) B USING (experiment_id) "
						+ "ORDER BY residuals ASC LIMIT 100"; 
				System.out.println(sql);

				rs = stm.executeQuery(sql); 
				while(rs.next()) { 


					if(graphType.equals("summerized")) {
						String cellline_id = "CEL" + rs.getString("master_ccl_id"); 
						if (cellLines.containsKey(cellline_id) == false) { 
							GraphObject cell_line_obj = new GraphObject(cellline_id, 
									rs.getString("ccl_name").toUpperCase(), 
									"DRG" + rs.getString("master_cpd_id"), "None"); 


							cell_line_obj.props.put("ccle_primary_site", rs.getString("ccle_primary_site")); 
							cellLines.put(cellline_id, cell_line_obj); 
						}
						else {
							cellLines.get(cellline_id).AddConnection("DRG" + rs.getString("master_cpd_id"));
						}
						cellline_id = null;
						continue; 
					}

					Node cellLineNode = new Node("CEL" + rs.getString("master_ccl_id")); 
					cellLineNode.AddData("dbName", "Cell Line");
					cellLineNode.AddData("fullName", rs.getString("ccl_name").toUpperCase());
					cellLineNode.AddData("l0", rs.getString("ccl_name").toUpperCase());
					cellLineNode.AddData("sensitivity", rs.getString("residuals"));
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

					Edge drugCellLineEdge = new Edge("DRG" + rs.getString("master_cpd_id"), 
							"CEL" + rs.getString("master_ccl_id")); 
					gmlw.AddEdge(drugCellLineEdge); 

					Edge cellLineOrganEdge = new Edge("CEL" + rs.getString("master_ccl_id"), 
							organNodeId); 
					gmlw.AddEdge(cellLineOrganEdge); 

				}
				rs.close();

				// Loading Mutated Gens:   SELECT * FROM `gene_mutations_res` WHERE  mut_typ = 'MUT'

				String max_gene_limit = "100";
				if(graphType.equals("summerized") == true) {
					max_gene_limit = "200";
				}
				
				String where_clause_filter = ""; 
				if (genesFilter.equals("cancerGenes")) { 
					where_clause_filter = " is_cancer_genes = TRUE "; 
				}else if (genesFilter.equals("excludeFrequentMut")) { 
					where_clause_filter = " is_common = 'N'  "; 
				}
				


				sql = "SELECT master_cpd_id, gene_id, gene_symbol, COUNT(master_ccl_id) AS Total_CellLines "
						+ "FROM (SELECT  DISTINCT master_cpd_id, master_ccl_id, gene_symbol, gene_id FROM "
						+ "(SELECT experiment_id, master_cpd_id, residuals "
						+ "FROM `data_curves_post_qc_res` WHERE `residuals` < " + drugSensitivity + " "
						+ "AND `master_cpd_id` IN(  "+ drugIDsString + " )) A "
						+ "INNER JOIN `meta_per_experiment` USING (experiment_id) "
						+ "INNER JOIN `gene_mutations_res`"
						+ "USING (master_ccl_id) WHERE " + where_clause_filter + " ) AS C GROUP BY (gene_symbol) "
						+ "ORDER BY COUNT(master_ccl_id)  DESC LIMIT " + max_gene_limit; 
				System.out.println(sql);
				rs = stm.executeQuery(sql); 

				ArrayList<String> genes_list = new ArrayList<>(); 
				String genes_list_string = ""; 
				while(rs.next()) { 

					double percent_metated = rs.getDouble("Total_CellLines") / (double)total_sensitive_cell_lines;
					percent_metated = Math.round(percent_metated * 100); 

					if (percent_metated < mutationFrequency) {
						continue; 
					}
					
					if(graphType.equals("summerized")) {
						String gene_symbol = "GN" + rs.getString("gene_id"); 
						if (mutated_genes.containsKey(gene_symbol) == false) { 
							mutated_genes.put(gene_symbol, new GraphObject(gene_symbol, 
									rs.getString("gene_symbol").toUpperCase(), 
									"DRG" + rs.getString("master_cpd_id"), "None")); 

							mutated_genes.get(gene_symbol).props.put("gene_id", rs.getString("gene_id")); 
							mutated_genes.get(gene_symbol).AddData("MutationPerct", drugName + ": " + Double.toString(percent_metated)); 

						}
						else {
							
							mutated_genes.get(gene_symbol).AddConnection("DRG" + rs.getString("master_cpd_id"));
							mutated_genes.get(gene_symbol).UpdateData("MutationPerct", drugName + ": " + Double.toString(percent_metated)); 
							if (genes_list.contains(rs.getString("gene_id")) == false) {
								genes_list.add(rs.getString("gene_id")); 
								genes_list_string = genes_list_string + "," + rs.getString("gene_id"); 
							}
							
						}
						
						gene_symbol = null;
						continue; 
					}

					
					//

					if (gmlw.ContainNode("GN" + rs.getString("gene_id"))) {
						gmlw.nodes.get("GN" + rs.getString("gene_id"))
						.UpdateData("MutationPerct", drugName + ": " + Double.toString(percent_metated)); 
					}
					Node geneNode = new Node("GN" + rs.getString("gene_id")); 

					geneNode.AddData("dbName", "Gene");
					geneNode.AddData("fullName", rs.getString("gene_symbol").toUpperCase());
					geneNode.AddData("l0", rs.getString("gene_symbol").toUpperCase());


					//System.out.println(percent_metated);
					geneNode.AddData("MutationPerct", drugName + ": " + Double.toString(percent_metated)); 
					//geneNode.AddData("MutationPerct","AN 100%");
					gmlw.AddNode(geneNode); 

					Edge drugCellLineEdge = new Edge("GN" + rs.getString("gene_id"), 
							"DRG" + rs.getString("master_cpd_id")); 
					gmlw.AddEdge(drugCellLineEdge); 

					if (genes_list.contains(rs.getString("gene_id")) == false) {
						genes_list.add(rs.getString("gene_id")); 
						genes_list_string = genes_list_string + "," + rs.getString("gene_id"); 
					}

				}
				rs.close();

				genes_list_string = genes_list_string.replaceFirst(",", ""); 
				genes_list_string = genes_list_string.replace(",null", ""); 
				genes_list = null; 

				if(graphType.equals("summerized") == false && genes_list_string.length() > 0
						&& databases.contains("pathways") ) { 

					System.out.println(genes_list_string);
					
					String whereClause = ""; 
					if (pathwayType.equals("metabolicPathways")) { 
						whereClause = "AND pathway_type = 'M'"; 
					} else if (pathwayType.equals("signalingPathways")) { 
						whereClause =  "AND pathway_type = 'S'"; 
					}

					sql = "SELECT gene_symbol, gene_id, reactome_id, pathway_name FROM "
							+ "(SELECT * FROM `reactome_pathways_simple` "
							+ "WHERE `gene_id` IN ("+ genes_list_string + " )  " + whereClause + ") AS A "
							+ "INNER JOIN "
							+ "(SELECT DISTINCT gene_id, gene_symbol FROM `meta_genes` WHERE `gene_id` IN ("+ genes_list_string + " )) AS B "
							+ "USING (gene_id) "; 
					
					
					System.out.println(sql);
					rs = stm.executeQuery(sql); 

					while(rs.next()) { 
						Node reactome_node = new Node("RTM" + rs.getString("reactome_id")); 
						reactome_node.AddData("dbName", "Pathway");
						reactome_node.AddData("fullName", rs.getString("pathway_name"));
						reactome_node.AddData("l0", rs.getString("pathway_name"));
						gmlw.AddNode(reactome_node); 

						Edge reactomeGeneEdge = new Edge("GN" + rs.getString("gene_id"), 
								"RTM" + rs.getString("reactome_id")); 
						gmlw.AddEdge(reactomeGeneEdge); 
					}
					rs.close();
				}
				if(graphType.equals("summerized") == false && genes_list_string.length() > 0
						&& databases.contains("atlasexpression")) {
				//if(graphType.equals("summerized") == false && genes_list_string.length() > 0) {
					
					sql = "SELECT * FROM  (SELECT * FROM `expariments_results` "
							+ "WHERE `gene_id` IN (" + genes_list_string + " ) "
							+ "AND (`log_fold_change` > 1 OR log_fold_change < -1 )) AS A INNER JOIN ("
							+ "SELECT DISTINCT `expr_condition_id` FROM `exparimental_conditions` "
							+ "WHERE `property_name` LIKE 'disease' AND `property_value` LIKE '%cancer%') AS B "
							+ "USING(`expr_condition_id`) "
							+ "INNER JOIN `exparimental_configs` USING(expr_condition_id)"; 
					System.out.println(sql);
					rs = stm.executeQuery(sql); 

					while(rs.next()) { 
						String exparimentNodeId = "GXA" + rs.getString("expar_text_id"); 
						if (gmlw.ContainNode(exparimentNodeId)) {
							String exparimentNodeIdDB = gmlw.nodes.get(exparimentNodeId).Data.get("dbName");
							if (exparimentNodeIdDB.contains("Up") == false && 
									rs.getDouble("log_fold_change") > 1.0) {
								exparimentNodeIdDB = exparimentNodeIdDB.replaceAll("Down", "Up Down");  
							}
							
							if (exparimentNodeIdDB.contains("Down") == false && 
									rs.getDouble("log_fold_change") < -1.0) {
								exparimentNodeIdDB = exparimentNodeIdDB.replaceAll("Up", "Up Down");  
							}
							
							gmlw.nodes.get(exparimentNodeId).Data.put("dbName", exparimentNodeIdDB);
						}
						
						Node reactome_node = new Node(exparimentNodeId); 

						if (rs.getDouble("log_fold_change") > 1.0)
							reactome_node.AddData("dbName", "Gene Expression Up Regulated");
						else if (rs.getDouble("log_fold_change") < -1.0)
							reactome_node.AddData("dbName", "Gene Expression Down Regulated");
						else 
							reactome_node.AddData("dbName", "Gene Expression");

						reactome_node.AddData("fullName", rs.getString("expariment_detail"));
						reactome_node.AddData("l0", rs.getString("expariment_detail"));
						gmlw.AddNode(reactome_node); 

						Edge reactomeGeneEdge = new Edge("GN" + rs.getString("gene_id"), 
								exparimentNodeId); 
						gmlw.AddEdge(reactomeGeneEdge); 
					}
					rs.close();
				}
				//*/


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
					}
				}

				ArrayList<String> genes_list = new ArrayList<>(); 
				String genes_list_string = ""; 
				it = mutated_genes.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				//System.out.println(reactoms.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject rec = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ObjectID + " " + rec.Genes.toString()); 
					if(rec.HasMultiRelations()) { 
						//System.out.println()

						Node geneNode = new Node(rec.ObjectID); 
						geneNode.AddData("l0", rec.ObjectLabel.toUpperCase());
						geneNode.AddData("fullName", rec.ObjectLabel.toUpperCase());
						geneNode.AddData("dbName", "Gene");
						geneNode.AddData("MutationPerct", rec.GetData("MutationPerct"));
						gmlw.AddNode(geneNode); 


						if (genes_list.contains(rec.props.get("gene_id")) == false) {
							genes_list.add(rec.props.get("gene_id")); 
							genes_list_string = genes_list_string + "," + rec.props.get("gene_id"); 
						}

						for(int j = 0; j<rec.ObjectConntions.size(); j++) { 
							Edge reactomeEdge = new Edge(rec.ObjectID, rec.ObjectConntions.get(j)); 
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}
				genes_list_string = genes_list_string.replaceFirst(",", ""); 
				genes_list_string = genes_list_string.replace(",null", ""); 
				
				if(databases.contains("pathways")
						&& genes_list_string.length() > 0) {
					
					genes_list = null; 
					System.out.println(genes_list_string);
					String whereClause = ""; 
					if (pathwayType.equals("metabolicPathways")) { 
						whereClause = "AND pathway_type = 'M'"; 
					} else if (pathwayType.equals("signalingPathways")) { 
						whereClause =  "AND pathway_type = 'S'"; 
					}

					String sql = "SELECT gene_id, gene_symbol, reactome_id, pathway_name FROM "
							+ "(SELECT * FROM `reactome_pathways_simple` "
							+ "WHERE `gene_id` IN ("+ genes_list_string + " )  " + whereClause + ") AS A "
							+ "INNER JOIN "
							+ "(SELECT DISTINCT gene_id, gene_symbol FROM `meta_genes` WHERE `gene_id` IN ("+ genes_list_string + " )) AS B "
							+ "USING (gene_id) "; 
					
					
					
					System.out.println(sql);
					ResultSet rs = stm.executeQuery(sql); 
					while(rs.next()) { 
						Node reactome_node = new Node("RTM" + rs.getString("reactome_id")); 
						reactome_node.AddData("dbName", "Pathway");
						reactome_node.AddData("fullName", rs.getString("pathway_name").toUpperCase());
						reactome_node.AddData("l0", rs.getString("pathway_name").toLowerCase());
						gmlw.AddNode(reactome_node); 

						Edge reactomeGeneEdge = new Edge("GN" + rs.getString("gene_id"), 
								"RTM" + rs.getString("reactome_id")); 
						gmlw.AddEdge(reactomeGeneEdge); 
					}
					rs.close();
				}
				
				
				if(databases.contains("atlasexpression") && 
						genes_list_string.length() > 0) {
					//if(graphType.equals("summerized") == false && genes_list_string.length() > 0) {
						
						String sql = "SELECT *,  MIN(p_value)  FROM  (SELECT * FROM `expariments_results` "
								+ "WHERE `gene_id` IN (" + genes_list_string + " ) "
								+ "AND (`log_fold_change` > 1 OR log_fold_change < -1 )) AS A INNER JOIN ("
								+ "SELECT DISTINCT `expr_condition_id` FROM `exparimental_conditions` "
								+ "WHERE `property_name` LIKE 'disease' AND `property_value` LIKE '%cancer%') AS B "
								+ "USING(`expr_condition_id`) "
								+ "INNER JOIN `exparimental_configs` USING(expr_condition_id) "
								+ "GROUP BY expr_condition_id, gene_id"; 
						System.out.println(sql);
						ResultSet rs = stm.executeQuery(sql); 

						while(rs.next()) { 
							String exparimentNodeId = "GXA" + rs.getString("expar_text_id"); 
							if (gmlw.ContainNode(exparimentNodeId)) {
								String exparimentNodeIdDB = gmlw.nodes.get(exparimentNodeId).Data.get("dbName");
								if (exparimentNodeIdDB.contains("Up") == false && 
										rs.getDouble("log_fold_change") > 1.0) {
									exparimentNodeIdDB = exparimentNodeIdDB.replaceAll("Down", "Up Down");  
								}
								
								if (exparimentNodeIdDB.contains("Down") == false && 
										rs.getDouble("log_fold_change") < -1.0) {
									exparimentNodeIdDB = exparimentNodeIdDB.replaceAll("Up", "Up Down");  
								}
								
								gmlw.nodes.get(exparimentNodeId).Data.put("dbName", exparimentNodeIdDB);
							}
							
							Node reactome_node = new Node(exparimentNodeId); 

							if (rs.getDouble("log_fold_change") > 1.0)
								reactome_node.AddData("dbName", "Gene Expression Up Regulated");
							else if (rs.getDouble("log_fold_change") < -1.0)
								reactome_node.AddData("dbName", "Gene Expression Down Regulated");
							else 
								reactome_node.AddData("dbName", "Gene Expression");

							reactome_node.AddData("fullName", rs.getString("expariment_detail"));
							reactome_node.AddData("l0", rs.getString("expariment_detail"));
							gmlw.AddNode(reactome_node); 

							Edge reactomeGeneEdge = new Edge("GN" + rs.getString("gene_id"), 
									exparimentNodeId); 
							gmlw.AddEdge(reactomeGeneEdge); 
						}
						rs.close();
					}



			}





			stm.close();
			conn.close();
			System.out.println(output); 
			if (output.equals("json")) {

				return gmlw.GetJSONString().toString(); 
			}
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


	@SuppressWarnings("unchecked")
	public List<String> GetAffectedAssocatedGenes(List<String> drugIDs, String graphType,
			String drugSensitivity) throws SQLException {
		String drugIDsString = String.join(",", drugIDs); 

		String max_gene_limit = "100000";

		Map<String, ArrayList<String>> genesToDrungMapping = new HashMap<>(); 

		java.sql.Statement stm = conn.createStatement();
		
		
		
		
		String sql = "SELECT master_cpd_id, gene_id, gene_symbol, COUNT(master_ccl_id) AS Total_CellLines "
				+ "FROM (SELECT  DISTINCT master_cpd_id, master_ccl_id, gene_symbol, gene_id FROM "
				+ "(SELECT experiment_id, master_cpd_id, residuals "
				+ "FROM `data_curves_post_qc_res` WHERE `residuals` < " + drugSensitivity + " "
				+ "AND `master_cpd_id` IN(  "+ drugIDsString + " )) A "
				+ "INNER JOIN `meta_per_experiment` USING (experiment_id) "
				+ "INNER JOIN `gene_mutations_mut`"
				+ "USING (master_ccl_id)) AS C GROUP BY gene_symbol, master_cpd_id "
				+ "ORDER BY COUNT(master_ccl_id)  DESC "; // LIMIT " + max_gene_limit; 

		ResultSet rs = stm.executeQuery(sql); 
		String geneId = ""; 
		String drugId = ""; 
		while(rs.next()) {

			geneId = rs.getString("gene_id"); 
			drugId = rs.getString("master_cpd_id"); 

			//System.out.println(geneId + "\t" + drugId);

			if (genesToDrungMapping.containsKey(geneId) == false) {
				genesToDrungMapping.put(geneId, new ArrayList<>()); 
			}
			if (genesToDrungMapping.get(geneId).contains(drugId) == false) {
				genesToDrungMapping.get(geneId).add(drugId); 
			}
		}

		List<String> genesList = new ArrayList<String>(); 
		if (graphType.equals("summerized") == true) {
			for (Entry<String, ArrayList<String>> entry : genesToDrungMapping.entrySet())
			{
				if (entry.getValue().size() >= 2) {
					genesList.add(entry.getKey()); 
				}
			}
		} else {
			genesList = new ArrayList<>(genesToDrungMapping.keySet()) ; 
		}

		System.out.println(genesList.size());
		rs.close();
		stm.close(); 
		return genesList; 



	}
	
	public Map<String, Double> GetAssocatedGenesFreq(String graphType,
			String drugSensitivity, double threshold) throws SQLException {
		//System.out.println("HELLO WORLD");
		return this.GetAssocatedGenesFreq(graphType, drugSensitivity, threshold, true); 
	}
	
	public Map<String, Double> GetAssocatedGenesFreq(String graphType,
			String drugSensitivity, double threshold,  boolean display_geneName) throws SQLException {
		
		if (this.drugIds.size() == 1) {
			//System.out.println("Only one drug Prining Detailed Graph");
			graphType = "detailed"; 
		}
		
		if (graphType.equals("summerized") == true) {
			threshold = threshold / 2.0; 
		}
		HashMap<String, Double> frequencyMap = new HashMap<>(); 
		
		String drugIDsString = String.join(",", this.drugIds.keySet()); 
		
		HashMap<Integer, Integer> total_sensitive_cell_lines = new HashMap<>(); 
		
		HashMap<String, List<Integer>> genesToDrungMapping = new HashMap<>(); 
		
		
		String sql = "SELECT master_cpd_id, COUNT(experiment_id) AS TOTAL FROM `data_curves_post_qc_res` "
				+ "WHERE `residuals` < " + drugSensitivity 
				+ " AND `master_cpd_id` IN( "+ drugIDsString + ") GROUP BY master_cpd_id "; 
		//System.out.println(sql);
		
		java.sql.Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery(sql); 
		while(rs.next()) {
			total_sensitive_cell_lines.put(rs.getInt("master_cpd_id"), rs.getInt("TOTAL")); 
		}


		 sql = "SELECT master_cpd_id, gene_id, gene_symbol, COUNT(master_ccl_id)*mutation_weight AS Total_CellLines "
					+ "FROM (SELECT  DISTINCT master_cpd_id, master_ccl_id,  meta_genes.gene_symbol, gene_id, mutation_weight FROM "
					+ "(SELECT experiment_id, master_cpd_id, residuals "
					+ "FROM `data_curves_post_qc_res` WHERE `residuals` < " + drugSensitivity + " "
					+ "AND `master_cpd_id` IN(  "+ drugIDsString + " )) A "
					+ "INNER JOIN `meta_per_experiment` USING (experiment_id) "
					+ "INNER JOIN `gene_mutations_mut`USING (master_ccl_id) "
					+ "INNER JOIN meta_genes USING(gene_id)) AS C GROUP BY gene_symbol, master_cpd_id "
					+ "ORDER BY COUNT(master_ccl_id)  DESC"; 

		 
		//System.out.println(sql);
		rs = stm.executeQuery(sql); 
		while(rs.next()) {
			
			if (rs.getDouble("Total_CellLines") / total_sensitive_cell_lines.get(rs.getInt(("master_cpd_id")))
					< threshold) {
				continue; 
			}
			
			String output_field = "gene_id"; 
			if (display_geneName == true) {
				output_field = "gene_symbol"; 
			}
			
			if (genesToDrungMapping.containsKey(rs.getString(output_field)) == false) {
				genesToDrungMapping.put(rs.getString(output_field), new ArrayList<>());
			}
			genesToDrungMapping.get(rs.getString(output_field)).add(rs.getInt("master_cpd_id")); 
			//System.out.println(rs.getString(output_field));
			if (frequencyMap.containsKey(rs.getString(output_field)) == false) {
				frequencyMap.put(rs.getString(output_field), rs.getDouble("Total_CellLines"));
			} else {
				frequencyMap.put(rs.getString(output_field), 
						frequencyMap.get(rs.getString(output_field)) + rs.getDouble("Total_CellLines"));
			}
		}
		//System.out.println("WORK DONE");
		
		
		
		if (graphType.equals("summerized") == true) {
			System.out.println("Removing Single Drug Genes");
			for (Entry<String, List<Integer>> entry : genesToDrungMapping.entrySet())
			{
				if (entry.getValue().size() < 2) {
					frequencyMap.remove(entry.getKey()); 
				} 
			}
			
			//System.out.println(genesToDrungMapping.size());
		}
		
		double maxFreq = 0; 
		for (double freq : frequencyMap.values()) { 
			maxFreq = Math.max(maxFreq, freq); 
		}
		
		for (String entry : frequencyMap.keySet())
		{
			frequencyMap.replace(entry, frequencyMap.get(entry) / maxFreq); 
		}
		//System.out.println(frequencyMap);
		rs.close();
		stm.close();
		return frequencyMap; 
	}
	

	public void CloseConnection() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
	}
}
