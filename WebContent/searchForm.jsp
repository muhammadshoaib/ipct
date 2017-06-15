<% 
//boolean navbarmode = false; 
//navbarmode = request.getParameter("mode").equals("navbar") ; 
%>

<div id="filters">
			<!--  
					<select id="taxonomicOrganisims" class="filtering-options1" 
						data-selected-text-format="count"
						multiple="multiple" name="taxonomicOrganisims">
						<option value="" selected="selected">All Organisims</option>
					</select>
					<select id="atlasOrgans" class="filtering-options1" 
						multiple="multiple" name="atlasOrgans"
						data-selected-text-format="count">
						<option value="" selected="selected">All Organs</option>
					</select>
					 -->
					<span>
					<label style="color:#fff;">Drug Sensitivity</label>
					<select class="filtering-options" id="filteringSensitivity" title="Drug Sensitivity" >
						  <option  value="-1.00">-1.00</option>
						  <option value="-1.25">-1.25</option>
						  <option value="-1.50" selected="selected">-1.50</option>
						  <option value="-1.75">-1.75</option>
						  <option value="-2.00">-2.00</option>
						  <option value="-2.25">-2.25</option>
						  <option value="-2.50">-2.50</option>
						  <option value="-2.75">-2.75</option>
						  <option value="-3.00">-3.00</option>
						</select>
					</span>
					<span>
					<label style="color:#fff;">Mutation Frequency</label>
					<select class="filtering-options" id="mutationSensitivity" title="Drug Sensitivity" >
						  <option  value="0.10">10%</option>
						  <option value="0.15">15%</option>
						  <option value="0.20" selected="selected">20%</option>
						  <option value="0.25">25%</option>
						  <option value="0.30">30%</option>
						  <option value="0.40">40%</option>
						  <option value="0.50">50%</option>
						  <option value="0.60">60%</option>
						</select>
					</span>
					
					<select id="associatedGenesOption" class="filtering-options" name="associatedGenesOption">
						<option value="cancerGenes" selected="selected">Cancer Genes Only</option>
						<option value="excludeFrequentMut">Exclude commonly mutated </option>
						<option value="allGenes">All Genes</option>
					</select>
					
					<select id="pathWayOption" class="filtering-options" name="pathWayOption">
						<option value="allPathways" selected="selected">All Pathways</option>
						<option value="metabolicPathways">Metabolic Pathways</option>
						<option value="signalingPathways">Signaling Pathways</option>
					</select>
					
					<select id="graphOptions" class="filtering-options" name="graphOptions">
						<option value="detailed" selected="selected">All Connections</option>
						<option value="summerized">Shared Connections</option>
					</select>
					
				</div>
				
                <div class="intro-lead-in">Please Enter Cell Line, Small Molecule or Gene Names</div>
                <div id="main-input">
                <div id="databaseFilter-container" class="input-lg" >
                
                
               	<span id="geneOrSmallMolecule"  >
               	
				<input type="radio" name="gene_molecule_op" value="cellLine"
					checked="checked" id="cellRadioButton"  /><label
					for="cellRadioButton">Cell Line</label> <input type="radio"
					name="gene_molecule_op" value="molecule"
					id="meleculeRadioButton" /> <label for="meleculeRadioButton">Small
					Molecule</label> <input type="radio" name="gene_molecule_op" value="gene"
					id="geneRadioButton" /><label
					for="geneRadioButton">Gene</label>
					
				
				</span>
				<span>
				<select class="selectpicker" id="databaseFilters" multiple 
					title="<span class='fa fa-database'></span> Databases" 
					data-selected-text-format="static" >
					  <option style="background: #C6EFF7"  selected="selected" value="mutGenes">Mutated Genes</option>
					  <option style="background: #D63194" selected="selected" value="cellLines">Cell Lines</option>
					  <option style="background: #BFBFBF" value="atlasexpression">Atlas Expression<</option>
					  <option style="background: #FF9C4A" value="pathways">Reactome Pathways<</option>
					</select>
				</span>
				<span>
				<select id="outputOption" class="filtering-options" name="outputOption">
						<option value="ViewGraph" selected="selected">View Graph </option>
						<option value="DownloadGraphML">Download GraphML</option>
						<option value="DownloadJSON">Download JSON</option>
						<option value="DownloadCSV">Download CSV</option>
					</select>
				</span>	
				</div>
                
				<div id="gene-input-container" class="header-form"><div class="tokenfield-container" id="tokenfield-container">
				
				 <input type="text" class="form-control input-xl" id="genesList"  required data-validation-required-message="Please enter your name.">
                </div>
                </div>
                </div>
				<span id="geneSubmitButton-container"> 
				<a id="geneSubmitButton" class="btn btn-xl">Explore Connections</a>
				</span>