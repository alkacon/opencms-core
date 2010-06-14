<%@ page import="org.opencms.ade.galleries.CmsOpenGallery" %>
<%	
	// initialize the workplace class to open the advanced gallery
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