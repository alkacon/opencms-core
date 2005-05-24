/*
 * File   : $Source: $
 * Date   : $Date: $
 * Version: $Revision: $
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
 
 /*
  * This script uses the methods findPosX and findPosY from the help.js file.
  * Be careful when changing the help JavaScript methods.
  */
 
var activeCombo;

function showCombo(itemId, comboId) { 
    // collect the elements
    var text = document.getElementById(comboId);
    
    if (itemId != '') {
    
    var inputElem = document.getElementById(itemId);
    
    var inputWidth = inputElem.offsetWidth;

    // just return if already visible     
    if (text.style.visibility == "visible") {
        return;
    }
    
    // get input element center position
    x = findPosX(inputElem);
    y = findPosY(inputElem) + 16;
    
    // some variables
    var textHeight = text.scrollHeight;
    var textWidth = text.scrollWidth;
    var scrollSize = 20;    
    var scrollTop = 0;
    var scrollLeft = 0;
    var clientHeight = 0;
    var clientWidth = 0;

    // the usual NS / IE stuff to get the client window size
    if (document.documentElement && (document.documentElement.scrollTop || document.documentElement.clientHeight)) {
    	// NS
        scrollTop = document.documentElement.scrollTop;
        scrollLeft = document.documentElement.scrollLeft;
        clientHeight = document.documentElement.clientHeight;
        clientWidth = document.documentElement.clientWidth;
        inputWidth += 15;
    } else if (document.body) {
    	// IE
        scrollTop = document.body.scrollTop;
        scrollLeft = document.body.scrollLeft;
        clientHeight = document.body.clientHeight;
        clientWidth = document.body.clientWidth;
        inputWidth += 17;
    }
    
    // ensure the help is always displayed on the screen
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
    
    // now display the help
    text.style.left = x + "px";
    text.style.top =  y + "px";
    text.style.width = inputWidth + "px";
    
    }
    
    text.style.visibility = "visible";
    activeCombo = text;
}

function setComboValue(itemId, valueId) {

    // collect the elements
    var item = document.getElementById(itemId);   
    var value = document.getElementById(valueId);
    // set value and select it
    item.value = value.innerHTML;   
    item.select();
    item.focus();
    // hide the combo box
    hideCombo();
}

function hideCombo() {
	// hide the active combo box
	if (activeCombo != null) {
		activeCombo.style.visibility = "hidden";
    	activeCombo = null;
	}
}

function initComboBox() {
	// add event listeners to close combo boxes on mouse up
	if (document.addEventListener) {
		document.addEventListener("mouseup", hideCombo, false );
	}
	else if (document.attachEvent) {
		document.attachEvent("onmouseup", function () { hideCombo(); } );
	}
	
    Global_run_event_hook = false;
}