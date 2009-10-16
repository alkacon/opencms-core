<%@ page import="
		org.opencms.workplace.CmsDialog,
		org.opencms.workplace.CmsMultiDialog,
		org.opencms.workplace.CmsWorkplace,
		org.opencms.workplace.commons.CmsPublishProject,
		org.opencms.workplace.commons.CmsProgressWidget,
		org.opencms.workplace.commons.CmsPublishResourcesList,
		org.opencms.workplace.commons.Messages,
		java.util.List,
		java.util.ArrayList,
		org.opencms.util.*,
		org.opencms.file.*
" %><%	

	// initialize the workplace class
	CmsPublishProject wp = new CmsPublishProject(pageContext, request, response);
	
	// set the select project as current project
	CmsProject oldProject = wp.getCms().getRequestContext().currentProject();
	String projectId = wp.getParamProjectid();
	wp.getCms().getRequestContext().setCurrentProject(wp.getCms().readProject(projectId));
//////////////////// start of switch statement 
	
switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();
    break;

case CmsDialog.ACTION_LOCKS_CONFIRMED:
    
    wp.setParamAction(CmsPublishProject.DIALOG_RESOURCES_CONFIRMED); %>
<%= wp.htmlStart("help.explorer.contextmenu.publish") %>

<% 
	wp.getProgress().setJsFinishMethod("setList"); 
	wp.getProgress().setShowWaitTime(5000);
%>
<%= wp.getProgress().getJsIncludes() %>

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

function setList() {

   // hide progress bar
   document.getElementById("progress").style.display = "none";

   var elem = document.getElementById("resourcesreport");   
   elem.innerHTML = progressResult;

   if (document.forms['main'].result.value == '0') {
     img = "error.png";
     txt = "<%= wp.key(Messages.GUI_PUBLISH_LIST_EMPTY_0) %>";
     
     var html = "<table border='0' style='vertical-align:middle; height: 200px;'>";
     html += "<tr><td width='40' align='center' valign='middle'><img src='<%= CmsWorkplace.getSkinUri() %>commons/";
     html += img;
     html += "' width='32' height='32' alt=''></td>";
     html += "<td valign='middle'><span style='color: #000099; font-weight: bold;'>";
     html += txt;
     html += "</span><br></td></tr></table>";
     elem.innerHTML = html;
     
   } else {
     showRelatedResources(true);
     
     var okButton = document.getElementById("ok-button");
     if (okButton != null) {
       okButton.disabled = false;
     }
   }
}

function reloadReport() {

   var elem = document.getElementById("resourcesreport");
   elem.innerHTML = "";
   
   // show progress bar
   resetProgressBar();
   document.getElementById("progressbar_bar").parentNode.style.display = "none";
   document.getElementById("progressbar_percent").style.display = "none";
   document.getElementById("progressbar_wait").style.display = "block";
   
   document.getElementById("progress").style.display = "block";   
     
   restartProgress();
}

function restartProgress() {

	var publishSiblings = document.forms["main"].<%=CmsPublishProject.PARAM_PUBLISHSIBLINGS%>.checked;
	var publishSubresources = document.forms["main"].<%=CmsPublishProject.PARAM_SUBRESOURCES%>.checked;
	var relatedResources = document.forms["main"].<%=CmsPublishProject.PARAM_RELATEDRESOURCES%>.checked;   
   
	if (progressState > 0) {
		progressState = 2;
		window.setTimeout("restartProgress()", <%= wp.getProgress().getRefreshRate() %>);
		return;
	}
	progressState = 1;
	<%
    String params = "action=resourcereport"
    	+ "&" 
        + CmsMultiDialog.PARAM_RESOURCELIST 
        + "=" 
        + wp.getParamResourcelist()
        + "&"
        + CmsDialog.PARAM_RESOURCE
        + "="
        + wp.getParamResource()
        + "&"
        + CmsPublishProject.PARAM_PUBLISHSIBLINGS
        + "='+"
        + "publishSiblings+'" 
        + "&"
        + CmsPublishProject.PARAM_SUBRESOURCES
        + "='+" 
        + "publishSubresources+'" 
        + "&" 
        + CmsPublishProject.PARAM_RELATEDRESOURCES
        + "='+"
        + "relatedResources+'" 
        + "&" 
        + CmsProgressWidget.PARAMETER_KEY
        + "="
        + wp.getProgress().getKey()
        + "&"
        + CmsProgressWidget.PARAMETER_SHOWWAITTIME
        + "="
        + wp.getProgress().getShowWaitTime()
        + "&"
        + CmsProgressWidget.PARAMETER_REFRESHRATE
        + "=" 
        + wp.getProgress().getRefreshRate();
	params = CmsStringUtil.substitute(params, "<", "&lt;");
	params = CmsStringUtil.substitute(params, ">", "&gt;");
	%>
	makeRequest('<%= wp.getJsp().link("/system/workplace/commons/report-publishresources.jsp") %>', '<%= params %>', 'updateProgressBar');
}

