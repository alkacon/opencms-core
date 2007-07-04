<%@ page import="org.opencms.workplace.CmsDialog, 
                 org.opencms.workplace.commons.CmsDeletedResources,
                 org.opencms.workplace.commons.Messages,
		 org.opencms.workplace.commons.CmsDeletedResourcesList,
                 org.opencms.workplace.list.A_CmsListDialog" %><% 

    CmsDeletedResources wp = new CmsDeletedResources(pageContext, request, response);

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

    wp.actionCloseDialog();
    break;

case A_CmsListDialog.ACTION_LIST_MULTI_ACTION:
//////////////////// ACTION: MultiAction executed

    wp.executeListMultiActions();
    break;

case CmsDialog.ACTION_DEFAULT:
default:

    wp.setParamAction(CmsDialog.DIALOG_INITIAL);
%>

<%= wp.htmlStart() %>
<%= wp.buildIncludeJs() %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>
<form name="<%= CmsDeletedResourcesList.LIST_ID %>-form" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, '<%= CmsDeletedResourcesList.LIST_ID %>-form');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.buildAjaxResultContainer(wp.key(Messages.GUI_DELETED_RESOURCES_LIST_TITLE_0)) %>
<%= wp.buildOptions() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtons() %>
</form>
<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.buildReportRequest() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>