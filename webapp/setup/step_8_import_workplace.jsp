<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%

    /* true if properties are initialized */
    boolean setupOk = (Bean.getProperties()!=null);

    /* check params */
	boolean importWp = false;
	String param = request.getParameter("importWorkplace");
	if(param != null)   {
		importWp = param.equals("true");
	}

	CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());

	Bean.checkEthernetAddress();

	/* Save Properties to file "opencms.properties" the 2nd time */
	Utils.saveProperties(Bean.getProperties(), "opencms.properties", true);

	// Restore the registry.xml either to or from a backup file
	Utils.backupRegistry("registry.xml", "registry.ori");

	/* next page */
	String nextPage = "step_9_browser_configuration_notes.jsp";
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>

<% if (importWp) { %>
</head>
<frameset rows="100%,*">
	<frame src="step_8a_display_import.jsp" name="display">
	<frame src="about:blank" name="data">
</frameset>
</html>
<%} else { %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Import workplace
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (setupOk) { %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: middle;">
		<%= Bean.getHtmlPart("C_BLOCK_START", "Import workplace") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			
			<tr>
				<td><img src="resources/warning.gif" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100%;">
					You have not imported the workplace.<br>
					<b>Warning:</b> OpenCms will not work without the virtual file system!
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.back();">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
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
<% } %>
