/*
* File   : $Source: $
* Date   : $Date: $
* Version: $Revision: $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
//------------------------------------------------------//
// Script for simple page editor
//------------------------------------------------------//

// Definition of constants
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;
var SAVEACTION=55;

// dialog windows
var dialogElementWindow = null;
var dialogPropertyWindow = null;

// Indicates if the text of the editor window is already set
var textSetted = false;

// loads the file content into the editor
function setText()
{
    // setting text can not be done now here for the text editor.
    // MS IE 5 has problems with setting text when the editor control is
    // not loaded. 
    // Workaround: focus() the text editor here and set the text
    // using the onFocus event of the editor.

    if (document.forms.EDITOR.edit1) document.forms.EDITOR.edit1.focus();
}

// load the file content into the editor. this is called by the onFocus event of the edit control
function setTextDelayed()
{
	if(!textSetted) {
        document.EDITOR.edit1.value = decodeURIComponent(text);
		textSetted = true;
	}
}

function doSubmit() {
    // We have to do a blur on the textarea here. otherwise netscape may have problems with reading the value
    document.EDITOR.edit1.blur();
    document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
}

// Function action on button click for Netscape Navigator
function doNsEdit(para) {
    switch(para)
    {
    case 1:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "exit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 2:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "saveexit";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    case 3:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "save";
        document.EDITOR.target = "_self";
        document.EDITOR.submit();
        break;
    }
    case SAVEACTION:
    {
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.edit1.value);
        document.EDITOR.action.value = "saveaction";
        document.EDITOR.target = "_top";
        document.EDITOR.submit();
        break;
    }
    }
}

// which button is clicked
function doTemplSubmit(para) {
	document.EDITOR.action.value = "";
	switch (para)
	{
	case 1:
		// reload the editor
		doSubmit();
		document.EDITOR.action.value = "show";
		document.EDITOR.target = "_self";
		document.EDITOR.submit();
		break;
	case 2:
		// preview selected	
		doSubmit();		
		document.EDITOR.action.value = "preview";
		document.EDITOR.target = "PREVIEW";
		document.EDITOR.submit();
		break;
	case 3:
		// change body;
		doSubmit();
		document.EDITOR.action.value = "changebody";
		document.EDITOR.target = "_self";
		document.EDITOR.submit();
		break;
	case 4:
		// open elements window;
		dialogElementWindow = window.open("about:blank","DIALOGELEMENT","width=320,height=250,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,dependent=yes");
		document.ELEMENTS.submit();
		dialogElementWindow.focus();
		break;		
	case 5:
		// open properties window;
		dialogPropertyWindow = window.open("about:blank","DIALOGPROPERTY","width=600,height=320,left=0,top=0,resizable=yes,scrollbars=no,location=no,menubar=no,toolbar=no,dependent=yes");
		document.PROPERTIES.submit();
		dialogPropertyWindow.focus();
		break;
	}
}

function deleteEditorContent(bodyName, language) {
	if (bodyName == document.EDITOR.bodyname.value && language == document.EDITOR.bodylanguage.value) {
		document.EDITOR.edit1.value = "";
	}
}

function changeBody(bodyName, language) {
	if (bodyName != document.EDITOR.bodyname.value && language == document.EDITOR.bodylanguage.value) {
		document.EDITOR.bodyname.value = bodyName;
		doTemplSubmit(3);	
	} else {
		doTemplSubmit(1);
	}
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