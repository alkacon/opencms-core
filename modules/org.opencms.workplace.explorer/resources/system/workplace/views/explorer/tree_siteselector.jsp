<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsTree wp = new CmsTree(cms);
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= CmsWorkplace.getStyleUri(wp.getJsp(),"workplace.css")%>">
<title><%= wp.key(org.opencms.workplace.explorer.Messages.GUI_TITLE_EXPLORERTREE_0) %></title>

<script type="text/javascript">
<!--
function changeSite() {
	document.forms.siteselect.submit();
}
//-->
</script>

</head>
<body class="buttons-head" unselectable="on">

<%= wp.buttonBar(CmsWorkplace.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel(org.opencms.workplace.explorer.Messages.GUI_LABEL_SITE_0) %>

<td>
<form name="siteselect" method="post" action="tree_fs.jsp" target="_parent">
<input type="hidden" name="<%= CmsTree.PARAM_INCLUDEFILES %>" value="<%= wp.includeFiles() %>">
<input type="hidden" name="<%= CmsTree.PARAM_TYPE %>" value="<%= wp.getTreeType() %>">
<input type="hidden" name="<%= CmsTree.PARAM_SHOWSITESELECTOR %>" value="<%= wp.showSiteSelector() %>">
<input type="hidden" name="<%= CmsTree.PARAM_RESOURCE %>" value="/">
<%if (request.getParameter(CmsTree.PARAM_INTEGRATOR) != null) {%>
<input type="hidden" name="<%=CmsTree.PARAM_INTEGRATOR%>" value="<%=request.getParameter(CmsTree.PARAM_INTEGRATOR)%>" />
<% }  %>
<%= wp.getSiteSelector("name=\"treesite\" onchange=\"changeSite();\" style=\"width:250px;\"") %>
</form>
</td>

<%= wp.buttonBar(CmsWorkplace.HTML_END) %>
</body>
</html>