<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

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
			String dbDefaultTablespace = request.getParameter("dbDefaultTablespace");
			String dbTemporaryTablespace = request.getParameter("dbTemporaryTablespace");
            String dbIndexTablespace = request.getParameter("dbIndexTablespace");

			Bean.setDbCreateUser(dbCreateUser);
			Bean.setDbCreatePwd(dbCreatePwd);
			
			Bean.setDbWorkUser(dbWorkUser);
			Bean.setDbWorkPwd(dbWorkPwd);
			
			Hashtable replacer = new Hashtable();			
			replacer.put("$$user$$",dbWorkUser);			
			replacer.put("$$password$$",dbWorkPwd);
			replacer.put("$$defaultTablespace$$", dbDefaultTablespace);
            replacer.put("$$indexTablespace$$", dbIndexTablespace);
			replacer.put("$$temporaryTablespace$$", dbTemporaryTablespace);
			
			Bean.setReplacer(replacer);
						
			session.setAttribute("createTables",createTables);
			session.setAttribute("createDb",createDb);
		
		} else {
			
			// initialize the work user with the app name
			Bean.setDbWorkUser(Bean.getAppName());
		}
				
		
	}
%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->
<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="Stylesheet" type="text/css" href="resources/style.css">
	<script language="Javascript">
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
		if(submited)	{
			out.println("location.href='"+nextPage+"';");
		}
	%>
	</script>	
</head>

<body>
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
	<td><form method="POST" onSubmit="return checkSubmit()">
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">	
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
                <td height="50" align="right"><img src="resources/opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			
			<% if(setupOk)	{ %>
			
			<tr>
				<td height="375" align="center" valign="top">				
					<table border="0">
						<tr>
							<td align="center">
								<table border="0" cellpadding="2">
									<tr>
										<td width="150" class="bold">
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
								<table border="0" cellpadding="5" cellspacing="0" class="header">
									<tr><td>&nbsp;</td><td>User</td><td>Password</td></tr>
									<tr>
										<td>Setup Connection</td><td><input type="text" name="dbCreateUser" size="8" style="width:120px;" value='<%= Bean.getDbCreateUser() %>'></td><td><input type="text" name="dbCreatePwd" size="8" style="width:120px;" value='<%= Bean.getDbCreatePwd() %>'></td>										
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
										<td>OpenCms Connection</td><td><input type="text" name="dbWorkUser" size="8" style="width:120px;" value='<%= user %>'></td><td><input type="text" name="dbWorkPwd" size="8" style="width:120px;" value='<%= Bean.getDbWorkPwd() %>'></td>
									</tr>
									<tr>
										<td>Connection String</td><td colspan="2"><input type="text" name="dbCreateConStr" size="22" style="width:250px;" value='<%= Bean.getDbCreateConStr() %>'></td>
									</tr>
									<tr>
									    <td>Create Database</td><td><input type="checkbox" name="createDb" value="true" checked> User</td><td><input type="checkbox" name="createTables" value="true" checked> Tables<input type="hidden" name="createTables" value="false"></td>
									</tr>
									<tr>
										<td colspan="3" align="center"><b><font color="FF0000">Warning:</font></b> Existing database will be dropped !</font></b></td>
									</tr>
									<tr><td colspan="3"><hr></td></tr>
									<tr>
										<td>Default Tablespace</td><td colspan="2"><input type="text" name="dbDefaultTablespace" size="8" style="width:250px;" value='<%= Bean.getDbDefaultTablespace() %>'></td>   
									</tr>
                                    <tr>
                                        <td>Index Tablespace</td><td colspan="2"><input type="text" name="dbIndexTablespace" size="8" style="width:250px;" value='<%= Bean.getDbIndexTablespace() %>'></td>
									</tr>
                                    <tr>
                                        <td>Temporary Tablespace</td><td colspan="2"><input type="text" name="dbTemporaryTablespace" size="8" style="width:250px;" value='<%= Bean.getDbTemporaryTablespace() %>'></td>
                                    </tr>					
								</table>
							</td>
						</tr>
						<tr><td align="center"><b>Attention:</b> You must have a working oracle driver in your classpath!</td></tr>
						
					</table>
				</td>
			</tr>			
			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.go(-2);">
							</td>
							<td width="200" align="left">
								<input type="submit" name="submit" class="button" style="width:150px;" width="150" value="Continue &#062;&#062;">
							</td>
							<td width="200" align="center">
								<input type="button" class="button" style="width:150px;" width="150" value="Cancel" onclick="location.href='cancel.jsp'">
							</td>
						</tr>
					</table>
				</td>
			</tr>							
			<% } else	{ %>
			<tr>
				<td align="center" valign="top">
					<p><b>ERROR</b></p>
					The setup wizard has not been started correctly!<br>
					Please click <a href="">here</a> to restart the Wizard
				</td>
			</tr>			
			<% } %>
			</form>
			</table>
		</td>
	</tr>
</table>
</td></tr>
</table>
</body>
</html>