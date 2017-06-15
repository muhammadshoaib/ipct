/*!
 * Start Bootstrap - Agency Bootstrap Theme (http://startbootstrap.com)
 * Code licensed under the Apache License v2.0.
 * For details, see http://www.apache.org/licenses/LICENSE-2.0.
 */

// jQuery for page scrolling feature - requires jQuery Easing plugin


	var template = {
         button: '<button type="button" class="multiselect dropdown-toggle" data-toggle="dropdown"></button>',
         ul: '<ul class="multiselect-container dropdown-menu"></ul>',
         filter: '<li class="multiselect-item filter ui-widget-content "><div class="input-group"><span class="input-group-addon"><i class="glyphicon glyphicon-search"></i></span><input class="form-control multiselect-search" type="text"></div></li>',
         filterClearBtn: '<span class="input-group-btn"><button class="btn btn-default multiselect-clear-filter" type="button"><i class="glyphicon glyphicon-remove-circle"></i></button></span>',
         li: '<li class="ui-widget-content"><a href="javascript:void(0);"><label></label></a></li>',
         divider: '<li class="multiselect-item divider"></li>',
         liGroup: '<li class="multiselect-item group "><label class="multiselect-group ui-widget-content"></label></li>'
     }; 
	 var filteringOptionmultiselectProps = {
			 							
							     		enableCaseInsensitiveFiltering: true,
							    		includeSelectAllOption: true, 
							    		selectAllValue: 'all',
							    		maxHeight: 200
 									}; 

$(function() {
    $('a.page-scroll').bind('click', function(event) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: $($anchor.attr('href')).offset().top
        }, 10, 'easeInOutExpo');
        event.preventDefault();
    });
});

$(function() {
    $('a#geneset').bind('click', function(event) {
        alert("HELLO WORLD"); 
		$('header').hide()
		$('nav#navbar-homepage').hide();
		$('nav#navbar-network').show();
    });
});

// Highlight the top nav as scrolling occurs
$('body').scrollspy({
    target: '.navbar-fixed-top'
})

// Closes the Responsive Menu on Menu Item Click
$('.navbar-collapse ul li a').click(function() {
    $('.navbar-toggle:visible').click();
});


$('#databases-lagent').popover({
    placement: 'left',
    html: true,
    content: $("#lagents-filter-panel").html(), 
    offset: 20,
});

$('#pathway-filter-popover').popover({
    placement: 'bottom',
    html: true,
    content: $("#pathway-filter-panel").html()
});

 $(document).ready(function() {
        $('.filtering-options').multiselect(filteringOptionmultiselectProps);
        
        filteringOptionmultiselectProps['selectAllValue'] = 'allOrgans'
        $("#atlasOrgans").multiselect(filteringOptionmultiselectProps);
        
        filteringOptionmultiselectProps['selectAllValue'] = 'allOrganisims'
        $("#taxonomicOrganisims").multiselect(filteringOptionmultiselectProps);
    });