<%@ page import="
	org.opencms.editors.codemirror.*,
	org.opencms.jsp.*,
	org.opencms.util.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.help.*"
%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsCodeMirror wp = new CmsCodeMirror(cms);

int buttonStyle = wp.getSettings().getUserSettings().getEditorButtonStyle();

String mode = wp.getHighlightMode();

//////////////////// start of switch statement 

switch (wp.getAction()) {

case CmsEditor.ACTION_SHOW_ERRORMESSAGE:
//////////////////// ACTION: display the common error dialog
	// do nothing here, only prevent editor content from being displayed!

break;
case CmsEditor.ACTION_EXIT:
//////////////////// ACTION: exit the editor without saving

	if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(wp.getParamCloseFunction()) && !"null".equals(wp.getParamCloseFunction())) {
	    wp.actionClear(false);
		%>
		<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
		<html>
			<head>
				<script type="text/javascript">
					<%= wp.getParamCloseFunction() %>
				</script>
			</head>
		</html>
		<%
	} else {
		wp.actionExit();
	}

break;
case CmsEditor.ACTION_SAVEEXIT:
//////////////////// ACTION: save the modified content and exit the editor

	wp.actionSave();
	if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(wp.getParamCloseFunction()) && !"null".equals(wp.getParamCloseFunction())) {
	    wp.actionClear(false);
		%>
		<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
		<html>
			<head>
				<script type="text/javascript">
					<%= wp.getParamCloseFunction() %>
				</script>
			</head>
		</html>
		<%
	} else {
		wp.actionExit();
	}

break;
case CmsEditor.ACTION_SAVE:
//////////////////// ACTION: save the modified content and show the editor again

	wp.actionSave();
	if (wp.getAction() == CmsDialog.ACTION_CANCEL) {
		// an error occurred during save
		break;
	}

case CmsDialog.ACTION_DEFAULT:
default:
//////////////////// ACTION: show editor frame (default)

%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=<%= wp.getEncoding() %>">
<title>(<%= wp.getSettings().getUser().getName() %>) - <%= wp.getParamResource() %></title>

<link rel=stylesheet type="text/css" href="<%= wp.getStyleUri("workplace.css") %>">
<link rel="stylesheet" type="text/css" href="<%= wp.getEditorResourceUri() %>dist/lib/codemirror.css">
<link rel="stylesheet" type="text/css" href="<%= wp.getEditorResourceUri() %>dist/theme/eclipse.css">
<link rel="stylesheet" href="<%= wp.getEditorResourceUri() %>dist/addon/dialog/dialog.css">
<link rel="stylesheet" href="<%= wp.getEditorResourceUri() %>dist/addon/hint/show-hint.css">
<link rel="stylesheet" type="text/css" href="<%= wp.getEditorResourceUri() %>codemirror-ocms.css" title="cssocms">

<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>js/lang-<%= wp.getEditorLanguage() %>.js"></script>
<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>edit.js"></script>
<script type="text/javascript" src="<%= wp.getEditorResourceUri() %>dist/lib/codemirror.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/dialog/dialog.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/search/searchcursor.js"></script>
<script src="<%= wp.getEditorResourceUri() %>js/search.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/edit/closebrackets.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/edit/closetag.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/edit/matchbrackets.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/hint/show-hint.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/hint/html-hint.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/hint/javascript-hint.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/hint/xml-hint.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/fold/foldcode.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/fold/brace-fold.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/fold/xml-fold.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/comment/comment.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/addon/selection/active-line.js"></script>
<%

// determine the codemirror mode name to use for highlighting
String modeName = mode;
if (mode.equals(CmsCodeMirror.HIGHLIGHT_TYPE_JAVASCRIPT)) {
    modeName = "javascript";
} else if (mode.equals(CmsCodeMirror.HIGHLIGHT_TYPE_HTML)) {
    modeName = "text/html";
} else if (mode.equals(CmsCodeMirror.HIGHLIGHT_TYPE_JSP)) {
    modeName = "application/x-jsp";
}

// determine which hint method to use depending on the mode
String hintConfig;
if (modeName.equals("text/html")) {
	hintConfig = "CodeMirror.htmlHint";
} else {
	hintConfig = "CodeMirror.javascriptHint";
}

// include all necessary scripts for syntax highlighting
%><script src="<%= wp.getEditorResourceUri() %>dist/mode/css/css.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/mode/xml/xml.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/mode/clike/clike.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/mode/javascript/javascript.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/mode/css/css.js"></script>
<script src="<%= wp.getEditorResourceUri() %>dist/mode/htmlmixed/htmlmixed.js"></script>
<script src="<%= wp.getEditorResourceUri() %>js/htmlembedded_modified.js"></script>

