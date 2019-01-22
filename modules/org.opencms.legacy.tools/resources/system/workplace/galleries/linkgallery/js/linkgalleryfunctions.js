$(document).ready(function(){
	$tabs = $("#tabs").tabs({
	});
	initSearchDialog();
	initPopup();
	isLinkGallery = true;	
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
		$("#" + modeName + "itempreviewbutton").hide();
		if (initValues.dialogmode == "widget") {
			$("#" + modeName + "okbutton").hide();
		}		
		$("#" + modeName + "itemtitle").unbind();
		$("#" + modeName + "itemtitle").removeClass();
		$("#" + modeName + "itemlinkurl").unbind();
		$("#" + modeName + "itemlinkurl").removeClass();
		//Delete
		$("#" + modeName + "itemdeletebutton").hide();
		//Delete
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
		// display the upload button if user has write permissions		
		galleryItems.directpublish = publishInfo.directpublish;
		galleryItems.writepermission = publishInfo.writepermission;
		if (galleryItems.writepermission == false) { // disabled	
			$("#gallerynewlinkbutton").addClass("ui-state-disabled");
			$("#gallerynewlinkbutton").get(0).disabled = true;
		} else {
			$("#gallerynewlinkbutton").removeClass("ui-state-disabled");
			$("#gallerynewlinkbutton").get(0).disabled = false;
		}

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
		$("#" + modeName + "itempreviewbutton").hide();
		if (initValues.dialogmode == "widget") {
			$("#" + modeName + "okbutton").hide();
		}
		$("#" + modeName + "itemtitle").unbind();
		$("#" + modeName + "itemtitle").removeClass();
		$("#" + modeName + "itemlinkurl").unbind();
		$("#" + modeName + "itemlinkurl").removeClass();
		//Delete
		$("#" + modeName + "itemdeletebutton").hide();
		//Delete
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
		var itemName = fileitem.linkpath.substring(fileitem.linkpath.lastIndexOf("/") + 1);
		if (itemName.length > 50) {
			itemName = itemName.substr(0, 49) + " ...";
		}
		// title of the file item
		var itemTitle = fileitem.title;
		if (itemTitle.length > 60) {
			itemTitle = itemTitle.substr(0, 59) + " ...";
		}
		
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
		// show the thumb for the link item
		itemHtml += "<img alt=\"\" title=\"\" src=\"";
		itemHtml += vfsPathPrefixItems  + "pointer.png";
		itemHtml += "\" />"; 
		itemHtml += "</td>";
		itemHtml += "<td>";
		itemHtml += "<table style=\"width\ : 100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tbody>";
		itemHtml += "<tr>";
		itemHtml += "<td colspan=\"2\" style=\"width\ : 95%\">";
		itemHtml += "<span id=\"";
		itemHtml += modeName;
		itemHtml += "fileitemtitle";
		itemHtml += i;
		itemHtml += "\" ";
		itemHtml += "class=\"title\">" + itemTitle;
		itemHtml += "</span>";
		itemHtml += "</td>";
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
		itemHtml += "<td style=\"width\ : 75%\">";
		itemHtml += "<span id=\"";
		itemHtml += modeName;
		itemHtml += "fileitemlinkurl";
		itemHtml += i;
		itemHtml += "\" class=\"filename\" alt=\"";
		itemHtml += LANG.ITEM_INPUT_LINKURL + ": " + fileitem.sitepath;
		itemHtml += "\"  >";
		itemHtml += fileitem.pointer;
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
function showItemInfo(itemIndex, idPrefix, currItem, showAll) {
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
		$(idPrefix + "linkurl").html(currItem.pointer.toString());
		$(idPrefix + "title").html(currItem.title);

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
		$("#" + idPrefix + "fileitem" + itemIndex).removeClass("hover");
	}
	if (markedIndex == -1) {
		imgDetailTimeout = setTimeout("doHideItemInfo(\'" + idPrefix + "\')", 350);
	}
}

/* Really hides the additional item information. */
function doHideItemInfo(idPrefix) {
	$("#" + idPrefix + "itemstate").html("&nbsp;");
	$("#" + idPrefix + "itemlinkurl").html("&nbsp;");
	$("#" + idPrefix + "itemtitle").html("&nbsp;");
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
		
		// set the active item onclick in editor mode
		if (initValues.dialogmode == "editor" && itemIndex != -1) {
			setActiveItem(itemIndex, idPrefix, true);
		}	
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
		var hasDirectPublish;	
		var hasWritePermission;
		// set permissions on the given resource
		if (idPrefix == "gallery") {
			isEditable = galleryItems.items[markedIndex].editable;
			hasDirectPublish = galleryItems.items[markedIndex].directpublish;	
			hasWritePermission = galleryItems.items[markedIndex].writepermission;
			state = galleryItems.items[markedIndex].state;
		} else {
			isEditable = categoryItems.items[markedIndex].editable;
			hasDirectPublish = categoryItems.items[markedIndex].directpublish;	
			hasWritePermission = categoryItems.items[markedIndex].writepermission;
			state = categoryItems.items[markedIndex].state;
		}
		// is editable or unlocked
		if (isEditable == true) {
			
			// if user has write permission for this resource
			if (hasWritePermission == true) {
				// enable title edit mode
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
				// enable link url edit mode
				$("#" + idPrefix + "itemlinkurl").attr("class", "editable");
				$("#" + idPrefix + "itemlinkurl").editable(function(value, settings) {
				    setItemLinkurl(value, idPrefix);
				    return(value);
				 }, {
				    submit   : LANG.BUTTON_OK,
				    cancel   : LANG.BUTTON_CANCEL,
				    cssclass : "edittitle",
				    height   : "none",
				    select   : true,
				    tooltip  : LANG.DETAIL_EDIT_URL_HELP
				});
				// Delete
				$("#" + idPrefix + "itemdeletebutton").show();	

			} else {
				// disable title and link url edit mode
				$("#" + idPrefix + "itemtitle").unbind();
				$("#" + idPrefix + "itemtitle").removeClass();
				$("#" + idPrefix + "itemlinkurl").unbind();
				$("#" + idPrefix + "itemlinkurl").removeClass();
				// Delete
				$("#" + idPrefix + "itemdeletebutton").fadeOut("fast");
				// Delete
			}

			// user has direct publish permission for this resource
			if (state != 0 && hasDirectPublish == true) {
				$("#" + idPrefix + "itempublishbutton").show();
			} else {
				$("#" + idPrefix + "itempublishbutton").fadeOut("fast");
			}
		} else {
			// disable title and link url edit mode
			$("#" + idPrefix + "itemtitle").unbind();
			$("#" + idPrefix + "itemtitle").removeClass();
			$("#" + idPrefix + "itemlinkurl").unbind();
			$("#" + idPrefix + "itemlinkurl").removeClass();
			$("#" + idPrefix + "itempublishbutton").fadeOut("fast");
			// Delete
			$("#" + idPrefix + "itemdeletebutton").fadeOut("fast");
			// Delete
		}
		showItemInfo(markedIndex, idPrefix);
		$("#" + idPrefix + "itempreviewbutton").show();
		if (initValues.viewonly == false) {
			$("#" + idPrefix + "itemselectbutton").show();
			if (initValues.dialogmode == "widget") {
				$("#" + idPrefix + "okbutton").show();			
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
		$("#" + idPrefix + "itempublishbutton").fadeOut("slow");
		$("#" + idPrefix + "itemtitle").removeClass();
		$("#" + idPrefix + "itemlinkurl").removeClass();
		if (initValues.dialogmode == "widget") {
			$("#" + idPrefix + "okbutton").fadeOut("slow");
		}
		$("#" + idPrefix + "itemstate").html("&nbsp;");
		$("#" + idPrefix + "itempreviewbutton").fadeOut("slow");
		// Delete
		$("#" + idPrefix + "itemdeletebutton").fadeOut("fast");
		// Delete

	}
}


/* Loads the active item in the preview tab and sets the parameters */
function loadActiveItem(data, isInitial) {
	if (data == "none") {
		return;
	} 
	activeItem = eval("(" + data + ")");
	
	if (isInitial == false) {

		if (initValues.dialogmode == "widget") {
			okPressed();
		}else if (initValues.dialogmode == "editor") {
				Ok();
			try {
				dialog.CloseDialog();
			} catch (e) {}
		}
	}
}

/* Opens preview window for given item */
function openPreview(markedItem, modeName) {
	var checkItem;
	if (modeName == "gallery") {
		checkItem = galleryItems.items[galleryItems.markedItem];
	} else {
		checkItem = categoryItems.items[categoryItems.markedItem];
	}
	window.open(checkItem.linkpath, "_blank", 'toolbar=no, location=no, directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=800,height=700');

	
}

/* Refreshes the marked item. */
function refreshMarkedItem(data, modeName) {
	var state;
	var itemIndex;
	// direct publish permissions for given resource
	var hasDirectPublish;
	var newItem = eval("(" + data + ")");
	if (activeItem != null && activeItem.sitepath == newItem.sitepath) {
		// update the item preview with the new iteminformation
		activeItem = newItem;
		showItemInfo(-1, "detail", activeItem, true);
	}
	if (modeName == "gallery") {
		itemIndex = galleryItems.markedItem;
		galleryItems.items[itemIndex] = newItem;
		updateTitleInItemlist(itemIndex, modeName, newItem.title);
		updateLinkurlInItemlist(itemIndex, modeName, newItem.pointer.toString());
		updateModDateInItemlist(itemIndex, modeName, newItem.datelastmodified);
		state = galleryItems.items[itemIndex].state;
		// direct publish permissions for given resource
		hasDirectPublish = galleryItems.items[itemIndex].directpublish;
		// direct publish permissions for the gallery folder
		if (galleryItems.directpublish == true) {
			$("#gallerypublishbutton").get(0).disabled = false;
			$("#gallerypublishbutton").removeClass("ui-state-disabled");
		}
	} else {
		itemIndex = categoryItems.markedItem;
		categoryItems.items[itemIndex] = newItem;
		updateTitleInItemlist(itemIndex, modeName, newItem.title);
		updateLinkurlInItemlist(itemIndex, modeName, newItem.pointer.toString());
		updateModDateInItemlist(itemIndex, modeName, newItem.datelastmodified);
		state = categoryItems.items[itemIndex].state;
		// direct publish permissions for given resource
		hasDirectPublish = categoryItems.items[itemIndex].directpublish;
	}
	if (state == 1 || state == 2) {
		$("#" + modeName + "itemlayer" + itemIndex).empty();
		// show the resource publish button, if user has direct publish permission
		if (hasDirectPublish == true) {
			$("#" + modeName + "itempublishbutton").show();
		}
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
	if (newTitle.length > 60) {
		newTitle= newTitle.substr(0, 59) + " ...";
	}
	$(id).html(newTitle);
	$(id).addClass("title");
}

function updateLinkurlInItemlist(itemIndex, modeName, newUrl) {
	var id = "#" + modeName + "fileitemlinkurl" + itemIndex;
	$(id).html(newUrl);
	$(id).addClass("filename");
}

function updateModDateInItemlist(itemIndex, modeName, modDate) {
	var id = "#" + modeName + "fileitemmd" + itemIndex;
	$(id).html(modDate);
	$(id).addClass("filename");
}