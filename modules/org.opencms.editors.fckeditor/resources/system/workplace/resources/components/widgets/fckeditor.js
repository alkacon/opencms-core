/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.editors.fckeditor/resources/system/workplace/resources/components/widgets/fckeditor.js,v $
 * Date   : $Date: 2005/12/16 14:12:39 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
		cf.value = encodeURIComponent(editInst.GetXHTML(false));
	}
}

// show toolbar if editor content is selected
function showToolbar(editorInstance) {
		if (expandedToolbars[editorInstance.Name] == null) {
        	editorInstance.ToolbarSet.Expand();
        	expandedToolbars[editorInstance.Name] = true;    
    	}
}

// add event for selection change
function FCKeditor_OnComplete(editorInstance) {
	editorInstance.Events.AttachEvent("OnSelectionChange", showToolbar) ;
}