<%@ page import= "org.opencms.jsp.*, 
						org.opencms.workplace.commons.*,
						org.opencms.util.CmsStringUtil" buffer="none" session="false" %>
<%	

	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryDownloads wp = new CmsGalleryDownloads(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>
	<link rel="stylesheet" type="text/css" href="gallery.css">	
	<script type="text/javascript">
	<!--
		function validateGalleryPath() {
			top.gallery_fs.gallery_head.displayGallery();
			var field_id = top.gallery_fs.gallery_head.document.forms['list'].<%= wp.PARAM_FIELDID %>.value;
			var dialogmode = top.gallery_fs.gallery_head.document.forms['list'].<%= wp.PARAM_DIALOGMODE %>.value;
			top.preview_fs.gallery_buttonbar.location.href="<%=wp.getJsp().link("download_buttonbar.jsp")%>?<%= wp.PARAM_FIELDID %>="+field_id+"&<%= wp.PARAM_DIALOGMODE %>="+dialogmode+"&resourcepath="+top.gallery_fs.gallery_head.previewUri;
			top.preview_fs.gallery_preview.location.href="<%=wp.getJsp().link("download_preview.jsp")%>?resourcepath="+top.gallery_fs.gallery_head.previewUri;
		}
		
		function preview(uri) {
			top.gallery_fs.gallery_head.previewUri = uri;
			top.preview_fs.gallery_buttonbar.location.href="<%=wp.getJsp().link("download_buttonbar.jsp")%>?<%= wp.PARAM_FIELDID %>=<%= wp.getParamFieldId() %>&<%= wp.PARAM_DIALOGMODE %>=<%= wp.getParamDialogMode() %>&resourcepath="+uri;
			top.preview_fs.gallery_preview.location.href="<%=wp.getJsp().link("download_preview.jsp")%>?resourcepath="+uri;					
		}												
	//-->
	</script>
</head>

<body class="dialog" unselectable="on"<%=CmsStringUtil.isEmpty(wp.getParamGalleryPath())?" onload=\"validateGalleryPath();\"":""%>>
<table border="0" cellpadding="0" cellspacing="0" class="maxwidth">
<%@ include file="gallery_list_headline.txt" %>
<%= wp.buildGalleryItems() %>
</table>

</body>
<%= wp.htmlEnd() %>