<%@ page import="java.util.*,org.opencms.util.*, org.opencms.jsp.*, org.opencms.workplace.*, org.opencms.workplace.galleries.*, org.opencms.workplace.explorer.*" %><%

// initialize the workplace class 
CmsNewResourcePointer wp = new CmsNewResourcePointer(pageContext, request, response);

if (CmsStringUtil.isNotEmpty(request.getParameter("createlink"))) {

	Map params = new HashMap(5);
	params.put("resource", request.getParameter("createlink"));   
	//path to the current folder, so the items are uploaded correctly
	params.put(CmsNewResource.PARAM_CURRENTFOLDER, request.getParameter("createlink"));
	params.put(CmsDialog.PARAM_REDIRECT, "true");
	params.put(CmsDialog.PARAM_CLOSELINK, wp.getJsp().link(A_CmsAjaxGallery.PATH_GALLERIES + "linkgallery/createlink.jsp"));
	wp.sendForward(CmsWorkplace.VFS_PATH_COMMONS + "newresource_pointer.jsp", params);

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