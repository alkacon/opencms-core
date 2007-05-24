<%@ page session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:useBean id="dbBean" class="org.opencms.setup.update6to7.CmsUpdateDBManager" scope="page" /><%--
--%><jsp:setProperty name="dbBean" property="*" /><%
	
    // next page
	String nextPage = "step_3_xmlupdate.jsp";	
	// previous page
	String prevPage = "step_2_settings.jsp";
	
	boolean isFormSubmitted = (request.getParameter("submit") != null);
		
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>

<script type="text/javascript">
<!--
	function checkSubmit()	{
		
		
		return true
	}

	<%
		if(isFormSubmitted)	{
			// initialize the CmsUpdateManager
			dbBean.initialize(Bean);
			dbBean.run();
		    //dbBean.closeDatabaseConnection();
			out.println("location.href='"+nextPage+"';");
		}
	%>
//-->
</script>

<style type="text/css">
	pre.code {
		font-family: "Courier New", Courier, monospace;
		background-color: #ffffff;
		border: 1px;
		border-style: solid;
		border-color: #000000;
		padding: 5px 5px 5px 5px;
		margin-left: 5px;
		margin-right: 5px;
	}
</style>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Update Wizard - Database upgrade
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% if (Bean.isInitialized()) { %>
<form method="post" class="nomargin" name="dbupdate" onSubmit="return checkSubmit()">
<input type="hidden" name="selectedPlugins" value="">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Upgrade of the database") %>

	<div style="width:96%; height: 300px; overflow: auto;">
    <table border="0" cellpadding="2" cellspacing="0">
		<tr><td>You are about to upgrade the database</td></tr>
	</table>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr>
</table>

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
