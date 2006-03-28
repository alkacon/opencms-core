<%@ page 
	buffer="none" 
	import="org.opencms.workplace.demos.list.*" %>
<%	
	// initialize the workplace class
	CmsListDemo2 wp = new CmsListDemo2(pageContext, request, response);
        wp.displayDialog();
%>