$(function() {
	$.preload(['expand', 'collapse', 'hier_vertical_line', 'hier_container_with_siblings', 'hier_container', 
	           'hier_file', 'hier_folder', 'hier_collection'], {
		base: "/static/images/",
		extension: ".png"
	});
	
	var actionTooltipSettings = {
			content: {
				text: false
			},
			show: {
				delay: 500
			},
			position: {
				target: 'mouse',
				corner: {
					target: 'bottomMiddle',
					tooltip: 'topLeft'
				},
				adjust: {
					screen: true
				}
			},
			style: {
				classes: {
					content: "tooltip_browse_results"
				},
				border: {
					width: 0
				},
				tip: {
					corner: 'topRight',
					color: '#f2f2f2',
					size: {
						x: 7,
						y: 5
					}
				}
			}
		}; 
	
		
	$(".hier_entry_primary_action").qtip(actionTooltipSettings);
	
	actionTooltipSettings['show']['delay'] = 100;
	$(".hier_entry_secondary_action").qtip(actionTooltipSettings);
	
	$(".hier_container_collapse").live('click', function(){
		var toggleImage = $("#" + this.id + " img");
		toggleImage.attr("src", "/static/images/expand.png");
		
		var pid = this.id.substring(this.id.lastIndexOf("_") + 1);
		$("#hier_container_children_" + pid).hide();
		$(this).removeClass("hier_container_collapse");
		$(this).addClass("hier_container_expand");
		return false;
	});
	
	$(".hier_container_expand").live('click', function(){
		var initiatingLink = $(this);
		var toggleImage = $("#" + this.id + " img");
		toggleImage.attr("src", "/static/images/collapse.png");
		
		$(this).removeClass("hier_container_expand");
		$(this).addClass("hier_container_collapse");
		
		var pid = this.id.substring(this.id.lastIndexOf("_") + 1);
		var structureUrl = $(this).attr("href");
		var poundIndex = structureUrl.indexOf("#");
		structureUrl = structureUrl.substring(poundIndex + 1);
		
		var indentDepth = $("#" + this.id + ":parent").parent().find(".indent_unit").size() + 1;
		
		if ($(this).hasClass("hier_container_not_loaded")){
			var loadingImage = $("<img src=\"/static/images/ajax_loader.gif\"/>");
			initiatingLink.after(loadingImage);
			$.ajax({
				url: structureUrl,
				success: function(data){
					var childrenContainer = $("#hier_container_children_" + pid);
					$("#hier_container_children_" + pid + " .hier_entry .indent_unit:nth-child(" + indentDepth + ")").each(function(){
						if (!$(this).hasClass("hier_with_siblings")){
							$(this).addClass("hier_with_siblings");
						}
					});
					
					$("#hier_container_children_" + pid + " .hier_entry .hier_container_not_loaded").each(function(){
						var expandUrl = $(this).attr("href");
						beginIndentCode = expandUrl.indexOf("&indentCode=") + 12;
						if (beginIndentCode != -1){
							endIndentCode = expandUrl.indexOf("&", beginIndentCode);
							if (endIndentCode == -1){
								endIndentCode = expandUrl.length-1;
							}
							indentCode = expandUrl.substring(beginIndentCode, endIndentCode);
							indentCode = indentCode.substring(0, indentDepth-1) + "1" + indentCode.substring(indentDepth+1);
							
							$(this).attr("href", expandUrl.substring(0, beginIndentCode) + indentCode + expandUrl.substring(endIndentCode));
						}
						
					});
					childrenContainer.html(childrenContainer.html() + data);
					childrenContainer.show();
					initiatingLink.removeClass("hier_container_not_loaded");
					loadingImage.remove();
					if ($(".hier_truncate").length > 0){
						$("#hier_container_children_" + pid + " .hier_entry_primary_action").ellipsis({
							ellipsisClass: "hier_ellipsis",
							segmentClass: "hier_segment",
							trailingSelector: ".hier_count"
						});
					}
				},
				error: function(xhr, ajaxOptions, thrownError){
					loadingImage.remove();
				}
			});
			
		} else {
			$("#hier_container_children_" + pid).show();
		}
		return false;
	});
});