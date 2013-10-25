<%@ page import="org.opencms.workplace.tools.searchindex.sourcesearch.*"%><% 
	CmsSourceSearchDialog dialog = new CmsSourceSearchDialog(pageContext, request, response);
	dialog.displayDialog(true);
	dialog.writeDialog(); 
%>