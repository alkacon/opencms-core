/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2006 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * "Support Open Source software. What about a donation today?"
 * 
 * File Name: fck_image.js
 * 	Scripts related to the Image dialog window (see fck_image.html).
 * 
 * File Authors:
 * 		Frederico Caldeira Knabben (fredck@fckeditor.net)
 *              modified and enhanced by Andreas Zahner
 */

var oEditor		= window.parent.InnerDialogLoaded() ;
var FCK			= oEditor.FCK ;
var FCKLang		= oEditor.FCKLang ;
var FCKConfig		= oEditor.FCKConfig ;
var FCKDebug		= oEditor.FCKDebug ;
var FCKBrowserInfo	= oEditor.FCKBrowserInfo;

/* Enables or disables the enhanced image dialog options */
var showEnhancedOptions = false;

var bImageButton = ( document.location.search.length > 0 && document.location.search.substr(1) == 'ImageButton' ) ;

//#### Dialog Tabs

// Set the dialog tabs.
window.parent.AddTab( 'Info', FCKLang.DlgImgInfoTab ) ;
window.parent.AddTab( 'Gallery', FCKLang.DlgImgGalleryTab ) ;

if ( !bImageButton && !FCKConfig.ImageDlgHideLink )
	window.parent.AddTab( 'Link', FCKLang.DlgImgLinkTab ) ;

if ( FCKConfig.ImageUpload )
	window.parent.AddTab( 'Upload', FCKLang.DlgLnkUpload ) ;

if ( !FCKConfig.ImageDlgHideAdvanced )
	window.parent.AddTab( 'Advanced', FCKLang.DlgAdvancedTag ) ;

// Function called when a dialog tag is selected.
function OnDialogTabChange( tabCode )
{
	ShowE('divInfo'		, ( tabCode == 'Info' ) ) ;
	ShowE('divGallery'	, ( tabCode == 'Gallery' ) ) ;
	ShowE('divLink'		, ( tabCode == 'Link' ) ) ;
	ShowE('divUpload'	, ( tabCode == 'Upload' ) ) ;
	ShowE('divAdvanced'	, ( tabCode == 'Advanced' ) ) ;
}

// Get the selected image (if available).
var oImage = FCK.Selection.GetSelectedElement() ;

if ( oImage && oImage.tagName != 'IMG' && oImage.tagName != 'SPAN' && !( oImage.tagName == 'INPUT' && oImage.type == 'image' ) ) {
	oImage = null ;
}


// Get the active link.
var oLink = FCK.Selection.MoveToAncestorNode("A");

// get the table around the image, if present
var oTable = null;

var oImageOriginal ;

function UpdateOriginal( resetSize )
{
	if ( !eImgPreview )
		return ;
	
	if ( GetE('txtUrl').value.length == 0 )
	{
		oImageOriginal = null ;
		return ;
	}
		
	oImageOriginal = document.createElement( 'IMG' ) ;	// new Image() ;

	if ( resetSize )
	{
		oImageOriginal.onload = function()
		{
			this.onload = null ;
			ResetSizes(true) ;
		}
	}

	oImageOriginal.src = eImgPreview.src ;
}

var bPreviewInitialized ;

window.onload = function()
{
	// Translate the dialog box texts.
	oEditor.FCKLanguageManager.TranslatePage(document) ;

	GetE('btnLockSizes').title = FCKLang.DlgImgLockRatio ;
	GetE('btnResetSize').title = FCKLang.DlgBtnResetSize ;

	// Load the selected element information (if any).
	LoadSelection() ;

	// Show/Hide the "Browse Server" button.
	GetE('tdBrowse').style.display				= FCKConfig.ImageBrowser	? '' : 'none' ;
	GetE('divLnkBrowseServer').style.display	= FCKConfig.LinkBrowser		? '' : 'none' ;

	if (showEnhancedOptions) {
		GetE("enhAltCheck").style.display = "";
		GetE("enhAltBt").style.display = "";
		GetE("enhCopy").style.display = "";
		GetE("enhOrig").style.display = "";
	}

	UpdateOriginal() ;

	// Set the actual uploader URL.
	if ( FCKConfig.ImageUpload )
		GetE('frmUpload').action = FCKConfig.ImageUploadURL ;

	window.parent.SetAutoSize( true ) ;

	// Activate the "OK" button.
	window.parent.SetOkButton( true ) ;

	// initialize the OpenCms image gallery
	setTimeout("showGallerySelectBox()", 200);
	setTimeout("showGalleryItems()", 400);
}

