<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:useBean id="Thread" class="org.opencms.setup.CmsSetupThread" scope="session"/>

<%
	/* stop possible running threads */
	Thread.stopLoggingThread();
	Thread.stop();

	/* invalidate the sessions */
	request.getSession().invalidate();

	/* next page to be accessed */
	String nextPage = "";
%>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Setup cancelled
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="middle">
		<strong>Setup cancelled!</strong><br><br>Click <a href="index.jsp">here</a> to restart setup
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" disabled="disabled">
<input name="submit" type="button" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" style="margin-left: 50px;" disabled="disabled">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<%= Bean.getHtmlPart("C_HTML_END") %>