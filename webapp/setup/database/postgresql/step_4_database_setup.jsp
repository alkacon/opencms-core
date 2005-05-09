<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page
	String nextPage = "../../step_5_database_creation.jsp";		
	// previous page
	String prevPage = "../../step_2_check_components.jsp";
	
	String conStr = request.getParameter("dbCreateConStr");
	boolean isFormSubmitted =( (request.getParameter("submit") != null) && (conStr != null));

	if (Bean.isInitialized()) {
		String createDb = request.getParameter("createDb");
		if(createDb == null) {
			createDb = "";
		}

		String createTables = request.getParameter("createTables");
		if(createTables == null) {
			createTables = "";
		}

		if (isFormSubmitted) {
			if(!conStr.endsWith("/"))conStr += "/";

			String dbCreateUser = request.getParameter("dbCreateUser");
			String dbCreatePwd = request.getParameter("dbCreatePwd");
			String dbWorkUser = request.getParameter("dbWorkUser");
			String dbWorkPwd = request.getParameter("dbWorkPwd");
			String dbName = request.getParameter("dbName");
			
			Bean.setDbCreateUser(dbCreateUser);
			Bean.setDbCreatePwd(dbCreatePwd);
			Bean.setDbWorkUser(dbWorkUser);
			Bean.setDbWorkPwd(dbWorkPwd);
			
			Bean.setDbWorkConStr(conStr + dbName);
			
			Bean.setDb(dbName);			
			
			Map replacer = (Map) new HashMap();
			replacer.put("${user}", dbWorkUser);
			replacer.put("${password}", dbWorkPwd);
			replacer.put("${database}", dbName);
			
			Bean.setReplacer(replacer);

			session.setAttribute("createTables",createTables);
			session.setAttribute("createDb",createDb);
		} else {
			// initialize the database name with the app name
			if (org.opencms.util.CmsStringUtil.isNotEmptyOrWhitespaceOnly(request.getContextPath())) {
				Bean.setDbWorkUser(request.getContextPath().substring(1));
			} else {
				Bean.setDbWorkUser("opencms");
			}
		}
	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
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
		else if (document.forms[0].createDb.value != "" && document.forms[0].dbDefaultTablespace.value == "") {
			alert("Please insert the name of the Default tablespace");
			document.forms[0].dbWorkPwd.focus();
			return false;
		}
		else if (document.forms[0].createDb.value != "" && document.forms[0].dbIndexTablespace.value == "") {
			alert("Please insert the name of the Index tablespace");
			document.forms[0].dbWorkPwd.focus();
			return false;
		}
		else if (document.forms[0].createDb.value != "" && document.forms[0].dbTemporaryTablespace.value == "") {
			alert("Please insert the name of the Temporary tablespace");
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
OpenCms Setup Wizard - <%= Bean.getDatabaseName(Bean.getDatabase()) %> database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%;">
<tr><td style="vertical-align: top;">

<form method="POST" onSubmit="return checkSubmit()" class="nomargin">
<%= Bean.getHtmlPart("C_BLOCK_START", "Database") %>
<table border="0" cellpadding="2" cellspacing="0">
	<tr>
		<td>Select Database</td>
		<td>
			<select name="database" style="width: 250px;" size="1" onchange="location.href='../../step_3_database_selection.jsp?database='+this.options[this.selectedIndex].value;">
			<!-- --------------------- JSP CODE --------------------------- -->
			<%
				/* get all available databases */
				List databases = Bean.getSortedDatabases();
				/* 	List all databases found in the dbsetup.properties */
				if (databases !=null && databases.size() > 0)	{
					for(int i=0;i<databases.size();i++)	{
						String db = (String) databases.get(i);
						String dn = Bean.getDatabaseName(db);
						String selected = "";
						if(Bean.getDatabase().equals(db))	{
							selected = "selected";
						}
						out.println("<option value='"+db+"' "+selected+">"+dn);
					}
				}
				else	{
					out.println("<option value='null'>no database found");
				}
			%>
			<!-- --------------------------------------------------------- -->
			</select>
		</td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>

</td></tr>
<tr><td style="vertical-align: middle;">

<div class="dialogspacer" unselectable="on">&nbsp;</div>
<iframe src="database_information.html" name="dbinfo" style="width: 100%; height: 80px; margin: 0; padding: 0; border-style: none;" frameborder="0" scrolling="no"></iframe>
<div class="dialogspacer" unselectable="on">&nbsp;</div>

</td></tr>
<tr><td style="vertical-align: bottom;">

<%= Bean.getHtmlPart("C_BLOCK_START", "Database specific settings") %>
					<table border="0" cellpadding="2" cellspacing="0">
						<tr>
							<td>&nbsp;</td>
							<td>User</td>
							<td>Password</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Setup Connection</td>
							<td><input type="text" name="dbCreateUser" size="8" style="width:120px;" value='<%= Bean.getDbCreateUser() %>'></td>
							<td><input type="text" name="dbCreatePwd" size="8" style="width:120px;" value='<%= Bean.getDbCreatePwd() %>'></td>
							<td><%= Bean.getHtmlHelpIcon("1", "../../") %></td>
							<td>&nbsp;</td>
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
							<td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:250px;" value='<%= Bean.getDbCreateConStr() %>'></td>
							<td><%= Bean.getHtmlHelpIcon("3", "../../") %></td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Database Name</td>
							<td colspan="2"><input type="text" name="dbName" style="width:315px;" value=''></td>
							<td><%= Bean.getHtmlHelpIcon("4", "../../") %></td>
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
<%= Bean.getHtmlPart("C_HELP_END") %>

<%= Bean.getHtmlPart("C_HELP_START", "2") %>
The <b>OpenCms Connection</b> is used when running OpenCms after the installation.<br>&nbsp;<br>
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
The setup wizard <b>creates</b> the PostgreSql user, the database and the tables for OpenCms.<br>&nbsp;<br>
<b>Attention</b>: Existing user, database and tables will be overwritten!<br>&nbsp;<br>
Uncheck this option if an already existing user/database should be used.
<%= Bean.getHtmlPart("C_HELP_END") %>



<% } else	{ %>
OpenCms Setup Wizard - Database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% request.setAttribute("pathPrefix", "../../"); %>
<%@ include file="../../error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>