/*
* File   : $Source: $
* Date   : $Date:  $
* Version: $Revision: $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
 
//------------------------------------------------------//
// Script for html editcontrol
//------------------------------------------------------//
 
var binlist=null;

// Definition of constants, which button is clicked
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;
var SAVEACTION=55;

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

<!-- Define Arrays for the context menue -->

var MENU_SEPARATOR = ""; 
var ContextMenu = new Array();
var GeneralContextMenu = new Array();
var TableContextMenu = new Array();
var AbsPosContextMenu = new Array();

<!--  Constructor for custom object that represents an item on the context menu -->

function ContextMenuItem(string, cmdId) {
  this.string = string;
  this.cmdId = cmdId;
}

<!-- Displays the Context menue, taken from the MS example editor -->

function ShowContextMenu() {
  var menuStrings = new Array();
  var menuStates = new Array();
  var state;
  var i
  var idx = 0;

  // Rebuild the context menu. 
  ContextMenu.length = 0;

  // Always show general menu
  for (i=0; i<GeneralContextMenu.length; i++) {
    ContextMenu[idx++] = GeneralContextMenu[i];
  }

  // Is the selection inside a table? Add table menu if so
  if (document.all.EDIT_HTML.QueryStatus(DECMD_INSERTROW) != DECMDF_DISABLED) {
    for (i=0; i<TableContextMenu.length; i++) {
      ContextMenu[idx++] = TableContextMenu[i];
    }
  }

   // Set up the actual arrays that get passed to SetContextMenu
  for (i=0; i<ContextMenu.length; i++) {
    menuStrings[i] = ContextMenu[i].string;
    if (menuStrings[i] != MENU_SEPARATOR) {
      state = document.all.EDIT_HTML.QueryStatus(ContextMenu[i].cmdId);
    } else {
      state = DECMDF_ENABLED;
    }
    if (state == DECMDF_DISABLED || state == DECMDF_NOTSUPPORTED) {
      menuStates[i] = OLE_TRISTATE_GRAY;
    } else if (state == DECMDF_ENABLED || state == DECMDF_NINCHED) {
      menuStates[i] = OLE_TRISTATE_UNCHECKED;
    } else { // DECMDF_LATCHED
      menuStates[i] = OLE_TRISTATE_CHECKED;
    }
  }
    // Set the context menu
  document.all.EDIT_HTML.SetContextMenu(menuStrings, menuStates);
}

<!-- Do the Action when a Context menu entry is selected. Taken from the MS example editor -->

function ContextMenuAction(itemIndex) {
  
  if (ContextMenu[itemIndex].cmdId == DECMD_INSERTTABLE) {
    InsertTable();
  } else {
    document.all.EDIT_HTML.ExecCommand(ContextMenu[itemIndex].cmdId, OLECMDEXECOPT_DODEFAULT);
  }
}

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
    var value = document.all.EDIT_HTML.ExecCommand(DECMD_GETFONTNAME, OLECMDEXECOPT_DODEFAULT);
    if ((value != null) && (USE_FONTFACE == true)) {
	    document.all.FONTFACE.disabled = false;  
    	document.all.FONTFACE.value = value;
     } else {
	    document.all.FONTFACE.disabled = true;  
     }    
  }
  if (s == DECMDF_DISABLED || s == DECMDF_NOTSUPPORTED) {
    document.all.FONTSIZE.disabled = true;
  } else {
    var value = document.all.EDIT_HTML.ExecCommand(DECMD_GETFONTSIZE, OLECMDEXECOPT_DODEFAULT);
    if ((value != null) && (USE_FONTSIZE == true)) {
	    document.all.FONTSIZE.disabled = false;  
    	document.all.FONTSIZE.value = value;
     } else {
	    document.all.FONTSIZE.disabled = true;  
     }
  }
  
  if(document.activeElement != EDITOR.EDIT_HTML) {
    EDITOR.EDIT_HTML.focus();
  }
}

var linkEditor = null;
var linkEditorRange = null;
var linkEditorSelection = null;
var linkEditorStyleInputs = null;

// which button is clicked
function doEditHTML(para)
{
    switch (para)
    {
    case CLOSE:
        document.EDITOR.action.value = "exit";      
        document.EDITOR.target = "_top";        
        doSubmit();
        document.EDITOR.submit();
        break;
    case SAVECLOSE:
        document.EDITOR.action.value = "saveexit";
        document.EDITOR.target = "_top";        
        doSubmit();
        document.EDITOR.submit();
        break;
    case SAVEACTION:
        document.EDITOR.action.value = "saveaction";
        document.EDITOR.target = "_top";        
        doSubmit();
        document.EDITOR.submit();
        break;
    case SAVE:
        document.EDITOR.action.value = "save";
        document.EDITOR.target = "_self";
        doSubmit();
        document.EDITOR.submit();
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
    case 14:
        MENU_FILE_SAVEAS_onclick();
        break;      
    case 15:
        EDITOR.EDIT_HTML.PrintDocument(true);
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
        ColorSelected=-1;
        SelColor=-1;
        CheckFGCol= window.setInterval("setFGColor(SelColor)",500);
        var SelColorWindow= window.open(workplacePath + 'action/edit_html_selcolor.html',"SelColor","width=500,height=400,resizable=no,top=200,left=450");
        SelColorWindow.opener = self;
        break;
    case 39:
        ColorSelected=-1;
        SelColor=-1;
        CheckBGCol= window.setInterval("setBGColor(SelColor)",500);
        var SelColorWindow= window.open(workplacePath + 'action/edit_html_selcolor.html',"SelColor","width=500,height=400,resizable=no,top=200,left=450");
        SelColorWindow.opener = self;
        break;
    case 40:
        checkTableSelection();
        break;          
    case 41:
        link = window.open(workplacePath + 'action/edit_html_link.html','SetLink', "width=450, height=300, resizable=no,status=no, top=300, left=250");
        break;      
    case 42:
        DECMD_IMAGE_onclick();
        break;      
    case 43:
        window.open(workplacePath + "action/picturebrowser.html?initial=true", "PicBrowser", "width=550, height=500, resizable=yes, top=200, left=450");
        break;
    case 44: 
        binlist = window.open(workplacePath + 'action/downloadbrowser.html?initial=true','DownBrowser', "width=550, height=500, resizable=yes, top=200, left=450");
        binlist.focus(); 
        break;
    case 45:
        DECMD_HYPERLINK_NODIALOG_onclick();
        break;
    case 46:
        vfslink = window.open(workplacePath + 'action/edit_html_vfslink.html','SetLink', "width=450, height=300, resizable=no, top=300, left=250");
        break;  
    case 47:
        EDITOR.EDIT_HTML.showDetails = !EDITOR.EDIT_HTML.showDetails; 
        break;   
    case 48:
        specchar = window.open(workplacePath + 'action/edit_html_chars.html','characters', "width=450, height=300, resizable=no, status=yes, top=300, left=250");
        specchar.focus();
        break;     
    case 49:    
        DECMD_HYPERLINK_onclick();
        break;                
    case 50:
        var winheight = (USE_LINKSTYLEINPUTS?240:190);
        linkEditor = EDITOR.EDIT_HTML;
        linkEditorAll = EDITOR.EDIT_HTML.DOM.all.tags("A"); 
        linkEditorRange = EDITOR.EDIT_HTML.DOM.body.createTextRange();
        linkEditorSelection = EDITOR.EDIT_HTML.DOM.selection;
        linkEditorStyleInputs = USE_LINKSTYLEINPUTS;
        linkwin = window.open('dialogs/link.html','SetLink', "width=480, height=" + winheight + ", resizable=no, top=300, left=250");        
        break;      	         
    case 51:
        checkTableElSelection("TR");
        break;          
    case 52:
        checkTableElSelection("TD");
        break;          
    default:
        alert("Sorry, the requested function code " + para + " is not implemented.");          
    }   
}
    
<!-- Includes the Document Source-Code into the HTML-Editor and sets up the contect menue-->
function setText()
{
    document.EDITOR.EDIT_HTML.SourceCodePreservation = true;
    document.EDITOR.EDIT_HTML.DocumentHTML = decodeURIComponent(text);
    GeneralContextMenu[0] = new ContextMenuItem(LANG_CUT, DECMD_CUT);
    GeneralContextMenu[1] = new ContextMenuItem(LANG_COPY, DECMD_COPY);
    GeneralContextMenu[2] = new ContextMenuItem(LANG_PASTE, DECMD_PASTE);
    TableContextMenu[0] = new ContextMenuItem(MENU_SEPARATOR, 0);
    TableContextMenu[1] = new ContextMenuItem(LANG_INSERTROW, DECMD_INSERTROW);
    TableContextMenu[2] = new ContextMenuItem(LANG_DELETEROW, DECMD_DELETEROWS);
    TableContextMenu[3] = new ContextMenuItem(MENU_SEPARATOR, 0);
    TableContextMenu[4] = new ContextMenuItem(LANG_INSERTCOL, DECMD_INSERTCOL);
    TableContextMenu[5] = new ContextMenuItem(LANG_DELETECOL, DECMD_DELETECOLS);
    TableContextMenu[6] = new ContextMenuItem(MENU_SEPARATOR, 0);
    TableContextMenu[7] = new ContextMenuItem(LANG_INSERTCELL, DECMD_INSERTCELL);
    TableContextMenu[8] = new ContextMenuItem(LANG_DELETECELL, DECMD_DELETECELLS);
    TableContextMenu[9] = new ContextMenuItem(LANG_MERGECELL, DECMD_MERGECELLS);
    TableContextMenu[10] = new ContextMenuItem(LANG_SPLITCELL, DECMD_SPLITCELL);
    EDITOR.EDIT_HTML.focus();
}

// Submitts the Document to the OpenCms System
function doSubmit() 
{
    if(document.EDITOR.EDIT_HTML.DOM.documentElement) {
        // IE5
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.EDIT_HTML.filterSourceCode(document.EDITOR.EDIT_HTML.DocumentHTML));
    } else {
        // IE4
        document.EDITOR.content.value = encodeURIComponent(document.EDITOR.EDIT_HTML.DocumentHTML);
    }
}



// Main Function to access HTML-Editor functions.

function DECMD_UNDO_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_UNDO,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_REDO_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_REDO,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_FINDTEXT_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_FINDTEXT,OLECMDEXECOPT_PROMPTUSER);
  EDITOR.EDIT_HTML.focus();
}

function DECMD_CUT_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_CUT,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}

function DECMD_COPY_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_COPY,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_PASTE_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_PASTE,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function MENU_FILE_IMPORT_onclick()
{
  docComplete = false;
  EDITOR.EDIT_HTML.LoadDocument("", true);
  EDITOR.EDIT_HTML.focus();
}
function MENU_FILE_EXPORT_onclick()
{
  if (EDITOR.EDIT_HTML.CurrentDocumentPath != "") {
    var path;
    
    path = EDITOR.EDIT_HTML.CurrentDocumentPath;
    if (path.substring(0, 7) == "http://")
      EDITOR.EDIT_HTML.SaveDocument("", true);
    else
      EDITOR.EDIT_HTML.SaveDocument(EDITOR.EDIT_HTML.CurrentDocumentPath, false);
  } else {
    EDITOR.EDIT_HTML.SaveDocument("", true);
  }
  EDITOR.EDIT_HTML.focus();
}
function MENU_FILE_SAVEAS_onclick()
{
  EDITOR.EDIT_HTML.SaveDocument("", true);
  EDITOR.EDIT_HTML.focus();
}
//=======================================================
function ParagraphStyle_onchange() 
{    
  document.EDITOR.EDIT_HTML.ExecCommand(DECMD_SETBLOCKFMT, OLECMDEXECOPT_DODEFAULT, EDITOR.BLOCK.value);
  EDITOR.EDIT_HTML.focus();
}
function FontName_onchange()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_SETFONTNAME, OLECMDEXECOPT_DODEFAULT, EDITOR.FONTFACE.value);
  EDITOR.EDIT_HTML.focus();
}
function FontSize_onchange()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_SETFONTSIZE, OLECMDEXECOPT_DODEFAULT, parseInt(EDITOR.FONTSIZE.value));
  EDITOR.EDIT_HTML.focus();
}
function DECMD_BOLD_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_BOLD,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_ITALIC_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_ITALIC,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_UNDERLINE_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_UNDERLINE,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
//=======================================================
function DECMD_JUSTIFYLEFT_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_JUSTIFYLEFT,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}

function DECMD_JUSTIFYCENTER_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_JUSTIFYCENTER,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_JUSTIFYRIGHT_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_JUSTIFYRIGHT,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_UNORDERLIST_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_UNORDERLIST,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_ORDERLIST_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_ORDERLIST,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_INDENT_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_INDENT,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_OUTDENT_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_OUTDENT,OLECMDEXECOPT_DODEFAULT);
  EDITOR.EDIT_HTML.focus();
}

<!-- Function to set the ForegroundColor with the data received set by the "selcolor" dialog --> 
 
 function setFGColor(arr)
  {
  if (arr != -1) 
    {
     if (document.all.EDIT_HTML.QueryStatus( DECMD_GETFORECOLOR )   != DECMDF_DISABLED)
     {
      document.all.EDIT_HTML.ExecCommand(DECMD_SETFORECOLOR, OLECMDEXECOPT_DODEFAULT, arr);
     }
      window.clearInterval(CheckFGCol);
      SelColor=-1; 
    }
  }
  
<!-- Function to set the BackgroundColor with the data received set by the "selcolor" dialog --> 
 
 function setBGColor(arr)
  {
  if (arr != -1) 
    {
     if (document.all.EDIT_HTML.QueryStatus( DECMD_SETBACKCOLOR )  != DECMDF_DISABLED )
     {
     document.all.EDIT_HTML.ExecCommand(DECMD_SETBACKCOLOR, OLECMDEXECOPT_DODEFAULT, arr);
     }
     window.clearInterval(CheckBGCol);
     SelColor=-1;
    }
  }


function DECMD_SETFORECOLOR_onclick()
{
  var arr = showModalDialog( workplacePath + "action/edit_html_selcolor.html",
                             "",
                             "font-family:Verdana; font-size:12; dialogWidth:30em; dialogHeight:30em" );

  if (arr != null)
  {
    EDITOR.EDIT_HTML.ExecCommand(DECMD_SETFORECOLOR,OLECMDEXECOPT_DODEFAULT, arr);
  }
}

function DECMD_SETBACKCOLOR_onclick()
{
  var arr = showModalDialog( workplacePath + "templates/selcolor.htm",
                             "",
                             "font-family:Verdana; font-size:12; dialogWidth:30em; dialogHeight:30em" );

  if (arr != null)
  {
    EDITOR.EDIT_HTML.ExecCommand(DECMD_SETBACKCOLOR,OLECMDEXECOPT_DODEFAULT, arr);
  }
  EDITOR.EDIT_HTML.focus();
}

/* Checks if an table-element is selected in the DHTML Editor */
function checkTableSelection() {
  var editor = document.all.EDIT_HTML;
  var sel = editor.DOM.selection;
    
  if(sel.type == "Control") {
    var range = sel.createRange()(0);
    
    // we have selected a table object
    if(range.tagName == "TABLE" || range.tagName == "table") {
      
      // get table properties
      var args1 = new Array();      
      args1["border"] = range.border;  
      args1["cellpadding"] = range.cellPadding;  
      args1["cellspacing"] = range.cellSpacing; 
      if(range.bgColor != "" && range.bgColor.length > 0) {
        args1["bgcolor"] = range.bgColor       
      }
      
      //get new attributes
      var args2 = new Array();               
      args2 = showModalDialog( workplacePath + "action/edit_html_changetable.html", args1,"font-family:Verdana; font-size:12; dialogWidth:50em; dialogHeight:25em");
      
      // set the new attributes
      if (args2 != null) {     
        for ( elem in args2 ) {
          if ("border" == elem && args2["border"] != null) {      
            range.border = args2["border"];
          }
          else if ("cellpadding" == elem && args2["cellpadding"] != null) {      
            range.cellPadding = args2["cellpadding"];
          }          
          else if ("cellspacing" == elem && args2["cellspacing"] != null) {      
            range.cellSpacing = args2["cellspacing"];
          }
          else if ("bgcolor" == elem && args2["bgcolor"] != null) {      
            range.bgColor = args2["bgcolor"];
          }                              
        }
      }
    }
  }
  else {
    InsertTable();
  }
  
}

