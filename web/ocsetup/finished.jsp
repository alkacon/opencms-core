<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->

<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />
<jsp:useBean id="Thread" class="com.opencms.boot.CmsSetupThread" scope="session"/>

<%
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
		
	if(setupOk)	{
		/* stop possible running threads */
		Thread.stopLoggingThread();		
		Thread.stop();
	
		/* invalidate the sessions */
		request.getSession().invalidate();
	}
	
	/* next page to be accessed */
	String nextPage = "";	
%>

<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
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
				<td height="375" align="center" valign="middle">
					<p><b>OpenCms setup finished.</b><br>					
					The wizard is now locked. To use the wizard again reset the flag in the "opencms.properties".</p>
					<p>To start OpenCms click <a target="_blank" href="<%= request.getContextPath() %>/opencms/system/workplace/action/login.html">here</a>.</p>
				</td>
			</tr>			
			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
								<input type="button" disabled class="button" style="width:150px;" width="150" value="&#060;&#060; Back">
							</td>
							<td width="200" align="left">
								<input type="button" disabled class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
							</td>
							<td width="200" align="center">
								<input type="button" disabled class="button" style="width:150px;" width="150" value="Cancel" >
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