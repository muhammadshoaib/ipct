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


<title>CTRP Drugs Atlas Gene Expression</title>
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
				<a class="navbar-brand" href="#">CTRP Portal</a>
			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav navbar-right">
					<li><a href="printAtlasDashboard.jsp?drugString=<%= drugString %>">Atlas Expression</a></li>
					<li><a href="printGraphDashboard.jsp?drugString=<%= drugString %>"">Bubble Graph</a></li>
					<li><a href="/">Main Page</a></li>
				</ul>
				
				<div class="navbar-form navbar-right">
					<input id="genesList" type="text" class="form-control" style="width:400px;" value="<%= drugString %> ">
					<button id="drug_data_update_button" class="form-control">Create Graph</button>
				</div>
			</div>
		</div>
	</nav>

	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-9 col-sm-offset-2 col-md-10 col-md-offset-1 main">
				<h1 class="page-header">Bubble Graph of Associated Genes</h1>


				<section id="main-table">
						<div style="width: 800px; height: 600px; margin: 0 auto;">
				         <svg width="800" height="600" font-family="sans-serif" font-size="10" text-anchor="middle"></svg>
				         </div>
				</section>


			</div>
		</div>
	</div>

	<footer class="navbar navbar-inverse navbar-fixed-bottom ">
	<div class="footer">
		<div class="container">
			<p class="text-muted">Place sticky footer content here.</p>
		</div>
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

		
	<script type="text/javascript" src="js/bootstrap-tokenfield.js"></script>
	
	<script src="https://d3js.org/d3.v4.min.js"></script>

	<script type="text/javascript">
$(document).ready(function() {


	function loadMutationGenesToGraph(drungstring) {

		$( "svg" ).empty();
		
		var svg = d3.select("svg"),
	    width = +svg.attr("width"),
	    height = +svg.attr("height");
	
			var format = d3.format(",d");
			
			var color = d3.scaleOrdinal(d3.schemeCategory20);
			
			var pack = d3.pack()
			    .size([width, height])
			    .padding(1.5);

			d3.csv("GetAssocatedGenesFreq?drugString=" + drungstring, function(data) {
				  data.forEach(function(d) {
					  d.value = +d.value;
					  if (d.value) return d;
				  });
				  console.log(data[0]);
			});
			
			d3.csv("GetAssocatedGenesFreq?drugString=" + drungstring, function(d) {
			  d.value = +d.value;
			  if (d.value) return d;
			}, function(error, classes) {
			  if (error) throw error;
				
			  var root = d3.hierarchy({children: classes})
			      .sum(function(d) { return d.value; })
			      .each(function(d) {
			        if (id = d.data.id) {
			          var id, i = id.lastIndexOf(".");
			          d.id = id;
			          d.package = id.slice(0, i);
			          d.class = id.slice(i + 1);
			        }
			      });
			  
			  var node = svg.selectAll(".node")
			    .data(pack(root).leaves())
			    .enter().append("g")
			      .attr("class", "node")
			      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
			
			  node.append("circle")
			      .attr("id", function(d) { return d.id; })
			      .attr("r", function(d) { return d.r; })
			      .style("fill", function(d) { return color(Math.round(d.id)); });
			
			  node.append("clipPath")
			      .attr("id", function(d) { return "clip-" + d.id; })
			    .append("use")
			      .attr("xlink:href", function(d) { return "#" + d.id; });
			
			  node.append("text")
			      .attr("clip-path", function(d) { return "url(#clip-" + d.id + ")"; })
			    .selectAll("tspan")
			    .data(function(d) { return d.id.split(/(?=[A-Z][^A-Z])/g); })
			    .enter().append("tspan")
			      .attr("x", 0)
			      .attr("y", function(d, i, nodes) { return 13 + (i - nodes.length / 2 - 0.5) * 10; })
			      .text(function(d) { return d; });
			
			  node.append("title")
			      .text(function(d) { return d.id + "\n" + format(d.value); });
			});
    		
		}
	loadMutationGenesToGraph("<%= drugString %> ");
	

	$("#drug_data_update_button").click(function() { 
		loadMutationGenesToGraph($("#genesList").val()); 
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
			    source: "GetGeneListJson?listType=molecule",
			    delay: 150
			  },
			  
			  showAutocompleteOnFocus: true,
		}); 
	}  

	$("#genesList").tokenify();  
	

	
} );
</script>


</body>
</html>