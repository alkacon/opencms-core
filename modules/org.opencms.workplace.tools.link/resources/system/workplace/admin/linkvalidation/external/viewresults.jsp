<%@ page import="

	org.opencms.main.*,
	org.opencms.workplace.*,
    org.opencms.relations.*

"%><%	

	// initialize the workplace class
	CmsDialog wp = new CmsDialog(pageContext, request, response);
	
//////////////////// start of switch statement 

	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


//////////////////// ACTION: show start dialog
case CmsDialog.ACTION_DEFAULT:
default:

	wp.setParamAction(CmsDialog.DIALOG_CONFIRMED);

%><%= wp.htmlStart() %>

<%= wp.bodyStart("dialog", "") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<% 
  CmsExternalLinksValidationResult result = OpenCms.getLinkManager().getPointerLinkValidationResult();
  if (result != null) {
    out.println(result.toHtml(wp.getCms().getRequestContext().getLocale()));
    out.println(wp.dialogContentEnd());
    out.println(wp.dialogButtonsOk(" onclick=\"submitAction('cancel', form);\""));
  } else {
    out.println(wp.key("GUI_NO_VALIDATION_YET_0"));
    out.println(wp.dialogContentEnd());
    out.println(wp.dialogButtonsOk(" onclick=\"submitAction('cancel', form);\""));
  }
%>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>