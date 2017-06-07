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

//------------------------------------------------------//
// Script for XML content editor
//------------------------------------------------------//

// Searches for a frame by the specified name. Will only return siblings or ancestors.
function findFrame(startFrame, frameName){
    if (startFrame == top){
        // there may be security restrictions prohibiting access to the frame name
        try{
            if (startFrame.name == frameName){
                return startFrame;
            }
        }catch(err){}
        return null;
    }
    for (var i=0; i<startFrame.parent.frames.length; i++){
        // there may be security restrictions prohibiting access to the frame name
        try{
            if (startFrame.parent.frames[i].name == frameName) {
                return startFrame.parent.frames[i];
            }
        }catch(err){}
    }
    return findFrame(startFrame.parent, frameName);
}
// the editors top frame target, may be !='_top' if in advanced direct edit!
var editorTopFrameTarget= '_top';
if (top.frames['cmsAdvancedDirectEditor']!=null){
    editorTopFrameTarget='cmsAdvancedDirectEditor';
}
// edit frame object
var editFrame=findFrame(self,'edit');

// stores the opened window object
var treewin = null;
// stores id prefix of opened element operation buttons
var oldEditorButtons = null;
// helper for the timeout on button row
var buttonTimer = null;
// mouse up & resize event closes open element operation buttons
document.onmouseup = hideElementButtons;
window.onresize = hideElementButtons;
document.onkeydown = handleKeyDownEvents;

// adds common shortcuts to the editor
function handleKeyDownEvents(ev) {

	if (!ev) {
		ev = window.event;
	}
	if (ev.ctrlKey) {
		var key;
		if (ev.which) {
			key = ev.which;
		} else {
			key = ev.keyCode;
		}
		if (key == 83) {
			// 's' pressed
			if (ev.shiftKey) {
				// save content and exit
				buttonAction(2);
			} else {
				// save content without exiting
				buttonAction(3);
			}
			return false;
		}
		if (ev.shiftKey && key == 88) {
			// 'x' pressed, exit editor
			confirmExit();
			return false;
		}
	}
	return true;
}

// function action on button click
function buttonAction(para) {
	var _form = document.EDITOR;
	_form.target = "_self";
	submit(_form);
	try {
		editFrame.buttonbar.focus();
	} catch (e) {}
	var isWp = false;
    try { 
        if (top.document.querySelector(".o-editor-frame")) {
            isWp = true; 
        } else { 
            isWp = false; 
        }
    } catch (e) {}

	switch (para) {
	case 1:
		// exit editor without saving
		_form.action.value = actionExit;
        if (isWp) {
            _form.target="_self";
            _form.submit();
            break;
        }
		_form.target = editorTopFrameTarget;
		_form.submit();
		break;
	case 2:
		// save and exit editor
		_form.action.value = actionSaveExit;
        if (isWp) {
            _form.target="_self";
            _form.submit();
            break;
        }
		_form.submit();
		break;
	case 3:
		// save content
		setLastPosition();
		_form.action.value = actionSave;
		_form.submit();
		break;
	case 4:
		// change element (change locale)
		clearLastPosition();
		_form.action.value = actionChangeElement;
		_form.submit();
		break;
	case 5:
		// add optional element
		_form.action.value = actionAddElement;
		_form.submit();
		break;
	case 6:
		// remove optional element
		_form.action.value = actionRemoveElement;
		_form.submit();
		break;
	case 7:
		// preview
		_form.action.value = actionPreview;
		_form.target = "PREVIEW";
		openWindow = window.open("about:blank", "PREVIEW", "width=950,height=700,left=10,top=10,resizable=yes,scrollbars=yes,location=yes,menubar=yes,toolbar=yes,dependent=yes");
		_form.submit();
		break;
	case 8:
		// check elements before performing customized action
		clearLastPosition();
		_form.action.value = actionCheck;
		_form.submit();
		break;
	case 9:
		// save and perform customized action
		_form.action.value = actionSaveAction;
		_form.target = editorTopFrameTarget;
		_form.submit();
		break;
	case 10:
		// move element down
		_form.action.value = actionMoveElementDown;
		_form.submit();
		break;
	case 11:
		// move element up
		_form.action.value = actionMoveElementUp;
		_form.submit();
		break;
	case 12:
		// correct the XML structure
		_form.action.value = actionCorrectXml;
		_form.submit();
		break;
	case 14:
		// delete the current locale content
		clearLastPosition();
		_form.action.value = actionDeleteLocale;
		_form.submit();
		break;
	case 15:
		// copy the current locale content
		_form.action.value = actionCopyLocale;
		_form.submit();
		break;
	default:
		alert("No action defined for this button!");
		break;
	}
}

