<%@ page import="org.opencms.workplace.*" %>

<%
    CmsDialog wp = new CmsDialog(pageContext, request, response);
    // clears the list cache
    wp.getSettings().setHtmlList(null);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
</head>
<script language='javascript' type='text/javascript'><!--
   window.top.location.reload(true);
--></script>
<body>
</body>
</html>
