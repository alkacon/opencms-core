<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%
	/* next page to be accessed */
	String nextPage = "step_6_save_properties.jsp";

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
                    nextPage = "step_5_database_creation.jsp";
			  	}
			}
		}
	}

	boolean enableContinue = false;
	if(!createDb && !createTables && dbExists)	{
		enableContinue = true;
	}

%><%= Bean.getHtmlPart("C_HTML_START") %>
OpenCms Setup Wizard
<%= Bean.getHtmlPart("C_HEAD_START") %>
<%= Bean.getHtmlPart("C_STYLES") %>
<%= Bean.getHtmlPart("C_STYLES_SETUP") %>
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - <%= Bean.getDatabaseName(Bean.getDatabase()) %> database setup
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: middle;">

				<%
					if (!createDb && !createTables && !dbExists)	{
						%>
						<%= Bean.getHtmlPart("C_BLOCK_START", "Create Database") %>
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td><img src="resources/warning.gif" border="0"></td>
								<td>&nbsp;&nbsp;</td>
								<td>You have not created the OpenCms database.<br>
									<b>Warning:</b> You cannot import the workplace successfully without the database and tables!
								</td>
							</tr>
						</table>
						<%= Bean.getHtmlPart("C_BLOCK_END") %>
						<%
					}
					else {
						if (dbExists && createTables && !dropDb)	{
							db.closeConnection(); %>
							<%= Bean.getHtmlPart("C_BLOCK_START", "Create Database") %>
							<table border="0" cellpadding="0" cellspacing="0">
								<tr>
									<td><img src="resources/warning.gif" border="0"></td>
									<td>&nbsp;&nbsp;</td>
									<td><b>Warning:</b> An existing database has been detected. Drop it ?</td>
								</tr>
								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>
								<tr>
									<td colspan="2">&nbsp;</td>
									<td>
										<input type="submit" name="dropDb" class="dialogbutton" style="margin-left: 0;" value="Yes">&nbsp;&nbsp;&nbsp;&nbsp;
										<input type="button" value="No" onClick="history.go(-3);" class="dialogbutton">
									</td>
								</tr>
							</table>
							<%= Bean.getHtmlPart("C_BLOCK_END") %>
							<%
						}
						else	{
							if (createDb && dropDb)	{
								// Drop Database %>
								<%= Bean.getHtmlPart("C_BLOCK_START", "Dropping database ...") %>
								<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
								
								<%
								db.dropDatabase(Bean.getDatabase(), Bean.getReplacer());
								if (db.noErrors())	{ %>
									<tr>
										<td><img src="resources/ok.gif" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">Database has been successfully dropped.</td>
									</tr>									
									<%
									enableContinue = true;
								}
								else	{ %>
									<tr>
										<td><img src="resources/error.gif" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">
											<div style="width: 100%; height:70px; overflow: auto;">
											<p style="margin-bottom: 4px;"><b>Failed:</b> Database could not be dropped!</p>
											<%
											Vector errors = db.getErrors();
											for (int i = 0; i < errors.size(); i++)	{
												out.println(errors.elementAt(i) + "<br>");
												out.println("-------------------------------------------" + "<br>");
											}
											db.clearErrors();
									 		%>
											</div>
										</td>
									</tr>				
								
									<%
						
								} %>
								</table>
								<%= Bean.getHtmlPart("C_BLOCK_END") %>
								<div class="dialogspacer" unselectable="on">&nbsp;</div>
								
								<%
							}

							if (createDb) {
								// Create Database %>
								<%= Bean.getHtmlPart("C_BLOCK_START", "Creating database ...") %>
								<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
								
								<%
								db.createDatabase(Bean.getDatabase(), Bean.getReplacer());
								if (db.noErrors())	{ %>
									<tr>
										<td><img src="resources/ok.gif" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">Database has been successfully created.</td>
									</tr>									
									<%
									enableContinue = true;
								}
								else	{ %>
								
									<tr>
										<td><img src="resources/error.gif" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">
											<div style="width: 100%; height:70px; overflow: auto;">
											<p style="margin-bottom: 4px;"><b>Failed:</b> Database could not be created!</p>
											<%
											Vector errors = db.getErrors();
											for (int i = 0; i < errors.size(); i++)	{
												out.println(errors.elementAt(i) + "<br>");
												out.println("-------------------------------------------" + "<br>");
											}
											db.clearErrors();
									 		%>
											</div>
										</td>
									</tr>				
									<%
								}
								%>
								</table>
								<%= Bean.getHtmlPart("C_BLOCK_END") %>
								<div class="dialogspacer" unselectable="on">&nbsp;</div>
								
								<%
							}

							db.closeConnection();



							if (createTables) {
								db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
								//Drop Tables (intentionally quiet)
								db.dropTables(Bean.getDatabase());
								db.clearErrors();
								db.closeConnection();

								// reopen the connection in order to display errors
								db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
								//Create Tables %>
								
								<%= Bean.getHtmlPart("C_BLOCK_START", "Creating tables ...") %>
								<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
								<%
								db.createTables(Bean.getDatabase(), Bean.getReplacer());
								if(db.noErrors())	{
									%>
									<tr>
										<td><img src="resources/ok.gif" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">Tables have been successfully created.</td>
									</tr>									
									<%
									enableContinue = true;
								}
								else	{ %>
								
									<tr>
										<td><img src="resources/error.gif" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">
											<div style="width: 100%; height:70px; overflow: auto;">
											<p style="margin-bottom: 4px;"><b>Failed:</b> Tables could not be created!</p>
											<%
											Vector errors = db.getErrors();
											for (int i = 0; i < errors.size(); i++)	{
												out.println(errors.elementAt(i) + "<br>");
												out.println("-------------------------------------------" + "<br>");
											}
											db.clearErrors();
											db.closeConnection();
									 		%>
											</div>
										</td>
									</tr>				
								
									<%
									
								}
								
								%>
								</table>
								<%= Bean.getHtmlPart("C_BLOCK_END") %>
								<%
								
							}
						}
					}
				%>

	</td>
</tr>
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.go(-2);">
<input name="btcontinue" type="submit" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled" id="btcontinue">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='cancel.jsp';" style="margin-left: 50px;">
</form>
<% if(enableContinue)	{
	out.println("<script type=\"text/javascript\">\ndocument.getElementById(\"btcontinue\").disabled = false;\n</script>");
} %>
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