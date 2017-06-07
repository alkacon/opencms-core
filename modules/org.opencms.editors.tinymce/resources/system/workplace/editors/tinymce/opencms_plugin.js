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

var workplacePath="<%= cms.link("/system/workplace/") %>";

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
       url: url, 
       width : width, 
       height: height, 
       inline: "yes", 
       classes: "opencmsDialog"
   }, {});
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
   searchParam+="&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_CURRENT_ELEMENT%>="+ ( path==null ? "" : path)+"&__locale="+elementLanguage;
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
      var resParam = "";
       var editFrame=findFrame(self, 'edit');
       var editResource = getEditResource();
       if (editResource) {
          resParam = "&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH%>=" + editResource;           
       }
      var baseLink =  "<cms:link>/system/workplace/galleries/linkgallery/index.jsp</cms:link>";
      var integrator = "/system/workplace/editors/tinymce/linkgallery_integrator.js";
      var integratorParam = "&integrator=" + integrator;  
      return baseLink + "?dialogmode=editor"+resParam+integratorParam;
   
}

var USE_LINKSTYLEINPUTS = <%= options.showElement("option.linkstyleinputs", displayOptions) %>;

// opens the link dialog window
function openLinkDialog(errorMessage) {
   var linkType = "link";
   if (hasSelectedText()) {
      var winheight;
      var winwidth;
      winheight = (USE_LINKSTYLEINPUTS?220:170);
      winwidth = 480;
      var linkInformation = getSelectedLink();
      var params = "?showCss=" + USE_LINKSTYLEINPUTS;
      if (linkInformation != null) {
         params += "&href=" + encodeURIComponent(linkInformation["href"]);
         params += "&target=" + linkInformation["target"];
         params += "&title= "+linkInformation["title"];
         if (USE_LINKSTYLEINPUTS) {
            params += "&style=" + linkInformation["style"];
            params += "&class=" + linkInformation["class"];
         }
      }
      openWindow = window.open(workplacePath + "editors/dialogs/" + linkType + ".jsp" + params, "SetLink", "width=" + winwidth + ", height=" + winheight + ", resizable=yes, top=300, left=250");
      openWindow.focus();
    } else {
      alert(errorMessage);
    }
}

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

// retrieves the information about the selected link
function getSelectedLink() {
   var linkInformation = null;

   // get the element of the current selection
   var thelink = tinymce.activeEditor.selection.getNode();
   if (thelink) {
      if (/^img$/i.test(thelink.tagName)) {
            thelink = thelink.parentNode;
      }
      if (!/^a$/i.test(thelink.tagName)) {
            thelink = null;
      }
   }

   if (thelink != null) {
      var linkUri = thelink.getAttribute("href", 0);
      linkInformation = new Object();
      linkInformation["href"] = encodeURIComponent(linkUri);
      linkInformation["name"] = thelink.name;
      linkInformation["target"] = thelink.target;
      linkInformation["title"] = thelink.title;
      if (USE_LINKSTYLEINPUTS) {
         linkInformation["class"] = thelink.getAttribute("class", 0);
         linkInformation["style"] = thelink.style.cssText;
      }     
   }
   return linkInformation;
}

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {
    var editor = tinymce.activeEditor;
    var attributes = {};
    var a = editor.dom.getParent(editor.selection.getNode(), "A");
    if (a) {
       a.removeAttribute("target");
       a.removeAttribute("class");
       a.removeAttribute("title");
       a.removeAttribute("style");
    }

    attributes.href = linkInformation.href; 
    if (linkInformation["target"] != "") {
       attributes.target = linkInformation.target;
    }
    if (linkInformation["title"] != null && linkInformation["title"] != "") {
      attributes.title = linkInformation["title"];
    }
   
   if (USE_LINKSTYLEINPUTS) {
      if (linkInformation["class"] != "") {
         attributes['class'] = linkInformation['class'];
      }
      if (linkInformation["style"] != "") {
         attributes.style = linkInformation.style;
      }
   }
   editor.execCommand('mceInsertLink', false, attributes);
}

//create the path to the HTML gallery dialog with some request parameters for the dialog
function htmlGalleryDialogUrl() {
   var resParam = "";
   var editFrame = findFrame(self, "edit");
   resParam = "&<%=org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH%>=" + getEditResource(); 
   var integratorUri = "/system/workplace/editors/tinymce/htmlgallery_integrator.js";
   var integratorParam = "&integrator="+integratorUri; 
   return "<%= cms.link("/system/workplace/galleries/htmlgallery/index.jsp") %>?dialogmode=editor" + resParam +integratorParam;
}


