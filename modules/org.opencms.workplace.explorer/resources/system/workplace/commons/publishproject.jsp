<%@ page import="
		org.opencms.workplace.CmsDialog,
		org.opencms.workplace.CmsMultiDialog,
		org.opencms.workplace.CmsWorkplace,
		org.opencms.workplace.commons.CmsPublishProject,
		org.opencms.workplace.commons.Messages,
		java.util.List,
		java.util.ArrayList
" %><%	

	// initialize the workplace class
	CmsPublishProject wp = new CmsPublishProject(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();
    break;

case CmsDialog.ACTION_LOCKS_CONFIRMED:
    
    wp.setParamAction(CmsPublishProject.DIALOG_RESOURCES_CONFIRMED); %>
<%= wp.htmlStart("help.explorer.contextmenu.publish") %>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>commons/ajax.js'></script>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>editors/xmlcontent/help.js'></script>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>admin/javascript/general.js'></script>
<script type='text/javascript' src='<%=CmsWorkplace.getSkinUri()%>admin/javascript/list.js'></script>
<script type="text/javascript">
<!--
function showRelatedResources(show){
    var elem = document.getElementById("resourcesreport");
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

var cnfMsgTxt = '';

function doReportUpdate(msg, state) {
   var img = state + ".png";
   var txt = '';
   var elem = document.getElementById("resourcesreport");
   if (state != 'ok') {
      if (state == 'fatal') {
         img = "error.png";
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_GIVEUP_0) %>";
      } else if (state == 'wait') {
         img = "wait.gif";
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0) %>";
      } else if (state == 'error') {
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_ERROR_0) %> " + msg;
      }
   } else {
      elem.innerHTML = msg;
      if (document.forms['main'].result.value == '0') {
        img = "error.png";
        txt = "<%= wp.key(Messages.GUI_PUBLISH_LIST_EMPTY_0) %>";
        state = "error";
      } else {
        showRelatedResources(true);
      }
   }
   if (txt != '') {
      var html = "<table border='0' style='vertical-align:middle; height: 200px;'>";
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
   var isCanContinue = (state == 'ok' && document.forms['main'].result.value != '0');
   okButton.disabled = !isCanContinue;
   if (isCanContinue && state == 'ok') {
      confMsg.innerHTML = cnfMsgTxt;
   } else if (!isCanContinue || state != 'ok') {
      confMsg.innerHTML = '<%= wp.key(Messages.GUI_PUBLISH_LIST_EMPTY_0) %>';
   } else {
      confMsg.innerHTML = '<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0) %>';
   }
}

function reloadDialog(publishSiblings, publishSubresources, relatedResources) {

   document.forms["main"].<%=CmsPublishProject.PARAM_PUBLISHSIBLINGS%>.value=publishSiblings;   
   document.forms["main"].<%=CmsPublishProject.PARAM_SUBRESOURCES%>.value=publishSubresources;   
   document.forms["main"].<%=CmsPublishProject.PARAM_RELATEDRESOURCES%>.value=relatedResources;   
   makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-publishresources.jsp") %>', '<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsPublishProject.PARAM_PUBLISHSIBLINGS%>=' + publishSiblings + '&<%=CmsPublishProject.PARAM_SUBRESOURCES%>=' + publishSubresources + '&<%=CmsPublishProject.PARAM_RELATEDRESOURCES%>=' + relatedResources, 'doReportUpdate');
}

function submitActionWithOptions() {

   if (!submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main')) {
      return;
   }
   document.forms["main"].<%=CmsPublishProject.PARAM_PUBLISHSIBLINGS%>.checked = true;   
   document.forms["main"].<%=CmsPublishProject.PARAM_SUBRESOURCES%>.checked = true;   
   document.forms["main"].<%=CmsPublishProject.PARAM_RELATEDRESOURCES%>.checked = true;   
   return true;
}

// -->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitActionWithOptions();">
<% 
   List excludes = new ArrayList();
   excludes.add(CmsPublishProject.PARAM_PUBLISHSIBLINGS);
   excludes.add(CmsPublishProject.PARAM_SUBRESOURCES);
   excludes.add(CmsPublishProject.PARAM_RELATEDRESOURCES);
