<%@ page import="org.opencms.workplace.*, org.opencms.workplace.explorer.*" buffer="none" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	wp.setParamIsPopup("true");
	
%><%= wp.htmlStart(null, wp.key("title.newlink")) %>
<script type="text/javascript">
<!--

var linkEditor = null;
var linkEditorAll = null;
var linkEditorRange = null;
var linkEditorSelection = null;
var linkEditorStyleInputs = false;
var linkEditorPrefix = null;

var foundRange = null;
var foundLink = null;

/**
* Extends Javascript String to have a trim() function.
*/
String.prototype.trim = function() {
	// skip leading and trailing whitespace
	return this.replace(/^\s*(\b.*\b|)\s*$/, "$1");
}

/**
* Paste the calculated link to the calling editor.
*/
function pasteLink() {
	var linkurl = document.NEU.neulink.value;
	var linktarget = document.NEU.linktarget.options[document.NEU.linktarget.selectedIndex].value;
	if ("named" == linktarget) {
  		linktarget = document.NEU.targetname.value;
  		if ((linktarget == null) || (linktarget.trim() == "")) {
      			linktarget="_self";
  		}
	}
	
	if (linkEditorStyleInputs) {
  		var linkstyle = document.NEU.linkstyle.value;
  		var linkstyleclass = document.NEU.linkstyleclass.value;
	}

	if (foundLink != null) {
  		foundLink.removeNode();
	}

	if (linkurl.length > 0) {

  		foundRange.execCommand("CreateLink", false, "/");

  		var el = foundRange.parentElement();
  		while ((el.tagName != "BODY") && (el.tagName != "A")) {
      			if (el.tagName == "IMG") {
          			// Set border to 0 for images, this is what you want in 99% of all cases
          			el.border = 0;
      			}
      			el = el.parentElement;
  		}

	  	if (linkurl.length > 0) {
	  			
	      		el.setAttribute("HREF", checkContext(linkurl, true), 0);
	  	} else {
	      		el.removeAttribute("HREF", false);
	  	}
	
	  	if ((linktarget.length > 0) && (linkurl.length > 0)) {
	      		el.target = linktarget;
	  	} else {
	      		el.removeAttribute("TARGET", false);
	  	}

	  	if (linkEditorStyleInputs) {
	      		if(linkstyle.length > 0) {
	          		el.style.cssText = linkstyle;
	      		}
	
	      		if(linkstyleclass.length > 0) {
	          		el.className = linkstyleclass;
	      		}
	  	}
	}

	window.close();
}

