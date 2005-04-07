<%@page buffer="none" session="false" import="java.util.*, org.opencms.jsp.*" %><%

// initialize action element to access API
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

// determine JS version to build from requested URI
boolean isMouseOver = cms.getRequestContext().getUri().endsWith("over.js");

%>//********************************************************
// Do not remove this notice.
//
// Copyright 2000-2004 by Mike Hall.
// See http://www.brainjar.com for terms of use.
//********************************************************
// Modified by Andreas Zahner (a.zahner@alkacon.com)
//********************************************************

//----------------------------------------------
// Code to determine the browser and version.
//----------------------------------------------

function Browser() {

	var ua, s, i;

	this.isIE    = false;  // Internet Explorer
	this.isOP    = false;  // Opera
	this.isNS    = false;  // Netscape
	this.version = null;

	ua = navigator.userAgent;

	s = "Opera";
	if ((i = ua.indexOf(s)) >= 0) {
		this.isOP = true;
		this.version = parseFloat(ua.substr(i + s.length));
		return;
	}

	s = "Netscape6/";
	if ((i = ua.indexOf(s)) >= 0) {
		this.isNS = true;
		this.version = parseFloat(ua.substr(i + s.length));
		return;
	}

	// Treat any other "Gecko" browser as Netscape 6.1.

	s = "Gecko";
	if ((i = ua.indexOf(s)) >= 0) {
		this.isNS = true;
		this.version = 6.1;
		return;
	}

	s = "MSIE";
	if ((i = ua.indexOf(s))) {
		this.isIE = true;
		this.version = parseFloat(ua.substr(i + s.length));
		return;
	}
}

var browser = new Browser();

//----------------------------------------------
// Code for handling the menu bar and active button.
//----------------------------------------------

var activeButton = null;

<% 
	if (! isMouseOver) {
%>
// Capture mouse clicks on the page so any active button can be deactivated.

if (browser.isIE)
	document.onmousedown = pageMousedown;
else
	document.addEventListener("mousedown", pageMousedown, true);

function pageMousedown(event) {

	var el;

	// If there is no active button, exit.

	if (activeButton == null)
		return;

	// Find the element that was clicked on.

	if (browser.isIE)
		el = window.event.srcElement;
	else
		el = (event.target.tagName ? event.target : event.target.parentNode);

	// If the active button was clicked on, exit.

	if (el == activeButton)
		return;

	// If the element is not part of a menu, reset and clear the active
	// button.

	if (getContainerWith(el, "DIV", "menu") == null) {
		resetButton(activeButton);
		activeButton = null;
	}
}
<%
	}
%>

function buttonClick(event, menuId) {

	var button;
	try {
		// Get the target button element.
	
		if (browser.isIE)
			button = window.event.srcElement;
		else
			button = event.currentTarget;
	
		// Blur focus from the link to remove that annoying outline.
	
		button.blur();
	
		// Associate the named menu to this button if not already done.
		// Additionally, initialize menu display.
	
		if (button.menu == null) {
			button.menu = document.getElementById(menuId);
			if (button.menu.isInitialized == null)
				menuInit(button.menu);
		}
	
	<%
		if (isMouseOver) {
	%>
		// [MODIFIED] Added for activate/deactivate on mouseover.
	
		// Set mouseout event handler for the button, if not already done.
	
		if (button.onmouseout == null)
			button.onmouseout = buttonOrMenuMouseout;
	
		// Exit if this button is the currently active one.
	
		if (button == activeButton)
			return false;
	
		// [END MODIFIED]
	<%
		}
	%>
	
		// Reset the currently active button, if any.
	
		if (activeButton != null) {
			resetButton(activeButton);}
	
		// Activate this button, unless it was the currently active one.
	
		if (button != activeButton) {
			depressButton(button);
			activeButton = button;
		}
		else {<%
		if (! isMouseOver) {
	%>
			// AZ: open link when pressing top menu button again
			document.location.href = activeButton.href;
			// /AZ
	<% 
		} 
	%>
			activeButton = null;	
		}
	} catch (e) {}
	
	return false;
}