function LoadSelection()
{
	if ( ! oImage ) {
		GetE("txtImageBorder").checked = true;
		setImageBorder();
		GetE('cmbAlign').value = "left";
		return ;
	}

	var altText = "";
	var copyText = "";
	var imgBorder = false;
	var imgHSp = "";
	var imgVSp = "";
	if (FCK.Selection.HasAncestorNode("TABLE")) {
		oTable = FCK.Selection.MoveToAncestorNode("TABLE");
		try {
			var idPart = oTable.getAttribute("id").substring(1);
			if (idPart == oImage.getAttribute("id").substring(1)) {

				var altElem = oEditor.FCK.EditorDocument.getElementById("s" + idPart);
				if (altElem) {
					altText = altElem.firstChild.data;
				}

				var cpElem = oEditor.FCK.EditorDocument.getElementById("c" + idPart);
				if (cpElem) {
					copyText = cpElem.firstChild.data;
				}
				imgHSp = oTable.style.marginLeft;
				imgVSp = oTable.style.marginTop;
				if (imgHSp.indexOf("px") != -1) {	
					imgHSp = imgHSp.substring(0, imgHSp.length - 2);
				}
				if (imgVSp.indexOf("px") != -1) {	
					imgVSp = imgVSp.substring(0, imgVSp.length - 2);
				}
			}
		} catch (e) {}
	} else {
		imgHSp = GetAttribute( oImage, 'hspace', '' );
		imgVSp = GetAttribute( oImage, 'vspace', '' );
	}
	if (altText == "") {
		altText = GetAttribute( oImage, 'alt', '' ) ;
	}

	var sUrl = oImage.getAttribute( '_fcksavedurl' ) ;
	if ( sUrl == null )
		sUrl = GetAttribute( oImage, 'src', '' ) ;

	var paramIndex = sUrl.indexOf("?" + paramScale);
	if (paramIndex != -1) {
		var scaler = createScaler(sUrl.substring(paramIndex + 1));
		sUrl = sUrl.substring(0, paramIndex);
	}

	GetE('txtUrl').value    	= sUrl ;
	GetE('txtAlt').value    	= altText ;
	if (copyText != "") {
		GetE("txtCopyrightText").value = copyText;
	}

	if (imgHSp != "" || imgVSp != "") {
		imgBorder = true;
	}
	
	if (imgBorder) {
		GetE('txtVSpace').value	= imgVSp;
		GetE('txtHSpace').value	= imgHSp;
		GetE("txtImageBorder").checked = true;
	}
	
	GetE('txtBorder').value		= GetAttribute( oImage, 'border', '' ) ;
	GetE('cmbAlign').value		= GetAttribute( oImage, 'align', '' ) ;

	var iWidth, iHeight ;

	var regexSize = /^\s*(\d+)px\s*$/i ;
	
	if ( oImage.style.width )
	{
		var aMatch  = oImage.style.width.match( regexSize ) ;
		if ( aMatch )
		{
			iWidth = aMatch[1] ;
			oImage.style.width = '' ;
		}
	}

	if ( oImage.style.height )
	{
		var aMatch  = oImage.style.height.match( regexSize ) ;
		if ( aMatch )
		{
			iHeight = aMatch[1] ;
			oImage.style.height = '' ;
		}
	}

	GetE('txtWidth').value	= iWidth ? iWidth : GetAttribute( oImage, "width", '' ) ;
	GetE('txtHeight').value	= iHeight ? iHeight : GetAttribute( oImage, "height", '' ) ;

	// Get Advanced Attributes
	GetE('txtAttId').value			= oImage.id ;
	GetE('cmbAttLangDir').value		= oImage.dir ;
	GetE('txtAttLangCode').value	= oImage.lang ;
	GetE('txtAttTitle').value		= oImage.title ;
	GetE('txtAttClasses').value		= oImage.getAttribute('class',2) || '' ;
	GetE('txtLongDesc').value		= oImage.longDesc ;

	if ( oEditor.FCKBrowserInfo.IsIE )
		GetE('txtAttStyle').value	= oImage.style.cssText ;
	else
		GetE('txtAttStyle').value	= oImage.getAttribute('style',2) ;

	if ( oLink )
	{
		var sUrl = oLink.getAttribute( '_fcksavedurl' ) ;
		if ( sUrl == null )
			sUrl = oLink.getAttribute('href',2) ;
	
		GetE('txtLnkUrl').value		= sUrl ;
		GetE('cmbLnkTarget').value	= oLink.target ;
	}

	UpdatePreview(true) ;
}