/**
* Set the current selection in the calling editor and fill the fields of the editor form.
* You must set the following variables in the javascript of the opening window:
*
* linkEditor
* linkEditorAll
* linkEditorRange
* linkEditorSelection
* linkEditorStyleInputs
* linkEditorPrefix
*/
function init() {
	// Get the editor element, a complete range of the editor and the editor selection
	linkEditor = window.opener.linkEditor;
	linkEditorAll = window.opener.linkEditorAll;
	linkEditorRange = window.opener.linkEditorRange;
	linkEditorSelection = window.opener.linkEditorSelection;
	if (window.opener.linkEditorStyleInputs != null) {
    	linkEditorStyleInputs = window.opener.linkEditorStyleInputs;
	}
	if (window.opener.linkEditorPrefix != null) {
    	linkEditorPrefix = window.opener.linkEditorPrefix;
	}

	// Get all links in editor (ie. tags like <A HREF>)
	var allLinks = linkEditorAll.tags("A");

	// Create a range on the current selection
	var range = linkEditorSelection.createRange();

	if (typeof(range.text) != 'undefined') {
    // If this is undefined, the selection is a MS IE "ControlSelection",
    // which can not be used for adding a link

    for(i = 0; i < allLinks.length; i++) {
        foundRange = null;

        // Create range on whole text
        var mainrange = linkEditorRange;

        // Move range to the current A-element
        mainrange.moveToElementText(allLinks[i]);

        // Compare the selection with the current range, and expand if neccessary
        if (mainrange.inRange(range)) {
            foundRange = mainrange;
        } else if (range.inRange(mainrange) || range.isEqual(mainrange)) {
            foundRange = range;
        } else {
            var s2e = range.compareEndPoints("StartToEnd", mainrange);
            var s2s = range.compareEndPoints("StartToStart", mainrange);
            var e2s = range.compareEndPoints("EndToStart", mainrange);
            var e2e = range.compareEndPoints("EndToEnd", mainrange);
            if ((s2s == -1) && (e2s >= 0)) {
                foundRange = range;
                foundRange.setEndPoint("EndToEnd", mainrange);
            } else if ((s2e == -1) && (e2e >= 0)) {
                foundRange = range;
                foundRange.setEndPoint("StartToStart", mainrange);
            }
        }

        // Finally fill the input fields of the form
        if (foundRange != null) {
            // Use expanded selection to fill input areas
            foundRange.select();
            foundLink = allLinks[i];
            document.forms["NEU"].elements["neulink"].value = checkContext(foundLink.getAttribute("HREF", 2), false);
            if (linkEditorStyleInputs) {
                document.forms["NEU"].elements["linkstyle"].value = foundLink.style.getAttribute("CSSTEXT", 2);
                document.forms["NEU"].elements["linkstyleclass"].value = foundLink.getAttribute("CLASSNAME", 2);
            }

            document.forms["NEU"].elements["targetname"].value = "";
            if((foundLink.target == "_self") || (foundLink.target == "") || (foundLink.target == null)) {
                document.forms["NEU"].elements["linktarget"].selectedIndex = 0;
            } else if(foundLink.target == "_blank") {
                document.forms["NEU"].elements["linktarget"].selectedIndex = 1;
            } else if(foundLink.target == "_top") {
                document.forms["NEU"].elements["linktarget"].selectedIndex = 2;
            } else {
                document.forms["NEU"].elements["linktarget"].selectedIndex = 3;
                document.forms["NEU"].elements["targetname"].value = foundLink.target;
            }
            setNameTarget(false);
            break;
        }
    }

    if (foundLink == null) {
        // No previous "A" element found, set selection text in input area
        foundRange = range;
    }
}

if ((foundRange == null) || (foundRange.htmlText == "") || (foundRange.htmlText == null)) {
    // No valid selection, display message and close window
    alert("<%= wp.key("editor.message.noselection") %>");
    window.close();
} else {
    document.forms["NEU"].elements["neulink"].focus();
}
}

function setNameTarget(param) {
	var select = document.forms["NEU"].elements["linktarget"];
	var input  = document.forms["NEU"].elements["targetname"];
	var span   = document.all["targetinput"];
	if (param) {
   		var target = input.value;
    	if ((target != null) && (target.trim() != "")) {
        	target = target.trim();
        	input.value = target;
        	select.selectedIndex = 3;
    	} else {
        	if (select.selectedIndex == 3) {
            	select.selectedIndex = 0;
            	span.style.visibility = "hidden";
        	}
    	}
	} else {
    	if (select.selectedIndex == 3) {
        	span.style.visibility = "visible";
    	} else {
       		span.style.visibility = "hidden";
    	}
	}
}

/**
* This regular expression checks if a string looks like as if
* it starts with a server name, ie. "xxx.yyy.zzz", but without
* a protocol.
*/
function checkUrl(strValue) {
	var objRegExp  = /(^[a-z]([a-z_]*)[.]([a-z0-9\-]*)[.]([a-z_\.]*)([\/]*)([a-z_\/\.]*)$)/i;
	return objRegExp.test(strValue);
}

/**
* If a user forgets to insert a protocol at the beginning of the URL,
* (eg. just "www.server.com" instead of "http://www.server.com"),
* MSHTML will treat this as a relative URL at the current server. This
* is not wanted in almost 99% of all cases, so the input value is checked
* and the user is warned in case the URL looks like a server string without protocol.
*/
function checkLinkUrl() {
	var url = document.forms["NEU"].elements["neulink"];
	if (checkUrl(url.value)) {
	    var conf = confirm("<%= wp.key("editor.message.confirmhttp") %>");
    	if (conf) {
        	url.value = "http://" + url.value;
    	}
	}
}

/**
* Checks the link URL and adds or removes the OpenCms context path when opening and closing the dialog window.
* add = true means to add the path to the given String, false removes the context path from it (if present and initialized).
*/
function checkContext(linkUrl, add) {
	if (linkEditorPrefix != null) {
		if (add) {
			if (linkUrl.charAt(0) == "/" && linkUrl.indexOf(linkEditorPrefix) != 0) {
				// add the context path to the link when closing the dialog
				return linkEditorPrefix + linkUrl;
			}
		} else if (linkUrl.indexOf(linkEditorPrefix) == 0) {
			// remove the context path from the link when opening the window
			return linkUrl.substring(linkEditorPrefix.length);
		}
	}
	return linkUrl;
}

