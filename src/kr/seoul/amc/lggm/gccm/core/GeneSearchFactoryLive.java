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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.researchaware.core.DataExtracter;
import org.researchaware.core.MySQLConnector;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

public class GeneSearchFactoryLive {
	ServletContext context = null; 
	public GeneSearchFactoryLive(ServletContext context) { 
		this.context = context; 
	}
	public String GetGeneGraphML(String GeneID) { 

		System.out.println(); 
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		try {

			Connection conn = MySQLConnector.ConnectToServer("172.25.53.172", "cbio", "cbio", "GENEDB"); 
			java.sql.Statement stm = conn.createStatement(); 
			String sql = "SELECT * FROM PROTINE_GENE_REL WHERE MATCH (GENE_ID) AGAINST ('" + GeneID + "')"; 
			System.out.println(sql); 
			ResultSet rs = stm.executeQuery(sql); 
			
			GraphMLWriter gmlw = new GraphMLWriter(); 
			gmlw.AddKey(new Key("l0", "all", "label", "string")); 
			gmlw.AddKey(new Key("dbName", "node", "dbName", "string"));
			
			Node geneNode = new Node(GeneID); 
			geneNode.AddData("l0", GeneID);
			geneNode.AddData("dbName", "gene");
			gmlw.AddNode(geneNode);
			
			while(rs.next()) { 
				
				String uniprotAC = rs.getString("UNIPROT_ID"); 
				if(uniprotAC.indexOf(";") != -1) { 
					uniprotAC = uniprotAC.substring(0, uniprotAC.indexOf(";")); 
				}

				String protineURI = Globals.UNIPROT_UP_IRI + uniprotAC; 
				System.out.println(Globals.UNIPROT_UP_IRI + uniprotAC + ".rdf");
				String rdfData; 
				try { 
				 rdfData =  DataExtracter.GetDataFromWebURL(Globals.UNIPROT_UP_IRI + uniprotAC + ".rdf"); 
				//System.out.println(rdfData); 
				} catch(Exception ex) { 
					System.out.println("Error in Loading " + Globals.UNIPROT_UP_IRI + uniprotAC + ".rdf"); 
					continue; 
				}

				model.read(new ByteArrayInputStream(rdfData.getBytes()), Globals.UNIPROT_BASE_IRI);

				StmtIterator iter = model.listStatements();
				BasicTrippleStore basicTrippleStore = new BasicTrippleStore(); 

				ArrayList<String> nodes = new ArrayList<>(); 
				ArrayList<String> edges = new ArrayList<>(); 

				while(iter.hasNext()) { 
					basicTrippleStore.Add(iter.next());
				}

				for(int i = 0; i<basicTrippleStore.tirpples.size(); i++) { 
					Statement stm1 = basicTrippleStore.tirpples.get(i); 
					//System.out.println(stm1.getSubject() + " " + stm1.getPredicate() + " " + stm1.getObject() ) ; 
				}

				//String protineURI = "http://purl.uniprot.org/uniprot/P0C9I5"; 
				Statement[] stms = basicTrippleStore.Search(protineURI); 

				

				Node protineNode = new Node(uniprotAC); 
				protineNode.AddData("l0", uniprotAC);
				protineNode.AddData("dbName", "uniprot");

				gmlw.AddNode(protineNode);
				Edge geneProtineEdge = new Edge(geneNode, protineNode); 
				geneProtineEdge.AddData("l0", "encodedWith");
				gmlw.AddEdge(geneProtineEdge);
				
				/*
				ArrayList<String> predicates = new ArrayList<>(); 
				predicates.add(0, Globals.UNIPROT_BASE_IRI + "encodedBy");
				predicates.add(1, Globals.UNIPROT_BASE_IRI + "orfName"); 

				System.out.println(protineURI ); 
				List<RDFNode> genes = basicTrippleStore.NestedSearch(protineURI, predicates); 
				if(genes.size() < 1) { 
					predicates.add(1, Globals.SKOS_CORE + "prefLabel"); 
					genes = basicTrippleStore.NestedSearch(protineURI, predicates); 
				}

				System.out.println(protineURI + " "  + genes.size()); 

				for(int i = 0; i<genes.size(); i++) { 
					Node node = new Node("encodedBy_" + i); 
					node.AddData("l0", genes.get(i).toString());
					gmlw.AddNode(node);

					Edge edge = new Edge(protineNode, node); 
					edge.AddData("l0", "encodedBy" );
					gmlw.AddEdge(edge);
				}
				//System.out.println("HELLO 2" ); 
				*/
				ArrayList<RDFNode> organisms = basicTrippleStore.Search(protineURI, Globals.UNIPROT_BASE_IRI + "organism"); 
				int size = organisms.size(); 
				for(int i = 0; i<size; i++) { 
					String tempStr = organisms.toString(); 
					tempStr = tempStr.substring(tempStr.lastIndexOf("/")); 
					String nodeID = "organism_" + tempStr; 
					Node node = new Node(nodeID);
					node.AddData("l0", tempStr);
					node.AddData("dbName", "uniprot");
					gmlw.AddNode(node);
					Edge edge = new Edge(protineNode, node); 
					edge.AddData("l0", "organism");

					gmlw.AddEdge(edge);
				}

				AtlasExpression atlessExpression = new AtlasExpression(); 
				HashMap<String, List<Properties>> atlasmappings = atlessExpression.GetExpression(uniprotAC); 
				Iterator it = atlasmappings.entrySet().iterator();
				while(it.hasNext()) { 
					System.out.println("HELLO WORLD");
					Map.Entry pair = (Map.Entry)it.next();
					Node node = new Node((String) pair.getKey()); 
					node.AddData("l0", (String) pair.getKey());
					node.AddData("dbName", "atlas");
					gmlw.AddNode(node);
					Edge edge = new Edge(protineNode, node); 
					edge.AddData("l0", "expariment");
					gmlw.AddEdge(edge);
					List<Properties> properties = (List<Properties>) pair.getValue(); 

					for(int i = 0; i<properties.size(); i++) { 
						Properties p = properties.get(i); 
						Node pValueChieldNode = new Node((String) pair.getKey() + "_P_VALUE"); 
						pValueChieldNode.AddData("l0", p.getProperty("P_VALUE"));
						pValueChieldNode.AddData("dbName", "atlas");
						gmlw.AddNode(pValueChieldNode);
						Edge pValuEdge = new Edge(node, pValueChieldNode); 
						pValuEdge.AddData("l0", "P Value");
						gmlw.AddEdge(pValuEdge);
					}
				}
			}

			return gmlw.GetGraphML(); 


		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		} 
	}
	public void AddLinkNodes() { 

	}

}
