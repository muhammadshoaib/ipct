<%@page import="kr.seoul.amc.lggm.gccm.core.AtlasExpression"%>
<%@page import="kr.seoul.amc.lggm.gccm.core.AtlasGeneInterface"%>
<%@page import="java.util.List"%>
<%@page import="kr.seoul.amc.lggm.gccm.core.DriverManager"%>
<%@page import="java.sql.Connection"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%
//Connection conn = DriverManager.getConnection(); 
String drugString = request.getParameter("drugString");
//List<AtlasGeneInterface> atlasExpressions = new AtlasExpression(conn).GetAtlasTableByDrugNames(drugString, "detailed", "-1.5");
//conn.close(); 
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">


    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="css/agency.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Montserrat:400,700" rel="stylesheet" type="text/css">
    <link href='https://fonts.googleapis.com/css?family=Kaushan+Script' rel='stylesheet' type='text/css'>
    <link href='https://fonts.googleapis.com/css?family=Droid+Serif:400,700,400italic,700italic' rel='stylesheet' type='text/css'>
    <link href='https://fonts.googleapis.com/css?family=Roboto+Slab:400,100,300,700' rel='stylesheet' type='text/css'>
    
	<link href="jqueryui/jquery-ui.css" rel="stylesheet" type="text/css"/>

	<link rel="stylesheet" href="css/bootstrap-multiselect.css" type="text/css">
	<link rel="stylesheet" type="text/css" href="files/jquery.tagsinput.min.css" />
	
	<link href="css/bootstrap-tokenfield.css" rel="stylesheet" type="text/css"/>
	<link href="css/tokenfield-typeahead.css" rel="stylesheet" type="text/css"/>
	
	<link href="css/bootstrap-select.css" rel="stylesheet" type="text/css"/>

<link type="text/css" href="https://cdn.datatables.net/1.10.13/css/jquery.dataTables.min.css" rel="stylesheet"></link>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.13/js/jquery.dataTables.min.js"></script>

<script type="text/javascript">
$(document).ready(function() {

	jQuery.fn.dataTableExt.oSort["logFoldChange-desc"] = function(x, y) {
		return x < y;
	};

	jQuery.fn.dataTableExt.oSort["logFoldChange-asc"] = function(x, y) {
	  return x > y;
	}

	jQuery.fn.dataTableExt.oSort["logFoldChange-pre"] = function(num) {
		return Math.abs(num)
	}
	
    $('#example').DataTable({
        	"pageLength" : 100, 
        	"lengthMenu": [ 100, 250, 500, 750, 1000 ], 

        	"ajax" : "GetAtlasTable?drugString=<%= drugString %>",
        	"deferRender": true, 
        	"rowCallback": function(row, data, index){
        	    if(data[2] < -1){
        	        $(row).find('td').css('color', 'blue');
        	    }
        	    else if(data[2] > 1){
        	        $(row).find('td').css('color', 'red');
        	    }
        	}, 
        	"columnDefs": [ {
        	    "targets": 2,
        	    "data" : "Log Fold Change",
        	    "render": function (data, type, full, meta){
            	    return Math.round(data)
        	    }
        	  } ], 
        	"aoColumnDefs": [{
        	    "sType": "logFoldChange",
        	    "bSortable": true,
        	    "aTargets": [2]
        	  }], 
        	"order" : [[2, "dsc"]]
        	});
} );
</script>
<title>Insert title here</title>
</head>
<body>

<nav id="navbar-homepage" class="navbar navbar-default navbar-fixed-top">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header page-scroll">
                <a class="navbar-brand page-scroll" href="http://cmapper.ewostech.net">CTRP Edge</a>
            </div>
			<div class="navbar-text" id="input-text-static" style="display:none"> 
			<span class="lead" id="geneslist-text"><%= drugString %></span>
			<a  data-toggle="collapse" 
                		 href="#navbar-network" aria-expanded="false" aria-controls="navbar-network">
   							<i class="fa fa-edit fa-2x"></i>
 					 </a>&nbsp; &nbsp; 
 			<a id="btnpdf" href="#">
					<i class="fa fa-file-pdf-o fa-2x" aria-hidden="true"></i>
				</a>&nbsp; &nbsp; 
 			<a id="btnSave" href="#">
					<i class="fa fa-download fa-2x" aria-hidden="true"></i>
				</a>		 
			</div>
			
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav navbar-right">

                    <li>
                        <a  id="aboutcMapper" href="#">About c-Mapper</a>
                    </li>
					  <li>
                        <a id="teamModel" href="#contact">Team</a>
                    </li>
                    <li>
                    	<a href="docs/cMapperDocumentation.pdf" target="_blank">Docs</a>
                    <li>
                        <a id="contactUS" href="#contact">Contact</a>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container-fluid -->
    </nav>

<section id="main-table" style="padding-left: 50px; padding-right: 50px;">
<table id="example" class="display">
<thead>
            <tr>
                <th>Gene Name</th>
                <th>Exparimental Condition</th>
                <th>Log Fold Change</th>
                <th>pValue</th>
            </tr>
        </thead>
        
</table>
</section>
<footer class="navbar-fixed-bottom">
        <div class="container">
            <div class="row">
                <div class="col-md-4">
                    <span class="copyright">Copyright &copy; Shoaib et.al. 2016</span>
                </div>
                <div class="col-md-4">
                	<a href="cytoscapejs.jsp"  data-placement="top"
                	title="Please note that Cytoscape.JS version of cMapper is in the development stage yet. "
                	id="cytoscapeBetaVersion" target="_blank">cMapper JS Version (Beta)</a>
                	&nbsp; | &nbsp; 
                	<a href="https://github.com/muhammadshoaib/cmapper"  data-placement="top"
                	title="To get full database access please write us an email"
                	id="sourceCodeDownload" target="_blank">Source Code GitHub</a>
                <!--  
                    <ul class="list-inline social-buttons">
                        <li><a href="#"><i class="fa fa-twitter"></i></a>
                        </li>
                        <li><a href="#"><i class="fa fa-facebook"></i></a>
                        </li>
                        <li><a hre<a href="cytoscapejs.jsp"  data-placement="top"
                	title="Please note that Cytoscape.JS version of cMapper is in the development stage yet. "
                	id="cytoscapeBetaVersion" target="_blank">cMapper JS Version (Beta)</a>f="#"><i class="fa fa-linkedin"></i></a>
                        </li>
                    </ul>
                 -->
                </div>
                <div class="col-md-4">
                    <ul class="list-inline quicklinks">
                        <li>Gachon University Gil Medical Center</li>
                        
                        <li><a href="mailto:muhemmed.shoaib@gmail.com">Write Us</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </footer>

</body>


<script type="text/javascript" src="js/bootstrap-tokenfield.js"></script>
	
			<script type="text/javascript"  src="js/html2canvas.js"></script>
		<script type="text/javascript"  src="js/canvas2image.js"></script>
		<script type="text/javascript"  src="js/base64.js"></script>
		
		<script type="text/javascript" src="js/bootstrap-select.js">
		</script>

    <!-- Contact Form JavaScript -->
    <script src="js/jqBootstrapValidation.js"></script>
    <script src="js/contact_me.js"></script>

    <!-- Custom Theme JavaScript -->
    <script src="js/agency.js"></script>

</html>