/*
 * File   : $Source: $
 * Date   : $Date: $
 * Version: $Revision: $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 * When using this script to open the color selection dialog, be sure to
 * initialize the colorPicker object properly:
 * - colorPicker.title is the title used for the popup dialog
 * - colorPicker.url is the absolute path to the popup dialog page
 */
 
var colorPicker = new Object();

colorPicker.title = "Color";
colorPicker.color = "000000";
colorPicker.url = "";

var currField;

function showColorPicker(fieldName) {
	var theField = document.getElementsByName(fieldName)[0];
	var fieldValue = theField.value;
	fieldValue = cutHexChar(fieldValue, "000000");
	if (document.all) {		
		colorPicker.color = fieldValue;
		var selColor = -1;
		selColor = showModalDialog(colorPicker.url, colorPicker, "resizable: yes; help: no; status: no; scroll: no;");
		if (selColor != null) {
			theField.value = "#" + selColor;
			previewColor(fieldName);
		}
	} else {
		// Mozilla based or other browser, use standards compliant method to open popup
		currField = theField;
		window.open(colorPicker.url + "?" + fieldValue, "colorpicker",
				      "toolbar=no,menubar=no,personalbar=no,width=10,height=10," +
				      "scrollbars=no,resizable=yes"); 
	}
}

function setColor(color) {
	if (currField != null) {
		currField.value = "#" + color;
		previewColor(currField.name);
	}
}

function cutHexChar(fieldValue, defaultValue) {
	if (fieldValue != null) {
		if (fieldValue.charAt(0) == "#") {
			return fieldValue.slice(1);
		} else {
			return fieldValue;
		}
	} else {
		return defaultValue;
	}
}

function previewColor(fieldName) {
	var theField = document.getElementsByName(fieldName)[0];
	var colorValue = validateColor(cutHexChar(theField.value, "FFFFFF"));
	if (colorValue == null) {
		theField.style.color = '#000000';
		theField.style.backgroundColor = '#FFFFFF';
	} else if (colorValue < 50000) {
		theField.style.color = '#FFFFFF';
		theField.style.backgroundColor = "#" + colorValue;
	} else {
		theField.style.color = '#000000';
		theField.style.backgroundColor = "#" + colorValue;
	}
}

function validateColor(string) {                // return valid color code
	string = string || '';
	string = string + "";
	string = string.toUpperCase();
	chars = '0123456789ABCDEF';
	out   = '';

	for (i=0; i<string.length; i++) {             // remove invalid color chars
		schar = string.charAt(i);
		if (chars.indexOf(schar) != -1) {
			out += schar;
		}
	}

	if (out.length != 6 && out.length != 3) {
		return null;
	}
	return out;
}