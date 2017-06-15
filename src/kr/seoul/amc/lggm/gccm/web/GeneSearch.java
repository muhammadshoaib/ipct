package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.seoul.amc.lggm.gccm.core.CellLineSearchFactory;
import kr.seoul.amc.lggm.gccm.core.DriverManager;
import kr.seoul.amc.lggm.gccm.core.DrugSearchFactory;
import kr.seoul.amc.lggm.gccm.core.GeneSearchFactory;

@WebServlet({"/GeneSearch"})
public class GeneSearch
extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public GeneSearch() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		PrintWriter writer = response.getWriter();
		String inputString = request.getParameter("genesList");
		String dbsString = request.getParameter("databases");
		String output = request.getParameter("output");
		String graphType = request.getParameter("graphType");
		String inputType = request.getParameter("inputType");
		String drugSensitivity = request.getParameter("sensitivity");
		String mutationFrequency = request.getParameter("mutationFreq"); 
		String pathwayType = request.getParameter("pathwayType"); 
		String genesFilter = request.getParameter("genesFilter"); 
		System.out.println(drugSensitivity);
		System.out.println(mutationFrequency);
		System.out.println(pathwayType);
		System.out.println(genesFilter);
		
		Map<String, String[]> perms = request.getParameterMap();

		String fileName = inputString.replace(";", "-");


		String[] databases = dbsString.split(";");
		if (inputString == "") {
			writer.write("NO INPUT GIVEN");
			return;
		}

		if (perms.containsKey("graphOption")) {
			if (((String[])perms.get("graphOption"))[0].equals("json")) {
				response.setContentType("application/json");
				fileName = fileName + ".json";
			}
			else if (((String[])perms.get("graphOption"))[0].equals("csv")) {
				response.setContentType("text/plain");
				fileName = fileName + ".csv";
			}
			else {
				fileName = fileName + ".graphml";
			}
		}
		else {
			fileName = fileName + ".graphml";
		}


		if (!output.equals("ViewGraph")) {
			response.setContentType("application/force-download");

			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		}

		ServletContext context = getServletContext();

		Connection conn = DriverManager.getConnection(context);

		try
		{
			if (inputType.equals("molecule")) {
				DrugSearchFactory dsf = new DrugSearchFactory(context, conn);
				writer.println(dsf.GetDrugGraph(inputString, graphType, dbsString, drugSensitivity, 
						Double.parseDouble(mutationFrequency), 
						((String[])perms.get("graphOption"))[0], pathwayType, genesFilter));
			}
			else if (inputType.equals("cellLine")) {
				CellLineSearchFactory clsf = new CellLineSearchFactory(context, conn);
				writer.println(clsf.GetCellLineGraph(inputString, graphType, dbsString, 
						drugSensitivity, ((String[])perms.get("graphOption"))[0], pathwayType, genesFilter));
			}
			else if (inputType.equals("gene")) {
				GeneSearchFactory gsf = new GeneSearchFactory(context, conn);
				writer.println(gsf.GetGeneGraphML(inputType, inputString, graphType, drugSensitivity, perms));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();

			try
			{
				conn.close();
			}
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("HELLO WORLD");
		writer.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{}
}