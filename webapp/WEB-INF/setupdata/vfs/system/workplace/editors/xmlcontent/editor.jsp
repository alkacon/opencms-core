<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*,
	java.util.*"
	buffer="none"
%><%
	
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsXmlContentEditor wp = new CmsXmlContentEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsXmlContentEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!
	
break;
case CmsXmlContentEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	wp.actionExit();

break;
case CmsXmlContentEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	wp.actionExit();

break;
case CmsXmlContentEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again

	wp.actionSave();
	if (wp.getAction() == CmsXmlContentEditor.ACTION_CANCEL) {
		// an error occured during save
		break;
	}

case CmsXmlContentEditor.ACTION_NEW:
//////////////////// ACTION: create a new content item

	if (wp.getAction() == CmsXmlContentEditor.ACTION_NEW) {
		// required since a save action continues with this code
		wp.actionNew();
		// new continues with editing the new resource
	}

case CmsXmlContentEditor.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getSkinUri() %>commons/css_workplace.css">

<style type="text/css">
<!--
.xmlTable { width:100%; }
.xmlTd    { width: 100%; height: 22px; }
.xmlLabel { font-family:verdana, sans-serif; font-size:11px; font-weight:bold; height: 22px; white-space: nowrap; }
.xmlInput { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; }
.xmlInputSmall { width: 200px; }
.xmlInputMedium { width: 400px; }
-->
</style>

<%= wp.calendarIncludes() %>

<script type="text/javascript" src="<%= wp.getSkinUri() %>commons/tree.js"></script>

<script type="text/javascript">
<!--

// VFS FILE SELECTOR START
<%= CmsTree.initTree(wp.getCms(), wp.getEncoding(), wp.getSkinUri()) %>
        
var treewin = null;
var treeForm = null;
var treeField = null;
var treeDoc = null;

function openTreeWin(formName, fieldName, curDoc) {
	var paramString = "?type=vfslink&includefiles=true";

	treewin = openWin(vr.contextPath + vr.workplacePath + "views/explorer/tree_fs.jsp" + paramString, "opencms", 300, 450);
	treeForm = formName;
	treeField = fieldName;
	treeDoc = curDoc;
}

function openWin(url, name, w, h) {
	var newwin = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(newwin != null) {
		if (newwin.opener == null) {
			newwin.opener = self;
		}
	}
	newwin.focus();
	return newwin;
}

function setFormValue(filename) {
	var curForm;
	var curDoc;
	if (treeDoc != null) {
		curDoc = treeDoc;
	} else {
		curDoc = win.files;
	}
	if (treeForm != null) {
		curForm = curDoc.forms[treeForm];	
	} else {
		curForm = curDoc.forms[0];
	}
	if (curForm.elements[treeField]) {
		curForm.elements[treeField].value = filename;	
	}
}
// VFS FILE SELECTOR END


// COLORPICKER START

var colorPicker = new Object();
colorPicker.title = "<%= wp.key("dialog.color.title") %>";
colorPicker.color = "000000";

var currField;

function showColorPicker(fieldName) {
	var theField = document.getElementsByName(fieldName)[0];
	var fieldValue = theField.value;
	fieldValue = cutHexChar(fieldValue, "000000");
	if (document.all) {		
		colorPicker.color = fieldValue;
		var selColor = -1;
		selColor = showModalDialog("<%= wp.getSkinUri() %>components/js_colorpicker/index.html", colorPicker, "resizable: no; help: no; status: no; scroll: no;");
		if (selColor != null) {
			theField.value = "#" + selColor;
			previewColor(fieldName);
		}
	} else {
		currField = theField;
		window.open("<%= wp.getSkinUri() %>components/js_colorpicker/index.html?" + fieldValue, "colorpicker",
				      "toolbar=no,menubar=no,personalbar=no,width=10,height=10," +
				      "scrollbars=no,resizable=yes"); 
	}
}

function setColor(color) {
	if (currField != null) {
		currField.value = "#" + color;
		previewColor(currField.name);
	}
}

function cutHexChar(fieldValue, defaultValue) {
	if (fieldValue != null) {
		if (fieldValue.charAt(0) == "#") {
			return fieldValue.slice(1);
		} else {
			return fieldValue;
		}
	} else {
		return defaultValue;
	}
}

function previewColor(fieldName) {
	var theField = document.getElementsByName(fieldName)[0];
	var colorValue = validateColor(cutHexChar(theField.value, "FFFFFF"));
	if (colorValue == null) {
		theField.style.color = '#000000';
		theField.style.backgroundColor = '#FFFFFF';
	} else if (colorValue < 50000) {
		theField.style.color = '#FFFFFF';
		theField.style.backgroundColor = "#" + colorValue;
	} else {
		theField.style.color = '#000000';
		theField.style.backgroundColor = "#" + colorValue;
	}
}

