<%@ page import="org.opencms.workplace.explorer.*" buffer="none" %><%	

	// initialize the workplace class
	CmsNewResourceSibling wp = new CmsNewResourceSibling(pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsNewResourceSibling.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;


case CmsNewResourceSibling.ACTION_OK:
//////////////////// ACTION: resource name specified and form submitted
	wp.actionCreateResource();
	wp.actionEditProperties(); // redirects only if the edit properties option was checked
break;


case CmsNewResourceSibling.ACTION_NEWFORM:
case CmsNewResourceSibling.ACTION_DEFAULT:
default:
//////////////////// ACTION: show the form to specify the folder name, edit properties option and create index file option
	
	wp.setParamAction(wp.DIALOG_OK);

%><%= wp.htmlStart("help.explorer.new.link") %>
<script type="text/javascript">
<!--
	var labelFinish = "<%= wp.key("button.endwizard") %>";
	var labelNext = "<%= wp.key("button.nextscreen") %>";

	function checkValue() {
		var resName = document.getElementById("newresfield").value;
		var linkTarget = document.getElementById("<%= wp.PARAM_LINKTARGET %>").value;
		var theButton = document.getElementById("nextButton");
		if (resName.length == 0 || linkTarget.length == 0) { 
			if (theButton.disabled == false) {
				theButton.disabled =true;
			}
		} else {
			if (theButton.disabled == true) {
				theButton.disabled = false;
			}
		}
	}
	
	function toggleButtonLabel() {
		var theCheckBox = document.getElementById("newresedit");
		var theButton = document.getElementById("nextButton");
		if (theCheckBox.checked == true) {
			theButton.value = labelNext;
		} else {
			theButton.value = labelFinish;
		}
	}
	
	// this function is invoked when the user selected
	// a file/folder in the treeview window...
	function copySelection() {
    	var name = document.getElementById("<%= wp.PARAM_RESOURCE %>").value;
    	var link = document.getElementById("<%= wp.PARAM_LINKTARGET %>").value;
   		if (link.charAt(link.length -1) != "/") {
        	document.getElementById("<%= wp.PARAM_RESOURCE %>").value = link.substring( link.lastIndexOf("/")+1, link.length );
    	}
    	else {
        	document.getElementById("<%= wp.PARAM_RESOURCE %>").value = "";
    	}
    	checkValue();
	}
	
	function fillValues(theValue) {
		var curForm = document.forms["main"];
		var linkname = theValue;
		if (linkname.charAt(linkname.length - 1) == "/") {
			linkname = linkname.substring(0, linkname.length - 1);
			theValue = linkname;
		}
		if (linkname.lastIndexOf("/") != -1) {
			linkname = linkname.substring(linkname.lastIndexOf("/") + 1);
		}
		
		if (theValue.indexOf("/") != -1) {
			theValue = theValue.substring(0, theValue.lastIndexOf("/") + 1);
		}
		
		var curPath = "<%= wp.getCurrentPath() %>";
		if (curPath == theValue) {
			linkname = "link_" + linkname;
		}	
		curForm.elements["<%= wp.PARAM_RESOURCE %>"].value = linkname;
		checkValue();
	}
//-->
</script>
<%= wp.bodyStart("dialog", " onunload=\"top.closeTreeWin();\"") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.name") %></td>
	<td colspan="2" class="maxwidth"><input name="<%= wp.PARAM_RESOURCE %>" id="newresfield" type="text" value="" class="maxwidth" onkeyup="checkValue();"></td>
</tr> 
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.newsibling") %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_LINKTARGET %>" id="<%= wp.PARAM_LINKTARGET %>" type="text" value="" class="maxwidth" onkeyup="checkValue();"></td>
	<td><input name="selectfolder" type="button" value="<%= wp.key("button.search") %>" onClick="top.openTreeWin('vfslink', true, 'main', '<%= wp.PARAM_LINKTARGET %>');" class="dialogbutton" style="width: 60px;"></td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" colspan="2"><input name="<%= wp.PARAM_NEWRESOURCEEDITPROPS %>" id="newresedit" type="checkbox" value="true" checked="checked" onclick="toggleButtonLabel();">&nbsp;<%= wp.key("input.newfile.editproperties") %></td>    
</tr>
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" colspan="2"><input name="<%= wp.PARAM_KEEPPROPERTIES %>" id="<%= wp.PARAM_KEEPPROPERTIES %>" type="checkbox" value="true" checked="checked">&nbsp;<%= wp.key("input.keepproperties") %></td>    
</tr>
</table>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsNextCancel("id=\"nextButton\" disabled=\"disabled\"", null) %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>