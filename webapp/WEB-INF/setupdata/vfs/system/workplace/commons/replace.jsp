<%@ page import="org.opencms.workplace.commons.*" buffer="none" %><%	

	// initialize the workplace class
	CmsReplace wp = new CmsReplace(pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsReplace.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;


case CmsReplace.ACTION_OK:
//////////////////// ACTION: ok button pressed
	wp.actionReplace();
break;


case CmsReplace.ACTION_DEFAULT:
default:
//////////////////// ACTION: show the form to specify the replace file
	
	wp.setParamAction(wp.DIALOG_OK);

%><%= wp.htmlStart("help.explorer.new.file") %>
<script type="text/javascript">
<!--
	function checkValue() {
		var resName = document.getElementById("newresfield").value;
		var theButton = document.getElementById("okButton");
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
	
	function startTimeOut() {
		// this is required for Mozilla since the onChange event doesn't work there for <input type="file">
		window.setTimeout("checkValue();startTimeOut();", 500);
	}
	
	startTimeOut();	
//-->
</script>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');" enctype="multipart/form-data">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.name") %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_UPLOADFILE %>" id="newresfield" type="file" value="" size="60" class="maxwidth" onchange="checkValue();"></td>
</tr> 
</table>


<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel("id=\"okButton\" disabled=\"disabled\"", null) %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>