<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsCommentImages
" %><%	

	// initialize the workplace class
	CmsCommentImages wp = new CmsCommentImages(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsCommentImages.ACTION_COMMENTIMAGES:	
//////////////////// ACTION: main comment images action

	wp.actionCommentImages();

break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:

//////////////////// ACTION: show comment images dialog (default)

	wp.setParamAction(CmsCommentImages.DIALOG_TYPE);

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.buildDialogForm() %>

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