//#### The OK button was hit.
function Ok()
{
	if ( GetE('txtUrl').value.length == 0 )
	{
		window.parent.SetSelectedTab( 'Info' ) ;
		GetE('txtUrl').focus() ;

		alert( FCKLang.DlgImgAlertUrl ) ;

		return false ;
	}

	var bHasImage = ( oImage != null ) ;

	if ( bHasImage && bImageButton && oImage.tagName == 'IMG' )
	{
		if ( confirm( 'Do you want to transform the selected image on a image button?' ) )
			oImage = null ;
	}
	else if ( bHasImage && !bImageButton && oImage.tagName == 'INPUT' )
	{
		if ( confirm( 'Do you want to transform the selected image button on a simple image?' ) )
			oImage = null ;
	}

	var imgCreated = false;
	
	if ( !bHasImage )
	{
		if ( bImageButton )
		{
			oImage = FCK.EditorDocument.createElement( 'INPUT' ) ;
			oImage.type = 'image' ;
			oImage = FCK.InsertElementAndGetIt( oImage ) ;
		}
		else {
			oImage = FCK.CreateElement( 'IMG' ) ;
			// set flag that image is newly created
			imgCreated = true;
		}
	}
	else
		oEditor.FCKUndo.SaveUndoStep() ;
		
	UpdateImage( oImage, false, true ) ;

	// now its getting difficult, be careful when modifying anything below this comment...

	if (showEnhancedOptions && oLink && (insertSubTitle() || insertCopyright())) {
		// original link has to be removed if a table is created in enhanced options
		FCK.Selection.SelectNode(oLink);
		FCK.ExecuteNamedCommand( 'Unlink' ) ;
	}

	// now we set the image object either to the image or in case of enhanced options to a table element
	oImage = createEnhancedImage();

	if (showEnhancedOptions && oTable != null && oTable.id.substring(0, 5) == "timg_") {
		// table is already present, select it
		FCK.Selection.SelectNode(oTable);
		
	}

	if (!imgCreated) {
		// delete the selection (either the image or the complete table) if the image was not freshly created
		FCK.Selection.Delete();
		// now insert the new element
		oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
	} else {
		// this handles the initial creation of an image, might be buggy...
		if (!oEditor.FCKBrowserInfo.IsIE) {
			// we have to differ here, otherwise the stupid IE creates the image twice!
			oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
		} else if (isTablePreview()) {
			// in IE... insert the new element to make sure the table is inserted
			oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
		}
	}

	
	if (oImage.tagName != "TABLE") {
		// the object to insert is a simple image, check the link to set
		FCK.Selection.SelectNode(oImage);

		oLink = FCK.Selection.MoveToAncestorNode("A");


		var sLnkUrl = GetE('txtLnkUrl').value.Trim();
		var linkOri = "";

 		if (insertLinkToOriginal()) {
			sLnkUrl = "#";
			linkOri = getLinkToOriginal();
		} else if (sLnkUrl == "#") {
			sLnkUrl = "";
		}
	
		if ( sLnkUrl.length == 0 )
		{
			if ( oLink )
				FCK.ExecuteNamedCommand( 'Unlink' ) ;
		}
			else
		{
			if ( oLink ) {	
				// remove an existent link and create it newly, because otherwise the "onclick" attribute does not vanish in Mozilla
				FCK.ExecuteNamedCommand( 'Unlink' ) ;
				oLink = oEditor.FCK.CreateLink( sLnkUrl ) ;
			}
			else			// Creating a new link.
			{
				if ( !bHasImage )
					oEditor.FCKSelection.SelectNode( oImage ) ;

				oLink = oEditor.FCK.CreateLink( sLnkUrl ) ;	

				if ( !bHasImage )
				{
					oEditor.FCKSelection.SelectNode( oLink ) ;
					oEditor.FCKSelection.Collapse( false ) ;
				}
			}

			if (linkOri != "") {
				// set the necessary attributes for the link to original image
				try {
					oLink.setAttribute("id", "limg_" + activeImage.hashCode);
					oLink.setAttribute("onclick", linkOri);
					if (GetE('txtBorder').value == "") {
						oImage.setAttribute("border", "0");
					}
				} catch (e) {}
			}
			try {
				SetAttribute( oLink, '_fcksavedurl', sLnkUrl ) ;
				SetAttribute( oLink, 'target', GetE('cmbLnkTarget').value ) ;
			} catch (e) {}
		}

	} // end simple image tag

	return true;
}