/* Checks if tablerow- or tablecell-element is selected in the DHTML Editor */
function checkTableElSelection(type)
{
  var editor = document.all.EDIT_HTML;
  var sel = editor.DOM.selection;
  var sel2 = null;
  var args1 = new Array();

  cursorPos=sel.createRange();

  // there should be no selection !
  if (sel.type == 'None') {
    var elt = cursorPos.parentElement(); 
	// find next TD or TR
    while (elt) {
      if (elt.tagName == type) {
        break;
      }
      elt = elt.parentElement;
    }

    if (elt) {
      // don't select document area
      if (elt.id != editor.id) {
        // get all attributes
        var eltheight = elt.getAttribute("height", 0);      
        var eltwidth = elt.getAttribute("width", 0);      
        var eltalign = elt.getAttribute("align", 0);      
        var eltvAlign = elt.getAttribute("vAlign", 0);      
        var eltbgColor = elt.getAttribute("bgColor", 0);      
        var eltborderColor = elt.getAttribute("borderColor", 0);      
        // set arguments for dialog
        if(eltbgColor != null && eltbgColor != "undefined" && eltbgColor.length > 0) {
          args1["bgColor"] = eltbgColor;       
        } else {
	      args1["bgColor"] = "";
  	    }
        if(eltborderColor != null && eltborderColor != "undefined" && eltborderColor.length > 0) {
          args1["borderColor"] = eltborderColor;       
        } else {
	      args1["borderColor"] = "";
	    }
        if(eltheight != null && eltheight.length > 0) {
          args1["height"] = eltheight;       
        } else {
	      args1["height"] = "";
  	    }
        if(eltwidth != null && eltwidth.length > 0) {
          args1["width"] = eltwidth;       
        } else {
	      args1["width"] = "";
	    }
        if(eltalign != null && eltalign.length > 0) {
          args1["align"] = eltalign;       
        } else {
	      args1["align"] = "";
	    }
        if(eltvAlign != null && eltvAlign.length > 0) {
          args1["vAlign"] = eltvAlign; 
        } else {
	      args1["vAlign"] = "";
	    }
  	    args1["title"] = type;
	  
		// call dialog
        args2 = showModalDialog( workplacePath + "action/edit_html_changetable_el.html", args1,"font-family:Verdana; font-size:12; dialogWidth:50em; dialogHeight:32em");

		// args == null if cancel button was pressed
        if (args2 != null) {     
		  // clear all attributes	
  	      elt.removeAttribute("bgColor", 0);
  	      elt.removeAttribute("borderColor", 0);
  	      elt.removeAttribute("height", 0);
	      elt.removeAttribute("width", 0);
	      elt.removeAttribute("align", 0);
	      elt.removeAttribute("vAlign", 0);
	      // get values from dialog and set attributes of table element
          for ( elem in args2 ) {
            if ("bgColor" == elem && args2["bgColor"] != null) {
              elt.setAttribute("bgColor", args2["bgColor"]);
            } else if ("borderColor" == elem && args2["borderColor"] != null) {      
              elt.borderColor = args2["borderColor"];
            } else if ("height" == elem && args2["height"] != null) {      
              elt.height = args2["height"];
            } else if ("width" == elem && args2["width"] != null) {      
              elt.width = args2["width"];
            } else if ("align" == elem && args2["align"] != null) {      
                elt.align = args2["align"];
            } else if ("vAlign" == elem && args2["vAlign"] != null) {      
                elt.vAlign = args2["vAlign"];
            }          
          }
        }
      } else {
        // id of found element == id of Editor, so cursor is not inside table
    	args1["error_notable"] = "true";
    	showModalDialog( workplacePath + "action/edit_html_changetable_el.html", args1,"font-family:Verdana; font-size:12; dialogWidth:50em; dialogHeight:32em");
      } 
    } else {
      // no parent found with tag.name == TR or TD
      args1["error_notable"] = "true";
      showModalDialog( workplacePath + "action/edit_html_changetable_el.html", args1,"font-family:Verdana; font-size:12; dialogWidth:50em; dialogHeight:32em");
    }
  } else {
    // text or picture or control selected
    args1["error_selection"] = "true";
    showModalDialog( workplacePath + "action/edit_html_changetable_el.html", args1,"font-family:Verdana; font-size:12; dialogWidth:50em; dialogHeight:32em");
  }
}

