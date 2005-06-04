<%@ page import="
	org.opencms.workplace.*,
	org.opencms.jsp.*"
	buffer="none"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsFrameset wp = new CmsFrameset(cms);

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= wp.getStyleUri(wp.getJsp(),"workplace.css")%>">
<title>OpenCms Workplace Foot Frame</title>
<script type="text/javascript"> 
function doReloadFoot() {
	document.location.href="<%= cms.link("top_foot.jsp?wpFrame=foot") %>";
}
</script>

<%= wp.getBroadcastMessage() %>

</head>

<body class="buttons-foot" unselectable="on" onload="setTimeout('doReloadFoot()', 300000);">
<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel("label.user") %><td><%= wp.getSettings().getUser().getName() %></td>
<%= wp.buttonBarLabel("label.logintime") %><td><%= wp.getLoginTime() %></td>
<%= wp.buttonBarLabel("label.loginaddress") %><td><%= wp.getLoginAddress() %></td>
<%= wp.buttonBar(wp.HTML_END) %>
</body>
</html>