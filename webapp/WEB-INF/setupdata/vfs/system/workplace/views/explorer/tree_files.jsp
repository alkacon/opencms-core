<%@ page import="
	org.opencms.workplace.explorer.*,
	org.opencms.jsp.*"
	buffer="none"
%><%
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsTree wp = new CmsTree(cms);
	
%><html>

<head>
<script language="JavaScript">
<%
	String tree = wp.getTree();
	// System.err.println(tree);
	out.println(tree);
%>
</script>
</head>

<body onload="init();">
</body>

</html>