/* Builds a new table */
function InsertTable()
{
  var pVar = document.all.ObjTableInfo;
  var args = new Array();
  var arr = null;
  
  document.all.ObjTableInfo.TableAttrs =" ";
  document.all.ObjTableInfo.CellAttrs =" ";
   
<!-- Preset values for the Table Dialog. Data is stored in an array that is submitted to the dialog -->
  
  args["NumRows"] = document.all.ObjTableInfo.NumRows;
  args["NumCols"] = document.all.ObjTableInfo.NumCols;
  args["TableAttrs"] =document.all.ObjTableInfo.TableAttrs;
  args["CellAttrs"] = document.all.ObjTableInfo.CellAttrs;
  args["Caption"] = document.all.ObjTableInfo.Caption;
  args["BorderLineWidth"] = 1;
  args["CellSpacing"] = 1;
  args["CellPadding"] = 1;
  args["TableAlignment"] = "left";
  args["TableWidth"]=100;
  args["TableHeight"]=100;
  args["TableWidthMode"]="%"; 
  args["TableHeightMode"]="%"; 
    
  arr = null; 
  
<!-- Call the "addtable" dialog and receive its results in the arr array -->

  arr = showModalDialog( workplacePath + "action/edit_html_newtable.html",
                          args,
                          "font-family:Verdana; font-size:12; dialogWidth:50em; dialogHeight:40em");
  if (arr != null) 
  { 

<!-- Initialize table object. Values from the arr array are processed for creating the Control call -->
    
    for ( elem in arr ) 
    {
      if ("NumRows" == elem && arr["NumRows"] != null) 
       {
        document.all.ObjTableInfo.NumRows = arr["NumRows"];
       }
      else if ("NumCols" == elem && arr["NumCols"] != null)
       {
        document.all.ObjTableInfo.NumCols = arr["NumCols"];
       }
       else if ("BorderLineWidth" == elem)
       {
        document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "border="+arr["BorderLineWidth"]+" "; 
       } 
       else if ("CellSpacing" == elem)
       {
        document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "cellspacing="+arr["CellSpacing"]+" "; 
       }
       else if ("CellPadding" == elem)
       {
        document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "cellpadding="+arr["CellPadding"]+" "; 
       }
       else if ("TableWidth" == elem)
        {
         if(arr["TableWidthSelected"] == "TRUE")
         {
            document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "width="+arr["TableWidth"]; 
            if(arr["TableWidthMode"] == "%") 
            {
                document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +"% "
            }
            else
            {
                document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +" "
            }
         }
       }
       else if ("TableHeight" == elem) 
        {
         if(arr["TableHeigthSelected"] == "TRUE")
         {
            document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "height="+arr["TableHeight"]; 
            if(arr["TableHeightMode"] == "%") 
            {
                document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +"% "
            }
            else
            {
                document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +" "
            }   
          }      
        }
      else if ("TableAlignment" == elem) 
       {
        document.all.ObjTableInfo.CellAttrs = document.all.ObjTableInfo.CellAttrs + "align="+arr["TableAlignment"]+" "; 
       }
      else if ("TableColor" == elem)
       {
        if(arr["TableColorSelected"] == "TRUE")
        {
         document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "bgcolor="+arr["TableColor"];
        }
       }
      else if ("Caption" == elem)
        {
        document.all.ObjTableInfo.Caption = arr["Caption"];
       }
    }
  
    document.all.EDIT_HTML.ExecCommand(DECMD_INSERTTABLE, OLECMDEXECOPT_DODEFAULT, pVar);  
  }
}







