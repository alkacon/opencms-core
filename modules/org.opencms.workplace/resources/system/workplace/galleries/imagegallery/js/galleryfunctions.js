/* Represents an image list to use (either for category or gallery view). */
var ImageList = function() {
	this.images = null;
	this.markedImage = -1;
	this.currentPage = 0;
	this.publishable = false;
}

/* Global variables. */
var categories;
var categoryImages;
var galleries;
var galleryImages;
var activeGallery = -1;
var activeCategory = -1;
var activeImage;
var galleriesLoaded = false;
var categoriesLoaded = false;
var startGallery = null;

/* Stores the hide image info timeout to interrupt it on a mouseover. */
var imgDetailTimeout;

var previewX = 600;
var previewY = 450;

var itemsPerPage = 16;

$(document).ready(function(){
	$tabs = $("#tabs").tabs({
	});
	initPopup();
	initFormatSelectBox();
});

/* Collects all available categories using a post request. */
function getCategories() {
	$.post(vfsPathAjaxJsp, { action: "getcategories", resource: initValues.editedresource}, function(data){ fillCategories(data); });
}

/* Creates the HTML for the categories from the given JSON array data. */
function fillCategories(data) {
	categories = eval(data);
	for (var i = 0; i < categories.length; i++) {
		var currCat = categories[i];
		var actClass = "";
		if (currCat.active == true) {
			// this is the active gallery, get the images for this gallery
			getImages(currCat.path, true);
			activeCategory = i;
			actClass = " class=\"active\"";
		}
		var actStyle = "";
		if (currCat.level > 0) {
			actStyle = " style=\"margin-left: " + (currCat.level * 20) + "px;\"";
		}
		$("#categoryfolderlist").append("<div id=\""
			+ "category" + i
			+ "\""
			+ actClass
			+ actStyle
			+ " onclick=\"selectCategory('"
			+ currCat.path
			+ "', "
			+ i
			+ ");\">"
			+ "<span class=\"title\">"
			+ currCat.title
			+ "</span><span class=\"path\">"
			+ currCat.path
			+ "</span></div>"
		);
	}
	categoriesLoaded = true;
}

/* Collects all available galleries using a post request. */
function getGalleries(showActiveGallery) {
	$.post(vfsPathAjaxJsp, { action: "getgalleries" }, function(data){ fillGalleries(data, showActiveGallery); });
}

/* Creates the HTML for the galleries from the given JSON array data. */
function fillGalleries(data, showActiveGallery) {
	galleries = eval(data);
	for (var i = 0; i < galleries.length; i++) {
		var currGall = galleries[i];
		var actClass = "";
		if (currGall.active == true) {
			// this is the active gallery, set active class
			actClass = " class=\"active\"";
		}
		$("#galleryfolderlist").append("<div id=\""
			+ "gallery" + i
			+ "\""
			+ actClass
			+ " onclick=\"selectGallery('"
			+ currGall.path
			+ "', "
			+ i
			+ ");\">"
			+ "<span class=\"title\">"
			+ currGall.title
			+ "</span><span class=\"path\">"
			+ currGall.path
			+ "</span></div>"
		);
		if ((showActiveGallery == null || showActiveGallery == true) && currGall.active == true) {
			// this is the active gallery, get the images for this gallery
			selectGallery(currGall.path, i);
		} else if (currGall.active == true) {
			activeGallery = i;
		}
	}
	galleriesLoaded = true;
}

/* Refreshes the currently selected gallery, e.g. after an image upload. */
function refreshGallery() {
	if (activeGallery != -1) {
		getImages(galleries[activeGallery].path, true);
		$("#galleryimagetitle").removeClass();
	} else if (startGallery != null) {
		getImages(startGallery.path, true);
		$("#galleryimagetitle").removeClass();
	}
}

