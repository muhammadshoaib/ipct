package gcsearch;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import org.researchaware.core.DataExtracter;
import org.researchaware.core.MySQLConnector;
import org.xml.sax.InputSource;


import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

public class GeneSearchFactory {
	ServletContext context = null; 
	public GeneSearchFactory(ServletContext context) { 
		this.context = context; 
	}
	public String GetGeneGraphML(String inputType, String GeneIDs, 
			String databases,  String graphType, Map<String, String[]> perms) { 

		try {
			System.out.println(graphType); 
			Connection conn = MySQLConnector.ConnectToServer("172.25.53.172", "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement(); 

			GraphMLWriter gmlw = new GraphMLWriter(); 
			gmlw.AddKey(new Key("l0", "all", "label", "string")); 
			gmlw.AddKey(new Key("dbName", "node", "Database Name", "string"));
			gmlw.AddKey(new Key("fullName", "node", "Full Name", "string")); 
			gmlw.AddKey(new Key("url", "node", "URL", "string"));

			String GeneID; 
			String[] GeneIDsArray = GeneIDs.split(";"); 

			HashMap<String, Reactome> reactoms = new HashMap<>(); 
			String upDBTableName; 
			String upDBTableField; 
			
			String taxnFilter = ""; 
			if(perms.containsKey("taxn")) { 
			taxnFilter = perms.get("taxn")[0];
				if(taxnFilter.equals("all") == false && taxnFilter.equals("null") == false)
					taxnFilter = " AND OX LIKE '" + taxnFilter + "' "; 
				else { 
					taxnFilter = ""; 
				}
			}
			

			for(int outer = 0; outer<GeneIDsArray.length; outer++) { 
				GeneID = GeneIDsArray[outer]; 
				
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

					String sql = "SELECT * FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('" + GeneID + "')" + taxnFilter ; 
					System.out.println(sql); 
					ResultSet rs = stm.executeQuery(sql); 


					Node geneNode = new Node(GeneID); 
					geneNode.AddData("l0", GeneID);
					geneNode.AddData("dbName", "gene");
					geneNode.degree = 10; 
					gmlw.AddNode(geneNode);


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
					
					String recSQL = "SELECT * FROM `REACTOM_UNIPROT_MAPPINGS_BASE` INNER JOIN ("
							+ "SELECT DISTINCT `UNIPROT_ID` FROM "+ upDBTableName +" "
							+ "WHERE MATCH ("+upDBTableField+") AGAINST ('"+GeneID+"') " + taxnFilter + " ) AS a "
							+ "ON a.UNIPROT_ID = REACTOM_UNIPROT_MAPPINGS_BASE.UNIPROT_ID "
							+ "INNER JOIN REACTOME_PATHWAYS_NAMES USING (REACTOME_ID) ";
					
					String pathwayType = ""; 
					
					if(perms.containsKey("pathwayType")) { 
						pathwayType = perms.get("pathwayType")[0]; 
						if(pathwayType.equals("metabolicPathways")) { 
							recSQL = recSQL + " WHERE PATHWAY_TYPE = 'S'"; 
						} else if (pathwayType.equals("signalingPathways")) {
							recSQL = recSQL + " WHERE PATHWAY_TYPE = 'M'"; 
						} 
					}
					
					java.sql.Statement recStatement = conn.createStatement(); 
					System.out.println(recSQL); 
					ResultSet rrs = recStatement.executeQuery(recSQL); 
					
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
					
					recSQL = "SELECT * FROM `REACTOM_UNIPROT_MAPPINGS` INNER JOIN ("
							+ "SELECT DISTINCT `UNIPROT_ID` FROM "+ upDBTableName +" "
							+ "WHERE MATCH ("+upDBTableField+") AGAINST ('"+GeneID+"') " + taxnFilter + " ) AS a "
							+ "ON a.UNIPROT_ID = REACTOM_UNIPROT_MAPPINGS.UNIPROT_ID "
							+ "INNER JOIN REACTOME_PATHWAYS_NAMES USING (REACTOME_ID) ";
					
					if(perms.containsKey("pathwayType")) {
					pathwayType = perms.get("pathwayType")[0]; 
					if(pathwayType.equals("metabolicPathways")) { 
						recSQL = recSQL + " WHERE PATHWAY_TYPE = 'S'"; 
					} else if (pathwayType.equals("signalingPathways")) {
						recSQL = recSQL + " WHERE PATHWAY_TYPE = 'M'"; 
					} 
					}

					System.out.println(recSQL); 

					recStatement = conn.createStatement(); 
					System.out.println(recSQL); 
					 rrs = recStatement.executeQuery(recSQL); 
					while(rrs.next()) { 

						if(graphType.equals("summerized")) { 
							if(reactoms.containsKey(rrs.getString("REACTOME_ID")) == false) { 
								reactoms.put(rrs.getString("REACTOME_ID"), 
										new Reactome(rrs.getString("REACTOME_ID"), rrs.getString("REACTOME_NAME"), 
												rrs.getString("UNIPROT_ID"), GeneID)); 
							} else { 
								reactoms.get(rrs.getString("REACTOME_ID")).AddProtein(rrs.getString("UNIPROT_ID"));
								reactoms.get(rrs.getString("REACTOME_ID")).AddGene(GeneID);
							}
						} else { 

							Node recNode = new Node(rrs.getString("REACTOME_ID")); 
							recNode.AddData("l0", rrs.getString("REACTOME_NAME"));
							recNode.AddData("fullName", rrs.getString("REACTOME_NAME") + ", " + rrs.getString("DETAILS_2"));
							recNode.AddData("dbName", "reactome");
							recNode.AddData("url", rrs.getString("REACTOME_IRI"));
							gmlw.AddNode(recNode);
/*
							Edge recEdge = new Edge(rrs.getString("UNIPROT_ID"), rrs.getString("REACTOME_ID")); 
							recEdge.AddData("l0", "hasReactom");
							gmlw.AddEdge(recEdge);
							*/
						}
					}
					rrs.close();
					recStatement.close();

					if(!graphType.equals("summerized"))  {  
						recSQL = " SELECT RC1.RECT_1 AS RC11, RC1.RECT_2 AS RC12, RC2.`RECT_1` AS RC21, `RC2`.`RECT_2` AS RC22 FROM ( "
								+ "SELECT REACTOME_ID FROM `REACTOM_UNIPROT_MAPPINGS` INNER JOIN "
								+ "(SELECT DISTINCT `UNIPROT_ID` FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS a "
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
					
					/*
					recSQL = "SELECT DISTINCT GENE_NAME_SIMPLE,  GENE_NAME, REACTOME_ID FROM "
							+ "(SELECT REACTOM_UNIPROT_MAPPINGS_BASE.UNIPROT_ID, REACTOME_ID "
							+ "FROM REACTOM_UNIPROT_MAPPINGS_BASE "
					+ "INNER JOIN (SELECT UNIPROT_ID, REACTOME_ID FROM `REACTOM_UNIPROT_MAPPINGS_BASE` "
					+ "INNER JOIN (SELECT DISTINCT `UNIPROT_ID` "
					+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('" + GeneID +"')  ) AS a USING (UNIPROT_ID)) B "
					+ "USING (REACTOME_ID)) C INNER JOIN PROTEINS USING ( UNIPROT_ID) ";
					
					recStatement = conn.createStatement(); 
					System.out.println(recSQL); 
					 rrs = recStatement.executeQuery(recSQL); 
					while(rrs.next()) { 
						Node geneNode = new Node(rrs.getString("GENE_NAME_SIMPLE")); 
						geneNode.AddData("l0", rrs.getString("GENE_NAME_SIMPLE").split(" ")[0]);
						geneNode.AddData("dbName", "gene");
						geneNode.AddData("fullName", rrs.getString("GENE_NAME"));

						gmlw.AddNode(geneNode); 
						
						//System.out.println(rrs.getString("GENE_NAME_SIMPLE") + " " + rrs.getString("REACTOME_ID")); 

						Edge geneProtineEdge = new Edge(rrs.getString("GENE_NAME_SIMPLE"), rrs.getString("REACTOME_ID")); 
						geneProtineEdge.AddData("l0", "hasEffactOn");
						gmlw.AddEdge(geneProtineEdge);
					}
					*/
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
					
					sql = "SELECT * FROM (SELECT DISTINCT  `PROPERTY_VAL`, `ATLAS_EXPR_ID`, EXPR_ID,  LABEL, DIRECTION "
							+ "FROM ATLAS_EXPRESSION_CLEAN WHERE MATCH (LABEL) AGAINST ('"+GeneID+"') "
							+ "AND `PROPERTY_TYPE` = 'organism_part' " + organName + ") EXPRS INNER JOIN "
							+ "(SELECT ATLAS_UP_MAPPINGS.* FROM `ATLAS_UP_MAPPINGS` INNER JOIN "
							+ "(SELECT DISTINCT `UNIPROT_ID` "
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"') " + taxnFilter + " ) AS a "
							+ "USING(UNIPROT_ID ) )B ON EXPRS.ATLAS_EXPR_ID = B.`ATLAS_EXPR_ID` ";
					System.out.println(sql);
					java.sql.Statement atlesStm = conn.createStatement(); 
					ResultSet atlesrs = atlesStm.executeQuery(sql); 
					String atlesFectorValue = ""; 
					while(atlesrs.next()) { 
						atlesFectorValue = atlesrs.getString("PROPERTY_VAL").replace(" ", "_"); 
						if(!gmlw.ContainNode(atlesrs.getString("EXPR_ID"))) { 
							
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

						if(!gmlw.ContainNode(atlesFectorValue)) { 
							
							String organURL = GLOBAL_CONSTANTS.ATLAS_EXPRESSION_URL 
									+ "query?geneQuery=" + GeneID + "&ORGANISM_PART=" + atlesrs.getString("PROPERTY_VAL"); 
							
							Node atlesOrgNode = new Node(atlesFectorValue); 
							atlesOrgNode.AddData("l0", atlesrs.getString("PROPERTY_VAL"));
							atlesOrgNode.AddData("dbName", "atlas");
							atlesOrgNode.AddData("fullName", atlesrs.getString("LABEL"));
							atlesOrgNode.AddData("url", organURL);
							gmlw.AddNode(atlesOrgNode); 
							
						}
						Edge edge = new Edge(atlesrs.getString("EXPR_ID"), atlesFectorValue); 
						gmlw.AddEdge(edge); 
						//System.out.println("HELLO ");


					}

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
							+ "FROM PROTEINS WHERE MATCH (GENE_NAME) AGAINST ('"+GeneID+"')) AS a "
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
								+ "WHERE `MOLECULE_NAME` = '"+GeneID+"')  a "
								+ "LEFT JOIN `PROTEINS` USING (`UNIPROT_ID`)) b INNER JOIN  BIOSAMPLES_GENE_MAPPING "
								+ "ON `GENE_NAME` = `GENE_NAME_SIMPLE`)"
								+ " a LEFT JOIN `BIOSAMPLES_SIO_59` ON a.`SAMPLE_IRI` = BIOSAMPLES_SIO_59.`OBJECT` "
								+ " WHERE SUBJECT IS NOT NULL) SAMPLES INNER JOIN BIOSAMPLES_TAXN USING (SAMPLE_IRI)";
					} else { 
						bioSampleSQL = "SELECT * FROM (SELECT SAMPLE_IRI, SAMPLE_NAME, GENE_NAME, SUBJECT FROM ("
								+ "SELECT * FROM `BIOSAMPLES_GENE_MAPPING` WHERE `GENE_NAME` = '"+GeneID+"')"
								+ " a LEFT JOIN `BIOSAMPLES_SIO_59` ON a.`SAMPLE_IRI` = BIOSAMPLES_SIO_59.`OBJECT`"
								+ " WHERE SUBJECT IS NOT NULL) SAMPLES INNER JOIN BIOSAMPLES_TAXN USING (SAMPLE_IRI) "; 

					}

					System.out.println(bioSampleSQL); 
					java.sql.Statement bioSMPLStatement = conn.createStatement(); 
					ResultSet bsrs = bioSMPLStatement.executeQuery(bioSampleSQL); 
					while(bsrs.next()) { 
						String NodeTaxID = ""; 
						if(inputType == "molecule") { 
						
							NodeTaxID = bsrs.getString("GENE_NAME") + "_" + bsrs.getString("OX").replace("=", "_"); 
						} else {
							NodeTaxID = bsrs.getString("OX").replace("NCBI_TaxID=", ""); 
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

						String nodeID = bsrs.getString("SAMPLE_IRI").replace(
								"<http://rdf.ebi.ac.uk/resource/biosamples/sample/", "").replace(">", ""); 
						
						String sampleGroup = bsrs.getString("SUBJECT").replace(
								"<http://rdf.ebi.ac.uk/resource/biosamples/sample-group/", "").replace(">", ""); 

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
				}





			}
			stm.close();
			conn.close();

			if(graphType.equals("summerized")) { 
				Iterator it = reactoms.entrySet().iterator(); 
				//System.out.println("HELLO SUMERIZED"); 
				//System.out.println(reactoms.size()); 
				while(it.hasNext()) { 
					Map.Entry pair =  (Map.Entry) it.next(); 
					Reactome rec = (Reactome) pair.getValue(); 
					//System.out.println(rec.ReactomeID + " " + rec.Genes.toString()); 
					if(rec.HasMultiGeneRelations()) { 
						//System.out.println()
						Node reactomeNode = new Node(rec.ReactomeID); 
						reactomeNode.AddData("l0", rec.ReactomeLabel);
						reactomeNode.AddData("dbName", "reactome");
						gmlw.AddNode(reactomeNode); 

						for(int i = 0; i<rec.Proteins.size(); i++) { 
							Edge reactomeEdge = new Edge(rec.ReactomeID, rec.Proteins.get(i)); 
							reactomeEdge.AddData("l0", "hasReactome");
							gmlw.AddEdge(reactomeEdge); 
						}
					}
				}
			}

			//return gmlw.GetJSONString().toString(); 
			return gmlw.GetGraphML(); 


		}  catch (SQLException e) {
			// TODO Auto-generated catch block


			e.printStackTrace();
			return null;

		} 
	}
	
	public ArrayList<String> ReturnOrganisims() { 
		ArrayList<String> requiredList = new ArrayList<>(); 
		try {
			Connection conn = MySQLConnector.ConnectToServer("172.25.53.172", "cbio", "cbio", "GENEDB"); 

			java.sql.Statement stm = conn.createStatement();
			String sql = "SELECT * FROM GENEDB.PROTEINS_TAXONOMIES ORDER BY PROTEINS DESC LIMIT 0, 100"; 
			
			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				requiredList.add(rs.getString("OX") + "\t" + rs.getString("OS")); 
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}return requiredList; 
		
	}
	
	public ArrayList<String> ReturnOrgansList() { 
		ArrayList<String> requiredList = new ArrayList<>(); 
		
		try {
			Connection conn = MySQLConnector.ConnectToServer("172.25.53.172", "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement();
			String sql = "SELECT ORGAN_NAMES FROM ATLAS_BODY_ORGANS ORDER BY ORGAN_NAMES" ; 
			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) { 
				requiredList.add(rs.getString("ORGAN_NAMES")); 
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return requiredList; 
	}
	public ArrayList<String> ReturnList(String inputChar, String GeneMolecule) { 
		ArrayList<String> requiredList = new ArrayList<>(); 
		try {
			Connection conn = MySQLConnector.ConnectToServer("172.25.53.172", "cbio", "cbio", "GENEDB"); 
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
		} 
		return requiredList; 
	}
	public void AddLinkNodes() { 

	}

}
