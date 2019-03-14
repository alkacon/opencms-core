<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page 
	String nextPage = "step_5_database_creation.jsp";	
	// previous page 
	String prevPage = "step_3_database_selection.jsp";

	CmsSetupDb db = null;
	boolean enableContinue = false;
	String chkVars = null;
	List conErrors = null;

	if (Bean.isInitialized()) {
		db = new CmsSetupDb(Bean.getWebAppRfsPath());
		// try to connect as the runtime user
		db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbConStrParams(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
		if (!db.noErrors()) {
		    // try to connect as the setup user
		    db.closeConnection();
			db.clearErrors();
			db.setConnection(Bean.getDbDriver(), Bean.getDbCreateConStr(), Bean.getDbConStrParams(), Bean.getDbCreateUser(), Bean.getDbCreatePwd());
		}
		conErrors = new ArrayList(db.getErrors());
		db.clearErrors();
		enableContinue = conErrors.isEmpty();
		chkVars = db.checkVariables(Bean.getDatabase());
		db.closeConnection();
		if (enableContinue && db.noErrors() && chkVars == null && Bean.validateJdbc()) {
			response.sendRedirect(nextPage);
			return;
		}
	}
%><%= Bean.getHtmlPart("C_HTML_START") %>
Alkacon OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
Alkacon OpenCms Setup Wizard - Validate database connection
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (Bean.isInitialized())	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 350px;">
<tr>
	<td style="vertical-align: middle;">
				<%
					if (!enableContinue) {
						%>
						<%= Bean.getHtmlPart("C_BLOCK_START", "Creating Database Connection") %>
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td><img src="resources/error.png" border="0"></td>
								<td>&nbsp;&nbsp;</td>
								<td>It was not possible to create a database connection with the given parameters.<br>
									Please check the Exception below. There can be two reasons for this error:
									<ul>
									  <li><b>Your database is down</b>, or</li>
									  <li><b>Your database is not accessible with the given connection parameters.</b></li>
									</ul>
									Be also aware that Alkacon recommends to use the
									following JDBC drivers for <%=Bean.getDatabaseName(Bean.getDatabase())%>:<br>
									<code><%=Bean.getDatabaseLibs(Bean.getDatabase()).toString()%></code><p>
									Check that the Jdbc drivers are included in your class path.
								</td>
							</tr>
							<tr>
								<td colspan='2'>&nbsp;&nbsp;</td>
								<td style="width: 100%;">
									<div style="width: 100%; height:200px; overflow: auto;">
									<%
									for (int i = 0; i < conErrors.size(); i++)	{
										out.println(conErrors.get(i) + "<br>");
										out.println("-------------------------------------------" + "<br>");
									}
							 		%>
									</div>
								</td>
							</tr>				
						</table>
						<%= Bean.getHtmlPart("C_BLOCK_END") %>
						<%
					} else {
						if (!Bean.validateJdbc()) {
							%>
							<%= Bean.getHtmlPart("C_BLOCK_START", "Validating Jdbc Drivers") %>
							<table border="0" cellpadding="0" cellspacing="0">
								<tr>
									<td><img src="resources/warning.png" border="0"></td>
									<td>&nbsp;&nbsp;</td>
									<td>Be aware that Alkacon recommends to use the
										following JDBC drivers for <%=Bean.getDatabaseName(Bean.getDatabase())%>:<br>
										<code><%=Bean.getDatabaseLibs(Bean.getDatabase()).toString()%></code><p>
										<b>But these drivers are not located in folder <code><%=Bean.getLibFolder()%></code></b><p>
										<i>If you are using a different driver or if you added the driver in another way 
										   to the classpath, you may continue to try it out. If <b>not</b>, be sure to get the 
										   drivers and restart your servlet container before you continue.</i>
									</td>
								</tr>
							</table>
							<%= Bean.getHtmlPart("C_BLOCK_END") %>
							<%
						}					
						if (!db.noErrors() || chkVars != null)	{ %>
							<%= Bean.getHtmlPart("C_BLOCK_START", "Validating Database Server Configuration") %>
							<table border="0" cellpadding="0" cellspacing="0"><%
						    boolean isError = !db.noErrors();
							enableContinue = enableContinue && !isError;
							if (chkVars != null) {%>
								<tr>
									<td><img src="resources/warning.png" border="0"></td>
									<td>&nbsp;&nbsp;</td>
									<td><%=chkVars%></td>
								</tr><%
							}
							if (!db.noErrors()) {%>
								<tr>
									<td><img src="resources/error.png" border="0"></td>
									<td>&nbsp;&nbsp;</td>
									<td style="width: 100%;">
										<div style="width: 100%; height:140px; overflow: auto;">
										<p style="margin-bottom: 4px;">Error while checking the server configuration!</p>
										<%
										out.println("-------------------------------------------" + "<br>");
										List<String> errors = db.getErrors();
										Iterator<String> it = errors.iterator();
										while (it.hasNext())	{
											out.println(it.next() + "<br>");
										}
										db.clearErrors();
										%>
										</div>
									</td>
								</tr><%
							}%>
							</table>
							<%= Bean.getHtmlPart("C_BLOCK_END") %>
							<%
						}
					}
				%>

	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="btcontinue" type="submit" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled" id="btcontinue">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<% if (enableContinue)	{
	out.println("<script type=\"text/javascript\">\ndocument.getElementById(\"btcontinue\").disabled = false;\n</script>");
} %>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
