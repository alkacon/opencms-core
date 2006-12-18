<%@ page import="org.opencms.workplace.tools.accounts.*"%><%	

                CmsUsersList usersList = new CmsUsersList(pageContext, request, response);
             	usersList.actionSwitchUser();
%>
<html>
<head></head>
<body onload="window.top.head.doReload();"></body>
</html>