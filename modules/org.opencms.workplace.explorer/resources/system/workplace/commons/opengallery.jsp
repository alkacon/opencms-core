<%@ page import="org.opencms.workplace.galleries.CmsOpenGallery" %>
<%	
	// initialize the workplace class
	CmsOpenGallery wp = new CmsOpenGallery(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
<script type="text/javascript">
<!--
	<%= wp.openGallery() %>
//-->
</script>
<% wp.actionCloseDialog(); %>
<%= wp.htmlEnd() %>
