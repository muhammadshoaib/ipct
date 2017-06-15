package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kr.seoul.amc.lggm.gccm.core.AtlasExpression;
import kr.seoul.amc.lggm.gccm.core.AtlasGeneInterface;
import kr.seoul.amc.lggm.gccm.core.DriverManager;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class GetAtlasTable
 */
@WebServlet("/GetAtlasTable")

public class GetAtlasTable extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetAtlasTable() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println(request.getQueryString()); 
		System.out.println(request.getParameter("start")); 
		HttpSession session = request.getSession(false) ; 		
		ServletContext context = getServletContext(); 
		
		Connection conn = null; 
		try {
			
			conn = DriverManager.getConnection(context); 
			
			String drugString = request.getParameter("drugString");
			DecimalFormat df2 = new DecimalFormat("0.00");
			AtlasExpression atlasExpressionClass = new AtlasExpression(conn);
			
			if (session.getAttribute("drugString") == null ) {
				System.out.println("Adding New Session");
				session.setAttribute("drugString",drugString);
				atlasExpressionClass = new AtlasExpression(conn);
				
			} else if (session.getAttribute("drugString").equals(drugString) == false) {
				System.out.println("Adding New Session");
				session.setAttribute("drugString", drugString);
				atlasExpressionClass = new AtlasExpression(conn);
			}
			else {
				System.out.println("Drug Name Already Exists");
				System.out.println(session.getAttribute("drugString"));
				atlasExpressionClass = new AtlasExpression(conn, 
						(Set<String>) session.getAttribute("drug_associated_genes"));
			}
			
			System.out.println(session.getAttribute("drugString"));
			
			
			
			List<AtlasGeneInterface> atlasExpressions = 
					atlasExpressionClass.GetAtlasTableByDrugNames(drugString, "detailed", "-1.5", 
							request.getParameter("start"), request.getParameter("length"));
			
			session.setAttribute("drug_associated_genes", atlasExpressionClass.associated_genes); 
			
			JSONArray atlasexpressionsJSON = new JSONArray(); 
			for (AtlasGeneInterface atlasExpression : atlasExpressions) {
				JSONArray atlasexpressionRow = new JSONArray(); 
				atlasexpressionRow.put(atlasExpression.geneName);
				atlasexpressionRow.put(atlasExpression.exparimentalCondition);
				atlasexpressionRow.put(df2.format(atlasExpression.expressionLFC));
				atlasexpressionRow.put(df2.format(atlasExpression.pValue));
				atlasexpressionsJSON.put(atlasexpressionRow); 
			}
			
			JSONObject atlasExpressionJSONServerSide = new JSONObject(); 
			atlasExpressionJSONServerSide.put("draw", request.getParameter("draw"));
			atlasExpressionJSONServerSide.put("recordsTotal", atlasExpressionClass.total_rows); 
			atlasExpressionJSONServerSide.put("recordsFiltered", atlasExpressionClass.filtered_rows); 
			atlasExpressionJSONServerSide.put("data", atlasexpressionsJSON); 
			
			response.setContentType("application/json");
			PrintWriter writer = response.getWriter(); 
			//writer.write("{ \"data\" : " + atlasexpressionsJSON.toString() + "}");
			writer.write(atlasExpressionJSONServerSide.toString());
			writer.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
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
