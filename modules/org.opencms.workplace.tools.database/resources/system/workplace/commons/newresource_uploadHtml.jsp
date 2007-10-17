<%@ page import="org.opencms.jsp.*,org.opencms.util.CmsRequestUtil"%><%	
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

	String link = "/system/workplace/views/admin/admin-main.jsp?root=explorer&path=/uploadhtml";
	
	CmsRequestUtil.redirectRequestSecure(cms, link);	
%>