<%@ page import= "
	org.opencms.workplace.tools.content.*, 
	org.opencms.workplace.*,
	java.util.*
"%><%	

	// initialize the workplace class
	CmsPropertyDelete wp = new CmsPropertyDelete(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;

case CmsDialog.ACTION_OK:		

//////////////////// ACTION: main delete property definition action

	List resourcesWithProperty = wp.getCms().readResourcesWithProperty(wp.getParamPropertyName());
   if (resourcesWithProperty.isEmpty()) {  
		// property is not linked with any resource
		wp.actionDelete();
	} else {
		wp.setParamAction(CmsPropertyDelete.DIALOG_DELETE_CASCADE);

%><%= wp.htmlStart(null) %>
<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%= wp.dialogBlockStart(null) %>
<%= wp.key(  org.opencms.workplace.tools.content.Messages.GUI_MESSAGE_DELETEPROPERTY_0)%>
<%= wp.dialogBlockEnd() %>
<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">

<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.dialogWhiteBoxStart() %>
<%= wp.buildResourceList() %>
<%= wp.dialogWhiteBoxEnd() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%	   
	}
	
break;

case CmsPropertyDelete.ACTION_DELETE_CASCADE:		

//////////////////// ACTION: cascade delete properties on resources the delete property definitions

		wp.actionDeleteCascade();
		// wp.actionDelete();
	
break;

case CmsDialog.ACTION_DEFAULT:
default:

//////////////////// ACTION: show property definition delete dialog (default)

	wp.setParamAction(CmsDialog.DIALOG_OK);

%><%= wp.htmlStart(null) %>
<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%= wp.dialogBlockStart(null) %>
<%= wp.key(  org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTYDELETE_INFO_0)%>
<%= wp.dialogBlockEnd() %>
<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">

<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(org.opencms.workplace.tools.content.Messages.GUI_INPUT_PROPERTY_0) %></td>
	<td colspan="2" class="maxwidth"><%= wp.buildSelectProperty("name=\"" + CmsPropertyDelete.PARAM_PROPERTYNAME + "\" size=\"1\"") %></td>
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