function buttonMouseover(event, menuId) {

	var button;
<%
	if (isMouseOver) {
%>
	// [MODIFIED] Added for activate/deactivate on mouseover.

	// Activates this button's menu if no other is currently active.

	if (activeButton == null) {
		buttonClick(event, menuId);
		return;
	}

	// [END MODIFIED]
<%
	}
%>

	// Find the target button element.

	if (browser.isIE)
		button = window.event.srcElement;
	else
		button = event.currentTarget;

	// If any other button menu is active, make this one active instead.

	if (activeButton != null && activeButton != button)
		buttonClick(event, menuId);
}

function depressButton(button) {

	var x, y;

	// Update the button's style class to make it look like it's depressed.

	button.className += " menuButtonActive";
	
<%
	if (isMouseOver) {
%>

	// [MODIFIED] Added for activate/deactivate on mouseover.

	// Set mouseout event handler for the button, if not already done.

	if (button.onmouseout == null)
		button.onmouseout = buttonOrMenuMouseout;
	if (button.menu.onmouseout == null)
		button.menu.onmouseout = buttonOrMenuMouseout;

	// [END MODIFIED]
<%
	}
%>

	// Position the associated drop down menu under the button and show it.

	x = getPageOffsetLeft(button);
	y = getPageOffsetTop(button) + button.offsetHeight;

	// For IE, adjust position.

	if (browser.isIE) {
		x += button.offsetParent.clientLeft;
		y += button.offsetParent.clientTop;
	}
	
	// AZ: corrected IE position error and NS position error on non image menubar links
	var childNodeName = "";
	try {
		if (browser.isNS) {
			// check if there is an image child node in Mozilla based browsers
			childNodeName = button.childNodes[0].tagName;
		}
	} catch (e) {}
		if (browser.isIE) {
		y += 1;
	}
	// /AZ
	
	button.menu.style.left = x + "px";
	button.menu.style.top  = y + "px";
	if (browser.isNS) {
		// AZ: workaround to avoid display issues in NS based browsers
		tempMenu = button.menu;
		setTimeout("showMainMenu();", 0);
	} else {
		button.menu.style.visibility = "visible";
	}
	// For IE; size, position and show the menu's IFRAME as well.

	if (button.menu.iframeEl != null) {
		button.menu.iframeEl.style.left = button.menu.style.left;
		button.menu.iframeEl.style.top  = button.menu.style.top;
		button.menu.iframeEl.style.width  = button.menu.offsetWidth + "px";
		button.menu.iframeEl.style.height = button.menu.offsetHeight + "px";
		button.menu.iframeEl.style.display = "";
	}
}

function resetButton(button) {

	// Restore the button's style class.

	removeClassName(button, "menuButtonActive");

	// Hide the button's menu, first closing any sub menus.

	if (button.menu != null) {
		closeSubMenu(button.menu);
		button.menu.style.visibility = "hidden";

		// For IE, hide menu's IFRAME as well.

		if (button.menu.iframeEl != null)
			button.menu.iframeEl.style.display = "none";
	}
}

//----------------------------------------------
// Code to handle the menus and sub menus.
//----------------------------------------------

function menuMouseover(event) {

	var menu;

	// Find the target menu element.

	if (browser.isIE)
		menu = getContainerWith(window.event.srcElement, "DIV", "menu");
	else
		menu = event.currentTarget;

	// Close any active sub menu.

	if (menu.activeItem != null)
		closeSubMenu(menu);
}

