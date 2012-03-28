<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%
   
// get gallery instance
A_CmsAjaxGallery wp = new CmsAjaxDownloadGallery(pageContext, request, response); 

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

/** The editor instance. */
var parentDialog = window.parent; 
var editor=parentDialog.tinymce.activeEditor;
var tinymce = parentDialog.tinymce;

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
   $("#gallerycancelbutton").click(function() {tinyMCEPopup.close(); } )
   $("#categorycancelbutton").remove();
   $("#categoryokbutton").remove();
   $("#galleryresetsearchbutton").hide();
   $("#categoryresetsearchbutton").hide();
   
   if (hasSelectedText() == true) {
      loadItemSitepath = getSelectedLinkUri();
   }
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
      link(activeItem.linkpath, activeItem.title, activeItem.description);
   }
   tinyMCEPopup.close();
   return true;
}

function link(uri, title, desc) {
   tinyMCEPopup.restoreSelection();      
   if (hasSelectedText() == true) {
      // text selected.
      setLink(uri, title); 
   } else {
      pasteLink(uri, title, desc);
   }
      
}

/* Pastes a link to the current position of the editor */
function pasteLink(uri, title, desc) {
   
   var result = "<a href=\"";
   result += uri;
   result += "\" title=\"";
   result += escapeBrackets(desc);
   result += "\" target=\"";
   if (modeType == "gallery") {
      result += $("#gallerylinktarget").val();
   } else {
      result += $("#categorylinktarget").val();
   }
   result += "\">";
   result += escapeBrackets(title);
   result += "<\/a>";
   insertHtml(result);
}


// checks if a text part has been selected by the user
function hasSelectedText() {
   return !editor.selection.isCollapsed() && editor.selection.getContent() != '';
}


function setLink(uri, title) {
   
   var linkInformation = new Object();
   linkInformation["type"] = "link";
   linkInformation["href"] = uri;
   if (modeType == "gallery") {
      linkInformation["target"] = $("#gallerylinktarget").val();
   } else {
      linkInformation["target"] = $("#categorylinktarget").val();
   }
   linkInformation["style"] = "";
   linkInformation["class"] = "";
   linkInformation["title"] = title;
   createLink(linkInformation);
      
}  

function getSelectedLinkUri() {
   var loadItem = new ItemSitepath();
   var a = _selectionMoveToAncestorNode('A') ;
   if (a) {
         editor.selection.setNode(a);
         //path to resource
         loadItem.path = a.getAttribute("data-mce-href");
         // target attribute
         loadItem.target = a.getAttribute("target");
   }
   return loadItem;
}


function _editorCreateLink(target) {
   var linkAttrs = {href: target};
   linkAttrs["class"] = 'cmsInsertedLink';
   editor.execCommand("mceInsertLink", false, linkAttrs);
   var selectionNode = editor.selection.getNode();
   var result = editor.dom.select(".cmsInsertedLink", selectionNode)[0];
   editor.dom.removeClass(result, "cmsInsertedLink");
   return result; 
}

function _selectionMoveToAncestorNode(nodeName) {
   var node = editor.selection.getNode();
   return editor.dom.getParent(node, nodeName);
}



function createLink(linkInformation){
   var path = linkInformation["href"];
   var title = linkInformation["title"];
   var target = linkInformation["target"];
   tinyMCEPopup.restoreSelection();
   var a = _selectionMoveToAncestorNode('A') ;
   if (a) {
        // link present, manipulate it
        editor.selection.setNode(a);
        a = _editorCreateLink(path);
   } else {
        // new link, create it
        a = _editorCreateLink(path);        
     }
     // set or remove target attribute
     if (target!=null && target!="") {
         a.setAttribute("target", target);
         a.target = target;
     } else {
         a.removeAttribute("target");
     }
     a.title = title;
}





// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
   editor.execCommand("mceInsertContent", false, htmlContent);
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