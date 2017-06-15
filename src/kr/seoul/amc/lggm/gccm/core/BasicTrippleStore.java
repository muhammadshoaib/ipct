package kr.seoul.amc.lggm.gccm.core;

import java.util.ArrayList;
import java.util.Queue;

import riotcmd.trig;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class BasicTrippleStore {
	ArrayList<Statement> tirpples = new ArrayList<>(); 
	public void Add(Statement stm) { 
		this.tirpples.add(stm); 
	}
	public Statement[] Search(Resource subject) { 
		return Search(subject.toString()); 
	}
	public Statement[] Search(String subject) { 
		int size = tirpples.size(); 
		ArrayList<Statement> searchedStatements = new ArrayList<>(); 
		for(int i = 0; i< size; i++) { 
			if(tirpples.get(i).getSubject().toString().equals(subject)) { 
				searchedStatements.add(tirpples.get(i)); 
				//return tirpples.get(i); 
			}
		}
		return (Statement[]) searchedStatements.toArray(new Statement[searchedStatements.size()]); 
	}
	public ArrayList<RDFNode> Search(Resource subject, Resource predicate) { 
		
		return Search(subject.toString(), predicate.toString()); 
	}
	
	
	public ArrayList<RDFNode> Search(String subject, String predicate) { 
		ArrayList<RDFNode> objects = new ArrayList<>(); 
		int size = tirpples.size(); 
		Statement stm = null; 
		for(int i = 0; i<size; i++) { 
			stm = tirpples.get(i); 
			if(stm.getSubject().toString().equals(subject) && 
					stm.getPredicate().toString().equals(predicate)) { 
				objects.add(stm.getObject()); 
			}
		}
		return objects;
	}
	
	public ArrayList<RDFNode> NestedSearch(String subject, ArrayList<String> properties) { 
		
		ArrayList<RDFNode> objects = new ArrayList<>(); 
		
		
		ArrayList<RDFNode> firstOrderNodes = Search(subject, properties.get(0)); 
		System.out.println(firstOrderNodes.size()); 
		for(int i = 0; i<firstOrderNodes.size(); i++) { 
			objects.addAll(Search(firstOrderNodes.get(i).toString(), properties.get(1))); 
		}
		
		return objects;
	}
}