function menuItemMouseover(event, menuId) {

	var item, menu, x, y;

	// Find the target item element and its parent menu element.

	if (browser.isIE) {
		item = getContainerWith(window.event.srcElement, "A", "mI");
		// AZ: added support to mark current top navigation item
		if (item == null) {
			item = getContainerWith(window.event.srcElement, "A", "mICurrent");
		}
		// /AZ
	} else {
		item = event.currentTarget;
	}

	menu = getContainerWith(item, "DIV", "menu");

	// Close any active sub menu and mark this one as active.

	if (menu.activeItem != null)
		closeSubMenu(menu);
	menu.activeItem = item;

	// Highlight the item element.

	item.className += " mIHighlight";

	// Initialize the sub menu, if not already done.

	if (item.subMenu == null) {
		item.subMenu = document.getElementById(menuId);
		if (item.subMenu.isInitialized == null)
			menuInit(item.subMenu);
	}

<%
	if (isMouseOver) {
%>
	// [MODIFIED] Added for activate/deactivate on mouseover.

	// Set mouseout event handler for the sub menu, if not already done.

	if (item.subMenu.onmouseout == null)
		item.subMenu.onmouseout = buttonOrMenuMouseout;

	// [END MODIFIED]
<%
	}
%>
	// Get position for submenu based on the menu item.

	x = getPageOffsetLeft(item) + item.offsetWidth;
	y = getPageOffsetTop(item);

	// Adjust position to fit in view.

	var maxX, maxY;

	if (browser.isIE) {
		maxX = Math.max(document.documentElement.scrollLeft, document.body.scrollLeft) +
			(document.documentElement.clientWidth != 0 ? document.documentElement.clientWidth : document.body.clientWidth);
		maxY = Math.max(document.documentElement.scrollTop, document.body.scrollTop) +
			(document.documentElement.clientHeight != 0 ? document.documentElement.clientHeight : document.body.clientHeight);
	}
	if (browser.isOP) {
		maxX = document.documentElement.scrollLeft + window.innerWidth;
		maxY = document.documentElement.scrollTop  + window.innerHeight;
	}
	if (browser.isNS) {
		maxX = window.scrollX + window.innerWidth;
		maxY = window.scrollY + window.innerHeight;
	}
	maxX -= item.subMenu.offsetWidth;
	maxY -= item.subMenu.offsetHeight;

	if (x > maxX)
		x = Math.max(0, x - item.offsetWidth - item.subMenu.offsetWidth
			+ (menu.offsetWidth - item.offsetWidth));
	y = Math.max(0, Math.min(y, maxY));

	// Position and show the sub menu.

	item.subMenu.style.left       = x + "px";
	item.subMenu.style.top        = y + "px";
	item.subMenu.style.visibility = "visible";

	// For IE; size, position and display the menu's IFRAME as well.

	if (item.subMenu.iframeEl != null) {
		item.subMenu.iframeEl.style.left    = item.subMenu.style.left;
		item.subMenu.iframeEl.style.top     = item.subMenu.style.top;
		item.subMenu.iframeEl.style.width   = item.subMenu.offsetWidth + "px";
		item.subMenu.iframeEl.style.height  = item.subMenu.offsetHeight + "px";
		item.subMenu.iframeEl.style.display = "";
	}

	// Stop the event from bubbling.

	if (browser.isIE)
		window.event.cancelBubble = true;
	else
		event.stopPropagation();
}

function closeSubMenu(menu) {

	if (menu == null || menu.activeItem == null)
		return;

	// Recursively close any sub menus.

	if (menu.activeItem.subMenu != null) {
		closeSubMenu(menu.activeItem.subMenu);
		menu.activeItem.subMenu.style.visibility = "hidden";

	// For IE, hide the sub menu's IFRAME as well.

		if (menu.activeItem.subMenu.iframeEl != null)
			menu.activeItem.subMenu.iframeEl.style.display = "none";

		menu.activeItem.subMenu = null;
	}

	// Deactivate the active menu item.

	removeClassName(menu.activeItem, "mIHighlight");
	menu.activeItem = null;
}

<%
	if (isMouseOver) {
%>
// [MODIFIED] Added for activate/deactivate on mouseover. Handler for mouseout event on buttons and menus.

function buttonOrMenuMouseout(event) {

	var el;

	// If there is no active button, exit.

	if (activeButton == null)
		return;

	// Find the element the mouse is moving to.

	if (browser.isIE)
		el = window.event.toElement;
	else if (event.relatedTarget != null)
		el = (event.relatedTarget.tagName ? event.relatedTarget : event.relatedTarget.parentNode);

	// If the element is not part of a menu, reset the active button.

	if (getContainerWith(el, "DIV", "menu") == null) {
		resetButton(activeButton);
		activeButton = null;
	}
}

// [END MODIFIED]
<%
	}
