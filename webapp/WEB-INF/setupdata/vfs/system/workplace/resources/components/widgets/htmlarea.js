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
 * These scripts are required for the html areas in the xml content editor
 */
 
// HTMLArea configuration
var config;
var htmlAreas = new Array();
var textAreas = new Array();

function initHtmlAreas() {

	config = new HTMLArea.Config();
	config.toolbar = [
		[
			"copy", "cut", "paste", "separator",
			"bold", "italic", "underline", "separator",
			"strikethrough", "subscript", "superscript", "separator",
			"insertorderedlist", "insertunorderedlist", "outdent", "indent", "separator",
			"htmlmode"
		]
	];

	config.pageStyle = 
		'body { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; margin: 0 0 0 0; padding: 1 1 1 1; border-width:2px; border-color:#FFF; border-style:inset; } ';

	// disable the status bar
	config.statusBar = false;

	// kill MS Word formatting on paste
	config.killWordOnPaste = true;
	
	// set autofocus to false to avoid jumping to last htmlarea
	config.autoFocus = false;

	var tas = document.getElementsByTagName("textarea");
	for (var i=0; i<tas.length; i++) {
		var idAttr = tas[i].getAttribute("id");
		if (idAttr != null && idAttr != "") {
			// only use textareas with "id" attribute value set
			textAreas[textAreas.length] = tas[i];
			var ha = new HTMLArea(tas[i], config);
			htmlAreas[htmlAreas.length] = ha;
			ha.generate();
		}
	}
}

function registerHtmlArea(id) {
}

function submitHtmlArea(form) {
	for (var i=0; i<textAreas.length; i++) {
		ta = textAreas[i];
		ha = htmlAreas[i];
		ta.value = encodeURIComponent(ha.getHTML());
	}
}