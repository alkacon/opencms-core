<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="org.opencms.setup.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<% /* Import packages */ %>
<%@ page import="org.opencms.setup.*,java.util.*" %>

<%
	/* next page to be accessed */
	String nextPage = "step_5_check_versions.jsp";

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
                    nextPage = "step_4_database_creation.jsp";
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
<%= Bean.getHtmlPart("C_HEAD_END") %>
OpenCms Setup Wizard - <%= Bean.getDatabase() %>
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<%= Bean.getHtmlPart("C_LOGO_OPENCMS") %>
<% if(setupOk)	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td align="center" valign="center">

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
							out.println("<p><nobr><input type=\"submit\" name=\"dropDb\" class=\"dialogbutton\" value=\"Yes\">&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" value=\"No\" onClick=\"history.go(-3)\" class=\"dialogbutton\"></nobr></p>");
						}
						else	{
							if(createDb && dropDb)	{

								//Drop Database
								out.print("<p>Trying to drop database  ...");
								db.dropDatabase(Bean.getDatabase(), Bean.getReplacer());
								if(db.noErrors())	{
									out.println(" <b>Ok</b></p>");
									enableContinue = true;
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
									enableContinue = true;
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
								db.createTables(Bean.getDatabase(), Bean.getReplacer());
								if(db.noErrors())	{
									out.println(" <b>Ok</b></p>");
									enableContinue = true;
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
</table>
<%= Bean.getHtmlPart("C_CONTENT_END") %>

<%= Bean.getHtmlPart("C_BUTTONS_START") %>
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="history.go(2);">
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