/* Selects the active gallery, is triggered when clicking on the gallery name. */
function selectGallery(vfsPath, galleryIndex) {
	getImages(vfsPath, true);
	if (galleryIndex != -1 && galleryIndex != activeGallery) {
		$("#gallery" + galleryIndex).addClass("active");
		if (activeGallery != -1) {
			$("#gallery" + activeGallery).removeClass();
		}
		activeGallery = galleryIndex;
	}
	// fill required data in upload link
	var uploadLink = "upload.jsp?gallery=";
	uploadLink += vfsPath;
	uploadLink += "&amp;TB_iframe=true&amp;width=480&amp;height=95&amp;modal=true";
	$("#galleryimageuploadlink").attr("href", uploadLink);
	// fill required data in publish link
	$("#gallerypublishlink").attr("href", createPublishLink(vfsPath));
	$("#galleryfolders").slideUp("fast", function() { $("#galleryimages").slideDown("fast"); });
	$("#galleryimagetitle").removeClass();
}

/* Shows the gallery folder list and hides the gallery images. */
function showGalleryFolders() {
	$("#galleryimages").slideUp("fast", function() { $("#galleryfolders").slideDown("fast"); });
}

/* Refreshes the currently selected category, e.g. after a publish event. */
function refreshCategory() {
	if (activeCategory != -1) {
		getImages(categories[activeCategory].path, false);
		$("#categoryimagetitle").removeClass();
	}
}

/* Selects the active category, is triggered when clicking on the category name. */
function selectCategory(vfsPath, categoryIndex) {
	getImages(vfsPath, false);
	if (categoryIndex != activeCategory) {
		$('#category' + categoryIndex).addClass('active');
		if (activeCategory != -1) {
			$('#category' + activeCategory).removeClass('active');
		}
		activeCategory = categoryIndex;
	}
	$("#categoryfolders").slideUp("fast", function() { $("#categoryimages").slideDown("fast"); });
	$("#categoryimagetitle").removeClass();
}

/* Shows the category folder list and hides the category images. */
function showCategoryFolders() {
	$("#categoryimages").slideUp("fast", function() { $("#categoryfolders").slideDown("fast"); });
}

/* Collects the images of a gallery or a category. */
function getImages(vfsPath, isGallery) {
	var modeName;
	if (isGallery == true) {
		modeName = "gallery";
	} else {
		modeName = "category";
	}
	$("#" + modeName + "imagelist").empty();
	$.post(vfsPathAjaxJsp, { action: "getimages", gallerypath: vfsPath, listmode: modeName, resource: initValues.editedresource}, function(data) { fillImages(data, modeName) });
}