function createEnhancedImage() {
	if (isTablePreview()) {

		var oNewTable= oEditor.FCK.EditorDocument.createElement("TABLE");
		// now set the table attributes		
		var st = "width: " + GetE("txtWidth").value + "px;";
		var al = GetE("cmbAlign").value;
		if (al == "left" || al == "right") {
			st += " float: " + al + ";";
		}
		var imgVSp = GetE('txtVSpace').value;
		var imgHSp = GetE('txtHSpace').value;
		if (imgVSp != "" || imgHSp != "") {
			if (imgVSp == "") {
				imgVSp = "0";
			}
			if (imgHSp == "") {
				imgHSp = "0";
			}
			st += "margin: " + imgVSp + "px " + imgHSp + "px " + imgVSp + "px " + imgHSp + "px;";
		}
		oNewTable.style.cssText = st;
		SetAttribute(oNewTable, "id", "timg_" + activeImage.hashCode);
		SetAttribute(oNewTable, "border", "0");
		SetAttribute(oNewTable, "cellpadding", "0");
		SetAttribute(oNewTable, "cellspacing", "0");

		// insert the 1st row with the image
		var oRow1 = oNewTable.insertRow(-1);
		var oCell1 = oRow1.insertCell(-1);
		oCell1.id = "aimg_" + activeImage.hashCode;

		if (insertLinkToOriginal()) {
			var oLinkOrig = oEditor.FCK.EditorDocument.createElement("A");
			oLinkOrig.href = "#";
			oLinkOrig.setAttribute("onclick", getLinkToOriginal());
			oLinkOrig.setAttribute("id", "limg_" + activeImage.hashCode);
			if (GetE('txtBorder').value == "") {
				oImage.setAttribute("border", "0");
			}
			oLinkOrig.appendChild(oImage);
			oCell1.appendChild(oLinkOrig);
		} else {
			// simply add image
			oCell1.appendChild(oImage);
		}
		if (insertCopyright()) {
			// insert the 2nd row with the copyright information
			var copyText = GetE("txtCopyrightText").value;
			if (copyText == "") {
				copyText = "&copy; " + activeImage.copyright;
			}
			var oRow2 = oNewTable.insertRow(-1);
			var oCell2 = oRow2.insertCell(-1);
			oCell2.className = "imgCopyright";
			oCell2.id = "cimg_" + activeImage.hashCode;
			oCell2.innerHTML = copyText;
		}

		if (insertSubTitle()) {
			// insert the 3rd row with the subtitle
			var altText = GetE("txtAlt").value;
			if (altText == "") {
				altText = activeImage.title;
			}
			var oRow3 = oNewTable.insertRow(-1);
			var oCell3 = oRow3.insertCell(-1);
			oCell3.className = "imgSubtitle";
			oCell3.id = "simg_" + activeImage.hashCode;
			oCell3.innerHTML = altText;
		}

		// return the new object
		return oNewTable;
	} else {
		// return the original object
		return oImage;
	}
}

