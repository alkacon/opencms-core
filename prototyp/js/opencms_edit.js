/*
 * File   : $Source: /alkacon/cvs/opencms/prototyp/js/Attic/opencms_edit.js,v $
 * Date   : $Date: 2000/03/21 16:48:31 $
 * Version: $Revision: 1.9 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

//------------------------------------------------------//
// Script for editcontrol
//------------------------------------------------------//

// Definition of Constants
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;

var UNDO=4;
var REDO=5;

var SEARCH=6;
var REPLACE=7;
var GOTO=8;

var CUT=9;
var COPY=10;
var PASTE=11;

var IMPORT=12;
var EXPORT=13;
var EXPORTAS=14;

var PRINT=15;


var windowWidth=null;
var windowHeight=null;
var NS = null;
var IE = null;
var str=null;
var textSetted=null;

NS = (document.layers)? true:false;
IE = (document.all)? true:false;

// function for calculating the right dimensions of a HTML textarea
function getDimensions() {
	if( NS==true ) {
		windowWidth = innerWidth;
		windowHeight = innerHeight;
	}
	else if( IE==true ) {
		windowWidth = document.body.clientWidth;
		windowHeight = document.body.clientHeight;
	}
	windowWidth = Math.round(windowWidth/8.5);
	windowHeight = Math.round(windowHeight/19);
}


// to load the file content into the editor
function setText()
{
	  if(! textSetted )
	  {
		 if (IE)
	   		document.all.edit1.Text = unescape("<#=content#>");
		 else if (NS)
			document.EDITTEXT.TextEditor.value = unescape(code);
	   	textSetted = true;
	}
}

// Function action on button click
function doEdit(para)
{
	switch(para)
	{
	case 1:
	{
		top.close();
		break;
	}
	case 2:
	{
		history.back();
		break;
	}
	case 3:
	{
		//	history.back();
			break;
	}
	
	case 4:
	{
		document.all.edit1.Undo();
		break;	
	}
	case 5:
	{
		document.all.edit1.Redo();
		break;	
	}
	case 6:
	{
		document.all.edit1.ShowFindDialog();
		break;
	}
	case 7:
	{
		document.all.edit1.ShowReplaceDialog();
		break;
	}
	case 8:
	{
		document.all.edit1.ShowGotoLineDialog();
		break;	
	}
	case 9:
	{
		document.all.edit1.CutToClipboard();
		break;
	}
	case 10:
	{
		document.all.edit1.CopyToClipboard();
		break;
	}
	case 11:
	{
		document.all.edit1.PasteFromClipboard();
		break;
	}
	case 12:
	{
	    document.all.edit1.OpenFile();
		break;
	}
	case 13:
	{
	    document.all.edit1.SaveFile();
		break;
	}
	case 14:
	{
	    document.all.edit1.SaveFileAs();
		break;
	}
	case 15:
	{
	    document.all.edit1.PrintText();
		break;
	}
	default:
	{
		alert("NYI");
		break;
	}
}	
	document.all.edit1.focus();
}