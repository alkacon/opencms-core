<%@page import="java.util.*" %>

<% /* Initialize the setup bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />
<jsp:setProperty name="Bean" property="*" />

<%
/* next page in the setup process */
String nextPage = "database_connection.jsp";

// Reading the system properties
Properties vmProperties = System.getProperties();

String vmEncoding = vmProperties.getProperty("file.encoding");
boolean encodingOk = Bean.getDefaultContentEncoding().equalsIgnoreCase(vmEncoding);

//boolean encodingEditable = Bean.getSetupType();
boolean encodingEditable = true;

if (encodingOk) {
	response.sendRedirect(nextPage);
}
%>

<html>
<head> 
<title>OpenCms Setup Wizard</title>
<script type="text/javascript" language="JavaScript">
<!--
function runSubmit() {
	var form = document.encoding;
	var vmEncoding = "<%= vmEncoding %>";
	var ocEncoding = "" + form.defaultContentEncoding.value;
	
	if (vmEncoding == ocEncoding) {
		return true;
	}
	
	alert( "The character encoding of your Java VM is different from the default OpenCms encoding!" );
	return false;
}
// -->
</script>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="Stylesheet" type="text/css" href="style.css">
</head>

<body>
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
	<td><form name="encoding" action="<%= nextPage %>" method="post" onSubmit="return runSubmit()">
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
							<td class="bold">
							Character encoding properties:
							</td>
						</tr>				
						
						<tr><td>&nbsp;</td></tr>
						
						<tr>
							<td align="center">
								<table border="0" cellpadding="5" cellspacing="0" class="header">
									<tr>
									<td>Java VM encoding:</td><td align="left"><%= vmEncoding %></td>
									</tr>
									<tr>
<% if (encodingEditable) { %>
									<td>OpenCms encoding:</td><td align="left"><input type="text" name="defaultContentEncoding" size="22" onKeyup="checkEncoding();" style="width:125px;" value='<%= Bean.getDefaultContentEncoding() %>'></td>
<% } else { %>
									<td>OpenCms encoding:</td><td align="left"><%= Bean.getDefaultContentEncoding() %></td>
<% } %>
									</tr>									
								</table>
							</td>
						</tr>	
						
<% if (!encodingOk) { %>
						<tr><td>&nbsp;</td></tr>
						<tr><td class="bold"><font color="FF0000">Error:</font> the character encoding of your Java VM is different from the default OpenCms encoding!</td></tr>
						<tr><td>&nbsp;</td></tr>
						<tr><td>
						To continue the setup process:
						<ul>
						<li>change the setting for the default character encoding of OpenCms,</li>
						<li><b>OR</b> change the character encoding of your Java VM, and restart the setup wizard. Using Apache Tomcat, a different
						character encoding is set in the environment variable CATALINA_OPTS using the -D parameter: <pre>CATALINA_OPTS=-Dfile.encoding=ISO-8859-1</pre></li>
						</ul>
						Please refer to the <a href="http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html" target="_blank">Sun documentation</a> for a list of supported encodings.
						</td></tr>
<% } %>
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
<% if (encodingOk || encodingEditable) { %>							
							<td width="200" align="left">
								<input type="submit" name="submit" class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
							</td>
<% } %>							
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