<%@ page import="java.util.*,org.opencms.util.*, org.opencms.jsp.*, org.opencms.workplace.*, org.opencms.workplace.galleries.*, org.opencms.workplace.explorer.*" %><%

//use the CmsNewResourceUpload object with required constants
CmsNewResourceUpload wp = new CmsNewResourceUpload(pageContext, request, response);

if (CmsStringUtil.isNotEmpty(request.getParameter("gallery"))) {
	Map params = new HashMap(5);
	params.put("resource", request.getParameter("gallery"));  
	
	//path to the current folder, so the items are uploaded correctly
	params.put(CmsNewResourceUpload.PARAM_CURRENTFOLDER, request.getParameter("gallery"));
	params.put(CmsNewResourceUpload.PARAM_TARGETFRAME, "_self");
	//redirect URL  
	params.put(CmsNewResourceUpload.PARAM_REDIRECTURL, A_CmsAjaxGallery.PATH_GALLERIES + "galleryelements/upload.jsp");  
	params.put(CmsNewResourceUpload.PARAM_REDIRECT, "true");
	params.put(CmsDialog.PARAM_CLOSELINK, wp.getJsp().link(A_CmsAjaxGallery.PATH_GALLERIES + "galleryelements/upload.jsp"));
	wp.sendForward(CmsNewResourceUpload.VFS_PATH_COMMONS + "newresource_upload.jsp", params);
} else {

%>
<html>
<head>
<script type="text/javascript">
	parent.markItem(-1, "gallery");
	// refresh gallery item list
	parent.refreshGallery();
	// close thickbox iFrame
	parent.tb_remove();
</script>
</head>
<%
}
%>
</html>