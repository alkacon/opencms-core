/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.editors.htmlarea/resources/system/workplace/resources/components/widgets/Attic/htmlarea.js,v $
 * Date   : $Date: 2005/10/01 20:50:06 $
 * Version: $Revision: 1.1.2.3 $
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
 * These scripts are required for the html areas in the xml content editor
 */
 
// HTMLArea global objects
var htmlAreas = new Array();
var textAreas = new Array();
var htmlAreaConfigs = new Array();

// creates a HtmlArea configuration object with standard values set
function getHtmlAreaConfiguration() {

	var newConfig = new HTMLArea.Config();

	newConfig.pageStyle = 
		'body { font-family:verdana, sans-serif; font-size:11px; font-weight:normal; margin: 0 0 0 0; padding: 1 1 1 1; border-width:2px; border-color:#FFF; border-style:inset; } ';

	// disable the status bar
	newConfig.statusBar = false;

	// kill MS Word formatting on paste
	newConfig.killWordOnPaste = true;
	
	// set autofocus to false to avoid jumping to last htmlarea
	newConfig.autoFocus = false;
	
	return newConfig;
}

// generates the HtmlArea editor instances
function generateHtmlAreas() {
	var tas = document.getElementsByTagName("textarea");
	for (var i=0; i<tas.length; i++) {
		var idAttr = tas[i].getAttribute("id");
		if (idAttr != null && idAttr != "") {
			// only use textareas with "id" attribute value set
			textAreas[textAreas.length] = tas[i];
			var ha = new HTMLArea(tas[i], htmlAreaConfigs[idAttr]);
			htmlAreas[htmlAreas.length] = ha;
			ha.generate();
		}
	}
}

// writes the HTML back from the editor instances back to the textareas
function submitHtml(form) {
	for (var i=0; i<textAreas.length; i++) {
		ta = textAreas[i];
		ha = htmlAreas[i];
		ta.value = encodeURIComponent(ha.getHTML());
	}
}