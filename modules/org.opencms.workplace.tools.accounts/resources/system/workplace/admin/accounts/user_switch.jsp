<%@ page import="org.opencms.workplace.tools.accounts.*"%><%	

    CmsUserOverviewDialog usersList = new CmsUserOverviewDialog(pageContext, request, response);
    usersList.actionSwitchUser();
%>
<html>
<head></head>
<body onload="window.top.head.doReload();"></body>
</html>