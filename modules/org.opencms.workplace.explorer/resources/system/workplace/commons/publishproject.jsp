<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsPublishProject
" %><%	

	// initialize the workplace class
	CmsPublishProject wp = new CmsPublishProject(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();
    break;

//////////////////// ACTION: other actions handled outside of this JSP
case CmsDialog.ACTION_LOCKS_CONFIRMED:
case CmsDialog.ACTION_CONFIRMED:
case CmsDialog.ACTION_REPORT_BEGIN:
case CmsDialog.ACTION_REPORT_UPDATE:
case CmsDialog.ACTION_REPORT_END:

	wp.actionReport();
    break;

case CmsDialog.ACTION_DEFAULT:
default:
%>
<%= wp.buildLockDialog() %>
<% } 
//////////////////// end of switch statement 
%>