<%@ page import="org.opencms.workplace.*" %><%
	
	// get workplace class from request attribute
	CmsDialog wp =  CmsDialog.initCmsDialog(pageContext, request, response);
	
	boolean link = (wp.getParamCloseLink() != null);

 %><%= wp.htmlStart() %>
<script type="text/javascript">
<!--
<% if (link) { %>
// check for direct edit frame
var isWp = false; 
try { 
     if (top.document.querySelector(".o-editor-frame")) {
         isWp = true; 
     } else { 
         isWp = false; 
     }
} catch (e) {} 

if (isWp) {
    top.location.href = "<%=wp.getParamCloseLink()%>";
} else if (top.frames['cmsAdvancedDirectEditor']!=null && top.frames['cmsAdvancedDirectEditor'].document!=null){
    location.href = "<%= wp.getParamCloseLink() %>";
}else{
	this.location.href = "<%= wp.getParamCloseLink() %>";
}
<%	} %>
//-->
</script>
<body>
</body>
<%= wp.htmlEnd() %>