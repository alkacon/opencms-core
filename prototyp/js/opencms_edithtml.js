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
// Script for  html editcontrol
//------------------------------------------------------//

// Definition of constants, which button is clicked
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;

var UNDO=4;
var REDO=5;

//var SEARCH=6;
//var REPLACE=7;
var GOTO=8;

var CUT=9;
var COPY=10;
var PASTE=11;

var IMPORT=12;
var EXPORT=13;
var EXPORTAS=7;

var PRINT=15;

var FORMAT=21;
var FONTFACE=22;
var FONTSIZE=23;
var BOLD=24;
var ITALIC=25;
var UNDERLINE=26;

var ALIGNLEFT=31;
var ALIGNCENTER=32;
var ALIGNRIGHT=33;
var ULIST=34;
var OLIST=35;
var INDENTIN=36;
var INDENTOUT=37;
var FONTCOLOR=38;
var BACKCOLOR=39;
var TABLE=40;
var LINK=41;
var PIC=42;
var PICLIST=43;

// Command IDs for the activeX contros
//
DECMD_BOLD =                      5000
DECMD_COPY =                      5002
DECMD_CUT =                       5003
DECMD_DELETE =                    5004
DECMD_DELETECELLS =               5005
DECMD_DELETECOLS =                5006
DECMD_DELETEROWS =                5007
DECMD_FINDTEXT =                  5008
DECMD_FONT =                      5009
DECMD_GETBACKCOLOR =              5010
DECMD_GETBLOCKFMT =               5011
DECMD_GETBLOCKFMTNAMES =          5012
DECMD_GETFONTNAME =               5013
DECMD_GETFONTSIZE =               5014
DECMD_GETFORECOLOR =              5015
DECMD_HYPERLINK =                 5016
DECMD_IMAGE =                     5017
DECMD_INDENT =                    5018
DECMD_INSERTCELL =                5019
DECMD_INSERTCOL =                 5020
DECMD_INSERTROW =                 5021
DECMD_INSERTTABLE =               5022
DECMD_ITALIC =                    5023
DECMD_JUSTIFYCENTER =             5024
DECMD_JUSTIFYLEFT =               5025
DECMD_JUSTIFYRIGHT =              5026
DECMD_LOCK_ELEMENT =              5027
DECMD_MAKE_ABSOLUTE =             5028
DECMD_MERGECELLS =                5029
DECMD_ORDERLIST =                 5030
DECMD_OUTDENT =                   5031
DECMD_PASTE =                     5032
DECMD_REDO =                      5033
DECMD_REMOVEFORMAT =              5034
DECMD_SELECTALL =                 5035
DECMD_SEND_BACKWARD =             5036
DECMD_BRING_FORWARD =             5037
DECMD_SEND_BELOW_TEXT =           5038
DECMD_BRING_ABOVE_TEXT =          5039
DECMD_SEND_TO_BACK =              5040
DECMD_BRING_TO_FRONT =            5041
DECMD_SETBACKCOLOR =              5042
DECMD_SETBLOCKFMT =               5043
DECMD_SETFONTNAME =               5044
DECMD_SETFONTSIZE =               5045
DECMD_SETFORECOLOR =              5046
DECMD_SPLITCELL =                 5047
DECMD_UNDERLINE =                 5048
DECMD_UNDO =                      5049
DECMD_UNLINK =                    5050
DECMD_UNORDERLIST =               5051
DECMD_PROPERTIES =                5052
//
// Enums
//
// OLECMDEXECOPT  
OLECMDEXECOPT_DODEFAULT =         0 
OLECMDEXECOPT_PROMPTUSER =        1
OLECMDEXECOPT_DONTPROMPTUSER =    2
// DHTMLEDITCMDF
DECMDF_NOTSUPPORTED =             0 
DECMDF_DISABLED =                 1 
DECMDF_ENABLED =                  3
DECMDF_LATCHED =                  7
DECMDF_NINCHED =                  11
// DHTMLEDITAPPEARANCE
DEAPPEARANCE_FLAT =               0
DEAPPEARANCE_3D =                 1 
// OLE_TRISTATE
OLE_TRISTATE_UNCHECKED =          0
OLE_TRISTATE_CHECKED =            1
OLE_TRISTATE_GRAY =               2


