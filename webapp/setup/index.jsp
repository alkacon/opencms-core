<%
boolean isInitialized = false;
boolean wizardEnabled = false;
boolean showButtons = false;

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


<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>

<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
	<% if (wizardEnabled && isInitialized)	{ 
		showButtons = true; %>
		<tr>
			<td style="text-align: center; font-weight: bold;">Welcome to the OpenCms Setup Wizard</td>
		</tr>
		<tr>
			<td style="text-align: center; vertical-align: top;">Please press "continue" to start the setup wizard</td>
		</tr>
	<% } else if (! isInitialized) { %>
		<tr>
			<td style="text-align: center; font-weight: bold;">Error starting OpenCms setup wizard.</td>
		</tr>
		<tr>
			<td style="text-align: center; vertical-align: top;">
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
			<td style="text-align: center; font-weight: bold;">Sorry, wizard not available.</td>
		</tr>
		<tr>
			<td style="text-align: center; vertical-align: top;">
				The OpenCms setup wizard has been locked!<br>
				To use the wizard again, unlock it in "opencms.properties".
			</td>
		</tr>
	<% } %>

</table>


<%= Bean.getHtmlPart("C_CONTENT_END") %>

<% if (showButtons) { %>
<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" disabled="disabled">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>