isImageGallery = true;

$(document).ready(function(){
	$tabs = $("#tabs").tabs({
	});
	initSearchDialog();
	initPopup();
	initFormatSelectBox();
});

/* Creates the HTML for the images from the given JSON array data. */
function fillItems(data, modeName) {
	currentPage = 0;
	var foundImages = eval(data);
	var publishInfo = foundImages.shift();
	if (modeName == "category") {
		// disable search button, if there are no items in the gallery
		if (foundImages.length == 0) {
			$("#categorysearchbutton").addClass("ui-state-disabled");
			$("#categorysearchbutton").get(0).disabled = true;
		} else {
			$("#categorysearchbutton").removeClass("ui-state-disabled");
			$("#categorysearchbutton").get(0).disabled = false;
		}
		// filter search results
		foundImages = filterItems(foundImages, modeName);
		if (foundImages == "noresults") {
			return;
		}
		categoryItems = new ItemList();
		categoryItems.items = foundImages;
		// display number of search results if not 0 
		var categorysearchresult = " ";
		var cTitle = categories[activeCategory].title;
		if (searchKeyword != null) {
			if (foundImages.length == 1) {
				categorysearchresult = ": " + foundImages.length + " " + LANG.SEARCH_RESULT + " \""  + searchKeyword + "\"";
			} else {
				categorysearchresult = ": " + foundImages.length + " " + LANG.SEARCH_RESULTS + " \""  + searchKeyword + "\"";
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
		// hide the buttons
		$("#" + modeName + "itemtitle").unbind();
		$("#" + modeName + "itemtitle").removeClass();
		$("#" + modeName + "itempublishbutton").hide();
		$("#" + modeName + "itemselectbutton").hide();
		//Delete
		$("#" + modeName + "itemdeletebutton").hide();
		//Delete
	} else {
		// disable search button, if there are no items in the gallery
		if (foundImages.length == 0) {
			$("#gallerysearchbutton").addClass("ui-state-disabled");
			$("#gallerysearchbutton").get(0).disabled = true;
		} else {
			$("#gallerysearchbutton").removeClass("ui-state-disabled");
			$("#gallerysearchbutton").get(0).disabled = false;
		}
		// filter search results
		foundImages = filterItems(foundImages, modeName);
		if (foundImages == "noresults") {
			return;
		}
		galleryItems = new ItemList();
		galleryItems.items = foundImages;
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
			$("#galleryuploadbutton").addClass("ui-state-disabled");
			$("#galleryuploadbutton").get(0).disabled = true;
		} else {
			$("#galleryuploadbutton").removeClass("ui-state-disabled");
			$("#galleryuploadbutton").get(0).disabled = false;
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
			if (foundImages.length == 1) {
				gallerysearchresult = ": " + foundImages.length + " " + LANG.SEARCH_RESULT + " \""  + searchKeyword + "\"";
			} else {
				gallerysearchresult = ": " + foundImages.length + " " + LANG.SEARCH_RESULTS + " \""  + searchKeyword + "\"";
			}
			//prepare title of the gallery, if too long
			gTitle = cutTitle(gTitle);
		}
		$("#" + modeName + "itemlist").append("<div id=\"galleryname\">"
			+ gTitle
			+ gallerysearchresult
			+ "<span>"
			+ gPath
			+ "</span></div>");
		// hide the buttons
		$("#" + modeName + "itemselectbutton").hide();
		$("#" + modeName + "itemtitle").unbind();
		$("#" + modeName + "itemtitle").removeClass();
		$("#" + modeName + "itempublishbutton").hide();
		//Delete
		$("#" + modeName + "itemdeletebutton").hide();
		//Delete
	}
	var innerListId = modeName + "imagelistinner";
	$("#" + modeName + "itemlist").append("<div class=\"imagelist\" id=\"" + innerListId  + "\"></div>");
	innerListId = "#" + innerListId;
	$(innerListId).hide();
	// create empty div elements for the pages
	var paginationId = modeName + "itempage";
	var pageCount = Math.ceil((foundImages.length + 1) / imagesPerPage);
	for (var i = 0; i < pageCount; i++) {
		$(innerListId).append("<div id=\"" + paginationId + "" + i + "\"></div>");
		if (i > 0) {
			$("#" + paginationId + i).hide();
		}
	}  
	// fill the first page with images
	fillImagesOnPage(foundImages, modeName, 0);
	$(innerListId).append("<div style=\"clear: left;\"></div>");
	if (foundImages.length + 1 > imagesPerPage) {
		$("#" + modeName + "itemlist").append("<div id=\"item" + modeName + "-paginationwrapper\"><span id=\"item" + modeName + "-pagination\"></span></div>");
		if (modeName == "gallery") {
			$("#item" + modeName + "-pagination").pagination(foundImages.length, {
				items_per_page: imagesPerPage,
				callback: showImagePagegallery,
				prev_text: LANG.PAGINATION_PREVIOUS,
				next_text: LANG.PAGINATION_NEXT,
				prev_show_always: false,
				next_show_always: false,
				num_edge_entries: 1
			});
		} else {
			$("#item" + modeName + "-pagination").pagination(foundImages.length, {
				items_per_page: imagesPerPage,
				callback: showImagePagecategory,
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

/* Fills the images that should be shown on the currently selected page. */
function fillImagesOnPage(imageList, modeName, page) {
	var paginationId = modeName + "itempage";
	// check if the images have to be created
	if ($("#" + paginationId + page).children().length == 0) {
		// calculate start and end indexes in image list
		var beginIndex = page * imagesPerPage;
		var endIndex = beginIndex + imagesPerPage;
		if (endIndex > imageList.length) {
			endIndex = imageList.length;
		}
		for (var i = beginIndex; i < endIndex; i++) {
			var image = imageList[i];
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
			var imgHtml = "";
			imgHtml += "<div class=\"imgitem\"";
			imgHtml += " id=\"";
			imgHtml += modeName + "item" + i;
			imgHtml += "\"><div class=\"imgitemwrapper\">";
			// show icon for new or changed images
			imgHtml += "<span class=\"imglayer\" id=\"";
			imgHtml += modeName + "itemlayer" + i;
			imgHtml += "\"" + mouseAttrs + mouseAttrsClick + ">";
			if (image.state == 1) {
				// changed image
				imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_chg.png\" alt=\"\" title=\"";
				imgHtml += LANG.IMGITEM_STATE_CHANGED;
				imgHtml += "\" />";
			} else if (image.state == 2) {
				// new image
				imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_new.png\" alt=\"\" title=\"";
				imgHtml += LANG.IMGITEM_STATE_NEW;
				imgHtml += "\" />";
			}
			if (image.lockedby != "") {
				// image is locked by other user
				imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_locked.png\" alt=\"\" title=\"";
				imgHtml += LANG.IMGITEM_LOCKSTATE_LOCKED.replace(/%\(user\)/, image.lockedby);
				imgHtml += "\" />";
			}
			imgHtml += "</span>";
			// show the thumb image
			imgHtml += "<img class=\"imgthumb\" alt=\"";
			imgHtml += image.width + " x " + image.height + "<br/>" + LANG.DETAIL_SIZE + " " + image.size;
			imgHtml += "\" alt=\"\" title=\"\" src=\"";
			imgHtml += image.scalepath;
			imgHtml += "\"";
			imgHtml += mouseAttrs + mouseAttrsClick;
			imgHtml += "/>";
			imgHtml += "</div></div>";
			$("#" + paginationId + page).append(imgHtml);
		}
		// initialize image tool tips on hover
		$("#" + paginationId + page + " img.imgthumb").jHelperTip({trigger: "hover", source: "attribute", attrName: "alt", opacity: 0.75});
	}
}

/* Callback function of the pagination, shows the clicked gallery page. */
function showImagePagegallery(page_id, jq) {
	fillImagesOnPage(galleryItems.items, "gallery", page_id);
	showItemPagegallery(page_id, jq);
}

/* Callback function of the pagination, shows the clicked category page. */
function showImagePagecategory(page_id, jq) {
	fillImagesOnPage(categoryItems.items, "category", page_id);
	showItemPagecategory(page_id, jq);
}

/* Shows the additional image information (called on mouseover or on preview tab). */
function showItemInfo(imgIndex, idPrefix, currImg, showAll) {
	clearTimeout(imgDetailTimeout);
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	if (imgIndex != -1 && currImg == null) {
		if (idPrefix == "category") {
			currImg = categoryItems.items[imgIndex];
			markedIndex = categoryItems.markedItem;
		} else {
			currImg = galleryItems.items[imgIndex];
			markedIndex = galleryItems.markedItem;
		}
		if (markedIndex != imgIndex) {
			$("#" + idPrefix + "item" + imgIndex).attr("class", "imgitemhover");
		}
	}
	if (markedIndex == -1 || markedIndex == imgIndex) {
		idPrefix = idPrefix + "item";
		if (idPrefix.indexOf("#") != 0) {
			idPrefix = "#" + idPrefix;
		}
		var imgName = "<span title=\"";
		imgName += currImg.sitepath;
		imgName += "\">";
		imgName += currImg.linkpath.substring(currImg.linkpath.lastIndexOf("/") + 1);
		imgName += "</span>";
		var stateTxt = "";
		if (currImg.state == 1) {
			// changed image
			stateTxt = LANG.IMGDETAIL_STATE_CHANGED;
			$(idPrefix + "state").attr("class", "stateinfochanged");
		} else if (currImg.state == 2) {
			// new image
			stateTxt = LANG.IMGDETAIL_STATE_NEW;
			$(idPrefix + "state").attr("class", "stateinfonew");
		}
		$(idPrefix + "state").html(stateTxt);
		$(idPrefix + "name").html(imgName);
		$(idPrefix + "title").html(currImg.title);
		$(idPrefix + "type").html(getImageType(currImg.type));
		$(idPrefix + "dc").html(currImg.datecreated);
		$(idPrefix + "dm").html(currImg.datelastmodified);
		$(idPrefix + "id").html(currImg.id);
		if (showAll != null && showAll == true) {
			$(idPrefix + "format").html(currImg.width + " x " + currImg.height);
			$(idPrefix + "size").html(currImg.size);
		}
	}
}

/* Hides the additional image information delayed (called on mouseout). */
function hideItemInfo(imgIndex, idPrefix) {
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	if (idPrefix == "category") {
		markedIndex = categoryItems.markedItem;
	} else {
		markedIndex = galleryItems.markedItem;
	}
	if (markedIndex != imgIndex) {
		$("#" + idPrefix + "item" + imgIndex).attr("class", "imgitem");
	}
	if (markedIndex == -1) {
		imgDetailTimeout = setTimeout("doHideItemInfo(\'" + idPrefix + "\')", 350);
	}
}

/* Really hides the additional image information. */
function doHideItemInfo(idPrefix) {
	$("#" + idPrefix + "itemstate").html("&nbsp;");
	$("#" + idPrefix + "itemname").html("&nbsp;");
	$("#" + idPrefix + "itemtitle").html("&nbsp;");
	$("#" + idPrefix + "itemtype").html("&nbsp;");
	$("#" + idPrefix + "itemdc").html("&nbsp;");
	$("#" + idPrefix + "itemdm").html("&nbsp;");
}

/* Loads the active image in the preview tab and sets the parameters */
function loadActiveItem(data, isInitial) {
	if (data == "none") {
		return;
	} 
	activeItem = eval("(" + data + ")");
	activeItem.newwidth = 0;
	activeItem.newheight = 0;
	if (isInitial == true) {
		// initial image loaded
		var cropIt = false;
		if (getScaleValue(initValues.scale, "cx") != "") {
			cropIt = true;
			activeItem.cropx = getScaleValueInt(initValues.scale, "cx");
			activeItem.cropy = getScaleValueInt(initValues.scale, "cy");
			activeItem.cropw = getScaleValueInt(initValues.scale, "cw");
			activeItem.croph = getScaleValueInt(initValues.scale, "ch");
			var cropParams = "cx:" + activeItem.cropx;
			cropParams += ",cy:" + activeItem.cropy;
			cropParams += ",cw:" + activeItem.cropw;
			cropParams += ",ch:" + activeItem.croph;
			activeItem.crop = cropParams;
			initValues.scale = removeScaleValue(initValues.scale, "cx");
			initValues.scale = removeScaleValue(initValues.scale, "cy");
			initValues.scale = removeScaleValue(initValues.scale, "cw");
			initValues.scale = removeScaleValue(initValues.scale, "ch");
		}
		if (initValues.useformats == true) {
			if (initValues.imgwidth != "?") {
				$("#txtWidth").get(0).value = initValues.imgwidth;
				if (initValues.imgheight == "?") {
					onSizeChanged("Width", initValues.imgwidth, false, false);
					if (cropIt == true) {
						var newHeight = Math.round(activeItem.croph / (activeItem.cropw / initValues.imgwidth));
						if (newHeight != parseInt($("#txtHeight").get(0).value)) {
							setLockRatio(false);
							$("#txtHeight").get(0).value = newHeight;
							onSizeChanged("Height", newHeight, false, false);
						}
					}
				} else if (parseInt($("#txtHeight").get(0).value) != initValues.imgheight) {
					setLockRatio(false);
					$("#txtHeight").get(0).value = initValues.imgheight;
					onSizeChanged("Height", initValues.imgheight, false, false);
				}
			} else if (initValues.imgheight != "?") {
				$("#txtHeight").get(0).value = initValues.imgheight;
				onSizeChanged("Height", initValues.imgheight, false, false);
				if (cropIt == true) {
					var newWidth = Math.round(activeItem.cropw / (activeItem.croph / initValues.imgheight));
					if (newWidth != parseInt($("#txtWidth").get(0).value)) {
						setLockRatio(false);
						$("#txtWidth").get(0).value = newWidth;
						onSizeChanged("Width", newHeight, false, false);
					}
				}
			}
			if (initValues.widgetmode == "simple" && initValues.showformats == true) {
            			// refresh the format select box
				refreshSelectBox(true);
			}

			if (cropIt == true) {
				setCropActive(true);
			} else {
				setCropActive(false);
			}
		} else {
			var sizeChanged = false;
			if (initValues.imgwidth != "") {
				var newWidth = parseInt(initValues.imgwidth);
				$("#txtWidth").get(0).value = newWidth;
				activeItem.newwidth = newWidth;
				sizeChanged = true;
			}
			if (initValues.imgheight != "") {
				var newHeight = parseInt(initValues.imgheight);
				$("#txtHeight").get(0).value = newHeight;
				activeItem.newheight = newHeight;
				sizeChanged = true;
			}
			if (sizeChanged == true) {
				var testW = activeItem.newwidth > 0 ? activeItem.newwidth : activeItem.width;
				var testH = activeItem.newheight > 0 ? activeItem.newheight : activeItem.height;
				var delta = testW / activeItem.width;
				var calH = Math.round(activeItem.height * delta);
				if (calH != testH) {
					setLockRatio(false);
				}
			}
			if (cropIt == true) {
				setCropActive(true);
			} else {
				setCropActive(false);
			}
	        	if (initValues.widgetmode != "simple" || initValues.showformats == true) {
            			// refresh the format select box
				refreshSelectBox();
			} else  if (initValues.showformats == false) {
				$("#croplink").hide();
			} 
		}
		initValues.scale = removeScaleValue(initValues.scale, "w");
		initValues.scale = removeScaleValue(initValues.scale, "h");
	} else {
		// image loaded by user selection
		$tabs.data("disabled.tabs", []);
		$tabs.tabs("select", 0);
		resetSizes();
		setCropActive(false);
		if (initValues.useformats != true) {
			$("#croplink").hide();
		}
		if (initValues.useformats == true && initValues.showformats == true) {
			$("#croplink").show();
		}
	}
	try {
		// do additional stuff with the active image if necessary
		activeImageAdditionalActions(isInitial);
	} catch (e) {}
	$('#previmgname').html(activeItem.title);
	showItemInfo(-1, "detail", activeItem, true);
}

/* Refreshes the image preview depending on the scale & crop settings. */
function refreshActiveImagePreview() {
	var scaleParams = "";
	if (initValues.scale != null && initValues.scale != "") {
		scaleParams = initValues.scale;
	}
	var imgWidth = activeItem.width;
	var imgHeight = activeItem.height;
	var useSelectedDimensions = false;
	if (activeItem.isCropped == true) {
		if (scaleParams != "") {
			scaleParams += ",";
		}
		scaleParams += activeItem.crop;
		imgWidth = getScaleValueInt(activeItem.crop, "cw");
		imgHeight = getScaleValueInt(activeItem.crop, "ch");
	}
	if (activeItem.newwidth > 0) {
		imgWidth = activeItem.newwidth;
		useSelectedDimensions = true;
	}
	if (activeItem.newheight > 0) {
		imgHeight = activeItem.newheight;
		useSelectedDimensions = true;
	}
	if (initValues.useformats != true || (initValues.useformats == true && (formatSelected.width == -1 || formatSelected.height == -1))) {
		setImageFormatFields(imgWidth, imgHeight);
	}
	var maxWidth = previewX;
	var maxHeight = previewY;
	if (initValues.useformats == true) {
		var formatWidth = formatSelected.width;
		var formatHeight = formatSelected.height;
		if (formatWidth > -1) {
			if (formatWidth < maxWidth) {
				maxWidth = formatWidth;
			}
		}
		if (formatHeight > -1) {
			if (formatHeight < maxHeight) {
				maxHeight = formatHeight;
			}
		}
	}

	var newDimensions = calculateDimensions(imgWidth, imgHeight, maxWidth, maxHeight);
	if (newDimensions.scaleFactor != 1) {
		if (scaleParams != "") {
			scaleParams += ",";
		}
		scaleParams += "w:" + newDimensions.width;
		scaleParams += ",h:" + newDimensions.height;
	} else if (useSelectedDimensions == true) {
		if (scaleParams != "") {
			scaleParams += ",";
		}
		scaleParams += "w:" + imgWidth;
		scaleParams += ",h:" + imgHeight;
	}
	var path = activeItem.linkpath;
	if (scaleParams != "") {
		scaleParams = "?__scale=" + scaleParams;
	}
	$("#imgpreview").html("<img src=\"" + path + scaleParams + "\" />");
}

/* Marks the currently selected image. */
function markItem(imgIndex, idPrefix) {
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
	if (markedIndex != imgIndex) {
		$("#" + idPrefix + "item" + markedIndex).attr("class", "imgitem");
		$("#" + idPrefix + "item" + imgIndex).attr("class", "imgitemactive");
		markedIndex = imgIndex;
	} else {
		$("#" + idPrefix + "item" + imgIndex).attr("class", "imgitem");
		markedIndex = -1;
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
			// user has write permission for this resource
			if (hasWritePermission == true) { 
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
				    tooltip  : LANG.IMGDETAIL_EDIT_HELP
				});
				// Delete
				$("#" + idPrefix + "itemdeletebutton").fadeIn("fast");	

			} else {
				$("#" + idPrefix + "itemtitle").unbind();
				$("#" + idPrefix + "itemtitle").removeClass();
				// Delete
				$("#" + idPrefix + "itemdeletebutton").show();
				// Delete
			}
			// user has direct publish permission for this resource
			if (state != 0 && hasDirectPublish == true) {
				$("#" + idPrefix + "itempublishbutton").show();
			} else {
				$("#" + idPrefix + "itempublishbutton").fadeOut("fast");
			}
		} else {
			$("#" + idPrefix + "itemtitle").unbind();
			$("#" + idPrefix + "itemtitle").removeClass();
			$("#" + idPrefix + "itempublishbutton").fadeOut("fast");
			// Delete
			$("#" + idPrefix + "itemdeletebutton").fadeOut("fast");
			// Delete
		}
		
		showItemInfo(markedIndex, idPrefix);
		
		if (initValues.viewonly == false) {
			$("#" + idPrefix + "itemselectbutton").show();
		}
	} else {
		$("#" + idPrefix + "itemselectbutton").fadeOut("fast");
		$("#" + idPrefix + "itemtitle").removeClass();
		$("#" + idPrefix + "itempublishbutton").fadeOut("fast");
		// Delete
		$("#" + idPrefix + "itemdeletebutton").fadeOut("fast");
		// Delete
	}
}

/* Refreshes the marked image. */
function refreshMarkedItem(data, modeName) {
	var state;
	var imgIndex;
	// direct publish permissions for given resource
	var hasDirectPublish;
	var newImg = eval("(" + data + ")");
	if (activeItem != null && activeItem.sitepath == newImg.sitepath) {
		// update the image preview with the new image information
		newImg.newwidth = activeItem.newwidth;
		newImg.newheight = activeItem.newheight;
		if (activeItem.isCropped == true) {
			newImg.isCropped = true;
			newImg.crop = activeItem.crop;
			newImg.cropx = activeItem.cropx;
			newImg.cropy = activeItem.cropy;
			newImg.cropw = activeItem.cropw;
			newImg.croph = activeItem.croph;
		}
		activeItem = newImg;
		showItemInfo(-1, "detail", activeItem, true);
	}
	if (modeName == "gallery") {
		imgIndex = galleryItems.markedItem;
		galleryItems.items[imgIndex] = newImg;
		state = galleryItems.items[imgIndex].state;
		// direct publish permissions for given resource
		hasDirectPublish = galleryItems.items[imgIndex].directpublish;
		// direct publish permissions for the gallery folder
		if (galleryItems.directpublish == true) {
			$("#gallerypublishbutton").removeClass("ui-state-disabled");	
			$("#gallerypublishbutton").get(0).disabled = false;
		}
		
	} else {
		imgIndex = categoryItems.markedItem;
		categoryItems.items[imgIndex] = newImg;
		state = categoryItems.items[imgIndex].state;
		// direct publish permissions for given resource
		hasDirectPublish = categoryItems.items[imgIndex].directpublish;	
	}
	if (state == 1 || state == 2) {
		// show the resource publish button, if user has direct publish permission
		if (hasDirectPublish == true) {
			$("#" + modeName + "itempublishbutton").show();
		}
		$("#" + modeName + "itemlayer" + imgIndex).empty();
		var imgHtml = "";
		if (state == 1) {
			// changed image
			imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_chg.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_STATE_CHANGED;
			imgHtml += "\" />";
		} else {
			// new image
			imgHtml += "<img src=\"" + vfsPathPrefixItems + "img_new.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_STATE_NEW;
			imgHtml += "\" />";
		}
		$("#" + modeName + "itemlayer" + imgIndex).append(imgHtml);
	} else {
		$("#" + modeName + "itemlayer" + imgIndex).empty();
		$("#" + modeName + "itempublishbutton").fadeOut("slow");
	}
	showItemInfo(imgIndex, modeName);
}

/* Removes the specified scale parameter value. */
function removeScaleValue(scale, valueName) {
	if (scale == null) {
		return null;
	}
	var pos = scale.indexOf(valueName + ":");
	if (pos != -1) {
		// found value, remove it from scale string
		var scalePrefix = "";
		if (pos > 0 && (valueName == "h" || valueName == "w")) {
			// special handling for "w" and "h", could also match "cw" and "ch"
			if (scale.charAt(pos - 1) == "c") {
				scalePrefix = scale.substring(0, pos + 1);
				scale = scale.substring(pos + 1);
			}
		}
		if (scale.indexOf(valueName + ":") != -1) {
			var searchVal = new RegExp(valueName + ":\\d+,*", "");
			scale = scale.replace(searchVal, "");
		}
		scale = scalePrefix + scale;
	}
	return scale;
}

/* Returns the value of the specified scale parameter. */
function getScaleValue(scale, valueName) {
	if (scale == null) {
		return "";
	}
	var pos = scale.indexOf(valueName + ":");
	if (pos != -1) {
		// found value, return it
		if (pos > 0 && (valueName == "h" || valueName == "w")) {
			// special handling for "w" and "h", could also match "cw" and "ch"
			if (scale.charAt(pos - 1) == "c") {
				scale = scale.substring(pos + 1);
			}
		}
		var searchVal = new RegExp(valueName + ":\\d+,*", "");
		var result = scale.match(searchVal);
		if (result != null && result != "") {
			result = result.toString().substring(valueName.length + 1);
			if (result.indexOf(",") != -1) {
				result = result.substring(0, result.indexOf(","));
			}
			return result;
		}
	}
	return "";
}

/* Returns the integer value of the specified scale parameter. */
function getScaleValueInt(scale, valueName) {
	try {
		return parseInt(getScaleValue(scale, valueName));
	} catch (e) {
		return 0;
	}
}

/* Calculates the image dimensions that can be shown in the preview area. */
function calculateDimensions(imgWidth, imgHeight, maxWidth, maxHeight) {
	var newWidth = imgWidth;
	var newHeight = imgHeight;
	var scaleFactor = 1;
	if (imgWidth > maxWidth || imgHeight > maxHeight) {
		if (imgWidth > maxWidth) {
			newWidth = maxWidth;
			scaleFactor = imgWidth / newWidth;
			newHeight = imgHeight / scaleFactor;
		}
		if (newHeight > maxHeight) {
			var tempHeight = newHeight;
			newHeight = maxHeight;
			scaleFactor = tempHeight / newHeight;
			newWidth = newWidth / scaleFactor;
		}
		scaleFactor = imgHeight / newHeight;
	}
	return new Object({"width": Math.round(newWidth), "height": Math.round(newHeight), "scaleFactor": scaleFactor});
}

/* Sets the width and height format fields of the preview image. */
function setImageFormatFields(imgWidth, imgHeight) {
	$("#txtWidth").get(0).value = imgWidth;
	$("#txtHeight").get(0).value = imgHeight;
}


/* Toggles the preview image information. */
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

/* Determines the image type, e.g. gif or jpg image */
function getImageType(imgSuffix) {
	if (imgSuffix == null || imgSuffix .length == 0) {
		return "";
	}
	if (imgSuffix == "gif") {
		return "GIF";
	}
	if (imgSuffix == "jpg" || imgSuffix == "jpeg") {
		return "JPEG";
	}
	if (imgSuffix == "png") {
		return "PNG";
	}
	if (imgSuffix == "tif" || imgSuffix == "tiff") {
		return "TIFF";
	}
	if (imgSuffix == "bmp") {
		return "BMP";
	}
	return "Unknown";
}