function isTablePreview() {
	return showEnhancedOptions && (insertSubTitle() || insertCopyright());
}

function UpdateImage( e, skipId, setScaleParam )
{
	var txtUrl = GetE('txtUrl').value;
	var newWidth = GetE('txtWidth').value;
	var newHeight = GetE('txtHeight').value;
	if (setScaleParam && activeImage != null) {
		if (newWidth != activeImage.width || newHeight != activeImage.height) {
			var scaler = new OCmsScaler(newWidth, newHeight); 
			txtUrl += scaler.toParams();
		}
	}
	
	if (!setScaleParam && txtUrl != null && txtUrl != "") {
		if (activeImage == null || activeImage.url != txtUrl) {
			// set the active image from the selection information
			setActiveImage(-1, txtUrl);
		}
	}

	e.src = txtUrl ;
	SetAttribute( e, "_fcksavedurl", txtUrl) ;
	SetAttribute( e, "alt"   , GetE('txtAlt').value ) ;
	SetAttribute( e, "width" , newWidth ) ;
	SetAttribute( e, "height", newHeight ) ;
	SetAttribute( e, "border", GetE('txtBorder').value ) ;

	if (isTablePreview()) {
		SetAttribute( e, "vspace", "" ) ;
		SetAttribute( e, "hspace", "" ) ;
	} else {
		SetAttribute( e, "vspace", GetE('txtVSpace').value ) ;
		SetAttribute( e, "hspace", GetE('txtHSpace').value ) ;
		if (insertLinkToOriginal() && GetE('txtBorder').value == "") {
			SetAttribute(e, "border", "0");
		}

	}
	
	SetAttribute( e, "align" , GetE('cmbAlign').value ) ;

	// Advanced Attributes

	if ( ! skipId ) {
		var idVal = GetE('txtAttId').value;
		if (idVal == "" || idVal.substring(0, 5) == "iimg_") {
			idVal = "iimg_" + activeImage.hashCode;
		}
		SetAttribute( e, 'id', idVal ) ;
	}

	SetAttribute( e, 'dir'		, GetE('cmbAttLangDir').value ) ;
	SetAttribute( e, 'lang'		, GetE('txtAttLangCode').value ) ;
	SetAttribute( e, 'title'	, GetE('txtAttTitle').value ) ;
	SetAttribute( e, 'class'	, GetE('txtAttClasses').value ) ;
	SetAttribute( e, 'longDesc'	, GetE('txtLongDesc').value ) ;

	if ( oEditor.FCKBrowserInfo.IsIE )
		e.style.cssText = GetE('txtAttStyle').value ;
	else
		SetAttribute( e, 'style', GetE('txtAttStyle').value ) ;
}

var eImgPreview ;
var eImgPreviewLink;
var eTblPreview;

function SetPreviewElements( imageElement, linkElement, tableElement )
{
	eImgPreview = imageElement;
	eImgPreviewLink = linkElement;
	eTblPreview = tableElement;

	UpdatePreview();
	UpdateOriginal();
	
	bPreviewInitialized = true;
}

