package org.researchaware.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class DataExtracter {

	public static String GetDataFromWebURL(String requestUrl) {
		try {
        	String output = ""; 
            URL url = new URL(requestUrl.toString());
            BufferedReader in = new BufferedReader(
            		new InputStreamReader(url.openStream()));
            String inputLine;
            //System.out.println("-----RESPONSE START-----");
            while ((inputLine = in.readLine()) != null) {
                //System.out.println(inputLine);
                output = output + inputLine; 
            }
            in.close();
            //System.out.println("-----RESPONSE END-----");
            return output; 
            
            //Document doc = p.getDocument(); 
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return null; 
	}
	



}
