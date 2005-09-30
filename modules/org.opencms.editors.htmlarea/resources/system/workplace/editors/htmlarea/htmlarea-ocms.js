/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.editors.htmlarea/resources/system/workplace/editors/htmlarea/Attic/htmlarea-ocms.js,v $
 * Date   : $Date: 2005/09/30 15:09:30 $
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
 * These scripts implement the required functions for the OpenCms specific editor popup dialogs like galleries or link dialogs
 */
var activeEditor;
var USE_LINKSTYLEINPUTS = false;


// opens the specified gallery in a popup window
function openGallery(galleryType) {
	openWindow = window.open(workplacePath + "galleries/gallery_fs.jsp?gallerytypename=" + galleryType, "GalleryBrowser", "width=650, height=700, resizable=yes, top=20, left=100");
	focusCount = 1;
	openWindow.focus();
}

// opens the link dialog window
function openLinkDialog(errorMessage) {
	openAnchorDialogWindow("link", errorMessage);
}

// opens the anchor dialog window
function openAnchorDialog(errorMessage) {
	openAnchorDialogWindow("anchor", errorMessage);
}

// opens the anchor or link dialog window depending on the given link type ("link" or "anchor")
function openAnchorDialogWindow(linkType, errorMessage) { 
	if (hasSelectedText()) {
		var winheight;
		var winwidth;
		if (linkType == "link") {
			winheight = (USE_LINKSTYLEINPUTS?220:170);
			winwidth = 480;
		} else {
			winheight = (USE_LINKSTYLEINPUTS?180:130);
			winwidth = 350;
		}
		var linkInformation = getSelectedLink();
		var params = "?showCss=" + USE_LINKSTYLEINPUTS;
		if (linkInformation != null) {
			if (linkType == "link") {
				params += "&href=" + linkInformation["href"];
				params += "&target=" + linkInformation["target"];
				params += "&title= "+linkInformation["title"];
			} else {
				params += "&name=" + linkInformation["name"];
			}
			if (USE_LINKSTYLEINPUTS) {
				params += "&style=" + linkInformation["style"];
				params += "&class=" + linkInformation["class"];
			}
		}
	openWindow = window.open(workplacePath + "editors/dialogs/" + linkType + ".jsp" + params, "SetLink", "width=" + winwidth + ", height=" + winheight + ", resizable=yes, top=300, left=250");
	openWindow.focus();
    } else {
    	alert(errorMessage);
    }
}

// Returns the currently active editor instance to use for the popup dialogs
function getActiveEditor() {
	return activeEditor;
}

// Sets the currently active editor instance to use for the popup dialogs
function setActiveEditor(actEditor) {
	activeEditor = actEditor;
}

// inserts the passed html fragment at the current cursor position
function insertHtml(htmlContent) {
	getActiveEditor().insertHTML(htmlContent);
}

// checks if a text part has been selected by the user
function hasSelectedText() {
	return getActiveEditor().hasSelectedText();
}

// gets the selected html parts
function getSelectedHTML() {
	return getActiveEditor().getSelectedHTML();
}

