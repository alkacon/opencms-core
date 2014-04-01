<%@ page import="org.opencms.workplace.explorer.*" %><%	

	// initialize the workplace class
	CmsNewResourceXmlContent wp = new CmsNewResourceXmlContent(pageContext, request, response);
	
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


case CmsNewResourceXmlContent.ACTION_CHOOSEMODEL:
//////////////////// ACTION: choose the model file for the new resource
break;

case CmsNewResource.ACTION_NEWFORM:
case CmsNewResource.ACTION_DEFAULT:
//////////////////// ACTION: show the form to specify the resource name and the edit properties checkbox
	
	wp.setParamAction(wp.DIALOG_CHECKMODEL);

%><%= wp.htmlStart("help.explorer.new.file") %>
<script type="text/javascript">
<!--
	var labelFinish = "<%= wp.key(Messages.GUI_BUTTON_ENDWIZARD_0) %>";
	var labelNext = "<%= wp.key(Messages.GUI_BUTTON_CONTINUE_0) %>";
	var hasModelFiles = <%= Boolean.valueOf(wp.hasModelFiles()) %>;

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
		if (hasModelFiles || theCheckBox.checked == true) {
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
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" class="maxwidth"><input name="<%= wp.PARAM_APPENDSUFFIXHTML %>" id="<%= wp.PARAM_APPENDSUFFIXHTML %>" type="checkbox" value="true" checked="checked">&nbsp;<%= wp.key(Messages.GUI_NEWRESOURCE_APPENDSUFFIX_HTML_1, new String[] {wp.getSuffixHtml()}) %></td>    
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
}

%>