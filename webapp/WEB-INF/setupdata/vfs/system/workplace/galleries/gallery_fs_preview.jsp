<%@ page import="org.opencms.jsp.*, org.opencms.workplace.galleries.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// get gallery instance
	A_CmsGallery wp = A_CmsGallery.createInstance(cms);
	
	String params = "?" + wp.paramsAsRequest();
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<frameset rows="25,*" border="0" frameborder="0" framespacing="0">
    <frame <%= wp.getFrameSource("gallery_buttonbar", cms.link("gallery_buttonbar.jsp" + params)) %> noresize="noresize" scrolling="no" style="border-top: 1px solid WindowFrame;">
    <frame <%= wp.getFrameSource("gallery_preview", cms.link("gallery_preview.jsp" + params)) %> scrolling="auto">
</frameset>
</html>