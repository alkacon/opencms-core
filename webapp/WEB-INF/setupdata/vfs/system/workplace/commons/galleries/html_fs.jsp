<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %><%	
	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryHtmls wp = new CmsGalleryHtmls(pageContext, request, response);
	
	String params = "?" + wp.paramsAsRequest();
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
	<title><%= wp.key("button.htmllist") %></title>
</head>

<frameset rows="270,*" framespacing="0">
    <frame <%= wp.getFrameSource("gallery_fs", cms.link("html_fs_sub.jsp" + params)) %> style="border-bottom: 1px solid Window;">
    <frame <%= wp.getFrameSource("gallery_preview", cms.link("html_preview.jsp")) %> scrolling="auto" style="border-top: 1px solid Menu;">
</frameset>

<body>
</body>
</html>