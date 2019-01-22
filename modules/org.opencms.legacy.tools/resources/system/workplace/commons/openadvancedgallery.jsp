<%@ page import="org.opencms.workplace.galleries.CmsOpenAdvancedGallery" %>
<%	
	// initialize the workplace class to open the advanced gallery
	CmsOpenAdvancedGallery wp = new CmsOpenAdvancedGallery(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
<script type="text/javascript">
<!--
	<%= wp.openGallery() %>
//-->
</script>
<% wp.actionCloseDialog(); %>
<%= wp.htmlEnd() %>