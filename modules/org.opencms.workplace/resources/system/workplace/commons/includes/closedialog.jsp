<%@ page import="org.opencms.workplace.*" %><%
	
	// get workplace class from request attribute
	CmsDialog wp =  CmsDialog.initCmsDialog(pageContext, request, response);
	
	boolean link = (wp.getParamCloseLink() != null);

%><%= wp.htmlStart() %>
<script type="text/javascript">
<!--
<%
	if (link) {
		%>this.location.href = "<%= wp.getParamCloseLink() %>";<%
	}
%>
//-->
</script>
<body>
</body>
<%= wp.htmlEnd() %>