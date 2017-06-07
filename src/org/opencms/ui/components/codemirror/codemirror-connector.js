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
window.org_opencms_ui_components_codemirror_CmsCodeMirror = function() {
	var stylesheetId="codemirrorDynamicStyle";
	var fontSizeRule;
	var tabVisibilityRule;
	var codeMirrorId;
    var codemirror;
    var hintConfig;
    var e = this.getElement();
    var self = this;
    var currentCodeData;
    var foldFuncInt;
    var initializing=false;
    
    this.onStateChange = function() {
        var state = this.getState();
        if (typeof codemirror === 'undefined') {
        	storeData(state);
        	if (!initializing){
            	initializing=true;
            	// set the messages
            	window.lang=JSON.parse(state.m_messages);
            	// load required CSS and scripts
            	window.cmsLoadCSS(state.m_cssURIs)
            	window.cmsLoadScripts(state.m_scriptURIs,function(){
            		initCodeMirror();
            	});
            } 
        }else {
        	modifyCodeMirrorState(state);
        } 
    };
    
    foldFunction = function (){
    	self.foldFuncInt.apply(self, arguments);
    }
    
    modifyCodeMirrorState = function(codeData) {
        if (typeof currentCodeData !== 'undefined') {
            if (currentCodeData.m_contentValue !== codeData.m_contentValue) {
                codemirror.setValue(codeData.m_contentValue);
            }
            if (currentCodeData.m_theme !== codeData.m_theme) {
                codemirror.setOption("theme", codeData.m_theme);
            }
            if (currentCodeData.m_mode !== codeData.m_mode||currentCodeData.m_highlighting !== codeData.m_highlighting) {
            	var mode=codeData.m_highlighting? codeData.m_mode:"text/plain";
                codemirror.setOption("mode", mode);
                if (currentCodeData.m_mode !== codeData.m_mode){
                	updateMode(codeData.m_mode);
                }
            }
            if (currentCodeData.m_width !== codeData.m_width || currentCodeData.m_height !== codeData.m_height) {
            	 codemirror.setSize(codeData.m_width,codeData.m_height);
            }
            
            if (currentCodeData.m_lineWrapping !== codeData.m_lineWrapping) {
                codemirror.setOption("lineWrapping", codeData.m_lineWrapping);
            }
            
            if (currentCodeData.m_closeBrackets !== codeData.m_closeBrackets) {
                codemirror.setOption("autoCloseBrackets", codeData.m_closeBrackets);
            }
            
            if (currentCodeData.m_tabsVisible !== codeData.m_tabsVisible) {
                setTabsVisible(codeData.m_tabsVisible);
            }
            if (currentCodeData.m_fontSize !== codeData.m_fontSize) {
                setFontSize(codeData.m_fontSize);
            }
            if (currentCodeData.m_enableUndoRedo !== codeData.m_enableUndoRedo){
            	// defer registering to make sure HTML elements are already available
            	setTimeout(registerUndoRedo,100);
            }
            if (currentCodeData.m_enableSearchReplace !== codeData.m_enableSearchReplace){
            	// defer registering to make sure HTML elements are already available
            	setTimeout(registerSearchReplace,100);
            }
        }
        storeData(codeData);
    };
    
    setFontSize = function(fontSize){
    	if (typeof fontSizeRule == "undefined"){
    		fontSizeRule=createRule("#"+codeMirrorId+ " .CodeMirror span, #"+codeMirrorId+ " .CodeMirror pre, #"+codeMirrorId+ " .CodeMirror-linenumber");
    	}
    	fontSizeRule.style.fontSize=fontSize;
    }
    
    setTabsVisible = function(visible){
    	if (typeof tabVisibilityRule == "undefined"){
    		tabVisibilityRule=createRule("#"+codeMirrorId+ " .cm-tab:after");
    	}
    	tabVisibilityRule.style.visibility=visible?"visible":"hidden";
    }
    
    createRule = function(selector){
    	var style=window.document.getElementById(stylesheetId);
    	if (style==null){
    		style = window.document.createElement("style");
    		style.setAttribute("id",stylesheetId)
            style.appendChild(window.document.createTextNode(""));
            window.document.head.appendChild(style);
    	}
    	style.sheet.insertRule(selector+"{}",0);
		var rules=style.sheet.cssRules?style.sheet.cssRules:style.sheet.rules;
		return rules[0];
    }
        
    updateMode = function(mode){
    	if (mode == "text/html") {
			hintConfig = "CodeMirror.htmlHint";
		} else {
			hintConfig = "CodeMirror.javascriptHint";
		}
    	
    	foldFuncInt = CodeMirror.newFoldFunction((currentCodeData.m_mode=="text/html"||currentCodeData.m_mode=="application/xml") ? CodeMirror.tagRangeFinder : CodeMirror.braceRangeFinder);
    };
    
    storeData = function(codeData){
    	currentCodeData = JSON.parse(JSON.stringify(codeData));
    }
    
    registerUndoRedo = function(){
    	var undo=window.document.getElementById(codeMirrorId+"-undo");
    	if (undo!=null){
    		undo.addEventListener("click",function(){
    			codemirror.undo();
    		});
    	}
    	
    	var redo=window.document.getElementById(codeMirrorId+"-redo");
    	if (redo!=null){
    		redo.addEventListener("click", function(){
    			codemirror.redo();
    		});
    	}
    }
    
    registerSearchReplace = function(){
    	var search=window.document.getElementById(codeMirrorId+"-search");
    	if (search!=null){
    		search.addEventListener("click",function(){
    			CodeMirror.commands.find(codemirror);
    			insertShortcutInfo();
    		});
    	}
    	
    	var replace=window.document.getElementById(codeMirrorId+"-replace");
    	if (replace!=null){
    		replace.addEventListener("click", function(){
    			CodeMirror.commands.replace(codemirror);
    			insertShortcutInfo();
    		});
    	}
    }
    
    insertShortcutInfo = function(){
    	var dialog = window.document.querySelector("#"+codeMirrorId+" .CodeMirror-dialog");
    	if (dialog!=null){
    		var insert=window.document.createElement("DIV");
    		insert.setAttribute("class", "CodeMirror-shortcutinfo")
    		insert.innerHTML=currentCodeData.m_shortcutsMessage;
    		dialog.insertBefore(insert, dialog.firstChild);
    	}
    }
    
    initCodeMirror = function() {
    	initializing=false;
    	codeMirrorId="cm-addon-"+currentCodeData.m_id;
    	e.innerHTML = e.innerHTML + "<div id='"+codeMirrorId+ "'></div>";
        var mode=currentCodeData.m_highlighting? currentCodeData.m_mode:"text/plain";
        codemirror = CodeMirror(document.getElementById(codeMirrorId), {
        	value: currentCodeData.m_contentValue,
            mode: mode,
            theme: currentCodeData.m_theme,
            autoCloseBrackets: currentCodeData.m_closeBrackets,
            lineWrapping: currentCodeData.m_lineWrapping,
            autofocus : true,
            lineNumbers: true,
            styleActiveLine: true,
			fixedGutter: true,
			indentUnit: 4,
			indentWithTabs: true,
			matchBrackets: true,
			smartIndent: false,
			autoCloseTags: true,
			extraKeys: {
				"Ctrl-Space": function(cm) {
					CodeMirror.showHint(cm, self.hintConfig);
				},
				"Ctrl-F": function(cm) {
					CodeMirror.commands.find(cm);
					insertShortcutInfo();
				},
				"Shift-Ctrl-F": function(cm) {
					CodeMirror.commands.replace(cm);
					insertShortcutInfo();
				},
				"Shift-Ctrl-R": function(cm) {
					CodeMirror.commands.replaceAll(cm);
					insertShortcutInfo();
				},
				"Ctrl-G": function(cm) {
					CodeMirror.commands.findNext(cm);
				},
				"Shift-Ctrl-G": function(cm) {
					CodeMirror.commands.findPrev(cm);
				}				
			}
        });
        setTabsVisible(currentCodeData.m_tabsVisible);
        setFontSize(currentCodeData.m_fontSize)
        updateMode(currentCodeData.m_mode);
        codemirror.setSize(currentCodeData.m_width,currentCodeData.m_height);
        codemirror.on("blur", function() {
            var value = codemirror.getValue();
            self.onBlur(value);
        });
        codemirror.on("change", function() {
            var value = codemirror.getValue();
            self.onChange(value);
        });
        
        // activate fold functionality on gutter click
        codemirror.on("gutterClick", self.foldFunction);        
        
        if (currentCodeData.m_enableUndoRedo){
        	// defer registering to make sure HTML elements are already available
        	setTimeout(registerUndoRedo,100);
        }
        if (currentCodeData.m_enableSearchReplace){
        	// defer registering to make sure HTML elements are already available
        	setTimeout(registerSearchReplace,100);
        }
    };
};

