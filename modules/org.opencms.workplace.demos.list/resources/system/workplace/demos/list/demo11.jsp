<%@ page 
	buffer="none" 
	import="org.opencms.workplace.demos.list.*" %>
<%	
	// initialize the workplace class
	CmsListDemo11 wp = new CmsListDemo11(pageContext, request, response);
        wp.displayDialog();
%>