<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Initialize Thread */ %>
<jsp:useBean id="Thread" class="com.opencms.boot.CmsSetupThread" scope="session"/>
<% Thread.setBasePath(config.getServletContext().getRealPath("/")); %>
		
<%	/* next page to be accessed */
	String nextPage = "";
%>

<% /* Import packages */ %>
<%@ page import="java.util.*" %>

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
%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	
	<% 	if(!Thread.finished() && setupOk)	{
			out.println("<meta http-equiv='refresh' content='5'>");
		}
	%>
	
	<link rel="Stylesheet" type="text/css" href="style.css">
</head>

<body>
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>	
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
	<td><form action="<%= nextPage %>" method="POST">	
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
				<td height="50" align="right"><img src="opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			<% if(setupOk)	{ %>
			<tr>
				<td height="375" align="center" valign="top">
				<% 
						if(Thread.finished())	{
				%>
							<strong>Setup finished. If no major errors have occured (see output below) the OpenCms is ready to use.<br>Click 'Finish' to end the wizard an start OpenCms.
				<%		}
						else	{
							out.println("Running...");
						}
				%>
						<textarea style="width:650px;height:350px;" cols="60" rows="20" readonly wrap="off"><%																				
							for(int i = messages.size()-1;i >= 0;i--)	{
								out.println(messages.elementAt(i).toString());
							}
				%>		</textarea>

				</td>
			</tr>
			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
								<input type="button" class="button" disabled style="width:150px;" width="150" width="150" value="&#060;&#060; Back">
							</td>
							<% if(Thread.finished())	{ %>
							<td width="200" align="left">
								<input type="button" class="button" style="width:150px;" width="150" width="150" value="Finish" onclick="location.href='finished.jsp'">
							</td>							
							<%	}	else	{ %>
							<td width="200" align="left">
								<input type="button" disabled class="button" style="width:150px;" width="150" width="150" value="Finish">
							</td>
							<% } %>
							<td width="200" align="center">
								<input type="button" class="button" style="width:150px;" width="150" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<% } else	{ %>
			<tr>
				<td align="center" valign="top">
					<p><b>ERROR</b></p>
					The setup wizard has not been started correctly!<br>
					Please click <a href="">here</a> to restart the Wizard
				</td>
			</tr>				
			<% } %>	
			</form>
			</table>
		</td>
	</tr>
</table>
</td>
</tr>
</table>
</body>
</html>