<%@ page import="org.opencms.workplace.tools.accounts.*, org.opencms.ui.*"%><%	

    CmsUserOverviewDialog usersList = new CmsUserOverviewDialog(pageContext, request, response);
   String directEditTarget= usersList.actionSwitchUser();
%>
<html>
<head>
	<script type="text/javascript">
	function reload(){
		<% if (directEditTarget==null) { %>
		var location=window.top.location.href;
		if (location.indexOf("/system/workplace/")>=0){
			window.top.head.doReload();
		}else{
			window.top.location = "<%= CmsVaadinUtils.getWorkplaceLink() %>?_lrid="+(new Date()).getTime();
		}
		<% } else { %>
		window.top.location = "<%= directEditTarget %>";
		<% } %>
	}
	</script>
</head>
<body onload="reload();"></body>
</html>