<jsp:useBean id="Bean" class="com.opencms.boot.CmsSetup" scope="session" />
<jsp:setProperty name="Bean" property="*" /><%--

--%><%
	/* next page to be accessed */
	String nextPage = "save_properties.jsp";
%><%--

--%><html>
<head> 
<title>OpenCms Setup Wizard</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="Stylesheet" type="text/css" href="style.css">
<script language="Javascript">

	function checkSubmit()	{
		if(document.forms[0].ethernetAddress.value != "")	{
			var octet = document.forms[0].ethernetAddress.value.split(":");
			var i = octet.length;

			if (i == 6) {			
				for (; i > 0; i--) {
					var o = parseInt(octet[i-1],16)
					if (isNaN(o) || o < 0 || o > 0xFF) break;
				}
			}
			if (i) {
				alert("MAC address format must be FF:FF:FF:FF:FF:FF");			
				document.forms[0].ethernetAddress.focus();
				return false;
			}
		}
		
		return true;
	}
	
</script>	
</head>
<body>

<form action="<%=nextPage%>" method="POST" name="" onSubmit="return checkSubmit()">
<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
<tr>	
<td align="center" valign="middle">
<table border="1" cellpadding="0" cellspacing="0">
<tr>
	<td>	
		<table class="background" width="700" height="500" border="0" cellpadding="5" cellspacing="0">
			<tr>
				<td class="title" height="25">OpenCms Setup Wizard</td>
			</tr>

			<tr>
				<td height="50" align="right"><img src="opencms.gif" alt="OpenCms" border="0"></td>
			</tr>
			
						
			
			<tr>
				<td height="375" align="center" valign="middle">									
					<table border="1" cellpadding="5">
						<tr>
							<td valign="top" align="center">
								<table width="220" border="0" valign="top"  cellspacing="0">
									<tr>
										<td colspan="2" align="center" class="header">
											Flex Cache Settings
										</td>
									</tr>	
									<tr>
										<td class="bold">
											Flex Cache
										</td>
										<td align="right">
											<input type="radio" name="flexCacheEnabled" value="true" <%=Bean.isChecked(Bean.getFlexCacheEnabled(),"true")%>> on
											<input type="radio" name="flexCacheEnabled" value="false" <%=Bean.isChecked(Bean.getFlexCacheEnabled(),"false")%>> off
										</td>
									</tr>	
									<tr>
										<td class="bold">
											Cache offline
										</td>
										<td align="right">
											<input type="radio" name="cacheOfflineEnabled" value="true" <%=Bean.isChecked(Bean.getCacheOfflineEnabled(),"true")%>> on
											<input type="radio" name="cacheOfflineEnabled" value="false" <%=Bean.isChecked(Bean.getCacheOfflineEnabled(),"false")%>> off
										</td>
									</tr>	
									<tr>
										<td class="bold">
											Force GC
										</td>
										<td align="right">
											<input type="radio" name="forceGc" value="true" <%=Bean.isChecked(Bean.getForceGc(),"true")%>> on
											<input type="radio" name="forceGc" value="false" <%=Bean.isChecked(Bean.getForceGc(),"false")%>> off
										</td>
									</tr>									
								</table>
							</td>
							<td rowspan="2" align="center" valign="top" >
						     <table width="220" border="0" valign="top"  cellspacing="0">
									<tr>
										<td colspan="2" align="center" class="header">
											Flex Cache Parameters
										</td>
									</tr>
									<tr>
										<td>
											Max. bytes in cache
										</td>
										<td align="center">
											<input type="text" size="8" name="maxCacheBytes" value="<%=Bean.getMaxCacheBytes()%>">
										</td>
									</tr>
									<tr>
										<td>
											Avg. bytes in cache
										</td>
										<td align="center">
											<input type="text" size="8" name="avgCacheBytes" value="<%=Bean.getAvgCacheBytes()%>">
										</td>
									</tr>
									<tr>
										<td>
											Max. byte size per entry
										</td>
										<td align="center">
											<input type="text" size="8" name="maxEntryBytes" value="<%=Bean.getMaxEntryBytes()%>">
										</td>
									</tr>
									<tr>
										<td>
											Max. number of entries
										</td>
										<td align="center">
											<input type="text" size="8" name="maxEntries" value="<%=Bean.getMaxEntries()%>">
										</td>
									</tr>
									<tr>
										<td>
											Max. number of keys
										</td>
										<td align="center">
											<input type="text" size="8" name="maxKeys" value="<%=Bean.getMaxKeys()%>">
										</td>
									</tr>													
								</table>
							</td>
						</tr>
						<tr>
							<td align="center" valign="top">
								<table width="220" border="0" valign="top"  cellspacing="0">
									<tr>
										<td colspan="2" align="center" class="header">
											File System Settings (I)
										</td>
									</tr>	
									<tr>
										<td class="bold">
											Filename translation
										</td>
										<td align="right">
											<input type="radio" name="filenameTranslationEnabled" value="true" <%=Bean.isChecked(Bean.getFilenameTranslationEnabled(),"true")%>> on
											<input type="radio" name="filenameTranslationEnabled" value="false" <%=Bean.isChecked(Bean.getFilenameTranslationEnabled(),"false")%>> off
										</td>
									</tr>	
									<tr>
										<td class="bold">
											Directory translation
										</td>
										<td align="right">
											<input type="radio" name="directoryTranslationEnabled" value="true" <%=Bean.isChecked(Bean.getDirectoryTranslationEnabled(),"true")%>> on
											<input type="radio" name="directoryTranslationEnabled" value="false" <%=Bean.isChecked(Bean.getDirectoryTranslationEnabled(),"false")%>> off
										</td>
									</tr>										
								</table>
							</td>
						</tr>
						
						
						<tr>
							<td align="center" valign="top" colspan="2">
								<table width="440" border="0" valign="top"  cellspacing="0">
									<tr>
										<td align="center" class="header">
											File System Settings (II)
										</td>
									</tr>	
									<tr>
										<td align="left">
											Directory index file(s):
										</td>
									</tr>									
									<tr>
										<td align="center">
											<input type="text" size="70" name="directoryIndexFiles" value="<%=Bean.getDirectoryIndexFiles()%>">
										</td>
									</tr>										
								</table>
							</td>
						</tr>						
						
						
						<tr>
							<td align="center" valign="top" colspan="2">
								<table width="440" border="0" valign="top"  cellspacing="0">
									<tr>
										<td align="center" class="header">
											Server ethernet address
										</td>
									</tr>	
									<tr>
										<td align="left">
											MAC address (used for UUID generation, leave blank if unknown):
										</td>
									</tr>									
									<tr>
										<td align="center">
											<input type="text" size="70" name="ethernetAddress" value="<%= Bean.getEthernetAddress()%>">
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
				</table>
			</td>
		</tr>
	</table>
</td>
</tr>
</table>
</form>

</body>
</html>