<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Initialize Thread */ %>
<jsp:useBean id="Thread" class="com.opencms.boot.CmsSetupThread" scope="session"/>
<% Thread.setBasePath(config.getServletContext().getRealPath("/")); %>

<% /* Import packages */ %>
<%@ page import="java.util.*,com.opencms.boot.*" %>

<%
	Vector messages = new Vector();
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
	
	if(setupOk)	{
		if(!Thread.isAlive())	{	
			Thread.start();
		}
		messages = com.opencms.boot.CmsSetupLoggingThread.getMessages();
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
<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<script language="Javascript">
		var output = new Array();
		<%			
			if(setupOk)	{
				for(int i = 0; i < (size-offset) ;i++)	{
					out.println("output[" + i + "] = \"" + CmsSetupUtils.escape(messages.elementAt(i+offset).toString(),"UTF-8") + "\";");
				}			
			}
			else	{
				out.println("output[0] = 'ERROR';");
			}
		%>
		function send()	{
			top.window.display.start(output);
		}
		
	</script>
	
</head>
<body onload="<% if(setupOk)	{ out.print("send();");if(!Thread.finished())	{out.print("setTimeout('location.reload()',5000);");} else	{out.print("setTimeout('top.display.finish()',5000);");}} %>"></body>
</html>
