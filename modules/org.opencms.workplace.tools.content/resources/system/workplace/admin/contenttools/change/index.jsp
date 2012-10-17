<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.tools.content.*,
	org.opencms.util.*
"%><%	

	// initialize the workplace class
	CmsPropertyChange wp = new CmsPropertyChange(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;

case CmsDialog.ACTION_OK:		
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main change property value action (with optional wait and final result screen)

	wp.actionChange();
	
	if (wp.getAction() == CmsPropertyChange.ACTION_SHOWRESULT) {
//////////////////// ACTION: show change result
	wp.setParamAction(CmsDialog.DIALOG_CANCEL);
%><%= wp.htmlStart(null) %>
<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_RESULT_0) %> "<%= CmsStringUtil.escapeHtml(wp.getParamNewValue()) %>":
<%= wp.dialogSpacer() %>

<%= wp.dialogWhiteBoxStart() %>
<div style="height: <%= wp.getResultListHeight() %>px; overflow: auto;">
<%= wp.buildResultList() %>
</div>
<%= wp.dialogWhiteBoxEnd() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsClose() %>
</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%	
	}

break;


case CmsDialog.ACTION_DEFAULT:
default:

//////////////////// ACTION: show property change dialog (default)

	wp.setParamAction(CmsDialog.DIALOG_OK);

%><%= wp.htmlStart(null) %>
<%= wp.bodyStart("dialog", "onunload=\"top.closeTreeWin();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%= wp.dialogBlockStart(null) %>
<ul>
<li><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_INFO_0) %></li>
<li><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_INFO2_0) %></li>
<li><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_INFO3_0) %></li>
</ul>
<%= wp.dialogBlockEnd() %>
<%= wp.dialogSpacer() %>

<% if(wp.hasValidationErrors()) { %>
<%= wp.dialogBlockStart(null) %>
<table border="0" cellpadding="4" cellspacing="0">
	<tr>
		<td style="vertical-align: middle;"><img src="<%= CmsWorkplace.getSkinUri() %>commons/error.png" border="0"></td>
		<td style="vertical-align: middle;"><%= wp.getErrorMessage() %></td> 
	</tr> 
</table>
<%= wp.dialogBlockEnd() %>
<%= wp.dialogSpacer() %>
<% } %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<input type="hidden" name="action" value="<%=wp.getParamAction()%>">
<input type="hidden" name="framename" value="admin_content">
<input type="hidden" name="title" value="<%=wp.getParamTitle()%>">
<input type="hidden" name="dialogtype" value="<%= CmsPropertyChange.DIALOG_TYPE%>">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_RESOURCE_0) %></td>
	<td class="maxwidth"><input name="<%= CmsDialog.PARAM_RESOURCE %>" type="text" value="<%= wp.getParamResource()==null?"":wp.getParamResource() %>" class="maxwidth" style="width: 99%"></td>
	<td><%=wp.button("javascript:top.openTreeWin('param_resource', true, 'main', '"+CmsDialog.PARAM_RESOURCE+"', document);", null, "folder",org.opencms.workplace.tools.content.Messages.GUI_BUTTON_SEARCH_0 , 0)%></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_RECURSIVE_0) %></td>
	<td colspan="2" class="maxwidth"><input name="<%= CmsPropertyChange.PARAM_RECURSIVE %>" type="checkbox" value="true"<% if ("true".equals(wp.getParamRecursive())) { out.print("checked=\"checked\""); } %>></td>    
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTY_0)%></td>
	<td colspan="2" class="maxwidth"><%= wp.buildSelectProperty("name=\"" + CmsPropertyChange.PARAM_PROPERTYNAME + "\" size=\"1\"") %></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYCHANGE_OLDVALUE_0)%></td>
	<td colspan="2" class="maxwidth"><input name="<%= CmsPropertyChange.PARAM_OLDVALUE %>" type="text" value="<%= wp.getParamOldValue()==null?"":wp.getParamOldValue() %>" class="maxwidth" style="width: 99%"></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_EDITPROPERTYINFO_0) %></td>
	<td colspan="2" class="maxwidth"><input name="<%= CmsPropertyChange.PARAM_NEWVALUE %>" type="text" value="<%= wp.getParamNewValue()==null?"":wp.getParamNewValue() %>" class="maxwidth" style="width: 99%"></td>
</tr>
</table>

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