<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%>

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

/** The editor frame. */
var parentDialog=window.parent;

// remove loading overlay and get editor reference
/** The editor instance. */
var editor=parentDialog.tinymce.activeEditor;
var tinymce = parentDialog.tinymce;

/** The fck editor configuration. */
var editorConfig= {};

var targetWindow = editor.cmsTargetWindow;
var fieldId = editor.cmsFieldId;

function setFormValue(url) {
   var inputField = targetWindow.document.getElementById(fieldId);
   if (url.indexOf("://") == -1) {
      var url = "<%=org.opencms.main.OpenCms.getSystemInfo().getOpenCmsContext()%>"+"/" + url;
      url = url.replace(/\/+/g, "/");
   }
   inputField.value = url; 
}
