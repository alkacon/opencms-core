<%@ page import="java.util.*" %>

<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<%
	boolean setupOk = (Bean.getProperties() != null);
%>
<jsp:setProperty name="Bean" property="*" />
</jsp:useBean>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<title>OpenCms Setup Wizard</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta http-equiv="refresh" content="0;url=<%= Bean.getDatabaseConfigPage(Bean.getDatabase()) %>">
</head>
<body>
</body>
</html>