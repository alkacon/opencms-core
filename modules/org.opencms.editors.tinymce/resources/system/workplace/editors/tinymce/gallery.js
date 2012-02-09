<%@ page import="org.opencms.jsp.*,
                 org.opencms.workplace.*,                 
             org.opencms.main.*,
             org.opencms.file.types.CmsResourceTypeImage" %><%
%><%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement galleryCms = new CmsJspActionElement(pageContext, request, response); 
   pageContext.setAttribute("cms", galleryCms);
   CmsDialog dialog = new CmsDialog(pageContext, request, response);
   pageContext.setAttribute("locale", dialog.getLocale().toString());
    String itemResType = CmsResourceTypeImage.getStaticTypeName();
%><fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.ade.galleries.messages">
var galleryDialog = null;

/**
 * Closes the gallery dialog.
 */
function closeGalleryDialog() {
   if (galleryDialog) {
      tinymce.activeEditor.windowManager.close(null, galleryDialog.id);
   }
} 

/**
 * Opens the image gallery popup.
 */
function doShowCmsGalleries(editor, url) {
   var width = 685;
   var height = 502;
   galleryDialog = editor.windowManager.open({url: url, width : width, height: height, inline: "yes"}, {});
}


/**
 * Installs the gallery functionality for TinyMCE.
 */
function initGalleries(editor) {
   editor.addCommand("cmsImageGallery", function() {
      doShowCmsGalleries(editor, imageGalleryDialogUrl());
   });
   
   editor.addCommand("cmsDownloadGallery", function() {
      doShowCmsGalleries(editor, downloadGalleryDialogUrl());
   });
   
   
   editor.addButton('oc-imagegallery', {
      title: '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />',
      image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-imagegallery.gif")%>',
      cmd: "cmsImageGallery"
    });
   
   editor.addButton('oc-downloadgallery', {
      title: '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
      image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-downloadgallery.gif")%>',
      cmd: "cmsDownloadGallery"
   });
   
   editor.onInit.add(function(ed) {
      ed.plugins.contextmenu.onContextMenu.add(filterContextMenu);
   });
}



/**
 * Processes context menu items, e.g. by replacing built-in image options with OpenCms specific image options.
 */
function filterContextMenu(sender, menu, element) {
   var otherItems = {};
   for (var itemName in menu.items) {
      if (/^mce_/.test(itemName)) {
         var item = menu.items[itemName];
         if (item.settings) {
            if (item.settings.cmd == 'mceAdvImage' || item.settings.cmd == 'mceImage') {
               continue;
            }
         }
      }
      otherItems[itemName] = item;
   }
   menu.items = otherItems;
   if (element.nodeName === 'IMG') {
      menu.add({title : '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />', icon : 'media', cmd : 'cmsImageGallery'});
   }
};


/**
 * Searches for a frame by the specified name. Will only return siblings or ancestors.<p>
 * 
 * @return <code>Frame</code> the frame or <code>null</code> if no matching frame is found
 */ 
function findFrame(startFrame, frameName){
    if (startFrame == top){
        // there may be security restrictions prohibiting access to the frame name
        try{
            if (startFrame.name == frameName){
                return startFrame;
            }
        }catch(err){}
        return null;
    }
    for (var i=0; i<startFrame.parent.frames.length; i++){
        // there may be security restrictions prohibiting access to the frame name
        try{
            if (startFrame.parent.frames[i].name == frameName) {
                return startFrame.parent.frames[i];
            }
        }catch(err){}
    }
    return findFrame(startFrame.parent, frameName);
}

function getImageSelectionPath() {
   var selected = tinymce.activeEditor.selection.getNode();
   var path; 
   if (selected && ( selected.tagName == "IMG" || selected.tagName == "SPAN" || (selected.tagName == "INPUT" && selected.type == "image"))){
      // try to read selected url
      path = selected.getAttribute("data-mce-src");
      if (path == null) {
         path = selected.getAttribute("src");
      }
   }
   return path; 
}

function getDownloadSelectionPath() {
   var editor = tinymce.activeEditor;
   var path; 
   if (editor.selection.getContent() != '') {
      var a = editor.dom.getParent(editor.selection.getNode(), 'A');
      if (a) {
         // link present
         editor.selection.select(a, false);
         //path to resource
         path = a.getAttribute("data-mce-href");
         // in case of a newly created link, use the href attribute
         if (path == null || path==""){
            path=a.getAttribute("href");
         }
        }
   }
   return path; 
}



/**
 * Returns the path to the gallery dialog with some request parameters for the dialog.<p>
 * 
 * @return <code>String</code> the dialog URL
 */ 
function createGalleryDialogUrl(path, typesParam) {
   var resParam = "";
    var editFrame=findFrame(self, 'edit');
   if (editFrame.editedResource != null) {
      resParam = "&resource=" + editFrame.editedResource;
   } else {
      resParam = "&resource=" + editFrame.editform.editedResource;
   }
   var integratorParam = "&integrator=/system/workplace/editors/tinymce/integrator.js";
   var debugParam = "";   
   // uncomment the next line for debugging GWT code 
   //debugParam="&gwt.codesvr=localhost:9997";
   // set the content locale
   var elementLanguage="${locale}";
   try{
       elementLanguage=editFrame.editform.document.forms['EDITOR']['elementlanguage'].value;
   }catch(err){
       // nothing to do
   }
   var searchParam = "&types="+typesParam+"&currentelement="+ ( path==null ? "" : path)+"&__locale="+elementLanguage;
   return "<%= cms.link("/system/modules/org.opencms.ade.galleries/gallery.jsp") %>?dialogmode=editor" + searchParam + resParam + integratorParam + debugParam;
}

function imageGalleryDialogUrl() {
   return createGalleryDialogUrl(getImageSelectionPath(), "image");
}


/**
 * Returns the path to the download gallery dialog with some request parameters for the dialog.<p>
 * 
 * @return <code>String</code> the dialog URL
 */ 
function downloadGalleryDialogUrl() {
   return createGalleryDialogUrl(getDownloadSelectionPath(), "binary");
}


</fmt:bundle>
