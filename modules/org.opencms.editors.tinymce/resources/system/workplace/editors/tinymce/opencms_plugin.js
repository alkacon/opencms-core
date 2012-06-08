<%@ page taglibs="c,cms" import="
   org.opencms.i18n.CmsEncoder,
   org.opencms.jsp.*,
   org.opencms.workplace.editors.*,
   org.opencms.workplace.galleries.*,
   org.opencms.main.*,
   org.opencms.workplace.*,
   org.opencms.editors.tinymce.*,
   java.util.*,
   org.opencms.file.types.CmsResourceTypeImage" %><%
%><%@ taglib prefix="fmt"  uri="http://java.sun.com/jsp/jstl/fmt" %><% 
    CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response); 
   pageContext.setAttribute("cms", cms);
   CmsDialog dialog = new CmsDialog(pageContext, request, response);
   pageContext.setAttribute("locale", dialog.getLocale().toString());
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
   var width = 685;
   var height = 502;
   editor.windowManager.open({url: url, width : width, height: height, inline: "yes"}, {});
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
      menu.add({title : '<fmt:message key="GUI_IMAGE_GALLERY_TITLE_0" />', icon : 'image', cmd : 'cmsImageGallery'});
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
function createGalleryDialogUrl(path, typesParam, integrator) {
   var resParam = "";
    var editFrame=findFrame(self, 'edit');
   if (editFrame.editedResource != null) {
      resParam = "&resource=" + editFrame.editedResource;
   } else {
      resParam = "&resource=" + editFrame.editform.editedResource;
   }
   if (!integrator) {
      integrator = "/system/workplace/editors/tinymce/integrator.js";
   }
   var integratorParam = "&integrator="+integrator; 
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

function linkGalleryDialogUrl() {
      var resParam = "";
       var editFrame=findFrame(self, 'edit');
      if (editFrame.editedResource != null) {
         resParam = "&resource=" + editFrame.editedResource;
      } else {
         resParam = "&resource=" + editFrame.editform.editedResource;
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
   if (editFrame.editedResource != null) {
      resParam = "&resource=" + editFrame.editedResource;
   } else {
      resParam = "&resource=" + editFrame.editform.editedResource;
   }
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
         image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-imagegallery.gif")%>',
         cmd: "cmsImageGallery"
       });
      
      ed.addButton('OcmsDownloadGallery', {
         title: '<fmt:message key="GUI_DOWNLOAD_GALLERY_TITLE_0" />',
         image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-downloadgallery.gif")%>',
         cmd: "cmsDownloadGallery"
      });
      
      ed.addButton('oc-link', {
         title: '<%= CmsEncoder.encodeJavaEntities(OpenCms.getWorkplaceManager().getMessages(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.editors.Messages.GUI_BUTTON_LINKTO_0), encoding) %>',
         image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-link.gif")%>',
         cmd: "cmsLink"
       });
      
      ed.addButton('OcmsHtmlGallery', {
         title : '<%=org.opencms.workplace.galleries.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.galleries.Messages.GUI_HTMLGALLERY_EDITOR_TOOLTIP_0)%>',
         image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-htmlgallery.gif")%>',
         cmd: 'cmsHtmlGallery'
      });
      
      ed.addButton('OcmsLinkGallery', { 
         title : '<%=org.opencms.workplace.galleries.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.galleries.Messages.GUI_LINKGALLERY_EDITOR_TOOLTIP_0)%>',
         image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-linkgallery.gif")%>',
         cmd : 'cmsLinkGallery'
      });
      
      ed.onInit.add(function(ed) {
         ed.plugins.contextmenu.onContextMenu.add(filterContextMenu);
      });
   },

   getInfo : function() {
      return {
         longname : 'OpenCms TinyMCE plugin',
         author : 'Alkacon Software GmbH',
         authorurl : 'http://www.opencms.org',
         infourl : 'http://wwww.opencms.org',
         version : '1.0'
      };
   }
});

window.cmsTinyMceFileBrowser = function(fieldId, currentValue, browserType, targetWindow) {
   var editor = tinymce.activeEditor;
   if (browserType == "image") {
      var integrator = "/system/workplace/editors/tinymce/filebrowser_gallery_integrator.js"
      var url = createGalleryDialogUrl(currentValue, "image", integrator);
      url = url + "&hideformats=true";
   } else {
      var url = "<cms:link>/system/workplace/views/explorer/tree_fs.jsp?type=pagelink&includefiles=true</cms:link>"; 
      var integrator = "<cms:link>/system/workplace/editors/tinymce/filebrowser_integrator.js</cms:link>";
      var url = "<cms:link>/system/workplace/views/explorer/tree_fs.jsp?type=pagelink&includefiles=true</cms:link>";
      url = url + "&integrator=" + integrator;
   }
   var width = 685;
   var height = 502;
   editor.cmsTargetWindow = targetWindow;
   editor.cmsFieldId = fieldId; 
   editor.windowManager.open({url: url, width : width, height: height, inline: "yes"}, {});
   
}

tinymce.PluginManager.add('opencms', tinymce.opencms);
} // end function initOpenCmsTinyMCEPlugin

initOpenCmsTinyMCEPlugin();



</fmt:bundle>






