%>

//----------------------------------------------
// Code to initialize menus.
//----------------------------------------------

function menuInit(menu) {

	var itemList, spanList;
	var textEl, arrowEl;
	var itemWidth;
	var w, dw;
	var i, j;

	// For IE, replace arrow characters.

	if (browser.isIE) {
		menu.style.lineHeight = "2.5ex";
		spanList = menu.getElementsByTagName("SPAN");
		for (i = 0; i < spanList.length; i++)
			if (hasClassName(spanList[i], "mIArrow")) {
				spanList[i].style.fontFamily = "Webdings";
				spanList[i].firstChild.nodeValue = "4";
			}
	}

	// Find the width of a menu item.

	itemList = menu.getElementsByTagName("A");
	if (itemList.length > 0)
		itemWidth = itemList[0].offsetWidth;
	else
		return;

	// For items with arrows, add padding to item text to make the arrows flush right.

	for (i = 0; i < itemList.length; i++) {
		spanList = itemList[i].getElementsByTagName("SPAN");
		textEl  = null;
		arrowEl = null;
		for (j = 0; j < spanList.length; j++) {
			if (hasClassName(spanList[j], "mIText"))
				textEl = spanList[j];
			if (hasClassName(spanList[j], "mIArrow"))
				arrowEl = spanList[j];
		}
		if (textEl != null && arrowEl != null) {
			textEl.style.paddingRight = (itemWidth 
				- (textEl.offsetWidth + arrowEl.offsetWidth)) + "px";
			// For Opera, remove the negative right margin to fix a display bug.
			if (browser.isOP)
				arrowEl.style.marginRight = "0px";
		}
	}

	// Fix IE hover problem by setting an explicit width on first item of the menu.

	if (browser.isIE) {
		w = itemList[0].offsetWidth;
		itemList[0].style.width = w + "px";
		dw = itemList[0].offsetWidth - w;
		w -= dw;
		itemList[0].style.width = w + "px";
	}

	// Fix the IE display problem (SELECT elements and other windowed controls
	// overlaying the menu) by adding an IFRAME under the menu.

	if (browser.isIE) {
		menu.iframeEl = menu.parentNode.insertBefore(document.createElement("IFRAME"), menu);
		menu.iframeEl.style.display = "none";
		menu.iframeEl.style.position = "absolute";
	}

	// Mark menu as initialized.
	menu.isInitialized = true;
}

//----------------------------------------------
// General utility functions.
//----------------------------------------------

function getContainerWith(node, tagName, className) {

	// Starting with the given node, find the nearest containing element
	// with the specified tag name and style class.
	while (node != null) {
		if (node.tagName != null && node.tagName == tagName &&
				hasClassName(node, className))
			return node;
		node = node.parentNode;
	}

	return node;
}

function hasClassName(el, name) {

	var i, list;

	// Return true if the given element currently has the given class name.

	list = el.className.split(" ");
	for (i = 0; i < list.length; i++)
		if (list[i] == name)
			return true;

	return false;
}

function removeClassName(el, name) {

	var i, curList, newList;

	if (el.className == null)
		return;

	// Remove the given class name from the element's className property.

	newList = new Array();
	curList = el.className.split(" ");
	for (i = 0; i < curList.length; i++)
		if (curList[i] != name)
			newList.push(curList[i]);
	el.className = newList.join(" ");
}

function getPageOffsetLeft(el) {
	var x;
	// Return the x coordinate of an element relative to the page.
	x = el.offsetLeft;
	if (el.offsetParent != null)
	x += getPageOffsetLeft(el.offsetParent);

	return x;
}

function getPageOffsetTop(el) {
	var y;
	// Return the y coordinate of an element relative to the page.
	y = el.offsetTop;
	if (el.offsetParent != null)
	y += getPageOffsetTop(el.offsetParent);

	return y;
}

// AZ: workaround to avoid display issues in NS based browsers
var tempMenu;
function showMainMenu() {
	tempMenu.style.visibility = "visible";
}