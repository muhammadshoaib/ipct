<%@page import="kr.seoul.amc.lggm.gccm.core.AtlasExpression"%>
<%@page import="kr.seoul.amc.lggm.gccm.core.AtlasGeneInterface"%>
<%@page import="java.util.List"%>
<%@page import="kr.seoul.amc.lggm.gccm.core.DriverManager"%>
<%@page import="java.sql.Connection"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%
//Connection conn = DriverManager.getConnection(); 
String sessionid = request.getSession().getId(); 
String cellLinesString = request.getParameter("cellLines");
//List<AtlasGeneInterface> atlasExpressions = new AtlasExpression(conn).GetAtlasTableByDrugNames(drugString, "detailed", "-1.5");
//conn.close(); 
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="../../favicon.ico">


<!-- Bootstrap core CSS -->
<link href="css/bootstrap.min.css" rel="stylesheet">

<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
<link href="assets/css/ie10-viewport-bug-workaround.css"
	rel="stylesheet">

<!-- Custom styles for this template -->
<link href="css/dashboard.css" rel="stylesheet">

<link href="css/bootstrap-tokenfield.css" rel="stylesheet" type="text/css"/>
<link href="css/tokenfield-typeahead.css" rel="stylesheet" type="text/css"/>

<link href="jqueryui/jquery-ui.css" rel="stylesheet" type="text/css"/>

<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
<!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
<script src="assets/js/ie-emulation-modes-warning.js"></script>




<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

<link type="text/css"
	href="https://cdn.datatables.net/1.10.13/css/jquery.dataTables.min.css"
	rel="stylesheet"></link>


<title>CTRP Edge | Gene Abolusion Summery in CBio Portal</title>
</head>
<body>

	<nav class="navbar navbar-inverse navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed"
					data-toggle="collapse" data-target="#navbar" aria-expanded="false"
					aria-controls="navbar">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#">CTRP Edge </a>
			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav navbar-right">
				<li><a href="/">Main Page</a></li>
				</ul>
				</ul>
				
				<div class="navbar-form navbar-right">
					<input id="genesList" type="text" class="form-control" style="width:400px;" value="<%= cellLinesString %> ">
					<button id="drug_data_update_button" class="form-control">Create Graph</button>
				</div>
			</div>
		</div>
	</nav>

	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-9 col-sm-offset-2 col-md-10 col-md-offset-1 main">
			<h3>Gene Aberration Summary in cBioPortal</h3>
				<section id="main-table">
				
					<table id="example" class="display">
						<thead>
							<tr>
								<th>Gene Name</th>
								<th>Mutations</th>
								<th>CNV Alterations</th>
								<th>Up Regulation</th>
								<th>Down Regulation</th>
								<th>Common Cell Lines</th>
							</tr>
						</thead>

					</table>
				</section>


			</div>
		</div>
	</div>

	<footer class="navbar-fixed-bottom footer">
		<div class="container">
			<p class="text-muted">Copyright &copy 2017 Shoaib et.al. </p>
		</div>
	</footer>

	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
	<script>window.jQuery || document.write('<script src="assets/js/vendor/jquery.min.js"><\/script>')</script>
	<script type="text/javascript" src="jqueryui/jquery-ui.js"></script>
	<script src="js/bootstrap.min.js"></script>
	<!-- Just to make our placeholder images work. Don't actually copy the next line! -->
	<script src="assets/js/vendor/holder.min.js"></script>
	<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
	<script src="assets/js/ie10-viewport-bug-workaround.js"></script>

	<script type="text/javascript"
		src="https://cdn.datatables.net/1.10.13/js/jquery.dataTables.min.js"></script>
		
	<script type="text/javascript" src="js/bootstrap-tokenfield.js"></script>
	
	<script type="text/javascript" src="jqueryui/jquery-ui.js"></script>

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

	function loadAtlaxDataToTable(drungstring) {
 		$("#example").dataTable().fnDestroy();
 		var table = $('#example').DataTable({
        	"pageLength" : 100, 
        	"scrollY":        (screen.height / 2),
        	"lengthMenu": [ 100, 250, 500, 750, 1000 ], 
        	//"processing": true,
            //"serverSide": true,
        	"ajax" : "GetCBIOSummeryTable?jsessionid=<%=sessionid%>&cellLines=" + drungstring,
        	"deferRender": true, 
        	"columnDefs": [ {
                "targets": [ 1, 2, 3, 4 ],
                "render": function ( data, type, full, meta ) {
                return +data+'%';
                }
            } ],
        	
        	"order" : [[2, "dsc"]], 
        	"initComplete": function( settings, json ) {
        	  }
        	});
    		
		}
	loadAtlaxDataToTable("<%= cellLinesString %> ");
	

	$("#drug_data_update_button").click(function() { 
		loadAtlaxDataToTable($("#genesList").val()); 
	});

	$.fn.tokenify = function() {
		//alert($('input[name=gene_molecule_op]:checked').val()); 
		this
		.on('tokenfield:createtoken', function (e) {
		    e.attrs.value = e.attrs.value.toUpperCase(); 
		    e.attrs.label = e.attrs.label.toUpperCase(); 
		}).tokenfield({
			'delimiter': [';'],
			autocomplete: {
				minLength: 1,
			    //source: ['Green', 'Red', 'Blue', 'Black', 'Orange', 'Yello', 'Brown'],
			    source: "GetGeneListJson?listType=cellLine",
			    delay: 150
			  },
			  
			  showAutocompleteOnFocus: true,
		}); 
	}  

	$("#genesList").tokenify();  
	$( "#drug_data_update_button" ).button();
	

	
} );
</script>


</body>
</html>