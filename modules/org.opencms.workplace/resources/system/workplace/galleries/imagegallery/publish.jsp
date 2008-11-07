<%@ page import="java.util.*, org.opencms.util.*, org.opencms.workplace.*" %><%

CmsDialog wp = new CmsDialog(pageContext, request, response);

if (CmsStringUtil.isNotEmpty(request.getParameter("resource"))) {
	Map params = new HashMap(3);
	params.put("resource", request.getParameter("resource"));
	params.put(CmsDialog.PARAM_CLOSELINK, wp.getJsp().link(wp.getJsp().getRequestContext().getFolderUri() + "publish.jsp"));
	params.put(CmsDialog.PARAM_REDIRECT, "true");
	wp.sendForward(CmsWorkplace.VFS_PATH_COMMONS + "publishresource.jsp", params);
} else {
%><html>
<head>
<script type="text/javascript">
	parent.markImage(-1, "gallery");
	parent.markImage(-1, "category");
	// refresh gallery image list delayed
	setTimeout("parent.refreshGallery();", 50);
	// refresh category image list delayed
	setTimeout("parent.refreshCategory();", 150);
	// close thickbox iFrame delayed
	setTimeout("parent.tb_remove();", 300);
</script>
</head>
</html>
<%
}
%>