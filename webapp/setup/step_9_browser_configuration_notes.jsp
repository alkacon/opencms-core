<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "step_10_finished.jsp";
	// previous page
	String prevPage = "step_7_save_properties.jsp";

%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<script type="text/javascript">
	function toggleButton(theFlag) {
		document.getElementById("continue").disabled = theFlag;
	}
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - Browser configuration
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if(Bean.isInitialized())	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; padding-right: 3px;">
	<tr>
		<td style="vertical-align: top;">
			<div class="dialoginnerboxborder"><div class="dialoginnerbox">
			<iframe src="browser_config.html" name="config" style="width: 100%; height: 310px; margin: 0; padding: 0; border-style: none;" frameborder="0"></iframe>
			</div></div>
		</td>
	</tr>
	<tr>
		<td style="vertical-align: bottom;padding-top: 12px;">
			<table border="0" cellpadding="0" cellspacing="0" style="vertical-align: bottom; height: 20px;">
			<tr>
				<td>Did you read these important configuration notes?</td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 25px;"><input type="radio" name="understood" value="true" onclick="toggleButton(false);"></td>
				<td> yes</td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 25px;"><input type="radio" name="understood" value="no" onclick="toggleButton(true);" checked="checked"></td>
				<td> no</td>
			</tr>

			</table>
		</td>
	</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';" disabled="disabled">
<input name="continue" id="continue" type="submit" value="Finish" class="dialogbutton" disabled="disabled">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>