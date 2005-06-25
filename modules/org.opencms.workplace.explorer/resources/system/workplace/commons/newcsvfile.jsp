<%@ page import="org.opencms.workplace.explorer.*" %><%	

	// initialize the workplace class
	CmsNewCsvFileUpload wp = new CmsNewCsvFileUpload(pageContext, request, response);
	
	wp.setParamAction(wp.DIALOG_SUBMITFORM);

%>
<%= wp.htmlStart("help.explorer.new.file") %>
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