/* Creates the HTML for the images from the given JSON array data. */
function fillImages(data, modeName) {
	currentPage = 0;
	var foundImages = eval(data);
	var publishInfo = foundImages.shift();
	if (modeName == "category") {
		categoryImages = new ImageList();
		categoryImages.images = foundImages;
		$("#" + modeName + "imagelist").append("<div id=\"categoryname\">"
			+ categories[activeCategory].title
			+ "<span>"
			+ categories[activeCategory].path
			+ "</span></div>");
	} else {
		galleryImages = new ImageList();
		galleryImages.images = foundImages;
		galleryImages.publishable = publishInfo.publishable;
		var gTitle, gPath;
		if (activeGallery != -1) {
			gTitle = galleries[activeGallery].title;
			gPath = galleries[activeGallery].path;
		} else {
			gTitle = startGallery.title;
			gPath = startGallery.path;
		}
		$("#gallerypublishbutton").get(0).disabled = !galleryImages.publishable;
		$("#" + modeName + "imagelist").append("<div id=\"galleryname\">"
			+ gTitle
			+ "<span>"
			+ gPath
			+ "</span></div>");
	}
	var innerListId = modeName + "imagelistinner";
	$("#" + modeName + "imagelist").append("<div class=\"imagelist\" id=\"" + innerListId  + "\"></div>");
	innerListId = "#" + innerListId;
	var paginationId = modeName + "imagepage";
	$(innerListId).hide();
	for (var i = 0; i < foundImages.length; i++) {
		var page = Math.floor(i / itemsPerPage);
		if ((i + 1) % itemsPerPage == 1) {
			$(innerListId).append("<div id=\"" + paginationId + "" + page + "\"></div>");
			if (page > 0) {
				$("#" + paginationId + page).hide();
			}
		}
		var image = foundImages[i];
		var mouseAttrsClick = "";
		mouseAttrsClick += " onclick=\"markImage(";
		mouseAttrsClick += i + ", \'";
		mouseAttrsClick += modeName;
		mouseAttrsClick += "\');\"";
		mouseAttrsClick += " ondblclick=\"setActiveImage(";
		mouseAttrsClick += i + ", \'";
		mouseAttrsClick += modeName;
		mouseAttrsClick += "\');\"";
		var mouseAttrs = "";
		mouseAttrs += " onmouseover=\"showImageInfo(";
		mouseAttrs += i + ", \'";
		mouseAttrs += modeName;
		mouseAttrs += "\');\" onmouseout=\"hideImageInfo(";
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
			imgHtml += "<img src=\"" + vfsPathPrefixImages + "img_chg.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_STATE_CHANGED;
			imgHtml += "\" />";
		} else if (image.state == 2) {
			// new image
			imgHtml += "<img src=\"" + vfsPathPrefixImages + "img_new.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_STATE_NEW;
			imgHtml += "\" />";
		}
		if (image.lockedby != "") {
			// image is locked by other user
			imgHtml += "<img src=\"" + vfsPathPrefixImages + "img_locked.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_LOCKSTATE_LOCKED.replace(/%\(user\)/, image.lockedby);
			imgHtml += "\" />";
		}
		imgHtml += "</span>";
		// show the thumb image
		imgHtml += "<img class=\"imgthumb\" alt=\"";
		imgHtml += image.width + " x " + image.height + "<br/>" + LANG.IMGDETAIL_SIZE + " " + image.size;
		imgHtml += "\" alt=\"\" title=\"\" src=\"";
		imgHtml += image.scalepath;
		imgHtml += "\"";
		imgHtml += mouseAttrs + mouseAttrsClick;
		imgHtml += "/>";
		imgHtml += "</div></div>";
		$("#" + paginationId + page).append(imgHtml);

	}
	$(innerListId + " img.imgthumb").jHelperTip({trigger: "hover", source: "attribute", attrName: "alt", opacity: 0.75});
	$(innerListId).append("<div style=\"clear: left;\"></div>");
	if (foundImages.length + 1 > itemsPerPage) {
		$("#" + modeName + "imagelist").append("<div id=\"image" + modeName + "-paginationwrapper\"><span id=\"image" + modeName + "-pagination\"></span></div>");
		if (modeName == "gallery") {
			$("#image" + modeName + "-pagination").pagination(foundImages.length, {
				items_per_page: itemsPerPage,
				callback: showImagePagegallery,
				prev_text: LANG.PAGINATION_PREVIOUS,
				next_text: LANG.PAGINATION_NEXT,
				prev_show_always: false,
				next_show_always: false,
				num_edge_entries: 1
			});
		} else {
			$("#image" + modeName + "-pagination").pagination(foundImages.length, {
				items_per_page: itemsPerPage,
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

/* Callback function of the pagination, shows the clicked page. */
function showImagePagegallery(page_id, jq) {
	$("#galleryimagepage" + galleryImages.currentPage).hide();
	$("#galleryimagepage" + page_id).fadeIn("fast");
	galleryImages.currentPage = page_id;
}

/* Callback function of the pagination, shows the clicked page. */
function showImagePagecategory(page_id, jq) {
	$("#categoryimagepage" + categoryImages.currentPage).hide();
	$("#categoryimagepage" + page_id).fadeIn("fast");
	categoryImages.currentPage = page_id;
}


/* Sets the active image that is clicked in the image list. */
function setActiveImage(imgIndex, modeName) {
	if (initValues.viewonly == true) {
		return;
	}
	var sitePath;
	if (modeName == "category") {
		sitePath = categoryImages.images[imgIndex].sitepath;
	} else {
		sitePath = galleryImages.images[imgIndex].sitepath;
	}
	$.post(vfsPathAjaxJsp, { action: "getactiveimage", imagepath: sitePath}, function(data){ loadActiveImage(data, false); });
}

/* Loads the active image in the preview tab and sets the parameters */
function loadActiveImage(data, isInitial) {
	if (data == "none") {
		return;
	} 
	activeImage = eval("(" + data + ")");
	activeImage.newwidth = 0;
	activeImage.newheight = 0;
	if (isInitial == true) {
		// initial image loaded
		var cropIt = false;
		if (getScaleValue(initValues.scale, "cx") != "") {
			cropIt = true;
			activeImage.cropx = getScaleValueInt(initValues.scale, "cx");
			activeImage.cropy = getScaleValueInt(initValues.scale, "cy");
			activeImage.cropw = getScaleValueInt(initValues.scale, "cw");
			activeImage.croph = getScaleValueInt(initValues.scale, "ch");
			var cropParams = "cx:" + activeImage.cropx;
			cropParams += ",cy:" + activeImage.cropy;
			cropParams += ",cw:" + activeImage.cropw;
			cropParams += ",ch:" + activeImage.croph;
			activeImage.crop = cropParams;
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
						var newHeight = Math.round(activeImage.croph / (activeImage.cropw / initValues.imgwidth));
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
					var newWidth = Math.round(activeImage.cropw / (activeImage.croph / initValues.imgheight));
					if (newWidth != parseInt($("#txtWidth").get(0).value)) {
						setLockRatio(false);
						$("#txtWidth").get(0).value = newWidth;
						onSizeChanged("Width", newHeight, false, false);
					}
				}
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
				activeImage.newwidth = newWidth;
				sizeChanged = true;
			}
			if (initValues.imgheight != "") {
				var newHeight = parseInt(initValues.imgheight);
				$("#txtHeight").get(0).value = newHeight;
				activeImage.newheight = newHeight;
				sizeChanged = true;
			}
			if (sizeChanged == true) {
				var testW = activeImage.newwidth > 0 ? activeImage.newwidth : activeImage.width;
				var testH = activeImage.newheight > 0 ? activeImage.newheight : activeImage.height;
				var delta = testW / activeImage.width;
				var calH = Math.round(activeImage.height * delta);
				if (calH != testH) {
					setLockRatio(false);
				}
			}
			if (cropIt == true) {
				setCropActive(true);
			} else {
				setCropActive(false);
			}
			// refresh the format select box
			refreshSelectBox();
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
	}
	try {
		// do additional stuff with the active image if necessary
		activeImageAdditionalActions(isInitial);
	} catch (e) {}
	$('#previmgname').html(activeImage.title);
	showImageInfo(-1, "detail", activeImage, true);
}

/* Refreshes the image preview depending on the scale & crop settings. */
function refreshActiveImagePreview() {
	var scaleParams = "";
	if (initValues.scale != null && initValues.scale != "") {
		scaleParams = initValues.scale;
	}
	var imgWidth = activeImage.width;
	var imgHeight = activeImage.height;
	var useSelectedDimensions = false;
	if (activeImage.isCropped == true) {
		if (scaleParams != "") {
			scaleParams += ",";
		}
		scaleParams += activeImage.crop;
		imgWidth = getScaleValueInt(activeImage.crop, "cw");
		imgHeight = getScaleValueInt(activeImage.crop, "ch");
	}
	if (activeImage.newwidth > 0) {
		imgWidth = activeImage.newwidth;
		useSelectedDimensions = true;
	}
	if (activeImage.newheight > 0) {
		imgHeight = activeImage.newheight;
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
	var path = activeImage.linkpath;
	if (scaleParams != "") {
		scaleParams = "?__scale=" + scaleParams;
	}
	$("#imgpreview").html("<img src=\"" + path + scaleParams + "\" />");
}

/* Marks the currently selected image. */
function markImage(imgIndex, idPrefix) {
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	try {
		if (idPrefix == "category") {
			markedIndex = categoryImages.markedImage;
		} else {
			markedIndex = galleryImages.markedImage;
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
		categoryImages.markedImage = markedIndex;
	} else {
		galleryImages.markedImage = markedIndex;
	}
	if (markedIndex != -1) {
		var isEditable;
		var state = 0;
		if (idPrefix == "gallery") {
			isEditable = galleryImages.images[markedIndex].editable;
			state = galleryImages.images[markedIndex].state;
		} else {
			isEditable = categoryImages.images[markedIndex].editable;
			state = categoryImages.images[markedIndex].state;
		}
		if (isEditable == true) {
			$("#" + idPrefix + "imagetitle").attr("class", "editable");
			$("#" + idPrefix + "imagetitle").editable(function(value, settings) {
			    setImageTitle(value, idPrefix);
			    return(value);
			 }, {
			    submit   : LANG.BUTTON_OK,
			    cancel   : LANG.BUTTON_CANCEL,
			    cssclass : "edittitle",
			    height   : "none",
			    select   : true,
			    tooltip  : LANG.IMGDETAIL_EDIT_HELP
			});
			if (state != 0) {
				$("#" + idPrefix + "imagepublishbutton").fadeIn();
			} else {
				$("#" + idPrefix + "imagepublishbutton").fadeOut();
			}
		} else {
			$("#" + idPrefix + "imagetitle").unbind();
			$("#" + idPrefix + "imagetitle").removeClass();
			$("#" + idPrefix + "imagepublishbutton").fadeOut();
		}
		showImageInfo(markedIndex, idPrefix);
		if (initValues.viewonly == false) {
			$("#" + idPrefix + "imageselectbutton").fadeIn();
		}
	} else {
		$("#" + idPrefix + "imageselectbutton").fadeOut();
		$("#" + idPrefix + "imagetitle").removeClass();
		$("#" + idPrefix + "imagepublishbutton").fadeOut();
	}
}

/* Shows the additional image information (called on mouseover or on preview tab). */
function showImageInfo(imgIndex, idPrefix, currImg, showAll) {
	clearTimeout(imgDetailTimeout);
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	if (imgIndex != -1 && currImg == null) {
		if (idPrefix == "category") {
			currImg = categoryImages.images[imgIndex];
			markedIndex = categoryImages.markedImage;
		} else {
			currImg = galleryImages.images[imgIndex];
			markedIndex = galleryImages.markedImage;
		}
		if (markedIndex != imgIndex) {
			$("#" + idPrefix + "item" + imgIndex).attr("class", "imgitemhover");
		}
	}
	if (markedIndex == -1 || markedIndex == imgIndex) {
		idPrefix = idPrefix + "image";
		if (idPrefix.indexOf("#") != 0) {
			idPrefix = "#" + idPrefix;
		}
		var imgName = "<span title=\"";
		imgName += currImg.linkpath;
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
function hideImageInfo(imgIndex, idPrefix) {
	if (idPrefix == null) {
		idPrefix = "gallery";
	}
	var markedIndex = -1;
	if (idPrefix == "category") {
		markedIndex = categoryImages.markedImage;
	} else {
		markedIndex = galleryImages.markedImage;
	}
	if (markedIndex != imgIndex) {
		$("#" + idPrefix + "item" + imgIndex).attr("class", "imgitem");
	}
	if (markedIndex == -1) {
		imgDetailTimeout = setTimeout("doHideImageInfo(\'" + idPrefix + "\')", 350);
	}
}

/* Really hides the additional image information. */
function doHideImageInfo(idPrefix) {
	$("#" + idPrefix + "imagestate").html("&nbsp;");
	$("#" + idPrefix + "imagename").html("&nbsp;");
	$("#" + idPrefix + "imagetitle").html("&nbsp;");
	$("#" + idPrefix + "imagetype").html("&nbsp;");
	$("#" + idPrefix + "imagedc").html("&nbsp;");
	$("#" + idPrefix + "imagedm").html("&nbsp;");
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

/* Sets the new title property value of the marked image. */
function setImageTitle(newTitle, modeName) {
	var checkImage;
	if (modeName == "gallery") {
		checkImage = galleryImages.images[galleryImages.markedImage];
	} else {
		checkImage = categoryImages.images[categoryImages.markedImage];
	}
	if (checkImage.title != newTitle) {
		$.post(vfsPathAjaxJsp, { action: "changeimagetitle", imagepath: checkImage.sitepath, propertyvalue: newTitle}, function(data){ refreshMarkedImage(data, modeName); });
	}
}

/* Refreshes the marked image. */
function refreshMarkedImage(data, modeName) {
	var state;
	var imgIndex;
	var newImg = eval("(" + data + ")");
	if (activeImage != null && activeImage.sitepath == newImg.sitepath) {
		// update the image preview with the new image information
		newImg.newwidth = activeImage.newwidth;
		newImg.newheight = activeImage.newheight;
		if (activeImage.isCropped == true) {
			newImg.isCropped = true;
			newImg.crop = activeImage.crop;
			newImg.cropx = activeImage.cropx;
			newImg.cropy = activeImage.cropy;
			newImg.cropw = activeImage.cropw;
			newImg.croph = activeImage.croph;
		}
		activeImage = newImg;
		showImageInfo(-1, "detail", activeImage, true);
	}
	if (modeName == "gallery") {
		imgIndex = galleryImages.markedImage;
		galleryImages.images[imgIndex] = newImg;
		state = galleryImages.images[imgIndex].state;
		$("#gallerypublishbutton").get(0).disabled = false;
	} else {
		imgIndex = categoryImages.markedImage;
		categoryImages.images[imgIndex] = newImg;
		state = categoryImages.images[imgIndex].state;
	}
	if (state == 1 || state == 2) {
		$("#" + modeName + "itemlayer" + imgIndex).empty();
		$("#" + modeName + "imagepublishbutton").fadeIn();
		var imgHtml = "";
		if (state == 1) {
			// changed image
			imgHtml += "<img src=\"" + vfsPathPrefixImages + "img_chg.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_STATE_CHANGED;
			imgHtml += "\" />";
		} else {
			// new image
			imgHtml += "<img src=\"" + vfsPathPrefixImages + "img_new.png\" alt=\"\" title=\"";
			imgHtml += LANG.IMGITEM_STATE_NEW;
			imgHtml += "\" />";
		}
		$("#" + modeName + "itemlayer" + imgIndex).append(imgHtml);
	} else {
		$("#" + modeName + "itemlayer" + imgIndex).empty();
		$("#" + modeName + "imagepublishbutton").fadeOut();
	}
	showImageInfo(imgIndex, modeName);
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

/* Creates the publish link to open the publish dialog with the given VFS path. */
function createPublishLink(vfsPath) {
	var publishLink = "publish.jsp?resource=";
	publishLink += vfsPath;
	publishLink += "&amp;TB_iframe=true&amp;width=600&amp;height=550&amp;modal=true";
	return publishLink;
}

/* Publishes the specified image. */
function publishImage(imgIndex, modeName) {
	var sitePath;
	if (modeName == "category") {
		sitePath = categoryImages.images[imgIndex].sitepath;
	} else {
		sitePath = galleryImages.images[imgIndex].sitepath;
	}
	$("#resourcepublishlink").attr("href", createPublishLink(sitePath));
	$("#resourcepublishlink").click();
}