<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsDelete wp = new CmsDelete(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDelete.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsDelete.ACTION_DELETE:
case CmsDelete.ACTION_WAIT:

//////////////////// ACTION: main delete action (with optional confirm / wait screen)

	wp.actionDelete();

break;


case CmsDelete.ACTION_DEFAULT:
default:

//////////////////// ACTION: show delete dialog (default)

	wp.setParamAction("delete");

%><%= wp.htmlStart("help.explorer.contextmenu.delete") %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.buildDeleteSiblings() %>

<%= wp.key(Messages.GUI_DELETE_CONFIRMATION_0) %>

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