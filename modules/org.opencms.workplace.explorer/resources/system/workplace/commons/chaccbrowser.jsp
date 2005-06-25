<%@ page import="org.opencms.workplace.commons.*" %><%
	
	// initialize the workplace class
	CmsChaccBrowser wp = new CmsChaccBrowser(pageContext, request, response);
	
//////////////////// start of switch statement 
	
switch (wp.getFrame()) {

case CmsChaccBrowser.FRAME_GROUPS:
//////////////////// FRAME: groups iframe is displayed
%>
<%= wp.htmlStart() %>
<%= wp.bodyStart(null, "style=\"margin: 3px; background-color: #fff;\"") %>
<%= wp.buildGroupList() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;

case CmsChaccBrowser.FRAME_USERS:
//////////////////// FRAME: users iframe is displayed
%>
<%= wp.htmlStart() %>
<%= wp.bodyStart(null, "style=\"margin: 3px; background-color: #fff;\"") %>
<%= wp.buildUserList() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
break;

case CmsChaccBrowser.FRAME_DEFAULT:
default:
//////////////////// FRAME: main popup window is displayed
	
%>
<%= wp.htmlStart(null, wp.key("dialog.permission.headline.add")) %>

<script type="text/javascript">
<!--

function selectForm(type, name) {
	if (window.opener.document.forms["add"] != null) {
		window.opener.document.forms["add"].name.value = name;
		window.opener.document.forms["add"].type.value = type;
		window.close();
	}	
}

//-->
</script>
<%= wp.bodyStart("dialog dialogpopup") %>

<%= wp.dialogSubheadline(wp.key("dialog.permission.browse.groups")) %>
<iframe src="<%= wp.getDialogUri() %>?<%= wp.PARAM_FRAME %>=<%= wp.DIALOG_FRAME_GROUPS %>" width="100%" height="240" frameborder="1"></iframe>

<%= wp.dialogSubheadline(wp.key("dialog.permission.browse.users")) %>
<iframe src="<%= wp.getDialogUri() %>?<%= wp.PARAM_FRAME %>=<%= wp.DIALOG_FRAME_USERS %>" width="100%" height="240" frameborder="1"></iframe>

<div>&nbsp;</div>

<%= wp.dialogButtonsClose("style=\"margin-left: 0;\" onclick=\"window.close();\"") %>

<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<%
} 
//////////////////// end of switch statement 
%>