//-->
</script>

<script type="text/javascript" src="<%= wp.getSkinUri() %>editors/msdhtml/scripts/edithtml.js"></script>
<script type="text/javascript" src="<%= wp.getSkinUri() %>commons/tree.js"></script>
<script type="text/javascript">
<!--
        <%= CmsTree.initTree(wp.getCms(), wp.getEncoding(), wp.getSkinUri()) %>
                
        var treewin = null;
		var treeForm = null;
		var treeField = null;
		var treeDoc = null;
        
        function openTreeWin(formName, fieldName, curDoc) {
			var paramString = "?type=pagelink&includefiles=true";

			treewin = openWin(vr.contextPath + vr.workplacePath + "views/explorer/tree_fs.jsp" + paramString, "opencms", 300, 450);
			treeForm = formName;
			treeField = fieldName;
			treeDoc = curDoc;
		}
		
		function openWin(url, name, w, h) {
			var newwin = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
			if(newwin != null) {
				if (newwin.opener == null) {
					newwin.opener = self;
				}
			}
			newwin.focus();
			return newwin;
		}
		
		function closeTreeWin() {
			if (treewin != null) {
				window.treewin.close();
				treewin = null;
				treeForm = null;
				treeField = null;
				treeDoc = null;
			}
		}
		
		function setFormValue(filename) {
			var curForm;
			var curDoc;
			if (treeDoc != null) {
				curDoc = treeDoc;
			} else {
				curDoc = win.files;
			}
			if (treeForm != null) {
				curForm = curDoc.forms[treeForm];	
			} else {
				curForm = curDoc.forms[0];
			}
			if (curForm.elements[treeField]) {
				curForm.elements[treeField].value = filename;	
			}
		}
		
        //-->
        </script>


<%= wp.bodyStart("dialog", " onLoad=\"init();\" onunload=\"closeTreeWin();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.key("title.newlink")) %>

<form name="NEU" class="nomargin" onsubmit="pasteLink();">

<table border="0" cellspacing="0" cellpadding="4" width="100%">
                <tr>
                    <td style="white-space: nowrap;"><%= wp.key("input.linkto") %>:</td>
                    <td class="maxwidth">
                        <input type="text" name="neulink"  class="maxwidth" value="" onchange="checkLinkUrl();" />
                    </td>
                    <td><a href="javascript:openTreeWin('NEU', 'neulink', document);"><img src="<%= wp.getSkinUri() %>filetypes/folder.gif" border="0" alt="<%= wp.key("label.selectfolder") %>"></a></td>
                </tr>
                <script type="text/javascript">
                <!--
                if (window.opener.linkEditorStyleInputs) {
                  document.write('<tr><td style="white-space: nowrap;"><%= wp.key("input.linkstyle") %>:</td>' +
                    '<td class="maxwidth"><input type="text" name="linkstyle" class="maxwidth"  />' +
                    '</td><td>&nbsp;</td></tr><tr>' +
                    '<td style="white-space: nowrap;"><%= wp.key("input.linkstyleclass") %>:</td>' +
                    '<td class="maxwidth"><input type="text" name="linkstyleclass" class="maxwidth" />' +
                    '</td><td>&nbsp;</td></tr>');
                }
                // -->
                </script>
                <tr>
                    <td style="white-space: nowrap;"><%= wp.key("input.linktarget") %>:</td>
                    <td class="maxwidth" style="white-space: nowrap;">
                      <select name="linktarget" id="linktarget" size="1" style="width:150px" onchange="setNameTarget(false);">
                        <option value="_self"><%= wp.key("input.linktargetself") %></option>
                        <option value="_blank"><%= wp.key("input.linktargetblank") %></option>
                        <option value="_top"><%= wp.key("input.linktargettop") %></option>
                        <option value="named"><%= wp.key("input.linktargetnamed") %></option>
                      </select>
                      &nbsp;&nbsp;<span id="targetinput" class="maxwidth" style="visibility:hidden; text-align:right;"><input type="text" name="targetname" style="width:120px;" onchange="setNameTarget(true);" /></span></td>
                    <td>&nbsp;</td>
                </tr>              
            </table>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel(null, "onclick=\"window.close();\"") %>
</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>