<script type="text/javascript">

	// the editor instance object
	var editorCodeMirror;

	// stored editor modes, used for manual mode change
	var editorMode = "<%= modeName %>";
	var currMode = editorMode;
	var modeClass = "push";

	// visible tabs helpers, used for toggling the visible tab characters
	var tabsVisible = false;
	var tabsClass = "norm";

	// close brackets and tags button mode class
	var closeClass = "push";

	// the fold function to use
	var foldFunc = CodeMirror.newFoldFunction(CodeMirror.<% if (mode.equals(CmsCodeMirror.HIGHLIGHT_TYPE_HTML) || mode.equals(CmsCodeMirror.HIGHLIGHT_TYPE_XML)) { out.print("tagRangeFinder"); } else { out.print("braceRangeFinder"); } %>);
	
	// the optional JS close function name to call
	var callCloseFunction = false;
	<%
		if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(wp.getParamCloseFunction()) && !"null".equals(wp.getParamCloseFunction())) {
			%>
			callCloseFunction = true;
			function editorCloseFunction() {
				<%= wp.getParamCloseFunction() %>
			}
			<%
		}
	%>

	// sets the document source code for later including into the editor
	var content="<%= wp.getParamContent() %>";

	// action parameters of the form
	var actionExit = "<%= CmsEditor.EDITOR_EXIT %>";
	var actionSaveExit = "<%= CmsEditor.EDITOR_SAVEEXIT %>";
	var actionSave = "<%= CmsEditor.EDITOR_SAVE %>";

    // stores the content state (dirty or not)
	var contentDirty = false;
	function setContentDirty(newVal) {
		contentDirty = newVal;
	}

	// ask if user really wants to leave the editor without saving
	function confirmExit() {
		if (contentDirty) {
			// only ask if the content has been modified
			if (confirm ("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0)%>")) {
				buttonAction(1);
			}
		} else {
			buttonAction(1);
		}
	}

<%	if (wp.isHelpEnabled()) {
		out.println(CmsHelpTemplateBean.buildOnlineHelpJavaScript(wp.getLocale())); 
	} %>

    // load the editor by replacing the textarea
    function loadEditor() {
		// create the editor
		editorCodeMirror = CodeMirror(document.getElementById("edit1"), {
				autofocus : true,
				lineNumbers: true,
				styleActiveLine: true,
				mode: editorMode,
				theme: "eclipse",
				fixedGutter: true,
				indentUnit: 4,
				indentWithTabs: true,
				matchBrackets: true,
				smartIndent: false,
				autoCloseBrackets: true,
				autoCloseTags: true,
				extraKeys: {"Ctrl-Space": function(cm) {CodeMirror.showHint(cm, <%= hintConfig %>);}, "Ctrl-Q": function(cm){foldFunc(cm, cm.getCursor().line);}}
			}
		);
		// set the editor content
	  	editorCodeMirror.setValue(decodeURIComponent(content));
	  	//  adjust the editor height
	  	editorCodeMirror.setSize("100%", "100%");
		// trigger dirty flag on editor changes
		editorCodeMirror.on("change", function(cm, change) {
			setContentDirty(true);
		});
		// activate fold functionality on gutter click
		editorCodeMirror.on("gutterClick", foldFunc);
	}

</script>

