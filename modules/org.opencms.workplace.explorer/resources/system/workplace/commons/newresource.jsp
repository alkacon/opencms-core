<%@ page import="org.opencms.workplace.explorer.*" %><% 

    CmsNewResource wp = new CmsNewResource(pageContext, request, response);

	switch (wp.getAction()) {

case CmsNewResource.ACTION_NEWFORM:
//////////////////// ACTION: show the form to specify the resource name and the edit properties checkbox
	
	wp.setParamAction(wp.DIALOG_SUBMITFORM);

%><%= wp.htmlStart("help.explorer.new.file") %>
<script type="text/javascript">
<!--
	var labelFinish = "<%= wp.key(Messages.GUI_BUTTON_ENDWIZARD_0) %>";
	var labelNext = "<%= wp.key(Messages.GUI_BUTTON_CONTINUE_0) %>";

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
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_RESOURCE_NAME_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_RESOURCE %>" id="newresfield" type="text" value="<%= wp.getParamResource() %>" class="maxwidth" onkeyup="checkValue();" onchange="checkValue();" onpaste="setTimeout(checkValue,4);"></td>
</tr> 
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" class="maxwidth"><input name="<%= wp.PARAM_NEWRESOURCEEDITPROPS %>" id="newresedit" type="checkbox" value="true" checked="checked" onclick="toggleButtonLabel();">&nbsp;<%= wp.key(Messages.GUI_NEWFILE_EDITPROPERTIES_0) %></td>    
</tr>
</table>

<%= wp.dialogSpacer() %>

<table border="0">

</table>


<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsNextCancel("id=\"nextButton\" disabled=\"disabled\"", null) %>

</form>

<%= wp.dialogEnd() %>

<script type="text/javascript">
<!--
	checkValue();
//-->
</script>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;

default:
	wp.displayDialog();
	}
//////////////////// end of switch statement 
%>