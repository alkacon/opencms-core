<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %><%	
	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryImages wp = new CmsGalleryImages(pageContext, request, response);
	
	String params = "?" + wp.paramsAsRequest();
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
</head>

<frameset rows="25,*" border="0" frameborder="0" framespacing="0">
    <frame <%= wp.getFrameSource("gallery_buttonbar", cms.link("img_buttonbar.jsp" + params)) %> noresize="noresize" scrolling="no" style="border-top: 1px solid WindowFrame;">
    <frame <%= wp.getFrameSource("gallery_preview", cms.link("img_preview.jsp" + params)) %> scrolling="auto">
</frameset>

<body>
</body>
</html>