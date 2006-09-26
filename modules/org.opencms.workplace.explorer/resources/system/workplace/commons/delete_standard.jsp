<%@ page import="
		org.opencms.workplace.CmsDialog,
		org.opencms.workplace.CmsWorkplace,
		org.opencms.workplace.commons.CmsDelete,
		java.util.Collections
" %><%	

	// initialize the workplace class
	CmsDelete wp = new CmsDelete(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();
	break;

case CmsDelete.ACTION_DELETE:
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main delete action (with optional confirm / wait screen)

	wp.actionDelete();
	break;

case CmsDialog.ACTION_DEFAULT:
default:

//////////////////// ACTION: show delete dialog (default)

	wp.setParamAction("delete");

%><%= wp.htmlStart("help.explorer.contextmenu.delete") %>
<script type="text/javascript">
<!--

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

function reloadDialog(deleteSiblings) {

   document.forms["reloadform"].<%=CmsDelete.PARAM_DELETE_SIBLINGS%>.value=deleteSiblings;
   document.forms["reloadform"].submit();
}

function printBrokenRelations() {

   document.forms["reloadform"].action = "<%= wp.getJsp().link("/system/workplace/commons/print-brokenrelations.jsp") %>";
   document.forms["reloadform"].target = "print-brokenrelations";
   document.forms["reloadform"].submit();
}

//-->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { 
    // include resource list
    %><%@ include file="includes/multiresourcelist.txt" %><%
} else { 
    // include resource information
	%><%@ include file="includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

<form name="reloadform" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" >
<%= wp.paramsAsHidden(Collections.singleton(CmsDialog.PARAM_ACTION)) %>
</form>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.buildRelations(false) %>
<%= wp.buildDeleteSiblings() %>
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