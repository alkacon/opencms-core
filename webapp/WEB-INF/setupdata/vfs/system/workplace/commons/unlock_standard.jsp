<%@ page import="org.opencms.workplace.commons.*" buffer="none" %><%	

	// initialize the workplace class
	CmsLock wp = new CmsLock(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsLock.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsLock.ACTION_CONFIRMED:
//////////////////// ACTION: main unlocking action

	wp.actionToggleLock();

break;


case CmsLock.ACTION_SUBMIT_NOCONFIRMATION:
//////////////////// ACTION: auto submits the form without user confirmation

	wp.setParamAction(CmsLock.DIALOG_CONFIRMED);
%>
<%= wp.htmlStart() %>
<form id="main" name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin">
<%= wp.bodyStart(null) %>
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
</form>
<script type="text/javascript">
	document.forms["main"].submit();
</script>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;


case CmsLock.ACTION_DEFAULT:
default:
//////////////////// ACTION: show confirmation dialog (default)

	wp.setParamAction(CmsLock.DIALOG_CONFIRMED);

%><%= wp.htmlStart("help.explorer.contextmenu.lock") %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form id="main" name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.key("messagebox.message1.unlock") %> <%= wp.key("messagebox.message2.unlock") %>

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