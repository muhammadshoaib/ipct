package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.seoul.amc.lggm.gccm.core.DriverManager;
import kr.seoul.amc.lggm.gccm.core.DrugSearchFactory;

/**
 * Servlet implementation class GetAssocatedGenesFreq
 */
@WebServlet("/GetAssocatedGenesFreq")
public class GetAssocatedGenesFreq extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetAssocatedGenesFreq() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Connection conn = null; 
		try {
			
			ServletContext context = getServletContext(); 
			conn = DriverManager.getConnection(context); 
			
			String drugString = request.getParameter("drugString");
			
			DrugSearchFactory dsf = new DrugSearchFactory(context, conn, drugString); 
			Map<String, Double> associatedGenesFreq = dsf.GetAssocatedGenesFreq("summerized", "-1.50", 0.20);
			
			response.setContentType("text/plain");
			
			PrintWriter writer = response.getWriter(); 
			writer.write("id,value\n");
			for (Entry<String, Double> pair : associatedGenesFreq.entrySet()) {
				writer.write(pair.getKey() + "," + pair.getValue() + "\n");
			}
			writer.close();
			
		} catch (Exception e) {} 
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
