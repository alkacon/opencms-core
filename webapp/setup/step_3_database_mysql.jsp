<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<%	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
%>

<% /* Import packages */ %>
<%@ page import="java.util.*" %>

<% /* Set all given properties */ %>
<jsp:setProperty name="Bean" property="*" />

<%

	/* next page to be accessed */
	String nextPage ="step_4_database_creation.jsp";

	boolean submited = false;

	if(setupOk)	{

		String conStr = request.getParameter("dbCreateConStr");
		String database = request.getParameter("db");
		String createDb = request.getParameter("createDb");
		if(createDb == null)	{
			createDb = "";
		}

		submited = ((request.getParameter("submit") != null) && (conStr != null) && (database != null));

		if(submited)	{

			if(!conStr.endsWith("/"))conStr += "/";

			/* Set user and passwords manually. This is necessary because
			   jsp:setProperty does not set empty strings ("") :( */
			String dbCreateUser = 	request.getParameter("dbCreateUser");
			String dbWorkUser =		request.getParameter("dbWorkUser");

			String dbCreatePwd = 	request.getParameter("dbCreatePwd");
			String dbWorkPwd =		request.getParameter("dbWorkPwd");


			Bean.setDbWorkConStr(conStr + database);

			Bean.setDbCreateUser(dbCreateUser);
			Bean.setDbWorkUser(dbWorkUser);

			Bean.setDbCreatePwd(dbCreatePwd);
			Bean.setDbWorkPwd(dbWorkPwd);

			Hashtable replacer = new Hashtable();
			replacer.put("$$database$$",database);

			Bean.setReplacer(replacer);

			session.setAttribute("createDb",createDb);

		} else {

			// initialize the database name with the app name
			Bean.setDb(Bean.getAppName());
		}


	}
%>
<%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
	<script type="text/javascript">
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
		if(submited)	{
			out.println("location.href='"+nextPage+"';");
		}
	%>
	</script>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - <%= Bean.getDatabase() %>
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>

<% if(setupOk)	{ %>
<form method="POST" onSubmit="return checkSubmit()">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td height="375" align="center" valign="top">
		<table border="0">
			<tr>
				<td align="center">
					<table border="0" cellpadding="2">
						<tr>
							<td width="150" style="font-weight:bold;">
								Select Database
							</td>
							<td width="250">
								<select name="database" style="width:250px;" size="1" width="250" onchange="location.href='step_2_database_selection.jsp?database='+this.options[this.selectedIndex].value;">
								<!-- --------------------- JSP CODE --------------------------- -->
								<%
									/* get all available databases */
									Vector databases = Bean.getDatabases();
									Vector databaseNames = Bean.getDatabaseNames();
									/* 	List all databases found in the dbsetup.properties */
									if (databases !=null && databases.size() > 0)	{
										for(int i=0;i<databases.size();i++)	{
											String db = databases.elementAt(i).toString();
											String dn = databaseNames.elementAt(i).toString();
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
				</td>
			</tr>

			<tr><td>&nbsp;</td></tr>

			<tr>
				<td>
					<table border="0" cellpadding="5" cellspacing="0">
						<tr><td>&nbsp;</td><td>User</td><td>Password</td></tr>
						<tr>
							<td>Setup Connection</td><td><input type="text" name="dbCreateUser" size="8" style="width:120px;" value='<%= Bean.getDbCreateUser() %>'></td><td><input type="text" name="dbCreatePwd" size="8" style="width:120px;" value='<%= Bean.getDbCreatePwd() %>'></td>
						</tr>
						<tr>
							<td>OpenCms Connection</td><td><input type="text" name="dbWorkUser" size="8" style="width:120px;" value='<%= Bean.getDbWorkUser() %>'></td><td><input type="text" name="dbWorkPwd" size="8" style="width:120px;" value='<%= Bean.getDbWorkPwd() %>'></td>
						</tr>
						<tr><td colspan="3"><hr></td></tr>
						<tr>
							<td>Connection String</td><td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:250px;" value='<%= Bean.getDbCreateConStr() %>'></td>
						</tr>
						<tr>
							<td>Database</td><td colspan="2"><input type="text" name="db" size="22" style="width:250px;" value='<%= Bean.getDb() %>'></td>
						</tr>
						<tr><td colspan="3"><hr></td></tr>
						<tr><td colspan="3" align="center"><input type="checkbox" name="createDb" value="true" checked> Create database and tables<br>
						<b><span style="color: #FF0000;">Warning:</span></b> Existing database will be dropped !
						</td></tr>
					</table>
				</td>
			</tr>
		</table>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='index.jsp';">
<input name="submit" type="submit" value="Continue &#062;&#062;" class="dialogbutton">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<p><b>ERROR</b></p>
		The setup wizard has not been started correctly!<br>
		Please click <a href="">here</a> to restart the Wizard
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>