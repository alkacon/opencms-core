<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<%	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
%>

<% /* Import packages */ %>
<%@ page import="java.util.*" %>

<% /* Set all given properties */ %>
<jsp:setProperty name="Bean" property="*" />

<% 

	/* next page to be accessed */
	String nextPage ="";
	if(setupOk)	{
		if(Bean.getSetupType())	{
			nextPage= "advanced_1.jsp";
		}
		else	{
			nextPage= "create_database.jsp";
		}
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
					<table border="0">
						<tr>
							<td align="center">
								<table border="0" cellpadding="2">
									<tr>
										<td width="150" class="bold">
											Resource Broker
										</td>
										<td width="250">
											<select name="resourceBroker" style="width:250px;" size="1" width="250" onchange="location.href='database_connection.jsp?resourceBroker='+this.options[this.selectedIndex].value;">
											<!-- --------------------- JSP CODE --------------------------- -->
											<%
												/* get all available resource brokers */
												Vector resourceBrokers = Bean.getResourceBrokers();
												/* 	List all resource broker found in the dbsetup.properties */
												if (resourceBrokers !=null && resourceBrokers.size() > 0)	{
													for(int i=0;i<resourceBrokers.size();i++)	{
														String rb = resourceBrokers.elementAt(i).toString();
														String selected = "";													
														if(Bean.getResourceBroker().equals(rb))	{
															selected = "selected";
														}
														out.println("<option value='"+rb+"' "+selected+">"+rb);
													}
												}
												else	{
													out.println("<option value='null'>no resource broker found");
												}
											%>
											<!-- --------------------------------------------------------- -->
											</select>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						<tr><td>&nbsp;</td></tr>
						
						<tr>
							<td>
								<table border="0" cellpadding="5" class="header">
									<tr><td>&nbsp;</td><td>Conncection String</td><td>User</td><td>Password</td></tr>
									<tr>
										<td>Create Database Connection</td><td><input type="text" name="dbCreateConStr" size="22" style="width:250px;" value='<%= Bean.getDbCreateConStr() %>'></td>
										<td><input type="text" name="dbCreateUser" size="10" style="width:100px;" value='<%= Bean.getDbCreateUser() %>'></td>
										<td><input type="text" name="dbCreatePwd" size="10" style="width:100px;" value='<%= Bean.getDbCreatePwd() %>'></td>
									</tr>									
									<tr>
										<td>Create Tables Connection</td><td><input type="text" name="dbSetupConStr" size="22" style="width:250px;" value='<%= Bean.getDbSetupConStr() %>'></td>
										<td><input type="text" name="dbSetupUser" size="10" style="width:100px;" value='<%= Bean.getDbSetupUser() %>'></td>
										<td><input type="text" name="dbSetupPwd" size="10" style="width:100px;" value='<%= Bean.getDbSetupPwd() %>'></td>
									</tr>
									<tr>
										<td>OpenCms Work Connection</td><td><input type="text" name="dbWorkConStr" size="22" style="width:250px;" value='<%= Bean.getDbWorkConStr() %>'></td>
										<td><input type="text" name="dbWorkUser" size="10" style="width:100px;" value='<%= Bean.getDbWorkUser() %>'></td>
										<td><input type="text" name="dbWorkPwd" size="10" style="width:100px;" value='<%= Bean.getDbWorkPwd() %>'></td>
									</tr>									
								</table>
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
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.back();">
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
</td></tr>
</table>
</body>
</html>