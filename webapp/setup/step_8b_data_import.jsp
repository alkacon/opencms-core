<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	Bean.prepareStep8b();

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<title>OpenCms Setup Wizard - Import workplace</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script language="JavaScript">
<!--

var output = new Array();
<%
	boolean finished = Bean.prepareStep8bOutput(out);
%>
	
function initThread() {
<% 
	if(Bean.isInitialized())	{ 
		out.print("send();");
		if(finished) {
			out.println("setTimeout('top.display.finish()', 500);");
		} else {
			int timeout = 5000;
			if (Bean.getWorkplaceImportThread().getLoggingThread().getMessages().size() < 20) {
				timeout = 1000;
			} 
			out.println("setTimeout('location.reload()', " + timeout + ");");
		}
	}
%>	
}

function send()	{
	top.window.display.start(output);
}

//-->
</script>

</head>
<body onload="initThread()"></body>
</html>