function submit(form) {
	try {
		// submit html editing areas if present
		submitHtml(form);
	} catch (e) {}
}

function opensmallwin(url, name, w, h) {
	encodedurl = encodeURI(url);
	smallwindow = window.open(encodedurl, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(smallwindow != null) {
		if (smallwindow.opener == null) {
			smallwindow.opener = self;
		}
	}
	return smallwindow;
}

// add an optional element to the currently edited content
function addElement(elemName, insertAfter, addOptions) {
	setLastPosition();
  var _form = document.EDITOR;
  _form.elementname.value = elemName;
  _form.elementindex.value = insertAfter;
  if (_form.choiceelement) {
    _form.choiceelement.value = "";
  }
  if (addOptions == null) {
	 addOptions = "";
  }
	addOptions = decodeURIComponent(addOptions);
	if (addOptions != "" && addOptions != "[]") {
    var optionalElements = eval(addOptions);
    var choiceType = optionalElements[0].choicetype;
    _form.choicetype.value = choiceType;
    $("#xmladdelementdialog").html(buildChoiceDialogOptions(optionalElements));
    $("#xmladdelementdialog").dialog("option", "title", dialogTitleAddChoice);
    $("#xmladdelementdialog").dialog("open");
  } else {
  	buttonAction(5);
	}
}

// adds the selected choice element or gets sub choices for the selected elements
function addChoiceElement(choiceElement, subChoice) {
  var _form = document.EDITOR;
  _form.choiceelement.value = choiceElement;
  if (subChoice == true) {
    $.post(vfsPathEditorForm, { action: actionSubChoices, resource: _form.resource.value, tempfile: _form.tempfile.value, elementname: _form.elementname.value, elementindex: _form.elementindex.value, choiceelement: choiceElement, choicetype: _form.choicetype.value}, function(data){ buildChoiceDialog(data); });
  } else {
    buttonAction(5);
  }
}

// creates the dialog to select sub choice options
function buildChoiceDialog(data) {
  $("#xmladdelementdialog").dialog("close");
  $("#xmladdelementdialog").html(buildChoiceDialogOptions(eval(data)));
  $("#xmladdelementdialog").dialog("option", "title", dialogTitleAddSubChoice);
  $("#xmladdelementdialog").dialog("open");
}

//creates the HTML for the dialog to select the choice options
function buildChoiceDialogOptions(optionalElements) {
  var addHtml = "";
  for (var i=1; i<optionalElements.length; i++) {
    var currOption = optionalElements[i];
    var optName = currOption.name;
    if (document.EDITOR.choiceelement.value != "") {
      optName = document.EDITOR.choiceelement.value + "/" +optName;
    }
    addHtml += "<div class=\"xmlChoiceItem\" onclick=\"addChoiceElement('";
    addHtml += optName + "', " + currOption.subchoice + ");";
    addHtml += "\">" + currOption.label;
    if (currOption.help != "") {
      addHtml += "<div class=\"xmlChoiceHelp\">" + currOption.help + "</div>";
    }
    addHtml += "</div>";
  }
  return addHtml;
}

// move an element in currently edited content
function moveElement(elemName, index, direction) {
	setLastPosition();
	var _form = document.EDITOR;
	_form.elementname.value = elemName;
	_form.elementindex.value = index;
	if (direction == "down") {
		buttonAction(10);
	} else {
		buttonAction(11);
	}

}

// remove an optional element from currently edited content
function removeElement(elemName, index) {
	setLastPosition();
	var _form = document.EDITOR;
	_form.elementname.value = elemName;
	_form.elementindex.value = index;
	buttonAction(6);
}

// clears the last scroll position
function clearLastPosition() {
	try {
		editFrame.setLastPosY(0);
	} catch (e) {}
}

// sets the last scroll position to return to
function setLastPosition() {
	try {
		if (browser.isIE) {
			editFrame.setLastPosY(document.body.scrollTop);
		} else {
			editFrame.setLastPosY(window.pageYOffset);
		}
	} catch (e) {
		// ignore
	}
}

// checks and adjusts the language selector in case an error is found in the edited content
function checkElementLanguage(newValue) {
	try {
		var langBox = parent.buttonbar.document.forms["buttons"].elements["elementlanguage"];
		if (langBox.value != newValue) {
			langBox.value = newValue;
		}
	} catch (e) {
		// ignore
	}
}

// submits the checked form for customized action button and considers delayed string insertion
function submitSaveAction() {
	if (! initialized) {
		setTimeout('submitSaveAction()', 20);
		return;
	}
	if (stringsPresent == true) {
		if (stringsInserted == true) {
			buttonAction(9);
		} else {
			setTimeout('submitSaveAction()', 20);
		}
	} else {
		buttonAction(9);
	}
}

// checks if the preview button is shown in the form for download or image galleries
function checkPreview(fieldId) {
	try {
		var theUri = document.getElementById(fieldId).value;
		theUri = theUri.replace(/ /, "");
		if ((theUri != "") && (theUri.charAt(0) == "/" || theUri.indexOf("http://") == 0)) {
			document.getElementById("preview" + fieldId).className = "show";
		} else {
			document.getElementById("preview" + fieldId).className = "hide";
		}
	} catch (e) {
		document.getElementById("preview" + fieldId).className = "hide";
	}
}

// scrolls the input form to the position where last element was added or removed
function scrollForm() {
	var posY = 0;
	try {
		posY = editFrame.lastPosY;
	} catch (e) {}
	window.scrollTo(0, posY);
}

// closes the popup window, this method is called by the onunload event
function closeTreeWin() {
	if (treewin != null) {
		// close the file selector window
		window.treewin.close();
		treewin = null;
		treeForm = null;
		treeField = null;
		treeDoc = null;
	}
}

// shows the element operation buttons
function showElementButtons(elementName, elementIndex, showRemove, showUp, showDown, showAdd, addOptions) {
	var elemId = elementName + "." + elementIndex;
	if (oldEditorButtons != null && oldEditorButtons != elemId) {
		// close eventually open element buttons
		document.getElementById("xmlElementButtons").style.visibility = "hidden";
	}
	// get button element
	var elem = document.getElementById("xmlElementButtons");

	// create the button row HTML
	var buttons = "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";

	// remove element button
	if (showRemove) {
		buttons += button("javascript:removeElement('" + elementName + "', " + elementIndex + ")", null, "deletecontent", LANG_BT_DELETE, buttonStyle);
	} else {
		buttons += button(null, null, "deletecontent_in", LANG_BT_DELETE, buttonStyle);
	}

	// move up element button
	if (showUp) {
		buttons += button("javascript:moveElement('" + elementName + "', " + elementIndex + ", 'down')", null, "move_up", LANG_BT_MOVE_UP, buttonStyle);
	} else {
		buttons += button(null, null, "move_up_in", LANG_BT_MOVE_UP, buttonStyle);
	}

	// move down element button
	if (showDown) {
		buttons += button("javascript:moveElement('" + elementName + "', " + elementIndex + ", 'up')", null, "move_down", LANG_BT_MOVE_DOWN, buttonStyle);
	} else {
		buttons += button(null, null, "move_down_in", LANG_BT_MOVE_DOWN, buttonStyle);
	}

	// add element button
	if (showAdd) {
		buttons += button("javascript:addElement('" + elementName + "', " + elementIndex + ", '" + encodeURIComponent(addOptions) + "')", null, "new", LANG_BT_ADD, buttonStyle);
	} else {
		buttons += button(null, null, "new_in", LANG_BT_ADD, buttonStyle);
	}

	buttons += "</table>";

	// set the created HTML
	elem.innerHTML = buttons;
	// get the icon
	var icon = document.getElementById("btimg." + elemId);
	showEditorElement(elem, icon, 10, 20, true);
	oldEditorButtons = elemId;
}

// hides the element operation buttons
function hideElementButtons() {
	if (oldEditorButtons != null) {
		document.getElementById("xmlElementButtons").style.visibility = "hidden";
		oldEditorButtons = null;
		return false;
	}
	return true;
}

// checks presence of element buttons and hides row after a timeout
function checkElementButtons(resetTimer) {
	if (resetTimer) {
		// reset the timer because cursor is over buttons
		if (buttonTimer != null) {
			clearTimeout(buttonTimer);
		}
	} else {
		// set timeout for button row (mouseout)
		buttonTimer = setTimeout("hideElementButtons()", 1500);
	}
}

// shows the specified element belonging to the given icon (for help & element buttons
function showEditorElement(elem, icon, xOffset, yOffset, alignToLeft) {

    if (elem.style.visibility != "visible" && icon) {
	    var x = findPosX(icon) + xOffset;
	    var y = findPosY(icon) + yOffset;
	    var textHeight = elem.scrollHeight;
	    var textWidth = elem.scrollWidth;
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
	    if ((x + textWidth) > (clientWidth + scrollLeft) || alignToLeft) {
	        x = x - textWidth;
	    }
	    if (x < scrollLeft) {
	        x = (clientWidth + scrollLeft) - (textWidth + scrollSize);
	    }
	    if (x < scrollLeft) {
	        x = scrollLeft;
	    }

	    if (alignToLeft) {
	    	x += xOffset;
	    }

	    elem.style.left = x + "px";
	    elem.style.top =  y + "px";
	    elem.style.visibility = "visible";
	    return y;
    }
}

// finds the x position of an element
function findPosX(obj) {
    var curleft = 0;
    if (obj && obj.offsetParent) {
        while (obj.offsetParent) {
            curleft += obj.offsetLeft - obj.scrollLeft;
            obj = obj.offsetParent;
        }
    } else if (obj && obj.x) {
        curleft += obj.x;
    }
    return curleft;
}

// finds the y position of an element
function findPosY(obj) {
    var curtop = 0;
    if (obj && obj.offsetParent) {
        while (obj.offsetParent) {
            curtop += obj.offsetTop - obj.scrollTop;
            obj = obj.offsetParent;
        }
    } else if (obj && obj.y) {
        curtop += obj.y;
    }
    return curtop;
}

// formats a button in one of 3 styles (type 0..2)
function button(href, target, image, label, type) {

	if (image != null && image.indexOf('.') == -1) {
        // append default suffix for images
        image += ".png";
    }

	var result = "<td>";
	switch (type) {
		case 1:
		// image and text
		if (href != null) {
			result += "<a href=\"";
			result += href;
			result += "\" class=\"button\"";
			if (target != null) {
				result += " target=\"";
				result += target;
				result += "\"";
			}
			result += ">";
		}
		result += "<span unselectable=\"on\"";
		if (href != null) {
			result += " class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"";
		} else {
			result += " class=\"disabled\"";
		}
		result += "><span unselectable=\"on\" class=\"combobutton\" ";
		result += "style=\"background-image: url('";
		result += skinUri;
		result += "buttons/";
		result += image;
		result += "');\">";
		result += label;
		result += "</span></span>";
		if (href != null) {
			result += "</a>";
		}
		break;

		case 2:
		// text only
		if (href != null) {
			result += "<a href=\"";
			result += href;
			result += "\" class=\"button\"";
			if (target != null) {
				result += " target=\"";
				result += target;
				result += "\"";
			}
			result += ">";
		}
		result += "<span unselectable=\"on\"";
		if (href != null) {
			result += " class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"";
		} else {
			result += " class=\"disabled\"";
		}
		result += "><span unselectable=\"on\" class=\"txtbutton\">";
		result += label;
		result += "</span></span>";
		if (href != null) {
			result += "</a>";
		}
		break;

		default:
		// only image
		if (href != null) {
			result += "<a href=\"";
			result += href;
			result += "\" class=\"button\"";
			if (target != null) {
				result += " target=\"";
				result += target;
				result += "\"";
			}
			result += " title=\"";
			result += label;
			result += "\">";
		}
		result += "<span unselectable=\"on\"";
		if (href != null) {
			result += " class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"";
		} else {
			result += " class=\"disabled\"";
		}
		result += "><img class=\"button\" src=\"";
		result += skinUri;
		result += "buttons/";
		result += image;
		result += "\">";
		result += "</span>";
		break;
	}
	result += "</td>\n";
	return result;
}