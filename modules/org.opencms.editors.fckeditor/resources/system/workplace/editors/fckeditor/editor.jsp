<%@ page import="
	org.opencms.editors.fckeditor.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	java.util.*
"%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsFCKEditor wp = new CmsFCKEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

// create configuration object and store it in session to configure toolbar in external JavaScript, because request parameters do not work
CmsFCKEditorConfiguration extConf = new CmsFCKEditorConfiguration();
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

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">

<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() + "editors/fckeditor/" %>fckeditor.js"></script>

<script type="text/javascript">

// dialog windows
var dialogCharWindow = null;
var dialogElementWindow = null;
var dialogAnchorWindow = null;
var dialogPropertyWindow = null;

// OpenCms context prefix, required for page editor because no link replacement is done here
var linkEditorPrefix = "<%= OpenCms.getSystemInfo().getOpenCmsContext() %>";

// saves the editors contents
function saveContent() {
    document.EDITOR.content.value = encodeURIComponent(FCKeditorAPI.GetInstance("fckeditor").GetXHTML(false));
}

// Ask user whether he really wants to delete the locale
function confirmDeleteLocale() {
	if (confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_DELETELOCALE_0) %>")) {
		buttonAction(14);
	}
}

//action on button click 
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
    if (document.EDITOR.<%= CmsDialog.PARAM_ACTION %>.value == "null" || document.EDITOR.<%= CmsDialog.PARAM_ACTION %>.value == "") {
        top.closeframe.closePage("<%= wp.getParamTempfile() %>", "<%= wp.getParamResource() %>");
    }        
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
</head>

<body class="buttons-head" unselectable="on" onunload="closeDialog();">

<form style="width:100%; height:100%; margin:0px; padding:0px; " name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_CONTENT %>">
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
		out.println(wp.deleteLocaleButton("javascript:confirmDeleteLocale();", null, "deletecontent", org.opencms.workplace.editors.Messages.GUI_BUTTON_DELETE_0, buttonStyle));
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
out.println(wp.button("javascript:buttonAction(13);", null, "cleanup", org.opencms.workplace.editors.Messages.GUI_EDITOR_DIALOG_CLEANUP_BUTTON_0, buttonStyle));
%>             
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:buttonAction(2);", null, "preview", org.opencms.workplace.editors.Messages.GUI_BUTTON_PREVIEW_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>

</td></tr>

<tr>
<td style="width:100%; height:100%;">
<div style="width:100%; height:100%; background-color: Window;">
<script type="text/javascript">
<!--
	var editor = new FCKeditor("fckeditor");
	editor.Config["CustomConfigurationsPath"] = "<%= cms.link(CmsEditor.PATH_EDITORS + "fckeditor/customconfig.js") %>";
	editor.BasePath = "<%= CmsWorkplace.getSkinUri() + "editors/fckeditor/" %>";
	editor.Width = "100%";
	editor.Height = "100%";
	editor.ToolbarSet = "OpenCms";
	editor.Value = decodeURIComponent("<%= wp.getParamContent() %>");   
	editor.Create() ;
//-->
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

<form style="display: none;" name="PROPERTIES" action="../commons/property.jsp" target="DIALOGPROPERTY" method="post">
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamTempfile() %>">
<input type="hidden" name="usetempfileproject" value="true">
<input type="hidden" name="<%= CmsDialog.PARAM_ISPOPUP %>" value="true">
</form>

</body>
</html><%
}
%>