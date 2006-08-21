<%@ page import="org.opencms.workflow.*" %><%	

	// initialize the workplace class
	CmsWorkflow wp = new CmsWorkflow(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsWorkflow.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsWorkflow.ACTION_CONFIRMED:
case CmsWorkflow.ACTION_WAIT:
//////////////////// ACTION: main init workflow action

	wp.performAction();

break;

case CmsWorkflow.ACTION_INIT:
//////////////////// ACTION: show init dialog

	wp.checkNotInWorkflow();
	wp.setParamAction(CmsWorkflow.DIALOG_CONFIRMED);

%><%= wp.htmlStart("help.explorer.contextmenu.init_workflow") %>
<script type="text/javascript">
<!--
function toggleDetail(id) {
    var element = document.getElementById(id);
    var cl = element.className;
    if (cl == "hide") {
        element.className = "show";
    } else {
        element.className = "hide";
    }
}
//-->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { %>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { %>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogSpacer() %>
<table border="0" width="100%">
<tr>
	<td colspan="2" style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_WORKFLOW_NEW_TASK_0) %></td>
</tr>
</table>

<%= wp.dialogSpacer() %>
<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_WORKFLOW_TYPE_0) %></td>
	<td class="maxwidth"><%= wp.buildTasktypeSelector() %></td>
</tr>
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_WORKFLOW_DESCRIPTION_0) %></td>
	<td class="maxwidth"><input name="<%= wp.PARAM_DESCRIPTION %>" type="text" value="" class="maxwidth"></td>
</tr>
</table>

<%= wp.dialogSpacer() %>

<table border="0" width="100%">
<tr><td>
<input type="checkbox" name="sendmail" value="true" onclick="toggleDetail('mailtext');" checked="checked"> <%= wp.key(Messages.GUI_WORKFLOW_SEND_MAIL_0) %><br>&nbsp;<br>
<span id="mailtext" class="show" style="width: 100%;">
<%= wp.key(Messages.GUI_WORKFLOW_MAILTEXT_0) %><br>
<textarea name="mailtext" id="mailtext" rows="5" wrap="physical" style="overflow: auto; width: 100%;"></textarea>
</span>
</tr>
</table>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>

<%
break; 

case CmsWorkflow.ACTION_SIGNAL:
//////////////////// ACTION: show signal dialog

	wp.checkInWorkflow();
	wp.setParamAction(CmsWorkflow.DIALOG_CONFIRMED);

%><%= wp.htmlStart("help.explorer.contextmenu.init_workflow") %>
<script type="text/javascript">
<!--
function toggleDetail(id) {
    var element = document.getElementById(id);
    var cl = element.className;
    if (cl == "hide") {
        element.className = "show";
    } else {
        element.className = "hide";
    }
}
//-->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { %>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { %>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogSpacer() %>
<table border="0" width="100%">
<tr>
	<td colspan="2" style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_WORKFLOW_FORWARD_TASK_1, new Object[] {wp.buildTaskInfo()}) %></td>
</tr>
</table>

<%= wp.dialogSpacer() %>

<table border="0" width="100%">
<tr>
	<td style="white-space: nowrap;" unselectable="on"><%= wp.key(Messages.GUI_WORKFLOW_TRANSITION_0) %></td>
	<td class="maxwidth"><%= wp.buildTransitionSelector() %></td>
</tr>
</table>

<%= wp.dialogSpacer() %>

<table border="0" width="100%">
<tr><td>
<input type="checkbox" name="sendmail" value="true" onclick="toggleDetail('mailtext');" checked="checked"> <%= wp.key(Messages.GUI_WORKFLOW_SEND_MAIL_0) %><br>&nbsp;<br>
<span id="mailtext" class="show" style="width: 100%;">
<%= wp.key(Messages.GUI_WORKFLOW_MAILTEXT_0) %><br>
<textarea name="mailtext" id="mailtext" rows="5" wrap="physical" style="overflow: auto; width: 100%;"></textarea>
</span>
</tr>
</table>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>

<%
break;

case CmsWorkflow.ACTION_ABORT:
//////////////////// ACTION: show abort dialog

	wp.checkInWorkflow();
	wp.setParamAction(CmsWorkflow.DIALOG_CONFIRMED);

%><%= wp.htmlStart("help.explorer.contextmenu.init_workflow") %>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { %>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { %>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= wp.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= wp.PARAM_FRAMENAME %>" value="">

<%= wp.dialogSpacer() %>

<table border="0" width="100%">
<tr>
	<td><%= wp.key(Messages.GUI_WORKFLOW_ABORT_1, new Object[] { wp.buildTaskInfo() }) %></td>
</tr>
</table>

<%= wp.dialogSpacer() %>

<table border="0" width="100%">
<tr><td>
<input type="checkbox" name="<%= wp.PARAM_UNDO %>" value="true"> <%= wp.key(Messages.GUI_WORKFLOW_UNDO_0) %>
</td></tr>
</table>

<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>

<%
break; 

} 
//////////////////// end of switch statement 
%>