<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% 
boolean isInitialized = false;
boolean wizardEnabled = false;

/* next page to be accessed */
String nextPage = "step_1_content_encoding.jsp";
	
/* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<%
try { 	
	/* set the base path to the opencms home folder */
	Bean.setBasePath(config.getServletContext().getRealPath("/"));
		
	/* Initialize the properties */
	Bean.initProperties("opencms.properties");
	
	/* Initialize the name of the database */
	String appName = request.getContextPath().replaceAll("\\W","");
	if (appName != null && appName.length() > 0) {
		Bean.setAppName(appName);
	}
	
	/* check wizards accessability */
	wizardEnabled = Bean.getWizardEnabled();
	
	if(!wizardEnabled)	{
		request.getSession().invalidate();
	}
	
	isInitialized = true;
} catch (Exception e) {
	// the servlet container did not unpack the war, so lets display an error message
}
%>


<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="Stylesheet" type="text/css" href="resources/style.css">
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
                <td height="50" align="right"><img src="resources/opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			<% if(wizardEnabled && isInitialized)	{ %>
			<tr>
				<td align="center" valign="top" height="375">					
					<table border="0" width="600" cellpadding="5">
						<tr>
							<td align="center" valign="top" height="125" class="bold">Welcome to the OpenCms Setup Wizard</td>
						</tr>					
						<tr>
							<td align="center">Please press "continue" to start the setup wizard</td>
						</tr>
					</table>										
				</td>
			</tr>

			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
								<input type="button" class="button" style="width:150px;" width="150" disabled value="&#060;&#060; Back">
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
			<% } else if (! isInitialized) { %>			
			<tr>
				<td align="center" valign="top">
					<p><b>Error starting OpenCms setup wizard.</b></p>
					<p>
					It appears that your servlet container did not unpack the OpenCms WAR file.<br>
					OpenCms requires that it's WAR file is unpacked.
					</p><p>
					<b>Please unpack the OpenCms WAR file and try again.</b>
					</p><p>
					Check out the documentation of your Servlet container to learn how to unpack the WAR file,<br>
					or do it manually with some kind of unzip - tool.
					</p>
					<p>Tip for Tomcat users:<br>
					Open the file <code>{tomcat-home}/conf/server.xml</code> and search<br>
					for <code>unpackWARs="false"</code>. Replace this with <code>unpackWARs="true"</code>.<br>
					Then restart Tomcat.					
					</p>
				</td>
			</tr>					
			<% } else { %>				
			<tr>
				<td align="center" valign="top">
					<p><b>Sorry, wizard not available.</b></p>
					The OpenCms setup wizard has been locked!<br>
					To use the wizard again, unlock it in "opencms.properties".
				</td>
			</tr>			
			<% } %>									
		</form>
		</table>
	</td>
</tr>
</table>
</td></tr>
</table>
</body>
</html>