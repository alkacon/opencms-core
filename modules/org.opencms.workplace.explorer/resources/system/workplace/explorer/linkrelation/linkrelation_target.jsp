<%@ page import="org.opencms.workplace.commons.*" %>
<%@ page import="org.opencms.jsp.*" %>
<%
	// initialize the widget dialog
	CmsResourceLinkRelationList wpList = new CmsResourceLinkRelationList(new CmsJspActionElement(pageContext, request, response), true);
	// perform the widget actions   
	wpList.displayDialog(true);
	if (wpList.isForwarded()) {
		return;
	}

	wpList.writeDialog();
%>