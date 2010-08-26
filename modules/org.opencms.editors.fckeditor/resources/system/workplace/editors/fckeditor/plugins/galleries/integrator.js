<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%><%= cms.getContent("/system/workplace/resources/editors/fckeditor/editor/dialog/common/fck_dialog_common.js") %>
/**
 * The JavaScript functions of this file serve as an interface between the API of the FCKEditor and the gallery dialog.<p>
 *
 * Following function needs to be provided by the gallery dialog:<p>
 * 
 * boolean closeGalleryDialog()<p>
 * 
 * This should check if further user input is required and other wise set the selected resource via the provided functions <code>setLink</code> and <code>setImage</code>.<p>
 * Returning <code>true</code> when all data has been set and the dialog should be closed.<p>
 * 
 */

/** The fck editor frame. */
var parentDialog=window.parent;

// remove loading overlay and get editor reference
/** The fck editor instance. */
var fckEditor=parentDialog.InnerDialogLoaded();

/** The fck editor configuration. */
var fckConfig= fckEditor.FCKConfig;

/* Absolute path to the JSP that displays the image in original size. */
var imagePopupUri = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";

// some string constants

/** Link id prefix. */
var LINK_PREFIX='limg_';
/** Copyright span id prefix. */
var COPY_PREFIX='cimg_';
/** Subtitle span prefix. */
var SUB_PREFIX='simg_'
/** Enhancement span id prefix. */
var ENHANCE_PREFIX='aimg_';


/**
 * Function will be triggered by the editor dialog OK button. 
 * Will check if current gallery state allows closing and sets the selected resource parameters.<p>
 * 
 * @return <code>true</code> to close the dialog
 */
function Ok(){
	return setDataInEditor();
}

/**
 * Triggers the OK action.<p>
 * 
 * @return void
 */
function dialogOk(){
	parentDialog.Ok();
}

/**
 * Enables the dialog OK button.<p>
 * 
 * @param enabled <code>boolean</code> <code>true</code> to enable the button
 * 
 * @return void
 */
function enableDialogOk(enabled){
	parentDialog.SetOkButton(enabled);
}

/**
 * Returns all available information of the selected image tag, or null, if no image is selected.<p>
 * 
 * @return a map with the following keys: 
 * 			alt, clazz, height, hspace, linkPath, linkTarget, longDesc, style, title, vspace, width
 * 
 * 			all keys represent a tag attribute by the same name, only linkPath and linkTarget contain
 * 			information on an surrounding link tag
 */
function getImageInfo(){
	var image=_getSelectedImage()
	if (image==null){
		return {};
	}
	var result={};
	result['alt']=GetAttribute(image, 'alt', null);
	result['align']=GetAttribute(image, 'align', null);
	result['clazz']=GetAttribute(image, 'class', null);
	result['dir']=GetAttribute(image, 'dir', null);
	result['height']=GetAttribute(image, 'height', null);
	result['hspace']=GetAttribute(image, 'hspace', null);
	result['id']=GetAttribute(image, 'id', null);
	result['lang']=GetAttribute(image, 'lang', null);
	result['longDesc']=GetAttribute(image, 'longDesc', null);
	result['style']=GetAttribute(image, 'style', null);
	result['title']=GetAttribute(image, 'title', null);
	result['vspace']=GetAttribute(image, 'vspace', null);
	result['width']=GetAttribute(image, 'width', null);
	
	if (fckEditor.FCK.Selection.HasAncestorNode("A")) {
		var imageLink=parentDialog.Selection.GetSelection().MoveToAncestorNode("A");
		if (imageLink != null && imageLink.id.substring(0, 5) == LINK_PREFIX){
			result['hash']=imageLink.id.substring(5);
			result['target']=GetAttribute(imageLink,'target',null);
			var href=GetAttribute(imageLink,'href',null);
			if (href && href!="" && href!="#" && GetAttribute(image, 'src', null).indexOf(href)==-1){
				result['linkPath']=href;
			} else{
				result['insertLinkOrig']="true";
			}
		}
		
		fckEditor.FCK.Selection.SelectNode(image);
	}
	
	// image tag already present
	if (fckEditor.FCK.Selection.HasAncestorNode("SPAN")) {
		imageWrap=parentDialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
		if (imageWrap && imageWrap.id.substring(0, 5) == ENHANCE_PREFIX){
			result['hash']=imageWrap.id.substring(5);
			var child=imageWrap.firstChild;
			while (child){
				if (child.tagName=="SPAN"){
					if (child.id.substring(0, 5) == COPY_PREFIX){
						result['copyright']=child.innerHTML;
						result['insertCopyright']="true";
					}
					if (child.id.substring(0, 5) == SUB_PREFIX){
						result['insertSubtitle']="true";
					}
				}
				child=child.nextSibling;
			}
		}
		fckEditor.FCK.Selection.SelectNode(image);
	}
	
	
	return result;
}

