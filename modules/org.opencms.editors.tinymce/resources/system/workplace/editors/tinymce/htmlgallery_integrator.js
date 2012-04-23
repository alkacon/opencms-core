<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %>
<%   
// get gallery instance
A_CmsAjaxGallery wp = new CmsAjaxHtmlGallery(pageContext, request, response); 

String editedResource = "";
if (CmsStringUtil.isNotEmpty(wp.getParamResource())) {
   editedResource = wp.getParamResource();
}

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
<%= wp.getJsp().getContent("/system/workplace/resources/editors/tinymce/jscripts/tiny_mce/tiny_mce_popup.js") %>
/* Initialize important FCKeditor variables from editor. */
var dialog     = window.parent;


/* Size of the preview area. */
previewX = 600;
previewY = 230;

itemsPerPage = 7;

/* Initialize the dialog values. */
initValues = {};
initValues.dialogmode = "<% if (CmsStringUtil.isEmpty(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE))) { out.print(""); } else { out.print(request.getParameter(A_CmsAjaxGallery.PARAM_DIALOGMODE)); } %>";
initValues.viewonly = false;
initValues.editedresource = "<%= editedResource %>";
//saves the type of the gallery: 'gallery' or 'category'. It is used to choose the appropriate select box in widget mode.
var modeType = "";

/* Initializes the download gallery popup window. */
function initPopup() {
   
   $("#cancelbutton").click(function() {tinyMCEPopup.close(); } )
   $("head").append("<style type='text/css'>#galleryitemlist { height: 457px !important; } #galleryitemlistinner {  height: 385px !important; } #categoryfolderlist { height: 515px !important; }</style>");
   $("#galleryresetsearchbutton").hide();
   $("#categoryresetsearchbutton").hide();
   
   //always open the gallery tab
   $tabs.tabs("select", 1);
   $tabs.tabs("disable", 0);
   
   setTimeout("getGalleries();", 50);
   setTimeout("getCategories();", 100);
}

/* Do additional stuff when active item is loaded. */
function activeItemAdditionalActions() {
   // activate the "OK" button of the dialog
   window.parent.SetOkButton(true);
}

/* The OK button was hit, called by editor button click event. */
function Ok() {

   if (activeItem != null && activeItem != "") {
      var htmlContent = activeItem.html;
      // convert to string before inserting 
      insertHtml(htmlContent.toString());
   }
   tinyMCEPopup.close();
}

// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
   tinyMCEPopup.restoreSelection();
   window.parent.tinymce.activeEditor.execCommand("mceInsertContent", false, htmlContent); 
}

/* OK Button was pressed, stores the item information back in the editor fields. */
function okPressed() {
   if (activeItem != null && activeItem != "") {
      var htmlContent = activeItem.html;
      // convert to string before inserting 
      insertHtml(htmlContent.toString());
   }
   tinyMCEPopup.close();
}



function escapeBrackets(s) {
   var searchResultStart = s.search(/\[.+/);
   var searchResultEnd = s.search(/.+\]/);
   var cut = (searchResultStart == 0 && searchResultEnd != -1 && s.charAt(s.length - 1) == ']');
   if (cut) {
      // cut off the first '['
      s = s.substring(1,s.length);
      // cut off the last ']'
      s = s.substring(0,s.length-1);
   }

   return s;
}