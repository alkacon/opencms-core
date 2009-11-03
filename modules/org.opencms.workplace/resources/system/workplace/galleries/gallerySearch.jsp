<%@page buffer="none" session="false" import="org.opencms.workplace.galleries.CmsGallerySearchServer" %><%

CmsGallerySearchServer cgs = new CmsGallerySearchServer(pageContext, request, response);
  cgs.serve();
%>