<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsLock,
	org.opencms.workplace.commons.Messages
" %><%	

	// initialize the workplace class
	CmsLock wp = new CmsLock(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsDialog.ACTION_CONFIRMED:
case CmsDialog.ACTION_WAIT:
//////////////////// ACTION: main locking action

	wp.actionToggleLock();

break;


case CmsLock.ACTION_SUBMIT_NOCONFIRMATION:
//////////////////// ACTION: auto submits the form without user confirmation

	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);
%>
<%= wp.htmlStart() %>
<%= wp.bodyStart(null) %>
<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
</form>
<script type="text/javascript">
    submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');
	document.forms["main"].submit();
</script>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;


case CmsDialog.ACTION_DEFAULT:
default:
//////////////////// ACTION: show confirmation dialog (default)

	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);

%><%= wp.htmlStart("help.explorer.contextmenu.lock") %>
<% if (CmsLock.getDialogAction(wp.getCms()) != CmsLock.TYPE_UNLOCK) { %>
<%= wp.buildIncludeJs() %>
<%= wp.buildDefaultConfirmationJS() %>
<% } %>
<%= wp.bodyStart("dialog") %>
<div id='lock-body-id'>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { 
    // include multi resource list  %>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { 
    // include resource info  %>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>
<%= wp.dialogSpacer() %>
<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<% if (CmsLock.getDialogAction(wp.getCms()) != CmsLock.TYPE_UNLOCK) { %>
<%= wp.buildAjaxResultContainer(wp.key(Messages.GUI_LOCK_RESOURCES_TITLE_0)) %>
<% } %>
<div id='conf-msg'></div>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtons() %>
</form>
<%= wp.dialogEnd() %>
</div>
<%= wp.bodyEnd() %>
<% if (CmsLock.getDialogAction(wp.getCms()) != CmsLock.TYPE_UNLOCK) { %>
<%= wp.buildLockRequest() %>
<% } %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>