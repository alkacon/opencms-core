<%@ page import="
	org.opencms.workplace.*,
	org.opencms.jsp.*"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsFrameset wp = new CmsFrameset(cms);
	CmsLoginUserAgreement ua = new CmsLoginUserAgreement(cms);

if (ua.isShowUserAgreement()) {
	response.sendRedirect(cms.link(ua.getConfigurationVfsPath()));
	return;
}

if (wp.isReloadRequired()) {
	response.sendRedirect(cms.link(cms.getRequestContext().getUri()));
	return;
}

 %><!DOCTYPE html>
<html>
	<head>
		<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<title><%= wp.key(org.opencms.workplace.Messages.GUI_LABEL_WPTITLE_1, new Object[]{wp.getSettings().getUser().getFullName()}) %></title>
		<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() %>commons/explorer.js"></script>
		<script type="text/javascript" src="<%= CmsWorkplace.getSkinUri() %>commons/ajax.js"></script>
		<script type="text/javascript" src="<%= cms.link("/system/workplace/views/top_js.jsp") %>"></script>
	</head>
	<frameset rows="24,*,24" border="0" frameborder="0" framespacing="0">
	    <frame <%= wp.getFrameSource("head", cms.link("/system/workplace/views/top_head.jsp?wpFrame=head")) %> noresize scrolling="no">
	    <frame <%= wp.getFrameSource("body", wp.getStartupUri()) %> noresize scrolling="no">
	    <frame <%= wp.getFrameSource("foot", cms.link("/system/workplace/views/top_foot.jsp?wpFrame=foot")) %> noresize scrolling="no">
	</frameset>
</html>
