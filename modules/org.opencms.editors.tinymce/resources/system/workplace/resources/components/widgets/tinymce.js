/*
 * These scripts are required for the TinyMCE widgets in the xml content editor
 */

// FCKeditor global objects
var editorInstances = new Array();
var contentFields = new Array();
var expandedToolbars = new Array();
var editorsLoaded = false;

// generates the TinyMCE instances
function generateEditors() {
	for (var i=0; i<editorInstances.length; i++) {
		var editInst = editorInstances[i];
		editInst.ReplaceTextarea();
	}
}

// writes the HTML from the editor instances back to the textareas
function submitHtml(form) {
	for (var i=0; i<contentFields.length; i++) {
		var cf = contentFields[i];
		var editInst = tinyMCE.get('ta_' + cf.getAttribute('id', 0));
		var editedContent = editInst.getContent();
		if (editedContent != null && editedContent != "null") {
			cf.value = encodeURIComponent(editedContent);
		}
	}
}

// checks if at least one of the editors was loaded successfully
function editorsLoaded() {
	return editorsLoaded;
}