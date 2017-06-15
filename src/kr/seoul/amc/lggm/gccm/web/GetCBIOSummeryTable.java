package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import kr.seoul.amc.lggm.gccm.core.CBioDataSummery;
import kr.seoul.amc.lggm.gccm.core.CBioGeneInterface;
import kr.seoul.amc.lggm.gccm.core.CellLineSearchFactory;
import kr.seoul.amc.lggm.gccm.core.DriverManager;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class GetAtlasTable
 */
@WebServlet("/GetCBIOSummeryTable")

public class GetCBIOSummeryTable extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetCBIOSummeryTable() {
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
		HttpSession session = request.getSession() ; 		
		ServletContext context = getServletContext(); 
		
		Connection conn = null; 
		try {
			
			conn = DriverManager.getConnection(context); 
			
			String cellLinesString = request.getParameter("cellLines");
			DecimalFormat df2 = new DecimalFormat("0.00");
			CBioDataSummery cbioSummeryClass = new CBioDataSummery(conn);
			
			if (session.getAttribute("cellLineString") == null ) {
				System.out.println("Adding New Session");
				session.setAttribute("cellLineString",cellLinesString);
				cbioSummeryClass = new CBioDataSummery(conn);
				
			} else if (session.getAttribute("cellLineString").equals(cellLinesString) == false) {
				System.out.println("Adding New Session");
				session.setAttribute("cellLineString", cellLinesString);
				cbioSummeryClass = new CBioDataSummery(conn);
			}
			else {
				System.out.println("Drug Name Already Exists");
				System.out.println(session.getAttribute("drugString"));
				cbioSummeryClass = new CBioDataSummery(conn, 
						(Set<String>) session.getAttribute("drug_associated_genes"));
			}
			
			System.out.println(session.getAttribute("drugString"));
			
			ArrayList<String> cellLineIDs = new CellLineSearchFactory(context, conn).GetCellLinesIDs(cellLinesString); 
			
			
			List<CBioGeneInterface> cbioSummeryObjects = 
					cbioSummeryClass.GetGeneCBIOTable(cellLineIDs);
			
			session.setAttribute("drug_associated_genes", cbioSummeryClass.associated_genes); 
			
			JSONArray cbioSummeryJSON = new JSONArray(); 
			for (CBioGeneInterface cbioSummeryObject : cbioSummeryObjects) {
				JSONArray cbioSummeryRow = new JSONArray(); 
				cbioSummeryRow.put(cbioSummeryObject.geneName);
				cbioSummeryRow.put(df2.format(cbioSummeryObject.mutations * 100));
				cbioSummeryRow.put(df2.format(cbioSummeryObject.alterations * 100));
				cbioSummeryRow.put(df2.format(cbioSummeryObject.upRegulated * 100));
				cbioSummeryRow.put(df2.format(cbioSummeryObject.downRegulated * 100));
				cbioSummeryRow.put(cbioSummeryObject.noOfCellLines);
				
				cbioSummeryJSON.put(cbioSummeryRow); 
			}
			
			JSONObject cbioSummeryJSONServerSide = new JSONObject(); 
			cbioSummeryJSONServerSide.put("draw", request.getParameter("draw"));
			cbioSummeryJSONServerSide.put("recordsTotal", cbioSummeryClass.total_rows); 
			cbioSummeryJSONServerSide.put("recordsFiltered", cbioSummeryClass.filtered_rows); 
			cbioSummeryJSONServerSide.put("data", cbioSummeryJSON); 
			
			response.setContentType("application/json");
			PrintWriter writer = response.getWriter(); 
			//writer.write("{ \"data\" : " + atlasexpressionsJSON.toString() + "}");
			writer.write(cbioSummeryJSONServerSide.toString());
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
