$(document).ready(function(){
	$tabs = $("#tabs").tabs({
	});
	initSearchDialog();
	initPopup();
});


/* Creates the HTML for the items from the given JSON array data. */
function fillItems(data, modeName) {
	currentPage = 0;
	var currPage = 0;	
	var foundItems = eval(data);
	var publishInfo = foundItems.shift();
	if (modeName == "category") {
		// disable search button, if there are no items in the gallery
		if (foundItems.length == 0) {
			$("#categorysearchbutton").addClass("ui-state-disabled");
			$("#categorysearchbutton").get(0).disabled = true;
		} else {
			$("#categorysearchbutton").removeClass("ui-state-disabled");
			$("#categorysearchbutton").get(0).disabled = false;
		}
		// filter search results
		foundItems = filterItems(foundItems, modeName);
		// break building list, if no result are found for given search
		if (foundItems == "noresults") {
			return;
		}		
		categoryItems = new ItemList();
		categoryItems.items = foundItems;
		// display number of search results if not 0 
		var categorysearchresult = " ";
		var cTitle = categories[activeCategory].title;
		if (searchKeyword != null) {
			if (foundItems.length == 1) {
				categorysearchresult = ": " + foundItems.length + " " + LANG.SEARCH_RESULT + " \""  + searchKeyword + "\"";
			} else {
				categorysearchresult = ": " + foundItems.length + " " + LANG.SEARCH_RESULTS + " \""  + searchKeyword + "\"";
			}
			//cut the title of the gallery, if it is too long
			cTitle = cutTitle(cTitle);
		}
		$("#" + modeName + "itemlist").append("<div id=\"categoryname\">"
			+ cTitle
			+ categorysearchresult 
			+ "<span>"
			+ categories[activeCategory].path
			+ "</span></div>");
		$("#" + modeName + "itemselectbutton").hide();
		$("#" + modeName + "itempublishbutton").hide();
		if (initValues.dialogmode == "widget") {
			$("#" + modeName + "okbutton").hide();
		}
		$("#" + modeName + "itemtitle").removeClass();
		$("#" + modeName + "itemtitle").unbind();
	} else {
		// disable search button, if there are no items in the gallery
		if (foundItems.length == 0) {
			$("#gallerysearchbutton").addClass("ui-state-disabled");
			$("#gallerysearchbutton").get(0).disabled = true;
		} else {
			$("#gallerysearchbutton").removeClass("ui-state-disabled");
			$("#gallerysearchbutton").get(0).disabled = false;
		}
		// filter search results
		foundItems = filterItems(foundItems, modeName);
		// break building list, if no result are found for given search
		if (foundItems == "noresults") {
			return;
		}
		galleryItems = new ItemList();
		galleryItems.items = foundItems;
		galleryItems.publishable = publishInfo.publishable;
		if (galleryItems.publishable == true) { // enabled		
			$("#gallerypublishbutton").removeClass("ui-state-disabled");
		} else { //galleryItems.publishable == false -> disabled
			$("#gallerypublishbutton").addClass("ui-state-disabled");		
		}
		$("#gallerypublishbutton").get(0).disabled = !galleryItems.publishable;
		var gTitle, gPath;
		if (activeGallery != -1) {
			gTitle = galleries[activeGallery].title;
			gPath = galleries[activeGallery].path;
			
		} else {
			gTitle = startGallery.title;
			gPath = startGallery.path;
		}
		// display number of search results if not 0
		var gallerysearchresult = " ";
		if (searchKeyword != null) {
			if (foundItems.length == 1) {
				gallerysearchresult = ": " + foundItems.length + " " + LANG.SEARCH_RESULT + " \""  + searchKeyword + "\"";
			} else {
				gallerysearchresult = ": " + foundItems.length + " " + LANG.SEARCH_RESULTS + " \""  + searchKeyword + "\"";
			}
			//prepare title of the gallery, if too long
			gTitle = cutTitle(gTitle);
		}

		/* Name and path of the gallery */
		$("#" + modeName + "itemlist").append("<div id=\"galleryname\">"
			+ gTitle
			+ gallerysearchresult
			+ "<span>"
			+ gPath
			+ "</span></div>");
		//remove buttons to be not seen
		$("#" + modeName + "itemselectbutton").hide();
		$("#" + modeName + "itempublishbutton").hide();
		if (initValues.dialogmode == "widget") {
			$("#" + modeName + "okbutton").hide();
		}
		$("#" + modeName + "itemtitle").removeClass();
		$("#" + modeName + "itemtitle").unbind();
	}
	var innerListId = modeName + "itemlistinner";
	$("#" + modeName + "itemlist").append("<div id=\"" + innerListId  + "\"></div>");
	innerListId = "#" + innerListId;
	var paginationId = modeName + "itempage";
	$(innerListId).hide();
	for (var i = 0; i < foundItems.length; i++) {
		var page = Math.floor(i / itemsPerPage);
		if ((i + 1) % itemsPerPage == 1) {
			$(innerListId).append("<div id=\"" + paginationId + page + "\"></div>");
			if (page > 0) {
				$("#" + paginationId + page).hide();
			}
		}
		var fileitem = foundItems[i];
		
		// name of the file item
		var itemName = "";
		itemName += fileitem.linkpath.substring(fileitem.linkpath.lastIndexOf("/") + 1);
		
		var mouseAttrsClick = "";
		mouseAttrsClick += " onclick=\"markItem(";
		mouseAttrsClick += i + ", \'";
		mouseAttrsClick += modeName;
		mouseAttrsClick += "\');\"";
		mouseAttrsClick += " ondblclick=\"setActiveItem(";
		mouseAttrsClick += i + ", \'";
		mouseAttrsClick += modeName;
		mouseAttrsClick += "\', false);\"";
		var mouseAttrs = "";
		mouseAttrs += " onmouseover=\"showItemInfo(";
		mouseAttrs += i + ", \'";
		mouseAttrs += modeName;
		mouseAttrs += "\');\" onmouseout=\"hideItemInfo(";
		mouseAttrs += i + ", \'";
		mouseAttrs += modeName;
		mouseAttrs += "\');\"";

		var itemHtml = "";
		itemHtml += "<div id=\"";
		itemHtml += modeName;
		itemHtml += "fileitem";
		itemHtml += i;
		itemHtml += "\" class=\"\">";
		itemHtml += "<table style=\"width\ : 100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tbody>" ;
		
		itemHtml +="<tr" + mouseAttrs + " " + mouseAttrsClick + " >";
		itemHtml +="<td style=\"width\ : 32px; padding-right: 10px\" >";
		//show the thumb for the mimetype of the item
		itemHtml += "<img alt=\"";
		itemHtml += fileitem.title + "<br/>" + LANG.DETAIL_SIZE + fileitem.size;
		itemHtml += "\" alt=\"\" title=\"\" src=\"";
		itemHtml += vfsPathPrefixItems + "html.png\" />"; 
		itemHtml += "</td>";
		itemHtml += "<td >";
		itemHtml += "<table style=\"width\ : 100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tbody>";
		itemHtml += "<tr>";
		itemHtml += "<td style=\"width\ : 60%\">";
		itemHtml += "<span id=\"";
		itemHtml += modeName;
		itemHtml += "fileitemtitle";
		itemHtml += i;
		itemHtml += "\" ";
		itemHtml += "class=\"title\">" + fileitem.title;
		itemHtml += "</span>";
		itemHtml += "</td>";
		itemHtml += "<td style=\"width\ : 20%\">&nbsp;</td>";
		itemHtml += "<td style=\"width\ : 15%\">&nbsp;</td>";
		itemHtml += "<td style=\"width\ : 5%\">";
		
		itemHtml += "<span class=\"changeiconwrapper\">";
		// show icon for new or changed items
		itemHtml += "<span class=\"changeiconlayer\" id=\"";
		itemHtml += modeName + "itemlayer" + i;
		itemHtml += "\" >";
		if (fileitem.state == 1) {
			// changed item
			itemHtml += "<img src=\"" + vfsPathPrefixItems + "img_chg.png\" alt=\"\" title=\"";
			itemHtml += LANG.ITEM_STATE_CHANGED;
			itemHtml += "\" />";
		} else if (fileitem.state == 2) {
			// new item
			itemHtml += "<img src=\"" + vfsPathPrefixItems + "img_new.png\" alt=\"\" title=\"";
			itemHtml += LANG.ITEM_STATE_NEW;
			itemHtml += "\" />";
		}
		if (fileitem.lockedby != "") {
			// item is locked by other user
			itemHtml += "<img src=\"" + vfsPathPrefixItems + "img_locked.png\" alt=\"\" title=\"";
			itemHtml += LANG.ITEM_LOCKSTATE_LOCKED.replace(/%\(user\)/, fileitem.lockedby);
			itemHtml += "\" />";
		}
		itemHtml += "</span>";
		itemHtml += "</span>";
		itemHtml += "</td>";
		itemHtml += "</tr>";
		itemHtml += "<tr>";
		itemHtml += "<td style=\"width\ : 60%\"><span class=\"filename\" alt=\"";
		itemHtml += fileitem.linkpath;
		itemHtml += "\"  >";
		itemHtml += itemName;
		itemHtml += "</span></td>";
		itemHtml += "<td style=\"width\ : 20%\">";
		itemHtml += "<span id=\"";
		itemHtml += modeName;
		itemHtml += "fileitemmd";
		itemHtml += i;
		itemHtml += "\" ";
		itemHtml += "class=\"filename\" alt=\"";
		itemHtml += LANG.DETAIL_DM + " " + fileitem.datelastmodified;
		itemHtml += "\"  >";
		itemHtml += fileitem.datelastmodified;
		itemHtml += "</span></td>";
		itemHtml += "<td style=\"width\ : 15%\"><span class=\"filename\" alt=\"";
		itemHtml += LANG.DETAIL_SIZE + " " + fileitem.size;
		itemHtml += "\"  >";
		itemHtml += fileitem.size;
		itemHtml += "</span></td>";
		itemHtml += "<td style=\"width\ : 5%\">&nbsp;</td>";
		itemHtml += "</tr>";
		itemHtml += "</tbody></table>";
		itemHtml += "</td>";
		itemHtml += "</tr></tbody></table></div>";
		
		
		$("#" + paginationId + page).append(itemHtml);
		
		/* mark the item, if it is already selected */
		if (loadItemSitepath != "" && loadItemSitepath != null) {
			if (loadItemSitepath.path == fileitem.sitepath || loadItemSitepath.path == fileitem.linkpath ) {
				markItem(i, modeName);
				$("#" + paginationId + "0").hide();
				$("#" + paginationId + page).show();
				currPage = page;
				if (loadItemSitepath.target != null && loadItemSitepath.target != "") {
					$("#" + modeName + "linktarget").val(loadItemSitepath.target);
				}
			}
		} 		
	}
	
	//$(innerListId + " img.imgthumb").jHelperTip({trigger: "hover", source: "attribute", attrName: "alt", opacity: 0.75});
	$(innerListId + " span.filename").jHelperTip({trigger: "hover", source: "attribute", attrName: "alt", opacity: 0.75});
	$(innerListId).append("<div style=\"clear: left;\"></div>");
	if (foundItems.length + 1 > itemsPerPage) {
		$("#" + modeName + "itemlist").append("<div id=\"item" + modeName + "-paginationwrapper\"><span id=\"item" + modeName + "-pagination\"></span></div>");
		if (modeName == "gallery") {
			$("#item" + modeName + "-pagination").pagination(foundItems.length, {
				items_per_page: itemsPerPage,
				callback: showItemPagegallery,
				prev_text: LANG.PAGINATION_PREVIOUS,
				next_text: LANG.PAGINATION_NEXT,
				prev_show_always: false,
				next_show_always: false,
				num_edge_entries: 1,
				current_page: currPage
			});
		} else {
			$("#item" + modeName + "-pagination").pagination(foundItems.length, {
				items_per_page: itemsPerPage,
				callback: showItemPagecategory,
				prev_text: LANG.PAGINATION_PREVIOUS,
				next_text: LANG.PAGINATION_NEXT,
				prev_show_always: false,
				next_show_always: false,
				num_edge_entries: 1
			});
		}
	}
	$(innerListId).show();
}

