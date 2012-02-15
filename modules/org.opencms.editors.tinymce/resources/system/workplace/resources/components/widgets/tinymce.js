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

function setupTinyMCE(editor) {
   if (tinyMCE.isWebKit) {
      // fix weird layout problem in Chrome 
      // If we don't do this, the button bar won't wrap if the window is too small 
      editor.onInit.add(function() {
         var id = editor.id + "_tbl";
         var baseElem = document.getElementById(id); 
         var modElem = $(baseElem).parents(".cmsTinyMCE").get(0);
         $(modElem).removeClass("cmsTinyMCE");
         window.setTimeout(function() { $(modElem).addClass("cmsTinyMCE"); } , 1);
      });
   }
}

