package kr.seoul.amc.lggm.gccm.core;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

public class GeneSearchFactory {
	ServletContext context = null; 
	public String ServerIPAddress = ""; 
	public String dataSourceJNDIName = "java:/mygenedb"; 
	public GeneSearchFactory(ServletContext context) { 
		this.context = context; 
		
	}
	public String GetGeneGraphML(String inputType, String GeneIDs, 
			String databases,  String graphType, Map<String, String[]> perms) { 

		try {
			System.out.println(graphType); 
			Context initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			Connection conn = ds.getConnection();
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement(); 

			GraphMLWriter gmlw = new GraphMLWriter(); 
			gmlw.AddKey(new Key("l0", "all", "label", "string")); 
			gmlw.AddKey(new Key("dbName", "node", "Database Name", "string"));
			gmlw.AddKey(new Key("fullName", "node", "Full Name", "string")); 
			gmlw.AddKey(new Key("url", "node", "URL", "string"));
			gmlw.AddKey(new Key("details", "node", "Details", "string"));

			String GeneID; 
			String[] GeneIDsArray = GeneIDs.split(";"); 

			if(GeneIDsArray.length == 1 && inputType.equals("gene")) { 
				graphType = "detailed"; 
			}



			HashMap<String, String> geneUniProtMappings = new HashMap<>(); 
			HashMap<String, GraphObject> relatedgenes = new HashMap<>(); 
			HashMap<String, GraphObject> reactoms = new HashMap<>(); 
			HashMap<String, GraphObject> expressions = new HashMap<>(); 
			HashMap<String, GraphObject> biomodels = new HashMap<>(); 
			HashMap<String, GraphObject> biosamples = new HashMap<>(); 
			HashMap<String, GraphObject> ChemblMolecules = new HashMap<>(); 
			HashMap<String, GraphObject> geneAnnotations = new HashMap<>(); 


			String upDBTableName; 
			String upDBTableField; 

			String taxnFilter = ""; 
			if(perms.containsKey("taxn")) { 
				taxnFilter = perms.get("taxn")[0];
				if(taxnFilter.contains("all") == false && taxnFilter.equals("null") == false) {
					String[] taxnFilters = taxnFilter.split(","); 
					taxnFilter = " AND (OX LIKE '" + taxnFilters[0] + "'"; 
					for(int i = 1; i<taxnFilters.length; i++) { 
						taxnFilter = taxnFilter + " OR OX LIKE '" + taxnFilters[i] + "'" ; 
					}
					taxnFilter = taxnFilter + " ) "; 
				} else { 
					taxnFilter = ""; 
				}
			}


			for(int outer = 0; outer<GeneIDsArray.length; outer++) { 
				GeneID = GeneIDsArray[outer].trim().toUpperCase(); 

				String _geneID = GeneID; 

				System.out.println("Processing: " + GeneID); 

				upDBTableName = "SMALL_MOLECULES"; 
				upDBTableField = "MOLECULE_NAME"; 

				if(inputType.equals("molecule")) { 
					String sql = "SELECT DISTINCT PROTEINS.`UNIPROT_ID`, `PROTEIN_NAME`, `GENE_NAME_SIMPLE`, GENE_NAME, OX, OC, OS "
							+ "FROM `PROTEINS` INNER JOIN "
							+ "(SELECT DISTINCT `UNIPROT_ID` FROM `SMALL_MOLECULES` "
							+ "WHERE MATCH (`MOLECULE_NAME`) AGAINST ('" + GeneID + "') " + taxnFilter + ") "
							+ "AS a ON a.UNIPROT_ID = PROTEINS.`UNIPROT_ID`"; 
					System.out.println(sql); 
					ResultSet rs = stm.executeQuery(sql); 
					Node molecureNode = new Node(GeneID); 
					molecureNode.AddData("l0", GeneID);
					molecureNode.AddData("dbName", "molecule");
					gmlw.AddNode(molecureNode);

					while(rs.next()) { 

						if(gmlw.ContainNode(rs.getString("GENE_NAME_SIMPLE")) == false) { 
							Node geneNode = new Node(rs.getString("GENE_NAME_SIMPLE")); 
							geneNode.AddData("l0", rs.getString("GENE_NAME_SIMPLE").split(" ")[0]);
							geneNode.AddData("dbName", "gene");
							geneNode.AddData("fullName", rs.getString("GENE_NAME"));

							gmlw.AddNode(geneNode); 

							Edge geneProtineEdge = new Edge(geneNode, molecureNode); 
							geneProtineEdge.AddData("l0", "hasEffactOn");
							gmlw.AddEdge(geneProtineEdge);
						}

						Node protineNode = new Node(rs.getString("UNIPROT_ID")); 
						protineNode.AddData("l0", rs.getString("PROTEIN_NAME").split(" ")[0]);
						protineNode.AddData("dbName", "uniprot");
						protineNode.AddData("fullName", rs.getString("PROTEIN_NAME"));
						protineNode.AddData("url", GLOBAL_CONSTANTS.PROTEINS_URL + rs.getString("UNIPROT_ID"));

						gmlw.AddNode(protineNode);
						Edge geneProtineEdge = new Edge(rs.getString("GENE_NAME_SIMPLE"), rs.getString("UNIPROT_ID")); 
						geneProtineEdge.AddData("l0", "encodedWith");
						//geneProtineEdge.weight = 0.2f; 
						gmlw.AddEdge(geneProtineEdge);

						geneUniProtMappings.put(rs.getString("UNIPROT_ID"), rs.getString("GENE_NAME_SIMPLE")); 

						String texID =  rs.getString("OX").trim().split(" ", 2)[0]; 

						texID = texID.split("=")[1]; 

						if(databases.contains("organism")) {
							Node texonomyNode = new Node(texID); 
							texonomyNode.AddData("l0", rs.getString("OS").trim());
							texonomyNode.AddData("dbName", "uniprot");
							texonomyNode.AddData("fullName", rs.getString("OC").trim());
							gmlw.AddNode(texonomyNode);


							Edge protineTexEdge = new Edge(protineNode, texonomyNode); 
							geneProtineEdge.AddData("l0", "hasTexonomy");
							//geneProtineEdge.weight = 0.1f;
							gmlw.AddEdge(protineTexEdge);

						}
					}

				} else { 

					upDBTableName = "PROTEINS"; 
					upDBTableField = "GENE_NAME"; 
					
					String sql = "SELECT * FROM GENES_DETAILS WHERE GENE_NAME_SIMPLE = '" + GeneID + "'" ; 
					ResultSet rs = stm.executeQuery(sql); 
					rs.next(); 
					Node geneNode = new Node(GeneID); 
					geneNode.AddData("l0", GeneID);
					geneNode.AddData("dbName", "gene");
					geneNode.AddData("fullName", rs.getString("APPROVED_NAME"));
					geneNode.AddData("url", GLOBAL_CONSTANTS.NCBI_GENE + rs.getString("ENTREZ_GENE_ID"));
					geneNode.degree = 10; 
					gmlw.AddNode(geneNode);
					rs.close();

					 sql = "SELECT * FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('" + GeneID + "')" + taxnFilter ; 
					System.out.println(sql); 
					 rs = stm.executeQuery(sql); 

					while(rs.next()) { 

						String uniprotAC = rs.getString("UNIPROT_ID"); 
						if(uniprotAC.indexOf(";") != -1) { 
							uniprotAC = uniprotAC.substring(0, uniprotAC.indexOf(";")); 
						}

						//String protineURI = Globals.UNIPROT_UP_IRI + uniprotAC; 
						//System.out.println(Globals.UNIPROT_UP_IRI + uniprotAC + ".rdf");

						String texID =  rs.getString("OX").trim().split(" ", 2)[0]; 

						texID = texID.split("=")[1]; 


						Node protineNode = new Node(uniprotAC); 
						protineNode.AddData("l0", rs.getString("PROTEIN_NAME").split(" ")[0]);
						protineNode.AddData("dbName", "uniprot");
						protineNode.AddData("fullName", rs.getString("PROTEIN_NAME"));
						protineNode.AddData("url", GLOBAL_CONSTANTS.PROTEINS_URL + rs.getString("UNIPROT_ID"));

						gmlw.AddNode(protineNode);
						Edge geneProtineEdge = new Edge(geneNode, protineNode); 
						geneProtineEdge.AddData("l0", "encodedWith");
						//geneProtineEdge.weight = 0.2f; 
						gmlw.AddEdge(geneProtineEdge);


						if(databases.contains("organism")) {
							Node texonomyNode = new Node(texID); 
							texonomyNode.AddData("l0", rs.getString("OS").trim());
							texonomyNode.AddData("dbName", "uniprot");
							texonomyNode.AddData("fullName", rs.getString("OC").trim());
							gmlw.AddNode(texonomyNode);


							Edge protineTexEdge = new Edge(protineNode, texonomyNode); 
							geneProtineEdge.AddData("l0", "hasTexonomy");
							//protineTexEdge.weight = 0.2f; 
							gmlw.AddEdge(protineTexEdge);
						}
						//System.out.println(texID);


					} 
					rs.close();
					
					/*
					sql = "SELECT * FROM GENEDB.PROTEINS_GO_ANOT "
							+ "INNER JOIN ("
							+ "SELECT UNIPROT_ID FROM GENEDB.PROTEINS "
							+ "WHERE MATCH(GENE_NAME_SIMPLE) AGAINST ('" + GeneID + "')" + taxnFilter + ") AS A "
							+ "USING(UNIPROT_ID)"; 
					System.out.println(sql); 
					 rs = stm.executeQuery(sql); 

					while(rs.next()) { 
						String GoVal = rs.getString("GO_ANNOTATION"); 
						String GOID = GoVal.split("; ")[0]; 
						char _GOType = GoVal.split(";")[1].charAt(0); 
						String GOType = ""; 
						if(_GOType == 'C') {
							GOType = "CellulerComponent"; 
							
						} else if(_GOType == 'P') {
							GOType = "BiologicalProcesses"; 
							
						}else if(_GOType == 'F') {
							GOType = "MolecularFunctions"; 
						}
						
						
						if(graphType.equals("summerized")) { 
							if(geneAnnotations.containsKey(GOID) == false) { 
								geneAnnotations.put(GOID, 
										new GraphObject(GOID,GoVal, 
												rs.getString("UNIPROT_ID"), _geneID, null) ); 
							} else { 
								geneAnnotations.get(GOID).AddProtein(rs.getString("UNIPROT_ID"));
								geneAnnotations.get(GOID).AddGene(_geneID);
							}
							continue; 
						}
						
						Node goAnnotation = new Node(GOID); 
						goAnnotation.AddData("l0", GOID);
						goAnnotation.AddData("dbName", "GeneOntology");
						goAnnotation.AddData("fullName", rs.getString("GO_ANNOTATION").trim());
						gmlw.AddNode(goAnnotation);
						
						Edge protineGoAnotationEdge = new Edge(GOID, rs.getString("UNIPROT_ID")); 
						protineGoAnotationEdge.AddData("l0", "hasTexonomy");
						//protineTexEdge.weight = 0.2f; 
						gmlw.AddEdge(protineGoAnotationEdge);
						
					}
					*/
					
				}
				if(databases.contains("chembl")) {

					String chemblSQL = "SELECT B.*, chembl_id FROM (SELECT * FROM `SMALL_MOLECULES` INNER JOIN ("
							+ "SELECT DISTINCT `UNIPROT_ID` FROM PROTEINS "
							+ "WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"') " + taxnFilter +") AS a "
							+ "USING(UNIPROT_ID)  LIMIT 0, 300) B INNER JOIN MOL_DIC USING (molregno)";
					System.out.println(chemblSQL); 
					java.sql.Statement chemblStatement = conn.createStatement(); 
					ResultSet chrs = chemblStatement.executeQuery(chemblSQL); 
					while(chrs.next()) { 

						if(inputType.equals( "molecule" )) { 
							_geneID = geneUniProtMappings.get(chrs.getString("UNIPROT_ID")); 
						}

						if(graphType.equals("summerized")) { 
							if(ChemblMolecules.containsKey(chrs.getString("chembl_id")) == false) { 
								ChemblMolecules.put(chrs.getString("chembl_id"), 
										new GraphObject(chrs.getString("chembl_id"), chrs.getString("MOLECULE_NAME"), 
												chrs.getString("UNIPROT_ID"), _geneID, GLOBAL_CONSTANTS.ChEMBL_URL + chrs.getString("chembl_id")) ); 
							} else { 
								ChemblMolecules.get(chrs.getString("chembl_id")).AddProtein(chrs.getString("UNIPROT_ID"));
								ChemblMolecules.get(chrs.getString("chembl_id")).AddGene(_geneID);
							}
							continue; 
						}

						String chrID = chrs.getString("molregno")+"_" + chrs.getString("record_id"); 
						Node chbNode = new Node(chrID); 
						chbNode.AddData("l0", chrs.getString("MOLECULE_NAME"));
						chbNode.AddData("dbName", "chembl");
						chbNode.AddData("fullName", chrs.getString("MOLECULE_NAME"));
						chbNode.AddData("url", GLOBAL_CONSTANTS.ChEMBL_URL + chrs.getString("chembl_id"));
						gmlw.AddNode(chbNode);

						Edge chblEdge = new Edge(chrID, chrs.getString("UNIPROT_ID")); 
						chblEdge.AddData("l0", "haChambel");
						gmlw.AddEdge(chblEdge);
					}
				}

				if(databases.contains("reactome")) { 

					String recSQL = "";
					String pathwayType = ""; 
					java.sql.Statement recStatement = null; 
					ResultSet rrs = null; 

					if(!graphType.equals("summerized")) { 

						recSQL = "SELECT * FROM `REACTOM_UNIPROT_MAPPINGS_BASE` INNER JOIN ("
								+ "SELECT DISTINCT `UNIPROT_ID` FROM "+ upDBTableName +" "
								+ "WHERE MATCH ("+upDBTableField+") AGAINST ('"+GeneID+"') " + taxnFilter + " ) AS a "
								+ "ON a.UNIPROT_ID = REACTOM_UNIPROT_MAPPINGS_BASE.UNIPROT_ID "
								+ "INNER JOIN REACTOME_PATHWAYS_NAMES USING (REACTOME_ID) ";



						if(perms.containsKey("pathwayType")) { 
							pathwayType = perms.get("pathwayType")[0]; 
							if(pathwayType.equals("metabolicPathways")) { 
								recSQL = recSQL + " WHERE PATHWAY_TYPE = 'M'"; 
							} else if (pathwayType.equals("signalingPathways")) {
								recSQL = recSQL + " WHERE PATHWAY_TYPE = 'S'"; 
							} 
						}

						recStatement = conn.createStatement(); 
						System.out.println(recSQL); 
						rrs = recStatement.executeQuery(recSQL); 

						while(rrs.next()) { 
							Node recNode = new Node(rrs.getString("REACTOME_ID")); 
							recNode.AddData("l0", rrs.getString("REACTOME_NAME"));
							recNode.AddData("fullName", rrs.getString("REACTOME_NAME") + ", " + rrs.getString("DETAILS_2"));
							recNode.AddData("dbName", "reactome");
							recNode.AddData("url", rrs.getString("REACTOME_IRI"));
							gmlw.AddNode(recNode);

							Edge recEdge = new Edge(rrs.getString("UNIPROT_ID"), rrs.getString("REACTOME_ID")); 
							recEdge.AddData("l0", "hasReactom");
							gmlw.AddEdge(recEdge);
						}

					}

					recSQL = "SELECT * FROM `REACTOM_UNIPROT_MAPPINGS` INNER JOIN ("
							+ "SELECT DISTINCT `UNIPROT_ID` FROM "+ upDBTableName +" "
							+ "WHERE MATCH ("+upDBTableField+") AGAINST ('"+GeneID+"') " + taxnFilter + " ) AS a "
							+ "ON a.UNIPROT_ID = REACTOM_UNIPROT_MAPPINGS.UNIPROT_ID "
							+ "INNER JOIN REACTOME_PATHWAYS_NAMES USING (REACTOME_ID) ";

					if(perms.containsKey("pathwayType")) {
						pathwayType = perms.get("pathwayType")[0]; 
						if(pathwayType.equals("metabolicPathways")) { 
							recSQL = recSQL + " WHERE PATHWAY_TYPE = 'M'"; 
						} else if (pathwayType.equals("signalingPathways")) {
							recSQL = recSQL + " WHERE PATHWAY_TYPE = 'S'"; 
						} 
					}

					System.out.println(recSQL); 

					recStatement = conn.createStatement(); 
					rrs = recStatement.executeQuery(recSQL); 

					System.out.println(geneUniProtMappings);
					while(rrs.next()) { 

						if(inputType.equals( "molecule" )) { 
							_geneID = geneUniProtMappings.get(rrs.getString("UNIPROT_ID")); 
						}

						if(graphType.equals("summerized")) { 
							if(reactoms.containsKey(rrs.getString("REACTOME_ID")) == false) { 
								reactoms.put(rrs.getString("REACTOME_ID"), 
										new GraphObject(rrs.getString("REACTOME_ID"), rrs.getString("REACTOME_NAME"), 
												rrs.getString("UNIPROT_ID"), _geneID, rrs.getString("REACTOME_IRI")) ); 
							} else { 
								reactoms.get(rrs.getString("REACTOME_ID")).AddProtein(rrs.getString("UNIPROT_ID"));
								reactoms.get(rrs.getString("REACTOME_ID")).AddGene(_geneID);
							}
						} else { 

							Node recNode = new Node(rrs.getString("REACTOME_ID")); 
							recNode.AddData("l0", rrs.getString("REACTOME_NAME"));
							recNode.AddData("fullName", rrs.getString("REACTOME_NAME") + ", " + rrs.getString("DETAILS_2"));
							recNode.AddData("dbName", "reactome");
							recNode.AddData("url", rrs.getString("REACTOME_IRI"));
							gmlw.AddNode(recNode);

							Edge recEdge = new Edge(rrs.getString("UNIPROT_ID"), rrs.getString("REACTOME_ID")); 
							recEdge.AddData("l0", "hasReactom");
							gmlw.AddEdge(recEdge);

						}
					}
					//System.out.println(reactoms); 
					rrs.close();
					recStatement.close();

					if(!graphType.equals("summerized"))  {  
						recSQL = " SELECT RC1.RECT_1 AS RC11, RC1.RECT_2 AS RC12, RC2.`RECT_1` AS RC21, `RC2`.`RECT_2` AS RC22 FROM ( "
								+ "SELECT REACTOME_ID FROM `REACTOM_UNIPROT_MAPPINGS` INNER JOIN "
								+ "(SELECT DISTINCT `UNIPROT_ID` FROM "+ upDBTableName +" WHERE MATCH ("+upDBTableField+") AGAINST ('"+GeneID+"') " + taxnFilter + ") AS a "
								+ " USING(UNIPROT_ID)) B  INNER JOIN  `REACTOME_PATHWAYS` AS RC1 ON B.REACTOME_ID = RC1.`RECT_1`"
								+ "INNER JOIN REACTOME_PATHWAYS AS RC2 ON B.REACTOME_ID = RC2.`RECT_2`"; 

						System.out.println(recSQL); 

						recStatement = conn.createStatement(); 
						System.out.println(recSQL); 
						rrs = recStatement.executeQuery(recSQL); 
						while(rrs.next()) { 
							if(gmlw.ContainNode(rrs.getString("RC11")) && 
									gmlw.ContainNode(rrs.getString("RC12"))) { 
								Edge recEdge1 = new Edge(rrs.getString("RC11"), rrs.getString("RC12")); 
								recEdge1.AddData("l0", "hasPathWay");
								gmlw.AddEdge(recEdge1);
								Edge recEdge2 = new Edge(rrs.getString("RC21"), rrs.getString("RC22")); 
								recEdge2.AddData("l0", "hasPathWay");
								gmlw.AddEdge(recEdge2);
							}
						}
					}


					recSQL = "SELECT DISTINCT GENE_NAME_SIMPLE,  GENE_NAME, REACTOME_ID FROM "
							+ "(SELECT REACTOM_UNIPROT_MAPPINGS_BASE.UNIPROT_ID, REACTOME_ID "
							+ "FROM REACTOM_UNIPROT_MAPPINGS_BASE "
							+ "INNER JOIN (SELECT UNIPROT_ID, REACTOME_ID FROM `REACTOM_UNIPROT_MAPPINGS_BASE` "
							+ "INNER JOIN (SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('" + GeneID +"') " + taxnFilter + " ) AS a USING (UNIPROT_ID)) B "
							+ "USING (REACTOME_ID)) C INNER JOIN PROTEINS USING ( UNIPROT_ID) ";


					recStatement.close();
					rrs.close(); rrs = null; 
				}


				if(databases.contains("atlas")) { 
					String sql = "SELECT * FROM `ALTAS_UP_MAPPINGS` INNER JOIN ("
							+ "SELECT DISTINCT `UNIPROT_ID` FROM PROTEINS "
							+ "WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS a "
							+ "ON a.UNIPROT_ID = ALTAS_UP_MAPPINGS.`UNIPROT_AC`";
					sql = "SELECT * FROM (SELECT DISTINCT UP_AC, `FECTOR_VAL`, `EXPR_ID` "
							+ "FROM ATLAS_EXPRESSION_CLEAN WHERE MATCH (LABEL) AGAINST ('"+GeneID+"') "
							+ "AND `FECTOR_PROP` = 'organism_part') EXPRS INNER JOIN "
							+ "(SELECT ALTAS_UP_MAPPINGS.* FROM `ALTAS_UP_MAPPINGS` INNER JOIN "
							+ "(SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"') AND OX LIKE 'NCBI_TaxID=9606') AS a "
							+ "ON a.UNIPROT_ID = ALTAS_UP_MAPPINGS.`UNIPROT_AC`) B "
							+ "ON EXPRS.UP_AC = B.`ALTAS_MAPPING_ID` "; 

					String organName = ""; 
					if(perms.containsKey("organName")) { 
						organName = perms.get("organName")[0]; 
						System.out.println(perms.get("organName")[0]); 
						if(organName.equals("all") == false  && organName.equals("null") == false) { 
							organName = " AND PROPERTY_VAL LIKE '" + organName + "'"; 
						} else { organName = ""; } 
					} 

					sql = "SELECT group_concat(DISTINCT UNIPROT_ID) UNIPROT_IDS, PROPERTY_VAL, group_concat(DISTINCT LABEL) LABEL, "
							+ "group_concat(DISTINCT DIRECTION) DIRECTION"
							+ " FROM (SELECT DISTINCT  `PROPERTY_VAL`, `ATLAS_EXPR_ID`, EXPR_ID,  LABEL, DIRECTION "
							+ "FROM ATLAS_EXPRESSION_CLEAN WHERE MATCH (LABEL) AGAINST ('"+GeneID+"') "
							+ "AND `PROPERTY_TYPE` = 'organism_part' " + organName + ") EXPRS INNER JOIN "
							+ "(SELECT ATLAS_UP_MAPPINGS.* FROM `ATLAS_UP_MAPPINGS` INNER JOIN "
							+ "(SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"') " + taxnFilter + " ) AS a "
							+ "USING(UNIPROT_ID ) )B ON EXPRS.ATLAS_EXPR_ID = B.`ATLAS_EXPR_ID` GROUP BY PROPERTY_VAL";
					
					
					//sql = sql  + "WHERE DIRECTION = 'DOWN'"; 

					System.out.println(sql);
					java.sql.Statement atlesStm = conn.createStatement(); 
					ResultSet atlesrs = atlesStm.executeQuery(sql); 
					String atlesFectorValue = ""; 
					while(atlesrs.next()) { 

						atlesFectorValue = atlesrs.getString("PROPERTY_VAL").replace(" ", "_"); 

						String[] uniprotids = atlesrs.getString("UNIPROT_IDS").split(",");

						if(graphType.equals("summerized")) { 

							if(expressions.containsKey(atlesFectorValue) == false) { 
								expressions.put(atlesFectorValue, 
										new GraphObject(atlesFectorValue, atlesrs.getString("PROPERTY_VAL"), 
												atlesrs.getString("UNIPROT_IDS"), GeneID, null)); 
							} else { 
								expressions.get(atlesFectorValue).AddProtein(atlesrs.getString("UNIPROT_IDS"));
								expressions.get(atlesFectorValue).AddGene(GeneID);
							}
							expressions.get(atlesFectorValue).AddAtlasDirection(atlesrs.getString("DIRECTION"));
						}


						else { 

							if(!gmlw.ContainNode(atlesFectorValue)) { 

								String organURL = GLOBAL_CONSTANTS.ATLAS_EXPRESSION_URL 
										+ "query?geneQuery=" + GeneID + "&ORGANISM_PART=" + atlesrs.getString("PROPERTY_VAL"); 

								Node atlesOrgNode = new Node(atlesFectorValue); 
								atlesOrgNode.AddData("l0", atlesrs.getString("PROPERTY_VAL"));
								atlesOrgNode.AddData("dbName", "atlas" + "_" + atlesrs.getString("DIRECTION").replace(",", "_"));
								atlesOrgNode.AddData("fullName", atlesrs.getString("LABEL"));
								atlesOrgNode.AddData("url", organURL);
								gmlw.AddNode(atlesOrgNode); 



							}

							for(int i = 0; i<uniprotids.length; i++) { 
								Edge edge = new Edge(uniprotids[i], atlesFectorValue); 
								gmlw.AddEdge(edge); 
							}

						}

						/*
						if(!gmlw.ContainNode(atlesrs.getString("DIRECTION"))) { 

							String atlasExprURL = GLOBAL_CONSTANTS.ATLAS_EXPRESSION_URL + "experiments/" + 
									atlesrs.getString("EXPR_ID") + "?geneQuery=" + GeneID; 

							Node atlesExprNode = new Node(atlesrs.getString("EXPR_ID")); 
							atlesExprNode.AddData("l0", atlesrs.getString("EXPR_ID"));
							atlesExprNode.AddData("dbName", "atlas");

							atlesExprNode.AddData("url", atlasExprURL);
							gmlw.AddNode(atlesExprNode); 
							Edge edge = new Edge(atlesrs.getString("UNIPROT_ID"), atlesrs.getString("EXPR_ID")); 
							gmlw.AddEdge(edge); 
						}

						//System.out.println("HELLO ");
						 */

					}
					atlesrs.close();
					atlesStm.close();
					atlesrs = null; 

				}

				if(databases.contains("biomodels")) { 
					/*
					String dataModelSql = "SELECT * FROM BIOMODELS INNER JOIN ("
							+ "SELECT DISTINCT `MODEL_SUB_ID` "
							+ "FROM `BIOMODELS` INNER JOIN (SELECT * FROM BIOMODELS_MAPPINGS WHERE `UNIPROT_ID` = '"+uniprotAC+"') AS a ON "
							+ "`MODEL_INT_ID` = `BIOMODEL_ID`) AS B ON B.MODEL_SUB_ID = BIOMODELS.`MODEL_SUB_ID` "
							+ "WHERE `MODEL_PROP` = 'sbmlRdf:name'; "; 
					 */
					String dataModelSql = "SELECT DISTINCT OX, `PROP_VAL` FROM  (SELECT MODEL_ID, OX FROM `UP_BIOMODELS_S` INNER JOIN"
							+ "(SELECT UNIPROT_ID, OX FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS A ON "
							+ "UP_BIOMODELS_S.`UNIPROT_ID` = A.UNIPROT_ID ) B INNER JOIN `BIOMODELS_NAMES` ON `MODEL_SUB_ID` = B.`MODEL_ID`"; 

					dataModelSql = "SELECT DISTINCT OX, MODEL_CORE_ID FROM  "
							+ "(SELECT DISTINCT `MODEL_CORE_ID`, OX FROM `UP_BIOMODELS_S` INNER JOIN(SELECT UNIPROT_ID, OX "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS A "
							+ "ON UP_BIOMODELS_S.`UNIPROT_ID` = A.UNIPROT_ID ) B "
							+ "LEFT JOIN `BIOMODELS_NAMES` ON `MODEL_SUB_ID` = B.`MODEL_CORE_ID`"; 



					dataModelSql = "SELECT BIOMODELS_NAMES.MODEL_ID, BIOMODELS_NAMES.OBJECT AS SPECIES_NAME, UNIPROT_ID, OX FROM ( "
							+ "SELECT * FROM BIOMODELS_UNIPROT_MAPPINGS INNER JOIN ("
							+ "SELECT UNIPROT_ID, replace(OX, 'NCBI_TaxID=', '') AS OX FROM "+ upDBTableName +" "
							+ "WHERE MATCH ("+upDBTableField+") AGAINST ('"+GeneID+"') " + taxnFilter + " ) A "
							+ "ON A.UNIPROT_ID = OBJECT) B LEFT JOIN BIOMODELS_NAMES USING (SUBJECT)  "
							+ "WHERE BIOMODELS_NAMES.MODEL_ID IS NOT NULL AND OX IS NOT NULL "; 

					if(databases.contains("reactome")) { 
						dataModelSql = "SELECT * FROM ( " + dataModelSql + " ) CORE_MODELS "
								+ "LEFT JOIN  BIOMODELS_REACTOME_MAPPINGS USING(MODEL_ID)"; 
					}

					System.out.println(dataModelSql); 
					java.sql.Statement bioModelStatement = conn.createStatement(); 
					ResultSet dnmrs = bioModelStatement.executeQuery(dataModelSql); 
					int modelID = 0; 
					while(dnmrs.next()) { 

						/*
						String texID =  dnmrs.getString("OX").trim(); 

						if(databases.contains("organism") == false) {
							Node texonomyNode = new Node(texID); 
							texonomyNode.AddData("l0", dnmrs.getString("OS").trim());
							texonomyNode.AddData("dbName", "uniprot");
							texonomyNode.AddData("fullName", dnmrs.getString("OC").trim());
							gmlw.AddNode(texonomyNode);


							Edge protineTexEdge = new Edge(dnmrs.getString("UNIPROT_ID"), texID); 
							protineTexEdge.AddData("l0", "hasTexonomy");
							//protineTexEdge.weight = 0.2f; 
							gmlw.AddEdge(protineTexEdge);
						}
						 */
						String nodeID = dnmrs.getString("MODEL_ID").replace(
								"http://identifiers.org/biomodels.db/", "").replace(".rdf", ""); 

						if(graphType.equals("summerized")) { 
							if(biomodels.containsKey(nodeID) == false) { 
								biomodels.put(nodeID, new GraphObject(nodeID, nodeID, dnmrs.getString("UNIPROT_ID"),
										GeneID, null)); 
							}
							else { 

								biomodels.get(nodeID).AddProtein(dnmrs.getString("UNIPROT_ID"));
								biomodels.get(nodeID).AddGene(GeneID);
							}
							continue; 
						}

						if(gmlw.ContainNode(nodeID) == false) { 
							Node dmNode = new Node(new String(nodeID)); 
							dmNode.AddData("l0", nodeID);
							dmNode.AddData("dbName", "biomodels");
							dmNode.AddData("fullName", nodeID);
							dmNode.AddData("url", GLOBAL_CONSTANTS.BIO_MODELS_URL + nodeID );
							gmlw.AddNode(dmNode);
							//System.out.println(dnmrs.getString("OX"));


							//texID = texID.split("=")[1]; 

							Edge biomodelEdge = new Edge(nodeID, dnmrs.getString("UNIPROT_ID") ); 
							biomodelEdge.AddData("l0", "hasModel");
							gmlw.AddEdge(biomodelEdge);
						}

						String specieID = dnmrs.getString("SPECIES_NAME").replace(" ", "_"); 
						if(gmlw.ContainNode(specieID) == false) { 
							Node specieNode = new Node(new String(specieID)); 
							specieNode.AddData("l0", dnmrs.getString("SPECIES_NAME"));
							specieNode.AddData("dbName", "biomodels");
							specieNode.AddData("fullName", dnmrs.getString("SPECIES_NAME"));
							gmlw.AddNode(specieNode);


						}

						Edge biomodelEdge = new Edge(nodeID, specieID); 
						biomodelEdge.AddData("l0", "hasSpecies");
						gmlw.AddEdge(biomodelEdge);

						if(databases.contains("reactome")) { 
							Edge biomodelReactEdge = new Edge(nodeID, dnmrs.getString("REACT_ID")); 
							biomodelReactEdge.AddData("l0", "hasReactomePathway");
							if(!gmlw.ContainEdge(biomodelReactEdge.ID)) {
								gmlw.AddEdge(biomodelReactEdge);
							}
							else { biomodelReactEdge = null; }
						}

						/*
						String inodeID = nodeID + modelID; 

						Node dmNode = new Node(new String(inodeID)); 
						dmNode.AddData("l0",  dnmrs.getString("PROP_VAL"));
						dmNode.AddData("dbName", "biomodels");
						dmNode.AddData("fullName", dnmrs.getString("PROP_VAL"));
						gmlw.AddNode(dmNode);


						Edge biomodelEdge = new Edge(nodeID, inodeID); 
						biomodelEdge.AddData("l0", "hasName");
						gmlw.AddEdge(biomodelEdge);
						 */
					}
					dnmrs.close(); 
					bioModelStatement.close(); 
				}

				if(databases.contains("biosamples")) { 
					String bioSampleSQL = "SELECT * FROM ("
							+ "SELECT * FROM `ALTAS_UP_MAPPINGS` INNER JOIN (SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')" + taxnFilter + ") AS a "
							+ "ON a.UNIPROT_ID = ALTAS_UP_MAPPINGS.`UNIPROT_AC` WHERE `EXPARIMENT_ID` IS NOT NULL) AS B "
							+ "INNER JOIN DRIEVED_FROM WHERE OBJECT = EXPARIMENT_ID"; 
					bioSampleSQL = "SELECT SIO_59.OBJECT AS SAMPLE, UNIPROT_AC, EXPARIMENT_ID FROM ("
							+ "SELECT SUBJECT AS SAMPLEGROUP, UNIPROT_AC, EXPARIMENT_ID FROM `DRIEVED_FROM` INNER JOIN ("
							+ "SELECT UNIPROT_AC, `EXPARIMENT_ID` FROM `ALTAS_UP_MAPPINGS` INNER JOIN (SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS a "
							+ "ON a.UNIPROT_ID = ALTAS_UP_MAPPINGS.`UNIPROT_AC` WHERE `EXPARIMENT_ID` IS NOT NULL) B ON "
							+ "DRIEVED_FROM.`OBJECT` = EXPARIMENT_ID) GROUPS INNER JOIN "
							+ "`SIO_59` ON GROUPS.SAMPLEGROUP = SIO_59.SUBJECT"; 
					bioSampleSQL = "SELECT SIO_59.OBJECT AS SAMPLE, UNIPROT_AC,  `EXPR_ID` "
							+ "FROM (SELECT SUBJECT AS SAMPLEGROUP, UNIPROT_AC, EXPR_ID  "
							+ "FROM `DRIEVED_FROM` INNER JOIN (SELECT UNIPROT_AC, `EXPR_ID`  "
							+ "FROM `UNIPROT_EXPRESSION_MAPPING` INNER JOIN (SELECT DISTINCT `UNIPROT_ID`  "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS a  "
							+ "ON a.UNIPROT_ID = UNIPROT_EXPRESSION_MAPPING.`UNIPROT_AC`) B  "
							+ "ON DRIEVED_FROM.`OBJECT` = EXPR_ID) GROUPS INNER JOIN `SIO_59`  "
							+ "ON GROUPS.SAMPLEGROUP = SIO_59.SUBJECT"; 



					if(inputType.equals("molecule")) { 
						bioSampleSQL = "SELECT * FROM (SELECT * FROM (SELECT DISTINCT `SAMPLE_IRI`, `SAMPLE_NAME`, `GENE_NAME` "
								+ "FROM (SELECT `GENE_NAME_SIMPLE` FROM (SELECT * FROM `SMALL_MOLECULES` "
								+ "WHERE `MOLECULE_NAME` = '"+GeneID+"')   a "
								+ "LEFT JOIN `PROTEINS` USING (`UNIPROT_ID`)) b INNER JOIN  BIOSAMPLES_GENE_MAPPING "
								+ "ON `GENE_NAME` = `GENE_NAME_SIMPLE`)"
								+ " a LEFT JOIN `BIOSAMPLES_SIO_59` ON a.`SAMPLE_IRI` = BIOSAMPLES_SIO_59.`OBJECT` "
								+ " WHERE SUBJECT IS NOT NULL) SAMPLES INNER JOIN BIOSAMPLES_TAXN USING (SAMPLE_IRI)";
					} else { 
						bioSampleSQL = "SELECT * FROM (SELECT DISTINCT SAMPLE_IRI, SAMPLE_NAME, GENE_NAME, SUBJECT FROM ("
								+ "SELECT * FROM `BIOSAMPLES_GENE_MAPPING` WHERE `GENE_NAME` = '"+GeneID+"')"
								+ " a LEFT JOIN `BIOSAMPLES_SIO_59` ON a.`SAMPLE_IRI` = BIOSAMPLES_SIO_59.`OBJECT`"
								+ " WHERE SUBJECT IS NOT NULL) SAMPLES INNER JOIN BIOSAMPLES_TAXN USING (SAMPLE_IRI)"; 
						if(taxnFilter.equals("") == false) {
							bioSampleSQL = bioSampleSQL + " WHERE " + taxnFilter.replace("AND", "") ;  
						}

					}

					System.out.println(bioSampleSQL); 
					java.sql.Statement bioSMPLStatement = conn.createStatement(); 
					ResultSet bsrs = bioSMPLStatement.executeQuery(bioSampleSQL); 
					while(bsrs.next()) { 

						String nodeID = bsrs.getString("SAMPLE_IRI").replace(
								"<http://rdf.ebi.ac.uk/resource/biosamples/sample/", "").replace(">", ""); 

						String sampleGroup = bsrs.getString("SUBJECT").replace(
								"<http://rdf.ebi.ac.uk/resource/biosamples/sample-group/", "").replace(">", ""); 

						String NodeTaxID = ""; 
						if(inputType == "molecule") { 

							NodeTaxID = bsrs.getString("GENE_NAME") + "_" + bsrs.getString("OX").replace("=", "_"); 
						} else {
							NodeTaxID = bsrs.getString("OX").replace("NCBI_TaxID=", ""); 
						}

						if(graphType.equals("summerized")) { 
							if(biosamples.containsKey(nodeID) == false) { 
								biosamples.put(nodeID, new GraphObject(nodeID, bsrs.getString("SAMPLE_NAME"), 
										null, bsrs.getString("GENE_NAME"), GLOBAL_CONSTANTS.BIO_SAMPLES_URL + nodeID)); 
							} else {
								biosamples.get(nodeID).Genes.add(bsrs.getString("GENE_NAME")); 
							}

							continue; 
						}

						if(gmlw.ContainNode(NodeTaxID) == false) { 

							Node dsSampleNodeTax = new Node(NodeTaxID); 
							dsSampleNodeTax.AddData("l0",bsrs.getString("OX"));
							dsSampleNodeTax.AddData("dbName", "biosamples");
							dsSampleNodeTax.AddData("fullName", bsrs.getString("OX"));
							gmlw.AddNode(dsSampleNodeTax);

							Edge bioSampleGroup = new Edge(bsrs.getString("GENE_NAME"), NodeTaxID); 
							bioSampleGroup.AddData("l0", "hasSample");
							gmlw.AddEdge(bioSampleGroup);

						}



						if(gmlw.ContainNode(sampleGroup) == false) { 
							Node dsSampleNode = new Node(sampleGroup); 
							dsSampleNode.AddData("l0",sampleGroup);
							dsSampleNode.AddData("dbName", "biosamples");
							dsSampleNode.AddData("fullName", sampleGroup);
							gmlw.AddNode(dsSampleNode);

							Edge bioSampleGroup = new Edge(NodeTaxID, sampleGroup); 
							bioSampleGroup.AddData("l0", "hasSample");
							gmlw.AddEdge(bioSampleGroup);

						}


						Node dsNode = new Node(nodeID); 
						dsNode.AddData("l0",bsrs.getString("SAMPLE_NAME"));
						dsNode.AddData("dbName", "biosamples");
						dsNode.AddData("fullName", bsrs.getString("SAMPLE_NAME"));
						dsNode.AddData("url", GLOBAL_CONSTANTS.BIO_SAMPLES_URL + nodeID);
						gmlw.AddNode(dsNode);

						Edge biomodelEdge = new Edge(sampleGroup, nodeID); 
						biomodelEdge.AddData("l0", "hasSample");
						gmlw.AddEdge(biomodelEdge);


						/*
						if(gmlw.ContainNode(bsrs.getString("EXPR_ID")) == false) { 
							Node expressionNode = new Node(bsrs.getString("EXPR_ID")); 
							expressionNode.AddData("l0",nodeText);
							expressionNode.AddData("dbName", "atlas");
							expressionNode.AddData("fullName", bsrs.getString("EXPR_ID"));
							gmlw.AddNode(expressionNode);

							String protID =  bsrs.getString("UNIPROT_AC"); 

							Edge expressionEdge = new Edge(protID, bsrs.getString("EXPR_ID")); 
							expressionEdge.AddData("l0", "hasSample");
							gmlw.AddEdge(expressionEdge);
						}
						 */

					}
					bioSMPLStatement.close();
					bsrs.close();
					bsrs = null; 

				}

				if(databases.contains("associatedGenes")) { 
					/*
					String geneRelSQL = "SELECT DISTINCT GENE_NAME_SIMPLE,  GENE_NAME, COUNT(GENE_NAME_SIMPLE), group_concat( REACTOME_ID) AS REACTOMES "
							+ "FROM (SELECT REACTOM_UNIPROT_MAPPINGS_BASE.UNIPROT_ID, REACTOME_ID "
							+ "FROM REACTOM_UNIPROT_MAPPINGS_BASE INNER JOIN (SELECT UNIPROT_ID, REACTOME_ID "
							+ "FROM `REACTOM_UNIPROT_MAPPINGS` INNER JOIN (SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('" + GeneID +"')  ) AS a USING (UNIPROT_ID)) B "
							+ "USING (REACTOME_ID)) C INNER JOIN PROTEINS USING ( UNIPROT_ID)"
							+ "GROUP BY  GENE_NAME_SIMPLE HAVING COUNT(GENE_NAME_SIMPLE) > "
							+ "(SELECT GENES_LIST.REACTOMES*0.10 FROM GENES_LIST WHERE MATCH (GENE_NAME_SIMPLE) AGAINST ('" + GeneID +"'))";
					*/
					String pathwayFilter = ""; 
					if(perms.containsKey("pathwayType")) {
						String pathwayType = perms.get("pathwayType")[0]; 
						if(pathwayType.equals("metabolicPathways")) { 
							pathwayFilter = " INNER JOIN REACTOME_PATHWAYS_NAMES USING (REACTOME_ID) WHERE PATHWAY_TYPE = 'M'"; 
						} else if (pathwayType.equals("signalingPathways")) {
							pathwayFilter = " INNER JOIN REACTOME_PATHWAYS_NAMES USING (REACTOME_ID) WHERE PATHWAY_TYPE = 'S'"; 
						} 
					}
					
					String geneRelSQL = "SELECT GENE_NAME_SIMPLE, ENTREZ_GENE_ID, COUNT(PATHWAY), group_concat( DISTINCT REACTOME_NAME) pathways, group_concat( DISTINCT DIRECTION) FROM ( "
							+ "SELECT DISTINCT PATHWAY, REACTION, REACTOME_NAME FROM ( "
							+ "SELECT UNIPROT_ID, REACTOME_ID , REACTOME_NAME "
							+ "FROM `REACTOM_UNIPROT_MAPPINGS` INNER JOIN (SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('" + GeneID +"') " + taxnFilter + ") AS a USING (UNIPROT_ID) " + pathwayFilter + ") PATHWAYS "
							+ "INNER JOIN PATHWAY_REACTIONS ON PATHWAY_REACTIONS.PATHWAY = REACTOME_ID )REACTIONS "
							+ "INNER JOIN REACTION_GENE_RELATIONSHIP USING(REACTION) "
							+ "INNER JOIN GENES_LIST USING(GENE_NAME_SIMPLE) "
							+ "LEFT JOIN GENES_DETAILS USING (GENE_NAME_SIMPLE) GROUP BY GENE_NAME_SIMPLE  "
							+ "HAVING COUNT(PATHWAY) > (SELECT GENES_LIST.REACTOMES*0.001 FROM GENES_LIST  "
							+ "WHERE (GENE_NAME_SIMPLE) = ('" + GeneID +"')  )";

					Statement geneRelStatement = conn.createStatement(); 
					System.out.println(geneRelSQL); 
					ResultSet grrs = geneRelStatement.executeQuery(geneRelSQL); 


					if(grrs.isBeforeFirst()) { 


						String linkedObjectID = GeneID + "LinkedGenes";  

						if(graphType.equals("summerized") == false) { 
							Node geneRelMainNode = new Node(linkedObjectID); 
							gmlw.AddNode(geneRelMainNode); 

							Edge genegeneRelEdge = new Edge(linkedObjectID, GeneID); 
							genegeneRelEdge.AddData("l0", "CoOccuredWith");
							gmlw.AddEdge(genegeneRelEdge);
						}


						while(grrs.next()) { 
							String geneUrl = GLOBAL_CONSTANTS.NCBI_GENE + grrs.getString("ENTREZ_GENE_ID"); 
							if(graphType.equals("summerized")) { 
								if(relatedgenes.containsKey(grrs.getString("GENE_NAME_SIMPLE")) == false) { 
									
									relatedgenes.put(grrs.getString("GENE_NAME_SIMPLE"), 
											new GraphObject(grrs.getString("GENE_NAME_SIMPLE"), grrs.getString("GENE_NAME_SIMPLE"), 
													"none", GeneID, geneUrl)); 
								} else { 
									//relatedgenes.get(grrs.getString("GENE_NAME_SIMPLE")).AddProtein(grrs.getString("UNIPROT_ID"));
									relatedgenes.get(grrs.getString("GENE_NAME_SIMPLE")).AddGene(GeneID);
								}
							} else { 

								if(grrs.getString("GENE_NAME_SIMPLE").equals(GeneID))
									continue; 

								Node geneNode = new Node(grrs.getString("GENE_NAME_SIMPLE")); 
								geneNode.AddData("l0", grrs.getString("GENE_NAME_SIMPLE").split(" ")[0]);
								geneNode.AddData("dbName", "associatedGenes");
								geneNode.AddData("fullName", grrs.getString("GENE_NAME_SIMPLE"));
								geneNode.AddData("details", grrs.getString("pathways"));
								geneNode.AddData("url", geneUrl);
								gmlw.AddNode(geneNode); 

								//System.out.println(rrs.getString("GENE_NAME_SIMPLE") + " " + rrs.getString("REACTOME_ID")); 

								Edge geneProtineEdge = new Edge(grrs.getString("GENE_NAME_SIMPLE"), linkedObjectID); 
								//geneProtineEdge.AddData("l0", "hasEffactOn");
								gmlw.AddEdge(geneProtineEdge);

								// */
							}
						}
					}
					geneRelStatement.close();
					grrs.close();
				}


			}
			stm.close();
			conn.close();

			if(graphType.equals("summerized")) {

				int totalGenes; 
				totalGenes = GeneIDsArray.length; 
				totalGenes = 2; 

				Iterator it = reactoms.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				//System.out.println(reactoms.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject rec = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ObjectID + " " + rec.Genes.toString()); 
					if(rec.HasMultiGeneRelations()) { 
						//System.out.println()
						Node reactomeNode = new Node(rec.ObjectID); 
						reactomeNode.AddData("l0", rec.ObjectLabel);
						reactomeNode.AddData("dbName", "reactome");
						gmlw.AddNode(reactomeNode); 

						for(int i = 0; i<rec.Proteins.size(); i++) { 
							Edge reactomeEdge = new Edge(rec.ObjectID, rec.Proteins.get(i)); 
							reactomeEdge.AddData("l0", "hasReactome");
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}

				it = biomodels.entrySet().iterator(); 

				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject model = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ReactomeID + " " + rec.Genes.toString()); 
					//System.out.println(model.Genes);
					if(model.HasMultiGeneRelations()) { 
						//System.out.println()
						Node reactomeNode = new Node(model.ObjectID); 
						reactomeNode.AddData("l0", model.ObjectLabel);
						reactomeNode.AddData("dbName", "biomodels");
						gmlw.AddNode(reactomeNode); 

						for(int i = 0; i<model.Proteins.size(); i++) { 
							Edge reactomeEdge = new Edge(model.ObjectID, model.Proteins.get(i)); 
							reactomeEdge.AddData("l0", "hasModel");
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}


				it = ChemblMolecules.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				//System.out.println(reactoms.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject rec = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ObjectID + " " + rec.Genes.toString()); 
					if(rec.HasMultiGeneRelations()) { 
						//System.out.println()
						Node reactomeNode = new Node(rec.ObjectID); 
						reactomeNode.AddData("l0", rec.ObjectLabel);
						reactomeNode.AddData("dbName", "chembl");
						gmlw.AddNode(reactomeNode); 

						for(int i = 0; i<rec.Proteins.size(); i++) { 
							Edge reactomeEdge = new Edge(rec.ObjectID, rec.Proteins.get(i)); 
							reactomeEdge.AddData("l0", "hassmallMolecule");
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}

				it = geneAnnotations.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				System.out.println(geneAnnotations.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject rec = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ObjectID + " " + rec.Genes.toString()); 
					if(rec.HasMultiGeneRelations()) { 
						//System.out.println()
						Node reactomeNode = new Node(rec.ObjectID); 
						reactomeNode.AddData("l0", rec.ObjectLabel);
						reactomeNode.AddData("dbName", "geneAnnotation");
						gmlw.AddNode(reactomeNode); 

						for(int i = 0; i<rec.Proteins.size(); i++) { 
							Edge reactomeEdge = new Edge(rec.ObjectID, rec.Proteins.get(i)); 
							reactomeEdge.AddData("l0", "hasGoAnnotation");
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}

				//totalGenes = 2; 

				it = expressions.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				//System.out.println(reactoms.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject object = (GraphObject) pair.getValue(); 
					//System.out.println(rec.ReactomeID + " " + rec.Genes.toString()); 
					if(object.HasDefinedGenesRelations(totalGenes)) { 
						//System.out.println()
						Node objectNode = new Node(object.ObjectID); 
						objectNode.AddData("l0", object.ObjectLabel);
						objectNode.AddData("dbName", "atlas_" + object.GetAtlasDirections());
						gmlw.AddNode(objectNode); 

						for(int i = 0; i<object.Proteins.size(); i++) { 
							Edge reactomeEdge = new Edge(object.ObjectID, object.Proteins.get(i)); 
							reactomeEdge.AddData("l0", "hasExpressionIN");
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}

				it = relatedgenes.entrySet().iterator(); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject object = (GraphObject) pair.getValue(); 

					if(object.HasDefinedGenesRelations(totalGenes)) { 
						Node objectNode = new Node(object.ObjectID); 
						objectNode.AddData("l0", object.ObjectLabel);
						objectNode.AddData("dbName", "associatedGenes");
						gmlw.AddNode(objectNode); 

						for(int i = 0; i<object.Genes.size(); i++) { 
							/*
							String linkedObjectID = object.Genes.get(i) + "LinkedGenes";  
							if(gmlw.ContainNode(linkedObjectID)) { 
								Node geneRelMainNode = new Node(linkedObjectID); 
								gmlw.AddNode(geneRelMainNode); 

								Edge genegeneRelEdge = new Edge(linkedObjectID, object.Genes.get(i)); 
								genegeneRelEdge.AddData("l0", "CoOccuredWith");
								gmlw.AddEdge(genegeneRelEdge);
							}
							 */

							Edge reactomeEdge = new Edge(object.ObjectID, object.Genes.get(i)); 
							reactomeEdge.AddData("l0", "isRelatedTo");
							gmlw.AddEdge(reactomeEdge); 
						}	
					}
				}


				it = biosamples.entrySet().iterator(); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					GraphObject object = (GraphObject) pair.getValue(); 
					//System.out.println(object.Genes);
					if(object.HasDefinedGenesRelations(totalGenes)) { 
						Node objectNode = new Node(object.ObjectID); 
						objectNode.AddData("l0", object.ObjectLabel);
						objectNode.AddData("dbName", "biosamples");
						objectNode.AddData("url", object.ObjectURL);

						gmlw.AddNode(objectNode); 

						for(int i = 0; i<object.Genes.size(); i++) {

							Edge reactomeEdge = new Edge(object.ObjectID, object.Genes.get(i)); 
							reactomeEdge.AddData("l0", "isRelatedTo");
							gmlw.AddEdge(reactomeEdge); 
						}	
					}
				}
			}


			if(perms.containsKey("graphOption")) { 
				if(perms.get("graphOption")[0].equals("json")) { 
					//System.out.println("HELLO");
					return gmlw.GetJSONString().toString(); 
				}
				else if(perms.get("graphOption")[0].equals("csv")) { 
					return gmlw.GetCSVString(); 
				}
			} 
			//
			return gmlw.GetGraphML(); 


		}  catch (SQLException | NamingException e) {
			// TODO Auto-generated catch block


			e.printStackTrace();
			return null;

		} 
	}

	public ArrayList<String> ReturnOrganisims() throws SQLException { 
		ArrayList<String> requiredList = new ArrayList<>(); 
		Connection conn = null; 
		try {
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			
			Context initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			 conn = ds.getConnection();

			java.sql.Statement stm = conn.createStatement();
			String sql = "SELECT * FROM GENEDB.PROTEINS_TAXONOMIES ORDER BY PROTEINS DESC LIMIT 0, 100"; 

			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				requiredList.add(rs.getString("OX") + "\t" + rs.getString("OS")); 
			}

		} catch (SQLException | NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close(); 
		}
		return requiredList; 
	}

	public ArrayList<String> ReturnOrgansList() throws SQLException { 
		ArrayList<String> requiredList = new ArrayList<>(); 
		Connection conn = null; 
		try {
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			Context initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			 conn = ds.getConnection();
			java.sql.Statement stm = conn.createStatement();
			String sql = "SELECT ORGAN_NAMES FROM ATLAS_BODY_ORGANS ORDER BY ORGAN_NAMES" ; 
			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				requiredList.add(rs.getString("ORGAN_NAMES")); 
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return requiredList; 
	}
	public ArrayList<String> ReturnList(String inputChar, String GeneMolecule) { 
		Connection conn = null; 
		ArrayList<String> requiredList = new ArrayList<>(); 
		try {
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			Context initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			 conn = ds.getConnection();
			
			java.sql.Statement stm = conn.createStatement();
			String sql = null; 
			inputChar = inputChar.trim().replace("_", ""); 
			if(GeneMolecule.equals("gene")){ 
				sql = "SELECT GENE_NAME_SIMPLE AS NAME FROM GENES_LIST WHERE "; 
			} else { 
				sql = "SELECT MOLECULE_NAME AS NAME FROM SMALL_MOLECULES_LIST WHERE "; 
			}
			if(inputChar.length() == 1) { 
				sql = sql + "INDEX_1 = '" + inputChar + "'"; 
			} else if(inputChar.length() == 2) { 
				sql = sql + "INDEX_2 = '" + inputChar + "'"; 
			} else if(inputChar.length() == 3) { 
				sql = sql + "INDEX_3 = '" + inputChar + "'"; 
			} else { 
				return requiredList; 
			}
			System.out.println(sql); 
			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				requiredList.add(rs.getString("NAME").trim()); 
			}
			rs.close();
			stm.close();
			conn.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return requiredList; 
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return requiredList; 
	}
	
	
	public ArrayList<String> ReturnAllObjectsList(String GeneMolecule, String term) { 
		Connection conn = null; 
		ArrayList<String> requiredList = new ArrayList<>(); 
		try {
			//Connection conn = MySQLConnector.ConnectToServer(ServerIPAddress, "cbio", "cbio", "GENEDB"); 
			Context initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup(dataSourceJNDIName);
			 conn = ds.getConnection();
			
			java.sql.Statement stm = conn.createStatement();
			String sql = null; 
			if(GeneMolecule.equals("gene")){ 
				sql = "SELECT GENE_NAME_SIMPLE AS NAME FROM GENES_LIST "
						+ "WHERE GENE_NAME_SIMPLE LIKE '" + term + "%' LIMIT 0, 20"; 
			} else { 
				sql = "SELECT MOLECULE_NAME AS NAME FROM SMALL_MOLECULES_LIST "
						+ "WHERE MOLECULE_NAME LIKE '" + term + "%' LIMIT 0, 20"; ; 
			}

			System.out.println(sql); 
			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				requiredList.add(rs.getString("NAME").trim()); 
			}
			rs.close();
			stm.close();
			conn.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return requiredList; 
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return requiredList; 
	}
	
	public void AddLinkNodes() { 

	}

}
