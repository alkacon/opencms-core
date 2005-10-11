<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%
// next page 
String nextPage = "step_2_settings.jsp";

boolean isInitialized = false;
boolean wizardEnabled = false;
boolean showButtons = false;

try {
	if (Bean.isInitialized()) {
		session.invalidate();
		response.sendRedirect("index.jsp");
	}
	
	// Initialize the Bean 
	Bean.init(pageContext);

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
OpenCms Update Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<script type="text/javascript">
	function toggleButton(theFlag) {
		document.getElementById("continue").disabled = theFlag;
	}	
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Update Wizard - License Agreement
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
						<td><img src="resources/error.png" border="0"></td>
						<td>&nbsp;&nbsp;</td>
						<td>
							Error starting OpenCms update wizard.<br>
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
						<td><img src="resources/error.png" border="0"></td>
						<td>&nbsp;&nbsp;</td>
						<td style="width: 100%;">
							The OpenCms update wizard is not available!<br>
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
