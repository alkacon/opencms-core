<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsMove wp = new CmsMove(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsMove.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsMove.ACTION_MOVE:
case CmsMove.ACTION_CONFIRMED:		
case CmsMove.ACTION_WAIT:

//////////////////// ACTION: main move action (with optional confirm / wait screen)

	wp.actionMove();

break;


case CmsMove.ACTION_DEFAULT:
default:

//////////////////// ACTION: show move dialog (default)

	wp.setParamAction("move");

%><%= wp.htmlStart("help.explorer.contextmenu.move") %>
<%= wp.bodyStart("dialog", "onunload=\"top.closeTreeWin();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("GUI_MOVE_TO_0") %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_TARGET %>" type="text" value="<%= wp.getCurrentResourceName() %>" class="maxwidth"></td>
	<td><input name="selectfolder" type="button" value="<%= wp.key("GUI_LABEL_SEARCH_0") %>" onClick="top.openTreeWin('copy');" class="dialogbutton" style="width: 60px;">
</tr>
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