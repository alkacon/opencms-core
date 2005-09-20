<%@ page import="org.opencms.workplace.*, org.opencms.workplace.explorer.*" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	wp.setParamIsPopup("true");
	
%><%= wp.htmlStart(null, wp.key("title.newlink")) %>
<script type="text/javascript">
<!--

var linkEditorStyleInputs = <%= request.getParameter("showCss") %>;
var linkEditorPrefix = null;

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
	var linktarget = document.NEU.linktarget.options[document.NEU.linktarget.selectedIndex].value;
	if ("named" == linktarget) {
  		linktarget = document.NEU.targetname.value;
  		if ((linktarget == null) || (linktarget.trim() == "")) {
      			linktarget="_self";
  		}
	}
	
	var linkInformation = new Object();
	linkInformation["type"] = "link";
	var linkAnchor = document.NEU.neulink.value;
	if (linkAnchor.length > 0) {
		linkAnchor = checkContext(linkAnchor, true);
	}
	var linktitle = document.NEU.linktitle.value;

	linkInformation["href"] = linkAnchor;
	linkInformation["name"] = "";
	linkInformation["target"] = linktarget;
	linkInformation["title"] = linktitle;
	if (linkEditorStyleInputs) {
		linkInformation["style"] = document.NEU.linkstyle.value;
		linkInformation["class"] = document.NEU.linkstyleclass.value;
	} else {
		linkInformation["style"] = "";
		linkInformation["class"] = "";
	}
	window.opener.createLink(linkInformation);
	window.close();
}

/**
* Set the current selection in the calling editor and fill the fields of the editor form.
* You must set the request parameters in the javascript of the opening window:
*
* showCss
* href
* target
* style
* class
*/
function init() {
	if (window.opener.linkEditorPrefix != null) {
    	linkEditorPrefix = window.opener.linkEditorPrefix;
	}
	var anchor = "<%= request.getParameter("href") %>";
	if (anchor != "null") {
		document.forms["NEU"].elements["neulink"].value = anchor;
	}
	var title= "<%= request.getParameter("title") %>";
	if (title != "null") {
		document.forms["NEU"].elements["linktitle"].value = title.trim();
	}

	if (linkEditorStyleInputs) {
		var anchorStyle = "<%= request.getParameter("style") %>";
		var anchorClass = "<%= request.getParameter("class") %>";
		if (anchorStyle != "null") {
			document.forms["NEU"].elements["linkstyle"].value = anchorStyle;
		}
		if (anchorClass != "null") {
			document.forms["NEU"].elements["linkstyleclass"].value = anchorClass;
		}
	}
	
	document.forms["NEU"].elements["targetname"].value = "";
	var anchorTarget = "<%= request.getParameter("target") %>";
	if (anchorTarget != "null") {
		if ((anchorTarget == "_self") || (anchorTarget == "") || (anchorTarget == null)) {
    	    document.forms["NEU"].elements["linktarget"].selectedIndex = 0;
        } else if (anchorTarget == "_blank") {
            document.forms["NEU"].elements["linktarget"].selectedIndex = 1;
        } else if (anchorTarget == "_top") {
            document.forms["NEU"].elements["linktarget"].selectedIndex = 2;
        } else {
            document.forms["NEU"].elements["linktarget"].selectedIndex = 3;
            document.forms["NEU"].elements["targetname"].value = anchorTarget;
        }
        setNameTarget(false);
	}
	document.forms["NEU"].elements["neulink"].focus();
}

function setNameTarget(param) {
	var select = document.forms["NEU"].elements["linktarget"];
	var input  = document.forms["NEU"].elements["targetname"];
	var span   = document.getElementById("targetinput");
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
                if (linkEditorStyleInputs) {
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
                <tr>
                    <td style="white-space: nowrap;"><%= wp.key("input.linktitle") %>:</td>
                    <td class="maxwidth">
                        <input type="text" name="linktitle"  class="maxwidth" value="" />
                    </td>
                    <td></td>
                </tr>
           
            </table>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel(null, "onclick=\"window.close();\"") %>
</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>