<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

// calculate image link
String imageLink = cms.getRequest().getParameter("imagelink");
if (!"".equals(imageLink) && imageLink.startsWith("/")) {
	// calculate absolute path (internal link)
	imageLink = cms.link(imageLink);
}

%>
 
<div class="imagehead"><%
if (!"".equals(imageLink)) {
	%><a href="<%= imageLink %>"><span class="imagelink"></span></a><%
}
%></div>