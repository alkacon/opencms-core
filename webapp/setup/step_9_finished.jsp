<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->

<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*" %>

<%
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);

	/* get params */
	boolean understood = false;
	String temp = request.getParameter("understood");
	if(temp != null)	{
		understood = temp.equals("true");
	}	
	
	if(setupOk)	{

		if(understood)	{
			/* lock the wizard for further use */
			Bean.lockWizard();
		
			/* Save Properties to file "opencms.properties" */
			CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
			Utils.saveProperties(Bean.getProperties(),"opencms.properties",false);		
			
			/* invalidate the sessions */
			request.getSession().invalidate();			
		}
	
	}
	
	/* next page to be accessed */
	String nextPage = "";	
	
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
	<td><form method="POST">	
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
                <td height="50" align="right"><img src="resources/opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			<% if(setupOk)	{ %>
			<tr>
				<td height="375" align="center" valign="middle">
					<% if(understood)	{ %>
					<p><b>OpenCms setup finished.</b><br>					
					The wizard is now locked. To use the wizard again reset the flag in the "opencms.properties".</p>
					<p>To start OpenCms click <a target="_blank" href="<%= request.getContextPath() %>/opencms/index.jsp">here</a>.</p>
					<% } else { %>
						<b>Please confirm that you have read the configuration notes</b>
					<% } %>
				</td>
			</tr>			
			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
							<% if (understood)	{ %>							
								<input type="button" disabled class="button" style="width:150px;" width="150" value="&#060;&#060; Back">
							<% } else { %>
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.back();">
							<% } %>
							</td>
							<td width="200" align="left">
								<input type="button" disabled class="button" style="width:150px;" width="150" value="Finish">
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