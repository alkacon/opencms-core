<%@ page import="java.util.*,org.opencms.setup.*,org.opencms.i18n.*,org.opencms.util.*" %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:useBean id="Thread" class="org.opencms.setup.CmsSetupThread" scope="session"/>
<%	
	Thread.setBasePath(config.getServletContext().getRealPath("/"));
	Thread.setAdditionalShellCommand(Bean); 

	Vector messages = new Vector();
	boolean setupOk = (Bean.getProperties()!=null);

	if(setupOk)	{
		if(!Thread.isAlive())	{
			Thread.start();
		}
		messages = org.opencms.setup.CmsSetupLoggingThread.getMessages();
	}

	int size = messages.size();
	Object tempOffset = new Object();
	int offset = 0;
	try	{
		tempOffset = session.getAttribute("offset");

	}
	catch (NullPointerException e)	{
		tempOffset = "0";
	}
	if(tempOffset != null)	{
		offset = Integer.parseInt(tempOffset.toString());
	}
	else	{
		offset = 0;
	}

	session.setAttribute("offset",""+(size));

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<title>OpenCms Setup Wizard</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="JavaScript">
<!--
	
function initThread() {
<% 
	if(setupOk)	{ 
		out.print("send();");
		if(!Thread.finished()) {
			out.println("setTimeout('location.reload()',5000);");
		} else {
			out.println("setTimeout('top.display.finish()',5000);");
		}
	}
%>	
}

var output = new Array();
<%
	if(setupOk)	{
		for(int i = 0; i < (size-offset) ;i++)	{
			String str = messages.elementAt(i+offset).toString();
			//Thread.printToStdOut(str);			
			
			str = CmsEncoder.escapeWBlanks(str, "UTF-8");			
			out.println("output[" + i + "] = \"" + str + "\";");
		}
	} else {
		out.println("output[0] = 'ERROR';");
	}
%>

function send()	{
	top.window.display.start(output);
}

//-->
</script>

</head>
<body onload="initThread()"></body>
</html>