</head>
<body class="buttons-head" unselectable="on">
<form name="EDITOR" id="EDITOR" method="post" action="<%= wp.getDialogRealUri() %>">
<input type="hidden" name="<%= CmsEditor.PARAM_CONTENT %>"/>
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= wp.getParamAction() %>"/>
<input type="hidden" name="<%= CmsDialog.PARAM_RESOURCE %>" value="<%= wp.getParamResource() %>"/>
<input type="hidden" name="<%= CmsEditor.PARAM_LOADDEFAULT %>" value="<%= wp.getParamLoaddefault() %>"/>
<input type="hidden" name="<%= CmsEditor.PARAM_EDITASTEXT %>" value="<%= wp.getParamEditastext() %>"/>
<input type="hidden" name="<%= CmsEditor.PARAM_DIRECTEDIT %>" value="<%= wp.getParamDirectedit() %>"/>
<input type="hidden" name="<%= CmsEditor.PARAM_BACKLINK %>" value="<%= wp.getParamBacklink() %>"/>
<input type="hidden" name="<%= CmsEditor.PARAM_MODIFIED %>" value="<%= wp.getParamModified() %>"/>
<input type="hidden" name="<%= CmsCodeMirror.PARAM_CLOSEFUNCTION %>" value="<%= wp.getParamCloseFunction() %>"/>

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 5) %>
<%= wp.button("javascript:buttonAction(2);", null, "save_exit", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(3);", null, "save", org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:CodeMirror.commands['find'](editorCodeMirror);", null, "search", org.opencms.workplace.editors.Messages.GUI_BUTTON_SEARCH_0, buttonStyle) %>
<%= wp.button("javascript:CodeMirror.commands['replace'](editorCodeMirror);", null, "editorsearch", org.opencms.workplace.editors.Messages.GUI_BUTTON_REPLACE_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<%= wp.button("javascript:buttonAction(4);", null, "undo", org.opencms.workplace.editors.Messages.GUI_BUTTON_UNDO_0, buttonStyle) %>
<%= wp.button("javascript:buttonAction(5);", null, "redo", org.opencms.workplace.editors.Messages.GUI_BUTTON_REDO_0, buttonStyle) %>
<%= wp.buttonBarSeparator(5, 5) %>
<td>
	<select name="fontsize" onchange="setEditorFontSize(this.value);" title="<%= wp.key("GUI_EDITOR_SELECT_FONTSIZE_0") %>">
		<option value="-">--<%= wp.key("GUI_EDITOR_SELECT_FONTSIZE_0") %>--</option>
		<option value="10">10px</option>
		<option value="11">11px</option>
		<option value="12">12px</option>
		<option value="13">13px</option>
		<option value="14" selected="selected">14px</option>
		<option value="15">15px</option>
		<option value="16">16px</option>
		<option value="18">18px</option>
	</select>
</td>
<%= wp.buttonBarSeparator(5, 5) %>
<td>
	<select name="fontsize" onchange="setEditorSyntax(this.value);" title="<%= wp.key("GUI_EDITOR_SELECT_SYNTAX_0") %>">
		<option value="-">--<%= wp.key("GUI_EDITOR_SELECT_SYNTAX_0") %>--</option>
		<option value="css"<% if (modeName.equals("css")) {%> selected="selected"<%}%>>CSS</option>
		<option value="text/html"<% if (modeName.equals("text/html")) {%> selected="selected"<%}%>>HTML</option>
		<option value="javascript"<% if (modeName.equals("javascript")) {%> selected="selected"<%}%>>Javascript</option>
		<option value="application/x-jsp"<% if (modeName.equals("application/x-jsp")) {%> selected="selected"<%}%>>JSP</option>
		<option value="xml"<% if (modeName.equals("xml")) {%> selected="selected"<%}%>>XML</option>
	</select>
</td>
<%= wp.buttonBarSeparator(5, 5) %>
<td style="vertical-align: top;">
	<a href="#" onclick="javascript:buttonAction(6);" class="button" title="<%= wp.key("GUI_EDITOR_BUTTON_SYNTAXHIGHLIGHT_0") %>">
		<span unselectable="on" class="push" onmouseover="className='over'" onmouseout="className=modeClass" onmousedown="className='push'" onmouseup="className='over'">
			<img class="button" src="<%= wp.getEditorResourceUri() %>images/highlight.gif" alt="<%= wp.key("GUI_EDITOR_BUTTON_SYNTAXHIGHLIGHT_0") %>"/>
		</span>
	</a>
</td>
<td style="vertical-align: top;">
	<a href="#" onclick="javascript:buttonAction(7);" class="button" title="<%= wp.key("GUI_EDITOR_BUTTON_VISIBLETABS_0") %>">
		<span unselectable="on" class="norm" onmouseover="className='over'" onmouseout="className=tabsClass" onmousedown="className='push'" onmouseup="className='over'">
			<img class="button" src="<%= wp.getEditorResourceUri() %>images/visibletabs.png" alt="<%= wp.key("GUI_EDITOR_BUTTON_VISIBLETABS_0") %>"/>
		</span>
	</a>
</td>
<%-- (deactivated, does not work properly!) = wp.button("javascript:autoFormatSelection();", null, "../editors/codemirror/images/autoformat.png", "GUI_EDITOR_BUTTON_AUTOFORMAT_0", buttonStyle) --%>
<td style="vertical-align: top;">
	<a href="#" onclick="javascript:editorCodeMirror.setOption('autoCloseBrackets', !editorCodeMirror.getOption('autoCloseBrackets'));editorCodeMirror.setOption('autoCloseTags', !editorCodeMirror.getOption('autoCloseTags'));if (closeClass == 'push') { closeClass = 'norm'; } else { closeClass = 'push'; }" class="button" title="<%= wp.key("GUI_EDITOR_BUTTON_AUTOCLOSE_0") %>">
		<span unselectable="on" class="push" onmouseover="className='over'" onmouseout="className=closeClass" onmousedown="className='push'" onmouseup="className='over'">
			<img class="button" src="<%= wp.getEditorResourceUri() %>images/autoclose.png" alt="<%= wp.key("GUI_EDITOR_BUTTON_AUTOCLOSE_0") %>"/>
		</span>
	</a>
</td>
<%= wp.button("javascript:editorCodeMirror.setOption('lineWrapping', !editorCodeMirror.getOption('lineWrapping'));", null, "../editors/codemirror/images/word_wrap.gif", "GUI_EDITOR_BUTTON_WORDWRAP_0", buttonStyle) %>
<%
if (wp.isHelpEnabled()) {%>
	<%= wp.buttonBarSeparator(5, 5) %>
	<%= wp.button("javascript:openOnlineHelp('/editors');", null, "help.png", org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0, buttonStyle) %><%
} %>
<td class="maxwidth">&nbsp;</td>
<%= wp.button("javascript:confirmExit();", null, "exit", org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0, buttonStyle) %>
<%= wp.buttonBarSpacer(5) %>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %> 

<div id="edit1"></div>

</form>

<script type="text/javascript">
	// create the editor and manually set the content state
    loadEditor();
	setContentDirty(false);
</script>

</body>
</html>	
<% } %>