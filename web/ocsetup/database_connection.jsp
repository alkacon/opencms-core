<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Import packages */ %>
<%@ page import="source.org.apache.java.util.*,java.util.*" %>

<% /* Set all given properties */ %>
<jsp:setProperty name="Bean" property="*" />

<% 
	/* Initialize dbsetupscripts.properties */
	ExtendedProperties properties = new ExtendedProperties(Bean.getWorkFolder() + "dbsetupscripts.properties");
	
	/* next page to be accessed */
	String nextPage;
	if(Bean.getSetupType())	{
		nextPage= "advanced_1.jsp";
	}
	else	{
		nextPage= "create_database.jsp";
	}
%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->
<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="Stylesheet" type="text/css" href="style.css">
	
	<script language="Javascript">	
	<!--
		function disable()  {
		  	disabled = window.document.forms[0].extraWork[0].checked;	  	
	  		window.document.forms[0].dbWorkConStr.disabled = disabled;
	  		window.document.forms[0].dbWorkUser.disabled = disabled;
	  		window.document.forms[0].dbWorkPwd.disabled = disabled;
		}
	-->
	</script>
	
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
				<td height="375" align="center">
					<table border="0">
						<tr>
							<td colspan="2" valign="top">
								<table border="0" cellpadding="2" class="header">
									<tr>
										<td width="150" class="bold">
											Resource Broker
										</td>
										<td width="250">
											<select name="resourceBroker" style="width:250px;" size="1" width="250" onchange="location.href='database_connection.jsp?resourceBroker='+this.options[this.selectedIndex].value;">
											<!-- --------------------- JSP CODE --------------------------- -->
											<%
												/* 	List all resource broker found 
													in the dbsetupscripts.properties */
												Enumeration resourceBrokers = properties.keys();
												for(Enumeration e = properties.keys();e.hasMoreElements();)	{
													String key = e.nextElement().toString();
													String selected = "";													
													if(Bean.getResourceBroker().equals(key))	{
														selected = "selected";
													}
													out.println("<option value='"+key+"' "+selected+">"+key);
												}														
											%>
											<!-- --------------------------------------------------------- -->
											</select>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						<tr><td colspan="2">&nbsp;</td></tr>
						<tr>
							<td colspan="2">
								<table border="0" cellpadding="2" class="header">
									<tr>
										<td colspan="2" align="center">Database Setup Connection</td>
									</tr>
									<tr>
										<td width="150" class="bold">Conncection String:</td>
										<td width="250"><input type="text" name="dbSetupConStr" size="22" style="width:250px;" value='<%= Bean.getDbSetupConStr(true) %>'></td>
									</tr>
									<tr>
										<td class="bold">User:</td>
										<td><input type="text" name="dbSetupUser" size="22" style="width:250px;" value='<%= Bean.getDbSetupUser(true) %>'></td>
									</tr>
									<tr>
										<td class="bold">Password:</td>
										<td width="250"><input type="text" name="dbSetupPwd" size="22" style="width:250px;" value='<%= Bean.getDbSetupPwd(true) %>'></td>
									</tr>
								</table>
							</td>
						</tr>
						<tr><td colspan="2">&nbsp;</td></tr>						
						<tr>
							<td colspan="2">
								<table border="0" cellpadding="2">
									<tr>
										<td class="bold">
											Use setup conncetion as work connection ?
										</td>
									
										<td class="bold">
											<input type="radio" name="extraWork" value="false" <% if(!Bean.getExtraWork())	{ out.print("checked");} %> onclick="disable()"> YES
										</td>											
										<td class="bold">
											<input type="radio" name="extraWork" value="true"  <% if(Bean.getExtraWork())	{ out.print("checked");} %> onclick="disable()"> NO
										</td>
									
									</tr>
								</table>
							</td>
						</tr>						
						<tr>
							<td colspan="2">
								<table border="0" cellpadding="2" class="header">
									<tr>
										<td colspan="2" align="center">Database Work Connection</td>
									</tr>
									<tr>
										<td width="150" class="bold">Conncection String:</td>
										<td width="250"><input type="text" name="dbWorkConStr" size="22" style="width:250px;" value='<%= Bean.getDbWorkConStr() %>' disabled></td>
									</tr>
									<tr>
										<td class="bold">User:</td>
										<td><input type="text" name="dbWorkUser" size="22" style="width:250px;" value='<%= Bean.getDbWorkUser() %>' disabled></td>
									</tr>
									<tr>
										<td class="bold">Password:</td>
										<td width="250"><input type="text" name="dbWorkPwd" size="22" style="width:250px;" value='<%= Bean.getDbWorkPwd() %>' disabled></td>
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
			</form>
			</table>
		</td>
	</tr>
</table>
</td></tr>
</table>
</body>
</html>