<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" /><%--
--%><%@ page import="org.opencms.setup.*" %><%--

--%><%
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties() != null);
	
	String openLink = request.getContextPath() + "/opencms/index.jsp";

	if (setupOk) {
		/* lock the wizard for further use */
		Bean.lockWizard();

		/* Save Properties to file "opencms.properties" */
		CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
		Utils.saveProperties(Bean.getProperties(),"opencms.properties",false);

		/* invalidate the sessions */
		request.getSession().invalidate();
	} else {
		Bean.initHtmlParts();
	}

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<script type="text/javascript">
function openWin() {
	var theWindow = window.open("<%= openLink %>", "OpenCms", "top=10,left=10,width=780,height=550,location=yes,menubar=yes,resizable=yes,scrollbars=yes,status=yes,toolbar=yes");
	theWindow.focus();
}
<% if (setupOk) { %>
openWin();
<% } %>
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Finished
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (setupOk) { %>

<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: bottom;">
	
		<%= Bean.getHtmlPart("C_BLOCK_START", "OpenCms setup finished") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td><img src="resources/ok.gif" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100%;">
					To start OpenCms click <a target="OpenCms" href="<%= openLink %>">here</a>.
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
	</td>	
</tr>
<tr>
	<td style="vertical-align: top;">
		<%= Bean.getHtmlPart("C_BLOCK_START", "Wizard locked") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td><img src="resources/warning.gif" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td>
					This setup wizard is now locked.<br>
					To use the wizard again reset the flag in the "opencms.properties".
					For security reasons, you should remove the "/setup" folder later when
					you have your OpenCms installation running.
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
	</td>
</tr>
</table>


<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<form action="" method="post" class="nomargin">
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" disabled="disabled">
<input name="submit" type="button" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" style="margin-left: 50px;" disabled="disabled">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>