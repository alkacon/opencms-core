<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% 
	/* Set all given Properties */
	int expPointNr = 0;	
	while(request.getParameter("exportPoint"+expPointNr) != null)	{
		Bean.setExportPoint(request.getParameter("exportPoint"+expPointNr),expPointNr);
		if(request.getParameter("exportPointPath"+expPointNr) != null)	{
			Bean.setExportPointPath(request.getParameter("exportPointPath"+expPointNr),expPointNr);
		}
		expPointNr++;
	}
%>

<%	
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);

	/* next page to be accessed */
	String nextPage = "advanced_stexport.jsp";
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
				<td height="375" align="center" valign="top">
					<table border="1">
						<tr>
							<td align="center" class="bold">
								<p>Redirect parameters</p>
								<table border="0">
								<%
									int redirectNr = 0;
									while (!"".equals(Bean.getRedirect(redirectNr)))	{
								%>
									<tr>
										<td width="150">
											Redirect <%= ""+redirectNr %>
										</td>
										<td>
											<input type="text" size="30" style="width:300px;" name="redirect<%= ""+redirectNr %>" value="<%= Bean.getRedirect(redirectNr) %>">
										</td>
									</tr>
								<%
									if(!"".equals(Bean.getRedirectLocation(redirectNr)))	{
								%>
									<tr>
										<td>
											Redirect Location <%= ""+redirectNr %>
										</td>
										<td>
											<input type="text" size="30" style="width:300px;" name="redirectLocation<%= ""+redirectNr %>" value="<%= Bean.getRedirectLocation(redirectNr) %>">
										</td>
									</tr>
								<%
									}
									out.println("<tr><td colspan='2'><hr></td></tr>");
									redirectNr++;
									}
								%>
								</table>
								<p>
							</td>
						</tr>
																
										</table>
					</td>
				</tr>
				<tr>
					<td height="50" align="center">
						<table border="0">
							<tr>
								<td width="200" align="right">
									<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.back()">
								</td>
								<td width="200" align="left">
									<input type="submit" name="submit" class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
								</td>
								<td width="200" align="center">
									<input type="button" class="button" style="width:150px;" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
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