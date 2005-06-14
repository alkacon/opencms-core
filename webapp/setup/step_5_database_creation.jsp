<%@ page import="org.opencms.setup.*,java.util.*" session="true" %><%--
--%><jsp:useBean id="Bean" class="org.opencms.setup.CmsSetupBean" scope="session" /><%--
--%><jsp:setProperty name="Bean" property="*" /><%

	// next page 
	String nextPage = "step_6_module_selection.jsp";	
	// previous page 
	String prevPage = "step_3_database_selection.jsp";

	CmsSetupDb db = null;

	boolean createDb = false;
	boolean createTables = false;
	boolean dbExists = false;
	boolean dropDb = false;

	if (Bean.isInitialized()) {

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
			db = new CmsSetupDb(Bean.getWebAppRfsPath());
			temp = request.getParameter("dropDb");
			dropDb = temp != null && "Yes".equals(temp);

			/* check if database exists */
			if(!dropDb)	{
				db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbConStrParams(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
				dbExists = db.noErrors();
				if(dbExists)	{
					db.closeConnection();
				}
				else	{
					db.clearErrors();
				}
			}
			if( !dbExists || dropDb)	{
				db.setConnection(Bean.getDbDriver(), Bean.getDbCreateConStr(), Bean.getDbConStrParams(), Bean.getDbCreateUser(), Bean.getDbCreatePwd());
			}
			else {
				if (createDb || createTables) {
					nextPage = "step_5_database_creation.jsp";
			  	}
			}
		}
	}

	boolean dbError = false;
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
OpenCms Setup Wizard - Create database & tables
<%= Bean.getHtmlPart("C_CONTENT_SETUP_START") %>
<% if (Bean.isInitialized())	{ %>
<form action="<%= nextPage %>" method="post" class="nomargin">
<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 350px;">
<tr>
	<td style="vertical-align: middle;">
				<%
					if (!createDb && !createTables && !dbExists)	{
						enableContinue = true;
						%>
						<%= Bean.getHtmlPart("C_BLOCK_START", "Create Database") %>
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td><img src="resources/warning.png" border="0"></td>
								<td>&nbsp;&nbsp;</td>
								<td>You have not created the OpenCms database.<br>
									You cannot import the workplace successfully without the database and tables!
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
									<td><img src="resources/warning.png" border="0"></td>
									<td>&nbsp;&nbsp;</td>
									<td>An existing database has been detected. Drop it ?</td>
								</tr>
								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>
								<tr>
									<td colspan="2">&nbsp;</td>
									<td>
										<input type="submit" name="dropDb" class="dialogbutton" style="margin-left: 0;" value="Yes">&nbsp;&nbsp;&nbsp;&nbsp;
										<input type="button" value="No" onClick="location.href='step_3_database_selection.jsp';" class="dialogbutton">
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
										<td><img src="resources/ok.png" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">Database has been successfully dropped.</td>
									</tr>									
									<%
									enableContinue = true;
								}
								else	{
									enableContinue = false;
									dbError = true;
								 %>
									<tr>
										<td><img src="resources/error.png" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">
											<div style="width: 100%; height:70px; overflow: auto;">
											<p style="margin-bottom: 4px;">Database could not be dropped!</p>
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
										<td><img src="resources/ok.png" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">Database has been successfully created.</td>
									</tr>									
									<%
									enableContinue = true;
								}
								else	{ 
									enableContinue = false;
									dbError = true;
								%>
								
									<tr>
										<td><img src="resources/error.png" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">
											<div style="width: 100%; height:70px; overflow: auto;">
											<p style="margin-bottom: 4px;">Database could not be created!</p>
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
								db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbConStrParams(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
								//Drop Tables (intentionally quiet)
								db.dropTables(Bean.getDatabase());
								db.clearErrors();
								db.closeConnection();

								// reopen the connection in order to display errors
								db.setConnection(Bean.getDbDriver(), Bean.getDbWorkConStr(), Bean.getDbConStrParams(), Bean.getDbWorkUser(),Bean.getDbWorkPwd());
								//Create Tables %>
								
								<%= Bean.getHtmlPart("C_BLOCK_START", "Creating tables ...") %>
								<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
								<%
								db.createTables(Bean.getDatabase(), Bean.getReplacer());
								if(db.noErrors())	{
									%>
									<tr>
										<td><img src="resources/ok.png" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">Tables have been successfully created.</td>
									</tr>									
									<%
									enableContinue = true;
								}
								else	{ 
									enableContinue = false;
									dbError = true;
								%>
								
									<tr>
										<td><img src="resources/error.png" border="0"></td>
										<td>&nbsp;&nbsp;</td>
										<td style="width: 100%;">
											<div style="width: 100%; height:70px; overflow: auto;">
											<p style="margin-bottom: 4px;">Tables could not be created!</p>
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
<input name="back" type="button" value="&#060;&#060; Back" class="dialogbutton" onclick="location.href='<%= prevPage %>';">
<input name="btcontinue" type="submit" value="Continue &#062;&#062;" class="dialogbutton" disabled="disabled" id="btcontinue">
<input name="cancel" type="button" value="Cancel" class="dialogbutton" onclick="location.href='index.jsp';" style="margin-left: 50px;">
</form>
<% if (enableContinue && !dbError)	{
	out.println("<script type=\"text/javascript\">\ndocument.getElementById(\"btcontinue\").disabled = false;\n</script>");
} %>
<%= Bean.getHtmlPart("C_BUTTONS_END") %>
<% } else	{ %>

<%@ include file="error.jsp" %>

<%= Bean.getHtmlPart("C_CONTENT_END") %>
<% } %>
<%= Bean.getHtmlPart("C_HTML_END") %>