/* Shows the additional item information (called on mouseover or on preview tab). */
function showItemInfo(itemIndex, idPrefix, currItem) {
	clearTimeout(imgDetailTimeout);
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	if (itemIndex != -1 && currItem == null) {
		if (idPrefix == "category") {
			currItem = categoryItems.items[itemIndex];
			markedIndex = categoryItems.markedItem;
		} else {
			currItem = galleryItems.items[itemIndex];
			markedIndex = galleryItems.markedItem;
		}
		if (markedIndex != itemIndex) {
			$("#" + idPrefix + "item" + itemIndex).attr("class", "imgitemhover");
		}
	}
	if (markedIndex == -1 || markedIndex == itemIndex) {
		idPrefix = idPrefix + "item";
		if (idPrefix.indexOf("#") != 0) {
			idPrefix = "#" + idPrefix;
		}
		var itemName = "<span title=\"";
		itemName += currItem.linkpath;
		itemName += "\">";
		itemName += currItem.linkpath.substring(currItem.linkpath.lastIndexOf("/") + 1);
		itemName += "</span>";
		var stateTxt = "";
		if (currItem.state == 1) {
			// changed item
			stateTxt = LANG.DETAIL_STATE_CHANGED;
			$(idPrefix + "state").attr("class", "stateinfochanged");
		} else if (currItem.state == 2) {
			// new item
			stateTxt = LANG.DETAIL_STATE_NEW;
			$(idPrefix + "state").attr("class", "stateinfonew");
		}
		$(idPrefix + "state").html(stateTxt);
		$(idPrefix + "name").html(itemName);
		$(idPrefix + "title").html(currItem.title);
		$(idPrefix + "dm").html(currItem.datelastmodified);

	}
}

