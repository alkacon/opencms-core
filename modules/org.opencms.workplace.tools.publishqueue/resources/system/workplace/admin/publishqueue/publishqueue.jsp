<%@ page import="
    org.opencms.jsp.CmsJspActionElement, 
    org.opencms.workplace.tools.publishqueue.CmsPublishQueueList,
    org.opencms.workplace.tools.publishqueue.CmsPublishQueuePersonalList
" %><%

  CmsJspActionElement actionElement = new CmsJspActionElement(pageContext, request, response);

  CmsPublishQueueList wpPublishQueue = new CmsPublishQueueList(pageContext, request, response);
  // perform the list actions   
  wpPublishQueue.displayDialog(true);
  if (wpPublishQueue.isForwarded()) {
    return;
  }

  CmsPublishQueuePersonalList wpPersonalQueue = new CmsPublishQueuePersonalList(actionElement);
  // perform the list actions   
  wpPersonalQueue.displayDialog(true);
  if (wpPersonalQueue.isForwarded()) {
    return;
  }

  // write the content of list dialogs
  wpPublishQueue.writeDialog();
  wpPersonalQueue.writeDialog();
%>