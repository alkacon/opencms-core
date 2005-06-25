<%@ page import="

	org.opencms.workplace.*, 
	org.opencms.workplace.commons.*,
    org.opencms.workplace.tools.link.*

"%><%	

	// initialize the workplace class
	CmsXmlDocumentLinkValidatorReport wp = new CmsXmlDocumentLinkValidatorReport(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {


case CmsDialog.ACTION_REPORT_END:
case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


//////////////////// ACTION: other actions handled outside of this JSP
case CmsDialog.ACTION_CONFIRMED:
case CmsDialog.ACTION_REPORT_BEGIN:
case CmsDialog.ACTION_REPORT_UPDATE:

	wp.actionReport();

break;
//////////////////// ACTION: show start dialog
case CmsDialog.ACTION_DEFAULT:
default:

	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);

%><%= wp.htmlStart() %>

<script language="JavaScript">
function init() {
	if (window.top.body.admin_head) {
		window.top.body.admin_head.location.href="<%= wp.getJsp().link("/system/workplace/action/administration_head.html") %>";
	}
}
</script>

<%= wp.bodyStart("dialog", "onLoad=\"init();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.key("messagebox.html_link_validator1") %>

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