<%@ page import="java.util.*" %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:setProperty name="Bean" property="*" />
<%
	String conStr = request.getParameter("dbCreateConStr");
	String database = request.getParameter("db");
	boolean isSetupOk = (Bean.getProperties() != null);
	boolean isFormSubmitted = ((request.getParameter("submit") != null) && (conStr != null) && (database != null));
	String nextPage = "../../step_5_database_creation.jsp";	

	if (isSetupOk) {
		String createDb = request.getParameter("createDb");
		if(createDb == null) {
			createDb = "";
		}

		if(isFormSubmitted)	{
			if(!conStr.endsWith("/"))conStr += "/";
			   
			String dbCreateUser = request.getParameter("dbCreateUser");
			String dbWorkUser = request.getParameter("dbWorkUser");

			String dbCreatePwd = request.getParameter("dbCreatePwd");
			String dbWorkPwd = request.getParameter("dbWorkPwd");

			Bean.setDbWorkConStr(conStr + database);

			Bean.setDbCreateUser(dbCreateUser);
			Bean.setDbWorkUser(dbWorkUser);

			Bean.setDbCreatePwd(dbCreatePwd);
			Bean.setDbWorkPwd(dbWorkPwd);

			Map replacer = (Map) new HashMap();
			replacer.put("${database}", database);
			Bean.setReplacer(replacer);
			
			session.setAttribute("createDb", createDb);
		} else {
			// initialize the database name with the app name
			Bean.setDb(Bean.getAppName());
		}
	} else {
		Bean.initHtmlParts();
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
			alert("Please insert connection string");
			document.forms[0].dbCreateConStr.focus();
			return false;
		}
		else if (document.forms[0].db.value == "")	{
			alert("Please insert database name");
			document.forms[0].db.focus();
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

<% if (isSetupOk) { %>
OpenCms Setup Wizard - <%= Bean.getDatabaseName(Bean.getDatabase()) %> database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<form method="post" onSubmit="return checkSubmit()" class="nomargin">

<table border="0" cellpadding="0" cellspacing="0" style="width: 100%; height: 100%;">
<tr><td style="vertical-align: top;">

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
	</tr>
	<tr>
		<td>Setup Connection</td>
		<td><input type="text" name="dbCreateUser" size="8" style="width:150px;" value='<%= Bean.getDbCreateUser() %>'></td>
		<td style="text-align: right;"><input type="text" name="dbCreatePwd" size="8" style="width:150px;" value='<%= Bean.getDbCreatePwd() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("1", "../../") %></td>
	</tr>
	<tr>
		<td>OpenCms Connection</td>
		<td><input type="text" name="dbWorkUser" size="8" style="width:150px;" value='<%= Bean.getDbWorkUser() %>'></td>
		<td style="text-align: right;"><input type="text" name="dbWorkPwd" size="8" style="width:150px;" value='<%= Bean.getDbWorkPwd() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("2", "../../") %></td>
	</tr>
	<tr>
		<td>Connection String</td>
		<td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:315px;" value='<%= Bean.getDbCreateConStr() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("3", "../../") %></td>
	</tr>
	<tr>
		<td>Database</td>
		<td colspan="2"><input type="text" name="db" size="22" style="width:315px;" value='<%= Bean.getDb() %>'></td>
		<td><%= Bean.getHtmlHelpIcon("4", "../../") %></td>
	</tr>
	<tr>
		<td colspan="4"><input type="checkbox" name="createDb" value="true" checked> Create database and tables 
		</td>
	</tr>
</table>
<%= Bean.getHtmlPart("C_BLOCK_END") %>
</td></tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.go(-2);">
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
Enter the name of the MySQL <b>Database</b> which should be used by OpenCms.
<%= Bean.getHtmlPart("C_HELP_END") %>

<% } else	{ %>
OpenCms Setup Wizard - Database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>

<% request.setAttribute("pathPrefix", "../../"); %>
<%@ include file="../../error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>