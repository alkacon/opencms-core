/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

//---------------------------------------------------------------------//
// Script for CodeMirror text editor (editor with syntax highlighting)
//---------------------------------------------------------------------//



// function action on button click
function buttonAction(para) {
    var _form = document.EDITOR;
    _form.content.value = encodeURIComponent(editorCodeMirror.getValue());
    var isWp = false;
    try { 
        if (top.document.querySelector(".o-editor-frame")) {
            isWp = true; 
        } else { 
            isWp = false; 
        }
    } catch (e) {} 
    
    switch (para) {
        case 1:
        {
            _form.action.value = actionExit;
            if (isWp) {
                _form.target="_self";
                _form.submit();
                break;
            }
            
            if (!callCloseFunction) {
                _form.target = "_top";
            }
            _form.submit();
            break;
        }
        case 2:
        {
            _form.action.value = actionSaveExit;
            setContentDirty(false);
            if (isWp) {
                _form.target="_self";
                _form.submit();
                break;
            }

            if (!callCloseFunction) {
                _form.target = "_top";
            }
            _form.submit();
            break;
        }
        case 3:
        {
        	setContentDirty(false);
            _form.action.value = actionSave;
            _form.submit();
            break;
        }
        case 4:
        {
            editorCodeMirror.undo();
            break;
        }
        case 5:
        {
            editorCodeMirror.redo();
            break;
        }
        case 6:
        {
            if (currMode == editorMode) {
                currMode = "text/plain";
                modeClass = "norm";
            } else {
                currMode = editorMode;
                modeClass = "push";
            }
            editorCodeMirror.setOption("mode", currMode);
            editorCodeMirror.refresh();
            break;
        }
        case 7:
        {
            if (tabsVisible) {
                updateCSS(".cm-tab:after", "visibility", "hidden");
                tabsClass = "norm";
            } else {
                updateCSS(".cm-tab:after", "visibility", "visible");
                tabsClass = "push";
            }
            tabsVisible = !tabsVisible;
            editorCodeMirror.refresh();
            break;
        }
        default:
        {
            alert("No action defined for this button!");
            break;
        }
    }
}

document.onkeydown = keyDownHandler;

function keyDownHandler(e) {
    // EVENT HANDLER: shortcuts (have to be added to editor JS additionally)
    if (!e) {
        // if the browser did not pass the event information to the
        // function, we will have to obtain it from the event register
        if (window.event) {
            //DOM
            e = window.event;
        } else {
            // total failure, we have no way of referencing the event
            return;
        }
    }

    if (typeof(e.which) == 'number') {
        //NS 4, NS 6+, Mozilla 0.9+, Opera
        key = e.which;
    } else if (typeof(e.keyCode) == 'number') {
        //IE, NS 6+, Mozilla 0.9+
        key = e.keyCode;
    } else if (typeof(e.charCode) == 'number') {
        //also NS 6+, Mozilla 0.9+
        key = e.charCode;
    } else {
        // total failure, we have no way of obtaining the key code
        return;
    }

    if (e.ctrlKey) {
        if (key == 83) {
            // 's' pressed
            if (e.shiftKey == true) {
                // save content and exit
                buttonAction(2);
            } else {
                // save content without exiting
                buttonAction(3);
            }
            return false;
        }
        if (e.shiftKey && key == 88) {
            // 'x' pressed, exit editor
            confirmExit();
            return false;
        }
    }
}

// auto format the selected code
function autoFormatSelection() {
    var range = getSelectedRange();
    alert("From: "+ range.from + ", To: "+range.to);
    editorCodeMirror.autoFormatRange(range.from, range.to);
}

// get the selected code range
function getSelectedRange() {
    return { from: editorCodeMirror.getCursor(true), to: editorCodeMirror.getCursor(false) };
}

// changes the editor syntax highlighting to the given value
function setEditorSyntax(newSyntax) {
    if (newSyntax == "-") {
        return;
    }
    if (newSyntax == "text/html" || newSyntax == "xml") {
        foldFunc = CodeMirror.newFoldFunction(CodeMirror.tagRangeFinder);
    } else {
        foldFunc = CodeMirror.newFoldFunction(CodeMirror.braceRangeFinder);
    }
    editorCodeMirror.setOption("mode", newSyntax);
}

// changes the editor font size to the given value
function setEditorFontSize(newSize) {
    if (newSize == "-") {
        return;
    }
    updateCSS(".CodeMirror span", "font-size", newSize + "px;");
    updateCSS(".CodeMirror pre", "font-size", newSize + "px;");
    updateCSS(".CodeMirror-linenumber", "font-size", newSize + "px;");
    editorCodeMirror.refresh();
}

// updates the editor CSS to allow the change of font sizes
function updateCSS(theClass, element, value) {
  for (var loop = 0; loop < document.styleSheets.length; loop++) {
    if (document.styleSheets[loop].title == 'cssocms') {
        try {
            document.styleSheets[loop].insertRule(theClass + ' { ' + element + ': ' + value + '; }', document.styleSheets[loop][cssRules].length);
        } catch(err) {
            try {
                document.styleSheets[loop].addRule(theClass, element + ': ' + value + ';');
            } catch (err) { 
                try {
                    if (document.styleSheets[loop]['rules']) {
                        cssRules = 'rules';
                    } else if (document.styleSheets[loop]['cssRules']) {
                        cssRules = 'cssRules';
                    } else {
                        // no rules found... browser unknown
                    }
                
                    for (var inner = 0; inner < document.styleSheets[loop][cssRules].length; inner++) {
                        if (document.styleSheets[loop][cssRules][inner].selectorText == theClass) {
                            if(document.styleSheets[loop][cssRules][inner].style[element]) {
                                document.styleSheets[loop][cssRules][inner].style[element] = value;
                                break;
                            }
                        }
                    }
                } catch (err) {}
            }
        }
    }
  }
}