// creates a named anchor or a link from the OpenCms link dialog, called from popup window
function createLink(linkInformation) {
	var actEditor = getActiveEditor();
	var thelink = actEditor.getParentElement();
	var href = linkInformation["href"].trim();
	if (thelink) {
		if (/^img$/i.test(thelink.tagName)) {
			thelink = thelink.parentNode;
		}
		if (!/^a$/i.test(thelink.tagName)) {
			thelink = null;
		}
	}
	if (!thelink) {
		var sel = actEditor._getSelection();
		var range = actEditor._createRange(sel);
	}
	var a = thelink;
	if (!a) try {
		if (!HTMLArea.is_ie) {
			actEditor._doc.execCommand("createlink", false, "#");
			a = actEditor.getParentElement();
			var sel = actEditor._getSelection();
			var range = actEditor._createRange(sel);
			a = range.startContainer;
			if (!/^a$/i.test(a.tagName)) {
				a = a.nextSibling;
				if (a == null)
					a = range.startContainer.parentNode;
			}
		} else {
			// HACK: for IE, create a String representing the link
			var linkAnchor = '<a';
			if (linkInformation["type"] == "anchor") {
				linkAnchor += ' name="' + linkInformation["name"] + '"';
			} else {
				linkAnchor += ' href="' + linkInformation["href"] + '"';
				linkAnchor += ' target="' + linkInformation["target"] + '"';
			}
			if (linkInformation["title"] != null && linkInformation["title"] != "") {
				linkAnchor += ' title="' + linkInformation["title"] + '"';
			} 
			if (USE_LINKSTYLEINPUTS) { 
				if (linkInformation["style"] != "") {
					linkAnchor += ' style="' + linkInformation["style"] + '"';
					
				}
				if (linkInformation["class"] != "") {
					linkAnchor += ' class="' + linkInformation["class"] + '"';
				}
			}
			linkAnchor += '>';
			actEditor.surroundHTML(linkAnchor, "</a>");
			return;			
		}		
	} catch (e) {}
	else {
		actEditor.selectNodeContents(a);
		
		var deleteNode = false;
		if (linkInformation["type"] == "anchor" && linkInformation["name"] == "") {
			// set dummy href attribute value that deletion works correctly
			a.href = "#";
			deleteNode = true;
		}
		if (linkInformation["type"] != "anchor" && href == "") {
			deleteNode = true;
		}
		if (deleteNode) {
			// delete the anchor from document
			actEditor._doc.execCommand("unlink", false, null);
			actEditor.updateToolbar();
			return;
		}		
	}
	if (!(a && /^a$/i.test(a.tagName))) {
		// no anchor tag, return
		return;
	}
	
	if (linkInformation["type"] == "anchor") {
		// create a named anchor
		a.name = linkInformation["name"];
		a.removeAttribute("href");
		a.removeAttribute("target");
	} else {
		// create a link
		a.href = linkInformation["href"];
		if (linkInformation["target"] != "") {
			a.target = linkInformation["target"];
		}
		a.removeAttribute("name");
		
	}
	
	if (linkInformation["title"] != null && linkInformation["title"] != "") {
		a.title = linkInformation["title"];
	} else {
		a.removeAttribute("title");
	}
	
	if (USE_LINKSTYLEINPUTS) {
		if (linkInformation["style"] != "") {
			// does not work: a.style.setAttribute("CSSTEXT", linkInformation["style"]);
		} else {
			a.removeAttribute("style");
		}
		if (linkInformation["class"] != "") {
			a.setAttribute("class", linkInformation["class"]);
		} else {
			a.removeAttribute("class");
		}
	}
	actEditor.selectNodeContents(a);
	actEditor.updateToolbar();
}

// retrieves the information about the selected link
function getSelectedLink() {
	// Get the editor selection
	var linkInformation = null;
	
	var thelink = getActiveEditor().getParentElement();
	if (thelink) {
		if (/^img$/i.test(thelink.tagName)) {
			thelink = thelink.parentNode;
		}
		if (!/^a$/i.test(thelink.tagName)) {
			thelink = null;
		}
	}
	if (thelink != null) {
		linkInformation = new Object();
		var linkUri = thelink.href;
		if (linkUri != null) {
			linkUri = getActiveEditor().stripBaseURL(linkUri);
		}
		linkInformation["href"] = encodeURIComponent(linkUri);		
		linkInformation["name"] = thelink.name;
		linkInformation["target"] = thelink.target;
		linkInformation["title"] = thelink.title;

		if (USE_LINKSTYLEINPUTS) {
			linkInformation["style"] = encodeURIComponent(thelink.style.getAttribute("CSSTEXT", 2));
			linkInformation["class"] = thelink.className;
		}	
	}

	return linkInformation;
}