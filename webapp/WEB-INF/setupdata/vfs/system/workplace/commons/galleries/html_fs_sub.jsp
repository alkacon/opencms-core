<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %><%	
	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryHtmls wp = new CmsGalleryHtmls(pageContext, request, response);
	
	String params = "?" + wp.paramsAsRequest();
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
</head>

<frameset rows="70,*" border="0" frameborder="0" framespacing="0">
    <frame <%= wp.getFrameSource("gallery_head", cms.link("html_head.jsp" + params)) %> noresize="noresize" scrolling="no" style="border-bottom: 1px solid WindowFrame;">
    <frame <%= wp.getFrameSource("gallery_list", cms.link("html_list.jsp" + params)) %> scrolling="auto" style="border-bottom: 1px solid Menu;">
</frameset>

<body>
</body>
</html>