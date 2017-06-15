package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.seoul.amc.lggm.gccm.core.GeneSearchFactory;

/**
 * Servlet implementation class getOrganismList
 */
@WebServlet("/getOrganismList")
public class getOrganismList extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getOrganismList() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter writer = response.getWriter(); 
		String output = "<option value=\"all\">All</option>"; 
		
		ServletContext context = getServletContext(); 
		GeneSearchFactory gsf = new GeneSearchFactory(context); 
		
		ArrayList<String> list = gsf.ReturnOrganisims(); 
		String[] values = null; 
		
		for(int i = 0; i<list.size(); i++) { 
			//System.out.println(list.get(i)); 
			values = list.get(i).split("\t"); 
			
			if(values[1].length() > 50) { 
				values[1] = values[1].substring(0, values[1].indexOf('('));
			}
			output = output + "<option value=\"" + values[0] + "\">" + values[1] + "</option>\r\n";  
		}
		writer.println(output);
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */


}
