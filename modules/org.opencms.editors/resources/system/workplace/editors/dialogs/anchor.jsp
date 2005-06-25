<%@ page import="org.opencms.workplace.*" %><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	wp.setParamIsPopup("true");
	
%><%= wp.htmlStart(null, wp.key("title.newanchor")) %>
<script type="text/javascript">
<!--

var linkEditorStyleInputs = <%= request.getParameter("showCss") %>;

/**
* Paste the calculated link to the calling editor.
*/
function pasteLink() {
	var linkInformation = new Object();
	linkInformation["type"] = "anchor";
	linkInformation["href"] = "";
	linkInformation["name"] = document.NEU.linkanchor.value;
	linkInformation["target"] = "";
	linkInformation["title"] = "";
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
* You must set the following parameters in the rquest of the opening window:
*
* showCss
* name
* style
* class
*/
function init() {
	var anchorName = "<%= request.getParameter("name") %>";
	if (anchorName != "null") {
		document.forms["NEU"].elements["linkanchor"].value = anchorName;
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
	document.forms["NEU"].elements["linkanchor"].focus();

}

//-->
</script>

<script type="text/javascript" src="<%= wp.getSkinUri() %>editors/msdhtml/scripts/edithtml.js"></script>

<%= wp.bodyStart("dialog", " onLoad=\"init();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.key("title.newanchor")) %>

<form name="NEU" class="nomargin" onsubmit="pasteLink();">

<!-- ############################################################################## -->

<table border="0" cellspacing="0" cellpadding="4" width="100%">
            <tr>
                    <td style="white-space: nowrap;"><%= wp.key("input.linkanchor") %>:</td>
                    <td class="maxwidth">
                        <input type="text" name="linkanchor" class="maxwidth" />
                    </td>
                </tr>
                <script type="text/javascript">
                <!--
                if (linkEditorStyleInputs) {
                  document.write('<tr><td style="white-space: nowrap;"><%= wp.key("input.linkstyle") %>:</td>' +
                    '<td class="maxwidth"><input type="text" name="linkstyle" class="maxwidth"  />' +
                    '</td><td>&nbsp;</td></tr><tr>' +
                    '<td style="white-space: nowrap;"><%= wp.key("input.linkstyleclass") %>:</td>' +
                    '<td class="maxwidth"><input type="text" name="linkstyleclass" class="maxwidth" />');
                }
                // -->
                </script>               
            </table>

<!-- ############################################################################## -->

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel(null, "onclick=\"window.close();\"") %>
</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>