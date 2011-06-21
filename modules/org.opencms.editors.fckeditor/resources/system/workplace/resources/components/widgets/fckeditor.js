/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * These scripts are required for the FCKeditor widgets in the xml content editor
 */

// FCKeditor global objects
var editorInstances = new Array();
var contentFields = new Array();
var expandedToolbars = new Array();
var editorsLoaded = false;

// generates the FCKeditor instances
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
		var editInst = FCKeditorAPI.GetInstance("ta_" + cf.getAttribute("id", 0));
		var editedContent = editInst.GetXHTML(true);
		if (editedContent != null && editedContent != "null") {
			cf.value = encodeURIComponent(editedContent);
		}
		try {
			// fixes IE issue when inserting image and saving immediately
			editInst.Selection.Collapse(true);
		} catch (e) {}
	}
}

// show toolbar if editor content is selected
function showToolbar(editorInstance) {
		if (expandedToolbars[editorInstance.Name] == null) {
        	editorInstance.ToolbarSet.Expand();
        	expandedToolbars[editorInstance.Name] = true;
    	}
}

// un-maximize the editor if it loses the focus
function fitWindow(editorInstance) {
	if (editorInstance.Commands.GetCommand("FitWindow").IsMaximized) {
		editorInstance.Commands.GetCommand("FitWindow").Execute();
	}
}

// add event for selection change & blur
function FCKeditor_OnComplete(editorInstance) {
	editorInstance.Events.AttachEvent("OnFocus", showToolbar);
	editorInstance.Events.AttachEvent("OnBlur", fitWindow);
	editorsLoaded = true;
}

// checks if at least one of the editors was loaded successfully
function editorsLoaded() {
	return editorsLoaded;
}