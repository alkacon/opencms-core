<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%

	/* next page to be accessed */
	String nextPage = "";

	/* request parameters */
	boolean submited = (request.getParameter("systemInfo") != null);
	boolean info = (request.getParameter("systemInfo") != null) && (request.getParameter("systemInfo").equals("false"));
	boolean accepted = (request.getParameter("accept") != null) && (request.getParameter("accept").equals("true"));


	/* Servlet engine */
	String servletEngine = "";
	boolean supportedServletEngine = false;
	int unsupportedServletEngine = -1;

	/* add supported engines here */
	String[] supportedEngines = {"Apache Tomcat/4.1", "Apache Tomcat/4.0", "Apache Tomcat/5.0"};

	/* add unsupported engines here */
	String[] unsupportedEngines = {"Tomcat Web Server/3.2", "Tomcat Web Server/3.3", "Resin/2.0.b2" };
	String[] unsEngMessages = {
		"OpenCms does not work correctly with Tomcat 3.2.x. Tomcat 3.2.x uses its own XML parser which results in major errors while using OpenCms. Please use Tomcat 4.x instead.",
		"Tomcat 3.3 is no longer supported. Please use Tomcat 4.x instead." ,
		"The OpenCms JSP integration does currently not work with Resin. Please use Tomcat 4.x instead."
	};

	/* JDK version */
	String requiredJDK = "1.4.0";
	String JDKVersion = "";
	boolean supportedJDK = false;


	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);

	if(setupOk) {

		if(submited) {
			nextPage = "step_3_database_selection.jsp";
		}
		else    {
			/* checking versions */
			servletEngine = config.getServletContext().getServerInfo();
			JDKVersion = System.getProperty("java.version");

			CmsSetupUtils.writeVersionInfo(servletEngine, JDKVersion, config.getServletContext().getRealPath("/"));
			supportedJDK = CmsSetupUtils.compareJDKVersions(JDKVersion, requiredJDK);
			supportedServletEngine = CmsSetupUtils.supportedServletEngine(servletEngine, supportedEngines);
			unsupportedServletEngine = CmsSetupUtils.unsupportedServletEngine(servletEngine, unsupportedEngines);
		}
	}

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Check components
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (setupOk) { %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: middle; height: 100%;">
		<%  if (submited) {
				if (info && !accepted) {
					out.print("<b>To continue the OpenCms setup you have to recognize that your system may not work with OpenCms!");
				}
				else {
					out.print("<script type='text/javascript'>document.location.href='" + nextPage + "';</script>");
				}
			} else { %>
		
		<%= Bean.getHtmlPart("C_BLOCK_START", "System components") %>	
		
		<table border="0" cellpadding="5" cellspacing="0" style="width: 100%;">
			<tr>
				<td style="text-align: left; width: 130px;">JDK version:</td>
				<td style="text-align: left; font-weight:bold; width: 300px;"><%= JDKVersion %></td>
				<td style="text-align: right; width: 200px;"><img src="resources/<% if(supportedJDK)out.print("check");else out.print("cross"); %>.gif"></td>
			</tr>
			<tr>
				<td style="text-align: left;">Servlet engine:</td>
				<td style="text-align: left; font-weight:bold;"><%= servletEngine %></td>
				<td style="text-align: right; width: 200px;"><img src="resources/<% if(supportedServletEngine)out.print("check");else if (unsupportedServletEngine > -1)out.print("cross");else out.print("unknown"); %>.gif"></td>
			</tr>
			<tr>
				<td style="text-align: left;">Operating system:</td>
				<td style="text-align: left; font-weight:bold;"><%= System.getProperty("os.name") + " " + System.getProperty("os.version") %></td>
				<td style="text-align: right; width: 200px;"><img src="resources/check.gif"></td>
			</tr>
		</table>
		
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
		
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
		
		<table border="0" cellpadding="5" cellspacing="0">
			<tr><td align="center" valign="bottom">
			<%
				boolean red = !supportedJDK || (unsupportedServletEngine > -1);
				boolean yellow = !supportedServletEngine;

				boolean systemOk = !(red || yellow);

				if(red) {
					out.print("<img src='resources/error.gif'>");
				}
				else if (yellow) {
					out.print("<img src='resources/warning.gif'>");
				}
				else {
					out.print("<img src='resources/ok.gif'>");
				}
			%>
			</td>
			<td colspan="2" valign="middle">
			<%
				if (red) {
					out.println("<p><b>Attention:</b> Your system does not have the necessary components to use OpenCms. It is assumed that OpenCms will not run on your system.</p>");
					if (unsupportedServletEngine > -1) {
						out.println("<p>"+unsEngMessages[unsupportedServletEngine]+"</p>");
					}
				}
				else if (yellow) {
					out.print("<b>Attention:</b> Your system uses components which have not been tested to work with OpenCms. It is possible that OpenCms will not run on your system.");
				}
				else {
					out.print("<b>Your system uses components which have been tested to work properly with OpenCms.</b>");
				}
			%></td>
			</tr>
			<tr><td colspan="3" height="30">&nbsp;</td></tr>
			<% if (!systemOk) { %>
				<tr><td colspan="3">
				<table border="0"><tr>
					<td style="vertical-align: top;"><input type="checkbox" name="accept" value="true"> </td>
					<td>I have noticed that my system may not have the necessary components to use OpenCms. Continue anyway.</td>
				</tr></table>
				</td></tr>
			<% } %>
		</table>
			
		<input type="hidden" name="systemInfo" value="<% if (systemOk) out.print("true");else out.print("false"); %>">
		<% } %>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='index.jsp';">
<%
String disabled = "";
if(submited && info && !accepted) {
	disabled = " disabled=\"disabled\"";
} %>
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton"<%= disabled %>>
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
