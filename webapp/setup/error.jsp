<%

String pathPrefix = (String)request.getAttribute("pathPrefix");
if (pathPrefix == null) {
	pathPrefix = "";
}

%>

<table border="0" cellpadding="5" cellspacing="0" style="width: 100%; height: 100%;">
<tr>
	<td style="vertical-align: middle; height: 100%;">
		<%= Bean.getHtmlPart("C_BLOCK_START", "Error") %>
		<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td><img src="<%= pathPrefix %>resources/error.gif" border="0"></td>
				<td>&nbsp;&nbsp;</td>
				<td style="width: 100%;">
					The OpenCms setup wizard has not been started correctly!<br>
					Please click <a href="<%= pathPrefix %>index.jsp">here</a> to restart the wizard.
				</td>
			</tr>
		</table>
		<%= Bean.getHtmlPart("C_BLOCK_END") %>	
	</td>
</tr>
</table>