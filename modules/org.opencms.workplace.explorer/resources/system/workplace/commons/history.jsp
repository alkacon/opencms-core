<%@ page import="org.opencms.workplace.commons.*,org.opencms.jsp.CmsJspActionElement,org.opencms.file.CmsBackupResourceHandler" %>
<%	

	// initialize the workplace class
	CmsHistory wp = new CmsHistory(pageContext, request, response);
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);	

//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsHistory.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsHistory.ACTION_RESTORE_VERSION:

//////////////////// ACTION: restore action

	wp.actionRestore();
	wp.actionCloseDialog();

break;


case CmsHistory.ACTION_DEFAULT:
default:

//////////////////// ACTION: show history dialog (default)

	wp.setParamAction(wp.DIALOG_RESTORE);

%>
<%= wp.htmlStart(null, wp.getParamTitle()) %>
<script type="text/javascript">
<!--
	function restore(versionid) {
		if(confirm("<%=wp.key(Messages.GUI_HISTORY_CONFIRMATION_0)%>")) {
			document.main.<%= CmsBackupResourceHandler.PARAM_VERSIONID %>.value = versionid;
			document.main.submit();	
		}	
	}
	
	function viewVersion(resourcename, versionid) {
		
		window.open("<%=cms.link(CmsBackupResourceHandler.BACKUP_HANDLER) %>" + resourcename+"?<%= CmsBackupResourceHandler.PARAM_VERSIONID %>="+versionid,'version','scrollbars=yes, resizable=yes, width=800, height=600');
	}
//-->
</script>
<%= wp.bodyStart("dialog", null) %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<%@ include file="includes/resourceinfo.txt" %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">
<input type="hidden" name="<%= CmsBackupResourceHandler.PARAM_VERSIONID %>" value="">

<%= wp.dialogWhiteBoxStart() %>
<%= wp.buildVersionList() %>
<%= wp.dialogWhiteBoxEnd() %>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsClose() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>