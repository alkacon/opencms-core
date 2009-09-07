<%@ page import="org.opencms.file.*" %>
<%@ page import="org.opencms.jsp.*" %>
<%@ page import="java.util.*" %>
<%
	CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
	CmsObject cms = jsp.getCmsObject();
	for (int i = 1; i <= 20; i++) {
		cms.copyResource("/demo/news/news_0001.html", "/demo/news/foo_"+i+".html");
	}	
%>