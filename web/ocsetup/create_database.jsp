<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="com.opencms.boot.*,java.util.*" %>

<%
	
	/* next page to be accessed */
	String nextPage = "check_versions.jsp";
	
	
	
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
		
	CmsSetupDb db = null;	
		
	boolean createDb = false;
	boolean createTables = false;
	boolean dbExists = false;
	boolean dropDb = false;
	
	if(setupOk)	{	

		String temp;
		Object a;
		
		if ((a = session.getAttribute("createDb")) != null) {
			createDb = "true".equals(a.toString());
		}

		if (((a = session.getAttribute("createTables")) != null) && (a.toString().length() > 0)) {
			createTables = "true".equals(a.toString());		
	    } else {
			// if not explicitly set, we will certainly create the
			// tables when creating a new database 
	    	createTables = createDb;
	    }

		if(createDb || createTables)	{
			db = new CmsSetupDb(Bean.getBasePath());	
			temp = request.getParameter("dropDb");
			dropDb = temp != null && "Yes".equals(temp);
				
			/* check if database exists */
			if(!dropDb)	{
				db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());				
				dbExists = db.noErrors();
				if(dbExists)	{
					db.closeConnection();
				}
				else	{
					db.clearErrors();
				}
			}
			if( !dbExists || dropDb)	{
				db.setConnection(Bean.getDbDriver(), Bean.getDbCreateConStr(), Bean.getDbCreateUser(), Bean.getDbCreatePwd());
			}
			else {
				if (createDb || createTables) {
			  		nextPage = "create_database.jsp";
			  	}
			}
		}				
	}

%>
<!-- ------------------------------------------------------------------------------------------------------------------- -->

<html>
<head> 
	<title>OpenCms Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="Stylesheet" type="text/css" href="style.css">
</head>

<body>
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>	
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
	<td><form action="<%= nextPage %>" method="POST">	
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
				<td height="50" align="right"><img src="opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			<% if(setupOk)	{ %>
			<tr>
				<td height="375" align="center" valign="center">

					<table border="0" width="600" cellpadding="5">
						<tr>
							<td align="center" valign="top" height="50">
							<%
								if(!createDb && !createTables && !dbExists)	{
									out.println("<p>You have not created the OpenCms database.</p><p><b>Warning: &nbsp;&nbsp;</b>You cannot import the workplace succesfully without the database and tables!</p>");						
								}
								else {	
									if(dbExists && createTables && !dropDb)	{
										db.closeConnection();
										out.println("<p><strong><font color=\"#ff0000\">Warning:</font> An existing database has been detected. Drop it ?</strong></p>");
										out.println("<p><nobr><input type=\"submit\" name=\"dropDb\" class=\"button\" value=\"Yes\" style=\"width:150px;\" width=\"150\">&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" value=\"No\" onClick=\"history.go(-3)\" style=\"width:150px;\" class=\"button\" width=\"150\"></nobr></p>");
									}
									else	{
										if(createDb && dropDb)	{

											//Drop Database
											out.print("<p>Trying to drop database  ...");
											db.dropDatabase(Bean.getDatabase(), Bean.getReplacer());
											if(db.noErrors())	{

												out.println(" <b>Ok</b></p>");
											}
											else	{
												out.println(" <b>Failed</b></p>");
												Vector errors = db.getErrors();									
												out.print("<textarea rows='7' cols='50' style='width:600px;height:80px;' readonly wrap='off'>");
												for(int i = 0; i < errors.size(); i++)	{
													out.println(errors.elementAt(i));
													out.println("-------------------------------------------");													
												}
												out.print("</textarea><br>");
												db.clearErrors();
											}
										}
							%>
							</td></tr>
							<td align="center" valign="top" height="50">
							<%
										if (createDb) {
											//Create Database
											out.print("<p>Creating database ...");
											db.createDatabase(Bean.getDatabase(), Bean.getReplacer());
											if(db.noErrors())	{										
												out.println(" <b>Ok</b></p>");
											}
											else	{
												out.println(" <b>Failed</b></p>");
												Vector errors = db.getErrors();
												out.print("<textarea rows='7' cols='50' style='width:600px;height:80px;' readonly wrap='off'>");
												for(int i = 0; i < errors.size(); i++)	{
													out.println(errors.elementAt(i));
													out.println("-------------------------------------------");													
												}
												out.print("</textarea><br>");
												db.clearErrors();
											}
										}									
										
										db.closeConnection();		
							%>
								</td></tr>
								<td align="center" valign="top" height="50">
							<%			
										if (createTables) {
											db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());									
											//Drop Tables (intentionally quiet)
											db.dropTables(Bean.getDatabase());
											db.clearErrors();
											db.closeConnection();
											
											// reopen the connection in order to display errors
											db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
											//Create Tables
											out.print("<p>Creating tables ...");
											db.createTables(Bean.getDatabase());
											if(db.noErrors())	{										
												out.println(" <b>Ok</b></p>");
											}
											else	{
												out.println(" <b>Failed</b></p>");
												Vector errors = db.getErrors();
												out.print("<textarea rows='7' cols='50' style='width:600px;height:80px;' readonly wrap='off'>");
												for(int i = 0; i < errors.size(); i++)	{
													out.println(errors.elementAt(i));
													out.println("-------------------------------------------");													
												}
												out.print("</textarea><br>");
												db.clearErrors();
												db.closeConnection();
											}
										}																		
									}
								}
							%>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td height="50" align="center">
					<table border="0">
						<tr>
							<td width="200" align="right">
								<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.go(-2)">
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
</td>
</tr>
</table>
</body>
</html>