<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsMove
" %><%	

	// initialize the workplace class
	CmsMove wp = new CmsMove(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsMove.ACTION_MOVE:
case CmsDialog.ACTION_CONFIRMED:		
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main move action (with optional wait screen)

	wp.actionMove();

break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:
//////////////////// ACTION: show move dialog (default)

	wp.setParamAction("move");

%><%= wp.htmlStart("help.explorer.contextmenu.move") %>
<%= wp.bodyStart("dialog", "onunload=\"top.closeTreeWin();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
boolean includeFiles = true;
if (wp.isMultiOperation()) { 
	includeFiles = false; %>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { //%>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.commons.Messages.GUI_MOVE_TO_0) %></td>
	<td class="maxwidth"><input name="<%= CmsDialog.PARAM_TARGET %>" type="text" value="<%= wp.getCurrentResourceName() %>" class="maxwidth"></td>
	<td><input name="selectfolder" type="button" value="<%= wp.key(org.opencms.workplace.commons.Messages.GUI_LABEL_SEARCH_0) %>" onClick="top.openTreeWin('copy', <%= includeFiles %>);" class="dialogbutton" style="min-width: 60px;">
</tr>
</table>

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