<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %><%	
	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryLinks wp = new CmsGalleryLinks(pageContext, request, response);
	
	String params = "?" + wp.paramsAsRequest();
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
	<title><%= wp.key("button.linklist") %></title>
</head>

<frameset rows="450,*" framespacing="0">
    <frame <%= wp.getFrameSource("gallery_fs", cms.link("link_fs_head.jsp" + params)) %> style="border-bottom: 1px solid Window;">
    <frame <%= wp.getFrameSource("preview_fs", cms.link("link_fs_preview.jsp" + params)) %> style="border-top: 1px solid Window;">    
</frameset>

<body>
</body>
</html>