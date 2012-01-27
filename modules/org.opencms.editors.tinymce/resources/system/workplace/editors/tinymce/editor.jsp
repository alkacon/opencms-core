<%@ page import="
	org.opencms.editors.tinymce.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	java.util.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsTinyMCE wp = new CmsTinyMCE(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

// create configuration object and store it in session to configure toolbar in external JavaScript, because request parameters do not work
CmsTinyMCEConfiguration extConf = new CmsTinyMCEConfiguration();
extConf.setUriStyleSheet(wp.getUriStyleSheet());
extConf.setResourcePath(wp.getParamResource());
extConf.setConfiguration(session);

//////////////////// start of switch statement

switch (wp.getAction()) {

case CmsEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog

	// do nothing here, only prevents editor content from being displayed!

break;
case CmsEditor.ACTION_PREVIEW:
//////////////////// ACTION: preview the page

	wp.actionPreview();

break;
case CmsEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor

	wp.actionExit();

break;
case CmsEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;

case CmsEditor.ACTION_DELETELOCALE:
//////////////////// ACTION: delete a localeand show the editor again
	if (wp.getAction() == CmsEditor.ACTION_DELETELOCALE) {
		wp.actionDeleteElementLocale();
    }

case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content
	if (wp.getAction() == CmsEditor.ACTION_SAVE) {
		wp.actionSave();
	}

case CmsDialog.ACTION_DEFAULT:
case CmsEditor.ACTION_SHOW:
default:
//////////////////// ACTION: show editor frame (default)

	// escape the content parameter to display it in the form
	wp.escapeParams();
	wp.setParamAction(null);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">
<script type="text/javascript">
	<%@include file="%(link.strong:/system/workplace/editors/tinymce/gallery.js)" %>
</script>

<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() + "editors/tinymce/jscripts/tiny_mce/" %>tiny_mce.js"></script>
<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() + "jquery/packed/" %>jquery.js"></script>

<script type="text/javascript">

// dialog windows
var dialogCharWindow = null;
var dialogElementWindow = null;
var dialogAnchorWindow = null;
var dialogPropertyWindow = null;

// OpenCms context prefix, required for page editor because no link replacement is done here
var linkEditorPrefix = "<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";

// Path to the style sheet used in the editor
var cssPath = "<%= wp.getUriStyleSheet() %>";

// Path to the currently edited resource
var editedResource = "<%= wp.getParamResource() %>";

// saves the editors contents
function saveContent() {
    document.EDITOR.content.value = encodeURIComponent(tinyMCE.get('<%= CmsEditor.PARAM_CONTENT %>').getContent());
}

// Ask user whether he really wants to delete the locale
function confirmDeleteLocale() {
	if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_DELETELOCALE_0) %>")) {
		buttonAction(14);
	}
}

