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
 * Servlet implementation class GetList
 */
@WebServlet("/GetList")
public class GetList extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetList() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter writer = response.getWriter(); 
		String index = request.getParameter("indexletter");
		String type = request.getParameter("listType"); 
		
		
		ServletContext context = getServletContext(); 
		GeneSearchFactory gsf = new GeneSearchFactory(context); 
		
		ArrayList<String> list = gsf.ReturnList(index, type); 
		String outputHTML = ""; 
		String item = ""; 
		for(int i = 0; i<list.size(); i++) { 
			if(list.get(i).length() < 10) { 
				item = list.get(i); 
			} else { 
				item = list.get(i).substring(0, 9) + "..."; 
			}
			item = item.toUpperCase(); 
			outputHTML = outputHTML + "<a class = 'listhyperlinks' href=# id=\""+list.get(i)+"\" title=\""+list.get(i)+"\"  >" + item + "</a>\r\n"; 
		}
		writer.println(outputHTML);
		writer.close();
		
	}

}
