<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%

	/* next page to be accessed */
	String nextPage = "step_7_import_workplace.jsp";

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
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Properties
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>
<% if(setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">

		<table border="0" width="600" cellpadding="5">
			<tr>
				<td align="center" valign="top" height="125">
					Saving properties...
					<%
						if(error) {
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
			<tr>
				<td align="center">
					<b>Enter your servers ethernet address</b><br>
					You can leave this field empty, a random address will be generated.
				</td>
			</tr>
			<tr>
				<td align="center">
					<input type="text" name="ethernetAddress" value="<%= Bean.getEthernetAddress() %>">
				</td>
			</tr>
			<tr>
				<td align="center">
					<b>Enter the name of your OpenCms server</b><br>
					This name will be used for various messages.
				</td>
			</tr>
			<tr>
				<td align="center">
					<input type="text" name="serverName" value="<%= Bean.getServerName() %>">
				</td>
			</tr>
		</table>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.go(-2);">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<p><b>ERROR</b></p>
		The setup wizard has not been started correctly!<br>
		Please click <a href="">here</a> to restart the Wizard
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>