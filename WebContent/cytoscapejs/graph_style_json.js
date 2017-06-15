var style_json =  [
{
	selector: 'node',
	style: {
		'content': 'data(label)',
		'text-opacity':1.0,
		'text-valign': 'center',
		'text-halign': 'center',
		'text-wrap': 'wrap',
		'text-max-width': 80,
		'background-color': '#11479e', 
		'font-size': 8,
		'color' : '#000'
	}
},
{
	selector: 'node[group = "Cell Line"]',
	style: {
		'background-color': '#31B5D6'
	}
},
{
	selector: 'node[group = "Organ"]',
	style: {
		'background-color': '#D63194', 
		'shape': 'diamond', 
	}
},
{
	selector: 'node[group = "atlas"]',
	style: {
		'background-color': '#BFBFBF', 
		'shape': '', 
			
	}
},
{
	selector: 'node[group = "Pathway"]',
	style: {
		'background-color': '#FF9C4A', 
		'shape': 'roundrectangle', 
	}
},
{
	selector: 'node[group = "Drug"]',
	style: {
		'background-color': '#FFFF6B', 
		'shape': 'rectangle', 
	}
},
{
	selector: 'node[group = "biomodels"]',
	style: {
		'background-color': '#847308', 
		'shape': 'hexagon', 
	}
},
{
	selector: 'node[group = "biosamples"]',
	style: {
		'background-color': '#082984', 
		'shape': 'octagon', 
		'text-outline-width': 2,
        'text-outline-color': '#000', 
        'color': '#FFF'
	}
},
{
	selector: 'node[group = "atlas_UP_DOWN"]',
	style: {
		'background-color': '#BFBFBF', 
		'shape': '', 
	}
},
{
	selector: 'node[group = "atlas_DOWN_UP"]',
	style: {
		'background-color': '#BFBFBF', 
		'shape': '', 
	}
},
{
	selector: 'node[group = "atlas_UP"]',
	style: {
		'background-color': '#FF0000', 
		'shape': 'triangle', 
	}
},
{
	selector: 'node[group = "atlas_DOWN"]',
	style: {
		'background-color': '#7BC618', 
		'shape': 'vee', 
	}
	
},
{
	selector: 'node[group = "Gene"]',
	style: {
		'background-color': '#C6EFF7', 
		'shape': 'rhomboid'
	}
	
},
{
	selector: 'edge',
	style: {
		'width': 4,
		'target-arrow-shape': 'triangle',
		'line-color': '#9dbaea',
		'target-arrow-color': '#9dbaea'
	}
}
] ;