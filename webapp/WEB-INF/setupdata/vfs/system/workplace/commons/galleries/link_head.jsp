<%@ page import="org.opencms.jsp.*,
					  org.opencms.workplace.CmsWorkplaceManager,
					  org.opencms.main.OpenCms, 
					  org.opencms.workplace.commons.*,
					  org.opencms.workplace.explorer.CmsExplorerTypeSettings,
					  org.opencms.workplace.explorer.CmsNewResource,
					  org.opencms.workplace.explorer.CmsNewResourceUpload,
					  org.opencms.file.types.CmsResourceTypePointer" buffer="none" session="false" %>

<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryLinks wp = new CmsGalleryLinks(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>

<script type="text/javascript">
<!--
	var previewUri = null;
	function displayGallery() {
		var mainForm = document.forms["main"];
		if (mainForm.gallerypath.options != null) {
			mainForm.submit();
		} else {
			alert("<%=wp.key("error.reason.linknogallery")%>");
			top.window.close();			
		}
	}
	
	// displays initial gallery item list in list frame (needed when popup is loaded the first time)
	function displayFirstGallery() {
		var listForm = document.forms["list"];
		if (listForm.elements["<%= wp.PARAM_GALLERYPATH %>"].value == "") {
			var mainForm = document.forms["main"];
			listForm.elements["<%= wp.PARAM_GALLERYPATH %>"].value = mainForm.elements["<%= wp.PARAM_GALLERYPATH %>"].value;
			listForm.submit();
		}
	}
	
	function create() {
		var currentfolder = document.main.gallerypath.options[document.main.gallerypath.selectedIndex].value;
		top.gallery_fs.gallery_list.location.href="<%=wp.getJsp().link(wp.C_PATH_DIALOGS+OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypePointer.C_RESOURCE_TYPE_NAME).getNewResourceUri())%>?action=newform&<%=CmsNewResourceUpload.PARAM_REDIRECTURL%>=/system/workplace/commons/galleries/link_head.jsp&<%=CmsNewResourceUpload.PARAM_TARGETFRAME%>=gallery_list&<%=CmsNewResource.PARAM_CURRENTFOLDER%>="+currentfolder;		
	}
//-->
</script>

</head>

<body style="background-color: ThreeDFace; margin: 0; padding: 2px;">

<form name="main" action="<%= cms.link("link_fs_sub.jsp") %>" target="gallery_fs" method="post" class="nomargin">
<input type="hidden" name="<%= wp.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
<input type="hidden" name="<%= wp.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">

<table border="0" cellpadding="1" cellspacing="0" width="100%">
<tr>
	<td colspan="2"><%= wp.key("button.linklist") %></td>
</tr>
<tr>
	<td colspan="2"><%= wp.buildGallerySelectBox() %></td>
</tr>
<tr>
	<td class="maxwidth">
		<table class="maxwidth" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<%= wp.button("javascript:create();", null, "wizard", "input.upload", 0) %>
			<%= wp.buttonBarSpacer(5) %>
			<td class="maxwidth"><input type="text" style="width: 98%" name="<%= wp.PARAM_SEARCHWORD %>" id="<%= wp.PARAM_SEARCHWORD %>" value="<%= wp.getParamSearchWord() %>"></td>			
			<%= wp.button("javascript:displayGallery();", null, "search", "input.search", 0) %>
		</tr>
		</table>
	</td>
	<td align="right">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<%= wp.buildPageSelectBox() %>			
		</tr>
		</table>
	</td>
</tr>
</table>

</form>

<form name="list" action="<%= cms.link("link_list.jsp") %>" method="post" class="nomargin" target="gallery_list">
	<input type="hidden" name="<%= wp.PARAM_GALLERYPATH %>" value="<%= wp.getParamGalleryPath() %>">
	<input type="hidden" name="<%= wp.PARAM_PAGE %>">
	<input type="hidden" name="<%= wp.PARAM_SEARCHWORD %>">
	<input type="hidden" name="<%= wp.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
	<input type="hidden" name="<%= wp.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">
</form>

<script type="text/javascript">
<!--
	displayFirstGallery();
//-->
</script>


</body>
<%= wp.htmlEnd() %>