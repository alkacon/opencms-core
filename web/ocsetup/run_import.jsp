<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
	
<% /* Initialize Thread */ %>
<jsp:useBean id="Thread" class="com.opencms.boot.CmsSetupThread" scope="session"/>
<% Thread.setBasePath(config.getServletContext().getRealPath("/")); %>
		
<%	/* next page to be accessed */
	String nextPage = "";
%>

<% /* Import packages */ %>
<%@ page import="java.util.*" %>

<%
	if(!Thread.isAlive())	{	
		Thread.start();
	}
	Vector messages = com.opencms.boot.CmsSetupLoggingThread.getMessages();
%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	
	<% 	if(!Thread.notRunning())	{
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
			<tr>
				<td height="375" align="center" valign="top">
				<% 
						if(Thread.notRunning())	{
						/* invalidate session */
						request.getSession().invalidate();
				%>
						<strong>Setup finished. If no major errors have occured (see output below) the OpenCms is ready to use. Click <a target="_blank" href="<%= request.getContextPath() %>/engine/system/workplace/action/login.html">here</a> to start.</strong><br>
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
							<td width="200" align="left">
								<input type="button" disabled class="button" style="width:150px;" width="150" width="150" value="Continue &#062;&#062;">
							</td>
							<td width="200" align="center">
								<input type="button" class="button" style="width:150px;" width="150" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
							</td>
						</tr>
					</table>
				</td>
			</tr>
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