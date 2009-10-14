<%@ page
	import="org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.*"%>
<%@page import="org.opencms.module.CmsModule"%>
<%@page import="org.opencms.main.*"%>
<%@page import="org.opencms.util.*"%>
<%@page import="java.util.*"%>
<%@page import="org.opencms.file.*"%>
<%@page import="org.opencms.i18n.CmsEncoder"%>
<%@page import="org.opencms.file.types.CmsResourceTypeXmlContent"%>	

<%	

	// initialize the workplace class
	CmsDialogMultiPropertyEdit wp = new CmsDialogMultiPropertyEdit(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsDialogMultiPropertyEdit.ACTION_MULTIFILEPROPERTYEDIT:	
//////////////////// ACTION: main comment images action

	wp.actionCommentImages();

break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:

//////////////////// ACTION: show comment images dialog (default)

	wp.setParamAction(CmsDialogMultiPropertyEdit.DIALOG_TYPE);

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main"
	action="<%= wp.getDialogUri() %>" method="post" class="nomargin"
	onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %> <input type="hidden"
	name="<%= CmsDialog.PARAM_FRAMENAME %>" value=""> <%= wp.buildDialogForm() %>

<%= wp.dialogContentEnd() %> <%= wp.dialogButtonsOkCancel() %> 
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