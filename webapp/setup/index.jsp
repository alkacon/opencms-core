<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

// next page 
String nextPage = "step_2_check_components.jsp";

boolean isInitialized = false;
boolean wizardEnabled = false;
boolean showButtons = false;

try {
	if (Bean.isInitialized()) {
		session.invalidate();
		response.sendRedirect("index.jsp");
	}
	
	// Initialize the Bean 
	Bean.init(pageContext, request);

	// check wizards accessability 
	wizardEnabled = Bean.getWizardEnabled();
	
	if (!wizardEnabled) {
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
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<script type="text/javascript">
	function toggleButton(theFlag) {
		document.getElementById("continue").disabled = theFlag;
	}	
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - License Agreement
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<form action="<%= nextPage %>" method="post" class="nomargin">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%; padding-right: 3px;">
	<% if (wizardEnabled && isInitialized)	{ 
		showButtons = true; %>
		<tr>
			<td style="vertical-align: top;">
				<div class="dialoginnerboxborder"><div class="dialoginnerbox">	
				<iframe src="license.html" name="license" style="width: 100%; height: 310px; margin: 0; padding: 0; border-style: none;" frameborder="0"></iframe>
				</div></div>
			</td>
		</tr>
		<tr>
			<td style="vertical-align: bottom;padding-top: 12px;">
				<table border="0" cellpadding="0" cellspacing="0" style="vertical-align: bottom; height: 20px;">
				<tr>
					<td>Do you accept all the terms of the preceding license agreement?</td>
					<td>&nbsp;&nbsp;</td>
					<td style="width: 25px;"><input type="radio" name="agree" value="yes" onclick="toggleButton(false);"></td>
					<td> yes</td>
					<td>&nbsp;&nbsp;</td>
					<td style="width: 25px;"><input type="radio" name="agree" value="no" onclick="toggleButton(true);" checked="checked"></td>
					<td> no</td>
				</tr>
		
				</table>
			</td>
		</tr>
	<% } else if (! isInitialized) { %>
		<tr>
			<td style="vertical-align: middle;">
				<%= Bean.getHtmlPart("C_BLOCK_START", "Error starting wizard") %>
				<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
					<tr>
						<td><img src="resources/error.gif" border="0"></td>
						<td>&nbsp;&nbsp;</td>
						<td>
							Error starting OpenCms setup wizard.<br>
							It appears that your servlet container did not unpack the OpenCms WAR file.
							OpenCms requires that it's WAR file is unpacked.
							<br><br>
							<b>Please unpack the OpenCms WAR file and try again.</b>
							<br><br>
							Check out the documentation of your Servlet container to learn how to unpack the WAR file,
							or do it manually with some kind of unzip - tool.
							<br><br>
							Tip for Tomcat users:<br>
							Open the file <code>{tomcat-home}/conf/server.xml</code> and search
							for <code>unpackWARs="false"</code>. Replace this with <code>unpackWARs="true"</code>.
							Then restart Tomcat.
						</td>
					</tr>
				</table>
				<%= Bean.getHtmlPart("C_BLOCK_END") %>
			</td>
		</tr>
	<% } else { %>
		<tr>
			<td style="vertical-align: middle; height: 100%;">
				<%= Bean.getHtmlPart("C_BLOCK_START", "Wizard locked") %>
				<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
					<tr>
						<td><img src="resources/error.gif" border="0"></td>
						<td>&nbsp;&nbsp;</td>
						<td style="width: 100%;">
							The OpenCms setup wizard is not available!<br>
							To enable the wizard, unlock it in "opencms.properties".
						</td>
					</tr>
				</table>
				<%= Bean.getHtmlPart("C_BLOCK_END") %>
			</td>
		</tr>
	<% } %>

</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<% if (showButtons) { %>
<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" style="visibility: hidden;" disabled="disabled">
<input name="continue" id="continue" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" style="margin-left: 50px; visibility: hidden;" disabled="disabled">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<script type="text/javascript">
	toggleButton(true);
</script>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>