function DisplayChanged()
{
  var i, s;
		 
  s =  document.all.EDIT_HTML.QueryStatus(DECMD_GETBLOCKFMT);
  if (s == DECMDF_DISABLED || s == DECMDF_NOTSUPPORTED) {
	document.all.BLOCK.disabled = true;
 } else {
	document.all.BLOCK.disabled = false;
    document.all.BLOCK.value =  document.all.EDIT_HTML.ExecCommand(DECMD_GETBLOCKFMT, OLECMDEXECOPT_DODEFAULT);
 }
  s =  document.all.EDIT_HTML.QueryStatus(DECMD_GETFONTNAME);
  if (s == DECMDF_DISABLED || s == DECMDF_NOTSUPPORTED) {
	document.all.FONTFACE.disabled = true;
  } else {
	document.all.FONTFACE.disabled = false;
    document.all.FONTFACE.value =  document.all.EDIT_HTML.ExecCommand(DECMD_GETFONTNAME, OLECMDEXECOPT_DODEFAULT);
  }
  if (s == DECMDF_DISABLED || s == DECMDF_NOTSUPPORTED) {
	document.all.FONTSIZE.disabled = true;
  } else {
	document.all.FONTSIZE.disabled = false;
    document.all.FONTSIZE.value = document.all.EDIT_HTML.ExecCommand(DECMD_GETFONTSIZE, OLECMDEXECOPT_DODEFAULT);
  }
}

// which button is clicked
function doEditHTML(para)
{
	switch (para)
	{
	case 1:
		history.back();
		break;
	case 2:
		history.back();
		break;
	case 3:
		//document.all.form1.CONTENT.value = escape(document.all.edit1.DocumentHTML);
		break;
	case 4:
		DECMD_UNDO_onclick();
		break;	
	case 5:
		DECMD_REDO_onclick();
		break;
	case 6:
		DECMD_FINDTEXT_onclick();
		break;
	case 7:
		MENU_FILE_SAVEAS_onclick();
		break;	
	case 8:
		break;
	case 9:
		DECMD_CUT_onclick();
		break;		
	case 10:
		DECMD_COPY_onclick();
		break;
	case 11:
		DECMD_PASTE_onclick();
		break;
	case 12:
		MENU_FILE_IMPORT_onclick();
	    break;
	case 13:
		MENU_FILE_EXPORT_onclick();
        break;
	case 15:
		EDITHTML.EDIT_HTML.PrintDocument(true);
        break;
		
	case 21:
		ParagraphStyle_onchange();
		break;
	case 22:
		FontName_onchange();
		break;
	case 23:
		FontSize_onchange();
		break;
	case 24:
		DECMD_BOLD_onclick();
		break;	
	case 25:
		DECMD_ITALIC_onclick();
		break;	
	case 26:
		DECMD_UNDERLINE_onclick();
		break;
		
	case 31:
		DECMD_JUSTIFYLEFT_onclick();
		break;
	case 32:
		DECMD_JUSTIFYCENTER_onclick();
		break;
	case 33:
		DECMD_JUSTIFYRIGHT_onclick();
		break;
	case 34:
		DECMD_UNORDERLIST_onclick();
		break;					
	case 35:
		DECMD_ORDERLIST_onclick();
		break;
	case 36:
		DECMD_INDENT_onclick();
		break;
	case 37:
		DECMD_OUTDENT_onclick();
		break;
	case 38:
		DECMD_SETFORECOLOR_onclick();
		break;
    case 39:
		DECMD_SETBACKCOLOR_onclick();
		break;
	case 40:
		InsertTable();
		break;			
   	case 41:
		DECMD_HYPERLINK_onclick();
		break;		
   	case 42:
		DECMD_IMAGE_onclick();
		break;		
    case 43:
        openwin('edit_html_piclist.html','piclist',450,550);
        break;
 	default:
		alert("Sorry, leider kann die Funktion nicht ausgeführt werden.");			
	}	
}


<!-- Includes the Document Source-Code into the HTML-Editor and sets up the contect menue-->
function setText()
{
  //
  EDITHTML.EDIT_HTML.focus();
}

// Submitts the Document to the MhtCms System
function doSubmit() 
{
	document.all.EDIT_HTML.CONTENT.value = escape(document.all.EDIT_HTML.DocumentHTML);
}


// Main Function to access HTML-Editor functions.

function DECMD_UNDO_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_UNDO,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_REDO_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_REDO,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_FINDTEXT_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_FINDTEXT,OLECMDEXECOPT_PROMPTUSER);
  EDITHTML.EDIT_HTML.focus();
}

function DECMD_CUT_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_CUT,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}

