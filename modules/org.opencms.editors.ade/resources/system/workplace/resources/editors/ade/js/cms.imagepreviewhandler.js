(function(cms){
  
///// Content Handler Function definition //////////////////////////    
    var defaultPreview = {'height': 295 , 'width': 550};
   
   /**
    * Displays the content of the preview item.
    * 
    * @param {Object} itemData server response as JSON object with preview data 
    */
   var showItemPreview = function(itemData) {                         
       // displays the html preview
       $('.preview-area').append(itemData['previewdata']['itemhtml']);
       // read the json object for the active item       
       var jsonForActiveImage = JSON.parse($('.preview-area').find("input[type='hidden']").val());
       // display the image in the preview area
       loadActiveItemPreviewArea(jsonForActiveImage, true);          
       
       // display editable properties
       cms.galleries.getContentHandler()['showEditArea'](itemData['previewdata']['properties']);       
       
       //bind click event to preview close button 
       $('#cms-preview div.close-icon').click(function() {
          if ($(this).hasClass('cms-properties-changed')) {
              cms.galleries.loadSearchResults();
              $(this).removeClass('cms-properties-changed');
          }
          $(this).parent().fadeOut('slow');
          
       });
       
       $('.button-bar button').live('mouseover', function() {
             $(this).toggleClass('ui-state-hover', true);
          }).live('mouseout', function() {
             $(this).toggleClass('ui-state-hover', false);
      });                 
       
       // fade in the preview      
       $('#cms-preview').fadeIn('slow');                    
   }   	
   
   
/* Loads the active image in the preview tab and sets the parameters */
/**
 * Calculates the scaling parameters depending on the configuration and 
 * display the image. 
 * 
 * @param {Object} activeImageData the JSONobject with additional information for the image resource type
 * @param {Object} isInitial true, if true  
 */
var loadActiveItemPreviewArea = function(/**JSONobject*/activeImageData, /**Boolean*/ isInitial) {
	if (activeImageData == "none") {
		return;
	} 
    
    $.extend(cms.galleries.activeItem, activeImageData["activeimage"], {'newwidth':0, 'newheight':0} );
   
	if (isInitial == true) {
		// initial image loaded
		var cropIt = false;
		/*if (getScaleValue(initValues.scale, "cx") != "") {
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
		}*/
		/*if (initValues.useformats == true) {
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
		} else { */
			var sizeChanged = false;
			/*if (initValues.imgwidth != "") {
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
			}*/
            setCropActive(cropIt, isInitial);			
	        /*if (initValues.widgetmode != "simple" || initValues.showformats == true) {
            			// refresh the format select box
				refreshSelectBox();
			} else  if (initValues.showformats == false) {
				$("#croplink").hide();
			} */
		//}
		//initValues.scale = removeScaleValue(initValues.scale, "w");
		//initValues.scale = removeScaleValue(initValues.scale, "h");
	} else {
		// image loaded by user selection
		//$tabs.data("disabled.tabs", []);
		//$tabs.tabs("select", 0);
		//++++++++++++++++resetSizes();
		//+++++++++++++++setCropActive(false);
		/*if (initValues.useformats != true) {
			$("#croplink").hide();
		}*/
		/*if (initValues.useformats == true && initValues.showformats == true) {
			$("#croplink").show();
		}*/
	}
	try {
		// do additional stuff with the active image if necessary
		//activeImageAdditionalActions(isInitial);
	} catch (e) {}
	//$('#previmgname').html(activeItem.title);
	//showItemInfo(-1, "detail", activeItem, true);
}


/* Enables and disabled links and inputs depending if image is cropped or not. */
/**
 * Displays the image format editing fields depending on the config parameters.
 *   
 * @param {Object} isCropped 'true' if image is cropped, 'false' otherwise
 * @param {Object} forceRefreshPreview if true the image preview will be refreshed
 */
var setCropActive = function(isCropped, forceRefreshPreview) {
	//if (initValues.widgetmode == "simple" && initValues.showformats == false) {
		// disable input fields and buttons for simple widget mode
		//$('#txtWidth').get(0).disabled = true;
		//$('#txtHeight').get(0).disabled = true;
		//$('#formatselect').get(0).disabled = true;
		//$('#resetsize').hide();
		//$('#locksizes').hide();
		//$('#cropremove').hide();
		//$('#cropinfo').hide();
	/*} else {
		if (isCropped == true) {
			// cropping has been set, disable input fields and refresh view
			$('#txtWidth').get(0).disabled = true;
			$('#txtHeight').get(0).disabled = true;
			$('#formatselect').get(0).disabled = true;
			$('#resetsize').hide();
			$('#locksizes').hide();
			$('#cropremove').show();
			$('#cropinfo').show();
		} else {
			// cropping has been disabled, enable input fields and refresh view
			if (initValues.useformats == true && initValues.showformats != true) {
				// using formats, calculate image for currently selected size
				changeFormat();
			} else if (initValues.useformats == false) {
				// only enable if not using formats
				$('#txtWidth').get(0).disabled = false;
				$('#txtHeight').get(0).disabled = false;
			}
			$('#formatselect').get(0).disabled = false;
			$('#resetsize').show();
			$('#locksizes').show();
			$('#cropremove').hide();
			$('#cropinfo').hide();
		}
	}*/
	if (isCropped != cms.galleries.activeItem.isCropped || (forceRefreshPreview != null && forceRefreshPreview == true)) {
		cms.galleries.activeItem.isCropped = isCropped;
		refreshActiveImagePreview();
	}
}

/**
 * Refreshes the image preview scaling parameters depending on the scale & crop settings 
 * and displays the image in the preview area.
 */
var refreshActiveImagePreview = function () {
	var scaleParams = "";
	/*if (initValues.scale != null && initValues.scale != "") {
		scaleParams = initValues.scale;
	}*/
	var imgWidth = cms.galleries.activeItem.width;
	var imgHeight = cms.galleries.activeItem.height;
	var useSelectedDimensions = false;
	/*if (cms.galleries.activeItem.isCropped == true) {
		if (scaleParams != "") {
			scaleParams += ",";
		}
		scaleParams += cms.galleries.activeItem.crop;
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
	}*/
	/*if (initValues.useformats != true || (initValues.useformats == true && (formatSelected.width == -1 || formatSelected.height == -1))) {
		setImageFormatFields(imgWidth, imgHeight);
	}*/
	var maxWidth = defaultPreview['width'];
	var maxHeight = defaultPreview['height'];
	/*if (initValues.useformats == true) {
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
	}*/

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
	//var path = cms.galleries.activeItem.linkpath;
	if (scaleParams != "") {
		scaleParams = "?__scale=" + scaleParams;
	}
    $('.cms-image-preview').append('<img src="' + cms.galleries.activeItem['linkpath'] + scaleParams + '" />');
	//$("#imgpreview").html("<img src=\"" + path + scaleParams + "\" />");
}		

/**
 * Calculates the image dimensions that can be shown in the preview area.
 * 
 * @param {Object} imgWidth the width
 * @param {Object} imgHeight the height
 * @param {Object} maxWidth max width
 * @param {Object} maxHeight max height
 */
var calculateDimensions = function (imgWidth, imgHeight, maxWidth, maxHeight) {
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
   
///// Image Content Handler ////////////////              
   /**
    * Image handler to display the preview for images (resource id = 3). 
    */
   var imageContentTypeHandler = cms.imagepreviewhandler.imageContentTypeHandler = {
       'init': showItemPreview
   };
     
})(cms);