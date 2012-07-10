<%@ page session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:useBean id="dbBean" class="org.opencms.setup.db.CmsUpdateDBManager" scope="page" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%
	
	// previous page
	String prevPage = "index.jsp";	
    // next page
	String nextPage = "step_1_update_db.jsp";
	
	dbBean.initialize(Bean);
		
    boolean isFormSubmitted = (request.getParameter("submit") != null);
	if (isFormSubmitted)  {
		response.sendRedirect(nextPage);
		return;
	}
    Bean.setDetectedVersion(dbBean.getDetectedVersion());
	if (!dbBean.needUpdate()) {
		response.sendRedirect("step_2_settings.jsp");
		return;
	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Update Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<script type="text/javascript">//<!--
function switchview(id) {
	
	var elem = document.getElementById(id);
	if (elem != undefined) {
		if (elem.style.display == "block") {
			elem.style.display = "none";
		} else {
			elem.style.display = "block";
		}
	}
}
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
<form method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td valign="top">
	
<%= Bean.getHtmlPart("C_BLOCK_START", "Upgrade of the database") %>

	<div style="width:96%; height: 300px; overflow: auto;">
		<font size='+1'><b>OpenCms needs to update your database.</b></font><br>
		<hr>
		Detected Database is: <%=dbBean.getDbName() %><br>
		<hr>
		<b>Please be sure to have created a backup and exactly check all information stated below before continuing, and be also aware that this process may take several hours depending on your data.</b><br>
		<hr>
<% if (dbBean.getDetectedVersion() < 7) { %>		
		<table border=0>
		<tr><td><input type="checkbox" name="keepHistory" value="true"> </td><td>Keep and convert historical versions of resources? </td></tr>
		<tr><td>&nbsp;</td><td><b>doing so is not recommended since the converted data will not work 100% and may prolong the update process for several hours.</b></td></tr>
		</table>
		<hr>
<% } %>
		<p>Following db pool(s) will be upgraded:</p>
<% 
	java.util.Iterator<String> it = dbBean.getPools().iterator(); 
    while (it.hasNext()) {
        String pool = it.next(); %>
<%		out.println(dbBean.htmlPool(pool)); %>
<%
    }
%>
	</div>

<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td>
</tr>
</table>

<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else { %>
<%= Bean.displayError("")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
