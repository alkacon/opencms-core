<%@ page import="
   org.opencms.i18n.CmsEncoder,
   org.opencms.jsp.*,
   org.opencms.workplace.editors.*,
   org.opencms.workplace.galleries.*,
   org.opencms.main.*,
   org.opencms.workplace.*,
   org.opencms.editors.tinymce.*,
   java.util.*,
   org.opencms.file.types.CmsResourceTypeImage" %><%
%><%@ 
taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@ 
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ 
taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response); 
   pageContext.setAttribute("cms", cms);
   Locale locale=OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject());
   pageContext.setAttribute("locale", locale.toString());
    String itemResType = CmsResourceTypeImage.getStaticTypeName();
    CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);
String encoding = CmsEncoder.ENCODING_US_ASCII;

%><fmt:setLocale value="${locale}" />
<fmt:bundle basename="org.opencms.ade.galleries.messages">

function initOpenCmsTinyMCEPlugin() {
   if (tinymce['opencms']) return; 

/**
 * Opens the image gallery popup.
 */
function doShowCmsGalleries(editor, url) {
   var width = window.innerWidth;
   var height = window.innerHeight;
   // HACK: check for IE8 and add some attributes to iframe to prevent layout issues
   if (typeof document.addEventListener =='undefined'){
       // let's call this iframe attribute injection
       url+="\" allowtransparency=\"true\" scrolling=\"no\" frameborder=\"0\" framespacing=\"0"
   }
   editor.windowManager.open({
   	   title: "DIALOG",
   	   body: {
   	       type: "panel",
   	       items: [{
   	           type: "htmlpanel",
   	           html: "<iframe src=\""+url+"\" style=\"height:"+height+"px; width:"+width+"px; position:fixed; top: 0px; left:0px;\" />"
   	       }]
   	   },
   	   buttons: [
          {
            type: 'custom',
            name: 'close',
            text: 'Close',
            disabled: false
          }
        ],
       classes: "opencmsDialog",
       onAction: function (dialogApi, actionData) {
          if (actionData.name === 'close') {
          dialogApi.close();
          }
        }
   }, {});
   var cssClasses=document.body.getAttribute("class")+" "+OPENCMS_DIALOG_CLASS;
   document.body.setAttribute("class", cssClasses);
}

var OPENCMS_DIALOG_CLASS="opencms_dialog";

function removeOpenCmsDialogStyle(){
	var cssClasses=document.body.getAttribute("class");
	cssClasses=cssClasses.replace(/ opencms_dialog/g,"");
	document.body.setAttribute("class", cssClasses);
}

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
        try {
            if (startFrame.parent.frames[i].name == frameName) {
                return startFrame.parent.frames[i];
            }
        } catch(err){}
		try {
            if (startFrame.parent.frames[i].frameElement.name == frameName) {
                return startFrame.parent.frames[i];
            }
        } catch(err){}

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


function getEditResource() {
   if (typeof _editResource != 'undefined') {
      return _editResource;
   } else {
      var editFrame = findFrame(self, 'edit'); 
      var result = editFrame.editedResource; 
      if (result != null) {
         return result;
      }
      result = editFrame.editform.editedResource;
      return result; 
   }
}

/**
 * Returns the path to the gallery dialog with some request parameters for the dialog.<p>
 * 
 * @return <code>String</code> the dialog URL
 */ 
function createGalleryDialogUrl(path, typesParam, integrator, integratorArgs) {
   var resParam = "";
   var editFrame=window;
   if (typeof _editResource=='undefined'){
       editFrame=findFrame(self, 'edit');
   }
   var editResource = getEditResource();
   if (editResource) {
      resParam = "&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH%>=" + getEditResource(); 
   }
   var integratorParam = "&integrator="+integrator+"&integratorArgs="+integratorArgs; 
   
   var debugParam = "";   
   // uncomment the next line for debugging GWT code 
   //debugParam="&gwt.codesvr=localhost:9997";
   // set the content locale
   var elementLanguage="${locale}";
   if (typeof _editLanguage!='undefined'){
       elementLanguage=_editLanguage;
   } else{
       try{
           elementLanguage=editFrame.editform.document.forms['EDITOR']['elementlanguage'].value;
       }catch(err){
           // nothing to do
       }
   }
   var searchParam;
   if (typesParam=="all"){
       searchParam="&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG%>=selectAll&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.PARAM_USE_LINK_DEFAULT_TYPES%>=true";
   }else{
       searchParam="&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG%>=selectDoc&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_RESOURCE_TYPES%>="+typesParam;
   }
   searchParam+="&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_CURRENT_ELEMENT%>="+ ((path==null|| path.indexOf("#")==0)? "" : path)+"&__locale="+elementLanguage;
   var galleryStoragePrefixParam = "&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_GALLERY_STORAGE_PREFIX%>=";
   if (typesParam == "image") { 
       galleryStoragePrefixParam += "image";
   } else if (typesParam == "binary") {
       galleryStoragePrefixParam += "binary"; 
   } else {
       galleryStoragePrefixParam += "linkselect";
       // leave the parameter empty  
   } 
   
   return "<%= cms.link("/system/workplace/commons/gallery.jsp") %>?<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_GALLERY_MODE+"="+org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode.editor.name() %>" + searchParam + resParam + galleryStoragePrefixParam + integratorParam + debugParam;
}


var DEFAULT_INTEGRATOR =  "/system/workplace/editors/tinymce/integrator.js";
function imageGalleryDialogUrl() {
    var editor = tinymce.activeEditor;
   var result = createGalleryDialogUrl(getImageSelectionPath(), "image", DEFAULT_INTEGRATOR, "mode:imagegallery");
   if (editor.settings.imageGalleryConfig) {
       result += _paramsForEmbeddedOptions(editor.settings.imageGalleryConfig); 
   }
   return result;
}


/**
 * Returns the path to the download gallery dialog with some request parameters for the dialog.<p>
 * 
 * @return <code>String</code> the dialog URL
 */ 
function downloadGalleryDialogUrl() {
    var editor = tinymce.activeEditor;
   var result =createGalleryDialogUrl(getDownloadSelectionPath(), "binary", DEFAULT_INTEGRATOR, "mode:downloadgallery");
   if (editor.settings.downloadGalleryConfig) {
       result += _paramsForEmbeddedOptions(editor.settings.downloadGalleryConfig); 
   }
   return result; 
}



function _paramsForEmbeddedOptions(config) {
    var result = ""; 
    if (config.gallerytypes) {
        result += "&gallerytypes=" + config.gallerytypes; 
    }
    if (config.gallerypath) {
        result += "&gallerypath=" + config.gallerypath;
    }
    return result; 
}

function linkGalleryDialogUrl() {
   var editor = tinymce.activeEditor;
   var result =createGalleryDialogUrl(getDownloadSelectionPath(), "pointer", DEFAULT_INTEGRATOR, "mode:downloadgallery");
   return result;
}

var USE_LINKSTYLEINPUTS = <%= options.showElement("option.linkstyleinputs", displayOptions) %>;

// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
   tinymce.activeEditor.execCommand("mceInsertContent", false, htmlContent);
}

