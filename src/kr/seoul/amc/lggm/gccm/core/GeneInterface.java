package kr.seoul.amc.lggm.gccm.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class GeneInterface {
	private Connection conn;
	
	
	
	public GeneInterface(Connection conn)
	 {
	    this.conn = conn;
	 }
	
	public ArrayList<String[]> GetGenesCbioExpression(String geneId) throws SQLException {
		ArrayList<String[]> geneCbioRegulations = new ArrayList<>(); 
		String sql = "SELECT * FROM ("
				+ "SELECT *, ABS(up_regulated - down_regulated) / (up_regulated + down_regulated) AS score "
				+ "FROM `cbio_gene_expression_summary` WHERE gene_id = " + geneId 
				+ ") a INNER JOIN `cbio_case_studies_meta` USING(expression_type_id) "
				+ "INNER JOIN cancer_types_meta USING(cancer_type_id)  "
				+ " ORDER BY score DESC"; 
		System.out.println(sql);
		Statement stm = conn.createStatement(); 
		ResultSet rs = stm.executeQuery(sql); 
		while(rs.next()) {
			int total_samples = rs.getInt("total_samples" ); 
			double upRegulatedPercentage = rs.getDouble("up_regulated" ) / total_samples; 
			double downRegulatedPercentage = rs.getDouble("down_regulated" ) / total_samples; 
			double notRegulatedPercentage = rs.getDouble("score" ) / total_samples; 
			String[] geneData = new String[7]; 
			geneData[0] = rs.getString("gene_symbol"); 
			geneData[1] = rs.getString("name"); 
			geneData[2] = rs.getString("cancer_study"); 
			geneData[3] = rs.getString("measure_type"); 
			geneData[3] = geneData[3].replace("_", " ");
			geneData[4] = String.valueOf(Math.round(upRegulatedPercentage * 100)) + "%"; 
			geneData[5] = String.valueOf(Math.round(downRegulatedPercentage * 100)) + "%"; 
			//geneData[6] = String.valueOf(notRegulatedPercentage); 
			geneCbioRegulations.add(geneData); 
		}
		return geneCbioRegulations;
	}
		
	public ArrayList<String[]> GetGenesCbioMutations(String geneId) throws SQLException {
			ArrayList<String[]> geneCbioRegulations = new ArrayList<>(); 
			String sql = "SELECT * FROM (SELECT * FROM `cbio_variants`  WHERE gene_id = " + geneId + " ) A"
					+ " INNER JOIN cancer_types_meta ON cancer_type_id = typeOfCancer  "
					+ " ORDER BY ((`mutation` + `alteration`) / caseSetLength) DESC"; 
			
			System.out.println(sql);
			Statement stm = conn.createStatement(); 
			ResultSet rs = stm.executeQuery(sql); 
			while(rs.next()) {
				int total_samples = rs.getInt("caseSetLength" ); 
				
				double alterationPercentage = rs.getDouble("alteration" ) / total_samples; 
				double mutationPercentage = rs.getDouble("mutation" ) / total_samples; 
				
				String[] geneData = new String[7]; 
				geneData[0] = rs.getString("gene_symbol"); 
				geneData[1] = rs.getString("name"); 
				geneData[2] = rs.getString("caseSetId");
				//geneData[3] = geneData[3].replace("_", " ");
				geneData[4] = String.valueOf(Math.round(mutationPercentage * 100)) + "%"; 
				geneData[5] = String.valueOf(Math.round(alterationPercentage * 100)) + "%"; 
				geneCbioRegulations.add(geneData); 
			}
			return geneCbioRegulations;
	}
}
