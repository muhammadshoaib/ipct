var geneNames; 

$(function() {

	$.ajax({
		url: "GetOrgans",
		context: document.body,
		success: function(result) {
			$("#atlasOrgans").html(result);
			filteringOptionmultiselectProps['selectAllValue'] = 'allOrgans'

				$("#atlasOrgans").multiselect("rebuild", filteringOptionmultiselectProps)
				.addClass("overflow");
			 
			$("#atlasOrgans").multiselect('selectAll', false);
			$("#atlasOrgans").multiselect('updateButtonText');
			 
			//alert('hello world');

		}
	});
/*
	$.ajax({
		url: "getOrganismList",
		context: document.body,
		success: function(result) {
			$("#taxonomicOrganisims").html(result);
			filteringOptionmultiselectProps['selectAllValue'] = 'allOrganisims'

			$("#taxonomicOrganisims").multiselect('rebuild',  filteringOptionmultiselectProps)
				.addClass("overflow"); // .selectmenu(
			//$("#taxonomicOrganisims").multiselect('selectAll', false);
			//$("#taxonomicOrganisims").multiselect('updateButtonText');
			
			$('#taxonomicOrganisims option[value="NCBI_TaxID=9606"]').attr('selected', 'selected');
			$("#taxonomicOrganisims").multiselect('refresh');
		}
	});

*/	
	$.fn.tokenify = function() {
		//alert($('input[name=gene_molecule_op]:checked').val()); 
		this
		.on('tokenfield:createtoken', function (e) {
			
			if (e.attrs.value.indexOf(':') != -1) {
				e.attrs.value = e.attrs.value.substring(0, e.attrs.value.indexOf(':'));
				e.attrs.label = e.attrs.label.substring(0, e.attrs.label.indexOf(':'));
			}
			
		    e.attrs.value = e.attrs.value.toUpperCase(); 
		    e.attrs.label = e.attrs.label.toUpperCase(); 
		}).tokenfield({
			
			'delimiter': [';'],
			autocomplete: {
				minLength: 1,
			    //source: ['Green', 'Red', 'Blue', 'Black', 'Orange', 'Yello', 'Brown'],
			    source: "GetGeneListJson?listType=" + $('input[name=gene_molecule_op]:checked').val(),
			    delay: 250
			  },
			  
			  showAutocompleteOnFocus: true,
		}); 
	} 
	
	$( "#slider-range-max" ).slider({}); 
	
	$.fn.smalltokenify = function() {
		this.removeClass("input-xl");
		($('.tokenfield').removeClass('input-xl'))
		this.tokenfield('destroy');
		this.tokenify(); 
	} 
	
	$.fn.UpdatetokenifySource = function () {
		this.$input.autocomplete(('option', 'source', 
				"GetGeneListJson?listType=" + $('input[name=gene_molecule_op]:checked').val()))
	}
	
	$("#genesList").tokenify(); 
	
	$("#teamModel1").click( function(event) {
		//alert("HELLO WORLD"); 
		/*
		$('header').hide();
		$("#headerContainer").removeClass("container"); 
		$("#header-intro-text").removeClass("intro-text");
		$("#inputForm").wrap(" <div class='modal-dialog'><div class='modal-content'></div></div>"); 
		$("#inputsection").addClass("mymodel"); 
		$("#inputsection").attr("style", "display: block");
        $("#inputsection").attr("role", "dialog");
        $("#inputsection").addClass("modal fade"); 
        
        $('.modal-content').prepend('<div class="modal-header" style="padding:35px 50px;">' + 
        		'<button type="button" class="close" data-dismiss="modal">&times;</button>' + 
        		'<h4><span class="glyphicon glyphicon-lock"></span> Login</h4>' + 
        		'</div')
        
        $("#header").modal();
        */
		 $("#nodedetails").modal();
	}); 
	
	$("#teamModel").click( function(event) {
		 $("#teamMemebersModel").modal();
	});
	$("#aboutcMapper").click( function(event) {
		 $("#aboutcMapperModel").modal();
	});
	$("#contactUS").click( function(event) {
		 $("#contactUSModel").modal();
	});
	
	
	$(document).on('hidden.bs.modal', function (e) {
		
	    $(e.target).removeData('bs.modal');
	});
	
	$("#SupplementryInformationModel").on("shown.bs.modal",function(){
		
		   $(this).hide().show(); //hide first and then show here
		});
	
	
	
});
window.onload = function() {

	// id of Cytoscape Web container di
	
	//$("#navbar-network").hide();
	
	

	var div_id = "cygraph";

	// initialization options
	var options = {
		swfPath: "cytoscapeweb/swf/CytoscapeWeb",
		flashInstallerPath: "cytoscapeweb/swf/playerProductInstall"
	};

	var exportoptions = {

	}
	
	$("#cytoscapeBetaVersion").tooltip({ html: true});
	$("#sourceCodeDownload").tooltip({ html: true});
	var vis = new org.cytoscapeweb.Visualization(div_id, options);

	// callback when Cytoscape Web has finished drawing
	vis
		.ready(function() {

			//vis.Pdf();
			//vis.exportNetwork('pdf', "~/downloadnetwork");
			// add a listener for when nodes and edges are clicked
			vis.addListener("click", "nodes", function(event) {
				handle_click(event);
				return;
			});
			vis.addListener("dblclick", "nodes", function(event) {
				handle_dbl_click(event);
				return;
			});
			
			function loadSupplementryData() {
				alert("HELLO WORLD"); 
			}
			

			function handle_click(event) {
				$("#nodedetails").modal();
				var target = event.target;
				//alert(event.target);
				clear();
				//print("event.group = " + event.group);
				var keys = ["id", "Full Name",
					"Database Name", "URL", "Details", "Mutation Ratio",
					"Sensitivity"
				];
				var displaykeys = ["Identifier", "Full Name",
					"Database", "URL", "Details", "Mutation Ratio",
					"Sensitivity"
				];
				//alert(target.data + " " + keys.length);
				//alert(target.data["Database Name"] + "," + target.data["id"]);
				var htmlOutput = "''";
				for (i = 0; i < keys.length; i++) {
					var variable_name = keys[i];

					var variable_value = target.data[variable_name];
					//variable_value = variable_value.replace(";", "<br />"); 
					if (variable_name == "URL" &&
							variable_value != null) {
						variable_value = "<a href=\"" + variable_value + "\" target=_blank>" +
							variable_value + "</a>";
						
					}
					
					if (variable_value != null) {
						htmlOutput = "<div><div style='width:100px; float:left;'>" +
							displaykeys[i] +
							" : </div><div style='width:350px; float:left; font-size:18px'>" +
							variable_value.replace(/,/g, "<br />") + "</div><div style='clear:both'>" +
							"<br /></div></div>";
						print(htmlOutput);
						
						//alert(htmlOutput); 
					}
				}
				
				if (target.data["Database Name"] == "Gene") {
					___my_gene_id = target.data["id"].replace("GN", ""); 
					
					htmlOutput = "<div><div style='width:280px; float:left;'>" +
							"<a data-toggle='modal' " +
							"href='cbioGeneExpression.jsp?geneID="+___my_gene_id+"' " +
							"data-target='#SupplementryInformationModel'>" +
							"cBioPortal Expression Details</a></div>" +
							"<div style='clear:both'>" +
							"<br /></div>";
					print(htmlOutput);
					
					
					htmlOutput = "<div><div style='width:280px; float:left;'>" +
							"<a data-toggle='modal' " +
							"href='cbioMutations.jsp?geneID="+___my_gene_id+"' " +
							"data-target='#SupplementryInformationModel'>" +
							"cBioPortal Mutations Details</a></div>" +
							"<div style='clear:both'>" +
							"<br /></div>";
					print(htmlOutput);
					
					
	
				}
				
				event.stopPropagation();
			}
			
			
			/*
			$('#SupplementryInformationModel').on('show.bs.modal', function () {
				$('#SupplementryInformationModel-modal-content').css('height',$( window ).height()*0.7);
				$('#SupplementryInformationModel-modal-content').css('overflow-y', 'auto'); 
				});
			*/
			function handle_dbl_click(event) {

				var target = event.target;
				//alert(target.data["Database Name"] + "," + target.data["id"]);
				if (target.data["Database Name"] == 'associatedGenes' ||
					target.data["Database Name"] == 'gene') {
					//alert(target.data["Database Name"] + "," + target.data["id"]);
					if ($('input[name=gene_molecule_op]:checked').val() == "gene") {
						$("#genesList").addTag(target.data["id"]);
						return;
					}
					$("input[name=gene_molecule_op][value ='gene']")
						.prop('checked', true);
					$("#genesList").importTags(target.data["id"]);

				} else {

				}
				$('input[name=gene_molecule_op]:checked').button(
					"refresh");
				//alert($('input[name=gene_molecule_op]:checked').val());
				GetGeneList();

				//alert(target.data["Database Name"] + "," + target.data["id"]);

				event.stopPropagation();
			}

			function clear() {
				document.getElementById("cytoscapenote").innerHTML = "";
			}

			function print(msg) {
				document.getElementById("cytoscapenote").innerHTML += 
					msg ;
			}
		});


	function LinkHoverLinkSearch() {
		$('.listhyperlinks').click(function(event) {
			var geneNames = $('#genesList').val();
			//alert(geneNames);
			// alert(geneNames.indexOf(event.target.id));
			/*
			if(geneNames.indexOf(event.target.id) == -1) { 
				geneNames = geneNames + event.target.id + ";";
				$('#genesList').val(geneNames);
			} */
			if ($('#genesList').tagExist(event.target.id) == false) {
				$('#genesList').addTag(event.target.id);
			}

			//$('#genesList').

			//here you can also do all sort of things 
		});
	}

	function GetGeneList(triggeroption) {
		var val = $("#INDEX_A").val() + $("#INDEX_B").val() +
			$("#INDEX_C").val();
		//alert(val);
		$("#INPUT_LIST").html(val);

		var gene_moleculeop = $('input[name=gene_molecule_op]:checked')
			.val();

		var idata = "indexletter=" + val + "&listType=" + gene_moleculeop;
		//alert(idata);
		/*
		$.ajax({
			url: "GetList",
			data: idata,
			context: document.body,
			success: function(result) {
				//alert(result); 
				$("#INPUT_LIST").html(result);
				//alert('hello world');
				LinkHoverLinkSearch();

			}
		});
		*/ 

	}

	var xml = null;

	$('#INDEX_A').change(function(event, ui) {
		GetGeneList();
	});
	$('#INDEX_B').change(function() {
		GetGeneList();
	});
	$('#INDEX_C').change(function() {
		GetGeneList();
	});

	$("#geneReDrawButton").click(function() {
		if (xml != null) {

			var draw_options = {
				// your data goes here
				network: xml,
				layout: "ForceDirected",
				edgeLabelsVisible: true,
				visualStyle: visual_style,
				panToCenter: true,
				panZoomControlPosition: 'bottomLeft',
				// hide pan zoom
				// panZoomControlVisible: false 
			};
			vis.draw(draw_options);
		}

	});

	$("#geneSubmitButton")
		.click(
			function(event) {
				
				
				
				var outputType = $("#outputOption").val();
				//alert($("header").is(":visible") && (outputType == "ViewGraph"));
				
				if($("header").is(":visible") && (outputType == "ViewGraph") ) {
					//alert("First time loaded")
					$('header').hide()
					//$('nav#navbar-homepage').hide();
					
					$($('#filters').contents()).prependTo('#nav-filters')
					$($('#main-input').contents()).prependTo('#nav-input')
					
					$($('#geneSubmitButton-container').contents()).appendTo('#gene-input-container')
					//$('#geneSubmitButton').removeClass("btn");
					
					
					$('#genesList').removeClass("input-xl");
					$('.tokenfield').removeClass('input-xl')
					$('#gene-input-container').removeClass("header-form");
					$('#gene-input-container').addClass("input-group");
					
					$('#geneSubmitButton').addClass("btn-default");
					$('#geneSubmitButton').removeClass("btn-xl");
					$('#geneSubmitButton').addClass("btn-lg");
					$('#geneSubmitButton').wrap("<span class='input-group-btn'></span>")
					
					$('#databaseFilter-container').removeClass('input-lg')
					//$('#databaseFilter-container').addClass('input-sm')
					
					$('#gene-input-container').addClass("input-form-container");
					/*
					$('#genesList').tokenfield('destroy');
					
					$('#genesList').tokenfield({
						autocomplete: {
						    source: ['red','blue','green','yellow','violet','brown','purple','black','white'],
						    delay: 100
						  },
						  showAutocompleteOnFocus: true,
					}); 
					*/ 
					
					$('#genesList').smalltokenify()
					//$('#navbar-network').show();
				}
				$('#geneslist-text').html($('#genesList').val())
				$('#input-text-static').show();
				$('#navbar-network').collapse('hide');
				//$("#geneSubmitButton").attr("disabled", "disabled");
				//alert('hello world');
				$("#cygraph").html('<img src="files/load.gif"/>')
				$("#cytoscapeweb").text('');

				var databases =$('#databaseFilters').val();
				databases = String(databases).replace(/,/g, ";")
				//alert(databases);

				var gene_moleculeop = $(
						'input[name=gene_molecule_op]:checked')
					.val();
				//alert(gene_moleculeop); 
				
				var outputType = $('input[name=outputOption]:checked').val();

				var graphType = $('input[name=graphOptions]:checked').val();

				var geneNames = $('#genesList').val();
				var pathwayType = $("#pathWayOption").val();
				var outputType = $("#outputOption").val();
				var graphType = $("#graphOptions").val();
				var genesFilter = $("#associatedGenesOption").val(); 
				var sensitvity = $("#filteringSensitivity").val(); 
			
				
				var mutationFrequency = $("#mutationSensitivity").val();
				
				//alert($("#taxonomicOrganisims").mytestoutput()); 
				
				var graphOption = "graphml"; 
				if(outputType == "DownloadJSON") {
					graphOption = "json"
				} else if (outputType == "DownloadCSV") {
					graphOption = "csv"
				}

				var idata = 'inputType=' + gene_moleculeop +
					'&genesList=' + geneNames + '&databases=' +
					databases + "&output=" + outputType +
					"&graphType=" + graphType +
					"&genesFilter=" + genesFilter +  
					"&pathwayType=" + pathwayType + 
					"&graphOption=" + graphOption + 
					"&sensitivity=" + sensitvity + 
					"&mutationFreq=" + mutationFrequency; 
				console.log(idata)
					//alert(idata);

				if (outputType == "DownloadGraphML" ||
						outputType == "DownloadJSON" || 
						outputType == "DownloadCSV") {
					$("#btnSave").hide(); 
					$("#btnpdf").hide(); 
					$("#btnViewTable").hide();
					window.open("GeneSearch?" + idata);
					return;
				} 
				
				$("#btnSave").hide();
				$("#btnpdf").hide();
				$("#btnViewTable").hide();
				////alert(databases);

				$.ajax({
					url: "GeneSearch",
					data: idata,
					success: function(result) {
						// alert(result); 

						if (result == 'NO INPUT GIVEN') {
							alert("Please Input valid Gene ID");
							$("#geneSubmitButton").removeAttr(
								"disabled");
							return;
						}

					
						
						xml = result;
						/* 
						xml = {dataSchema: {
						    nodes: [ { name: "label", type: "string" },
						             { name: "group", type: "string" }
						        ],
						    edges: [ { name: "label", type: "string" }
						           
						    ]
						}, data: result}; 
						alert(xml)
						 */

						var draw_options = {
							// your data goes here
							network: xml,
							layout: "ForceDirected",
							edgeLabelsVisible: true,
							visualStyle: visual_style,
							panToCenter: true,
							panZoomControlPosition: 'bottomLeft',
							// hide pan zoom
							// panZoomControlVisible: false 
						};
						//alert(xml);
						
						
						
						
						vis.draw(draw_options);
						$("#btnSave").show();
						$("#btnpdf").show();
						
						if (gene_moleculeop == "cellLine") { 
							$("#btnViewTable").attr("href", "printcBioDashboard.jsp?cellLines=" + geneNames)
							$("#btnViewTable").show();
						}
						if (gene_moleculeop == "molecule") {
							$("#btnViewTable").attr("href", "printGeneDashboard.jsp?drugString=" + geneNames)
							$("#btnViewTable").show();
						}
						
						
						//$("#btnViewTable").show();

						$("#geneSubmitButton").removeAttr(
							"disabled");

					}
				});

			});

	var visual_style = {

		nodes: {
			size: 50,
			color: {
				defaultValue: "#FFF",
				discreteMapper: {
					attrName: "Database Name",
					entries: [{
						attrValue: "Gene",
						value: "#C6EFF7"
					}, {
						attrValue: "Cell Line",
						value: "#31B5D6"
					}, {
						attrValue: "Organ",
						value: "#D63194"
					}, {
						attrValue: "atlas",
						value: "#BFBFBF"
					}, {
						attrValue: "Pathway",
						value: "#FF9C4A"
					}, {
						attrValue: "Drug",
						value: "#FFFF6B"
					}, {
						attrValue: "biomodels",
						value: "#847308"
					}, {
						attrValue: "Gene Expression",
						value: "#BFBFBF"
					}, {
						attrValue: "Gene Expression Up Regulated",
						value: "#FF0000"
					}, {
						attrValue: "Gene Expression Down Regulated",
						value: "#7BC618"
					}, {
						attrValue: "Gene Expression Up Down Regulated",
						value: "#BFBFBF"
					}, {
						attrValue: "biosamples",
						value: "#082984"
					}]
				}
			},
			labelHorizontalAnchor: "center",
			shape: {
				defaultValue: "ELLIPSE",
				discreteMapper: {
					attrName: "Database Name",
					entries: [{
						attrValue: "associatedGenes",
						value: "PARALLELOGRAM"
					}, {
						attrValue: "gene",
						value: "ELLIPSE"
					}, {
						attrValue: "uniprot",
						value: "DIAMOND"
					}, {
						attrValue: "atlas",
						value: "ELLIPSE"
					}, {
						attrValue: "reactome",
						value: "ROUNDRECT"
					}, {
						attrValue: "chembl",
						value: "RECTANGLE"
					}, {
						attrValue: "biomodels",
						value: "HEXAGON"
					}, {
						attrValue: "atlas_UP_DOWN",
						value: "ELLIPSE"
					}, {
						attrValue: "atlas_DOWN_UP",
						value: "ELLIPSE"
					}, {
						attrValue: "atlas_UP",
						value: "TRIANGLE"
					}, {
						attrValue: "atlas_DOWN",
						value: "VEE"
					}, {
						attrValue: "biosamples",
						value: "ELLIPSE"
					}]
				}
			},
			height: {
				discreteMapper: {
					attrName: "Database Name",
					entries: [{
						attrValue: "associatedGenes",
						value: 24
					}, {
						attrValue: "reactome",
						value: 24
					}, {
						attrValue: "chembl",
						value: 24
					}]
				}
			}
		},
		edges: {
			width: 2,
			color: "#222"
		}
		
	};

	$.fn.center = function() {
		return this.css({
			'left': ($(window).width()) - $(this).width() - 20,
			'top': ($(window).height() / 2) - $(this).height() / 2,
			'position': 'fixed'
		});
	};
	$('#lagents').center();
	//alert("Hello World");
	$("button").button();
	//$( "#gene_or_smallmolecule").buttonset();

	$("#INDEX_A").selectmenu({
		change: function(event, ui) {
			GetGeneList();
		}
	}).selectmenu("menuWidget").addClass("overflow");
	$("#INDEX_B").selectmenu({
		change: function(event, ui) {
			GetGeneList();
		}
	}).selectmenu("menuWidget").addClass("overflow");;
	$("#INDEX_C").selectmenu({
		change: function(event, ui) {
			GetGeneList();
		}
	}).selectmenu("menuWidget").addClass("overflow");;

	$('input[name=gene_molecule_op]').change(function() {
		//$('#genesList').importTags('');
		//alert($('input[name=gene_molecule_op]:checked').val());
		$("#genesList").tokenfield("setTokens", []); 
		$("#genesList").tokenfield("setAutoCompleteSource", 
				"GetGeneListJson?listType=" + $('input[name=gene_molecule_op]:checked').val());
		//GetGeneList();
	});
	/*
	$('#genesList').tagsInput({
		'delimiter': [';'],
		'tagClass': 'big',
		'defaultText': '', 
		typeahead: {
		    source: ['Amsterdam', 'Washington', 'Sydney', 'Beijing', 'Cairo']
		  }
	}).addClass("form-control");
	*/
	
	///*
	
	
	//*/
	
	/*
	$('#genesList').tokenfield({
		autocomplete: {
			source: function (request, response) {
		        $.ajax({
		            url: "GetGeneListJson?listType=gene",
		            data: { term: request.term },
		            success: function (data) {
		            	console.log(data);
		                var transformed = $.map(data, function (el) {
		                	console.log(e1);
		                    return {
		                        label: el.phrase,
		                        id: el.id
		                    };
		                });
		                response(transformed);
		            },
		            error: function (xhr, status, error) {
		            	var err = xhr.responseText ;
		            	//alert(err); 
		            	console.log("ERROR " + err);
		            	console.log("ERROR status: " + status);
		            	console.log("ERROR out: " + error);
		                response([]);
		            }
		        });
		    },
		    delay: 100
		  },
		  showAutocompleteOnFocus: true,
	}); 
	*/
	/*
	var engine = new Bloodhound({
		  local: [{value: 'red'}, {value: 'blue'}, {value: 'green'} , {value: 'yellow'}, {value: 'violet'}, {value: 'brown'}, {value: 'purple'}, {value: 'black'}, {value: 'white'}],
		  datumTokenizer: function(d) {
		    return Bloodhound.tokenizers.whitespace(d.value);
		  },
		  queryTokenizer: Bloodhound.tokenizers.whitespace
		});

		engine.initialize();
	
	$('#genesList').tokenfield({
		typeahead: [null, { source: engine.ttAdapter() }]
		    //delay: 100
		  //showAutocompleteOnFocus: true,
	}); 
	*/
	/*
	$.ajax({
        url: "GetGeneListJson?listType=gene",
        dataType: "jsonp",
        success: function( data ) {
        	geneNames =  data ;
        	alert(geneNames);
        	$('#genesList').tokenfield({
        		
        	});
        }
    });
    */
	
	
	
	GetGeneList();
	
	
	$("#btnSave").click(function() { 
		
	var type = $("#exporttypes").val();
	  var blob = "";

	  var a = document.createElement('a');
	  contentType = 'application/octet-stream';
	        var png = vis.png();
	        var binary = fixBinary(atob(png));
	        blob = new Blob([binary], {type: 'image/png'});
	  a.href = window.URL.createObjectURL(blob);
	  a.download = $('#geneslist-text').html().replace(/;/g, '-') + '-network.png';
	  a.click();

		
	});

	
	$("#btnpdf").click(function() { 
		
		var type = $("#exporttypes").val();
		  var blob = "";

		  var a = document.createElement('a');
		  contentType = 'application/octet-stream';
		        var png = vis.pdf();
		        var binary = fixBinary(atob(png));
		        blob = new Blob([binary], {type: 'application/pdf'});
		        //alert(blob);
		  a.href = window.URL.createObjectURL(blob);
		  a.download = $('#geneslist-text').html().replace(/;/g, '-') + '-NETWORK.pdf';
		  a.click();

			
	});
	
	   $("#cover").hide();
	   $("#filters").show();

};

$(function() {
	$("#geneOrSmallMolecule").buttonset();
});

function fixBinary (bin) {
    var length = bin.length;
    var buf = new ArrayBuffer(length);
    var arr = new Uint8Array(buf);
    for (var i = 0; i < length; i++) {
      arr[i] = bin.charCodeAt(i);
    }
    return buf;
  }

function TooglleAdvanceOptions() {
	$("#advanceFilteringOptions").toggle();
	if ($("#advanceFilteringOptions").is(":visible")) {
		$("#basicAdvanceSearchToggler").html("Basic Search");
		$.ajax({
			url: "GetOrgans",
			context: document.body,
			success: function(result) {

				$("#atlasOrgans").html(result);
				$("#atlasOrgans").selectmenu().selectmenu("menuWidget")
					.addClass("overflow");
				//alert('hello world');

			}
		});

		$.ajax({
			url: "getOrganismList",
			context: document.body,
			success: function(result) {

				$("#taxonomicOrganisims").html(result);
				$("#taxonomicOrganisims").multiselect(); // .selectmenu(
				// "menuWidget").addClass("overflow");
				//alert('hello world');

			}
		});

	} else {
		$("#basicAdvanceSearchToggler").html("Advance Search");
	}

}