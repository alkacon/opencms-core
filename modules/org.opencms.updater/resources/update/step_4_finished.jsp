<%@ page import="org.opencms.update.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.update.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	String openLink = request.getContextPath() + "/opencms/index.jsp";
	if (Bean.isInitialized()) {
		Bean.prepareStep4();
		session.invalidate();
	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Update Wizard - Finished
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (Bean.isInitialized()) { %>

<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: bottom;">
	
		<%= Bean.getHtmlPart("C_BLOCK_START", "OpenCms update finished") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td><img src="resources/ok.png" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100%;">					
				    Your OpenCms instance has been update successfully.<br>
					Please restart your servlet container now, and continue working on OpenCms as usual.<br>
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
	</td>	
</tr>
<tr>
	<td style="vertical-align: top;">
		<%= Bean.getHtmlPart("C_BLOCK_START", "Wizard locked") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td><img src="resources/warning.png" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td>
					This update wizard has now been locked.<br>
					To use the wizard again reset the flag in the "opencms.properties".
					For security reasons, you should remove the "/update" folder later when
					you have your OpenCms installation running.
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
	</td>
</tr>
</table>


<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<form action="" method="post" class="nomargin">
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" disabled="disabled">
<input name="submit" type="button" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" style="margin-left: 50px;" disabled="disabled">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>