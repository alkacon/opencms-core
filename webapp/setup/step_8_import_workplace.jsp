<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "step_9_browser_configuration_notes.jsp";	
	// previous page 
	String prevPage = "index.jsp";
	
	boolean importWp = Bean.prepareStep8(request.getParameter("importWorkplace"));	

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard - Import workplace
<%= Bean.getHtmlPart("C_HEAD_START") %>

<% if (importWp) { %>
</head>
<frameset rows="100%,*">
	<frame src="step_8a_display_import.jsp" name="display">
	<frame src="about:blank" name="data">
</frameset>
</html>
<%} else { %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Import workplace
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (Bean.isInitialized()) { %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: middle;">
		<%= Bean.getHtmlPart("C_BLOCK_START", "Import workplace") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			
			<tr>
				<td><img src="resources/warning.gif" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100%;">
					You have not imported the workplace.<br>
					OpenCms will not work without the virtual file system!
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="top.document.location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
<% } %>
