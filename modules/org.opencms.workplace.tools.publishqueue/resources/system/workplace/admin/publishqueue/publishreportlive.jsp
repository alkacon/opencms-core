<%@ page import="org.opencms.workplace.tools.publishqueue.CmsPublishQueueLiveReportDialog" %><%	
	
	CmsPublishQueueLiveReportDialog wp = new CmsPublishQueueLiveReportDialog(pageContext, request, response);
	wp.displayReport();
%>
