<%@ page import="org.opencms.workplace.explorer.*" buffer="none" %><%	

	// initialize the workplace class
	CmsNewResource wp = new CmsNewResource(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsNewResource.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;


case CmsNewResource.ACTION_OK:
//////////////////// ACTION: ok button pressed
	wp.actionSelect();
break;


case CmsNewResource.ACTION_SUBMITFORM:
//////////////////// ACTION: resource name specified and form submitted
	wp.actionCreateResource();
	if (wp.isResourceCreated()) {
		wp.actionEditProperties(); // redirects only if the edit properties option was checked
	}	
break;


case CmsNewResource.ACTION_NEWFORM:
//////////////////// ACTION: show the form to specify the resource name and the edit properties checkbox
	
	wp.setParamAction(wp.DIALOG_SUBMITFORM);

%><%= wp.htmlStart("help.explorer.new.file") %>
<script type="text/javascript">
<!--
	var labelFinish = "<%= wp.key("button.endwizard") %>";
	var labelNext = "<%= wp.key("button.nextscreen") %>";

	function checkValue() {
		var resName = document.getElementById("newresfield").value;
		var theButton = document.getElementById("nextButton");
		if (resName.length == 0) { 
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
//-->
</script>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.name") %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_RESOURCE %>" id="newresfield" type="text" value="" class="maxwidth" onkeyup="checkValue();"></td>
</tr> 
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" class="maxwidth"><input name="<%= wp.PARAM_NEWRESOURCEEDITPROPS %>" id="newresedit" type="checkbox" value="true" checked="checked" onclick="toggleButtonLabel();">&nbsp;<%= wp.key("input.newfile.editproperties") %></td>    
</tr>
</table>

<%= wp.dialogSpacer() %>

<table border="0">

</table>


<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsNextCancel("id=\"nextButton\" disabled=\"disabled\"", null) %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;


case CmsNewResource.ACTION_DEFAULT:
default:
//////////////////// ACTION: show new resource selection dialog

	wp.setParamAction(wp.DIALOG_OK);

%><%= wp.htmlStart("help.explorer.new.file") %>
<script type="text/javascript">
<!--
	function enableButton() {
		var theButton = document.getElementById("nextButton");
		if (theButton.disabled == true) {
			theButton.disabled = false;
		}
	}
//-->
</script>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.key("select.new") %>

<%= wp.dialogSpacer() %>

<%= wp.dialogWhiteBoxStart() %>
<%= wp.buildNewList("onclick=\"enableButton();\"") %>
<%= wp.dialogWhiteBoxEnd() %>

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