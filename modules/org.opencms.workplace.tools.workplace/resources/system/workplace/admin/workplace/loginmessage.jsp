<%@ page 
	buffer="none" 
	import="org.opencms.workplace.tools.workplace.*" %><%	
	
	
	// initialize the workplace class
	CmsEditLoginMessageDialog wp = new CmsEditLoginMessageDialog(pageContext, request, response);
	
	// perform the dialog action	
	wp.actionDialog();
%>