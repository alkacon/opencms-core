<%@ page 
	buffer="none" 
	import="org.opencms.workplace.tools.workplace.rfsfile.*,org.opencms.workplace.CmsDialog" %><%	

	// initialize the workplace class
	CmsRfsFileDownloadDialog wp = new CmsRfsFileDownloadDialog(pageContext, request, response);
		
//////////////////// start of switch statement 

switch (wp.getAction()) {
    case CmsDialog.ACTION_CANCEL:

//////////////////// ACTION: cancel button pressed
      
        wp.actionCloseDialog();
        break;

//////////////////// ACTION: construct this page:

    case CmsDialog.ACTION_DEFAULT:
    default:
	wp.setParamAction(wp.DIALOG_CANCEL);

%><%= wp.htmlStart("administration/index.html") %>

<%= wp.bodyStart(null) %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<% if (wp.getParamFramename()==null) { %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
<%  } %>


<%= wp.buildDownloadFileView() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>

<%= wp.createDownloadScript() %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>