<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "../../step_4a_database_validation.jsp";		
	// previous page
	String prevPage = "../../step_2_check_components.jsp";
	
    boolean isFormSubmitted = Bean.setDbParamaters(request, CmsSetupBean.POSTGRESQL_PROVIDER);
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
Alkacon OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_SCRIPT_HELP") %>
<script type="text/javascript">
<!--
	function checkSubmit()	{
		if(document.forms[0].dbCreateConStr.value == "")	{
			alert("Please insert the Connection String");
			document.forms[0].dbCreateConStr.focus();
			return false;
		}
		else if (document.forms[0].dbWorkUser.value == "")	{
			alert("Please insert a User name");
			document.forms[0].dbWorkUser.focus();
			return false;
		}
		else if (document.forms[0].dbWorkPwd.value == "")	{
			alert("Please insert a password");
			document.forms[0].dbWorkPwd.focus();
			return false;
		}
		else if (document.forms[0].dbName.value == "")	{
			alert("Please insert a Database Name");
			document.forms[0].dbName.focus();
			return false;
		}
		else if (document.forms[0].createDb.value != "" && document.forms[0].templateDb.value == "") {
			alert("Please insert the name of the Template Database");
			document.forms[0].dbWorkPwd.focus();
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
Alkacon OpenCms Setup Wizard - <%= Bean.getDatabaseName(Bean.getDatabase()) %> database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<form method="POST" onSubmit="return checkSubmit()" class="nomargin" autocomplete="off">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%;">
<tr><td style="vertical-align: top;">

<%= Bean.getHtmlPart("C_BLOCK_START", "Database") %>
<table border="0" cellpadding="2" cellspacing="0">
	<tr>
		<td>Select Database</td>
		<td><%= Bean.getHtmlForDbSelection() %></td>
		<% if (Bean.getFullDatabaseKey().contains("_jpa")) { %>
			<td><%= Bean.getHtmlHelpIcon("6", "../../") %></td>
		<% } %>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td></tr>
<tr><td style="vertical-align: middle;">

<div class="dialogspacer" unselectable="on">&nbsp;</div>
<% if (Bean.getFullDatabaseKey().contains("_jpa")) { %>
	<iframe src="database_information_jpa.html" name="dbinfo" style="width: 100%; height: 80px; margin: 0; padding: 0; border-style: none;" frameborder="0" scrolling="no"></iframe>
<% } else { %>
	<iframe src="database_information.html" name="dbinfo" style="width: 100%; height: 82px; margin: 0; padding: 0; border-style: none;" frameborder="0" scrolling="no"></iframe>
<% } %>
<div class="dialogspacer" unselectable="on">&nbsp;</div>

</td></tr>
<tr><td style="vertical-align: bottom;">

<%= Bean.getHtmlPart("C_BLOCK_START", "Database specific settings") %>
					<table border="0" cellpadding="2" cellspacing="0">
						<tr>
							<td>&nbsp;</td>
							<td>User</td>
							<td>Password</td>
							<td>Template Db</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Setup Connection</td>
							<td><input type="text" name="dbCreateUser" size="8" style="width:120px;" value='<%= Bean.getDbCreateUser() %>'></td>
							<td><input type="text" name="dbCreatePwd" size="8" style="width:120px;" value='<%= Bean.getDbCreatePwd() %>'></td>
							<td><input type="text" name="templateDb" size="8" style="width:120px;" value='<%= Bean.getDbProperty(Bean.getDatabase() + ".templateDb") %>'></td>
							<td><%= Bean.getHtmlHelpIcon("1", "../../") %></td>		
						</tr>
						<%
						String user = Bean.getDbWorkUser();
						//if(user.equals(""))	{
						//	user = request.getContextPath();
						//}
						//if(user.startsWith("/"))	{
						//	user = user.substring(1,user.length());
						//}
						%>
						<tr>
							<td>OpenCms Connection</td>
							<td><input type="text" name="dbWorkUser" size="8" style="width:120px;" value='<%= user %>'></td>
							<td><input type="text" name="dbWorkPwd" size="8" style="width:120px;" value='<%= Bean.getDbWorkPwd() %>'></td>
							<td><%= Bean.getHtmlHelpIcon("2", "../../") %></td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Connection String</td>
							<%
								//Fixing the Back Button Bug in the next steps.
								String cnString=Bean.getDbCreateConStr();
								if (cnString.indexOf(Bean.getDbProperty(Bean.getDatabase() + ".templateDb"))>0) {
									cnString=cnString.substring(0,cnString.indexOf(Bean.getDbProperty(Bean.getDatabase() + ".templateDb")));
								}
							%>
							<td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:280px;" value='<%= cnString %>'></td>
							<td><%= Bean.getHtmlHelpIcon("3", "../../") %></td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Database Name</td>
							<td colspan="2"><input type="text" name="dbName" style="width:280px;" value=''></td>
							<td><%= Bean.getHtmlHelpIcon("4", "../../") %></td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Create Database</td>
							<td><input type="checkbox" name="createDb" value="true" checked> User</td>
							<td><input type="checkbox" name="createTables" value="true" checked> Tables<input type="hidden" name="createTables" value="false"></td>
							<td><%= Bean.getHtmlHelpIcon("5", "../../") %></td>
							<td>&nbsp;</td>
						</tr>
					</table>
				
<%= Bean.getHtmlPart("C_BLOCK_END") %>
</td></tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='../../index.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "1") %>
The <b>Setup Connection</b> is used <i>only</i> during this setup process.<br>&nbsp;<br>
The specified user must have database administration permissions in order to create the database and tables.
This user information is not stored after the setup is finished.
For Postgresql versions before 8.0 you can use "template1"/"template0" databases.
For Postgresql 8.0 and newer it is better to use "postgres" database.
If you discover problems accessing to the templatedb tipcally a "templatedb is being accessed by other users" try to restart your DBMS or change the templatedb you are accessing.
Some tools (i.e. PgAdmin3) are accessing to template1 by default, so turn off that tools.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "2") %>
The <b>OpenCms Connection</b> is used when running Alkacon OpenCms after the installation.<br>&nbsp;<br>
For security reasons, the specified user should <i>not</i> have database administration permissions.
This user information is stored in the <code>opencms.properties</code> file after the setup.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "3") %>
Enter the JDBC <b>Connection String</b> to your database.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "4") %>
Enter the Database Name.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "5") %>
The setup wizard <b>creates</b> the PostgreSql user, the database and the tables for Alkacon OpenCms.<br>&nbsp;<br>
<b>Attention</b>: Existing user, database and tables will be overwritten!<br>&nbsp;<br>
Uncheck this option if an already existing user/database should be used.
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "6") %>
<b>Traditional SQL drivers</b> are well tested and offer a 
slight performance increase in comparison with JPA driver. 
Because SQL drivers are specific for each RDBMS they may be not available for some databases.<br>&nbsp;<br>
<b>JPA driver</b> is a new generation driver based on JPA specification (Java Persistence API). 
It uses modern JPA implementation - <b>Apache OpenJPA</b> which is quite extensible and flexible.
<%= Bean.getHtmlPart("C_HELP_END") %>

<% } else	{ %>
Alkacon OpenCms Setup Wizard - Database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.displayError("../../")%>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>