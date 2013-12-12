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
				      "toolbar=no,menubar=no,personalbar=no,width=238,height=187," +
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
	} else if (isNaN(colorValue)) {
		// a system color value like "#999999": 
		if(((colorValue.indexOf("Text") > -1) && (colorValue.indexOf("Caption") < 0)) || (colorValue.indexOf("Frame") > -1)){
			theField.style.color = '#FFFFFF';		
		} else {
			theField.style.color = '#000000';
		}
		theField.style.backgroundColor = colorValue;		
		
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
	// test if system color values are used: 
	if("ACTIVEBORDER" == string) {
	  return "#dddddd";
	} 
	if("ACTIVECAPTION" == string) {
	  return "#1f232a";
	} 
	if("ACTIVECAPTIONTEXT" == string) {
	  return "#1f232aText";
	} 
	if("APPWORKSPACE" == string) {
	  return "#999999";
	} 
	if("BACKGROUND" == string) {
	  return "#000000";
	} 
	if("BUTTONFACE" == string) {
	  return "#dddddd";
	} 
	if("BUTTONHIGHLIGHT" == string) {
	  return "#ffffff";
	} 
	if("BUTTONSHADOW" == string) {
	  return "#999999";
	} 
	if("BUTTONTEXT" == string) {
	  return "#000000";
	} 
	if("CAPTIONTEXT" == string) {
	  return "#ffffff";
	} 
	if("GRAYTEXT" == string) {
	  return "#999999";
	} 
	if("HIGHLIGHT" == string) {
	  return "#1f232a";
	} 
	if("HIGHLIGHTTEXT" == string) {
	  return "#ffffff";
	} 
	if("INACTIVEBORDER" == string) {
	  return "#dddddd";
	} 
	if("INACTIVECAPTION" == string) {
	  return "#999999";
	} 
	if("INACTIVECAPTIONTEXT" == string) {
	  return "#dddddd";
	} 
	if("#f9f9f9" == string) {
	  return "#f9f9f9";
	} 
	if("INFOTEXT" == string) {
	  return "#ffffff";
	} 
	if("MENUTEXT" == string) {
	  return "MENUTEXT";
	} 
	if("MENU" == string) {
	  return "#dddddd";
	} 
	if("SCROLLBAR" == string) {
	  return "#dddddd";
	} 
	if("ThreeDDarkShadow" == string) {
	  return "#606161";
	} 
	if("#dddddd" == string) {
	  return "#dddddd";
	} 
	if("THREEDHIGHLIGHT" == string) {
	  return "#ffffff";
	} 
	if("THREEDLIGHTSHADOW" == string) {
	  return "#dddddd";
	} 
	if("#999999" == string) {
	  return "#999999";
	} 
	if("WINDOW" == string) {
	  return "window";
	} 
	if("WINDOWFRAME" == string) {
	  return "#000000";
	} 
	if("WINDOWTEXT" == string) {
	  return "#000000";
	} 

	// parse for numeric rgb colors ('#123456'):
	chars = '0123456789ABCDEF';
	out   = '';

	for (i=0; i<string.length; i++) {             // remove invalid color chars
		schar = string.charAt(i);
		if (chars.indexOf(schar) != -1) {
			out += schar;
		} else {
			return null;
		}
	}

	if (out.length != 6 && out.length != 3) {
		return null;
	}
	return out;
}