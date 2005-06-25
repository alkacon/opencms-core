<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsChtype wp = new CmsChtype(pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsChtype.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;


case CmsChtype.ACTION_OK:
//////////////////// ACTION: ok button pressed
	wp.actionChtype();
break;


case CmsChtype.ACTION_DEFAULT:
default:
//////////////////// ACTION: show the form to specify the new file type
	
	wp.setParamAction(wp.DIALOG_OK);

%><%= wp.htmlStart("help.explorer.new.file") %>

<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>
<%= wp.dialogSpacer() %>
<%= wp.dialogRowStart() %>
<%= wp.key("message.chtype") %>
<%= wp.dialogRowEnd() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogWhiteBoxStart() %>
<table border="0">
<%= wp.buildTypeList() %> 
</table>
<%= wp.dialogWhiteBoxEnd() %>


<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>