<%@page import="org.opencms.workplace.tools.accounts.*" %><%
	CmsUserKillSessions wp = new CmsUserKillSessions(pageContext, request, response);
	wp.actionKillUserSessions(); 
%>
