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

case CmsDialog.ACTION_LOCKS_CONFIRMED:

//////////////////// ACTION: show delete dialog (default)

	wp.setParamAction(CmsDelete.DIALOG_TYPE);

%><%= wp.htmlStart("help.explorer.contextmenu.delete") %>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>commons/ajax.js'></script>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>editors/xmlcontent/help.js'></script>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>admin/javascript/general.js'></script>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>admin/javascript/list.js'></script>
<script type="text/javascript">
<!--
function showBrokenLinks(show){
    var elem = document.getElementById("relationsreport");
	var elements = elem.getElementsByTagName('td');
	for(var i = 0; i < elements.length; i++){
		var node = elements.item(i);
		for(var j = 0; j < node.attributes.length; j++) {
			if(node.attributes.item(j).nodeName == 'class') {
				if(node.attributes.item(j).nodeValue == 'listdetailhead' || node.attributes.item(j).nodeValue == 'listdetailitem') {
				    // node found, now find the parent row
				    for (var k = 0; k < 5; k++) {
				      node = node.parentNode;				      
				    }
				    // change the class of the parent row
				    if (!show) {
						eval("node.className = node.className + ' hide'");						
					} else {
					    if (eval("node.className.substring(0,4)") == 'even') {
							eval("node.className = 'evenrowbg'");
						} else {
							eval("node.className = 'oddrowbg'");
						}
					}
				}
			}
		}
	}
	if (show) {
	   document.getElementById("drs").className = 'hide';
	   document.getElementById("drh").className = 'link';
	} else {
	   document.getElementById("drs").className = 'link';
	   document.getElementById("drh").className = 'hide';
	}
}

function reloadDialog(deleteSiblings) {

   document.forms["main"].<%=CmsDelete.PARAM_DELETE_SIBLINGS%>.value=deleteSiblings;   
   makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-brokenrelations.jsp") %>', '<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsDelete.PARAM_DELETE_SIBLINGS%>=' + deleteSiblings, 'doReportUpdate');
}

var cnfMsgTxt = '';

function doReportUpdate(msg, state) {
   var img = state + ".png";
   var txt = '';
   var elem = document.getElementById("relationsreport");
   if (state != 'ok') {
      if (state == 'fatal') {
         img = "error.png";
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_GIVEUP_0) %>";
      } else if (state == 'wait') {
         img = "wait.gif";
         txt = "<%= wp.key(Messages.GUI_DELETE_RELATIONS_REPORT_WAIT_0) %>";
      } else if (state == 'error') {
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_ERROR_0) %> " + msg;
      }
   } else {
      elem.innerHTML = msg;
      if (document.forms['main'].result.value == '0') {
        img = state + ".png";
        txt = "<%= wp.key(Messages.GUI_DELETE_RELATIONS_NOT_BROKEN_0) %>";
      } else {
        showBrokenLinks(true);
      }
   }
   if (txt != '') {
      var html = "<table border='0' style='vertical-align:middle; height: 150px;'>";
      html += "<tr><td width='40' align='center' valign='middle'><img src='<%= CmsWorkplace.getSkinUri() %>commons/";
      html += img;
      html += "' width='32' height='32' alt=''></td>";
      html += "<td valign='middle'><span style='color: #000099; font-weight: bold;'>";
      html += txt;
      html += "</span><br></td></tr></table>";
      elem.innerHTML = html;
   }

   var okButton = document.getElementById("ok-button");
   var confMsg = document.getElementById('conf-msg');
   var isCanDelete = <%= wp.isCanDelete() %>;
   okButton.disabled = !isCanDelete;
   if (!isCanDelete && state == 'ok' && txt == '') {
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
    %><%@ include file="/system/workplace/commons/includes/multiresourcelist.txt" %><%
} else { 
    // include resource information
	%><%@ include file="/system/workplace/commons/includes/resourceinfo.txt" %><%
} %>

<%= wp.dialogSpacer() %>

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
cnfMsgTxt = document.getElementById('conf-msg').innerHTML;
makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-brokenrelations.jsp") %>', '<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsDelete.PARAM_DELETE_SIBLINGS%>=<%=wp.getParamDeleteSiblings()%>', 'doReportUpdate');
//-->
</script>
<%= wp.htmlEnd() %>
<% 
   break;

case CmsDialog.ACTION_DEFAULT:
default:
%>
<%= wp.buildLockDialog() %>
<% } 
//////////////////// end of switch statement 
%>