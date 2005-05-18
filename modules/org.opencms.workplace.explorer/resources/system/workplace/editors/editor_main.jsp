<%@ page import="
	org.opencms.workplace.editors.*,
	org.opencms.jsp.*"
	buffer="none"
	session="false"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// initialize the workplace class
	CmsEditorSelector wp = new CmsEditorSelector(cms);
	String editorUri = wp.getSelectedEditorUri();
	
	if (editorUri != null) {
		cms.include(editorUri, null, request.getParameterMap());
	}

%>