function DECMD_HYPERLINK_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_HYPERLINK,OLECMDEXECOPT_PROMPTUSER);
  EDITOR.EDIT_HTML.focus();
}
function DECMD_IMAGE_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_IMAGE,OLECMDEXECOPT_PROMPTUSER);
  makeImageLinks();
  EDITOR.EDIT_HTML.focus();
}
function DECMD_HYPERLINK_NODIALOG_onclick()
{
  EDITOR.EDIT_HTML.ExecCommand(DECMD_HYPERLINK,OLECMDEXECOPT_DONTPROMPTUSER, EDITOR.URL.value);
  EDITOR.EDIT_HTML.focus();
}

// sends URL string from seperate browser window to a hidden field within the opener document
function sendURLString(destFormName,destFieldName,strURL) {
    var obj1='top.window.opener.self.document.'+ destFormName;
    var obj2='top.window.opener.self.document.'+ destFormName +'.'+ destFieldName;   
    if (eval(obj1) && eval(obj2)) { 
        eval(obj2 +'.value="'+strURL+'"'); 
        top.window.opener.doEditHTML(45); 
    }
}


var foundstyles = new Array();

function setStyles(i, name) {
    foundstyles[i] = name;
}

function resetStyles() {
    var sel = document.all.BLOCK;
    for (i=0; i<foundstyles.length; i++) {
       sel.options[i] = new Option(foundstyles[i], foundstyles[i]);
    }
}