// checks if a text part has been selected by the user
function hasSelectedText() {
   var sel;
   var content = tinymce.activeEditor.selection.getContent();
   return content && content != '';
}

tinymce.create('tinymce.opencms', {
   init : function(ed, url) {
      
      // periodically check if we are in fullscreen mode and the content has changed.
      // if this is the case, transfer the content from the fullscreen editor to the editor from which
      // the fullscreen mode has been started.
      
      var checkFullscreen = function() {
         var active = tinyMCE.activeEditor;
         if (active && (active.id == "mce_fullscreen")) {
           actualEditor = tinyMCE.get(active.settings.fullscreen_editor_id);
           var oldContent = tinyMCE.trim(actualEditor.getContent({format : 'raw', no_events : 1}));
           var newContent = tinyMCE.trim(active.getContent({format : 'raw', no_events : 1}));
           if (oldContent != newContent) {
             actualEditor.setContent(newContent);
           }
        }
     }
      
     if (!window.installedFullscreenSaveTimer) {
         window.setInterval(checkFullscreen, 600);
         window.installedFullscreenSaveTimer = true; 
     }
      
      ed.addCommand("removeDialogStyle", function() {
         removeOpenCmsDialogStyle();
      });
      
      ed.ui.registry.addIcon('download-gallery','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill-rule="nonzero" d="M2.8 2c-.4 0-.8.5-.8.9v17.2c0 .4.4.9.8.9H5v-2H4V7h16v12h-1v2h2.2c.4 0 .8-.5.8-1V6c0-.5-.4-1-.9-1H11L8 2zm0 0"/><path fill-rule="nonzero" d="M7 8c-.5 0-1 .5-1 1v13c0 .5.5 1 1 1h10c.5 0 1-.5 1-1v-8l-6-6zm0 1h5v4c0 .5.5 1 1 1h4v8H7zm0 0"/></svg>');
      ed.ui.registry.addIcon('image-gallery','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill-rule="nonzero" d="M2.8 2c-.4 0-.8.5-.8 1v17c0 .5.4 1 .8 1h18.4c.4 0 .8-.5.8-1V6c0-.5-.4-1-.9-1H11L8 2zM4 7h16v12H4zm0 0"/><path fill-rule="nonzero" d="M5 17v1h14v-3l-4-3-5 4-2-2zm0 0M10 11a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm0 0"/></svg>');
      ed.ui.registry.addIcon('link-gallery','<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill-rule="nonzero" d="M2.8 2c-.4 0-.8.4-.8.9v17.2c0 .5.4.9.8.9h18.4c.4 0 .8-.4.8-.9V5.9c0-.5-.4-1-.9-1H11L8 2zM4 7h16v12H4zm0 0"/><path fill-rule="nonzero" d="M16 14v2c0 1-1 2-2 2H8c-1 0-2-1-2-2v-5c0-1 1-2 2-2h5v1H8c-.5 0-1 .5-1 1v5c0 .5.5 1 1 1h6c.5 0 1-.5 1-1v-2zm3-5.5v4c-.1.5-.5.6-.9.4l-1.4-1.4-4.4 4.5-1.3-1.3 4.5-4.4-1.4-1.4c-.2-.4-.1-.8.4-.9h4c.3 0 .5.3.5.5zm0 0"/></svg>');
      ed.ui.registry.addButton('OcmsImageGallery', {
         tooltip: '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />',
         icon: 'image-gallery',
         onAction: function() {
         		doShowCmsGalleries(ed, imageGalleryDialogUrl());
      		}
       });
      
      ed.ui.registry.addButton('OcmsDownloadGallery', {
         tooltip: '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
         icon: 'download-gallery',
         onAction: function() {
         		doShowCmsGalleries(ed, downloadGalleryDialogUrl());
      		}
      });
     
      ed.ui.registry.addButton('OcmsLinkGallery', { 
    	 tooltip : '<%=org.opencms.workplace.galleries.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.galleries.Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0)%>',
    	 icon: 'link-gallery',
         onAction: function() {
        	 doShowCmsGalleries(ed, linkGalleryDialogUrl());
      	}
      });
      ed.ui.registry.addMenuItem('OcmsImageGallery', {
         text: '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />',
         context: 'tools',
         icon: 'image',
         onAction: function() {
         		doShowCmsGalleries(ed, imageGalleryDialogUrl());
      		}
       });
      ed.ui.registry.addMenuItem('OcmsDownloadGallery', {
          text: '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
          context: 'tools',
          icon: 'browse',
          onAction: function() {
         		doShowCmsGalleries(ed, downloadGalleryDialogUrl());
      		}
       });
      ed.ui.registry.addMenuItem('OcmsLinkGallery', {
          text: '<%=org.opencms.workplace.galleries.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.galleries.Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0)%>',
          context: 'tools',
          icon: 'link-gallery',
          onAction: function() {
         		doShowCmsGalleries(ed, linkGalleryDialogUrl());
      		}
       });
   },

   getInfo : function() {
      return {
         longname : 'OpenCms TinyMCE plugin',
         author : 'Alkacon Software GmbH & Co. KG',
         authorurl : 'http://www.opencms.org',
         infourl : 'http://wwww.opencms.org',
         version : '1.0'
      };
   }
});

window.cmsTinyMceFileBrowser = function (callback, currentValue, meta) {
   var editor = tinymce.activeEditor;
   var browserType=meta.fileType;
   var resourceType="all";
   if (browserType == "image") {
       resourceType=browserType;
   } 
   var integrator = "/system/workplace/editors/tinymce/filebrowser_gallery_integrator.js"
   var url = createGalleryDialogUrl(currentValue, resourceType, integrator);
   url = url + "&hideformats=true";
   editor.cmsSetValueFunction = callback; 
   doShowCmsGalleries(editor, url);
}

tinymce.PluginManager.add('opencms', tinymce.opencms);
} // end function initOpenCmsTinyMCEPlugin

initOpenCmsTinyMCEPlugin();



</fmt:bundle>