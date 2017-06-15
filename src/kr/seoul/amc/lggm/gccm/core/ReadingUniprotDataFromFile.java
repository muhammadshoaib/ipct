package kr.seoul.amc.lggm.gccm.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadingUniprotDataFromFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File("uniprot_sprot.dat")));
			String line; 
			int i = 0; 
			while((line = br.readLine()) != null) { 
				i++; 
			}
			System.out.println("Total number of rows : " + i);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

}
