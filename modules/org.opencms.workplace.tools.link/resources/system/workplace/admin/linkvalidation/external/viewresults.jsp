<%@ page import="

	org.opencms.main.*,
	org.opencms.workplace.*,
    org.opencms.validation.*,
	org.opencms.workplace.tools.workplace.*

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

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<% 
  CmsPointerLinkValidationResult result = OpenCms.getLinkManager().getPointerLinkValidationResult();
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