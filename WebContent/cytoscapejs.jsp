<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@page import="com.sun.research.ws.wadl.Include"%>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>cMapper Gene-centric connectivity mapper of EBI RDF resource</title>

    <!-- Bootstrap Core CSS -->
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
	<link href="visdist/vis-network.min.css"  rel="stylesheet" type="text/css" />
	
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    
	   <style>
	
	.links line {
	  stroke: #999;
	  stroke-opacity: 0.6;
	}
	
	.nodes circle {
	  stroke: #fff;
	  stroke-width: 1.5px;
	}
	
	</style>

</head>

<body id="page-top" class="index">
	<div id="cover"></div>
    <!-- Navigation -->
    <nav id="navbar-homepage" class="navbar navbar-default navbar-fixed-top">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header page-scroll">
                <a class="navbar-brand page-scroll" href="http://cmapperbeta.ewostech.net">c-Mapper</a>
            </div>
			<div class="navbar-text" id="input-text-static" style="display:none"> 
			<span class="lead" id="geneslist-text"></span>
			<a  data-toggle="collapse" 
                		 href="#navbar-network" aria-expanded="false" aria-controls="navbar-network">
   							<i class="fa fa-edit fa-2x"></i>
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
                        <a id="contactUS" href="#contact">Contact</a>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container-fluid -->
    </nav>
	
	<section id="navbar-network" class="collapse fade">
        <div class="container">
   
              <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            	<div id="nav-input"></div>	
                <div id="nav-filters">
                <!--  
				
				 -->
				<button type="button" id="btnSave" value="Save PNG" class="btn btn-default input-xl">
					<span class="glyphicon glyphicon-save" aria-hidden="true"></span>
				</button>
				
				</div>
				
				
				
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container-fluid -->
    </section>
    <!-- Header -->
    <header>
    <div id="inputForm1">
        <div class="container" >
       
            <div class="intro-text" id="header-intra-text">
				<%@include file="searchForm.jsp" %>
				
            </div>
        </div>
        </div>
    </header>
    <section id="nodedetails"  class="modal"  role="dialog">
    	<div class="modal-dialog" id="nodedatamodel">
    
      <!-- Modal content-->
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">&times;</button>
          <h4>Node's Information</h4>
        </div>
        <div class="modal-body" style="padding:40px 50px;" id="cytoscapenote">
			Hello World
        </div>
      </div>
      
    </div>
    </section>
    
    <section id="aboutcMapperModel"  class="modal "  role="dialog">
    	<div class="modal-dialog informationModel">
    
      <!-- Modal content-->
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">&times;</button>
          <h4>About cMapper</h4>
        </div>
        <div class="modal-body" style="padding:40px 50px;" id="cytoscapenote">
			<p class="lead">cMapper allows biologists to interrogate data points 
			in the EBI-RDF platform that are connected to genes or 
			small molecules of interest in multiple biological contexts. 
			Input to cMapper consists of a set of genes or small 
			molecules, and the output is the data points in six
			 independent EBI-RDF databases connected with the given
			  genes or small molecules in the user's query. cMapper pro-vides 
			  output to users in the form of a graph in which nodes 
			  represent data points and the edges represent connections 
			  between data points and inputted sets of genes or small 
			  molecules. Users can also apply filters based on database, taxonomy, organ and pathways 
			in order to focus on a core connectivity graph of their interests. </p>
        </div>
      </div>
      
    </div>
    </section>
    
    
    <section id="teamMemebersModel"  class="modal "  role="dialog">
    	<div class="modal-dialog informationModel">
    
      <!-- Modal content-->
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">&times;</button>
          <h4>Team</h4>
        </div>
        <div class="modal-body" style="padding:40px 50px;" id="cytoscapenote">
			<h2>Shoaib, Muhammad <small>PhD Student</small></h2>
			<h2>Ansar, Adnan Ahmad <small>PhD Student</small></h2>
			<h2>Ahn, Sung-min <small>Professor</small></h2>
        </div>
      </div>
      
    </div>
    </section>
    
     <section id="contactUSModel"  class="modal "  role="dialog">
    	<div class="modal-dialog informationModel">
    
      <!-- Modal content-->
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">&times;</button>
          <h4>Contact Us</h4>
        </div>
        <div class="modal-body" style="padding:40px 50px;" id="cytoscapenote">
			<p class="lead">Any query related to development should be directed to muhemmed.shoaib@gmail.com</p>
        </div>
      </div>
      
    </div>
    </section>
    
    
	<div id="cygraph"><img src="files/load.gif"/></div>
	<div id="cytoscapeweb"></div>
	
	<div id="database-filter-popover">
    <a href="#" id="databases-lagent">
 
	<i class="fa fa-database fa-5x"></i>
	 
	</a>
	</div>
	 
	<div id="lagents-filter-panel"style="display: none" class="popover">
		<div style="background: #31B5D6" class="lagentsItem">Candidate Genes</div>
		<div style="background: #C6EFF7" class="lagentsItem">Associated Genes</div>
		<div style="background: #D63194" class="lagentsItem">Uniprot</div>
		<div style="background: #FF0000" class="lagentsItem">Up Expressions</div>
		<div style="background: #7BC618" class="lagentsItem">Down Expressions</div>
		<div style="background: #BFBFBF" class="lagentsItem">Up-Down Expressions</div>
		<div style="background: #FF9C4A" class="lagentsItem">Reactome Pathways</div>
		<div style="background: #FFFF6B" class="lagentsItem">Small Molecules</div>
		<div style="background: #847308" class="lagentsItem">Bio Models</div>
		<div style="background: #082984; color: #FFF" class="lagentsItem">Bio Samples</div>
	</div>
	
    <footer class="navbar-fixed-bottom">
        <div class="container">
            <div class="row">
                <div class="col-md-4">
                    <span class="copyright">Copyright &copy; Shoaib et.al. 2016</span>
                </div>
                <div class="col-md-4">
                <!--  
                    <ul class="list-inline social-buttons">
                        <li><a href="#"><i class="fa fa-twitter"></i></a>
                        </li>
                        <li><a href="#"><i class="fa fa-facebook"></i></a>
                        </li>
                        <li><a href="#"><i class="fa fa-linkedin"></i></a>
                        </li>
                    </ul>
                 -->
                </div>
                <div class="col-md-4">
                    <ul class="list-inline quicklinks">
                        <li>University of Ulsan College of Medicine</li>
                        <li><a href="mailto:muhemmed.shoaib@gmail.com">Write Us</a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </footer>
   
	<script id="searching-input-form" type="text/template">
  		
				

	</script>
	
	<!-- 
	<div id="pathway-filter-panel" class="popover">
		<div class="filter-popover">
		<input type="radio" name="pathWayOption" value="allPathways"
								checked="checked" />All Pathways <br /> <br />
								<input
								type="radio" name="pathWayOption" value="metabolicPathways" />Metabolic
	Pathways <br /> <br /> <input type="radio" name="pathWayOption"
		value="signalingPathways" />Signaling Pathways <br /> <br />
		</div>
	</div>
	-->
	
	<script type="text/javascript" src="visdist/vis.js"></script>
    <!-- jQuery -->
    <script src="js/jquery.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="js/bootstrap.min.js"></script>
	<script type="text/javascript" src="js/bootstrap-multiselect.js"></script>

    <!-- Plugin JavaScript -->
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.3/jquery.easing.min.js"></script>
    <script src="js/classie.js"></script>
    <script src="js/cbpAnimatedHeader.js"></script>
    
    
    <script type="text/javascript" src="cytoscapeweb/js/min/json2.min.js"></script>
	<script type="text/javascript" src="cytoscapeweb/js/min/AC_OETags.min.js"></script>
	<script type="text/javascript" src="cytoscapeweb/js/min/cytoscapeweb.min.js"></script>
	<script type="text/javascript" src="jqueryui/jquery-ui.js"></script>
	<script src="files/jquery.tagsinput.min.js"></script>

	<script type="text/javascript" src="js/bootstrap-tokenfield.js"></script>
	
			<script type="text/javascript"  src="js/html2canvas.js"></script>
		<script type="text/javascript"  src="js/canvas2image.js"></script>
		<script type="text/javascript"  src="js/base64.js"></script>
		
		<script type="text/javascript" src="js/bootstrap-select.js">
		</script>

    <!-- Contact Form JavaScript -->
    <script type="text/javascript" src="js/jqBootstrapValidation.js"></script>
    <script type="text/javascript" src="js/contact_me.js"></script>
    
    
    <!--  Cytoscape JS Libraryies Starting  -->
    