// action on button click
function buttonAction(para) {
	var _form = document.EDITOR;
	_form.action.value = "";
    switch (para) {
    case 1:
        // reload the editor
    	saveContent();
        _form.action.value = "<%= CmsEditor.EDITOR_SHOW %>";
        _form.target = "_self";
        _form.submit();
        break;
    case 2:
        // preview selected
    	saveContent();
        _form.action.value = "<%= CmsEditor.EDITOR_PREVIEW %>";
        _form.target = "PREVIEW";
        _form.submit();
        break;
    case 3:
        // change element
    	saveContent();
        _form.action.value = "<%= CmsEditor.EDITOR_CHANGE_ELEMENT %>";
        _form.target = "_self";
        _form.submit();
        break;
    case 4:
        // open elements window
    	saveContent();
        dialogElementWindow = window.open("about:blank","DIALOGELEMENT","width=320,height=250,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
        document.ELEMENTS.submit();
        dialogElementWindow.focus();
        break;
    case 5:
        // open properties window
    	saveContent();
        dialogPropertyWindow = window.open("about:blank","DIALOGPROPERTY","width=600,height=280,left=0,top=0,resizable=yes,scrollbars=yes,location=no,menubar=no,toolbar=no,status=no,dependent=yes");
        document.PROPERTIES.submit();
        dialogPropertyWindow.focus();
        break;
    case 13:
        // clear document
        saveContent();
		_form.action.value = "<%= CmsEditor.EDITOR_CLEANUP %>";
		_form.target = "_self";
		_form.submit();
        break;
	case 14:
		// delete the current locale content
		_form.action.value = "<%= CmsEditor.EDITOR_DELETELOCALE %>";
		_form.submit();
		break;
    }
}

function changeElement(elementName, language) {
    if (elementName != document.EDITOR.<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>.value && language == document.EDITOR.<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>.value) {
        document.EDITOR.<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>.value = elementName;
        buttonAction(3);
    } else {
        buttonAction(1);
    }
}

// action for closing the window
function closeDialog() {
    if (dialogElementWindow) {
        window.dialogElementWindow.close();
    }
    if (dialogPropertyWindow) {
        window.dialogPropertyWindow.close();
    }
    if (dialogCharWindow) {
    	window.dialogCharWindow.close();
    }
    if (dialogAnchorWindow) {
    	window.dialogAnchorWindow.close();
    }

    var actionValue = document.EDITOR.<%= CmsDialog.PARAM_ACTION %>.value;
    if (actionValue == null || actionValue == "null" || actionValue == "") {
		closeBrowserWindow();
    }

}

// sends a request to the server to delete the temporary file if the browser was accidentally closed
function closeBrowserWindow() {
   var http_request = false;
   if (window.XMLHttpRequest) { // Mozilla, Safari, ...
      http_request = new XMLHttpRequest();
      if (http_request.overrideMimeType) {
         http_request.overrideMimeType('text/xml');
      }
   } else if (window.ActiveXObject) { // IE
      try {
         http_request = new ActiveXObject("Msxml2.XMLHTTP");
      } catch (e) {
         try {
            http_request = new ActiveXObject("Microsoft.XMLHTTP");
         } catch (e) {}
      }
   }
   if (!http_request) {
	return false;
   }
   http_request.onreadystatechange = httpStateDummy;
   http_request.open("POST", "<%= wp.getDialogRealUri() %>?<%= CmsDialog.PARAM_ACTION %>=<%= CmsEditor.EDITOR_CLOSEBROWSER %>&<%= CmsDialog.PARAM_RESOURCE %>=<%= wp.getParamResource() %>&<%= CmsEditor.PARAM_TEMPFILE %>=<%= wp.getParamTempfile() %>", true);
   http_request.send('');
}

// dummy function for html request
function httpStateDummy() {
	return;
}

// action when closing a popup window
function popupCloseAction(closeObj) {
	if (closeObj.refreshOpener && closeObj.refreshOpener == true) {
		buttonAction(1);
	} else if (closeObj.elemName) {
		changeElement(closeObj.elemName, closeObj.elemLocale);
	}
}

function FCKeditor_OnComplete(editorInstance) {
    <%
	// if necessary, switch to text mode
	if ("edit".equals(wp.getParamEditormode())) {
		out.print("FCKeditorAPI.GetInstance(\"fckeditor\").SwitchEditMode();");
	}
	%>
}

</script>
<script type="text/javascript">
<!--
tinyMCE.init({
    // General options
    mode : "exact",
    elements : "<%= CmsEditor.PARAM_CONTENT %>",
    theme : "advanced",
    plugins : "autolink,lists,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template",

    // Theme options
    theme_advanced_buttons1 : "oc-publish,oc-save-exit,oc-save,|,oc-imagegallery,oc-downloadgallery,|,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,fontselect,fontsizeselect,|,oc-exit",
    theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
    theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
    theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,spellchecker,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,blockquote,pagebreak,|,insertfile,insertimage",
    theme_advanced_toolbar_location : "top",
    theme_advanced_toolbar_align : "left",
    theme_advanced_statusbar_location : "bottom",
    theme_advanced_resizing : false,
    
    // Don't store editor size in cookies
    theme_advanced_resizing_use_cookie : false,

    // Skin options
    skin : "o2k7",
    skin_variant : "silver",
    relative_urls: false,

    // Example content CSS (should be your site CSS)
    content_css : "css/example.css",

    // Drop lists for link/image/media/template dialogs
    template_external_list_url : "js/template_list.js",
    external_link_list_url : "js/link_list.js",
    external_image_list_url : "js/image_list.js",
    media_external_list_url : "js/media_list.js",

    // Replace values for the template plugin
    template_replace_values : {
            username : "Some User",
            staffid : "991234"
    },
    
    // editor size
    width: "100%",
    
    // events
    setup : function(ed) {
		  ed.onInit.add(function(ed) {
		      ed.setContent(decodeURIComponent('<%= wp.getParamContent() %>'));
		      initHeight();
		  });
		// Add Publisg button
	    ed.addButton('oc-publish', {
	    	title : 'Publish',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-publish.gif")%>',
	        onclick : function() {
	        	var exitTarget='_top';
	        	//the editors exit frame target, may be !='_top' if in advanced direct edit!
	        	if (top.frames['cmsAdvancedDirectEditor']!=null && top.frames['cmsAdvancedDirectEditor'].document!=null){
	        	    exitTarget='cmsAdvancedDirectEditor';
	        	}
	        	execAction(tinyMCE.get('<%= CmsEditor.PARAM_CONTENT %>'), '<%= CmsEditor.EDITOR_SAVEACTION %>',exitTarget);
	        }
	   });
		
	 	// Add Save & Exit button
	    ed.addButton('oc-save-exit', {
	    	title : 'Save and Exit',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-save-exit.gif")%>',
	        onclick : function() {
	        	execAction(tinyMCE.get('<%= CmsEditor.PARAM_CONTENT %>'), '<%= CmsEditor.EDITOR_SAVEEXIT %>','_top');
	        }
	   });
	   
	 	// Add Save button
	    ed.addButton('oc-save', {
	    	title : 'Save',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-save.gif")%>',
	        onclick : function() {
	        	execAction(tinyMCE.get('<%= CmsEditor.PARAM_CONTENT %>'), '<%= CmsEditor.EDITOR_SAVE %>','_self');
	        }
	   });

	 	// Add Exit button
	    ed.addButton('oc-exit', {
	    	title : 'Exit',
	        image : '<%=cms.link("/system/workplace/resources/editors/tinymce/toolbar/oc-exit.gif")%>',
	        onclick : function() {
	        	if (!tinyMCE.get('<%= CmsEditor.PARAM_CONTENT %>').isDirty() || confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0)%>")) {
					execAction(tinyMCE.get('<%= CmsEditor.PARAM_CONTENT %>'), '<%= CmsEditor.EDITOR_EXIT %>','_top');
				}
	        }
	   });
       initGalleries(ed);
	 	
	}
});

