<%@ page import="
	org.opencms.workplace.*,
	org.opencms.jsp.*,
	org.opencms.main.CmsContextInfo,
	org.opencms.util.CmsDateUtil,
	java.text.DateFormat,
	java.util.Date"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsFrameset wp = new CmsFrameset(cms);

 %><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= CmsWorkplace.getStyleUri(wp.getJsp(), "workplace.css")%>">
<title>OpenCms Workplace Foot Frame</title>
<script type="text/javascript"> 
function doReloadFoot() {
	document.location.href="<%= cms.link("top_foot.jsp?wpFrame=foot") %>";
}
</script>

<%= wp.getBroadcastMessage() %>

</head>

<body class="buttons-foot" unselectable="on" onload="setTimeout('doReloadFoot()', 300000);">
<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel(org.opencms.workplace.Messages.GUI_LABEL_USER_0) %><td><%= wp.getSettings().getUser().getDisplayName(wp.getCms(), wp.getLocale()) %></td>
<%= wp.buttonBarLabel(org.opencms.workplace.Messages.GUI_LABEL_LOGINTIME_0) %><td><%= wp.getLoginTime() %></td>
<%= wp.buttonBarLabel(org.opencms.workplace.Messages.GUI_LABEL_LOGINADDRESS_0) %><td><%= wp.getLoginAddress() %></td><%
Long attrTimeWarp = (Long)session.getAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME);
if(attrTimeWarp != null) {%>
<td>
	<div class="timewarp">
		<%= wp.key(org.opencms.workplace.commons.Messages.GUI_LABEL_TIMEWARP_0) %>: <%= CmsDateUtil.getDateTime(new Date(attrTimeWarp.longValue()), DateFormat.SHORT, cms.getRequestContext().getLocale()) %>
	</div>
</td><%
}%>
<%= wp.buttonBar(CmsWorkplace.HTML_END) %>
</body>
</html>