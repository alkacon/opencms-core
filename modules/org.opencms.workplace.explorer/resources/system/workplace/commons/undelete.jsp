<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsUndelete wp = new CmsUndelete(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsUndelete.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsUndelete.ACTION_UNDELETE:
case CmsUndelete.ACTION_WAIT:

//////////////////// ACTION: main undelete action (with optional confirm / wait screen)

	wp.actionUndelete();

break;


case CmsUndelete.ACTION_DEFAULT:
default:

//////////////////// ACTION: show undelete dialog (default)

	wp.setParamAction("undelete");

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.key("GUI_UNDELETE_CONFIRMATION_0") %>

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