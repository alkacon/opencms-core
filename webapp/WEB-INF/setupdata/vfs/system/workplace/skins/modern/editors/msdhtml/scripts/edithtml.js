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

// Script for MS DHTML editor
var binlist=null;

// Command IDs for the MS DHTML editor
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

// Define Arrays for the context menue
var MENU_SEPARATOR = "";
var ContextMenu = new Array();
var GeneralContextMenu = new Array();
var TableContextMenu = new Array();
var AbsPosContextMenu = new Array();

// Variables for the link editor
var linkEditor = null;
var linkEditorRange = null;
var linkEditorSelection = null;
var linkEditorStyleInputs = null;


// Constructor for custom object that represents an item on the context menu
function ContextMenuItem(string, cmdId) {
	this.string = string;
	this.cmdId = cmdId;
}

// Displays the context menue
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


function ContextMenuAction(itemIndex) {
	if (ContextMenu[itemIndex].cmdId == DECMD_INSERTTABLE) {
		InsertTable();
	} else {
		document.all.EDIT_HTML.ExecCommand(ContextMenu[itemIndex].cmdId, OLECMDEXECOPT_DODEFAULT);
	}
}


function DisplayChanged() {
	var s = document.all.EDIT_HTML.QueryStatus(DECMD_GETBLOCKFMT);
	if (s == DECMDF_DISABLED || s == DECMDF_NOTSUPPORTED) {
		document.all.BLOCK.disabled = true;
	} else {
		document.all.BLOCK.disabled = false;
		document.all.BLOCK.value = document.all.EDIT_HTML.ExecCommand(DECMD_GETBLOCKFMT, OLECMDEXECOPT_DODEFAULT);
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


function checkPopup() {
	if (openWindow && focusCount > 0) {
		openWindow.focus();
		focusCount = 0;
	}
}


function saveContent() {
	var _form = document.EDITOR;
	var _editor = _form.EDIT_HTML;
	if (_editor.DOM.documentElement) {
		// IE5
		_form.content.value = encodeURIComponent(_editor.filterSourceCode(_editor.DocumentHTML));
	} else {
		// IE4
		_form.content.value = encodeURIComponent(_editor.DocumentHTML);
	}
}


function doEditHTML(para) {
	var _form = document.EDITOR;
	var _editor = _form.EDIT_HTML;

	switch (para) {	
	case 1:
		// exit
		_form.action.value = actionExit;
		_form.target = "_top";
		saveContent();
		_form.submit();
		break;
	case 2:
		// save & exit
	    _form.action.value = actionSaveExit;
	    _form.target = "_top";
	    saveContent();
	    _form.submit();
		break;
	case 3:
		// exit
	    _form.action.value = actionSave;
	    _form.target = "_self";
	    saveContent();
	    _form.submit();
		break;
	case 55:
		// save and reload top
	    _form.action.value = actionSaveAction;
	    _form.target = "_top";
	    saveContent();
	    _form.submit();
		break;
	case 4:
		// undo
		_editor.ExecCommand(DECMD_UNDO);
		break;
	case 5:
		// redo
		_editor.ExecCommand(DECMD_REDO);
		break;
	case 6:
		// search
		_editor.ExecCommand(DECMD_FINDTEXT, 1);
		break;
	case 9:
		// cut
		_editor.ExecCommand(DECMD_CUT);
		break;
	case 10:
		// copy
		_editor.ExecCommand(DECMD_COPY);
		break;
	case 11:
		// paste
		_editor.ExecCommand(DECMD_PASTE);
		break;
	case 21:
		// font style selection <h1>, <h2>...
		_editor.ExecCommand(DECMD_SETBLOCKFMT, 0, _form.BLOCK.value);
		break;
	case 22:
		// font name selection <font name="...">
		_editor.ExecCommand(DECMD_SETFONTNAME, 0, _form.FONTFACE.value);
		break;
	case 23:
		// font size selection
		_editor.ExecCommand(DECMD_SETFONTSIZE, 0, parseInt(_form.FONTSIZE.value));
		break;
	case 24:
		// bold
		_editor.ExecCommand(DECMD_BOLD);
		break;
	case 25:
		// italic
		_editor.ExecCommand(DECMD_ITALIC);
		break;
	case 26:
		// underline
		_editor.ExecCommand(DECMD_UNDERLINE);
		break;
	case 31:
		// left align
		_editor.ExecCommand(DECMD_JUSTIFYLEFT);
		break;
	case 32:
		// center align
		_editor.ExecCommand(DECMD_JUSTIFYCENTER);
		break;
	case 33:
		// right align
		_editor.ExecCommand(DECMD_JUSTIFYRIGHT);
		break;
	case 34:
		// <ul>
		_editor.ExecCommand(DECMD_UNORDERLIST);
		break;
	case 35:
		// <ol>
		_editor.ExecCommand(DECMD_ORDERLIST);
		break;
	case 36:
		// indent
		_editor.ExecCommand(DECMD_INDENT);
		break;
	case 37:
		// outdent
		_editor.ExecCommand(DECMD_OUTDENT);
		break;
	case 38:
		ColorSelected = -1;
		SelColor = -1;
		CheckFGCol= window.setInterval("setFGColor(SelColor)",500);
		SelColor = showModalDialog(skinUri + "components/js_colorpicker/index.html", colorPicker, "resizable: no; help: no; status: no; scroll: no;");
		if (SelColor != null) {
			ColorSelected = 1;
		}
		break;
	case 39:
		ColorSelected=-1;
		SelColor=-1;
		CheckBGCol= window.setInterval("setBGColor(SelColor)",500);
		SelColor = showModalDialog(skinUri + "components/js_colorpicker/index.html", colorPicker, "resizable: no; help: no; status: no; scroll: no;");
		if (SelColor != null) {
			ColorSelected = 1;
		}		
		break;
	case 40:
		checkTableSelection();
		break;
	case 41:
		link = window.open(workplacePath + 'action/edit_html_link.html','SetLink', "width=450, height=300, resizable=no,status=no, top=300, left=250");
		break;
	case 42:
		_editor.ExecCommand(DECMD_IMAGE, 1);
		makeImageLinks();
		break;
	case 43:
		openWindow = window.open(workplacePath + "action/picturebrowser.html?initial=true", "PicBrowser", "width=550, height=500, resizable=yes, top=200, left=450");
		focusCount = 1;
		openWindow.focus();
		break;
	case 44:
		openWindow = window.open(workplacePath + 'action/downloadbrowser.html?initial=true','DownBrowser', "width=550, height=500, resizable=yes, top=200, left=450");
		focusCount = 1;
		openWindow.focus();
		break;
	case 45:
		_editor.ExecCommand(DECMD_HYPERLINK, 0, EDITOR.URL.value);
		break;
	case 46:
		vfslink = window.open(workplacePath + 'action/edit_html_vfslink.html','SetLink', "width=450, height=300, resizable=no, top=300, left=250");
		break;
	case 47:
		_editor.showDetails = !_editor.showDetails;
		break;
	case 48:
		openWindow = window.open("dialogs/specialchars.html","characters", "width=450, height=300, resizable=no, scrollbars=no, location=no, menubar=no, toolbar=no,dependent=yes, top=300, left=250");
		focusCount = 1;
		openWindow.focus();
		break;
	case 49:
		// hyperlink (internal function)
		_editor.ExecCommand(DECMD_HYPERLINK, 1);
		break;
	case 50:
		var winheight = (USE_LINKSTYLEINPUTS?220:170);
		linkEditor = EDITOR.EDIT_HTML;
		linkEditorAll = EDITOR.EDIT_HTML.DOM.all.tags("A");
		linkEditorRange = EDITOR.EDIT_HTML.DOM.body.createTextRange();
		linkEditorSelection = EDITOR.EDIT_HTML.DOM.selection;
		linkEditorStyleInputs = USE_LINKSTYLEINPUTS;
		openWindow = window.open('dialogs/link.html','SetLink', "width=480, height=" + winheight + ", resizable=no, top=300, left=250");
		focusCount = 1;
		openWindow.focus();
		break;
	case 51:
		checkTableElSelection("TR");
		break;
	case 52:
		checkTableElSelection("TD");
		break;
	case 53:
		var winheight = (USE_LINKSTYLEINPUTS?180:130);
		linkEditor = EDITOR.EDIT_HTML;
		linkEditorAll = EDITOR.EDIT_HTML.DOM.all.tags("A");
		linkEditorRange = EDITOR.EDIT_HTML.DOM.body.createTextRange();
		linkEditorSelection = EDITOR.EDIT_HTML.DOM.selection;
		linkEditorStyleInputs = USE_LINKSTYLEINPUTS;
		openWindow  = window.open('dialogs/anchor.html','SetAnchor', "width=350, height=" + winheight + ", resizable=no, top=300, left=250");
		focusCount = 1;
		openWindow.focus();
		break;
	case 56:
		openWindow = window.open(workplacePath + 'action/linkbrowser.html?initial=true', 'LinkBrowser', "width=550, height=500, resizable=no, status=yes, top=300, left=250");
		focusCount = 1;
		openWindow.focus();
		break;
	case 57:
		openWindow = window.open(workplacePath + 'action/htmlbrowser.html?initial=true', 'HtmlBrowser', "width=550, height=500, resizable=no, status=yes, top=300, left=250");
		focusCount = 1;
		openWindow.focus();
		break;
	default:
		alert("Sorry, the requested function " + para + " is not implemented.");
	}
}

// Includes the document source into the HTML editor and sets up the context menue
function initContent() {
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

// Sets the foreground color with the data received by the "selcolor" dialog
function setFGColor(arr) {
	if (arr != -1) {
		if (document.all.EDIT_HTML.QueryStatus( DECMD_GETFORECOLOR ) != DECMDF_DISABLED) {
			document.all.EDIT_HTML.ExecCommand(DECMD_SETFORECOLOR, OLECMDEXECOPT_DODEFAULT, arr);
		}
		window.clearInterval(CheckFGCol);
		SelColor=-1;
	}
}

// Sets the background color with the data received by the "selcolor" dialog
function setBGColor(arr) {
	if (arr != -1) {
		if (document.all.EDIT_HTML.QueryStatus( DECMD_SETBACKCOLOR )  != DECMDF_DISABLED ) {
			document.all.EDIT_HTML.ExecCommand(DECMD_SETBACKCOLOR, OLECMDEXECOPT_DODEFAULT, arr);
		}
		window.clearInterval(CheckBGCol);
		SelColor=-1;
	}
}


// Checks if a table element is selected
function checkTableSelection() {
	var editor = document.all.EDIT_HTML;
	var sel = editor.DOM.selection;

	if(sel.type == "Control") {
		var range = sel.createRange()(0);

		// we have selected a table object
		if(range.tagName == "TABLE" || range.tagName == "table") {

			// get table properties
			var args1 = new Array();
	
			if (range.border != "" && range.border.length > 0) {
				args1["BorderLineWidth"] = range.border;
			}
	
			if (range.cellPadding != "" && range.cellPadding.length > 0) {
				args1["CellPadding"] = range.cellPadding;
			}
	
			if (range.cellSpacing != "" && range.cellSpacing.length > 0) {
				args1["CellSpacing"] = range.cellSpacing;
			}
	
			if (range.bgColor != "" && range.bgColor.length > 0) {
				args1["TableColor"] = range.bgColor;
			}
	
			//get new attributes
			var args2 = new Array();
			args2 = showModalDialog("dialogs/table_new.html?titleType=edit", args1, "dialogWidth:550px; dialogHeight:270px; resizable: no; help: no; status: no; scroll: no;");
	
			// set the new attributes
			if (args2 != null) {
				for ( elem in args2 ) {
					if ("BorderLineWidth" == elem && args2["BorderLineWidth"] != null) {
						range.border = args2["BorderLineWidth"];
					} else if ("CellPadding" == elem && args2["CellPadding"] != null) {
						range.cellPadding = args2["CellPadding"];
					} else if ("CellSpacing" == elem && args2["CellSpacing"] != null) {
						range.cellSpacing = args2["CellSpacing"];
					} else if ("TableColor" == elem && args2["TableColor"] != null) {
						range.bgColor = args2["TableColor"];
					}
				}
			}
		}
	} else {
		InsertTable();
	}
}

// Checks if a table row or cell element is selected 
function checkTableElSelection(type) {
	
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
			Elt = elt.parentElement;
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
				args2 = showModalDialog("dialogs/table_element.html?titleType=" + type, args1, "dialogWidth:450px; dialogHeight:270px; resizable: no; help: no; status: no; scroll: no;");

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
							Elt.setAttribute("bgColor", args2["bgColor"]);
						} else if ("borderColor" == elem && args2["borderColor"] != null) {
							Elt.borderColor = args2["borderColor"];
						} else if ("height" == elem && args2["height"] != null) {
							Elt.height = args2["height"];
						} else if ("width" == elem && args2["width"] != null) {
							Elt.width = args2["width"];
						} else if ("align" == elem && args2["align"] != null) {
							Elt.align = args2["align"];
						} else if ("vAlign" == elem && args2["vAlign"] != null) {
							Elt.vAlign = args2["vAlign"];
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

//  Inserts a new table
function InsertTable() {
	var pVar = document.all.ObjTableInfo;
	var args = new Array();
	var arr = null;
	
	document.all.ObjTableInfo.TableAttrs =" ";
	document.all.ObjTableInfo.CellAttrs =" ";

	// Preset values for the table dialog. 
	// Data is stored in an array that is submitted to the dialog.
	args["NumRows"] = document.all.ObjTableInfo.NumRows;
	args["NumCols"] = document.all.ObjTableInfo.NumCols;
	args["TableAttrs"] =document.all.ObjTableInfo.TableAttrs;
	args["CellAttrs"] = document.all.ObjTableInfo.CellAttrs;
	args["Caption"] = document.all.ObjTableInfo.Caption;
	args["BorderLineWidth"] = 1;
	args["CellSpacing"] = 1;
	args["CellPadding"] = 1;
	args["TableAlignment"] = "";
	args["TableWidth"] = 100;
	args["TableHeight"] = 100;
	args["TableWidthMode"] = "";
	args["TableHeightMode"] = "";

	arr = null;

	// Call the "addtable" dialog and receive its results in the arr array
	arr = showModalDialog("dialogs/table_new.html", args, "dialogWidth:550px; dialogHeight:270px; resizable: no; help: no; status: no; scroll: no;");
	if (arr != null) {

	// Initialize table object. Values from the arr array are processed for creating the Control call

	for ( elem in arr ) {
		if ("NumRows" == elem && arr["NumRows"] != null) {
			document.all.ObjTableInfo.NumRows = arr["NumRows"];
		} else if ("NumCols" == elem && arr["NumCols"] != null) {
			document.all.ObjTableInfo.NumCols = arr["NumCols"];
		} else if ("BorderLineWidth" == elem) {
			document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "border="+arr["BorderLineWidth"]+" ";
		} else if ("CellSpacing" == elem) {
			document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "cellspacing="+arr["CellSpacing"]+" ";
		} else if ("CellPadding" == elem) {
			document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "cellpadding="+arr["CellPadding"]+" ";
		} else if ("TableWidth" == elem) {
			if(arr["TableWidth"] != "") {
				document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "width="+arr["TableWidth"];
				if(arr["TableWidthMode"] == "%") {
					document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +"% "
				} else {
					document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +" "
				}
			}
		} else if ("TableHeight" == elem) {
			if(arr["TableHeight"] == "") {
				document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "height="+arr["TableHeight"];
				if(arr["TableHeightMode"] == "%") {
					document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +"% "
				} else {
					document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs +" "
				}
			}
		} else if ("TableAlignment" == elem) {
			document.all.ObjTableInfo.CellAttrs = document.all.ObjTableInfo.CellAttrs + "align="+arr["TableAlignment"]+" ";
		} else if ("TableColor" == elem) {
			if(arr["TableColor"] != "") {
				document.all.ObjTableInfo.TableAttrs = document.all.ObjTableInfo.TableAttrs + "bgcolor="+arr["TableColor"];
			}
		} else if ("Caption" == elem) {
			document.all.ObjTableInfo.Caption = arr["Caption"];
		}
	}

	document.all.EDIT_HTML.ExecCommand(DECMD_INSERTTABLE, OLECMDEXECOPT_DODEFAULT, pVar);
	}
}


// sends URL string from seperate browser window to a hidden field within the opener document
function sendURLString(destFormName,destFieldName,strURL) {
	var obj1='top.window.opener.self.document.'+ destFormName;
	var obj2='top.window.opener.self.document.'+ destFormName +'.'+ destFieldName;
	if (eval(obj1) && eval(obj2)) {
		Eval(obj2 +'.value="'+strURL+'"');
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

// Delete all empty <a>-Tags
function deleteEmptyATags() {
	var allLinks = EDITOR.EDIT_HTML.DOM.all.tags("A");
	var allImgLinks;

	for(var i = 0; i < allLinks.length; i++) {
		if (allLinks[i].innerText == "") {
			allImgLinks = allLinks[i].all.tags("IMG"); 
			if (allImgLinks.length == 0) {
				allLinks[i].removeNode();
			}
		}
	}
}

// Remove server name from image path
// Example: http://10.0.0.0:8080/system/test -> /system/test
function makeImageLinks() {
	var systemPath = getSystemPath();
	var col = document.EDIT_HTML.DOM.all.tags("img");
	var i;
	for (i=0; i<col.length; i++) {
		var el = col[i];
		var href = el.getAttribute("src");
		href = href.replace(systemPath, "");
		El.setAttribute("src", href);
		// el.removeAttribute("style");
	}
}

// Get the server name of the page
function getSystemPath() {
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