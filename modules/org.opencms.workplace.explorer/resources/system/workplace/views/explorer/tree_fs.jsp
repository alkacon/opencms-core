<%@ page import="
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
	buffer="none"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsTree wp = new CmsTree(cms);
	
	StringBuffer params = new StringBuffer(16);
	if (wp.includeFiles()) {
		params.append("&");
		params.append(wp.PARAM_INCLUDEFILES);
		params.append("=true");
	}
	if (wp.getTreeType() != null) {
		params.append("&");
		params.append(wp.PARAM_TYPE);
		params.append("=");
		params.append(wp.getTreeType());
	}
	
	if (wp.showSiteSelector()) {
		params.append("&");
		params.append(wp.PARAM_SHOWSITESELECTOR);
		params.append("=true");
	}
	
	String frameRows = "*,0";
	String frameSiteSelector = "";
	if (wp.showSiteSelector()) {
		frameRows = "24,*,0";
		frameSiteSelector = "<frame name=\"tree_siteselector\" src=\"tree_siteselector.jsp?" + wp.PARAM_RESOURCE + "=/" + params.toString() + "\" scrolling=\"no\">\n\t";
	}
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<title><%= wp.key("title.explorertree") %></title>
<script Language="Javascript" type="text/javascript" src="<%= wp.getSkinUri() %>commons/tree.js"></script>
<script type="text/javascript"> 
<% 

	String init =  wp.initTree();
	// System.err.println(init);
	out.println(init);
	
%>
</script>
</head>

<frameset rows="<%= frameRows %>" border="0" frameborder="0" framespacing="0">	
	<%= frameSiteSelector %><frame name="tree_display" src="<%= wp.getSkinUri() %>commons/empty.html" scrolling="auto">	
	<frame <%= wp.getFrameSource("tree_files", cms.link("tree_files.jsp?" + wp.PARAM_RESOURCE + "=" + wp.getRootFolder() + params.toString())) %>>
</frameset>

</html>