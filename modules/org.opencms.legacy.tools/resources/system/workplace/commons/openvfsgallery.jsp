<%@ page import="org.opencms.ade.galleries.CmsOpenVfsGallery" %>
<%	
	// initialize the workplace class to open the advanced gallery
	CmsOpenVfsGallery wp = new CmsOpenVfsGallery(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
<script type="text/javascript">
<!--
	<%= wp.openGallery() %>
//-->
</script>
<% wp.actionCloseDialog(); %>
<%= wp.htmlEnd() %>