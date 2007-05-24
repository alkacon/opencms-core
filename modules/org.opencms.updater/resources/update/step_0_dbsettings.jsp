<%@ page session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:useBean id="dbBean" class="org.opencms.setup.update6to7.CmsUpdateDBManager" scope="page" /><%--
--%><jsp:setProperty name="dbBean" property="*" /><%
	
	// previous page
	String prevPage = "index.jsp";	
    // next page
	String nextPage = "step_1_update_db.jsp";
	
	dbBean.initialize(Bean);
		
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>

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
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Upgrade of the database") %>

	<div style="width:96%; height: 300px; overflow: auto;">
    <table border="0" cellpadding="2" cellspacing="0">
		<tr><td colspan=2><h1>The Upgrade Wizard is about to upgrade the database.</h1></td></tr>
		<tr><td colspan=2><b>Please be sure to have created a backup and exactly check all information stated below before continuing, and be aware that this process may take several hours depending on your data</b></td></tr>
		<tr><td colspan=2><hr></td></tr>
		<tr><td>Detected Database is:</td><td><%=dbBean.getDbName() %></td></tr>
		<tr><td colspan=2><hr></td></tr>
		<tr><td colspan=2>Following db pool(s) will be upgraded:</td></tr>
<% 
	java.util.Iterator it = dbBean.getPools().iterator(); 
    while (it.hasNext()) {
        String pool = (String)it.next();
		out.println("<tr><td colspan=2><hr></td></tr>");
		out.println("<tr><td colspan=2><h2>"+pool+"</h2></td></tr>");
		out.println("<tr><td>JDBC Driver:</td><td>"+dbBean.getDbDriver(pool)+"</td></tr>");
		out.println("<tr><td>JDBC Connection Url:</td><td>"+dbBean.getDbUrl(pool)+"</td></tr>");
		out.println("<tr><td>JDBC Connection Url Params:</td><td>"+dbBean.getDbParams(pool)+"</td></tr>");
		out.println("<tr><td>Database User:</td><td>"+dbBean.getDbUser(pool)+"</td></tr>");
    }
%>
	</table>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="continue" type="button" value="Continue &#062;&#062;" class="dialogbutton" onclick="location.href='<%= nextPage %>';">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
