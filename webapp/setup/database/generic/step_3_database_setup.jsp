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
    String nextPage ="../../step_4_database_creation.jsp";

	boolean submited = false;

	if(setupOk)	{

		String conStr = request.getParameter("dbCreateConStr");
		String createDb = request.getParameter("createDb");
		if(createDb == null)	{
			createDb = "";
		}

		String createTables = request.getParameter("createTables");
		if(createTables == null)	{
			createTables = "";
		}

		submited = ((request.getParameter("submit") != null) && (conStr != null));

		if(submited)	{

			Bean.setDbWorkConStr(conStr);

			/* Set user and passwords manually. This is necessary because
			   jsp:setProperty does not set empty strings ("") :( */

			String dbCreateUser = 	request.getParameter("dbCreateUser");
			String dbCreatePwd = 	request.getParameter("dbCreatePwd");

			String dbWorkUser =		request.getParameter("dbWorkUser");
			String dbWorkPwd =		request.getParameter("dbWorkPwd");

			Bean.setDbCreateUser(dbCreateUser);
			Bean.setDbCreatePwd(dbCreatePwd);

			Bean.setDbWorkUser(dbWorkUser);
			Bean.setDbWorkPwd(dbWorkPwd);


			Hashtable replacer = new Hashtable();
			replacer.put("$$user$$",dbWorkUser);
			replacer.put("$$password$$",dbWorkPwd);

			Bean.setReplacer(replacer);

			session.setAttribute("createTables",createTables);
			session.setAttribute("createDb",createDb);

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
<%= Bean.getHtmlPart("C_LOGO_OPENCMS", "../../") %>
<% if(setupOk)	{ %>
<form method="POST" onSubmit="return checkSubmit()" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="top">
		<table border="0">
			<tr>
				<td align="center">
					<table border="0" cellpadding="2">
						<tr>
							<td width="150" style="font-weight:bold;">
								Select Database
							</td>
							<td width="250">
								<select name="database" style="width:250px;" size="1" width="250" onchange="location.href='../../step_2_database_selection.jsp?database='+this.options[this.selectedIndex].value;">
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
				</td>
			</tr>

			<tr><td>&nbsp;</td></tr>

			<tr>
				<td>
					<table border="0" cellpadding="5" cellspacing="0" class="header">
						<tr><td>&nbsp;</td><td>User</td><td>Password</td></tr>
						<tr>
							<td>Database Server Connection</td><td><input type="text" name="dbCreateUser" size="8" style="width:120px;" value='<%= Bean.getDbCreateUser() %>'></td><td><input type="text" name="dbCreatePwd" size="8" style="width:120px;" value='<%= Bean.getDbCreatePwd() %>'></td>
						</tr>
						<%
						String user = Bean.getDbWorkUser();
						if(user.equals(""))	{
							user = request.getContextPath();
						}
						if(user.startsWith("/"))	{
							user = user.substring(1,user.length());
						}
						%>
						<tr>
							<td>OpenCms Connection</td><td><input type="text" name="dbWorkUser" size="8" style="width:120px;" value='<%= user %>'></td><td><input type="text" name="dbWorkPwd" size="8" style="width:120px;" value='<%= Bean.getDbWorkPwd() %>'></td>
						</tr>
						<tr><td colspan="3"><hr></td></tr>
						<tr>
							<td>Database Driver</td><td colspan="2"><input type="text" name="dbDriver" size="22" style="width:250px;" value='<%= Bean.getDbDriver() %>'></td>
						</tr>
						<tr>
							<td>Connection String</td><td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:250px;" value='<%= Bean.getDbCreateConStr() %>'></td>
						</tr>
						<tr><td colspan="3"><hr></td></tr>
						<tr><td colspan="3" align="center"><input type="checkbox" name="createTables" value="true" checked> Create database and tables<input type="hidden" name="createTables" value="false"><br>
						<b><span style="color: #FF0000;">Warning:</span></b> Existing database will be dropped !<br></td></tr>

					</table>
				</td>
			</tr>
			<tr><td align="center"><b>Attention:</b> You must have a working database driver in your classpath!</td></tr>

		</table>
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='../../index.jsp';">
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
		Please click <a href="">here</a> to restart the Wizard
	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>