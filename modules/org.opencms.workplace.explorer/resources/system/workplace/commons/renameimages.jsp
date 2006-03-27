<%@ page import="org.opencms.workplace.commons.*" %><%	

	// initialize the workplace class
	CmsRenameImages wp = new CmsRenameImages(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsRenameImages.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsRenameImages.ACTION_RENAMEIMAGES:	
case CmsRenameImages.ACTION_WAIT:

//////////////////// ACTION: main rename images action (with wait screen)

	wp.actionRenameImages();

break;


case CmsRenameImages.ACTION_DEFAULT:
default:

//////////////////// ACTION: show rename images dialog (default)

	wp.setParamAction(CmsRenameImages.DIALOG_TYPE);

%><%= wp.htmlStart() %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.buildImageInformation() %>
<%= wp.dialogSpacer() %>

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_RENAMEIMAGES_PREFIX_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_PREFIX %>" type="text" value="<%= wp.getDefaultPrefix() %>" class="maxwidth"></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_RENAMEIMAGES_STARTCOUNT_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_STARTCOUNT %>" type="text" value="<%= wp.getDefaultStartcount() %>" class="maxwidth"></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_RENAMEIMAGES_PLACES_0) %></td>
	<td class="maxwidth"><%= wp.buildSelectPlaces("name=\"" + wp.PARAM_PLACES + "\"") %></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_RENAMEIMAGES_REMOVETITLE_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_REMOVETITLE %>" type="checkbox" value="true" checked="checked"></td>
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