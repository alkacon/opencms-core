<!-- ------------------------------------------------- JSP DECLARATIONS ------------------------------------------------ -->
<% /* Initialize the Bean */ %>
<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />

<% 
	/* Set all given Properties */
	
	Bean.setStaticExport(request.getParameter("staticExport"));			
	Bean.setStaticExportPath(request.getParameter("staticExportPath"));				
	
	Bean.setUrlPrefixExport(request.getParameter("urlPrefixExport"));		
	Bean.setUrlPrefixHttp(request.getParameter("urlPrefixHttp"));		
	Bean.setUrlPrefixHttps(request.getParameter("urlPrefixHttps"));	
	Bean.setUrlPrefixServername(request.getParameter("urlPrefixServername"));
	

	/* true if properties are initialized */
	boolean setupOk = (Bean.getProperties()!=null);
	
	/* next page to be accessed */
	String nextPage = "flex_properties.jsp";
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
					<table border="1">
						<tr>
							<td align="center">
								<table border="0">
									<tr>
										<td align="center"  valign="top">
											<table border="1">
												<tr>
													<td>
														<table border="0">
															<tr>
																<td class="bold">
																	OpenCms Logging: 
																</td>
																<td>
																	<input type="radio" name="logging" value="true" <% if(Bean.getLogging().equals("true")) { out.print("checked");} %>>enabled
																	<input type="radio" name="logging" value="false" <% if(Bean.getLogging().equals("false")) { out.print("checked");} %>>disabled
																</td>
															</tr>
															<tr><td colspan="2"><hr></td></tr>
															<tr>
																<td>
																	Timestamp
																</td>
																<td>
																	<input type="radio" name="logTimestamp" value="true" <% if(Bean.getLogTimestamp().equals("true")) { out.print("checked");} %>>enabled
																	<input type="radio" name="logTimestamp" value="false" <% if(Bean.getLogTimestamp().equals("false")) { out.print("checked");} %>>disabled
																</td>
															</tr>
															<tr>
																<td>
																	Memory
																</td>
																<td>
																	<input type="radio" name="logMemory" value="true" <% if(Bean.getLogMemory().equals("true")) { out.print("checked");} %>>enabled
																	<input type="radio" name="logMemory" value="false" <% if(Bean.getLogMemory().equals("false")) { out.print("checked");} %>>disabled
																</td>
															</tr>
															<tr>
																<td>
																	Date format
																</td>
																<td align="center">
																	<input type="text" size="15" style="width:150px;"  name="logDateFormat" value="<%= Bean.getLogDateFormat() %>">
																</td>
															</tr>
															<tr>
																<td>
																	Log queue max. age
																</td>
																<td align="center">
																	<input type="text" size="15" style="width:150px;" name="logQueueMaxAge" value="<%= Bean.getLogQueueMaxAge() %>">
																</td>
															</tr>
															<tr>
																<td>
																	Log queue max. size
																</td>
																<td align="center">
																	<input type="text" size="15" style="width:150px;" name="logQueueMaxSize" value="<%= Bean.getLogQueueMaxSize() %>">
																</td>
															</tr>
															<tr>
																<td>
																	Log file:
																</td>
																<td align="center">
																	<input type="text" name="logFile" size="15" style="width:150px;" value="<%= Bean.getLogFile() %>">
																</td>
															</tr>																		
														</table>
													</td>
												</tr>																
											</table>
										</td>
										<td align="center" valign="top">
											<table border="1">															
												<tr>
													<td>
														<table border="0">
															<tr>
																<td class="bold">
																	Logging channel name:
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelName" value="true" <% if (Bean.getLoggingChannelName().equals("true")) {out.print("checked");} %>>enabled
																</td>
																<td>
																	<input type="radio" name="loggingChannelName" value="false" <% if (Bean.getLoggingChannelName().equals("false")) {out.print("checked");} %>>disabled
																</td>
															</tr>
															<tr><td colspan="3"><hr></td></tr>
															<tr>
																<td align="left" class="bold">
																	Channel
																</td>
																<td align="center" class="bold">
																	Enabled
																</td>
																<td align="center" class="bold">
																	Disabled
																</td>																					
															</tr>
															<tr>
																<td align="left">
																	opencms_init
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_init" value="true" <% if (Bean.getLoggingChannelOpencms_init().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_init" value="false" <% if (Bean.getLoggingChannelOpencms_init().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>
															<tr>
																<td align="left">
																	opencms_debug
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_debug" value="true" <% if (Bean.getLoggingChannelOpencms_debug().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_debug" value="false" <% if (Bean.getLoggingChannelOpencms_debug().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>
															<tr>
																<td align="left">
																	opencms_cache
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_cache" value="true" <% if (Bean.getLoggingChannelOpencms_cache().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_cache" value="false" <% if (Bean.getLoggingChannelOpencms_cache().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>
															<tr>
																<td align="left">
																	opencms_info
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_info" value="true" <% if (Bean.getLoggingChannelOpencms_info().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_info" value="false" <% if (Bean.getLoggingChannelOpencms_info().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>
															<tr>
																<td align="left">
																	opencms_pool
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_pool" value="true" <% if (Bean.getLoggingChannelOpencms_pool().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_pool" value="false" <% if (Bean.getLoggingChannelOpencms_pool().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>						
															<tr>
																<td align="left">
																	opencms_streaming
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_streaming" value="true" <% if (Bean.getLoggingChannelOpencms_streaming().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_streaming" value="false" <% if (Bean.getLoggingChannelOpencms_streaming().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>
															<tr>
																<td align="left">
																	opencms_elementcache
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_elementcache" value="true" <% if (Bean.getLoggingChannelOpencms_elementcache().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_elementcache" value="false" <% if (Bean.getLoggingChannelOpencms_elementcache().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>															
															<tr>
																<td align="left">
																	opencms_critical
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_critical" value="true" <% if (Bean.getLoggingChannelOpencms_critical().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelOpencms_critical" value="false" <% if (Bean.getLoggingChannelOpencms_critical().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>	
															<tr>
																<td align="left">
																	modules_debug
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelModules_debug" value="true" <% if (Bean.getLoggingChannelModules_debug().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelModules_debug" value="false" <% if (Bean.getLoggingChannelModules_debug().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>
															<tr>
																<td align="left">
																	modules_info
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelModules_info" value="true" <% if (Bean.getLoggingChannelModules_info().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelModules_info" value="false" <% if (Bean.getLoggingChannelModules_info().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>																					
															<tr>
																<td align="left">
																	modules_critical
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelModules_critical" value="true" <% if (Bean.getLoggingChannelModules_critical().equals("true")) {out.print("checked");} %>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingChannelModules_critical" value="false" <% if (Bean.getLoggingChannelModules_critical().equals("false")) {out.print("checked");} %>>
																</td>
															</tr>	
															
															<tr>
																<td align="left">
																	flex_cache
																</td>
																<td align="center">
																	<input type="radio" name="loggingFlexCache" value="true" <%=Bean.isChecked(Bean.getLoggingFlexCache(),"true")%>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingFlexCache" value="false" <%=Bean.isChecked(Bean.getLoggingFlexCache(),"false")%>>
																</td>
															</tr>
															
															<tr>
																<td align="left">
																	flex_loader
																</td>
																<td align="center">
																	<input type="radio" name="loggingFlexLoader" value="true" <%=Bean.isChecked(Bean.getLoggingFlexLoader(),"true")%>>
																</td>
																<td align="center">
																	<input type="radio" name="loggingFlexLoader" value="false" <%=Bean.isChecked(Bean.getLoggingFlexLoader(),"false")%>>
																</td>
															</tr>																																			
														</table>
													</tr>
												</td>
											</table>
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
									<input type="button" class="button" style="width:150px;" width="150" value="&#060;&#060; Back" onclick="history.go(-1)">
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