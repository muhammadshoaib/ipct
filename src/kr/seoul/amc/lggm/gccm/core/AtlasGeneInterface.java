package kr.seoul.amc.lggm.gccm.core;

public class AtlasGeneInterface {
	public String geneName; 
	public String exparimentalCondition;
	public String exparimenalOrgan; 
	public float expressionLFC;
	public float pValue; 
	
	public AtlasGeneInterface(String geneName, 
			String exparimentalCondition, 
			String exparimenalOrgan, 
			float expressionLFC, 
			float pValue) {
		this.geneName = geneName; 
		this.exparimentalCondition = exparimentalCondition;
		this.exparimenalOrgan = exparimenalOrgan; 
		this.expressionLFC = expressionLFC; 
		this.pValue = pValue; 
	}
	
}