function validateColor(string) {                // return valid color code
	string = string || '';
	string = string + "";
	string = string.toUpperCase();
	chars = '0123456789ABCDEF';
	out   = '';

	for (i=0; i<string.length; i++) {             // remove invalid color chars
		schar = string.charAt(i);
		if (chars.indexOf(schar) != -1) {
			out += schar;
		}
	}

	if (out.length != 6 && out.length != 3) {
		return null;
	}
	return out;
}

// COLORPICKER END

// Workplacepath needed in included javascript files
var workplacePath="<%= cms.link("/system/workplace/") %>";
	
// action parameters of the form
var actionExit = "<%= wp.EDITOR_EXIT %>";
var actionSaveExit = "<%= wp.EDITOR_SAVEEXIT %>";
var actionSave = "<%= wp.EDITOR_SAVE %>";
	
// Ask user whether he really wants to leave Texteditor without saving
function confirmExit()
{
	if (confirm("<%= wp.key("editor.message.exit") %>")) {
		buttonAction(1);
	}
}

// function action on button click
function buttonAction(para) {
	var _form = document.EDITOR;

	switch (para) {
	case 1: 
		_form.action.value = actionExit;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	case 2:
		_form.action.value = actionSaveExit;
		_form.target = "_top";
		submit(_form);
		_form.submit();
		break;
	case 3:
		_form.action.value = actionSave;
		submit(_form);
		_form.submit();
		break;
	default:
		alert("No action defined for this button!");
		break;
	}
}

function opensmallwin(url, name, w, h) {
	encodedurl = encodeURI(url);
	smallwindow = window.open(encodedurl, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(smallwindow != null) {
		if (smallwindow.opener == null) {
			smallwindow.opener = self;
		}
	}
	return smallwindow;
}

function init() {
	initHtmlArea();
}

function exit() {
	if (treewin != null) {
		// close the file selector window
		window.treewin.close();
		treewin = null;
		treeForm = null;
		treeField = null;
		treeDoc = null;
	}
}

function submit(form) {	
	submitHtmlArea(form);
}

//-->
</script>


<script type="text/javascript">
   _editor_url = "<%= wp.getSkinUri() + "editors/htmlarea/" %>";
   _editor_lang = "en";
</script>

<script type="text/javascript" src="<%= wp.getSkinUri() + "editors/htmlarea/" %>htmlarea.js"></script>

<script type="text/javascript">
<!--

// HTMLArea configuration
var config;
var htmlAreas = new Array();
var textAreas = new Array();

function initHtmlArea() {

config = new HTMLArea.Config();
config.toolbar = [
[
"copy", "cut", "paste", "separator",
"bold", "italic", "underline", "separator",
"strikethrough", "subscript", "superscript", "separator",
"insertorderedlist", "insertunorderedlist", "outdent", "indent", "separator",
"htmlmode"
]
];

config.pageStyle = 
	'body { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; margin: 0 0 0 0; padding: 1 1 1 1; border-width:2px; border-color:#FFF; border-style:inset; } ';

// disable the status bar
config.statusBar = false;

// kill MS Word formatting on paste
config.killWordOnPaste = true;

var tas = document.getElementsByTagName("textarea");
for (var i=0; i<tas.length; i++) {
	textAreas[textAreas.length] = tas[i];
	var ha = new HTMLArea(tas[i], config);
	htmlAreas[htmlAreas.length] = ha;
	ha.generate();
}
}

function registerHtmlArea(id) {
}

function submitHtmlArea(form) {
for (var i=0; i<textAreas.length; i++) {
	ta = textAreas[i];
	ha = htmlAreas[i];
	ta.value = encodeURIComponent(ha.getHTML());
}
}


//-->
</script>

</head>
<body class="buttons-head" unselectable="on" onload="init();" onunload="exit();">

<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogUri() %>">
<input type="hidden" name="<%= wp.PARAM_CONTENT %>" value="<%= wp.getParamContent() %>">
<input type="hidden" name="<%= wp.PARAM_ACTION %>" value="<%= wp.getParamAction() %>">
<input type="hidden" name="<%= wp.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>">
<input type="hidden" name="<%= wp.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>">
<input type="hidden" name="<%= wp.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>">
<input type="hidden" name="<%= wp.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>">
<input type="hidden" name="<%= wp.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>">
 
<table cellspacing="0" cellpadding="0" border="0" style="width: 100%;">	
<tr>
	<td>

<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%= wp.button("javascript:buttonAction(2);", null, "save_exit", "button.saveclose", buttonStyle) %>
<%= wp.button("javascript:buttonAction(3);", null, "save", "button.save", buttonStyle) %>

	<td class="maxwidth">&nbsp;</td>
		
<%= wp.button("javascript:confirmExit();", null, "exit", "button.close", buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(wp.HTML_END) %>
<%= wp.buttonBarHorizontalLine() %>

	</td>
</tr>
</table>


		<div style="width: 100%; height: 100%; overflow: auto;">
		<%= wp.getXmlEditor() %>
		</div>
	

</form>
</body>
</html><%
}
%>