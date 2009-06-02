/* Represents an items list to use (either for category or gallery view). */
var ItemList = function() {
	this.items = null;
	this.markedItem = -1;
	this.currentPage = 0;
	this.publishable = false;

}

/* Global variables. */
var categories;
var categoryItems;
var galleries;
var galleryItems;
var activeGallery = -1;
var activeCategory = -1;
var activeItem;
var galleriesLoaded = false;
var categoriesLoaded = false;
var startGallery = null;

/* Path to already selected path for download gallery in widget and editor mode */
var ItemSitepath = function(){
	this.path = null;
	this.target = null;
}
var loadItemSitepath = null;

/* this flag is to be set in the appropriate gallery to "true" */
var isLinkGallery = false;
var isTableGallery = false;

/* Stores the hide image info timeout to interrupt it on a mouseover. */
var imgDetailTimeout;

var previewX = 600;
var previewY = 450;

//number of items per page for different galleries
var imagesPerPage = 16;
var itemsPerPage  = 8;

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
			// this is the active gallery, get the items for this gallery
			getItems(currCat.path, true);
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
			// this is the active gallery, get the items for this gallery
			selectGallery(currGall.path, i);
		} else if (currGall.active == true) {
			activeGallery = i;
		}
	}
	galleriesLoaded = true;
}

/* Refreshes the currently selected gallery, e.g. after an items upload. */
function refreshGallery() {
	if (activeGallery != -1) {
		getItems(galleries[activeGallery].path, true);
		$("#galleryitemtitle").removeClass();

	} else if (startGallery != null) {
		getItems(startGallery.path, true);
		$("#galleryitemtitle").removeClass();
	}
}

/* Selects the active gallery, is triggered when clicking on the gallery name. */
function selectGallery(vfsPath, galleryIndex) {

	getItems(vfsPath, true);
	if (galleryIndex != -1 && galleryIndex != activeGallery) {
		$("#gallery" + galleryIndex).addClass("active");
		if (activeGallery != -1) {
			$("#gallery" + activeGallery).removeClass();
		}
		activeGallery = galleryIndex;
	}
	if (isLinkGallery == true){
		// fill required data to create new link
		var newLink = "createlink.jsp?createlink=";
		newLink += vfsPath;
		newLink += "&amp;TB_iframe=true&amp;width=560&amp;height=480&amp;modal=true";
		$("#gallerynewlink").attr("href", newLink);
		
	} else if (isTableGallery == true) {
		// fill required data in upload link
		var uploadLink = "upload.jsp?gallery=";
		uploadLink += vfsPath;
		uploadLink += "&amp;TB_iframe=true&amp;width=560&amp;height=480&amp;modal=true";
		$("#galleryitemuploadlink").attr("href", uploadLink);
	} else {
		// fill required data in upload link
		var uploadLink = "../galleryelements/upload.jsp?gallery=";
		uploadLink += vfsPath;
		if ( isAppletUsed == true ) {
			uploadLink += "&amp;TB_iframe=true&amp;width=480&amp;height=95&amp;modal=true";
		} else {
			uploadLink += "&amp;TB_iframe=true&amp;width=560&amp;height=480&amp;modal=true";
		}
		$("#galleryitemuploadlink").attr("href", uploadLink);
	}
	// fill required data in publish link
	$("#gallerypublishlink").attr("href", createPublishLink(vfsPath));
	$("#galleryfolders").slideUp("fast", function(){ $("#galleryitems").slideDown("fast"); });
	$("#galleryitemtitle").removeClass();
}

/* Shows the gallery folder list and hides the gallery items. */
function showGalleryFolders() {
	$("#galleryitems").slideUp("fast", function() { $("#galleryfolders").slideDown("fast"); });
}

/* Refreshes the currently selected category, e.g. after a publish event. */
function refreshCategory() {
	if (activeCategory != -1) {
		getItems(categories[activeCategory].path, false);
		$("#categoryitemtitle").removeClass();
	}
}

