<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:useBean id="Thread" class="org.opencms.setup.CmsSetupThread" scope="session"/>

<%	
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
	
	/* next page to be accessed */
	String nextPage = "step_9_finished.jsp";
	
	/* stop possible running threads */
	Thread.stopLoggingThread();		
	Thread.stop();	
	
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Browser configuration
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>
<% if(setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
	<tr>
		<td align="center" valign="top">	
		<iframe src="browser_config.html" name="config" width="600" height="280" style="margin: 0; padding: 0;" frameborder="0"></iframe>

<p><input type="checkbox" name="understood" value="true"><b>I have understood how to configure my browser</b></p>

</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.back();">
<input name="submit" type="submit" value="Finish" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>
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