/**
 * Returns the availability of enhanced image options.<p>
 * 
 * @return <code>true</code> if enhanced image options are available
 */
function hasEnhancedImageOptions(){
	return fckConfig.ShowEnhancedOptions;
}

/**
 * Returns if lightbox should be used with enhanced image options.<p>
 * 
 * @return <code>true</code> if lightbox is available for enhanced image options
 */
function hasLightboxOption(){
	return hasEnhancedImageOptions() && fckConfig.UseTbForLinkOriginal;
}

/**
 * Returns the target attribute of the currently selected link. May be null or empty.<p>
 * 
 * @return the target attribute of the currently selected link
 */
function getLinkTarget(){
	var target="";
	if (_hasSelectedText()){
		var a = fckEditor.FCK.Selection.MoveToAncestorNode('A') ;
    	if (a) {
    		// link present
    		fckEditor.FCK.Selection.SelectNode(a);
        	// target attribute
    		target = a.getAttribute("target");
	    }	
	}
	return target;
}

/**
 * Inserts or updates a selected image, setting the given path and tag attributes.<p>
 * 
 * @param path <code>String</code> the image path (including optional cropping parameters)
 * @param attributes <code>JSONObject</code> a map of attribute to set on the image tag
 * 
 * @return void
 */
function setImage(path, attributes){
	
	var image=_getSelectedImage();
	
	var insertElement=image;
	
	
	
	// wrapping span element - used in enhanced mode
	var imageWrap=null;
	
	// wrapping anchor element
	var imageLink=null;
	
	// will be removed later
	var previousSelection=null;
	
	if (image){
		previousSelection=image;
		// looking for wrapping anchor
		if (fckEditor.FCK.Selection.HasAncestorNode("A")) {
			imageLink=parentDialog.Selection.GetSelection().MoveToAncestorNode("A");
			if (imageLink == null || imageLink.id.substring(0, 5) != LINK_PREFIX){
				imageLink=null;
			}
			fckEditor.FCK.Selection.SelectNode(image);
		}
		
		// image tag already present
		if (fckEditor.FCK.Selection.HasAncestorNode("SPAN")) {
			imageWrap=parentDialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
			if (imageWrap == null || imageWrap.id.substring(0, 5) != ENHANCE_PREFIX){
				imageWrap=null;
			}else{
				previousSelection=imageWrap;
			}
			
			
		}
		fckEditor.FCK.Selection.SelectNode(image);
		
		
	}else{
		// no image selected, create new tag
		image = fckEditor.FCK.InsertElement("img");
	}
	image.src = path;
	SetAttribute(image, "_fcksavedurl", path);
	
	// the attributes map contains only string values, so check for "true" or "false"
	if (hasEnhancedImageOptions() && (attributes.insertCopyright=="true" || attributes.insertSubtitle=="true")){
			// sub title and/or copyright information has to be inserted 
			insertElement = fckEditor.FCK.EditorDocument.createElement("SPAN");
			// now set the span attributes      
			var st = "width: " + attributes.width + "px;";
			var al = attributes.align;
			if (al == "left" || al == "right") {
				st += " float: " + al + ";";
			}
			var imgVSp = attributes.vspace;
			var imgHSp = attributes.hspace;
			if ((imgVSp && imgVSp != "") || (imgHSp && imgHSp != "")) {
				if (!imgVSp || imgVSp == "") {
					imgVSp = "0";
				}
				if (!imgHSp || imgHSp == "") {
					imgHSp = "0";
				}
				if (al || al != "") {
					var marginH = "right";
					if (al == "right") {
						marginH = "left";
					}
					st += "margin-bottom: " + imgVSp + "px; margin-" + marginH + ": " + imgHSp + "px;";
				} else {
					st += "margin: " + imgVSp + "px " + imgHSp + "px " + imgVSp + "px " + imgHSp + "px";
				}
			}
			insertElement.style.cssText = st;
			SetAttribute(insertElement, "id", ENHANCE_PREFIX + attributes.hash);
			
			// insert the image
			if (attributes.insertLinkOrig=="true") {
				var oLinkOrig = fckEditor.FCK.EditorDocument.createElement("A");
				_setLinkOriginalProperties(oLinkOrig, path, attributes)
				image.setAttribute("border", "0");
				oLinkOrig.appendChild(image);
				insertElement.appendChild(oLinkOrig);
			} else {
				// simply add image
				insertElement.appendChild(image);
			}
	
			if (attributes.insertCopyright=="true") {
				// insert the 2nd span with the copyright information
				var copyText = attributes.copyright;
	//			if (copyText == "") {
	//				copyText = "&copy; " + activeItem.copyright;
	//			}
				var oSpan2 = fckEditor.FCK.EditorDocument.createElement("SPAN");
				oSpan2.style.cssText = "display: block; clear: both;";
				oSpan2.className = "imgCopyright";
				oSpan2.id = COPY_PREFIX + attributes.hash;
				oSpan2.innerHTML = copyText;
				insertElement.appendChild(oSpan2);
			}
	
			if (attributes.insertSubtitle=="true") {
				// insert the 3rd span with the subtitle
				var altText = attributes.alt;
				if (altText == "") {
					altText = attributes.title;
				}
				var oSpan3 = fckEditor.FCK.EditorDocument.createElement("SPAN");
				oSpan3.style.cssText = "display: block; clear: both;";
				oSpan3.className = "imgSubtitle";
				oSpan3.id = SUB_PREFIX + attributes.hash;
				oSpan3.innerHTML = altText;
				insertElement.appendChild(oSpan3);
			}
			if (previousSelection){
				fckEditor.FCK.Selection.SelectNode(previousSelection);
				while (previousSelection.firstChild) {
					previousSelection.removeChild(previousSelection.firstChild);
				}
				//fckEditor.FCK.Selection.Delete();
			}
			fckEditor.FCK.InsertElementAndGetIt(insertElement);
	}else{
		if (previousSelection){
			fckEditor.FCK.Selection.SelectNode(previousSelection);
			while (previousSelection.firstChild) {
				previousSelection.removeChild(previousSelection.firstChild);
			}
			//fckEditor.FCK.Selection.Delete();
			fckEditor.FCK.InsertElement(insertElement);
		}
		
		fckEditor.FCK.Selection.SelectNode(image);
		
		// remove previous link
		if (imageLink){
			imageLink.removeAttribute("class");
			fckEditor.FCK.ExecuteNamedCommand("Unlink");
		}
		
		if (attributes.insertLinkOrig=="true"){
			imageLink=fckEditor.FCK.CreateLink("#")[0];
			_setLinkOriginalProperties(imageLink, path, attributes)
		}else if (attributes.linkPath!=null && attributes.linkPath.length>0){
			imageLink=fckEditor.FCK.CreateLink(attributes.linkPath)[0];
			SetAttribute(imageLink, "_fcksavedurl", attributes.linkPath);
			SetAttribute(imageLink, "target", attributes.linkTarget);
			imageLink.setAttribute("id", LINK_PREFIX + attributes.hash);
		}
	}
	
    _removeEnhancementAttributes(attributes);
	//iterating given attributes and setting them on the image tag
	if (attributes){
		for (var key in attributes){
			if (key=='clazz'){
				SetAttribute(image, 'class', attributes[key]);
			}else{
				SetAttribute(image, key, attributes[key]);
			}
		}
	}
}

