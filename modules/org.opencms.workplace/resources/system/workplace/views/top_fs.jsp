<%@ page import="
    org.opencms.main.*,
	org.opencms.workplace.*,
	org.opencms.jsp.*"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsFrameset wp = new CmsFrameset(cms);

if (wp.isReloadRequired()) {
		response.sendRedirect(cms.link(cms.getRequestContext().getUri()));
        return;
} 

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">

<title><%= wp.key(org.opencms.workplace.Messages.GUI_LABEL_WPTITLE_2, new Object[]{wp.getSettings().getUser().getName(), request.getServerName()}) %></title>

<script type="text/javascript" src="<%= wp.getSkinUri() %>commons/explorer.js"></script>
<script type="text/javascript" src="<%= cms.link("/system/workplace/views/top_js.jsp") %>"></script> 

</head>

<frameset rows="24,*,24" border="0" frameborder="0" framespacing="0">
    <frame <%= wp.getFrameSource("head", cms.link("/system/workplace/views/top_head.jsp?wpFrame=head")) %> noresize scrolling="no">
    <frame <%= wp.getFrameSource("body", wp.getStartupUri()) %> noresize scrolling="no">
    <frame <%= wp.getFrameSource("foot", cms.link("/system/workplace/views/top_foot.jsp?wpFrame=foot")) %> noresize scrolling="no">
</frameset>

</html>