function UpdatePreview(useTimeout)
{
	if ( !eImgPreview || !eImgPreviewLink )
		return ;
	if ( GetE('txtUrl').value.length == 0 )
		eImgPreviewLink.style.display = 'none' ;
	else
	{
		var txtUrl = GetE('txtUrl').value;
		if (activeImage == null || activeImage.url != txtUrl) {
			var setNew = true;
			if (activeImage != null) {
				var fullPath = ocmsServer + ocmsContext + activeImage.sitePath;
				var contextPath = ocmsContext + activeImage.sitePath;
				if (fullPath == activeImage.url || contextPath == activeImage.url) {
					setNew = false;
				}
			}
			if (setNew) {
				// set the active image from the selection information
				setActiveImage(-1, GetE('txtUrl').value);
				if (useTimeout) {
					setTimeout("UpdatePreview(true)", 100);
					return;
				}
			}
		}
		if ((activeImage && activeImage.copyright == null) && GetE("txtCopyrightText").value == "") {
			GetE("txtCopyright").checked = false;
		}
		if (useTimeout && oTable != null && oTable.id == "timg_" + activeImage.hashCode) {
			var elemT = oEditor.FCK.EditorDocument.getElementById("simg_" + activeImage.hashCode);
			if (elemT) {
				GetE("txtSubtitle").checked = true;
			}

			var elemC = oEditor.FCK.EditorDocument.getElementById("cimg_" + activeImage.hashCode);
			if (elemC) {
				GetE("txtCopyright").checked = true;
			}
		}
		if (useTimeout && oLink) {
			var linkId = oLink.getAttribute("id");
			if (linkId != null && linkId.substring(0, 5) == "limg_") {
				GetE("txtLinkOriginal").checked = true;
			}
		}
		UpdateImage( eImgPreview, true, true) ;

		if ( GetE('txtLnkUrl').value.Trim().length > 0 )
			eImgPreviewLink.href = 'javascript:void(null);' ;
		else
			SetAttribute( eImgPreviewLink, 'href', '' ) ;

		eImgPreviewLink.style.display = '' ;

		if (isTablePreview()) {
			eImgPreviewLink.style.display = 'none' ;
			eImgPreview.style.display = "none";
			eTblPreview.style.display = "";
			var st = "width: " + GetE("txtWidth").value + "px;";
			var al = GetE("cmbAlign").value;
			if (al == "left" || al == "right") {
				st += " float: " + al + ";";
			}
			var imgVSp = GetE('txtVSpace').value;
			var imgHSp = GetE('txtHSpace').value;
			if (imgVSp != "" || imgHSp != "") {
				if (imgVSp == "") {
					imgVSp = "0";
				}
				if (imgHSp == "") {
					imgHSp = "0";
				}
				st += "margin: " + imgVSp + " " + imgHSp + " " + imgVSp + " " + imgHSp + ";";
			}

			eTblPreview.style.cssText = st;

			var prevImg = getPreviewElement("iPreview");
			UpdateImage(prevImg, true, true);
			if (insertSubTitle()) {
				var altText = GetE("txtAlt").value;
				if (altText == "") {
					altText = activeImage.title;
				}
				getPreviewElement("sPreview").innerHTML = altText;
			} else {
				getPreviewElement("sPreview").innerHTML = "";
			}
			if (insertCopyright()) {
				var copyText = GetE("txtCopyrightText").value;
				if (copyText == "") {
					copyText = "&copy; " + activeImage.copyright;
				}
				getPreviewElement("cPreview").innerHTML = copyText;
			} else {
				getPreviewElement("cPreview").innerHTML = "";
			}
		} else {
			eTblPreview.style.display = "none";
		}

	}
}

function getPreviewElement(elemId) {
	return window.previewFrame.document.getElementById(elemId);
}

function getLinkToOriginal() {
	var linkUri = "javascript:window.open('";
	linkUri += vfsPopupUri;
	linkUri += "?uri=";
	linkUri += activeImage.url;
	linkUri += "', 'original', 'width=";
	linkUri += activeImage.width;
	linkUri += ",height=";
	linkUri += activeImage.height;
	linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
	return linkUri;
}

var bLockRatio = true ;

function SwitchLock( lockButton )
{
	bLockRatio = !bLockRatio ;
	lockButton.className = bLockRatio ? 'BtnLocked' : 'BtnUnlocked' ;
	lockButton.title = bLockRatio ? 'Lock sizes' : 'Unlock sizes' ;

	if ( bLockRatio )
	{
		if ( GetE('txtWidth').value.length > 0 )
			OnSizeChanged( 'Width', GetE('txtWidth').value ) ;
		else
			OnSizeChanged( 'Height', GetE('txtHeight').value ) ;
	}
}

