<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>

<head>
<!--[if IE 7]>
  <link rel="stylesheet" type="text/css" href="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/galleries/css/imgselector_hack_ie7.css" />
  <![endif]-->
<link rel="stylesheet" type="text/css" href="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/galleries/css/crop.css" />
<link rel="stylesheet" type="text/css" href="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>jquery/css/ui-ocms/jquery.ui.css" />
<link rel="stylesheet" type="text/css" href="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>jquery/css/ui-ocms/jquery.ui.ocms.css" />
<script type="text/javascript" src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>jquery/packed/jquery.js"></script>
<script type="text/javascript" src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>jquery/packed/jquery.dimensions.js"></script>
<script type="text/javascript" src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/galleries/js/jquery.imgareaselect.min.js"></script>
<script type="text/javascript">
	var imgPreviewHeight = 450;
	if (parent.initValues.dialogmode == "editor") {
		imgPreviewHeight = 390;
	}

	/* The image to crop. */
	var img = parent.activeItem;
	
	/* The scale factor of the image. Initial value of 1 means that now downscaling is necessary. */
	var scaleFactor = 1;

	/* These variables store the DOM elements containing the selection information data. */
	var $x1, $y1, $w, $h;
	
	/* The values of the image selection. */
	var selX, selY, selW, selH;
	
	/* The initialization coordinates. */
	var initCoords;
	
	/* The flags for a variable width/height setting. */
	var variableWidth = false;
	var variableHeight = false;
	var targetSizeW = -1;
	var targetSizeH = -1;
 
        /* Callback function that is triggered every time the selection changes. */
	function selectChange(cropImg, selection) {
		// recalculate real selection values using the scale factor
		selX = Math.round(selection.x1 * scaleFactor);
		selY = Math.round(selection.y1 * scaleFactor);
		selW = Math.round(selection.width * scaleFactor);
		selH = Math.round(selection.height * scaleFactor);
		
		// assure that selection dimensions are correct and do not exceed image dimensions
		if ((selX + selW) > img.width) {
			selW = img.width - selX;
		}
		if ((selY + selH) > img.height) {
			selH = img.height - selY;
		}
		
		// adjust the selection dimensions to show the result size
		var shownW = selW;
		var shownH = selH;
		var factor = 1;
		if (variableWidth == true) {
			factor = selH / targetSizeH;
			shownW = Math.round(selW / factor);
			shownH = Math.round(selH / factor);
		} else if (variableHeight == true) {
			factor = selW / targetSizeW;
			shownW = Math.round(selW / factor);
			shownH = Math.round(selH / factor);
		} else if (parent.formatSelected.type != "free") {
			shownW = targetSizeW;
			shownH = targetSizeH;
		}
		
		// display the updated information
		$w.text(shownW);
		$h.text(shownH);
		//$w.text(shownW + ", W: " + selW + ", X: " + selX);
		//$h.text(shownH + ", H: " + selH + ", Y: " + selY);
		updateRatio();
	}
	
	/* Updates the crop ratio, which is an indicator for the result image quality. */
	function updateRatio() {
		var result = 0;
		if (parent.formatSelected.type == "free") {
			result = 100;
		} else {
			var sum = 0;
			if (variableWidth == true) {
				sum += (selH / targetSizeH) * 100;
			} else if (variableHeight == true) {
				sum += (selW / targetSizeW) * 100;
			} else {
				sum += (selW / targetSizeW) * 100;
				sum += (selH / targetSizeH) * 100;
				sum = sum / 2;
			}
			result = Math.round(sum);
		}
		$r.text(result + " %");
	}
	
	/* Sets the updated image information and closes the thickbox iframe. */
	function okPressed() {
		if (!isNaN(selX)) {
			if (variableWidth == true) {
				var factor = selH / targetSizeH;
				parent.activeItem.newwidth = Math.round(selW / factor); 
			} else if (variableHeight == true) {
				var factor = selW / targetSizeW;
				parent.activeItem.newheight = Math.round(selH / factor);
			} else if (parent.formatSelected.type != "free") {
				parent.activeItem.newwidth = targetSizeW;
				parent.activeItem.newheight = targetSizeH;
			}
			var cropParams = "cx:" + selX;
			cropParams += ",cy:" + selY;
			cropParams += ",cw:" + selW;
			cropParams += ",ch:" + selH;
			parent.activeItem.crop = cropParams;
			parent.activeItem.cropx = selX;
			parent.activeItem.cropy = selY;
			parent.activeItem.cropw = selW;
			parent.activeItem.croph = selH;
			if (parent.formatSelected.type == "free") {
				parent.activeItem.newwidth = selW;
				parent.activeItem.newheight = selH;
			}
			parent.setCropActive(true, true);
			//alert(cropParams);
		}
		parent.tb_remove();
	}
	
	/* Checks is the image is loaded, after successful load, activate the selector. */
	function checkImage() {
		if (!document.images[0].complete) {
			// image not yet loaded, check again
			setTimeout("checkImage();", 50);
		} else {
			// image loaded, now initialize image area selector
			setTimeout("$('#cropimg').imgAreaSelect({ selectionOpacity: 0, onSelectChange: selectChange" + initCoords + " });", 50);
		}
	}
	
	/* Buttons with ui jquery */
	function uiButtons() {
		$("button").addClass("ui-button ui-state-default ui-corner-all")
			.hover(
			function(){ 
				$(this).addClass("ui-state-hover"); 
			},
			function(){ 
				$(this).removeClass("ui-state-hover"); 	
			});
	}

	$(document).ready(function () {
		// show the image to crop, downscale it if necessary
		var srcAttr = img.linkpath;
		$(".imgbg").css("height", imgPreviewHeight + "px");
		var newDimensions = parent.calculateDimensions(img.width, img.height, 600, imgPreviewHeight);
		scaleFactor = newDimensions.scaleFactor;
		if (scaleFactor != 1) {
			srcAttr += "?__scale=w:" + Math.round(newDimensions.width) + ",h:" + Math.round(newDimensions.height) + ",c:transparent,q:70";
		}
		$("#cropimg").attr("src", srcAttr);
		// store the information DOM elements in variables
		$w = $('#w');
		$h = $('#h');
		$r = $('#r');
		// get target sizes
		var width = img.newwidth <= 0 ? img.width : img.newwidth;
		var height = img.newheight <= 0 ? img.height : img.newheight;
		targetSizeW = width;
		targetSizeH = height;
		// check if variable width or height should be used for format selection
		if (parent.formatSelected.type != "free" && parent.formatSelected.type != "user") {
			if (parent.formatSelected.height == -1) {
				// variable height
				variableHeight = true;
			} else if (parent.formatSelected.width == -1) {
				// variable width
				variableWidth = true;
			}
		}
		// calculate initialization coordinates
		initCoords = "";
		if (img.isCropped == true) {
			// the image is already cropped, show the selection from start
			selX = img.cropx;
			selY = img.cropy;
			selW = img.cropw;
			selH = img.croph;
			var factor = 1;
			var shownW = selW;
			var shownH = selH;
			if (variableHeight == true) {
				factor = selW / targetSizeW;
				shownW = Math.round(selW / factor);
				shownH = Math.round(selH / factor);
			} else if (variableWidth == true) {
				factor = selH / targetSizeH;
				shownW = Math.round(selW / factor);
				shownH = Math.round(selH / factor);
			} else if (parent.formatSelected.type != "free") {
				shownW = targetSizeW;
				shownH = targetSizeH;
			}

			// show the selection information
			$w.text(shownW);
			$h.text(shownH);
			updateRatio();
			// the selection coordinates must be calculated using the found scale factor
			initCoords += ", x1: " + Math.round(selX / scaleFactor);
			initCoords += ", y1: " + Math.round(selY / scaleFactor);
			initCoords += ", x2: " + Math.round((selX + selW) / scaleFactor);
			initCoords += ", y2: " + Math.round((selY + selH) / scaleFactor);
		} else {
			// image not yet cropped, eventually configure start parameters for format values
		}
		if ((parent.formatSelected.type != "free" && !(parent.formatSelected.width == -1 || parent.formatSelected.height == -1)) || parent.formatSelected.type == "user") {
			initCoords += ", aspectRatio: \"" + width + ":" + height + "\"";
		}
		// check if image is loaded and activate selector
		checkImage();
		uiButtons();
	});
</script>
</head>
<body>

<div class="head"><%= wp.key(Messages.GUI_IMAGEGALLERY_CROP_HEADLINE_0) %></div>

<div class="imgbg">
	<div class="container">
		<img id="cropimg" src="#" />
	</div>
</div>

<div class="selection">
	<div class="width"><%= wp.key(Messages.GUI_IMAGEGALLERY_CROP_WIDTH_0) %> <span id="w"></span></div>
	<div class="height"><%= wp.key(Messages.GUI_IMAGEGALLERY_CROP_HEIGHT_0) %> <span id="h"></span></div>
	<div class="ratio"><%= wp.key(Messages.GUI_IMAGEGALLERY_CROP_RATIO_0) %> <span id="r"></span></div>
</div>

<div class="buttons">
	<button onclick="okPressed();"><%= wp.key(Messages.GUI_GALLERY_BUTTON_OK_0) %></button>
	<button onclick="parent.tb_remove();"><%= wp.key(Messages.GUI_GALLERY_BUTTON_CANCEL_0) %></button>
</div>

</body>
</html>