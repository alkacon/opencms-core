<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<%	
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);

	/* next page to be accessed */
	String nextPage = "advanced_rdparams.jsp";
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
								<p>Exportpoint parameters</p>
								<table border="0">
								<%
									int expPointNr = 0;
									while (!"".equals(Bean.getExportPoint(expPointNr)))	{
								%>
									<tr>
										<td width="150">
											Exportpoint <%= ""+expPointNr %>
										</td>
										<td>
											<input type="text" size="30" style="width:300px;" name="exportPoint<%= ""+expPointNr %>" value="<%= Bean.getExportPoint(expPointNr) %>">
										</td>
									</tr>
								<%
									if(!"".equals(Bean.getExportPointPath(expPointNr)))	{
								%>
									<tr>
										<td>
											Exportpoint path <%= ""+expPointNr %>
										</td>
										<td>
											<input type="text" size="30" style="width:300px;" name="exportPointPath<%= ""+expPointNr %>" value="<%= Bean.getExportPointPath(expPointNr) %>">
										</td>
									</tr>
								<%
									}
									out.println("<tr><td colspan='2'><hr></td></tr>");
									expPointNr++;
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