/* Selects the active category, is triggered when clicking on the category name. */
function selectCategory(vfsPath, categoryIndex) {

	getItems(vfsPath, false);
	if (categoryIndex != activeCategory) {
		$('#category' + categoryIndex).addClass('active');
		if (activeCategory != -1) {
			$('#category' + activeCategory).removeClass('active');
		}
		activeCategory = categoryIndex;
	}
	$("#categoryfolders").slideUp("fast", function() { $("#categoryitems").slideDown("fast"); });
	$("#categoryitemtitle").removeClass();
}

/* Shows the category folder list and hides the category items. */
function showCategoryFolders() {
	$("#categoryitems").slideUp("fast", function() { $("#categoryfolders").slideDown("fast"); });
}

/* Collects the items of a gallery or a category. */
function getItems(vfsPath, isGallery) {
	var modeName;
	if (isGallery == true) {
		modeName = "gallery";
	} else {
		modeName = "category";
	}
	$("#" + modeName + "itemlist").empty();
	$.post(vfsPathAjaxJsp, { action: "getitems", gallerypath: vfsPath, listmode: modeName, resource: initValues.editedresource}, function(data) { fillItems(data, modeName) });
}

/* Sets the active item that is clicked in the item list. */
function setActiveItem(itemIndex, modeName, event) {
	if (initValues.viewonly == true) {
		return;
	}
	var sitePath;
	if (modeName == "category") {
		sitePath = categoryItems.items[itemIndex].sitepath;
		if (initValues.dialogmode == "editor") {
			modeType = "category";
		}
	} else {
		sitePath = galleryItems.items[itemIndex].sitepath;
		if (initValues.dialogmode == "editor") {
			modeType = "gallery";
		}
	}
	$.post(vfsPathAjaxJsp, { action: "getactiveitem", itempath: sitePath}, function(data){ loadActiveItem(data, event); }); // event = false normally
}

/* Callback function of the pagination, shows the clicked page. */
function showItemPagegallery(page_id, jq) {
	$("#galleryitempage" + galleryItems.currentPage).hide();
	$("#galleryitempage" + page_id).fadeIn("fast");
	galleryItems.currentPage = page_id;
}

/* Callback function of the pagination, shows the clicked page. */
function showItemPagecategory(page_id, jq) {
	$("#categoryitempage" + categoryItems.currentPage).hide();
	$("#categoryitempage" + page_id).fadeIn("fast");
	categoryItems.currentPage = page_id;
}


/* Sets the new title property value of the marked item. */
function setItemTitle(newTitle, modeName) {
	var checkItem;
	if (modeName == "gallery") {
		checkItem = galleryItems.items[galleryItems.markedItem];
	} else {
		checkItem = categoryItems.items[categoryItems.markedItem];
	}
	if (checkItem.title != newTitle) {
		$.post(vfsPathAjaxJsp, { action: "changeitemtitle", itempath: checkItem.sitepath, propertyvalue: newTitle}, function(data){ refreshMarkedItem(data, modeName); });
	}
}

/* Sets the new link url property value of the marked item. */
function setItemLinkurl(newLinkurl, modeName) {
	var checkItem;
	if (modeName == "gallery") {
		checkItem = galleryItems.items[galleryItems.markedItem];
	} else {
		checkItem = categoryItems.items[categoryItems.markedItem];
	}
	if (checkItem.pointer != newLinkurl) {
		$.post(vfsPathAjaxJsp, { action: "changeitemlinkurl", itempath: checkItem.sitepath, propertyvalue: newLinkurl}, function(data){ refreshMarkedItem(data, modeName); });
	}
}

/* Creates the publish link to open the publish dialog with the given VFS path. */
function createPublishLink(vfsPath) {
	var publishLink = "../galleryelements/publish.jsp?resource=";
	publishLink += vfsPath;
	publishLink += "&amp;TB_iframe=true&amp;width=600&amp;height=550&amp;modal=true";
	return publishLink;
}

/* Publishes the specified item. */
function publishItem(imgIndex, modeName) {
	var sitePath;
	if (modeName == "category") {
		sitePath = categoryItems.items[imgIndex].sitepath;
	} else {
		sitePath = galleryItems.items[imgIndex].sitepath;
	}
	$("#resourcepublishlink").attr("href", createPublishLink(sitePath));
	$("#resourcepublishlink").click();
}