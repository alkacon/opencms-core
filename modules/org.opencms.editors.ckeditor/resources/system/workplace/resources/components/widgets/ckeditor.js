/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.editors.ckeditor/resources/system/workplace/resources/components/widgets/ckeditor.js,v $
 * Date   : $Date: 2009/10/28 10:38:00 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/*
 * These scripts are required for the CKEditor widgets in the XML content editor
 */

// CKEditor global objects
var editorInstances = new Array();
var contentFields = new Array();
var expandedToolbars = new Array();
var editorsLoaded = false;
var tmpEditorSettings = null;
var createIndex = 0;

// helper variable storing the editor instance name, needed for iframe dialogs (gallleries)
var dialogEditorInstanceName;

// registers event listeners for each editor instance
CKEDITOR.on("instanceReady", function(e) {
	// replace the next textarea(s) , has to be done in event because of timing issues regarding configuration
	replaceTextarea();
	e.editor.on("focus", function(e) {
		showToolbar(e.editor);
	});
	e.editor.on("blur", function(e) {
		fitWindow(e.editor);
	});
});

// generates the CKEditor instances
function generateEditors() {
  // replace only the first textarea, otherwise the configuration of each instance gets messed up
  replaceTextarea();
}

// replaces a single textarea by a CKEditor instance
function replaceTextarea() {
	if (createIndex < editorInstances.length) {
		var editorSettings = editorInstances[createIndex];
		createIndex += 1;
		var editInst = CKEDITOR.replace( "ta_" + editorSettings.areaId, {
			contentsCss : editorSettings.contentsCss,
			stylesCombo_stylesSet : editorSettings.stylesCombo_stylesSet,
			fullPage : editorSettings.fullPage,
			customConfig : editorSettings.configPath
		});
	} else {
		editorsLoaded = true;
	}
}

// writes the HTML from the editor instances back to the textareas
function submitHtml(form) {
	for (var i=0; i<contentFields.length; i++) {
		var cf = contentFields[i];
		var instName = "ta_" + cf.getAttribute("id", 0);
		var editInst = CKEDITOR.instances[instName];
		var editedContent = editInst.getData();
		if (editedContent != null && editedContent != "null") {
			cf.value = encodeURIComponent(editedContent);
		}
	}
}

// show toolbar if editor content is selected
function showToolbar(editorInstance) {
	if (expandedToolbars[editorInstance.name] == null) {
        	editorInstance.execCommand("toolbarCollapse");
        	expandedToolbars[editorInstance.name] = true;
    	}
}

// un-maximize the editor if it loses the focus
function fitWindow(editorInstance) {
	if (editorInstance.getCommand("maximize").state == CKEDITOR.TRISTATE_ON) {
		editorInstance.execCommand("maximize");
	}
}

// checks if the editors were loaded successfully
function editorsLoaded() {
	return editorsLoaded;
}