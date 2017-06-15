package kr.seoul.amc.lggm.gccm.core;
import java.util.HashMap;

import kr.seoul.amc.lggm.gccm.core.Node;


public class Edge {
	public String ID; 
	public String from; 
	public String to; 
	public float weight; 
	public HashMap<String, String> Data = new HashMap<String, String>(); 
	
	
	
	public Edge(Node from, Node to) { 
		this.weight = 1.0f; 
		this.ID = from.ID + "_" + to.ID; 
		this.from = from.ID; 
		this.to = to.ID; 
	}
	
	public Edge (String from, String to) { 
		this.weight = 1.0f;
		this.ID = from + "_" + to; 
		this.from = from; 
		this.to = to; 
	}
	
	public void SetWeight(float weight) { 
		this.weight = weight; 
	}
	
	public void AddData(String key, String value) { 
		this.Data.put(key, value); 
	}
}
