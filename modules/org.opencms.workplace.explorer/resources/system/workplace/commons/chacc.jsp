<%@ page import="
	org.opencms.workplace.CmsDialog,
	org.opencms.workplace.CmsWorkplace,
	org.opencms.workplace.commons.CmsChacc
" %><%
	
	// initialize the workplace class
	CmsChacc wp = new CmsChacc(pageContext, request, response);
	if (wp.getAction() != CmsDialog.ACTION_CANCEL) {
		wp.init();
	}
	boolean displayForm = true;

// perform the users submitted action: create, delete or modify ace
switch (wp.getAction()) {


case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();
	displayForm = false;

break;


case CmsDialog.ACTION_SET:
	wp.actionModifyAce(request);
break;


case CmsChacc.ACTION_DELETE:
	wp.actionRemoveAce();
break;


case CmsChacc.ACTION_ADDACE:
	wp.actionAddAce();
break;

case CmsChacc.ACTION_INTERNALUSE:
	wp.actionInternalUse(request);
break;	
}

if (displayForm) {
%><%= wp.htmlStart() %>
<script type="text/javascript">
<!--

var browseWinPresent = false;

function openDialogWin(url, name) {
	smallwindow = window.open(url, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=no,resizable=no,top=50,left=700,width=350,height=600');
	smallwindow.focus();
	browseWinPresent = true;
	return smallwindow;
}

function closeDialogWin(){
	if(browseWinPresent == true ) {
		window.smallwindow.close();
		browseWinPresent = false;
	}
}

function toggleDetail(id) {

    var element = document.getElementById(id);
    var icon = document.getElementById("ic-"+id);
    var cl = element.className;
    if (cl == "hide") {
        element.className = "show";
        icon.setAttribute("src", '<%= CmsWorkplace.getSkinUri() %>commons/minus.png');
    } else {
        element.className = "hide";
        icon.setAttribute("src", '<%= CmsWorkplace.getSkinUri() %>commons/plus.png');
    }
}

//-->
</script>

<%= wp.bodyStart("dialog", " onunload=\"closeDialogWin();\"") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<!-- ###################################### Error messages ###################################### -->

<%= wp.buildErrorMessages() %>

<!-- ###################################### Detail buttons ###################################### -->

<%= CmsChacc.buildSummaryDetailsButtons(wp) %>

<!-- ###################################### responsible users ###################################### -->

<%= wp.buildResponsibleList(wp.getSettings().getUserSettings().getDialogExpandInheritedPermissions() || wp.getShowInheritedPermissions()) %>

<!-- ###################################### current users rights ###################################### -->

<%= wp.buildCurrentPermissions() %>

<!-- ###################################### Inherited rights list, add form and own rights list ###################################### -->

<%= wp.buildRightsList() %>

<%= wp.dialogContentEnd() %>

<form name="buttons" action="<%= wp.getDialogUri() %>" method="post" class="nomargin">
<input type="hidden" name="<%= CmsDialog.PARAM_ACTION %>" value="<%= CmsDialog.DIALOG_CANCEL %>">
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.dialogButtonsClose() %>
</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<% } %>