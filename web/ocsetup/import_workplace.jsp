<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="com.opencms.boot.*" %>

<%	
	if((request.getParameter("createDb") != null) && request.getParameter("createDb").equals("true"))	{
	
		/* clear Vector */
		Bean.getErrors().clear();
		
		/* Create Database */
		CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
		Utils.createDatabase(Bean.getDbDriver(), Bean.getDbSetupConStr(false), Bean.getDbSetupUser(false),
				Bean.getDbSetupPwd(false), Bean.getResourceBroker());			
	}		

	/* true if there are errors */
	boolean error = (Bean.getErrors().size() > 0);
	
	/* next page to be accessed */
	String nextPage = "run_import.jsp";
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
				<td height="375" align="center" valign="top">
					<table border="0" width="600" cellpadding="5">
						<tr>
							<td align="center" valign="top" height="125" class="bold">				
							<%
								if(request.getParameter("createDb").equals("false"))	{
									out.println("Database has not been created");
								}
								else {	
									out.print("Creating Database...");
									if(error)	{
										out.println("ERROR<br>");
										out.println("<textarea rows='10' cols='50' style='width:600px;height:200px;' readonly wrap='off'>");
										for(int i = 0; i < Bean.getErrors().size(); i++)	{
											out.println(Bean.getErrors().elementAt(i));
											out.println("-------------------------------------------");													
										}
										out.println("</textarea><br>");												
										Bean.getErrors().clear();
									}
									else	{
										out.println("OK");
									}
								}
							%>
							</td>
						</tr>
						<tr><td>&nbsp;</td></tr>
						<tr>
							<td align="center" class="bold">
								To complete the setup you have to import the workplace.<br><br>
								Click 'Finish' to import the workplace.
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
								<input type="button" class="button" style="width:150px;" width="150" value="Finish" onclick="location.href='run_import.jsp'">
							</td>
							<td width="200" align="center">
								<input type="button" class="button" style="width:150px;" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
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