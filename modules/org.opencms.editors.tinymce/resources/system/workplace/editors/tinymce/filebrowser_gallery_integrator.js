<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%>

function SetAttribute( element, attName, attValue )
{
   if ( attValue == null || attValue.length == 0 )
      element.removeAttribute( attName, 0 ) ;         // 0 : Case Insensitive
   else
      element.setAttribute( attName, attValue, 0 ) ;  // 0 : Case Insensitive
}

function GetAttribute( element, attName, valueIfNull )
{
   var oAtt = element.attributes[attName] ;

   if ( oAtt == null || !oAtt.specified )
      return valueIfNull ? valueIfNull : '' ;

   var oValue = element.getAttribute( attName, 2 ) ;

   if ( oValue == null )
      oValue = oAtt.nodeValue ;

   return ( oValue == null ? valueIfNull : oValue ) ;
}

<%= cms.getContent("/system/workplace/resources/editors/tinymce/jscripts/tiny_mce/tiny_mce_popup.js") %>
/**
 * The JavaScript functions of this file serve as an interface between the API of the TinyMCE and the gallery dialog.<p>
 *
 * Following function needs to be provided by the gallery dialog:<p>
 * 
 * boolean setDataInEditor()<p>
 * 
 * This should check if further user input is required and other wise set the selected resource via the provided functions <code>setLink</code> and <code>setImage</code>.<p>
 * Returning <code>true</code> when all data has been set and the dialog should be closed.<p>
 */
/* absolute path to the JSP that displays the image in original size */
var vfsPopupUri = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";
var showSelect = "true";

/** The editor frame. */
var parentDialog=window.parent;

// remove loading overlay and get editor reference
/** The editor instance. */
var editor=parentDialog.tinymce.activeEditor;
var tinymce = parentDialog.tinymce;

/** The fck editor configuration. */
var editorConfig= {};

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

var fieldId = editor.cmsFieldId;
var targetWindow = editor.cmsTargetWindow; 


/**
 * Function will be triggered by the editor dialog OK button. 
 * Will check if current gallery state allows closing and sets the selected resource parameters.<p>
 * 
 * @return <code>true</code> to close the dialog
 */
var Ok =function(){
    return setDataInEditor();
}

/**
 * Closes the dialog without setting any data.<p>
 */
function closeDialog(){
   tinyMCEPopup.close();
}

/**
 * Triggers the OK action.<p>
 * 
 * @return void
 */
function dialogOk(){
//    parentDialog.Ok();
}

function enableDialogOk(enabled){
}






/**
 * Returns all available information of the selected image tag, or null, if no image is selected.<p>
 * 
 * @return a map with the following keys: 
 *          alt, clazz, height, hspace, linkPath, linkTarget, longDesc, style, title, vspace, width
 * 
 *          all keys represent a tag attribute by the same name, only linkPath and linkTarget contain
 *          information on an surrounding link tag
 */
function getImageInfo(){
        return {emptySelection: "emptySelection"};
}


/**
 * Returns the availability of enhanced image options.<p>
 * 
 * @return <code>true</code> if enhanced image options are available
 */
function hasEnhancedImageOptions(){
   return false; 
}

/**
 * Returns if lightbox should be used with enhanced image options.<p>
 * 
 * @return <code>true</code> if lightbox is available for enhanced image options
 */
function hasLightboxOption(){
   return false;  
}

/**
 * Returns the target attribute of the currently selected link. May be null or empty.<p>
 * 
 * @return the target attribute of the currently selected link
 */
function getLinkTarget(){
   return ""; 
}

function _setValue(url) {
   var inputField = targetWindow.document.getElementById(fieldId);
   inputField.value = url;
   tinyMCEPopup.close(); 
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
   _setValue(path); 
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
   _setValue(path);
}