// set it in window, because this is used from the link selection dialog 
window.createLink = createLink; 

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
      
      ed.addCommand("cmsImageGallery", function() {
         doShowCmsGalleries(ed, imageGalleryDialogUrl());
      });
      
      ed.addCommand("cmsDownloadGallery", function() {
         doShowCmsGalleries(ed, downloadGalleryDialogUrl());
      });
      
      ed.addCommand("cmsLink", function() {
         openLinkDialog("<%= CmsEncoder.encodeJavaEntities(OpenCms.getWorkplaceManager().getMessages(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.editors.Messages.ERR_EDITOR_MESSAGE_NOSELECTION_0), encoding) %>");
      });
      
      ed.addCommand("cmsHtmlGallery", function() {
         var width = 685;
         var height = 610;
         var url = htmlGalleryDialogUrl();
         ed.windowManager.open({url: url, width : width, height: height, inline: "yes"}, {});
      });
      
      ed.addCommand("cmsLinkGallery", function() {
         var width = 685;
         var height = 600;
         var url = linkGalleryDialogUrl();
         ed.windowManager.open({url: url, width : width, height: height, inline: "yes"}, {});
      });
      
      ed.addButton('OcmsImageGallery', {
         title: '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />',
         image: '<%= org.opencms.workplace.CmsWorkplace.getStaticResourceUri("editors/tinymce/toolbar/oc-imagegallery.png") %>',
         cmd: "cmsImageGallery"
       });
      
      ed.addButton('OcmsDownloadGallery', {
         title: '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
         image: '<%= org.opencms.workplace.CmsWorkplace.getStaticResourceUri("editors/tinymce/toolbar/oc-downloadgallery.png") %>',
         cmd: "cmsDownloadGallery"
      });
      
      ed.addButton('oc-link', {
         title: '<%= CmsEncoder.encodeJavaEntities(OpenCms.getWorkplaceManager().getMessages(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.editors.Messages.GUI_BUTTON_LINKTO_0), encoding) %>',
         image: '<%= org.opencms.workplace.CmsWorkplace.getStaticResourceUri("editors/tinymce/toolbar/oc-link.gif") %>',
         cmd: "cmsLink"
       });
      
      ed.addButton('OcmsHtmlGallery', {
         title : '<%=org.opencms.workplace.galleries.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.galleries.Messages.GUI_HTMLGALLERY_EDITOR_TOOLTIP_0)%>',
         image : '<%= org.opencms.workplace.CmsWorkplace.getStaticResourceUri("editors/tinymce/toolbar/oc-htmlgallery.gif") %>',
         cmd: 'cmsHtmlGallery'
      });
      
      ed.addButton('OcmsLinkGallery', { 
         title : '<%=org.opencms.workplace.galleries.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.galleries.Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0)%>',
         image: '<%= org.opencms.workplace.CmsWorkplace.getStaticResourceUri("editors/tinymce/toolbar/oc-linkgallery.gif") %>',
         cmd : 'cmsLinkGallery'
      });
      ed.addMenuItem('OcmsImageGallery', {
         text: '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />',
         context: 'tools',
         icon: 'image',
         cmd: "cmsImageGallery"
       });
      ed.addMenuItem('OcmsDownloadGallery', {
          text: '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
          context: 'tools',
          icon: 'browse',
          cmd: "cmsDownloadGallery"
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

window.cmsTinyMceFileBrowser = function(fieldId, currentValue, browserType, targetWindow) {
   var editor = tinymce.activeEditor;
   var resourceType="all";
   if (browserType == "image") {
       resourceType=browserType;
   } 
   var integrator = "/system/workplace/editors/tinymce/filebrowser_gallery_integrator.js"
   var url = createGalleryDialogUrl(currentValue, resourceType, integrator);
   url = url + "&hideformats=true";
   editor.cmsTargetWindow = targetWindow;
   editor.cmsFieldId = fieldId; 
   doShowCmsGalleries(editor, url);
}

tinymce.PluginManager.add('opencms', tinymce.opencms);
} // end function initOpenCmsTinyMCEPlugin

initOpenCmsTinyMCEPlugin();



</fmt:bundle>