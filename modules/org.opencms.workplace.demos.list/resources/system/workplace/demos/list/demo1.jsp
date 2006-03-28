<%@ page 
	buffer="none" 
	import="org.opencms.workplace.demos.list.*" %>
<%	
	// initialize the workplace class
	CmsListDemo1 wp = new CmsListDemo1(pageContext, request, response);
        wp.displayDialog();
%>