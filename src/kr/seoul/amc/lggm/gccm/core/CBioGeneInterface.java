package kr.seoul.amc.lggm.gccm.core;

public class CBioGeneInterface {
	public String geneName; 
	public float mutations; 
	public float alterations; 
	public float upRegulated; 
	public float downRegulated; 
	public int noOfCellLines; 
	
	public CBioGeneInterface(String geneName, 
			float mutations, 
			float alterations, 
			float upRegulated, 
			float downRegulated, 
			int noOfCellLines) {
		this.geneName = geneName; 
		this.mutations = mutations;
		this.alterations = alterations; 
		this.upRegulated = upRegulated; 
		this.downRegulated = downRegulated; 
		this.noOfCellLines = noOfCellLines; 

	}
}
