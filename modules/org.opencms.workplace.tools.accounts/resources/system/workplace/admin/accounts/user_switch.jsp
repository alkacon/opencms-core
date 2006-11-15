<%@ page import="org.opencms.main.*, 
                 org.opencms.jsp.*, 
                 org.opencms.util.*, 
                 org.opencms.workplace.*, 
                 java.util.*"%><%	

	CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);
	CmsSessionManager sessionManager = OpenCms.getSessionManager();
	sessionManager.switchUser(actionElement.getCmsObject(), actionElement.getRequest(), actionElement.getCmsObject().readUser(new CmsUUID(request.getParameter("userid"))));
%>
<html>
<head></head>
<body onload="window.top.head.doReload();"></body>
</html>