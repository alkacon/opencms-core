<%@ page import="org.opencms.jsp.*, org.opencms.workplace.*, org.opencms.workplace.explorer.*" %><%

CmsDialog wp = new CmsDialog(pageContext, request, response);

%><html>
<head>
<%
if (request.getParameter("gallery") != null) {
	%>
	</head>
	<body style="margin: 0; padding: 0; text-align: center; background-color: ThreeDFace;">
	<%= CmsNewResourceUpload.createAppletCode(wp.getJsp(), wp.getLocale(), request.getParameter("gallery"), "/system/workplace/galleries/imagegallery/upload.jsp", "_self", null) %>
	</body>
<%
} else {
%>
<script type="text/javascript">
	parent.markImage(-1, "gallery");
	// refresh gallery image list
	parent.refreshGallery();
	// close thickbox iFrame
	parent.tb_remove();
</script>
</head>
<%
}
%>
</html>