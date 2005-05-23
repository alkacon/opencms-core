<%@ page import="
	org.opencms.workplace.*,
	org.opencms.jsp.*"
	buffer="none"

%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsFrameset wp = new CmsFrameset(cms);

	int buttonStyle = wp.getSettings().getUserSettings().getWorkplaceButtonStyle();

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= wp.getStyleUri(wp.getJsp(),"workplace.css")%>">

<script type="text/javascript">
    var pfad="<%= wp.getResourceUri() %>";
    var encoding="<%= wp.getEncoding() %>";
    
    function loadBody() {
        var link = document.forms.wpViewSelect.wpView.options[document.forms.wpViewSelect.wpView.selectedIndex].value;
        window.top.body.location.href = link;
    }
    
    var localePrefix = "../help/<%= wp.getLocale() %>/";
	var helpUrl = "index.html"; 
    
    function openHelp() {
		openwin(localePrefix + helpUrl, "<%= wp.key("button.help") %>", 600, 450);    
    }
    
    function openwin(url, name, w, h) {
        window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width='+w+',height='+h);
    }     
</script>
</head>

<body class="buttons-head" unselectable="on" onload="loadBody();">

<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel("label.project") %>

<form name="wpProjectSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<td><%=
	
	wp.getProjectSelect("name=\"wpProject\" onchange=\"document.forms.wpProjectSelect.submit()\"", "style=\"width:150px\"")
        
%></td>
</form>

<% 
if (wp.isPublishEnabled()) {
	out.println(wp.button("../commons/publishproject.jsp", "body", "publish", "button.publish", buttonStyle));
	} else {
	out.println(wp.button(null, null, "publish_in", "button.publish", buttonStyle));
}

if (wp.showSiteSelector()) {

%><%= wp.buttonBarSeparator(5, 0) %>          
<%= wp.buttonBarLabel("label.site") %>

<form name="wpSiteSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<td><%= 

	wp.getSiteSelect("name=\"wpSite\" style=\"width:150px\" onchange=\"document.forms.wpSiteSelect.submit()\"")
        
%></td>
</form>
<% } %>

<%= wp.buttonBarSeparator(5, 0) %>          
<%= wp.buttonBarLabel("label.view") %>

<form name="wpViewSelect" method="post" action="<%= cms.link(cms.getRequestContext().getUri()) %>">
<td><%= 

	wp.getViewSelect("name=\"wpView\" style=\"width:150px\" onchange=\"document.forms.wpViewSelect.submit()\"")
        
%></td>
</form>

<%= wp.buttonBarSeparator(5, 0) %>        
<%= wp.button("reload.jsp", null, "reload", "button.reload", buttonStyle) %>
<%= wp.button("../commons/preferences.jsp", "body", "preferences", "button.preferences", buttonStyle) %>

<% 
if (wp.isSyncEnabled()) {
	out.println(wp.button("../administration/syncfolder/synchronize.html", "body", "sync", "button.syncfolder", buttonStyle));
}
if (wp.isHelpEnabled()) {
	out.println(wp.button("javascript:openHelp();", null, "help", "button.help", buttonStyle));
}        
%>
       
<td width="100%">&nbsp;</td>
<%= wp.buttonBarSeparator(5, 0) %>  
<%= wp.button("../../login/index.html?logout=true", "_top", "logout", "button.exit", buttonStyle) %>

<% if (buttonStyle != 2) {%>
<td>&nbsp;</td>
<td><span style="display: block; width: 80px; height: 22px; background-image: url('<%= wp.getSkinUri() %>commons/workplace.gif'); "></span></td>
<% } %>
<%= wp.buttonBar(wp.HTML_END) %>

</body>
</html>