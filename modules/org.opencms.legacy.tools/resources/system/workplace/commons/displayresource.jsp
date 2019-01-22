<%@ page import="org.opencms.workplace.commons.*" session="false" %><%	

	// initialize the workplace class
	CmsDisplayResource wp = new CmsDisplayResource(pageContext, request, response);
	
	// redirect to the requested resource
	wp.actionShow();
%>