package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class downloadnetwork
 */
@WebServlet("/downloadnetwork")
public class downloadnetwork extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public downloadnetwork() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("GETTING"); 
		response.setContentType("application/force-download");
		//response.setContentType("application/pdf");
		response.setHeader("Content-Disposition","attachment; filename=network.pdf");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("POSTING"); 
		response.setContentType("application/force-download");
		//response.setContentType("application/pdf");
		response.setHeader("Content-Disposition","attachment; filename=network.pdf");
	}

}
