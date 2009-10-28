<%@ page import="
	org.opencms.i18n.CmsEncoder,
	org.opencms.jsp.*,
	org.opencms.workplace.*,
	org.opencms.workplace.editors.*,
	org.opencms.workplace.galleries.*,
	org.opencms.editors.ckeditor.*,
	java.util.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsCKEditor wp = new CmsCKEditor(cms);
CmsEditorDisplayOptions options = wp.getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);
String buttonPath = wp.getEditorResourceUri() + "skins/opencms/toolbar/";
String encoding = CmsEncoder.ENCODING_US_ASCII;

%>var workplacePath = "<%= cms.link("/system/workplace/") %>";
var USE_LINKSTYLEINPUTS = <%= options.showElement("option.linkstyleinputs", displayOptions) %>;

CKEDITOR.plugins.add("opencms", {

	// uncomment the following line in case of editor problems
	// beforeInit : function( editor ) { alert( 'Editor "' + editor.name + '" is to be initialized!' ); },

	init : function(editor) {

		// exit button
		var exitCommand = editor.addCommand("oc-exit", {
				exec : function( editor ) {
					if (!editor.checkDirty() || confirm("<%= wp.key(org.opencms.workplace.editors.Messages.GUI_EDITOR_MESSAGE_EXIT_0)%>")) {
						execAction(editor, '<%= CmsEditor.EDITOR_EXIT %>','_top');
					}
				},
				modes : { wysiwyg : 1, source : 1 },
				canUndo: false
			}
		);
		editor.ui.addButton("oc-exit",	{
			label : "<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_CLOSE_0), encoding) %>",
			command: "oc-exit",
			modes : { wysiwyg : 1, source : 1 },
			icon: "<%= buttonPath + "oc-exit.gif" %>"
		});

		// save button
		var saveCommand = editor.addCommand("oc-save", {
				exec : function( editor ) { execAction(editor, '<%= CmsEditor.EDITOR_SAVE %>','_self'); },
				modes : { wysiwyg : 1, source : 1 },
				canUndo: false
			}
		);
		editor.ui.addButton("oc-save", {
			label : "<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVE_0), encoding) %>",
			command: "oc-save",
			modes : { wysiwyg : 1, source : 1 },
			icon: "<%= buttonPath + "oc-save.gif" %>"
		});

		// save & exit button
		var saveExitCommand = editor.addCommand("oc-save_exit", {
				exec : function( editor ) { execAction(editor, '<%= CmsEditor.EDITOR_SAVEEXIT %>','_top'); },
				modes : { wysiwyg : 1, source : 1 },
				canUndo: false
			}
		);
		editor.ui.addButton("oc-save_exit", {
			label : "<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_SAVECLOSE_0), encoding) %>",
			command: "oc-save_exit",
			modes : { wysiwyg : 1, source : 1 },
			icon: "<%= buttonPath + "oc-save_exit.gif" %>"
		});

		// link dialog button
		var linkCommand = editor.addCommand("oc-link", {
				exec : function( editor ) { openLinkDialog(editor, "<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.ERR_EDITOR_MESSAGE_NOSELECTION_0), encoding) %>"); },
				modes : { wysiwyg : 1, source : 0 },
				canUndo: true
			}
		);
		editor.ui.addButton("oc-link", {
			label : "<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_LINKTO_0), encoding) %>",
			command: "oc-link",
			modes : { wysiwyg : 1, source : 0 },
			icon: "<%= buttonPath + "oc-link.gif" %>"
		});

		// help button
		var helpCommand = editor.addCommand("oc-help", {
				exec : function( editor ) { openOnlineHelp("/system/modules/org.opencms.editors.ckeditor/help/<%= wp.getLocale() %>/index.html"); },
				modes : { wysiwyg : 1, source : 1 },
				canUndo: false
			}
		);
		editor.ui.addButton("oc-help", {
			label : "<%= CmsEncoder.encodeJavaEntities(wp.key(org.opencms.workplace.editors.Messages.GUI_BUTTON_HELP_0), encoding) %>",
			command: "oc-help",
			modes : { wysiwyg : 1, source : 1 },
			icon: "<%= buttonPath + "oc-help.gif" %>"
		});
	}
});

// sets field values and submits the editor form
function execAction(editor, action, target) {
	var form = document.forms["EDITOR"];
	form.content.value = encodeURIComponent(editor.getData());
	form.action.value = action;
	form.target = target;
	form.submit(); 
}

