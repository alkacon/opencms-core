<%@ page import="
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
	buffer="none"
%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsTree wp = new CmsTree(cms);
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<link rel="stylesheet" type="text/css" href="<%= wp.getStyleUri(wp.getJsp(),"workplace.css")%>">
<title><%= wp.key("title.explorertree") %></title>

<script type="text/javascript">
<!--
function changeSite() {
	document.forms.siteselect.submit();
}
//-->
</script>

</head>
<body class="buttons-head" unselectable="on">

<%= wp.buttonBar(wp.HTML_START) %>
<%= wp.buttonBarStartTab(0, 0) %>
<%= wp.buttonBarLabel("label.site") %>

<form name="siteselect" method="post" action="tree_fs.jsp" target="_top">
<td>
<input type="hidden" name="includefiles" value="<%= wp.includeFiles() %>">
<input type="hidden" name="type" value="<%= wp.getTreeType() %>">
<input type="hidden" name="resource" value="/">
<%= wp.getSiteSelector("name=\"treesite\" onchange=\"changeSite();\" style=\"width:250px;\"") %>
</td>
</form>

<%= wp.buttonBar(wp.HTML_END) %>
</body>
</html>