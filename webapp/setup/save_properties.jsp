<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="com.opencms.boot.*,java.util.*" %>

<%
	
	/* next page to be accessed */
	String nextPage = "run_import.jsp";
	
	/* true if properties are initialized */
	boolean setupOk = Bean.checkProperties();
	
	/* true if there are errors */
	boolean error = false;
	
	Vector errors = new Vector();

	if(setupOk)	{
		/* Save Properties to file "opencms.properties" */
		CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
		Utils.saveProperties(Bean.getProperties(),"opencms.properties",true);
		errors = Utils.getErrors();
		error = !errors.isEmpty();						
	}		

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

					<table border="0" width="600" cellpadding="5">
						<tr>
							<td align="center" valign="top" height="125">
								Saving properties...
								<%										
									if(error)	{
										out.print("<b>Failed</b><br>");
										out.println("<textarea rows='10' cols='50'>");
										for(int i = 0; i < errors.size(); i++)	{
											out.println(errors.elementAt(i));
											out.println("-------------------------------------------");
										}
										out.println("</textarea>");
										errors.clear();
									}
									else	{
										out.print("<b>Ok</b>");
									}											
								%>								
							</td>
						</tr>
						<tr>
							<td align="center">
								<b>Do you want to import the workplace?</b><br>
							</td>
						</tr>
						<tr>
							<td class="bold" align="center">				
								<input type="radio" name="importWorkplace" value="true" checked> Yes
								<input type="radio" name="importWorkplace" value="false" > No
							</td>
						</tr>
<% if (!Bean.getSetupType()) { %>
						<tr>
							<td align="center">
								<b>Do you want to use directory translation?</b><br>
								Activate this option if you want to import a site built with OpenCms versions prior 5.0 (beta 2).
							</td>
						</tr>
						<tr>
							<td class="bold" align="center">				
								<input type="radio" name="directoryTranslationEnabled" value="true" <%=Bean.isChecked(Bean.getDirectoryTranslationEnabled(),"true")%>> Yes
								<input type="radio" name="directoryTranslationEnabled" value="false" <%=Bean.isChecked(Bean.getDirectoryTranslationEnabled(),"false")%>> No
							</td>
						</tr>							
<% } %>						
					</table>
				</td>
			</tr>
			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
<% if (Bean.getSetupType()) { %>
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.go(-1)">
<% } else { %>
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.go(-2)">
<% } %>
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