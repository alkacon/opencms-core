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

var parentDialog=window.parent;

// remove loading overlay and get editor reference
var fckEditor= parentDialog.InnerDialogLoaded();

/**
 * Function will be triggered by the editor dialog OK button. 
 * Will check if current gallery state allows closing and sets the selected resource parameters.<p>
 * 
 * @return <code>true</code> to close the dialog
 */
function Ok(){
	return closeGalleryDialog();
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
 * Returns all available information of the selected image tag, or null, if no image is selected.<p>
 * 
 * @return a map with the following keys: 
 * 			alt, class, height, hspace, linkPath, linkTarget, longDesc, style, title, vspace, width
 * 
 * 			all keys represent a tag attribute by the same name, only linkPath and linkTarget contain
 * 			information on an surrounding link tag
 */
function getImageInfo(){}

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
function setImage(path, attributes){}

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
function setImageLink(path, attributes, linkPath, target){}

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
		var result = "<a href=\"" + uri + "\" title=\"" + title;
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



//TODO: specify and implement extended image functions