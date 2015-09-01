<%@ page import="org.opencms.workplace.tools.searchindex.sourcesearch.*, java.util.*, org.opencms.main.*, org.opencms.file.CmsResource"%>
<%
    CmsSourceSearchFilesDialog wp = new CmsSourceSearchFilesDialog (pageContext, request, response);
	Collection<CmsResource> resultList = (Collection<CmsResource>)session.getAttribute(CmsSearchReplaceSettings.ATTRIBUTE_NAME_SOURCESEARCH_RESULT_LIST);	
	wp.seFiles(resultList);
    wp.displayDialog();
%>
