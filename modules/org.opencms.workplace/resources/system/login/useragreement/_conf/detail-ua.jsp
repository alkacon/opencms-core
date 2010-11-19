<%@ page buffer="none" import="org.opencms.workplace.*" %><%

CmsLoginUserAgreement wp = new CmsLoginUserAgreement(pageContext, request, response);

//////////////////// start of switch statement 
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: decline the user agreement

	wp.actionDecline();

break;


case CmsLoginUserAgreement.ACTION_ACCEPT:	
//////////////////// ACTION: accept the user agreement

	wp.actionAccept();

break;


case CmsDialog.ACTION_DEFAULT:
default:

//////////////////// ACTION: show user agreement form (default)

	wp.setParamAction(CmsLoginUserAgreement.DIALOG_TYPE);

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.dialogWhiteBoxStart() %>
<div style="height: 450px; overflow: auto;">
	<%= wp.getConfigurationContentStringValue(wp.NODE_TEXT) %>
</div>
<%= wp.dialogWhiteBoxEnd() %>

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