function _removeEnhancementAttributes(attributes){
	attributes.copyright=null;
	attributes.hash=null;
    attributes.insertCopyright=null;
    attributes.insertLinkOrig=null;
    attributes.insertSpacing=null;
    attributes.insertSubtitle=null;
    attributes.linkPath=null;
    attributes.linkTarget=null;
}

/**
 * Inserts or updates an image link, setting the given image path and tag attributes as well as the link path and target attribute.<p>
 * 
 * @param path <code>String</code> the image path (including optional cropping parameters)
 * @param attributes <code>JSONObject</code> a map of attribute to set on the image tag 
 * @param linkPath <code>String</code> the path to link to
 * @param target <code>String</code> the target attribute, optional
 * 
 * @return void
 */
function setImageLink(path, attributes, linkPath, target){

}

/**
 * Sets a link to the given path.<p>
 * 
 * @param path <code>String</code> the path to link to
 * @param title <code>String</code> the title, will be used as link content if no text has been selected within the editor
 * @param target <code>String</code> the target attribute, optional
 * 
 * @return void
 */
function setLink(path, title, target){
	if (_hasSelectedText()){
		var a = fckEditor.FCK.Selection.MoveToAncestorNode('A') ;
	    if (a) {
	    	// link present, manipulate it
	    	fckEditor.FCK.Selection.SelectNode(a);
		    a = fckEditor.FCK.CreateLink(path)[0];
			
	    } else {
	    	// new link, create it
	        a = fckEditor.FCK.CreateLink(path)[0];        
	    }
	    
	    // set or remove target attribute
	    if (target!=null && target!="") {
			a.target = target;
		} else {
			a.removeAttribute("target");
		}
	    a.title = title;
	}else{
		var result = "<a href=\"" + path + "\" title=\"" + title;
		result += (target!=null && target!="") ? ("\" target=\"" + target) : "";
		result += "\">" + title + "<\/a>";
		fckEditor.FCK.InsertHtml(result);
	}
}

