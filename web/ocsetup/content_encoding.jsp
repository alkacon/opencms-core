<%@page import="java.util.*" %><%--

--%><% /* Initialize the setup bean */ %><%--
--%><jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%--

--%><%
/* next page in the setup process */
String nextPage = "database_connection.jsp";

// Reading the system properties
Properties vmProperties = System.getProperties();

String vmEncoding = vmProperties.getProperty("file.encoding");
boolean encodingOk = Bean.getDefaultContentEncoding().equalsIgnoreCase(vmEncoding);

if (encodingOk) {
	response.sendRedirect(nextPage);
}
%><%--

--%><html>
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
	<td><form>
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">	
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
				<td height="50" align="right"><img src="opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			
			<tr>
				<td height="375" align="center" valign="top">				
					<table border="0" width="500">
						<tr>
						<td class="bold" align="center"><font color="FF0000">Error:</font> the encoding of your Java VM is different from the OpenCms encoding!</td>						
						</tr>	
						<tr><td>&nbsp;</td></tr>
						<tr>
							<td align="center">
								<table border="0" cellpadding="5" cellspacing="0" class="header">
									<tr>
									<td>Java VM file encoding:</td><td align="left"><%= vmEncoding %></td>
									</tr>
									<tr>
									<td>OpenCms encoding:</td><td align="left"><%= Bean.getDefaultContentEncoding() %></td>
									</tr>									
								</table>
							</td>
						</tr>	
						<tr><td>&nbsp;</td></tr>
						<tr><td>
						<table border="0" cellpadding="10" cellspacing="0">
							<tr>
							<td valign="middle"><img src="ampel_rot.gif" width="75" height="150"></td>
							<td>
							To continue the setup process:
							<ul>
							<li>Change the encoding of your Java VM. 
							To do that you must modify the <tt>file.encoding</tt> setting.
							Using Apache Tomcat, a different encoding can be set in the environment 
							variable <tt>CATALINA_OPTS</tt> by the -D parameter e.g.:<br><tt>CATALINA_OPTS=-Dfile.encoding=ISO-8859-1</tt></li>
							<p>
							<li>If you want to use an encoding different from <b>ISO-8859-1</b>, you must also
							adjust the <tt>defaultContentEncoding</tt> setting in <tt>WEB-INF/config/opencms.properties</tt>.</li>
							</ul>
							Unless you have specific encoding requirements, you should use the default <b>ISO-8859-1</b> setting.
							Please refer to the <a href="http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html" target="_blank">Sun documentation</a> for a list of supported encodings for your OS.
							</td>
							</tr>
						</table>
						</td></tr>
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
								<input type="submit" class="button" style="width:150px;" width="150" disabled value="Continue &#062;&#062;" name="submit" >
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