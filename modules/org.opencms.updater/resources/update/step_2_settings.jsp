<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsUpdateBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "step_3_module_selection.jsp";		
	// previous page
	String prevPage = "index.jsp";
	
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
		if(document.forms[0].adminUser.value == "")	{
			alert("Please insert the user name");
			document.forms[0].adminUser.focus();
			return false;
		}
		else if (document.forms[0].adminPwd.value == "")	{
			alert("Please insert the user password");
			document.forms[0].adminPwd.focus();
			return false;
		}
		else if (document.forms[0].updateProject.value == "")	{
			alert("Please insert the OpenCms project for update");
			document.forms[0].updateProject.focus();
			return false;
		}
		else if (document.forms[0].updateSite.value == "")	{
			alert("Please insert the OpenCms site for update");
			document.forms[0].updateSite.focus();
			return false;
		}
		else	{
			return true;
		}
	}

	<%
		if(isFormSubmitted)	{
			out.println("location.href='"+nextPage+"';");
		}
	%>
//-->
</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>

<% if (Bean.isInitialized()) { %>
OpenCms Update Wizard - Settings
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<form method="post" onSubmit="return checkSubmit()" class="nomargin">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: bottom;">	
		<%= Bean.getHtmlPart("C_BLOCK_START", "OpenCms update settings") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td><img src="resources/warning.png" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100%;">					
				    Make sure you have administration permissions to update your system.<br>
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>
		<div class="dialogspacer" unselectable="on">&nbsp;</div>
	</td>	
</tr>
<tr><td style="vertical-align: bottom;">
<%= Bean.getHtmlPart("C_BLOCK_START", "Identification") %>
<table border="0" cellpadding="2" cellspacing="0">
	<tr>
		<td>Admin User:</td>
		<td><input type="text" name="adminUser" size="15" style="width:150px;" value='<%= Bean.getAdminUser() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("1", "") %></td>
	</tr>
	<tr>
		<td>Admin Password:</td>
		<td><input type="password" name="adminPwd" size="15" style="width:150px;" value='<%= Bean.getAdminPwd() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("2", "") %></td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>
</td></tr>
<tr><td style="vertical-align: bottom;">
<%= Bean.getHtmlPart("C_BLOCK_START", "Settings") %>
<table border="0" cellpadding="2" cellspacing="0">
	<tr>
		<td>Update Project:</td>
		<td><input type="text" name="updateProject" size="15" style="width:150px;" value='<%= Bean.getUpdateProject() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("3", "") %></td>
	</tr>
	<tr>
		<td>Update Site:</td>
		<td><input type="text" name="updateSite" size="15" style="width:150px;" value='<%= Bean.getUpdateSite() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("4", "") %></td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>
</td></tr>
<tr><td style="vertical-align: bottom;">
<%= Bean.getHtmlPart("C_BLOCK_START", "Installation Paths") %>
<table border="0" cellpadding="2" cellspacing="0">
	<tr>
		<td>OpenCms Application rootpath:</td>
		<td><%= Bean.getWebAppRfsPath() %></td>
	</tr>
	<tr>
		<td>OpenCms Configuration folder:</td>
		<td><%= Bean.getConfigRfsPath() %></td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>
</td></tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "1") %>
The <b>Admin User</b> to use during the update process.<br>&nbsp;<br>
The specified user must be a valid user in your current installation and have administration permissions.
This user information is not stored after the update is finished.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "2") %>
Enter the right password for the given <b>Admin User</b>.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "3") %>
Enter the project to use during the update of the system modules.<br>&nbsp;<br>
This project should not exist, and will be created during update, and deleted once finished.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "4") %>
Enter the site to use during the update of the system modules.<br>&nbsp;<br>
If you are updating documentation modules, please use the same site where the old documentation modules are installed. <br>
If not you can use the root '/' site.
<%= Bean.getHtmlPart("C_HELP_END") %>
<% } else	{ %>
OpenCms Setup Wizard - Settings
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%@ include file="error.jsp" %>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>
