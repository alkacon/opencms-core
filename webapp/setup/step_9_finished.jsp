<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*" %>

<%
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties() != null);

	/* get params */
	boolean understood = false;
	String temp = request.getParameter("understood");
	if (temp != null)	{
		understood = temp.equals("true");
	}

	if(setupOk)	{

		if(understood)	{
			/* lock the wizard for further use */
			Bean.lockWizard();

			/* Save Properties to file "opencms.properties" */
			CmsSetupUtils Utils = new CmsSetupUtils(Bean.getBasePath());
			Utils.saveProperties(Bean.getProperties(),"opencms.properties",false);

			/* invalidate the sessions */
			request.getSession().invalidate();
		}

	}

	/* next page to be accessed */
	String nextPage = "";

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Finished
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>
<% if(setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="middle">
		<% if (understood) { %>
		<p><b>OpenCms setup finished.</b><br>
		The wizard is now locked. To use the wizard again reset the flag in the "opencms.properties".</p>
		<p>To start OpenCms click <a target="_blank" href="<%= request.getContextPath() %>/opencms/index.jsp">here</a>.</p>
		<% } else { %>
			<b>Please confirm that you have read the configuration notes</b>
		<% } %>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<% if (understood)	{ %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" disabled="disabled">
<% } else { %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.back();">
<% } %>
<input name="submit" type="button" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" style="margin-left: 50px;" disabled="disabled">
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