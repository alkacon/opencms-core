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

//------------------------------------------------------//
// Script for EditArea text editor (editor with syntax highlighting)
//------------------------------------------------------//

// loads the file content into the editor, is called by the onload event of the body
function setContent() {
	if (document.forms.EDITOR.edit1) {
		document.EDITOR.edit1.value = decodeURIComponent(content);
		contentSetted = true;
		// workaround for IE browsers to start at the first line, not in the middle of nowhere
		setTimeout("editAreaLoader.setSelectionRange(\"editarea\", 0, 0);", 250);
	}
}

// function action on button click
function buttonAction(para) {
	// We have to do a blur on the textarea here. Otherwise Netscape may have problems with reading the value
	var _form = document.EDITOR;
	_form.edit1.blur();
	_form.content.value = encodeURIComponent(editAreaLoader.getValue("editarea"));

	switch (para) {
	case 1:
	{
		_form.action.value = actionExit;
		_form.target = "_top";
		_form.submit();
		break;
	}
	case 2:
	{
		_form.action.value = actionSaveExit;
		_form.target = "_top";
		_form.submit();
		break;
	}
	case 3:
	{
		_form.action.value = actionSave;
		_form.submit();
		break;
	}
	default:
	{
		alert("No action defined for this button!");
		break;
	}
	}
}

document.onkeydown = keyDownHandler;

function keyDownHandler(e) {
	// EVENT HANDLER: shortcuts (have to be added to editor JS additionally)
	if (!e) {
		// if the browser did not pass the event information to the
		// function, we will have to obtain it from the event register
		if (window.event) {
			//DOM
			e = window.event;
		} else {
			// total failure, we have no way of referencing the event
			return;
		}
	}

	if (typeof(e.which) == 'number') {
		//NS 4, NS 6+, Mozilla 0.9+, Opera
		key = e.which;
	} else if (typeof(e.keyCode) == 'number') {
		//IE, NS 6+, Mozilla 0.9+
		key = e.keyCode;
	} else if (typeof(e.charCode) == 'number') {
		//also NS 6+, Mozilla 0.9+
		key = e.charCode;
	} else {
		// total failure, we have no way of obtaining the key code
		return;
	}

	if (e.ctrlKey) {
		if (key == 83) {
			// 's' pressed
			if (e.shiftKey == true) {
				// save content and exit
				buttonAction(2);
			} else {
				// save content without exiting
				buttonAction(3);
			}
			return false;
		}
		if (e.shiftKey && key == 88) {
			// 'x' pressed, exit editor
			confirmExit();
			return false;
		}
	}
}