<%@ page import="
		org.opencms.workplace.CmsDialog,
		org.opencms.workplace.CmsMultiDialog,
		org.opencms.workplace.CmsWorkplace,
		org.opencms.workplace.commons.CmsDelete,
		org.opencms.workplace.commons.Messages,
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

   document.forms["printform"].<%=CmsDelete.PARAM_DELETE_SIBLINGS%>.value=deleteSiblings;   
   top.makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-brokenrelations.jsp") %>?<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsDelete.PARAM_DELETE_SIBLINGS%>=' + deleteSiblings, 'body.explorer_body.explorer_files.doReportUpdate');
}

var cnfMsgTxt = '';

function doReportUpdate(msg, state) {
   var elem = document.getElementById("relationsreport");
   if (state != 'ok' || msg == '') {
      var img = state + ".png";
      var txt = msg;
      if (state == 'fatal') {
         img = "error.png";
         txt = "<%= wp.key(Messages.GUI_DELETE_RELATIONS_REPORT_GIVEUP_0) %>";
      } else if (state == 'wait') {
         img = "wait.gif";
         txt = "<%= wp.key(Messages.GUI_DELETE_RELATIONS_REPORT_WAIT_0) %>";
      } else if (state == 'error') {
         txt = "<%= wp.key(Messages.GUI_DELETE_RELATIONS_REPORT_ERROR_0) %> " + msg;
      } else {
         txt = "<%= wp.key(Messages.GUI_DELETE_RELATIONS_NOT_BROKEN_0) %>";
      }
      var html = "<table border='0' style='vertical-align:middle; height: 100px;'>";
      html += "<tr><td width='40' align='center' valign='middle'><img src='<%= CmsWorkplace.getSkinUri() %>commons/";
      html += img;
      html += "' width='32' height='32' alt=''></td>";
      html += "<td valign='middle'><span style='color: #000099; font-weight: bold;'>";
      html += txt;
      html += "</span><br></td></tr></table>";
      elem.innerHTML = html;
   } else {
      elem.innerHTML = msg;
   }

   var okButton = document.getElementById("ok-button");
   var confMsg = document.getElementById('conf-msg');
   var isCanDelete = <%= wp.isCanDelete() %>;
   okButton.disabled = !isCanDelete;
   if (!isCanDelete && state == 'ok' && msg != '') {
      confMsg.innerHTML = '<%= wp.key(Messages.GUI_DELETE_RELATIONS_NOT_ALLOWED_0) %>';
   } else if (isCanDelete || state == 'ok') {
      confMsg.innerHTML = cnfMsgTxt;
      okButton.disabled = false;
   } else {
      confMsg.innerHTML = '<%= wp.key(Messages.GUI_DELETE_RELATIONS_REPORT_WAIT_0) %>';
   }
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

<form name="printform" action="<%= wp.getJsp().link("/system/workplace/commons/print-brokenrelations.jsp") %>" method="post" class="nomargin" target="print-brokenrelations" >
<%= wp.paramsAsHidden(Collections.singleton(CmsDialog.PARAM_ACTION)) %>
</form>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.dialogBlockStart(wp.key(Messages.GUI_DELETE_RELATIONS_TITLE_0)) %>
<%= wp.dialogWhiteBoxStart() %>
<div id="relationsreport" >
</div>
<%= wp.dialogWhiteBoxEnd() %>
<%= wp.dialogBlockEnd() %>
&nbsp;<br>
<%= wp.buildDeleteSiblings() %>
<%= wp.buildConfirmation() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel("id='ok-button'", null) %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<script type="text/javascript">
<!--
var confMsg = document.getElementById('conf-msg');
cnfMsgTxt = confMsg.innerHTML;
top.makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-brokenrelations.jsp") %>?<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsDelete.PARAM_DELETE_SIBLINGS%>=<%=wp.getParamDeleteSiblings()%>', 'body.explorer_body.explorer_files.doReportUpdate');
//-->
</script>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>