<%@ page import="java.util.*, org.opencms.util.*, org.opencms.workplace.*, org.opencms.workplace.galleries.*" %><%

CmsDialog wp = new CmsDialog(pageContext, request, response);

if (CmsStringUtil.isNotEmpty(request.getParameter("resource"))) {
	Map params = new HashMap(3);
	params.put("resource", request.getParameter("resource"));
	params.put(CmsDialog.PARAM_CLOSELINK, wp.getJsp().link(A_CmsAjaxGallery.PATH_GALLERIES + "galleryelements/delete.jsp"));
	params.put(CmsDialog.PARAM_REDIRECT, "true");
	wp.sendForward(CmsWorkplace.VFS_PATH_COMMONS + "delete_standard.jsp", params);
} else {
%><html>
<head>
<script type="text/javascript">
	parent.markItem(-1, "gallery");
	parent.markItem(-1, "category");
	// refresh gallery item list delayed
	setTimeout("parent.refreshGallery();", 50);
	// refresh category item list delayed
	setTimeout("parent.refreshCategory();", 150);
	// close thickbox iFrame delayed
	setTimeout("parent.tb_remove();", 300);
</script>
</head>
</html>
<%
}
%>