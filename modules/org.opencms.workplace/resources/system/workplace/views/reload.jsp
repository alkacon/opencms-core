<%@ page import="org.opencms.workplace.*" %><%
    CmsDialog wp = new CmsDialog(pageContext, request, response);
    // clears the list cache
    wp.getSettings().setHtmlList(null);
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
</head>
<script language='javascript' type='text/javascript'>
window.top.location.href = "<%= wp.getJsp().link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI) %>";
</script>
<body>
</body>
</html>
