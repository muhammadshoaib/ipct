var geneNames; 

$(function() {


	$.fn.tokenify = function() {
		//alert($('input[name=gene_molecule_op]:checked').val()); 
		this
		.on('tokenfield:createtoken', function (e) {
			e.attrs.value = e.attrs.value.toUpperCase(); 
			e.attrs.label = e.attrs.label.toUpperCase(); 
		}).tokenfield({

			'delimiter': [';'],
			autocomplete: {
				minLength: 2,
				//source: ['Green', 'Red', 'Blue', 'Black', 'Orange', 'Yello', 'Brown'],
				source: "GetGeneListJson?listType=" + $('input[name=gene_molecule_op]:checked').val(),
				delay: 250
			},

			showAutocompleteOnFocus: true,
		}); 
	} 

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
		alert("HELLO WORLD"); 
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

	var vis = new org.cytoscapeweb.Visualization(div_id, options);
	var cy; 

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

		function handle_click(event) {
			$("#nodedetails").modal();
			var target = event.target;
			//alert(event.target);
			clear();
			//print("event.group = " + event.group);
			var keys = ["id", "label", "Full Name",
			            "Database Name", "URL", "Details"
			            ];
			var displaykeys = ["Identifier", "Label", "Full Name",
			                   "Database", "URL", "Details"
			                   ];
			//alert(target.data + " " + keys.length);
			//alert(target.data["Database Name"] + "," + target.data["id"]);
			var htmlOutput = "''";
			for (i = 0; i < keys.length; i++) {
				var variable_name = keys[i];

				var variable_value = target.data[variable_name];
				if (variable_name == "URL" &&
						target.data[i] != null) {
					variable_value = "<a href=\"" + target.data[i] + "\" target=_blank>" +
					target.data[i] + "</a>";
				}

				if (variable_value != null) {
					htmlOutput = "<div><div style='width:100px; float:left;'>" +
					displaykeys[i] +
					" : </div><div style='width:350px; float:left'>" +
					variable_value.replace(/,/g, "<br />") + "</div><div style='clear:both'>" +
					"<br /></div></div>";
					print(htmlOutput);

					//alert(htmlOutput); 
				}
			}
			aler("HELLO");

			event.stopPropagation();
		}

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

				$("#cygraph").html('<img src="files/load.gif"/>')
				$("#cytoscapeweb").text('');

				var databases =$('#databaseFilters').val();
				databases = String(databases).replace(/,/g, ";")

				var gene_moleculeop = $(
				'input[name=gene_molecule_op]:checked')
				.val();
				//alert(gene_moleculeop); 

				var outputType = $('input[name=outputOption]:checked').val();

				var graphType = $('input[name=graphOptions]:checked').val();

				var geneNames = $('#genesList').val();
				var organisim = $("#taxonomicOrganisims").val();
				var organName = $("#atlasOrgans").val();
				var pathwayType = $("#pathWayOption").val();
				var outputType = $("#outputOption").val();
				var graphType = $("#graphOptions").val();
				var sensitvity = $("#filteringSensitivity").val(); 

				if(String(organisim).startsWith("all")) {
					organisim = "all"; 
				}
				//alert(organisim); 

				if(String(organName).startsWith("all")) {
					organName = "all"; 
				}

				//alert($("#taxonomicOrganisims").mytestoutput()); 

				var graphOption = "json"; 
				if(outputType == "DownloadGraphML") {
					graphOption = "graphml"
				}

				var idata = 'inputType=' + gene_moleculeop +
				'&genesList=' + geneNames + '&databases=' +
				databases + "&output=" + outputType +
				"&graphType=" + graphType +
				"&pathwayType=" + pathwayType + "&taxn=" +
				organisim + "&organName=" + organName +
				"&graphOption=" + graphOption + 
				"&sensitivity=" + sensitvity; ;
				console.log(idata)

				if (outputType == "DownloadGraphML" || outputType == "DownloadJSON") {
					window.open("GeneSearch?" + idata);
					return;
				}

				////alert(databases);
				
				
				

				$.ajax({
					url: "GeneSearch", 
					data: idata,
					type: 'GET',
					dtype: "json", 
					success: function(data) {

						alert(JSON.stringify(data)); 
						var graphP = {}; 
						nodes = []; 
						//console.log(JSON.stringify(myjson)); 
						for (node in data.nodes) {
							nodes.push({data : data.nodes[node] })
						}
						edges = []; 
						//console.log(JSON.stringify(myjson)); 
						for (node in data.edges) {
							edges.push({data : data.edges[node] })
						}

						graphP["elements"] = {}; 
						graphP["elements"]["nodes"] = nodes;
						graphP["elements"]["edges"] = edges;
						alert(graphP)
						//alert(Object.keys(graphP.elements).length);
						//console.info(JSON.stringify(graphP))
						$("#cygraph").html(""); 
						//vs_js_draw(nodes, edges, data); 

						var vis = d3.select("#cygraph").append("svg");
						var w = 900, h = 400;
						vis.attr("width", w).attr("height", h);
						vis.text("The Graph").select("#cygraph")

						/*
						 * 
						cy = window.cy = cytoscape({
							container: document.getElementById('cygraph'),

							style: style_json,
							elements: graphP.elements,

							boxSelectionEnabled: false,
							autounselectify: false,

							layout: {
								//name: 'cose-bilkent',
								name: 'cose',
								animate: 'false',
								padding: 50,
							},

						});
						 */
						$("#geneSubmitButton").removeAttr(
						"disabled");
					}, 
					error: function (request, status, error) {
						console.write(request.responseText)
						alert(request.responseText);
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
							attrValue: "Cell Line",
							value: "#C6EFF7"
						}, {
							attrValue: "gene",
							value: "#31B5D6"
						}, {
							attrValue: "uniprot",
							value: "#D63194"
						}, {
							attrValue: "atlas",
							value: "#BFBFBF"
						}, {
							attrValue: "reactome",
							value: "#FF9C4A"
						}, {
							attrValue: "Drug",
							value: "#FFFF6B"
						}, {
							attrValue: "biomodels",
							value: "#847308"
						}, {
							attrValue: "atlas_UP_DOWN",
							value: "#BFBFBF"
						}, {
							attrValue: "atlas_DOWN_UP",
							value: "#BFBFBF"
						}, {
							attrValue: "atlas_UP",
							value: "#FF0000"
						}, {
							attrValue: "atlas_DOWN",
							value: "#7BC618"
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

		//vis.png(); 

		//vis.exportNetwork('xgmml', 'output.jsp?type=xml');




		html2canvas($("#cygraph"), {
			onrendered: function(canvas) {
				theCanvas = canvas;
				document.body.appendChild(canvas);

				// Convert and download as image 
				Canvas2Image.saveAsJPEG(canvas, false, 1024, 1024); 
				//$("#img-out").append(canvas);
				// Clean up 
				document.body.removeChild(canvas);
			}
		});

		/*
		 * 
		 * 
		var type = $("#exporttypes").val();
	  var blob = "";

	  var a = document.createElement('a');
	  contentType = 'application/octet-stream';
	        var png = cy.png();
	        alert(png);
	       // var binary = fixBinary(png);
	        blob = new Blob([png], {type: 'image/png'});
	  a.href = window.URL.createObjectURL(png);
	  a.download = 'network.png';
	  a.click();

		var screenshotData = new BitmapData(450,300);
		screenshotData.draw(document.getElementById("cytoscapeWeb1"));
		var pngBytesByteArray = PNGEncoder.encode(screenshotData);
		var screenshotBase64 = Base64.encodeByteArray(pngBytes);

		 */

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

function vs_js_draw(nodes, edges, data) {

	alert(JSON.stringify(nodes)); 
	alert(JSON.stringify(edges)); 
	// create some nodes
	/* 
    var nodes = [
        {id: 0, "label": "Myriel", "group": 1},
        {id: 1, "label": "Napoleon", "group": 1},
        {id: 2, "label": "Mlle.Baptistine", "group": 1},
        {id: 3, "label": "Mme.Magloire", "group": 1},
        {id: 4, "label": "CountessdeLo", "group": 1},
        {id: 5, "label": "Geborand", "group": 1},
        {id: 6, "label": "Champtercier", "group": 1},
        {id: 7, "label": "Cravatte", "group": 1},
        {id: 8, "label": "Count", "group": 1},
        {id: 9, "label": "OldMan", "group": 1},
        {id: 10, "label": "Labarre", "group": 2},
        {id: 11, "label": "Valjean", "group": 2},
        {id: 12, "label": "Marguerite", "group": 3},
        {id: 13, "label": "Mme.deR", "group": 2},
        {id: 14, "label": "Isabeau", "group": 2},
        {id: 15, "label": "Gervais", "group": 2},
        {id: 16, "label": "Tholomyes", "group": 3},
        {id: 17, "label": "Listolier", "group": 3},
        {id: 18, "label": "Fameuil", "group": 3},
        {id: 19, "label": "Blacheville", "group": 3},
        {id: 20, "label": "Favourite", "group": 3},
        {id: 21, "label": "Dahlia", "group": 3},
        {id: 22, "label": "Zephine", "group": 3},
        {id: 23, "label": "Fantine", "group": 3},
        {id: 24, "label": "Mme.Thenardier", "group": 4},
        {id: 25, "label": "Thenardier", "group": 4},
        {id: 26, "label": "Cosette", "group": 5},
        {id: 27, "label": "Javert", "group": 4},
        {id: 28, "label": "Fauchelevent", "group": 0},
        {id: 29, "label": "Bamatabois", "group": 2},
        {id: 30, "label": "Perpetue", "group": 3},
        {id: 31, "label": "Simplice", "group": 2},
        {id: 32, "label": "Scaufflaire", "group": 2},
        {id: 33, "label": "Woman1", "group": 2},
        {id: 34, "label": "Judge", "group": 2},
        {id: 35, "label": "Champmathieu", "group": 2},
        {id: 36, "label": "Brevet", "group": 2},
        {id: 37, "label": "Chenildieu", "group": 2},
        {id: 38, "label": "Cochepaille", "group": 2},
        {id: 39, "label": "Pontmercy", "group": 4},
        {id: 40, "label": "Boulatruelle", "group": 6},
        {id: 41, "label": "Eponine", "group": 4},
        {id: 42, "label": "Anzelma", "group": 4},
        {id: 43, "label": "Woman2", "group": 5},
        {id: 44, "label": "MotherInnocent", "group": 0},
        {id: 45, "label": "Gribier", "group": 0},
        {id: 46, "label": "Jondrette", "group": 7},
        {id: 47, "label": "Mme.Burgon", "group": 7},
        {id: 48, "label": "Gavroche", "group": 8},
        {id: 49, "label": "Gillenormand", "group": 5},
        {id: 50, "label": "Magnon", "group": 5},
        {id: 51, "label": "Mlle.Gillenormand", "group": 5},
        {id: 52, "label": "Mme.Pontmercy", "group": 5},
        {id: 53, "label": "Mlle.Vaubois", "group": 5},
        {id: 54, "label": "Lt.Gillenormand", "group": 5},
        {id: 55, "label": "Marius", "group": 8},
        {id: 56, "label": "BaronessT", "group": 5},
        {id: 57, "label": "Mabeuf", "group": 8},
        {id: 58, "label": "Enjolras", "group": 8},
        {id: 59, "label": "Combeferre", "group": 8},
        {id: 60, "label": "Prouvaire", "group": 8},
        {id: 61, "label": "Feuilly", "group": 8},
        {id: 62, "label": "Courfeyrac", "group": 8},
        {id: 63, "label": "Bahorel", "group": 8},
        {id: 64, "label": "Bossuet", "group": 8},
        {id: 65, "label": "Joly", "group": 8},
        {id: 66, "label": "Grantaire", "group": 8},
        {id: 67, "label": "MotherPlutarch", "group": 9},
        {id: 68, "label": "Gueulemer", "group": 4},
        {id: 69, "label": "Babet", "group": 4},
        {id: 70, "label": "Claquesous", "group": 4},
        {id: 71, "label": "Montparnasse", "group": 4},
        {id: 72, "label": "Toussaint", "group": 5},
        {id: 73, "label": "Child1", "group": 10},
        {id: 74, "label": "Child2", "group": 10},
        {id: 75, "label": "Brujon", "group": 4},
        {id: 76, "label": "Mme.Hucheloup", "group": 8}
    ];

    // create some edges
    var edges = [
        {"from": 1, "to": 0},
        {"from": 2, "to": 0},
        {"from": 3, "to": 0},
        {"from": 3, "to": 2},
        {"from": 4, "to": 0},
        {"from": 5, "to": 0},
        {"from": 6, "to": 0},
        {"from": 7, "to": 0},
        {"from": 8, "to": 0},
        {"from": 9, "to": 0},
        {"from": 11, "to": 10},
        {"from": 11, "to": 3},
        {"from": 11, "to": 2},
        {"from": 11, "to": 0},
        {"from": 12, "to": 11},
        {"from": 13, "to": 11},
        {"from": 14, "to": 11},
        {"from": 15, "to": 11},
        {"from": 17, "to": 16},
        {"from": 18, "to": 16},
        {"from": 18, "to": 17},
        {"from": 19, "to": 16},
        {"from": 19, "to": 17},
        {"from": 19, "to": 18},
        {"from": 20, "to": 16},
        {"from": 20, "to": 17},
        {"from": 20, "to": 18},
        {"from": 20, "to": 19},
        {"from": 21, "to": 16},
        {"from": 21, "to": 17},
        {"from": 21, "to": 18},
        {"from": 21, "to": 19},
        {"from": 21, "to": 20},
        {"from": 22, "to": 16},
        {"from": 22, "to": 17},
        {"from": 22, "to": 18},
        {"from": 22, "to": 19},
        {"from": 22, "to": 20},
        {"from": 22, "to": 21},
        {"from": 23, "to": 16},
        {"from": 23, "to": 17},
        {"from": 23, "to": 18},
        {"from": 23, "to": 19},
        {"from": 23, "to": 20},
        {"from": 23, "to": 21},
        {"from": 23, "to": 22},
        {"from": 23, "to": 12},
        {"from": 23, "to": 11},
        {"from": 24, "to": 23},
        {"from": 24, "to": 11},
        {"from": 25, "to": 24},
        {"from": 25, "to": 23},
        {"from": 25, "to": 11},
        {"from": 26, "to": 24},
        {"from": 26, "to": 11},
        {"from": 26, "to": 16},
        {"from": 26, "to": 25},
        {"from": 27, "to": 11},
        {"from": 27, "to": 23},
        {"from": 27, "to": 25},
        {"from": 27, "to": 24},
        {"from": 27, "to": 26},
        {"from": 28, "to": 11},
        {"from": 28, "to": 27},
        {"from": 29, "to": 23},
        {"from": 29, "to": 27},
        {"from": 29, "to": 11},
        {"from": 30, "to": 23},
        {"from": 31, "to": 30},
        {"from": 31, "to": 11},
        {"from": 31, "to": 23},
        {"from": 31, "to": 27},
        {"from": 32, "to": 11},
        {"from": 33, "to": 11},
        {"from": 33, "to": 27},
        {"from": 34, "to": 11},
        {"from": 34, "to": 29},
        {"from": 35, "to": 11},
        {"from": 35, "to": 34},
        {"from": 35, "to": 29},
        {"from": 36, "to": 34},
        {"from": 36, "to": 35},
        {"from": 36, "to": 11},
        {"from": 36, "to": 29},
        {"from": 37, "to": 34},
        {"from": 37, "to": 35},
        {"from": 37, "to": 36},
        {"from": 37, "to": 11},
        {"from": 37, "to": 29},
        {"from": 38, "to": 34},
        {"from": 38, "to": 35},
        {"from": 38, "to": 36},
        {"from": 38, "to": 37},
        {"from": 38, "to": 11},
        {"from": 38, "to": 29},
        {"from": 39, "to": 25},
        {"from": 40, "to": 25},
        {"from": 41, "to": 24},
        {"from": 41, "to": 25},
        {"from": 42, "to": 41},
        {"from": 42, "to": 25},
        {"from": 42, "to": 24},
        {"from": 43, "to": 11},
        {"from": 43, "to": 26},
        {"from": 43, "to": 27},
        {"from": 44, "to": 28},
        {"from": 44, "to": 11},
        {"from": 45, "to": 28},
        {"from": 47, "to": 46},
        {"from": 48, "to": 47},
        {"from": 48, "to": 25},
        {"from": 48, "to": 27},
        {"from": 48, "to": 11},
        {"from": 49, "to": 26},
        {"from": 49, "to": 11},
        {"from": 50, "to": 49},
        {"from": 50, "to": 24},
        {"from": 51, "to": 49},
        {"from": 51, "to": 26},
        {"from": 51, "to": 11},
        {"from": 52, "to": 51},
        {"from": 52, "to": 39},
        {"from": 53, "to": 51},
        {"from": 54, "to": 51},
        {"from": 54, "to": 49},
        {"from": 54, "to": 26},
        {"from": 55, "to": 51},
        {"from": 55, "to": 49},
        {"from": 55, "to": 39},
        {"from": 55, "to": 54},
        {"from": 55, "to": 26},
        {"from": 55, "to": 11},
        {"from": 55, "to": 16},
        {"from": 55, "to": 25},
        {"from": 55, "to": 41},
        {"from": 55, "to": 48},
        {"from": 56, "to": 49},
        {"from": 56, "to": 55},
        {"from": 57, "to": 55},
        {"from": 57, "to": 41},
        {"from": 57, "to": 48},
        {"from": 58, "to": 55},
        {"from": 58, "to": 48},
        {"from": 58, "to": 27},
        {"from": 58, "to": 57},
        {"from": 58, "to": 11},
        {"from": 59, "to": 58},
        {"from": 59, "to": 55},
        {"from": 59, "to": 48},
        {"from": 59, "to": 57},
        {"from": 60, "to": 48},
        {"from": 60, "to": 58},
        {"from": 60, "to": 59},
        {"from": 61, "to": 48},
        {"from": 61, "to": 58},
        {"from": 61, "to": 60},
        {"from": 61, "to": 59},
        {"from": 61, "to": 57},
        {"from": 61, "to": 55},
        {"from": 62, "to": 55},
        {"from": 62, "to": 58},
        {"from": 62, "to": 59},
        {"from": 62, "to": 48},
        {"from": 62, "to": 57},
        {"from": 62, "to": 41},
        {"from": 62, "to": 61},
        {"from": 62, "to": 60},
        {"from": 63, "to": 59},
        {"from": 63, "to": 48},
        {"from": 63, "to": 62},
        {"from": 63, "to": 57},
        {"from": 63, "to": 58},
        {"from": 63, "to": 61},
        {"from": 63, "to": 60},
        {"from": 63, "to": 55},
        {"from": 64, "to": 55},
        {"from": 64, "to": 62},
        {"from": 64, "to": 48},
        {"from": 64, "to": 63},
        {"from": 64, "to": 58},
        {"from": 64, "to": 61},
        {"from": 64, "to": 60},
        {"from": 64, "to": 59},
        {"from": 64, "to": 57},
        {"from": 64, "to": 11},
        {"from": 65, "to": 63},
        {"from": 65, "to": 64},
        {"from": 65, "to": 48},
        {"from": 65, "to": 62},
        {"from": 65, "to": 58},
        {"from": 65, "to": 61},
        {"from": 65, "to": 60},
        {"from": 65, "to": 59},
        {"from": 65, "to": 57},
        {"from": 65, "to": 55},
        {"from": 66, "to": 64},
        {"from": 66, "to": 58},
        {"from": 66, "to": 59},
        {"from": 66, "to": 62},
        {"from": 66, "to": 65},
        {"from": 66, "to": 48},
        {"from": 66, "to": 63},
        {"from": 66, "to": 61},
        {"from": 66, "to": 60},
        {"from": 67, "to": 57},
        {"from": 68, "to": 25},
        {"from": 68, "to": 11},
        {"from": 68, "to": 24},
        {"from": 68, "to": 27},
        {"from": 68, "to": 48},
        {"from": 68, "to": 41},
        {"from": 69, "to": 25},
        {"from": 69, "to": 68},
        {"from": 69, "to": 11},
        {"from": 69, "to": 24},
        {"from": 69, "to": 27},
        {"from": 69, "to": 48},
        {"from": 69, "to": 41},
        {"from": 70, "to": 25},
        {"from": 70, "to": 69},
        {"from": 70, "to": 68},
        {"from": 70, "to": 11},
        {"from": 70, "to": 24},
        {"from": 70, "to": 27},
        {"from": 70, "to": 41},
        {"from": 70, "to": 58},
        {"from": 71, "to": 27},
        {"from": 71, "to": 69},
        {"from": 71, "to": 68},
        {"from": 71, "to": 70},
        {"from": 71, "to": 11},
        {"from": 71, "to": 48},
        {"from": 71, "to": 41},
        {"from": 71, "to": 25},
        {"from": 72, "to": 26},
        {"from": 72, "to": 27},
        {"from": 72, "to": 11},
        {"from": 73, "to": 48},
        {"from": 74, "to": 48},
        {"from": 74, "to": 73},
        {"from": 75, "to": 69},
        {"from": 75, "to": 68},
        {"from": 75, "to": 25},
        {"from": 75, "to": 48},
        {"from": 75, "to": 41},
        {"from": 75, "to": 70},
        {"from": 75, "to": 71},
        {"from": 76, "to": 64},
        {"from": 76, "to": 65},
        {"from": 76, "to": 66},
        {"from": 76, "to": 63},
        {"from": 76, "to": 62},
        {"from": 76, "to": 48},
        {"from": 76, "to": 58}
    ];
	 */
	// create a network
	var container = document.getElementById('cygraph');
	//var data = {
	//    nodes: nodes,
	//    edges: edges
	//};
	var options = {
			nodes: {
				shape: 'dot',
				size: 16
			},
			physics: {
				forceAtlas2Based: {
					gravitationalConstant: -26,
					centralGravity: 0.005,
					springLength: 230,
					springConstant: 0.18
				},
				maxVelocity: 146,
				solver: 'forceAtlas2Based',
				timestep: 0.35,
				stabilization: {iterations: 150}
			}
	};
	var network = new vis.Network(container, data, options);

}