/**
 * Returns if a text part has been selected by the user.<p>
 * 
 * @return <code>true</code> if text is selected
 */
function _hasSelectedText() {
	var sel = parentDialog.Selection.GetSelection();
	var text = "";
	if (fckEditor.FCKSelection.GetSelection().createRange){
		text = fckEditor.FCKSelection.GetSelection().createRange().text;
	} else {
		text = fckEditor.FCKSelection.GetSelection();
	}
	
	if ((sel.GetType() == 'Text' || sel.GetType() == 'Control') && text != '') {
		return true;
	}
	return false; 
}

function _getSelectedImage(){
	var path=null;
	var selected=parentDialog.Selection.GetSelectedElement();
	if (selected && selected.tagName == "IMG"){
		// try to read selected url
		path = selected.getAttribute("_fcksavedurl");
		if (path == null) {
			path = GetAttribute(selected, "src", "");
		}
	}
	return (path && path.length>0) ? selected : null;
}

function _setLinkOriginalProperties(linkElement, path, attributes){
	if (path.indexOf("?")!=-1){
		path=path.substring(0, path.indexOf("?"));
	}
	if (hasLightboxOption()) {
		linkElement.href = path; //TODO: create lightbox link
		linkElement.setAttribute("title", attributes.title);
		linkElement.setAttribute("class", "thickbox");
	} else {
		linkElement.href = "#";
		var linkUri = "javascript:window.open('";
		linkUri += vfsPopupUri;
		linkUri += "?uri=";
		linkUri += path;
		linkUri += "', 'original', 'width=";
		linkUri += attributes.width;
		linkUri += ",height=";
		linkUri += attributes.height;
		linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
		linkElement.setAttribute("onclick", linkUri); //TODO: create popup link
	}
	linkElement.setAttribute("id", LINK_PREFIX + attributes.hash);
}


//TODO: specify and implement extended image functions