/* Hides the additional item information delayed (called on mouseout). */
function hideItemInfo(itemIndex, idPrefix) {
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	if (idPrefix == "category") {
		markedIndex = categoryItems.markedItem;
	} else {
		markedIndex = galleryItems.markedItem;
	}
	if (markedIndex != itemIndex) {
		//$("#" + idPrefix + "item" + itemIndex).attr("class", "imgitem");
		$("#" + idPrefix + "fileitem" + itemIndex).removeClass("hover");
	}
	if (markedIndex == -1) {
		imgDetailTimeout = setTimeout("doHideItemInfo(\'" + idPrefix + "\')", 350);
	}
}

/* Really hides the additional item information. */
function doHideItemInfo(idPrefix) {
	$("#" + idPrefix + "itemstate").html("&nbsp;");
	$("#" + idPrefix + "itemname").html("&nbsp;");
	$("#" + idPrefix + "itemtitle").html("&nbsp;");
	$("#" + idPrefix + "itemdm").html("&nbsp;");
}

/* Marks the currently selected item. */
function markItem(itemIndex, idPrefix) {
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	try {
		if (idPrefix == "category") {
			markedIndex = categoryItems.markedItem;
		} else {
			markedIndex = galleryItems.markedItem;
			
		}
	} catch (e) {
		return;
	}
	if (markedIndex != itemIndex) {
		$("#" + idPrefix + "fileitem" + markedIndex).removeClass();
		$("#" + idPrefix + "fileitem" + itemIndex).addClass("active");
		markedIndex = itemIndex;
			
	} else {
		$("#" + idPrefix + "fileitem" + markedIndex).removeClass();
		markedIndex = -1;
		
		activeItem = null;
		
	}
	if (idPrefix == "category") {
		categoryItems.markedItem = markedIndex;
	} else {
		galleryItems.markedItem = markedIndex;
	}
	if (markedIndex != -1) {
		var isEditable;
		var state = 0;
		if (idPrefix == "gallery") {
			isEditable = galleryItems.items[markedIndex].editable;
			state = galleryItems.items[markedIndex].state;
		} else {
			isEditable = categoryItems.items[markedIndex].editable;
			state = categoryItems.items[markedIndex].state;
		}
		if (isEditable == true) {
			$("#" + idPrefix + "itemtitle").attr("class", "editable");
			$("#" + idPrefix + "itemtitle").editable(function(value, settings) {
			    setItemTitle(value, idPrefix);
			    return(value);
			 }, {
			    submit   : LANG.BUTTON_OK,
			    cancel   : LANG.BUTTON_CANCEL,
			    cssclass : "edittitle",
			    height   : "none",
			    select   : true,
			    tooltip  : LANG.DETAIL_EDIT_HELP
			});
			if (state != 0) {
				$("#" + idPrefix + "itempublishbutton").fadeIn("fast");
			} else {
				$("#" + idPrefix + "itempublishbutton").fadeOut("slow");
			}
		} else {
			$("#" + idPrefix + "itemtitle").unbind();
			$("#" + idPrefix + "itemtitle").removeClass();
			$("#" + idPrefix + "itempublishbutton").fadeOut("slow");
		}
		showItemInfo(markedIndex, idPrefix);
		if (initValues.viewonly == false) {
			$("#" + idPrefix + "itemselectbutton").fadeIn("fast");
			if(initValues.dialogmode == "widget") {
				$("#" + idPrefix + "okbutton").fadeIn("fast");
			}
			if (initValues.dialogmode == "editor") {
				try {
					// do additional stuff with the active item if necessary
					activeItemAdditionalActions();
				} catch (e) {}
			}
		}
	} else {
		$("#" + idPrefix + "itemselectbutton").fadeOut("slow");
		$("#" + idPrefix + "itemtitle").unbind();
		$("#" + idPrefix + "itemtitle").removeClass();
		$("#" + idPrefix + "itempublishbutton").fadeOut("slow");
		if (initValues.dialogmode == "widget") {
			$("#" + idPrefix + "okbutton").fadeOut("slow");
		}
	}
}