//sets field values and submits the editor form
function execAction(editor, action, target) {
	var form = document.forms["EDITOR"];
	form.content.value = encodeURIComponent(editor.getContent());
	form.action.value = action;
	form.target = target;
	form.submit(); 
}
// JavaScript resize editor stuff

// height of bottom line of editor with HTML tag information
var tagBarHeight = 14;

// current window heigh
var windowHeight ;

// current editor height
var editorHeight ;

// calculate editor height in pixels
function getEditorHeight(){
	return document.getElementById('textarea-container').clientHeight -tagBarHeight;
}

// Set inital heights for window and editor
function initHeight(){
	windowHeight = $(window).height();
	editorHeight = $("#<%= CmsEditor.PARAM_CONTENT %>_ifr").height();
}

// Set editor size on resize window event
window.onresize = function() {
	var newWindowHeight = $(window).height();
	var delta = newWindowHeight - windowHeight ;

	windowHeight = newWindowHeight ;
	editorHeight = editorHeight + delta ;
	
	document.getElementById("<%= CmsEditor.PARAM_CONTENT %>_ifr").style.height = editorHeight + 'px';
	document.getElementById("<%= CmsEditor.PARAM_CONTENT %>_tbl").style.height = editorHeight + tagBarHeight + 'px';
	}