%>
<%= wp.paramsAsHidden(excludes) %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.dialogBlockStart(wp.key(Messages.GUI_PUBLISH_RESOURCES_TITLE_0)) %>
<%= wp.dialogWhiteBoxStart() %>
<div id="resourcesreport" >
</div>
<%= wp.dialogWhiteBoxEnd() %>
<%= wp.dialogBlockEnd() %>
<%= wp.buildPublishOptions() %>
<%= wp.buildConfirmation() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel("id='ok-button'", null) %>
</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<script type="text/javascript">
<!--
cnfMsgTxt = document.getElementById('conf-msg').innerHTML;
makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-publishresources.jsp") %>','<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsPublishProject.PARAM_PUBLISHSIBLINGS%>=<%=wp.getParamPublishsiblings()%>&<%=CmsPublishProject.PARAM_SUBRESOURCES%>=<%=wp.getParamSubresources()%>&<%=CmsPublishProject.PARAM_RELATEDRESOURCES%>=<%=wp.getParamRelatedresources()%>', 'doReportUpdate');
// -->
</script>
<%= wp.htmlEnd() %>
<% 
    break;
    
//////////////////// ACTION: other actions handled outside of this JSP
case CmsPublishProject.ACTION_RESOURCES_CONFIRMED:

    wp.setParamAction(CmsPublishProject.DIALOG_TYPE);  %>
<%= wp.htmlStart("help.explorer.contextmenu.publish") %>
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
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0) %>";
      } else if (state == 'error') {
         txt = "<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_ERROR_0) %> " + msg;
      }
   } else {
      elem.innerHTML = msg;
      if (document.forms['main'].result.value == '0') {
        img = state + ".png";
        txt = "<%= wp.key(Messages.GUI_PUBLISH_RELATIONS_NOT_BROKEN_0) %>";
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
   var isCanPublish = <%= wp.isCanPublish() %>;
   okButton.disabled = !isCanPublish;
   if (!isCanPublish && state == 'ok' && txt == '') {
      confMsg.innerHTML = '<%= wp.key(Messages.GUI_PUBLISH_RELATIONS_NOT_ALLOWED_0) %>';
   } else if (isCanPublish || state == 'ok') {
      confMsg.innerHTML = cnfMsgTxt;
      okButton.disabled = false;
   } else {
      confMsg.innerHTML = '<%= wp.key(org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0) %>';
   }
}
// -->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>
<%= wp.buildLockHeaderBox() %>
<%= wp.dialogSpacer() %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<%= wp.dialogBlockStart(wp.key(Messages.GUI_PUBLISH_RELATIONS_TITLE_0)) %>
<%= wp.dialogWhiteBoxStart() %>
<div id="relationsreport" >
</div>
<%= wp.dialogWhiteBoxEnd() %>
<%= wp.dialogBlockEnd() %>
<%= wp.buildConfirmation() %>
<%= wp.dialogContentEnd() %>
<%= wp.dialogButtonsOkCancel("id='ok-button'", null) %>
</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<script type="text/javascript">
<!--
cnfMsgTxt = document.getElementById('conf-msg').innerHTML;
makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-potentialbrokenrelations.jsp") %>','<%=CmsMultiDialog.PARAM_RESOURCELIST%>=<%=wp.getParamResourcelist()%>&<%=CmsDialog.PARAM_RESOURCE%>=<%=wp.getParamResource()%>&<%=CmsPublishProject.PARAM_PUBLISHSIBLINGS%>=<%=wp.getParamPublishsiblings()%>&<%=CmsPublishProject.PARAM_SUBRESOURCES%>=<%=wp.getParamSubresources()%>&<%=CmsPublishProject.PARAM_RELATEDRESOURCES%>=<%=wp.getParamRelatedresources()%>', 'doReportUpdate');
// -->
</script>
<%= wp.htmlEnd() %>
<% 
    break;
 
case CmsPublishProject.ACTION_PUBLISH:
case CmsDialog.ACTION_WAIT:
    
    wp.actionPublish();
    break;
    
case CmsDialog.ACTION_DEFAULT:
default:
%>
<%= wp.buildLockDialog() %>
<% } 
//////////////////// end of switch statement 
%>
