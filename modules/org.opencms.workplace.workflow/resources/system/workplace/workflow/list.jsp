<%@ page 
	buffer="none" 
	import="org.opencms.workplace.workflow.*" %>
<%	
	// initialize the workplace class
	CmsWorkflowList wp = new CmsWorkflowList(pageContext, request, response);
        wp.displayDialog();
%>