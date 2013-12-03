<%@ page import="org.opencms.workplace.tools.searchindex.sourcesearch.*,org.opencms.workplace.*,org.opencms.jsp.*,org.opencms.main.*,org.opencms.search.solr.*" %><% 
boolean solrEnabled = OpenCms.getSearchManager().getSolrServerConfiguration().isEnabled();
CmsSolrIndex solrIndex = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
CmsWidgetDialog dialog = null;
if (solrEnabled && solrIndex != null) {
    dialog = new CmsSolrSearchDialog(new CmsJspActionElement(pageContext, request, response));
} else {
    dialog = new CmsSourceSearchDialog(new CmsJspActionElement(pageContext, request, response));
}
dialog.displayDialog(true);
dialog.writeDialog();
%>
