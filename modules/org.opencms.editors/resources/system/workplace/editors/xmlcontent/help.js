/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.editors/resources/system/workplace/editors/xmlcontent/help.js,v $
 * Date   : $Date: 2005/05/19 09:29:39 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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
 
function Browser() {
	this.isIE = false;  // Internet Explorer
	if (navigator.userAgent.indexOf("MSIE") != -1) {
		this.isIE = true;
	}
}

var browser = new Browser();
 
function findPosX(obj) {
    var curleft = 0; 
    if (obj.offsetParent) {
        while (obj.offsetParent) {
            curleft += obj.offsetLeft - obj.scrollLeft; 
            obj = obj.offsetParent; 
        } 
    } else if (obj.x) {
        curleft += obj.x; 
    }
    return curleft; 
}

function findPosY(obj) {
    var curtop = 0; 
    if (obj.offsetParent) {
        while (obj.offsetParent) { 
            curtop += obj.offsetTop - obj.scrollTop; 
            obj = obj.offsetParent; 
        }
    } else if (obj.y) {
        curtop += obj.y;
    }
    return curtop;
}

function showHelp(id) { 

    var text = document.getElementById("help" + id);
    
    // get all select boxes for Internet Explorer
    if (browser.isIE && selectBoxes == null) {
    	selectBoxes = document.getElementsByTagName("select");
    }
    
    var icon = document.getElementById("img" + id);
    if (text.style.visibility == "visible") {
        return;
    }
    x = findPosX(icon) + 8;
    y = findPosY(icon) + 8;
    var textHeight = text.scrollHeight;
    var textWidth = text.scrollWidth;
    var scrollSize = 20; 
    var scrollTop = 0; 
    var scrollLeft = 0; 
    var clientHeight = 0; 
    var clientWidth = 0; 
    if (document.documentElement && (document.documentElement.scrollTop || document.documentElement.clientHeight)) {
        scrollTop = document.documentElement.scrollTop; 
        scrollLeft = document.documentElement.scrollLeft; 
        clientHeight = document.documentElement.clientHeight; 
        clientWidth = document.documentElement.clientWidth; 
    } else if (document.body) {
        scrollTop = document.body.scrollTop; 
        scrollLeft = document.body.scrollLeft; 
        clientHeight = document.body.clientHeight; 
        clientWidth = document.body.clientWidth; 
    }
    if ((y + textHeight) > (clientHeight + scrollTop)) {
        y = y - textHeight;
    }
    if (y < scrollTop) {
        y = (clientHeight + scrollTop) - (textHeight + scrollSize);
    }
    if (y < scrollTop) {
        y = scrollTop;
    }
    if ((x + textWidth) > (clientWidth + scrollLeft)) {
        x = x - textWidth;
    }  
    if (x < scrollLeft) {
        x = (clientWidth + scrollLeft) - (textWidth + scrollSize);
    }
    if (x < scrollLeft) {
        x = scrollLeft;
    }
    text.style.left = x + "px";
    text.style.top =  y + "px";
    text.style.visibility = "visible";
    
    if (browser.isIE) {
    	// hide select boxes which are in help bubble area to avoid display issues
    	for (var i=0; i<selectBoxes.length; i++) {
    		var topPos = findPosY(selectBoxes[i]);
    		if (topPos + selectBoxes[i].offsetHeight >= y && topPos <= y + textHeight) {
    			// hide this select box
    			selectBoxes[i].style.display = "none";
    		}
    	}
    }
}

function hideHelp(id) {
    var text = document.getElementById("help" + id);
    text.style.visibility = "hidden";
    text.style.left = "0px";
    text.style.top =  "0px";
    if (browser.isIE) {
    	// show select boxes which were hidden
    	for (var i=0; i<selectBoxes.length; i++) {
    		if (selectBoxes[i].style.display == "none") {
    			selectBoxes[i].style.display = "";
    		}
    	}
    }
}

