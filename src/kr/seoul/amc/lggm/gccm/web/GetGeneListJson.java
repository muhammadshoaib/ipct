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

import kr.seoul.amc.lggm.gccm.core.UtilitityFactory;

/**
 * Servlet implementation class GetGeneListJson
 */
@WebServlet("/GetGeneListJson")
public class GetGeneListJson extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetGeneListJson() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		response.setContentType("application/json");

		PrintWriter writer = response.getWriter(); 
		String type = request.getParameter("listType"); 
		String term = request.getParameter("term"); 
		System.out.println(request.getParameter("term"));
		
		
		ServletContext context = getServletContext(); 
		UtilitityFactory gsf = new UtilitityFactory(context); 
		
		System.out.println(type + " " + term);
		
		ArrayList<String> list = gsf.ReturnAllObjectsList(type, term); 

		String outputJSON = "["; 
		String item = ""; 
		int i = 0; 
		for(i = 0; i<(list.size()-1) ; i++) { 
			item = list.get(i).toUpperCase(); 
			outputJSON =   outputJSON + "\"" +  item + "\"" + " , "; 
		}
		item = list.get(i).toUpperCase(); 
		outputJSON =   outputJSON + "\"" +  item + "\""; 
		outputJSON = outputJSON + " ]"; 
		System.out.println(outputJSON); 
		writer.println(outputJSON);
		writer.close();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
