package kr.seoul.amc.lggm.gccm.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.seoul.amc.lggm.gccm.core.UtilitityFactory;

@WebServlet({"/GetOrgans"})
public class GetOrgans
  extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    PrintWriter writer = response.getWriter();
    String output = "";
    output = "<option value=\"allOrgans\">All Organs</option>";
    
    ServletContext context = getServletContext();
    UtilitityFactory usf = new UtilitityFactory(context);
    try
    {
      ArrayList<String> list = usf.ReturnOrgansList();
      for (int i = 0; i < list.size(); i++) {
        output = output + "<option value=\"" + (String)list.get(i) + "\">" + ((String)list.get(i)).toUpperCase() + "</option>\r\n";
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    writer.println(output);
  }
}
