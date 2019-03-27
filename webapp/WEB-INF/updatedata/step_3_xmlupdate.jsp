<%@ page import="org.opencms.setup.xml.*,java.io.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><%
	
	// next page
	String nextPage = "step_4_module_selection.jsp";	
	// previous page
	String prevPage = "step_2_settings.jsp";
	
	boolean isFormSubmitted = (request.getParameter("submit") != null);
	
	if (Bean.isInitialized()) {
	  if (isFormSubmitted) {
		CmsXmlConfigUpdater configUpdater = Bean.getXmlConfigUpdater();
		configUpdater.transformConfig();
		response.sendRedirect(nextPage);
		return;
      }
	}	
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update-Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Update-Wizard - XML Configuration Files Update
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>
<form method="post" class="nomargin" name="xmlupdate" onSubmit="getSelectedPlugins2();">
<div>
<p>In this step the XML configuration files will be updated.</p>
<p>The update will apply the XSLT transformations listed in the file update/xmlupdate/transforms.xml. </p>
</div>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton" >
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
