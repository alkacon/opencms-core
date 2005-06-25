<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsChnav wp = new CmsChnav(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsChnav.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsChnav.ACTION_CHNAV:

//////////////////// ACTION: main change navigation action (with optional confirm / wait screen)

	wp.actionChangeNav();

break;


case CmsChnav.ACTION_DEFAULT:
default:

//////////////////// ACTION: show change navigation dialog (default)

	wp.setParamAction("chnav");

%><%= wp.htmlStart("help.explorer.contextmenu.chnav") %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>

<form name="changenav" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'changenav');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>
<%= wp.dialogSpacer() %>

<table border="0">
<tr>
	<td><%= wp.key("input.navtext") %></td>
	<td class="maxwidth"><input type="text" name="<%= wp.PARAM_NAVTEXT %>" class="maxwidth" value="<%= wp.getCurrentNavText() %>"></td>
</tr>
<tr>
	<td><%= wp.key("input.insert") %></td>
	<td><%= wp.buildNavPosSelector() %></td>
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
