<%@ page import="org.opencms.workplace.tools.publishqueue.*" %><%// initialize list dialogs
	CmsPublishQueuePersonalList wpPersonalQueue = new CmsPublishQueuePersonalList(pageContext, request, response);
	CmsPublishQueueList wpPublishQueue = new CmsPublishQueueList(pageContext, request, response);
	CmsPublishQueueTwoListsDialog wpTwoLists = new CmsPublishQueueTwoListsDialog(wpPersonalQueue, wpPublishQueue);
	// perform the active list actions
	wpTwoLists.displayDialog(true);
	// perform the active list actions
	if (wpPersonalQueue.isForwarded() || wpPublishQueue.isForwarded()) {
		return;
	}
	// write the content of list dialogs
	wpTwoLists.writeDialog();%>