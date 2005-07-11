<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsUndoChanges wp = new CmsUndoChanges(pageContext, request, response);
	
//////////////////// start of switch statement 


switch (wp.getAction()) {

case CmsUndoChanges.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsUndoChanges.ACTION_UNDOCHANGES:
case CmsUndoChanges.ACTION_WAIT:

//////////////////// ACTION: main undo changes action with optional wait screen)

	wp.actionUndoChanges();

break;


case CmsUndoChanges.ACTION_DEFAULT:
default:

//////////////////// ACTION: show undo changes dialog (default)

	wp.setParamAction("undochanges");

%><%= wp.htmlStart("help.explorer.contextmenu.undo_changes") %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<%= wp.key("GUI_UNDO_LASTMODIFIED_INFO_3", new Object[] {wp.getFileName(), wp.getLastModifiedDate(), wp.getLastModifiedUser()}) %>

<%= wp.dialogSpacer() %>

<%= wp.key("GUI_UNDO_CONFIRMATION_0") %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<table border=0>
<tr><td>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
</td></tr>

<%= wp.buildCheckRecursive() %>

</table>
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