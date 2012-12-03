<%@page session="false"	import="org.opencms.jsp.*,org.opencms.util.CmsRequestUtil,org.opencms.file.*,org.opencms.main.*"%><%
    CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
	CmsObject cms = jsp.getCmsObject();
	OpenCms.getADEManager().handleHtmlRedirect(cms, request, response, cms.getRequestContext().getUri()); 
%>