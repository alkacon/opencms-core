<%@ page import="org.opencms.jsp.*, org.opencms.workplace.galleries.*" session="false" %>
<%		
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	A_CmsGallery wp = A_CmsGallery.createInstance(cms);
	
	String params = "?" + wp.paramsAsRequest();
	
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
</head>

<frameset rows="50,*" border="0" frameborder="0" framespacing="0">
    <frame <%= wp.getFrameSource("gallery_head", cms.link("gallery_head.jsp" + params)) %> noresize="noresize" scrolling="no" style="border-bottom: 1px solid WindowFrame;">
    <frame <%= wp.getFrameSource("gallery_list", cms.link("gallery_list.jsp" + params)) %> scrolling="auto" style="border-bottom: 1px solid Menu;">
</frameset>

<body>
</body>
</html>