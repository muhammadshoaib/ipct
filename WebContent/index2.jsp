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
	
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

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
			
			<button class="btn btn-primary" type="button" 
			data-toggle="collapse" data-target="#navbar-network" aria-expanded="false" aria-controls="navbar-network">
    		Button with data-target
  			</button>
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav navbar-right">
                    <li>
                        <a  href="#">About c-Mapper</a>
                    </li>
                    <li>
                        <a id="teamModel" href="#teamModel">Team</a>
                    </li>
                    <li>
                        <a id="teamModel1" href="#contact">Contact</a>
                    </li>
                </ul>
            </div>
            <!-- /.navbar-collapse -->
        </div>
        <!-- /.container-fluid -->
    </nav>
	
	<nav id="navbar-network" class="collapse navbar navbar-default navbar-fixed-top navbar-network">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header page-scroll">
                <a class="navbar-brand page-scroll" href="#page-top">c-Mapper</a>
				
            </div>
			
            <!-- Collect the nav links, forms, and other content for toggling -->
   
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
    </nav>
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
    <script src="js/jqBootstrapValidation.js"></script>
    <script src="js/contact_me.js"></script>

    <!-- Custom Theme JavaScript -->
    <script src="js/agency.js"></script>
	 <script src="js/cmapper.js"></script>
	
	

</body>

</html>
