<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->

<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />
<jsp:useBean id="Thread" class="com.opencms.boot.CmsSetupThread" scope="session"/>

<%
	/* stop possible running threads */
	Thread.stopLoggingThread();		
	Thread.stop();

	/* invalidate the sessions */
	request.getSession().invalidate();
	
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
			<tr>
				<td height="375" align="center" valign="middle">
					<strong>Setup cancelled!</strong><br><br>Click <a href="index.jsp">here</a> to restart setup
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