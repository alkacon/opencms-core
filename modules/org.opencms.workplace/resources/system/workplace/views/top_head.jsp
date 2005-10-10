<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.help.*,
	org.opencms.jsp.*"

%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsFrameset wp = new CmsFrameset(cms);

	int buttonStyle = wp.getSettings().getUserSettings().getWorkplaceButtonStyle();

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= wp.getStyleUri(wp.getJsp(),"workplace.css")%>">
<title>OpenCms Workplace Head Frame</title>
<script type="text/javascript">
    var pfad="<%= wp.getResourceUri() %>";
    var encoding="<%= wp.getEncoding() %>";

<%	if (wp.isHelpEnabled()) {
		out.println(CmsHelpTemplateBean.buildOnlineHelpJavaScript(wp.getLocale())); 
	}
%>
    
    function loadBody() {
        var link = document.forms.wpViewSelect.wpView.options[document.forms.wpViewSelect.wpView.selectedIndex].value;
        window.top.body.location.href = link;
    }
    
    // this can be removed after legacy views are not longer active
	var helpUrl = ""; 

    function doReload() {
		window.top.location.href = "<%= wp.getWorkplaceReloadUri() %>";
    }
    
    function openwin(url, name, w, h) {
        window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width='+w+',height='+h);
    }     
</script>
</head>

<body class="buttons-head" unselectable="on"<%= wp.isReloadRequired()?" onload=\"loadBody();\"":"" %>>

<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel("label.project") %>

<td>
<form style="margin: 0; padding: 0;" name="wpProjectSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<div>
<%=
	
	wp.getProjectSelect("name=\"wpProject\" onchange=\"document.forms.wpProjectSelect.submit()\"", "style=\"width:150px\"")
        
%>
<input type="hidden" name="<%= wp.PARAM_WP_FRAME %>" value="head">
</div>
</form></td>

<% 
if (wp.isPublishEnabled()) {
	out.println(wp.button("../commons/publishproject.jsp", "body", "publish.png", "button.publish", buttonStyle));
	} else {
	out.println(wp.button(null, null, "publish_in.png", "button.publish", buttonStyle));
}

if (wp.showSiteSelector()) {

%><%= wp.buttonBarSeparator(5, 0) %>          
<%= wp.buttonBarLabel("label.site") %>

<td>
<form style="margin: 0; padding: 0;" name="wpSiteSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<div>
<%= 

	wp.getSiteSelect("name=\"wpSite\" style=\"width:150px\" onchange=\"document.forms.wpSiteSelect.submit()\"")
        
%>
<input type="hidden" name="<%= wp.PARAM_WP_FRAME %>" value="head">
</div>
</form></td>
<% } %>

<%= wp.buttonBarSeparator(5, 0) %>          
<%= wp.buttonBarLabel("label.view") %>

<td>
<form style="margin: 0; padding: 0;" name="wpViewSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<div>
<%= 

	wp.getViewSelect("name=\"wpView\" style=\"width:150px\" onchange=\"document.forms.wpViewSelect.submit()\"")
        
%>
<input type="hidden" name="<%= wp.PARAM_WP_FRAME %>" value="head">
</div>
</form></td>

<%= wp.buttonBarSeparator(5, 0) %>        
<%= wp.button("javascript:doReload()", null, "reload.png", "button.reload", buttonStyle) %>
<%= wp.button("../commons/preferences.jsp", "body", "preferences.png", "button.preferences", buttonStyle) %>

<% 
if (wp.isSyncEnabled()) {
	out.println(wp.button("../commons/synchronize.jsp", "body", "folder_refresh.png", "button.syncfolder", buttonStyle));
}
if (wp.isHelpEnabled()) {
	out.println(wp.button("javascript:openOnlineHelp();", null, "help.png", "button.help", buttonStyle));
}        
%>
       
<td style="width: 100%">&nbsp;</td>
<%= wp.buttonBarSeparator(5, 0) %>  
<%= wp.button("../../login/index.html?logout=true", "_top", "logout.png", "button.exit", buttonStyle) %>

<% if (buttonStyle != 2) {%>
<td>&nbsp;</td>
<td><span style="display: block; width: 80px; height: 22px; background-image: url('<%= wp.getSkinUri() %>commons/workplace.png'); "></span></td>
<% } %>
<%= wp.buttonBar(wp.HTML_END) %>

</body>
</html>