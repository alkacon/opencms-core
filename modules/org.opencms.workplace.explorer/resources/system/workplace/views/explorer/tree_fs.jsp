<%@ page import="
	org.opencms.workplace.*,
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsTree wp = new CmsTree(cms);
	
	StringBuffer params = new StringBuffer(16);
	if (wp.includeFiles()) {
		params.append("&");
		params.append(CmsTree.PARAM_INCLUDEFILES);
		params.append("=true");
	}
	if (!wp.isProjectAware()) {
		params.append("&");
		params.append(CmsTree.PARAM_PROJECTAWARE);
		params.append("=false");
	}
	if (wp.getTreeType() != null) {
		params.append("&");
		params.append(CmsTree.PARAM_TYPE);
		params.append("=");
		params.append(wp.getTreeType());
	}
	
	if (wp.showSiteSelector()) {
		params.append("&");
		params.append(CmsTree.PARAM_SHOWSITESELECTOR);
		params.append("=true");
	}
	String integrator = request.getParameter(CmsTree.PARAM_INTEGRATOR);
	if (integrator != null) {
	    params.append("&");
	    params.append(CmsTree.PARAM_INTEGRATOR);
	    params.append("=");
	    params.append(integrator);
	}
	
	String frameRows = "*,0";
	String frameSiteSelector = "";
	if (wp.showSiteSelector()) {
		frameRows = "24,*,0";
		frameSiteSelector = "<frame name=\"tree_siteselector\" src=\"tree_siteselector.jsp?" + CmsTree.PARAM_RESOURCE + "=/" + params.toString() + "\" scrolling=\"no\">\n\t";
	}
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>

<head>
<meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=<%= wp.getEncoding() %>">
<title><%= wp.key(org.opencms.workplace.explorer.Messages.GUI_TITLE_EXPLORERTREE_0)%></title>
<script Language="Javascript" type="text/javascript" src="<%= CmsWorkplace.getSkinUri() %>commons/tree.js"></script>
<% if (request.getParameter("integrator") != null)  { %>
<script language="javascript" type="text/javascript" src="${param.integrator}"></script>
<% } %>
<script type="text/javascript"> 
<% 

	String init =  wp.initTree();
	// System.err.println(init);
	out.println(init);
	
%>
</script>
</head>

<frameset rows="<%= frameRows %>" border="0" frameborder="0" framespacing="0">	
	<%= frameSiteSelector %><frame name="tree_display" src="<%= CmsWorkplace.getSkinUri() %>commons/empty.html" scrolling="auto">	
	<frame <%= wp.getFrameSource("tree_files", cms.link("tree_files.jsp?" + CmsTree.PARAM_RESOURCE + "=" + wp.getRootFolder() + params.toString())) %>>
</frameset>

</html>