// -->
</script>
<%= wp.bodyStart("dialog") %>

<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<% 
   List excludes = new ArrayList();
   excludes.add(CmsPublishProject.PARAM_PUBLISHSIBLINGS);
   excludes.add(CmsPublishProject.PARAM_SUBRESOURCES);
   excludes.add(CmsPublishProject.PARAM_RELATEDRESOURCES);
%>
<%= wp.paramsAsHidden(excludes) %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">
<input type="hidden" name="<%= CmsProgressWidget.PARAMETER_KEY %>" value="<%= wp.getProgress().getKey() %>">
<%= wp.dialogBlockStart(wp.key(Messages.GUI_PUBLISH_RESOURCES_TITLE_0)) %>
<%= wp.dialogWhiteBoxStart() %>

<div id="resourcesreport" ></div>

<div id="progress">
	<table border="0" align="center" style="vertical-align:middle; height: 200px;">
		<tr>
			<td valign="middle">
<%
	CmsPublishResourcesList list = wp.getPublishResourcesList();
    if (list != null) {
	   wp.getProgress().startProgress(list); 
 %><%= wp.getProgress().getWidget() %><% 
    } %>
			</td>
		</tr>
	</table>
</div>

<%= wp.dialogWhiteBoxEnd() %>
<%= wp.dialogBlockEnd() %>
<%= wp.buildPublishOptions() %>
<%= wp.buildConfirmation() %>
<%= wp.dialogButtonsOkCancel("id='ok-button' onclick=\"this.disabled=true; document.forms['main'].submit(); \" disabled", null) %>
</form>

<%= wp.dialogContentEnd() %>
<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<script type="text/javascript">
<!--
cnfMsgTxt = document.getElementById('conf-msg').innerHTML;
// -->
</script>
<%= wp.htmlEnd() %>
<% 
    break;
    
//////////////////// ACTION: other actions handled outside of this JSP
case CmsPublishProject.ACTION_RESOURCES_CONFIRMED:

    wp.setParamAction(CmsPublishProject.DIALOG_TYPE);  %>
<%= wp.htmlStart("help.explorer.contextmenu.publish") %>

<% wp.getProgress().setJsFinishMethod("setList"); %>
<%= wp.getProgress().getJsIncludes() %>

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

function setList() {

   var elem = document.getElementById("relationsreport");
   elem.style.display = "none";
   elem.innerHTML = progressResult;
   
   if (document.forms['main'].result.value == '0') {
    
     document.forms['main'].submit();
     
   } else {
     // hide progress bar
     document.getElementById("progress").style.display = "none";

     elem.style.display = "block";
     showBrokenLinks(true);
     
     var okButton = document.getElementById("ok-button");
     if (okButton != null) {
       okButton.disabled = false;
     }
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

<div id="relationsreport" ></div>

<div id="progress">
	<table border="0" align="center" style="vertical-align:middle; height: 200px;">
		<tr>
			<td valign="middle">
				<%= wp.getProgress().getWidget() %> 
			</td>
		</tr>
	</table>
</div>
<%= wp.dialogWhiteBoxEnd() %>
<%= wp.dialogBlockEnd() %>
<%= wp.buildConfirmation() %>
<%= wp.dialogButtonsOkCancel("id='ok-button' onclick=\"this.disabled=true; document.forms['main'].submit(); \"", null) %>
</form>

<%= wp.dialogContentEnd() %>
<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>

<%= wp.htmlEnd() %>
<% 
    break;

case CmsPublishProject.ACTION_PUBLISH:
case CmsDialog.ACTION_WAIT:
    
    try {
    	wp.actionPublish();
    } finally {
    	// set back the "old" current project as current project 
	wp.getCms().getRequestContext().setCurrentProject(oldProject);
    }
    break;
    
case CmsDialog.ACTION_DEFAULT:
default:
%>
<%= wp.buildLockDialog() %>
<% }
// set back the "old" current project as current project 
wp.getCms().getRequestContext().setCurrentProject(oldProject);
 
//////////////////// end of switch statement 
%>