<script
	src="http://cytoscape.github.io/cytoscape.js/api/cytoscape.js-latest/cytoscape.min.js"></script>

<!-- for testing with local version of cytoscape.js -->
<!--<script src="../cytoscape.js/build/cytoscape.js"></script>-->

<script
	src="https://cdn.rawgit.com/cpettitt/dagre/v0.7.4/dist/dagre.min.js"></script>
<script
	src="https://cdn.rawgit.com/cytoscape/cytoscape.js-dagre/1.1.2/cytoscape-dagre.js"></script>
<script
	src="https://cdn.rawgit.com/cytoscape/cytoscape.js-spread/1.0.9/cytoscape-spread.js"></script>
	    <script src="https://cdn.rawgit.com/cytoscape/cytoscape.js-cose-bilkent/1.0.5/cytoscape-cose-bilkent.js"></script>
	
<script src="./cytoscapejs/graph_style_json.js"></script>
<script src="./cytoscapejs/html2canvas.js"></script>
<script src="./cytoscapejs/canvas2image.js"></script>
<script src="./cytoscapejs/base64.js"></script>

<script src="./cytoscapejs/arbor.js"></script>
<script src="cytoscapejs/cytoscape-arbor.js"></script>
<script type="text/javascript" src="cytoscapejs/cytoscape-cose-bilkent.js">
</script>

<script src="https://cdn.rawgit.com/dhotson/springy/2.7.1/springy.js"></script>
		<script src="./cytoscapejs/cytoscape-springy.js"></script>
    
    
    <!--  Cytoscape JS Libraries Ending -->

    <!-- Custom Theme JavaScript -->
    
    
    <script type="text/javascript" src="js/agency.js"></script>
    
    <script src="https://d3js.org/d3.v4.js"></script>
    
	<script type="text/javascript" src="js/cmapper.cytoscapejs.js">
	</script>
	

</body>

</html>
