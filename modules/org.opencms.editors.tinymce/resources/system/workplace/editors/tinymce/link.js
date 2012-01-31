<%@ page taglibs="c,cms,fmt" import="
   org.opencms.i18n.CmsEncoder,
   org.opencms.jsp.*,
   org.opencms.workplace.editors.*,
   org.opencms.workplace.galleries.*,
   org.opencms.main.*,
   org.opencms.workplace.*,
   org.opencms.editors.tinymce.*,
   java.util.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
pageContext.setAttribute("cms", cms);
CmsDialog dialog = new CmsDialog(pageContext, request, response);
pageContext.setAttribute("locale", dialog.getLocale().toString());
CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);
String encoding = CmsEncoder.ENCODING_US_ASCII;
%>
<fmt:setLocale value="${locale}" />
var workplacePath="<%= cms.link("/system/workplace/") %>";
var USE_LINKSTYLEINPUTS = <%= options.showElement("option.linkstyleinputs", displayOptions) %>;

function initLinks(editor) {
   editor.addCommand("cmsLink", function() {
      openLinkDialog("<%= CmsEncoder.encodeJavaEntities(OpenCms.getWorkplaceManager().getMessages(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.editors.Messages.ERR_EDITOR_MESSAGE_NOSELECTION_0), encoding) %>");
   });
   
   editor.addButton('oc-link', {
      title: '<%= CmsEncoder.encodeJavaEntities(OpenCms.getWorkplaceManager().getMessages(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms.getCmsObject())).key(org.opencms.workplace.editors.Messages.GUI_BUTTON_LINKTO_0), encoding) %>',
      image: '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-link.gif")%>',
      cmd: "cmsLink"
    });
}

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

// gets the selected html parts
function getSelectedHTML() {
   window.alert("not implemented ");
   return FCK.EditorWindow.getSelection();
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