// Fired when the width or height input texts change
function OnSizeChanged( dimension, value )
{
	// Verifies if the aspect ration has to be mantained
	if ( oImageOriginal && bLockRatio )
	{
		var e = dimension == 'Width' ? GetE('txtHeight') : GetE('txtWidth') ;
		
		if ( value.length == 0 || isNaN( value ) )
		{
			e.value = '' ;
			return ;
		}

		if ( dimension == 'Width' )
			value = value == 0 ? 0 : Math.round( activeImage.height * ( value  / activeImage.width ) ) ;
		else
			value = value == 0 ? 0 : Math.round( activeImage.width  * ( value / activeImage.height ) ) ;

		if ( !isNaN( value ) )
			e.value = value ;
	}

	UpdatePreview() ;
}

// Fired when the Reset Size button is clicked
function ResetSizes(useOriginalData)
{
	if ( ! oImageOriginal ) return ;

	if (useOriginalData) {
		GetE('txtWidth').value  = oImageOriginal.width;
		GetE('txtHeight').value  = oImageOriginal.height;
	} else {
		GetE('txtWidth').value  = activeImage.width ;
		GetE('txtHeight').value = activeImage.height ;
	}

	UpdatePreview() ;
}
// ######################################################################################################################
function BrowseServer()
{
	OpenServerBrowser(
		'Image',
		FCKConfig.ImageBrowserURL,
		FCKConfig.ImageBrowserWindowWidth,
		FCKConfig.ImageBrowserWindowHeight ) ;
}

function LnkBrowseServer()
{
	OpenServerBrowser(
		'Link',
		FCKConfig.LinkBrowserURL,
		FCKConfig.LinkBrowserWindowWidth,
		FCKConfig.LinkBrowserWindowHeight ) ;
}

function OpenServerBrowser( type, url, width, height )
{
	sActualBrowser = type ;
	OpenFileBrowser( url, width, height ) ;
}

var sActualBrowser ;

function SetUrl( url, width, height, alt )
{
	if ( sActualBrowser == 'Link' )
	{
		GetE('txtLnkUrl').value = url ;
		UpdatePreview() ;
	}
	else
	{
		GetE('txtUrl').value 		= url ;

		GetE('txtWidth').value 		= width ? width : '' ;
		GetE('txtHeight').value 	= height ? height : '' ;

		if ( alt )
			GetE('txtAlt').value = alt;

		UpdatePreview() ;
		UpdateOriginal( true ) ;
	}
	
	window.parent.SetSelectedTab( 'Info' ) ;
}

function OnUploadCompleted( errorNumber, fileUrl, fileName, customMsg )
{
	switch ( errorNumber )
	{
		case 0 :	// No errors
			alert( 'Your file has been successfully uploaded' ) ;
			break ;
		case 1 :	// Custom error
			alert( customMsg ) ;
			return ;
		case 101 :	// Custom warning
			alert( customMsg ) ;
			break ;
		case 201 :
			alert( 'A file with the same name is already available. The uploaded file has been renamed to "' + fileName + '"' ) ;
			break ;
		case 202 :
			alert( 'Invalid file type' ) ;
			return ;
		case 203 :
			alert( "Security error. You probably don't have enough permissions to upload. Please check your server." ) ;
			return ;
		default :
			alert( 'Error on file upload. Error number: ' + errorNumber ) ;
			return ;
	}

	sActualBrowser = ''
	SetUrl( fileUrl ) ;
	GetE('frmUpload').reset() ;
}

var oUploadAllowedExtRegex	= new RegExp( FCKConfig.ImageUploadAllowedExtensions, 'i' ) ;
var oUploadDeniedExtRegex	= new RegExp( FCKConfig.ImageUploadDeniedExtensions, 'i' ) ;

function CheckUpload()
{
	var sFile = GetE('txtUploadFile').value ;
	
	if ( sFile.length == 0 )
	{
		alert( 'Please select a file to upload' ) ;
		return false ;
	}
	
	if ( ( FCKConfig.ImageUploadAllowedExtensions.length > 0 && !oUploadAllowedExtRegex.test( sFile ) ) ||
		( FCKConfig.ImageUploadDeniedExtensions.length > 0 && oUploadDeniedExtRegex.test( sFile ) ) )
	{
		OnUploadCompleted( 202 ) ;
		return false ;
	}
	
	return true ;
}