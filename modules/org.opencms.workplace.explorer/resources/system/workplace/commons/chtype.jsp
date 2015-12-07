<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.*" %>
<%	

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
default:
//////////////////// ACTION: show the form to specify the new file type
	
	wp.setParamAction(CmsDialog.DIALOG_OK);
	wp.displayDialog();

	break;

case CmsDialog.ACTION_DEFAULT:

    %>
<%= CmsLock.buildLockDialog(wp) %>
<% } 
//////////////////// end of switch statement 
%>