package kr.seoul.amc.lggm.gccm.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadGeneDataFromFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("files/UniProt.txt")));
			String str = null; 
			GraphMLWriter writer = new GraphMLWriter(); 
			int i = 0; 
			String UniCodeID = null; 
			while((str = br.readLine()) != null) { 
				String[] lines = str.split("\t"); 
				System.out.println(lines[lines.length-1]);
				i++; 
			}
			System.out.println(i);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
