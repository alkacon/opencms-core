<%@ page import="org.opencms.workplace.explorer.*" %><%	

	// initialize the workplace class
	CmsNewResourceXmlPage wp = (CmsNewResourceXmlPage)CmsNewResource.getNewResourceHandler(org.opencms.file.types.CmsResourceTypeXmlPage.getStaticTypeName(), "org.opencms.workplace.explorer.CmsNewResourceXmlPage", pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsNewResourceXmlPage.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;


case CmsNewResourceXmlPage.ACTION_OK:
//////////////////// ACTION: resource name specified and form submitted
	wp.actionCreateResource();
	if (wp.isResourceCreated()) {
		wp.actionEditProperties(); // redirects only if the edit properties option was checked
	}
break;


case CmsNewResourceXmlPage.ACTION_NEWFORM:
case CmsNewResourceXmlPage.ACTION_DEFAULT:
default:
//////////////////// ACTION: show the form to specify the page name, edit properties option and template and body selection
	
	wp.setParamAction(wp.DIALOG_OK);

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
	<td class="maxwidth"><input name="<%= wp.PARAM_RESOURCE %>" id="newresfield" type="text" value="<%= wp.getParamResource() %>" class="maxwidth" onkeyup="checkValue();"></td>
</tr> 
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.template") %></td>
	<td class="maxwidth"><%= wp.buildSelectTemplates("name=\"" + wp.PARAM_TEMPLATE + "\" class=\"maxwidth noborder\"") %></td>
</tr> 
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.default_body") %></td>
	<td class="maxwidth"><%= wp.buildSelectBodyFile("name=\"" + wp.PARAM_BODYFILE + "\" class=\"maxwidth noborder\"") %></td>
</tr>
<tr>
	<td>&nbsp;</td>
	<td style="white-space: nowrap;" unselectable="on" class="maxwidth"><input name="<%= wp.PARAM_NEWRESOURCEEDITPROPS %>" id="newresedit" type="checkbox" value="true" checked="checked" onclick="toggleButtonLabel();">&nbsp;<%= wp.key("input.newfile.editproperties") %></td>    
</tr>

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
//////////////////// end of switch statement 
%>