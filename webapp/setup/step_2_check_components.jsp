<%@ page import="org.opencms.setup.*,java.util.*" %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:setProperty name="Bean" property="*" />
<%
	String nextPage = "step_3_database_selection.jsp";
	String prevPage = "index.jsp";

	boolean isSubmitted = (request.getParameter("systemInfo") != null);
	boolean hasSystemInfo = (request.getParameter("systemInfo") != null) && (request.getParameter("systemInfo").equals("false"));
	boolean hasUserAccepted = (request.getParameter("accept") != null) && (request.getParameter("accept").equals("true"));
	boolean isSetupOk = (Bean.getProperties() != null);

	CmsSetupTests setupTests = null;
	CmsSetupTestResult testResult = null;
	String resultIcon = null;
	String violatedConditions = "";
	String questionableConditions = "";

	if (isSetupOk) {
		if(!isSubmitted) {
			setupTests = new CmsSetupTests();
			setupTests.runTests(pageContext, Bean);
		}
	} else {
		Bean.initHtmlParts();
	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard - Check components
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Check components
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (isSetupOk) { %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 350px;">
<tr>
	<td style="vertical-align: middle; height: 100%;">
<%  
	if (isSubmitted) {
		if (hasSystemInfo && !hasUserAccepted) {
			out.print("<b>To continue the OpenCms setup you have to recognize that your system may not work with OpenCms!");
		} else {
			response.sendRedirect(nextPage);
		}
	} else { 	
%>	

		<%= Bean.getHtmlPart("C_BLOCK_START", "System components") %>		
		
		<table border="0" cellpadding="5" cellspacing="0" style="width: 100%;">
		
<%
		List testResults = setupTests.getTestResults();
		for (int i=0;i<testResults.size();i++) {
			testResult = (CmsSetupTestResult) testResults.get(i);
			
			if (testResult.isRed()) {
				resultIcon = "cross";
				violatedConditions += testResult.getInfo();
			} else if (testResult.isYellow()) {
				resultIcon = "unknown";
				questionableConditions += testResult.getInfo();
			} else {
				resultIcon = "check";
			}
%>
			<tr>
				<td style="text-align: left; width: 130px;"><%= testResult.getName() %>:</td>
				<td style="text-align: left; font-weight:bold; width: 300px;"><%= testResult.getResult() %></td>
				<td style="text-align: right; width: 200px;"><img src="resources/<%= resultIcon %>.gif"></td>
			</tr>
<%
		}	
%>
		</table>
		
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
		
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
		
		<table border="0" cellpadding="5" cellspacing="0">
			<tr><td align="center" valign="bottom">
			<%
				if(setupTests.isRed()) {
					out.print("<img src='resources/error.gif'>");
				} else if (setupTests.isYellow()) {
					out.print("<img src='resources/warning.gif'>");
				} else {
					out.print("<img src='resources/ok.gif'>");
				}
			%>
			</td>
			<td colspan="2" valign="middle">
			<%
				if (setupTests.isRed()) {
					out.println("<p><b>Attention:</b> Your system does not have the necessary components to use OpenCms. It is assumed that OpenCms will not run on your system.</p>");
					out.println(violatedConditions);
				} else if (setupTests.isYellow()) {
					out.print("<b>Attention:</b> Your system uses components which have not been tested to work with OpenCms. It is possible that OpenCms will not run on your system.");
					out.println(questionableConditions);
				} else {
					out.print("<b>Your system uses components which have been tested to work properly with OpenCms.</b>");
				}
			%></td>
			</tr>
			<tr><td colspan="3" height="30">&nbsp;</td></tr>
			<% if (!setupTests.isGreen()) { %>
				<tr><td colspan="3">
				<table border="0"><tr>
					<td style="vertical-align: top;"><input type="checkbox" name="accept" value="true"> </td>
					<td>I have noticed that my system may not have the necessary components to use OpenCms. Continue anyway.</td>
				</tr></table>
				</td></tr>
			<% } %>
		</table>
			
		<input type="hidden" name="systemInfo" value="<%= setupTests.isGreen() %>">
		<% } %>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<%
String disabled = "";
if (isSubmitted && hasSystemInfo && !hasUserAccepted) {
	disabled = " disabled=\"disabled\"";
} %>
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton"<%= disabled %>>
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>