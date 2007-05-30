<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.tools.history.*
"%><%	
	// initialize the workplace class
	CmsAdminHistorySettings wp = new CmsAdminHistorySettings(pageContext, request, response);
		
//////////////////// start of switch statement 
switch (wp.getAction()) {
    case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed
	wp.actionCloseDialog();
	break;

    case CmsAdminHistorySettings.ACTION_SAVE_EDIT:
//////////////////// ACTION: save edited history settings
	wp.actionEdit(request);
	break;

    case CmsDialog.ACTION_DEFAULT:
    default:
//////////////////// ACTION: show history settings dialog (default)
	wp.setParamAction(CmsAdminHistorySettings.DIALOG_SAVE_EDIT);
%>

    <%= wp.htmlStart("administration/index.html") %>
    <%= wp.bodyStart(null) %>

    <%= wp.dialogStart() %>
    <%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" class="nomargin" action="<%= wp.getDialogUri() %>" method="post" onsubmit="submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<% if (wp.getParamFramename()==null) { %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%  } %>

<%= wp.buildSettingsForm() %>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>

<script type="text/javascript">
<!--

function isDigit() {
	return ((event.keyCode >= 48) && (event.keyCode <= 57)) 
}

function checkEnabled() {
	var isEnabled = document.getElementById("enabled").checked;
	if (isEnabled) {
		document.getElementById("settingsSelect").disabled = false;
		document.getElementById("settingsRestore").disabled = false;
		document.getElementById("settingsKeep").disabled = false;
	} else {
		document.getElementById("settingsSelect").disabled = true;
		document.getElementById("settingsRestore").disabled = true;
		document.getElementById("settingsKeep").disabled = true;
	}
	checkDeletedVersionsEnabled();
	updateHiddenFields();
}


function checkDeletedVersionsEnabled() {
	var isChecked = document.getElementById("settingsRestore").checked;
        var isDisabled = document.getElementById("settingsRestore").disabled;
	if (isChecked && !isDisabled) {
		document.getElementById("settingsKeep").disabled = false;
        } else {
		document.getElementById("settingsKeep").disabled = true;
        }
	updateHiddenFields();
}


function updateHiddenFields() {
    var updateRestore = document.getElementById("settingsRestore").checked;
    if (updateRestore) {
        document.getElementById("restoreDeletedHidden").value = "true";
    } else {
        document.getElementById("restoreDeletedHidden").value = "false";
    }
 
    var updateKeep = document.getElementById("settingsKeep").checked;
    if (updateKeep) {
        document.getElementById("versionsDeletedHidden").value = "true";
    } else {
        document.getElementById("versionsDeletedHidden").value = "false";
    }
}

checkEnabled();
checkDeletedVersionsEnabled();
updateHiddenFields();

//-->
</script>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>