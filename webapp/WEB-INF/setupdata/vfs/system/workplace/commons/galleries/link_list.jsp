<%@ page import= "org.opencms.jsp.*, 
						org.opencms.workplace.commons.*,
						org.opencms.util.CmsStringUtil" buffer="none" session="false" %>
<%	

	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryLinks wp = new CmsGalleryLinks(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>
	<style type="text/css">
		a { text-decoration: none; color: #000; }
		a:hover { text-decoration: underline; color: #000088; }
		
		td.list { white-space: nowrap; padding-left: 2px; }
		
		td.headline { padding: 1px; white-space: nowrap; background-color:ThreeDFace; border-right: 1px solid ThreedDarkShadow; border-top: 1px solid ThreeDHighlight; border-bottom: 1px solid ThreedDarkShadow; border-left: 1px solid ThreeDHighlight; }
	</style>
	
	<script type="text/javascript">
	<!--
		function validateGalleryPath() {
			top.gallery_fs.gallery_head.displayGallery();
			top.gallery_preview.location.href="galleries/link_preview.jsp?resourcepath="+top.gallery_fs.gallery_head.previewUri;
		}
		
		function preview(uri) {
			top.gallery_fs.gallery_head.previewUri = uri;
			top.gallery_preview.location.href="link_preview.jsp?resourcepath="+uri;			
		}												
	//-->
	</script>
</head>

<body class="dialog" unselectable="on"<%=CmsStringUtil.isEmpty(wp.getParamGalleryPath())?" onload=\"validateGalleryPath();\"":""%>>
<table border="0" cellpadding="0" cellspacing="0" class="maxwidth">
<tr>
	<td class="headline">&nbsp;</td>
	<td class="headline maxwidth"><%= wp.key("label.title") %></td>
	<td class="headline"><%= wp.key("label.name") %></td>
	<td class="headline"><%= wp.key("input.linkto") %></td>	
</tr>
<%= wp.buildGalleryItems() %>
</table>

</body>
<%= wp.htmlEnd() %>