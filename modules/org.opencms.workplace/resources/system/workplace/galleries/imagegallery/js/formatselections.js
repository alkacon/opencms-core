var formatSelections;
var formatSelected;

var defaultOptions = LANG.FORMAT_ORIGINAL + "|" + LANG.FORMAT_USER + "|" + LANG.FORMAT_FREECROP + "|" + LANG.FORMAT_SMALL + "|" + LANG.FORMAT_LARGE;
var defaultValues = ["original","user","free", "200x?", "500x?"];

var lockRatio = true;


/* Enables and disabled links and inputs depending if image is cropped or not. */
function setCropActive(isCropped, forceRefreshPreview) {
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
		if (initValues.useformats == true) {
			// using formats, calculate image for currently selected size
			changeFormat();
		} else {
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
	if (isCropped != activeImage.isCropped || (forceRefreshPreview != null && forceRefreshPreview == true)) {
		activeImage.isCropped = isCropped;
		refreshActiveImagePreview();
	}
}

/* Fired when the width or height input texts change. */
function onSizeChanged(dimension, value, refreshImage, refreshSelect) {
	// verifies if the aspect ratio has to be mantained
	if (lockRatio == true) {
		var e = dimension == 'Width' ? $('#txtHeight').get(0) : $('#txtWidth').get(0) ;
	    
		if (value.length == 0 || isNaN(value)) {
			e.value = "";
			return;
		}
	
		var imgHeight = activeImage.height;
		var imgWidth = activeImage.width;
				
		if ( dimension == 'Width' ) {
			value = value == 0 ? 0 : Math.round( imgHeight * ( value  / imgWidth ) );
		} else {
			value = value == 0 ? 0 : Math.round( imgWidth  * ( value / imgHeight ) );
		}
		if ( !isNaN( value ) )
			e.value = value ;
	}
	activeImage.newwidth = parseInt($('#txtWidth').get(0).value);
	activeImage.newheight = parseInt($('#txtHeight').get(0).value);
	
	if (refreshSelect == null || refreshSelect == true) {
		refreshSelectBox();
	}
	if (refreshImage == null || refreshImage == true) {
		refreshActiveImagePreview();
	}
}

/* Checks if the image sizes should be reset when clicking on the "remove crop" button. */
function checkResetSizes() {
	if (activeImage.isCropped == false && !initValues.useformats == true) {
		resetSizes();
	}
}

/* Fired when the reset size button is clicked. */
function resetSizes() {
	activeImage.newwidth = -1;
	activeImage.newheight = -1;
	if (initValues.useformats == true) {
		// using formats, calculate image for currently selected size
		changeFormat();
	} else {
		// not using formats, lock ratio and use original image size
		lockRatio = true;
		$('#locksizes').attr("title", LANG.IMGPREVIEW_SIZE_UNLOCK);
		$('#locksizes').attr("class", "btnlocked");
		$('#txtWidth').get(0).value  = activeImage.width;
		$('#txtHeight').get(0).value  = activeImage.height;
		$("#formatselect").get(0).selectedIndex = 0;
		refreshActiveImagePreview();
	}
	$("#croplink").hide();
}

/* Switches the lock ratio. */
function switchLock() {
	var newRatio = !lockRatio ;
	setLockRatio(newRatio);
}

/* Enables or disables the lock ratio. */
function setLockRatio(newRatio) {
	lockRatio = newRatio;
	if (lockRatio == true) {
		if ($('#txtWidth').get(0).value.length > 0 ) {
		    onSizeChanged('Width', $('#txtWidth').get(0).value);
		} else {
		    onSizeChanged('Height', $('#txtHeight').get(0).value);
		}
		$('#locksizes').attr("title", LANG.IMGPREVIEW_SIZE_UNLOCK);
		$('#locksizes').attr("class", "btnlocked");
	} else {
		$('#locksizes').attr("title", LANG.IMGPREVIEW_SIZE_LOCK);
		$('#locksizes').attr("class", "btnunlocked");
	}
}			

/* Called if a format is selected in the format select box. */
function changeFormat() {
	var selected = $("#formatselect").get(0).selectedIndex;
	
	formatSelected = formatSelections[selected];
	
	if (formatSelected.type == "original") {
		// reset to original sizes
		resetSizes();
	} else if (formatSelected.type == "user") {
		// user defined format, nothing to do except remove cropping
		$("#croplink").show();
	} else if (formatSelected.type == "free") {
		// free cropping;
		$("#croplink").show();
	} else {
		// other format selected
		$("#croplink").show();
		if (formatSelected.width != -1) {
			if (formatSelected.height != -1) {
				// we have a width and height, we also have to check the lock ratio
				$('#txtWidth').get(0).value = formatSelected.width;
				onSizeChanged("Width", formatSelected.width, false, false);
				var txtHeight = parseInt($('#txtHeight').get(0).value);
				if (txtHeight != formatSelected.height) {
					setLockRatio(false);
					$('#txtHeight').get(0).value = formatSelected.height;
					onSizeChanged("Height", formatSelected.height, true, false);
				}
			} else {
				// we have only the width, change it
				if (initValues.useformats == true) {
					setLockRatio(true);
				}
				$('#txtWidth').get(0).value = formatSelected.width;
				onSizeChanged("Width", formatSelected.width, true, false);
			}
		} else {
			if (formatSelected.height != -1) {
				// we only have a height value, change it
				if (initValues.useformats == true) {
					setLockRatio(true);
				}
				$('#txtHeight').get(0).value = formatSelected.height;
				onSizeChanged("Height", formatSelected.height, true, false);
			}
		}
	}
}

/* Initializes the options and values shown in the format select box. */
function initFormatSelectBox() {
	var formatOptions, formatValues;
	if (initValues.useformats == true) {
		// using a preselected format, just display information that cannot be changed

		$("#txtWidth").get(0).disabled = true;
		$("#txtHeight").get(0).disabled = true;
		formatOptions = unescape(eval('window.opener.imgFmtNames' + initValues.hashid));
		formatValues = eval('window.opener.imgFmts' + initValues.hashid);
	} else {
		// not using a format, image width and height can be adjusted
		formatOptions = defaultOptions;
		formatValues = defaultValues;
	}
	
	formatSelections = new Array(formatValues.length);
	for (var i=0; i<formatValues.length; i++) {
		formatSelections[i] = new Object();
		var pos = formatOptions.indexOf("|");
		var currOptStr;
		if (pos != -1) {
			currOptStr = formatOptions.substring(0, pos);
			formatOptions = formatOptions.substring(pos + 1);
		} else {
			currOptStr = formatOptions;
		}
		pos = currOptStr.indexOf(":");
		if (pos != -1) {
			formatSelections[i].optionvalue = currOptStr.substring(0, pos);
			formatSelections[i].optionlabel = currOptStr.substring(pos + 1);
		} else {
			formatSelections[i].optionvalue = currOptStr;
			formatSelections[i].optionlabel = currOptStr;
		}
		pos = formatValues[i].indexOf("x");
		if (pos != -1) {
			formatSelections[i].type = "ocspecial";
			formatSelections[i].width = -1;
			formatSelections[i].height= -1;
			var pixels = formatValues[i].substring(0, pos);
			if (pixels != "?") {
				formatSelections[i].width = parseInt(pixels);
			}
			pixels = formatValues[i].substring(pos + 1);
			if (pixels != "?") {
				formatSelections[i].height = parseInt(pixels);
			}
		} else {
			formatSelections[i].type = formatValues[i];
			formatSelections[i].width = -1;
			formatSelections[i].height= -1;
		}
		var selected = "";
		if (initValues.useformats == true && initValues.formatname == formatSelections[i].optionvalue) {
			selected = " selected=\"selected\"";
			formatSelected = formatSelections[i];
		}
		$("#formatselect").append(
			  "<option value=\"" 
			+ formatSelections[i].optionvalue
			+ "\""
			+ selected
			+ ">"
			+ formatSelections[i].optionlabel
			+ "</option>"
		);
	}
}

/* Refreshes the selected option of the format select box. */
function refreshSelectBox() {
	var selectedIndex = -1;
	for (var i=0; i<formatSelections.length; i++) {
		// check if the values match a format selection
		var currSelect = formatSelections[i];
		if (currSelect.type == "user") {
			selectedIndex = i;
			$("#croplink").show();
		}
		if (currSelect.type == "free" && activeImage.isCropped == true) {
			selectedIndex = i;
			$("#croplink").show();
		}
		if (currSelect.type == "original" && activeImage.newwidth == activeImage.width && activeImage.newheight == activeImage.height) {
			selectedIndex = i;
			$("#croplink").hide();
			break;
		} else if (currSelect.width == activeImage.newwidth && (currSelect.height == -1 || currSelect.height == activeImage.newheight)) {
			selectedIndex = i;
			$("#croplink").show();
			break;
		} else if (currSelect.height == activeImage.newheight && (currSelect.width == -1 || currSelect.width == activeImage.newwidth)) {
			selectedIndex = i;
			$("#croplink").show();
			break;
		}
		
	}
	if (selectedIndex != -1) {
		if (formatSelections[selectedIndex].type == "user" && formatSelected != null && formatSelected.type == "free") {
			return;
		}
		$("#formatselect").get(0).selectedIndex = selectedIndex;
		formatSelected = formatSelections[selectedIndex];
	}
}