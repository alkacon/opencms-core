<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% /* Set all given Properties */%>
<jsp:setProperty name="Bean" property="*" />

<%	
	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);

%>

<%	/* next page to be accessed */
	String nextPage = "advanced_stexport.jsp";
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
				<td height="375" align="center" valign="top">									
					<table border="1" cellpadding="5">
						<tr>
							<td valign="top" align="center">
								<table width="220" border="0" valign="top"  cellspacing="0">
									<tr>
										<td colspan="2" align="center" class="header">
											Database
										</td>
									</tr>
									<tr>
										<td>
											Min. Conn.
										</td>
										<td align="right">
											<input type="text" size="10" name="minConn" value="<%= Bean.getMinConn() %>">
										</td>
									</tr>
									<tr>
										<td>
											Max. Conn.
										</td>
										<td align="right">
											<input type="text" size="10" name="maxConn" value="<%= Bean.getMaxConn() %>">
										</td>
									</tr>
									<%-- <tr>
										<td>
											Increase rate
										</td>
										<td align="right">
											<input type="text" size="10" name="increaseRate" value="<%= Bean.getIncreaseRate() %>">
										</td>
									</tr> --%>
									<tr>
										<td>
											Timeout
										</td>
										<td align="right">
											<input type="text" size="10" name="timeout" value="<%= Bean.getTimeout() %>">
										</td>
									</tr>
									<%-- <tr>
										<td>
											Max. Age.
										</td>
										<td align="right">
											<input type="text" size="10" name="maxAge" value="<%= Bean.getMaxAge() %>">
										</td>
									</tr> --%>													
								</table>
							</td>
							<td rowspan="2" align="center" valign="top" >
								<table width="250" border="0" cellspacing="0" cellpadding="0">
									<tr>
										<td colspan="2" align="center" class="header">
											Cache parameters
										</td>
									</tr>
									<tr>
										<td>
											User
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheUser" value="<%= Bean.getCacheUser() %>">
										</td>
									</tr>
									<tr>
										<td>
											Group
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheGroup" value="<%= Bean.getCacheGroup() %>">
										</td>
									</tr>
									<tr>
										<td>
											User groups
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheUserGroups" value="<%= Bean.getCacheUserGroups() %>">
										</td>
									</tr>
									<tr>
										<td>
											Project
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheProject" value="<%= Bean.getCacheProject() %>">
										</td>
									</tr>
									<tr>
										<td>
											Online Project
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheOnlineProject" value="<%= Bean.getCacheOnlineProject() %>">
										</td>
									</tr>
									<tr>
										<td>
											Resource
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheResource" value="<%= Bean.getCacheResource() %>">
										</td>
									</tr>
									<tr>
										<td>
											Resource Lists
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheSubres" value="<%= Bean.getCacheSubres() %>">
										</td>
									</tr>
									<tr>
										<td>
											Property
										</td>
										<td align="right">
											<input type="text" size="10" name="cacheProperty" value="<%= Bean.getCacheProperty() %>">
										</td>
									</tr>
									<tr>
										<td>
											Property def.
										</td>
										<td align="right">
											<input type="text" size="10" name="cachePropertyDef" value="<%= Bean.getCachePropertyDef() %>">
										</td>
									</tr>
									<tr>
										<td>
											Propety def. vector
										</td>
										<td align="right">
											<input type="text" size="10" name="cachePropertyDefVector" value="<%= Bean.getCachePropertyDefVector() %>">
										</td>
									</tr>
									<tr>
										<td class="bold">
											Element cache
										</td>
										<td>
											<input type="radio" name="elementCache" value="true" <% if(Bean.getElementCache().equals("true")) {out.print("checked");} %>> enabled
											<input type="radio" name="elementCache" value="false" <% if(Bean.getElementCache().equals("false")) {out.print("checked");} %>> disabled
										</td>
									</tr>									
									<tr>
										<td>
											Cache URI
										</td>
										<td align="right">
											<input type="text" size="10" name="elementCacheURI" value="<%= Bean.getElementCacheURI() %>">
										</td>
									</tr>
									<tr>
										<td>
											Cache elements
										</td>
										<td align="right">
											<input type="text" size="10" name="elementCacheElements" value="<%= Bean.getElementCacheElements() %>">
										</td>
									</tr>
									<tr>
										<td>
											Cache variants
										</td>
										<td align="right">
											<input type="text" size="10" name="elementCacheVariants" value="<%= Bean.getElementCacheVariants() %>">
										</td>
									</tr>									
								</table>
							</td>
						</tr>
						<tr>
							<td align="center" valign="top">
								<table width="220" border="0" cellspacing="0">
									<tr>
										<td align="center" class="header" colspan="2">
											Session failover
										</td>
									</tr>
									<tr>
										<td align="right">
											<input type="radio" name="sessionFailover" value="true" <% if(Bean.getSessionFailover().equals("true")) {out.print("checked");} %>> enabled
										</td>
										<td align="left">
											<input type="radio" name="sessionFailover" value="false" <% if(Bean.getSessionFailover().equals("false")) {out.print("checked");} %>> disabled
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<hr>
										</td>
									</tr>													
									<tr>
										<td align="center" class="header" colspan="2">
											Backup published resources
										</td>
									</tr>
									<tr>
										<td align="right">
											<input type="radio" name="historyEnabled" value="true" <% if(Bean.getHistoryEnabled().equals("true")) {out.print("checked");} %>> Yes
										</td>
										<td align="left">
											<input type="radio" name="historyEnabled" value="false" <% if(Bean.getHistoryEnabled().equals("false")) {out.print("checked");} %>> No
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<hr>
										</td>
									</tr>													
									<tr>
										<td align="center" class="header" colspan="2">
											Http streaming
										</td>
									</tr>
									<tr>
										<td align="right">
											<input type="radio" name="httpStreaming" value="true" <% if(Bean.getHttpStreaming().equals("true")) {out.print("checked");} %>> enabled
										</td>
										<td align="left">
											<input type="radio" name="httpStreaming" value="false" <% if(Bean.getHttpStreaming().equals("false")) {out.print("checked");} %>> disabled
										</td>
									</tr>																										
								</table>
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