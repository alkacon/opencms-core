<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsPublishProject wp = new CmsPublishProject(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsPublishProject.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


//////////////////// ACTION: other actions handled outside of this JSP
case CmsPublishProject.ACTION_CONFIRMED:
case CmsPublishProject.ACTION_REPORT_BEGIN:
case CmsPublishProject.ACTION_REPORT_UPDATE:
case CmsPublishProject.ACTION_REPORT_END:

	wp.actionReport();

break;
//////////////////// ACTION: show unlock confirmation dialog
case CmsPublishProject.ACTION_UNLOCK_CONFIRMATION:

	wp.setParamAction(CmsPublishProject.DIALOG_UNLOCK_CONFIRMED);

%><%= wp.htmlStart() %>

<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.key(Messages.GUI_PUBLISH_RELEASE_LOCKS_1, new Object[] {wp.getParamProjectname()}) %>


<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%

break;
//////////////////// ACTION: show start dialog
case CmsPublishProject.ACTION_DEFAULT:
default:

	wp.setParamAction(CmsPublishProject.DIALOG_CONFIRMED);

%><%= wp.htmlStart() %>

<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.key(Messages.GUI_PUBLISH_PROJECT_CONFIRMATION_1, new Object[] {wp.getParamProjectname()}) %>


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