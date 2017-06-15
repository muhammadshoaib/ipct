package kr.seoul.amc.lggm.gccm.core;

public class Protein {
	public String UniprotAC; 
	public String ProteinName; 
	public String TexID; 
	
	public Protein ( String UniprotAC, String ProteinName, String TexID) { 
		this.UniprotAC = UniprotAC; 
		this.ProteinName = ProteinName; 
		this.TexID = TexID; 
	}
}
