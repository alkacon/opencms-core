<%@ page import="org.opencms.update.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.update.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%--
--%><%

Bean.prepareStep2b();

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<title>OpenCms Update Wizard - Update modules</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script language="JavaScript">
<!--

var output = new Array();
<%

Bean.prepareStep2bOutput(out);

%>

function send()	{
	top.window.display.start(output);
}

//-->
</script>

</head>
<body onload="initThread()"></body>
</html>
