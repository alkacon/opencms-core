<%@ page import="
    org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsUndelete
" %><%	

	// initialize the workplace class
	CmsUndelete wp = new CmsUndelete(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsUndelete.ACTION_UNDELETE:
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main undelete action (with optional confirm / wait screen)

	wp.actionUndelete();

break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:
//////////////////// ACTION: show undelete dialog (default)

	wp.setParamAction("undelete");

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { %>
	<%@ include file="includes/multiresourcelist.txt" %>
	<%= wp.dialogSpacer() %><%
} %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.buildConfirmationMessage() %>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<% 
   break;

case CmsDialog.ACTION_DEFAULT:
default:
%>
<%= wp.buildLockDialog() %>
<% } 
//////////////////// end of switch statement 
%>