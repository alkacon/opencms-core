<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsChnav,
	org.opencms.workplace.commons.Messages
" %><%	

	// initialize the workplace class
	CmsChnav wp = new CmsChnav(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsChnav.ACTION_CHNAV:

//////////////////// ACTION: main change navigation action (with optional confirm / wait screen)

	wp.actionChangeNav();

break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:
//////////////////// ACTION: show change navigation dialog (default)

	wp.setParamAction("chnav");

%><%= wp.htmlStart("help.explorer.contextmenu.chnav") %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>

<form name="changenav" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'changenav');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>
<%= wp.dialogSpacer() %>

<table border="0">
<tr>
	<td><%= wp.key(Messages.GUI_LABEL_NAVTEXT_0) %></td>
	<td class="maxwidth"><input type="text" name="<%= CmsChnav.PARAM_NAVTEXT %>" class="maxwidth" value="<%= wp.getCurrentNavText() %>"></td>
</tr>
<tr>
	<td nowrap><%= wp.key(Messages.GUI_CHNAV_INSERT_AFTER_0) %></td>
	<td nowrap><%= wp.buildNavPosSelector() %></td>
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