<%@ page import="
		org.opencms.workplace.CmsDialog,
		org.opencms.workplace.commons.CmsDelete
" %><%	

	// initialize the workplace class
	CmsDelete wp = new CmsDelete(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;

////////////////////ACTION: other actions handled outside of this JSP
case CmsDialog.ACTION_CONFIRMED:
case CmsDialog.ACTION_REPORT_BEGIN:
case CmsDialog.ACTION_REPORT_UPDATE:
case CmsDialog.ACTION_REPORT_END:

	wp.actionReport();
	break;
	
case CmsDelete.ACTION_DELETE:
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main delete action (with optional confirm / wait screen)

	wp.actionDelete();
	break;


case CmsDialog.ACTION_DEFAULT:
default:

//////////////////// ACTION: show delete dialog (default)

	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);

%><%= wp.htmlStart("help.explorer.contextmenu.delete") %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { 
    // include resource list
    %><%@ include file="includes/multiresourcelist.txt" %><%
} else { 
    // include resource information
	%><%@ include file="includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.buildDeleteSiblings() %>
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