// opens the anchor or link dialog window depending on the given link type ("link" or "anchor")
function openLinkDialog(editor, errorMessage) { 
	if (hasSelection(editor)) {
		var winheight = (USE_LINKSTYLEINPUTS?220:170);
		var winwidth = 480;
		var linkInformation = getLinkInformation(editor);
		var params = "?showCss=" + USE_LINKSTYLEINPUTS;
		params += "&editorname=" + editor.name;
		if (linkInformation != null) {
			params += "&href=" + encodeURIComponent(linkInformation["href"]);
			params += "&target=" + linkInformation["target"];
			params += "&title= " + linkInformation["title"];
			if (USE_LINKSTYLEINPUTS) {
				params += "&style=" + linkInformation["style"];
				params += "&class=" + linkInformation["class"];
			}
		}
		openWindow = window.open(workplacePath + "editors/dialogs/link.jsp" + params, "SetLink", "width=" + winwidth + ", height=" + winheight + ", resizable=yes, top=300, left=250");
		openWindow.focus();
	} else {
		alert(errorMessage);
	}
}

// checks if a text or element has been selected in the editor by the user
function hasSelection(editor) {
	var sel = editor.getSelection();
	if ((sel.getType() == CKEDITOR.SELECTION_TEXT || sel.getType() == CKEDITOR.SELECTION_ELEMENT) && sel.getNative() != "") {
		return true;
	}
	return false; 
}

// returns the selected <a> element or null, if no link is selected
function getSelectedLinkElement(editor) {
	var element = editor.getSelection().getSelectedElement();
	if (!element) {
		// failed, try to get the start element of the selection
		element = editor.getSelection().getStartElement();
	}
	if (element && element.getName() != "a") {
	    	// selected element is no link, try to get the surrounding anchor
	    	element = element.getAscendant("a", false);
  	}
  	return element;
}

// retrieves the information about the selected link
function getLinkInformation(editor) {
	var linkInformation = null;

	// get the element of the current selection
	var thelink = getSelectedLinkElement(editor);

	if (thelink != null) {
		linkInformation = new Object();
		var linkUri = thelink.getAttribute("href");
		linkInformation["href"] = encodeURIComponent(linkUri);
		thelink.hasAttribute("target") ? linkInformation["target"] = thelink.getAttribute("target") : linkInformation["target"] = "";
		thelink.hasAttribute("title") ? linkInformation["title"] = thelink.getAttribute("title") : linkInformation["title"] = "";
		if (USE_LINKSTYLEINPUTS) {
			thelink.hasAttribute("class") ? linkInformation["class"] = thelink.getAttribute("class") : linkInformation["class"] = "";
			thelink.hasAttribute("style") ? linkInformation["style"] = thelink.getAttribute("style") : linkInformation["style"] = "";
		}	   
	}
	return linkInformation;
}

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {
	var attributes = {href: linkInformation["href"]};
	var removeAttributes = [];
	
	// set the hidden editor specific attribute, otherwise nothing works
	attributes._cke_saved_href = linkInformation["href"];
	if (linkInformation["target"] != "") {
		attributes.target = linkInformation["target"];
	} else {
		removeAttributes.push("target");
	}
	if (linkInformation["title"] != "") {
		attributes.title = linkInformation["title"];
	} else {
		removeAttributes.push("title");
	}
	if (USE_LINKSTYLEINPUTS) {
		if (linkInformation["class"] != "") {
			attributes["class"] = linkInformation["class"];
		} else {
			removeAttributes.push("class");
		}
		if (linkInformation["style"] != "") {
			attributes.style = linkInformation["style"];
		} else {
			removeAttributes.push("style");
		}
	}
	
	var editor = CKEDITOR.instances[linkInformation["editorname"]];
	// get the element of the current selection
	var element = getSelectedLinkElement(editor);

	if (!element) {
		// no link element found, create new one
		var style = new CKEDITOR.style({ element : "a", attributes : attributes });
		style.type = CKEDITOR.STYLE_INLINE;
		style.apply(editor.document); 
	} else {
		// link element exists, set attributes
		element.setAttributes(attributes);
		element.removeAttributes(removeAttributes);
	    	delete element;
	}
}
<%
if (wp.isHelpEnabled()) { %>
// opens the OpenCms online help popup window
function openOnlineHelp(wpUri) {
	window.open("<%= cms.link("/system/workplace/locales/" + wp.getLocale() + "/help/index.html") %>?buildframe=true&workplaceresource=" + wpUri, "cmsonlinehelp", "toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width=700,height=450");
}<%
} %>