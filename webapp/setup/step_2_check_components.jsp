<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "step_3_database_selection.jsp";	
	// previous page
	String prevPage = "index.jsp";

	boolean isSubmitted = (request.getParameter("systemInfo") != null);
	boolean hasSystemInfo = (request.getParameter("systemInfo") != null) && (request.getParameter("systemInfo").equals("false"));
	boolean hasUserAccepted = (request.getParameter("accept") != null) && (request.getParameter("accept").equals("true"));

	String descriptions = "";
	CmsSetupTests setupTests = null;
	CmsSetupTestResult testResult = null;
	String resultIcon = null;
	String helpIcon = null;
	String violatedConditions = "";
	String questionableConditions = "";

	if (Bean.isInitialized()) {
		if(!isSubmitted) {
			setupTests = new CmsSetupTests();
			setupTests.runTests(pageContext, Bean);
		} else {
			response.sendRedirect(nextPage);
		}
	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard - Component tests
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<script type="text/javascript" language="JavaScript">
<!--

function toggleContinueButton() {
	var form = document.components;	
	form.submit.disabled = !form.accept.checked;
}

//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Component tests
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (Bean.isInitialized()) { %>
<form action="<%= nextPage %>" method="post" class="nomargin" name="components">
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 350px;">
<tr>
	<td style="vertical-align: top; height: 100%;">
<%  
	if (isSubmitted) {
		if (hasSystemInfo && !hasUserAccepted) {
			out.print("<b>To continue the OpenCms setup you have to recognize that your system may not work with OpenCms!");
		}
	} else { 	
%>	
		
		<%= Bean.getHtmlPart("C_BLOCK_START", "Component tests") %>	
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;"><tr><td>
		<div style="width: 100%; height:130px; overflow: auto;">
		<table border="0" cellpadding="2">
		
<%
		List testResults = setupTests.getTestResults();
		for (int i=0;i<testResults.size();i++) {
			testResult = (CmsSetupTestResult) testResults.get(i);
			
			if (testResult.isRed()) {
				resultIcon = "cross";
				violatedConditions += "<p>" + testResult.getInfo() + "</p>";
			} else if (testResult.isYellow()) {
				resultIcon = "unknown";
				questionableConditions += "<p>" + testResult.getInfo() + "</p>";
			} else {
				resultIcon = "check";
			}
					
			if (!testResult.isGreen() && testResult.getHelp() != null && !"".equals(testResult.getHelp())) {
				descriptions += Bean.getHtmlPart("C_HELP_START", "" + i) + testResult.getHelp() + Bean.getHtmlPart("C_HELP_END");
				helpIcon = Bean.getHtmlHelpIcon("" + i, "");
			} else {
				helpIcon = "";
			}
			
%>
			<tr>
				<td style="text-align: left; white-space: nowrap;"><%= testResult.getName() %>:</td>
				<td style="text-align: left; font-weight:bold; width: 100%;"><%= testResult.getResult() %></td>
				<td style="text-align: right; width: 40px; height: 16px;"><%= helpIcon %>&nbsp;<img src="resources/<%= resultIcon %>.gif" border="0"></td>
			</tr>
<%
		}	
%>
		</table>
		</div>
		</td></tr></table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
		
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
		
		<div style="width: 100%; height:120px; overflow: auto;">
		<table border="0" cellpadding="5" cellspacing="0">
			<tr><td align="center" valign="top">
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
					out.println("<p>Your system does not have the necessary components to use OpenCms. It is assumed that OpenCms will not run on your system.</p>");
					out.println(violatedConditions);
				} else if (setupTests.isYellow()) {
					out.print("Your system uses components which have not been tested to work with OpenCms. It is possible that OpenCms will not run on your system.");
					out.println(questionableConditions);
				} else {
					out.print("<b>Your system uses components which have been tested to work properly with OpenCms.</b>");
				}
			%></td>
			</tr>
		</table>
		</div>
		
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
		
		<table border="0" cellpadding="2" cellspacing="0">
			<% if (!setupTests.isGreen()) { %>
				<tr><td>
				<table border="0"><tr>
					<td style="vertical-align: top;"><input type="checkbox" name="accept" value="true" onClick="toggleContinueButton()"> </td>
					<td style="padding-top: 5px;">I have noticed that my system may not have the necessary components to use OpenCms. Continue anyway.</td>
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
if (!setupTests.isGreen() && !hasUserAccepted) {
	disabled = " disabled=\"disabled\"";
}
%>
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton"<%= disabled %>>
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>

<%= descriptions %>

<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>