//-->
</script>
</head>

<body class="buttons-head" unselectable="on" onunload="closeDialog();">

<form style="width:100%; height:100%; margin:0px; padding:0px; " name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_OLDELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_OLDELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>">

<table cellspacing="0" cellpadding="0" border="0" style="width:100%; height:100%;">

<tr><td>
<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%
boolean elementSelection = options.showElement("option.element.selection", displayOptions);
boolean elementLanguage = options.showElement("option.element.language", displayOptions);
if (elementSelection || elementLanguage) {
	out.println(wp.buttonBarLabel(org.opencms.workplace.editors.Messages.GUI_INPUT_ELEMENT_0));
	if (elementLanguage) {
		out.println("<td>" + wp.buildSelectElementLanguage("name=\"" + CmsEditor.PARAM_ELEMENTLANGUAGE + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
		out.println(wp.deleteLocaleButton("javascript:confirmDeleteLocale();", null, "deletelocale", org.opencms.workplace.editors.Messages.GUI_BUTTON_DELETE_0, buttonStyle));
		out.println(wp.buttonBarSpacer(2));
	} else {
		%><input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>"><%
	}
	if (elementSelection) {
		out.println("<td>" + wp.buildSelectElementName("name=\"" + CmsDefaultPageEditor.PARAM_ELEMENTNAME + "\" width=\"150\" onchange=\"buttonAction(3);\"") + "</td>");
		out.println(wp.buttonBarSeparator(5, 5));
		out.println(wp.button("javascript:buttonAction(4);", null, "elements", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_ELEMENTS_BUTTON_0, buttonStyle));
	} else {
		%><input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>"><%
	}
} else {
	// build hidden input fields that editor works correctly
	%><input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>"><input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>"><%
}
if (options.showElement("option.properties", displayOptions)) {
	if (elementLanguage && !elementSelection) {
		out.println(wp.buttonBarSeparator(5, 5));
	}
	out.println(wp.button("javascript:buttonAction(5);", null, "properties", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_PROPERTIES_BUTTON_0, buttonStyle));
}
if (options.showElement("option.cleanup", displayOptions)) {
	out.println(wp.button("javascript:buttonAction(13);", null, "cleanup", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_CLEANUP_BUTTON_0, buttonStyle));
}
%>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:buttonAction(2);", null, "preview", org.opencms.workplace.editors.Messages.GUI_BUTTON_PREVIEW_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

</td></tr>

<tr>
<td style="width:100%; height:100%;">
<div id="textarea-container" style="width:100%; height:100%; background-color: Window;">
<script language="javascript">
document.write ('<textarea id="<%= CmsEditor.PARAM_CONTENT %>" name="<%= CmsEditor.PARAM_CONTENT %>" style="height:'+getEditorHeight()+'px; width:100%;"></textarea>');
</script>
</div>
</td>
</tr>

</table>

</form>

<form style="display: none;" name="ELEMENTS" action="dialogs/elements.jsp" target="DIALOGELEMENT" method="post">
<input type="hidden" name="<%= CmsEditor.PARAM_TEMPFILE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_ELEMENTLANGUAGE %>" value="<%= wp.getParamElementlanguage() %>">
<input type="hidden" name="<%= CmsDefaultPageEditor.PARAM_ELEMENTNAME %>" value="<%= wp.getParamElementname() %>">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="ispopup" value="true">
</form>

<form style="display: none;" name="PROPERTIES" action="<%= cms.link("/system/workplace/commons/property.jsp") %>" target="DIALOGPROPERTY" method="post">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="usetempfileproject" value="true">
<input type="hidden" name="<%= CmsDialog.PARAM_ISPOPUP %>" value="true">
</form>

</body>
</html><%
}
%>