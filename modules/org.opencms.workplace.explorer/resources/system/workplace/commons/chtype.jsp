<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsChtype,
	org.opencms.workplace.commons.Messages
" %><%	

	// initialize the workplace class
	CmsChtype wp = new CmsChtype(pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
break;


case CmsDialog.ACTION_OK:
//////////////////// ACTION: ok button pressed
	wp.actionChtype();
break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:
//////////////////// ACTION: show the form to specify the new file type
	
	wp.setParamAction(CmsDialog.DIALOG_OK);

%><%= wp.htmlStart("help.explorer.new.file") %>

<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>
<%= wp.dialogSpacer() %>
<%= wp.dialogRowStart() %>
<%= wp.key(Messages.GUI_CHTYPE_PLEASE_SELECT_0) %>
<%= wp.dialogRowEnd() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.dialogWhiteBoxStart() %>
<table border="0">
<%= wp.buildTypeList() %> 
</table>
<%= wp.dialogWhiteBoxEnd() %>


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