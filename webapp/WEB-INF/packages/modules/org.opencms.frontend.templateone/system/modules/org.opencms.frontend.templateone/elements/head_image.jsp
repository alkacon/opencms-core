<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*" %><%

// initialize action element to access the API
CmsTemplateBean cms = new CmsTemplateBean(pageContext, request, response);

// calculate image link
String imageLink = cms.getRequest().getParameter("imagelink");
boolean showLink = !"".equals(imageLink) && !"none".equals(imageLink);

if (showLink && imageLink.startsWith("/")) {
	// calculate absolute path (internal link)
	imageLink = cms.link(imageLink);
}

%>
 
<div class="imagehead"><%
if (showLink) {
	%><a href="<%= imageLink %>"><span class="imagelink"></span></a><%
}
%></div>