<%@ page import="org.opencms.workplace.commons.*" buffer="none" %><%	

	// initialize the workplace class
	CmsCopyToProject wp = new CmsCopyToProject(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsCopyToProject.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsCopyToProject.ACTION_COPYTOPROJECT:
//////////////////// ACTION: main copy to project action

	wp.actionCopyToProject();

break;


case CmsCopyToProject.ACTION_DEFAULT:
default:

//////////////////// ACTION: show copy to project dialog (default)

	wp.setParamAction(wp.DIALOG_TYPE);

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.buildProjectInformation() %>

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