/* Loads the active item in the preview tab and sets the parameters */
function loadActiveItem(data, isInitial) {
	if (data == "none") {
		return;
	} 
	//set the active item
	activeItem = eval("(" + data + ")");
	//swich to preview tab
	$tabs.data("disabled.tabs", []);
	$tabs.tabs("select", 0);
	try {
		// do additional stuff in editor mode
		activeImageAdditionalActions(isInitial);
	} catch (e) {}
	
	$('#htmlpreview').empty();
	var html = "<p><div width=\"100%\" height=\"100%\">";
	html += activeItem.html;
	html += "</div></p>";
	$('#htmlpreview').append(html);
	
	$('#prevhtmlname').html(activeItem.title);
	showItemInfo(-1, "detail", activeItem);

}



/* Refreshes the marked item. */
function refreshMarkedItem(data, modeName) {
	var state;
	var itemIndex;
	var newImg = eval("(" + data + ")");
	if (activeItem != null && activeItem.sitepath == newImg.sitepath) {
		// update the item preview with the new iteminformation
		activeItem = newImg;
		showItemInfo(-1, "detail", activeItem, true);
	}
	if (modeName == "gallery") {
		itemIndex = galleryItems.markedItem;
		galleryItems.items[itemIndex] = newImg;
		updateTitleInItemlist(itemIndex, modeName, newImg.title);
		updateModDateInItemlist(itemIndex, modeName, newImg.datelastmodified);
		state = galleryItems.items[itemIndex].state;
		$("#gallerypublishbutton").get(0).disabled = false;
		$("#gallerypublishbutton").removeClass("ui-state-disabled");
	} else {
		itemIndex = categoryItems.markedItem;
		categoryItems.items[itemIndex] = newImg;
		updateTitleInItemlist(itemIndex, modeName, newImg.title);
		updateModDateInItemlist(itemIndex, modeName, newImg.datelastmodified);
		state = categoryItems.items[itemIndex].state;
	}
	if (state == 1 || state == 2) {
		$("#" + modeName + "itemlayer" + itemIndex).empty();
		$("#" + modeName + "itempublishbutton").fadeIn();
		var imgHtml = "";
		if (state == 1) {
			// changed item
			imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_chg.png\" alt=\"\" title=\"";
			imgHtml += LANG.ITEM_STATE_CHANGED;
			imgHtml += "\" />";
		} else {
			// new item
			imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_new.png\" alt=\"\" title=\"";
			imgHtml += LANG.ITEM_STATE_NEW;
			imgHtml += "\" />";
		}
		$("#" + modeName + "itemlayer" + itemIndex).append(imgHtml);
	} else {
		$("#" + modeName + "itemlayer" + itemIndex).empty();
		$("#" + modeName + "itempublishbutton").fadeOut("slow");
	}
	showItemInfo(itemIndex, modeName);
}


function updateTitleInItemlist(itemIndex, modeName, newTitle) {
	var id = "#" + modeName + "fileitemtitle" + itemIndex;
	$(id).html(newTitle);
	$(id).addClass("title");
}

function updateModDateInItemlist(itemIndex, modeName, modDate) {
	var id = "#" + modeName + "fileitemmd" + itemIndex;
	$(id).html(modDate);
	$(id).addClass("filename");
}

/* Toggles the preview html information. */
function toggleDetailInfo() {
	if ($("#toggleInfo").hasClass("showinfo")) {
		showDetailInfo();
	} else {
		hideDetailInfo();
	}
}

/* Hides the preview image information. */
function hideDetailInfo() {
	$("#detailinfo").slideUp("normal");
	$("#toggleInfo").attr("class", "showinfo");
}

/* Shows the preview image information. */
function showDetailInfo() {
	$("#detailinfo").slideDown("normal");
	$("#toggleInfo").attr("class", "hideinfo");
}
