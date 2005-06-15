<%@ page 
	buffer="none" 
	import="org.opencms.workplace.tools.workplace.rfsfile.*" %>
<%
  CmsRfsFileDownloadServlet downloadServlet = new CmsRfsFileDownloadServlet();
  downloadServlet.doPost(request,response);
%>