<%@ page import="java.util.*" %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />
<jsp:setProperty name="Bean" property="*" />
<%
	String conStr = request.getParameter("dbCreateConStr");
	boolean isSetupOk = (Bean.getProperties() != null);
	boolean isFormSubmitted =( (request.getParameter("submit") != null) && (conStr != null));
	String nextPage = "../../step_5_database_creation.jsp";

	if(isSetupOk)	{
		String createDb = request.getParameter("createDb");
		if(createDb == null) {
			createDb = "";
		}

		String createTables = request.getParameter("createTables");
		if(createTables == null) {
			createTables = "";
		}

		if(isFormSubmitted)	{
			Bean.setDbWorkConStr(conStr);

			String dbCreateUser = request.getParameter("dbCreateUser");
			String dbCreatePwd = request.getParameter("dbCreatePwd");
			String dbWorkUser = request.getParameter("dbWorkUser");
			String dbWorkPwd = request.getParameter("dbWorkPwd");
			
			Bean.setDbCreateUser(dbCreateUser);
			Bean.setDbCreatePwd(dbCreatePwd);
			Bean.setDbWorkUser(dbWorkUser);
			Bean.setDbWorkPwd(dbWorkPwd);			
			
			String dbDefaultTablespace = request.getParameter("dbDefaultTablespace");
			String dbTemporaryTablespace = request.getParameter("dbTemporaryTablespace");
			String dbIndexTablespace = request.getParameter("dbIndexTablespace");
			
			Bean.setDbProperty(Bean.getDatabase() + ".defaultTablespace", dbDefaultTablespace);
			Bean.setDbProperty(Bean.getDatabase() + ".temporaryTablespace", dbTemporaryTablespace);
			Bean.setDbProperty(Bean.getDatabase() + ".indexTablespace", dbIndexTablespace);

			Map replacer = (Map) new HashMap();
			replacer.put("${user}", dbWorkUser);
			replacer.put("${password}", dbWorkPwd);
			replacer.put("${defaultTablespace}", dbDefaultTablespace);
			replacer.put("${indexTablespace}", dbIndexTablespace);
			replacer.put("${temporaryTablespace}", dbTemporaryTablespace);
			Bean.setReplacer(replacer);

			session.setAttribute("createTables",createTables);
			session.setAttribute("createDb",createDb);
		} else {
			// initialize the work user with the app name
			Bean.setDbWorkUser(Bean.getAppName());
		}
	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<script type="text/javascript">
<!--
	function checkSubmit()	{
		if(document.forms[0].dbCreateConStr.value == "")	{
			alert("Please insert connection string");
			document.forms[0].dbCreateConStr.focus();
			return false;
		}
		else if (document.forms[0].dbWorkUser.value == "")	{
			alert("Please insert user name");
			document.forms[0].dbWorkUser.focus();
			return false;
		}
		else if (document.forms[0].dbWorkPwd.value == "")	{
			alert("Please insert password");
			document.forms[0].dbWorkPwd.focus();
			return false;
		}
		else if (document.forms[0].createDb.value != "" && document.forms[0].dbDefaultTablespace.value == "") {
			alert("Please insert name of default tablespace");
			document.forms[0].dbWorkPwd.focus();
			return false;
		}
		else if (document.forms[0].createDb.value != "" && document.forms[0].dbIndexTablespace.value == "") {
			alert("Please insert name of index tablespace");
			document.forms[0].dbWorkPwd.focus();
			return false;
		}
		else if (document.forms[0].createDb.value != "" && document.forms[0].dbTemporaryTablespace.value == "") {
			alert("Please insert name of temporary tablespace");
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
OpenCms Setup Wizard - <%= Bean.getDatabaseName(Bean.getDatabase()) %> database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (isSetupOk) { %>

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
				List databases = Bean.getDatabases();
				List databaseNames = Bean.getDatabaseNames();
				/* 	List all databases found in the dbsetup.properties */
				if (databases !=null && databases.size() > 0)	{
					for(int i=0;i<databases.size();i++)	{
						String db = (String) databases.get(i);
						String dn = (String) databaseNames.get(i);
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

<%= Bean.getHtmlPart("C_BLOCK_START", "Settings") %>
					<table border="0" cellpadding="2" cellspacing="0">
						<tr>
							<td>&nbsp;</td>
							<td>User</td>
							<td>Password</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Setup Connection</td>
							<td><input type="text" name="dbCreateUser" size="8" style="width:120px;" value='<%= Bean.getDbCreateUser() %>'></td>
							<td><input type="text" name="dbCreatePwd" size="8" style="width:120px;" value='<%= Bean.getDbCreatePwd() %>'></td>
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
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>Connection String</td><td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:250px;" value='<%= Bean.getDbCreateConStr() %>'></td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td>&nbsp;</td>
							<td>Default</td>
							<td>Index</td>
							<td>Temporary</td>
						</tr>
						<tr>
							<td>Tablespace</td>
							<td><input type="text" name="dbDefaultTablespace" size="8" style="width:120px;" value='<%= Bean.getDbProperty(Bean.getDatabase() + ".defaultTablespace") %>'></td>
							<td><input type="text" name="dbIndexTablespace" size="8" style="width:120px;" value='<%= Bean.getDbProperty(Bean.getDatabase() + ".indexTablespace") %>'></td>
							<td><input type="text" name="dbTemporaryTablespace" size="8" style="width:120px;" value='<%= Bean.getDbProperty(Bean.getDatabase() + ".temporaryTablespace") %>'></td>
						</tr>
						<tr>
							<td>Create Database</td>
							<td><input type="checkbox" name="createDb" value="true" checked> User</td>
							<td><input type="checkbox" name="createTables" value="true" checked> Tables<input type="hidden" name="createTables" value="false"></td>
							<td>&nbsp;</td>
						</tr>
					</table>
				
<%= Bean.getHtmlPart("C_BLOCK_END") %>
</td></tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.go(-2);">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='../../cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<p><b>ERROR</b></p>
		The setup wizard has not been started correctly!<br>
		Please click <a href="<%= request.getContextPath() %>/setup/">here</a> to restart the Wizard
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>