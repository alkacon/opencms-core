<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsEditPointer wp = new CmsEditPointer(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsEditPointer.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsEditPointer.ACTION_OK:
//////////////////// ACTION: main change link target action

	wp.actionChangeLinkTarget();

break;


case CmsEditPointer.ACTION_DEFAULT:
default:

//////////////////// ACTION: show change link target dialog (default)

	wp.setParamAction(CmsEditPointer.DIALOG_OK);

%><%= wp.htmlStart("help.explorer.new.link") %>
<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key("input.linkto") %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_LINKTARGET %>" id="<%= wp.PARAM_LINKTARGET %>" type="text" value="<%= wp.getOldTargetValue() %>" class="maxwidth"></td>
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