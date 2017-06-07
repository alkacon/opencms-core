/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

var selectBoxes;
var selectY;

function Browser() {
	this.isIE = false;  // Internet Explorer
	if (navigator.userAgent.indexOf("MSIE") != -1) {
		this.isIE = true;
	}
}

var browser = new Browser();

function showHelpText(imgId, textValue) {
	if (imgId == null || imgId == "") {
		var elem = document.getElementById("helpText");
	    if (elem.style.visibility == "visible") {
	        return;
	    } else {
	    	elem.style.visibility = "visible";
	    	hideSelectBoxes(elem, selectY);
	    }
	} else {
		var elem = document.getElementById("helpText");
		elem.innerHTML = textValue;
		showHelpX(imgId, "Text");
	}
}

function showHelp(id) {

    showHelpX(id, id);
}

function showHelpX(id, helpId) {
    var text = document.getElementById("help" + helpId);
    if (undefined == text) {
      return;
    }
    if (text.style.visibility == "visible") {
        return;
    }

    // get the help icon element
    var icon = document.getElementById("img" + id);
    var xOffset = 8;
    if (icon == null) {
    	// no icon found, this is a combo help text
    	icon = document.getElementById(id);
    	xOffset = 50;
    }

    try {
    selectY = showEditorElement(text, icon, xOffset, 8, false);
    hideSelectBoxes(text, selectY);
    } catch (e)  {
    	// ignore
}
}

function hideHelpText() {
	var text = document.getElementById("helpText");
	try {
    text.style.visibility = "hidden";
    showSelectBoxes();
    } catch (e)  {
    	// ignore
}
}

function hideHelp(id) {
    var text = document.getElementById("help" + id);
	try {
    text.style.visibility = "hidden";
    text.style.left = "0px";
    text.style.top =  "0px";
    showSelectBoxes();
    } catch (e)  {
    	// ignore
}
}

// hide select boxes which are in help or combo area to avoid display issues
function hideSelectBoxes(elem, y) {
    if (browser.isIE) {
    	if (selectBoxes == null) {
    		selectBoxes = document.getElementsByTagName("select");
    	}
    	var textHeight = elem.scrollHeight;
    	for (var i=0; i<selectBoxes.length; i++) {
    		var topPos = findPosY(selectBoxes[i]);
    		if (topPos + selectBoxes[i].offsetHeight >= y && topPos <= y + textHeight) {
    			// hide this select box
    			for (var attrs=0; attrs<selectBoxes[i].attributes.length; attrs++) {
    				if (selectBoxes[i].attributes[attrs].nodeName == "multiple" && selectBoxes[i].attributes[attrs].nodeValue == true) {
    					selectBoxes[i].parentNode.style.height = (selectBoxes[i].offsetHeight + 6) + "px";
    					break;
    				}
    			}
    			selectBoxes[i].style.display = "none";
    		}
    	}
    }
}

// show select boxes which were hidden
function showSelectBoxes() {
	if (browser.isIE) {
		if (selectBoxes == null) {
    		selectBoxes = document.getElementsByTagName("select");
    	}
    	for (var i=0; i<selectBoxes.length; i++) {
    		if (selectBoxes[i].style.display == "none") {
    			selectBoxes[i].style.display = "";
    		}
    	}
    }
}
