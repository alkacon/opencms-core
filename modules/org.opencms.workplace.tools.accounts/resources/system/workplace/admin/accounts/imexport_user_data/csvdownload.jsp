<%@ page import="org.opencms.workplace.tools.accounts.*"%><%	
// initialize the workplace class
CmsUsersCsvDownloadDialog wp = new CmsUsersCsvDownloadDialog(pageContext, request, response);        
%><%= wp.generateCsv() %>