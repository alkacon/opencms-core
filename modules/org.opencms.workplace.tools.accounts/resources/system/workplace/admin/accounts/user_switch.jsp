<%@ page import="org.opencms.workplace.tools.accounts.*, org.opencms.ui.*"%><%	

    CmsUserOverviewDialog usersList = new CmsUserOverviewDialog(pageContext, request, response);
    usersList.actionSwitchUser();
%>
<html>
<head>
	<script type="text/javascript">
	function reload(){
		var location=window.top.location.href;
		if (location.indexOf("/system/workplace/")>=0){
			window.top.head.doReload();
		}else{
			window.top.location = "<%= CmsVaadinUtils.getWorkplaceLink() %>?_lrid="+(new Date()).getTime();
		}

	}
	</script>
</head>
<body onload="reload();"></body>
</html>