function DECMD_COPY_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_COPY,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_PASTE_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_PASTE,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function MENU_FILE_IMPORT_onclick()
{
  docComplete = false;
  EDITHTML.EDIT_HTML.LoadDocument("", true);
  EDITHTML.EDIT_HTML.focus();
}
function MENU_FILE_EXPORT_onclick()
{
  if (EDITHTML.EDIT_HTML.IsDirty) {
    if (EDITHTML.EDIT_HTML.CurrentDocumentPath != "") {
      var path;
      
      path = EDITHTML.EDIT_HTML.CurrentDocumentPath;
      if (path.substring(0, 7) == "http://")
        EDITHTML.EDIT_HTML.SaveDocument("", true);
      else
        EDITHTML.EDIT_HTML.SaveDocument(EDIT_HTML.CurrentDocumentPath, false);
    } else {
      EDITHTML.EDIT_HTML.SaveDocument("", true);
    }
  }
  EDITHTML.EDIT_HTML.focus();
}
function MENU_FILE_SAVEAS_onclick()
{
  EDITHTML.EDIT_HTML.SaveDocument("", true);
  EDITHTML.EDIT_HTML.focus();
}
//=======================================================
function ParagraphStyle_onchange() 
{	 
  document.all.EDIT_HTML.ExecCommand(DECMD_SETBLOCKFMT, OLECMDEXECOPT_DODEFAULT, document.all.BLOCK.value);
  document.all.EDIT_HTML.focus();
}
function FontName_onchange()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_SETFONTNAME, OLECMDEXECOPT_DODEFAULT, EDITHTML.FONTFACE.value);
  EDITHTML.EDIT_HTML.focus();
}
function FontSize_onchange()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_SETFONTSIZE, OLECMDEXECOPT_DODEFAULT, parseInt(EDITHTML.FONTSIZE.value));
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_BOLD_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_BOLD,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_ITALIC_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_ITALIC,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_UNDERLINE_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_UNDERLINE,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
//=======================================================
function DECMD_JUSTIFYLEFT_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_JUSTIFYLEFT,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}

function DECMD_JUSTIFYCENTER_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_JUSTIFYCENTER,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_JUSTIFYRIGHT_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_JUSTIFYRIGHT,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_UNORDERLIST_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_UNORDERLIST,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_ORDERLIST_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_ORDERLIST,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_INDENT_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_INDENT,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_OUTDENT_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_OUTDENT,OLECMDEXECOPT_DODEFAULT);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_SETFORECOLOR_onclick()
{
  var arr = showModalDialog( "selcolor.htm",
                             "",
                             "font-family:Verdana; font-size:12; dialogWidth:30em; dialogHeight:30em" );

  if (arr != null)
  {
    EDITHTML.EDIT_HTML.ExecCommand(DECMD_SETFORECOLOR,OLECMDEXECOPT_DODEFAULT, arr);
  }
}

function DECMD_SETBACKCOLOR_onclick()
{
  var arr = showModalDialog( "selcolor.htm",
                             "",
                             "font-family:Verdana; font-size:12; dialogWidth:30em; dialogHeight:30em" );

  if (arr != null)
  {
    EDITHTML.EDIT_HTML.ExecCommand(DECMD_SETBACKCOLOR,OLECMDEXECOPT_DODEFAULT, arr);
  }
  EDITHTML.EDIT_HTML.focus();
}
function InsertTable()
{
  var pVar = ObjTableInfo;
  var args = new Array();
  var arr = null;
     
  // Display table information dialog
  args["NumRows"] = ObjTableInfo.NumRows;
  args["NumCols"] = ObjTableInfo.NumCols;
  args["TableAttrs"] = ObjTableInfo.TableAttrs;
  args["CellAttrs"] = ObjTableInfo.CellAttrs;
  args["Caption"] = ObjTableInfo.Caption;
  
  arr = null;
    
  arr = showModalDialog( "instable.htm",
                             args,
                             "font-family:Verdana; font-size:12; dialogWidth:34em; dialogHeight:25em");
  if (arr != null)
  {
  
    // Initialize table object
    for ( elem in arr ) {
      if ("NumRows" == elem && arr["NumRows"] != null) {
        ObjTableInfo.NumRows = arr["NumRows"];
      } else if ("NumCols" == elem && arr["NumCols"] != null) {
        ObjTableInfo.NumCols = arr["NumCols"];
      } else if ("TableAttrs" == elem) {
        ObjTableInfo.TableAttrs = arr["TableAttrs"];
      } else if ("CellAttrs" == elem) {
        ObjTableInfo.CellAttrs = arr["CellAttrs"];
      } else if ("Caption" == elem) {
        ObjTableInfo.Caption = arr["Caption"];
      }
    }
    
    EDITHTML.EDIT_HTML.ExecCommand(DECMD_INSERTTABLE,OLECMDEXECOPT_DODEFAULT, pVar);  
  }
}
function DECMD_HYPERLINK_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_HYPERLINK,OLECMDEXECOPT_PROMPTUSER);
  EDITHTML.EDIT_HTML.focus();
}
function DECMD_IMAGE_onclick()
{
  EDITHTML.EDIT_HTML.ExecCommand(DECMD_IMAGE,OLECMDEXECOPT_PROMPTUSER);
  EDITHTML.EDIT_HTML.focus();
}







