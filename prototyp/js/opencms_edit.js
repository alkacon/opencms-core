/*
 * File   : <CVS Filename>
 * Date   : <CVS Checkout Date>
 * Version: <CVS Version>
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

NS = (document.layers)? true:false;
IE = (document.all)? true:false;

function getDimensions() {
	if( NS==true ) {
		windowWidth = innerWidth-20;
		windowHeight = innerHeight-20;
	}
	else if( IE==true ) {
		windowWidth = document.body.clientWidth-20;
		windowHeight = document.body.clientHeight-20;
	}
}


function displayCode() {
  document.EDITTEXT.GUIApplet.setCode(code);
}


function writeAppletTag()
{
  var str=null;
  getDimensions();
  document.open();
  if (NS==true) 
	document.write('<APPLET name="GUIApplet" CODE="GUIApplet" WIDTH="100%" WIDTH="'+ (windowWidth-40) +'" HEIGHT="86%" HEIGHT="'+ (windowHeight-40) +'" mayscript></APPLET>');
  else if (IE==true)
  {
	str = '<OBJECT classid=clsid:EB3A74C0-5343-101D-BB4D-040224009C02 height=100% id=edit1 name=edit1 onfocus=setText(); width=100%><PARAM NAME="_Version" VALUE="131083"><PARAM NAME="_ExtentX" VALUE="22887"><PARAM NAME="_ExtentY" VALUE="7223"><PARAM NAME="_StockProps" VALUE="125"><PARAM NAME="Text" VALUE="Loading ....."><PARAM NAME="ForeColor" VALUE="0"><PARAM NAME="BackColor" VALUE="16777215"><PARAM NAME="BorderStyle" VALUE="1"><PARAM NAME="Enabled" VALUE="-1"><PARAM NAME="AutoIndent" VALUE="-1"><PARAM NAME="BackColorSelected" VALUE="8388608"><PARAM NAME="BookmarksMax" VALUE="16">';
    str= str + '<PARAM NAME="CanChangeFile" VALUE="0">';
	str= str + '<PARAM NAME="CanChangeFont" VALUE="0">';
	str= str + '<PARAM NAME="CaretWidth" VALUE="0">';
	str= str + '<PARAM NAME="DefaultSelection" VALUE="-1">';
	str= str + '<PARAM NAME="ExtraComments" VALUE="0">';
	str= str + '<PARAM NAME="Item2AsComment" VALUE="0">';
	str= str + '<PARAM NAME="UnixStyleSave" VALUE="0">';
	str= str + '<PARAM NAME="CurrentWordAsText" VALUE="0">';
	str= str + '<PARAM NAME="MultilineItems" VALUE="0">';
	str= str + '<PARAM NAME="MultilineStrings" VALUE="0">';
	str= str + '<PARAM NAME="SinglelineStrings" VALUE="0">';
	str= str + '<PARAM NAME="ExtraHorzSpacing" VALUE="3">';
	str= str + '<PARAM NAME="ExtraVertSpacing" VALUE="0">';
	str= str + '<PARAM NAME="FileMask" VALUE="\/All Files\/*.*\/">';
	str= str + '<PARAM NAME="FileName" VALUE="+">';
	str= str + '<PARAM NAME="ForeColorSelected" VALUE="16777215">';
	str= str + '<PARAM NAME="HasFile" VALUE="-1">';
	str= str + '<PARAM NAME="HasMenu" VALUE="0">';
	str= str + '<PARAM NAME="Highlight" VALUE="0">';
	str= str + '<PARAM NAME="InsertMode" VALUE="1">';
	str= str + '<PARAM NAME="ScrollBars" VALUE="3">';
	str= str + '<PARAM NAME="Syntax" VALUE="">';
	str= str + '<PARAM NAME="PaintMode" VALUE="0">';
	str= str + '<PARAM NAME="TabStopSize" VALUE="4">';
	str= str + '<PARAM NAME="ReadOnly" VALUE="0">';
	str= str + '<PARAM NAME="UndoDepth" VALUE="-1">';
	str= str + '<PARAM NAME="InitializeType" VALUE="0">';
	str= str + '<PARAM NAME="GroupNumber" VALUE="0">';
	str= str + '<PARAM NAME="StartInComments" VALUE="0">';
	str= str + '<PARAM NAME="MacStyleSave" VALUE="0">';
	str= str + '<PARAM NAME="WantTab" VALUE="-1">';
	str= str + '<PARAM NAME="WordWrap" VALUE="0">';
	str= str + '<PARAM NAME="WordWrapAuto" VALUE="-1">';
	str= str + '<PARAM NAME="WordWrapRMargin" VALUE="0">';
	str= str + '<PARAM NAME="WordWrapWidth" VALUE="0">';
	str= str + '<PARAM NAME="NoPrintDialog" VALUE="0">';
	str= str + '<PARAM NAME="NoPrintProgress" VALUE="0">';
	str= str + '<PARAM NAME="WrapKeys" VALUE="0">';
	str= str + '<PARAM NAME="BackColorPrint" VALUE="16777215">';
	str= str + '<PARAM NAME="PrintJobName" VALUE="Document">';
	str= str + '<PARAM NAME="ZeroSubstitute" VALUE="0"></OBJECT>';
	document.write(str);
	}
  document.close();
}

// to load the file content into the editor
function setText()
{
//    if(! textSetted )
//    {
//	   	document.all.edit1.Text = unescape("<#=content#>");
//	   	textSetted = true;
//	}
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