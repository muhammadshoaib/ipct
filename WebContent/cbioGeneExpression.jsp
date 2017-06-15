<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.Connection"%>
<%@page import="kr.seoul.amc.lggm.gccm.core.*"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
String geneId = request.getParameter("geneID"); 
if (geneId == null) {
	geneId = "1499"; 
}
ServletContext context = getServletContext(); 
Connection conn = DriverManager.getConnection(context); 
GeneInterface geneInterface = new GeneInterface(conn); 
ArrayList<String[]> geneCbioExpression = geneInterface.GetGenesCbioExpression(geneId); 

%>

 <div class="modal-header">
<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
     <h4 class="modal-title"><%=geneCbioExpression.get(0)[0] %>expression in cBioPortal</h4>
</div>
<div class="modal-body supInformationModel-content"> 
	            
<table class="table table-bordered">
	<tr>
		<th>Gene Symbol</th>
		<th>Cancer Type</th>
		<th>Cancer Study</th>
		<th>Measurement Type</th>
		<th>Up Regulated</th>
		<th>Down Regulated</th>
	</tr>
	
	<% for (String[] geneData : geneCbioExpression) { %>
	<tr>
		<td><%= geneData[0] %></td>
		<td><%= geneData[1] %></td>
		<td><%= geneData[2] %></td>
		<td><%= geneData[3] %></td>
		<td><%= geneData[4] %></td>
		<td><%= geneData[5] %></td>
	</tr>
	<% } %>
</table>
</div>
</div>
<% conn.close(); %>