function initStyles() {
    getStyles();
    resetStyles();
}

/***********************************************************/
/* Delete all empty <a>-Tags 
/***********************************************************/
function deleteEmptyATags()
{
   	var allLinks = EDITOR.EDIT_HTML.DOM.all.tags("A"); 
	var allImgLinks;                                            

    for(var i = 0; i < allLinks.length; i++) 
	{
		if (allLinks[i].innerText == "")
		{
			allImgLinks = allLinks[i].all.tags("IMG");
			if (allImgLinks.length == 0)
				allLinks[i].removeNode();
		}
	}
}

/***********************************************************/
/* Replace  absolute Image-Path by relative Path.Example, 
/* http://10.0.0.0:8080/system/test -> /system/test
/***********************************************************/
function makeImageLinks()
{
 	var systemPath = getSystemPath();
       	var col = document.EDIT_HTML.DOM.all.tags("img");
 	var i;
    	for (i=0; i<col.length; i++)
	{
		var el = col[i];
	        var href = el.getAttribute("src");
        	href = href.replace(systemPath, "");
		el.setAttribute("src", href);
		// el.removeAttribute("style");
	}	
}

/***********************************************************/
/* Get the IP-adress of the page
/***********************************************************/
function getSystemPath()
{
	var systemPath="";
	var localURL=document.URL;
	var n;
   
    	n = localURL.indexOf("://", 0);
    	if (n<0) return systemPath;
    	
    	n = localURL.indexOf("/", n+3);
    	if (n<0) n = localURL.length;
    	
    	systemPath = localURL.substring(0, n);
    	return systemPath;
}
