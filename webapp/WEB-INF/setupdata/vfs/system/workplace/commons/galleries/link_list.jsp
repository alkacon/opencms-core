<%@ page import= "org.opencms.jsp.*, 
						org.opencms.workplace.commons.*,
						org.opencms.util.CmsStringUtil" buffer="none" session="false" %>
<%	

	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryLinks wp = new CmsGalleryLinks(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>
	<link rel="stylesheet" type="text/css" href="<%=wp.getJsp().link(wp.C_PATH_GALLERIES+"gallery.css")%>">
	<script type="text/javascript">
	<!--
		function refresh() {
			try {
				var field_id = top.gallery_fs.gallery_head.document.list.<%= wp.PARAM_FIELDID %>.value;
				var dialogmode = top.gallery_fs.gallery_head.document.list.<%= wp.PARAM_DIALOGMODE %>.value;
				var previewUri = top.gallery_fs.gallery_head.previewUri;
				if (top.gallery_fs.gallery_head.action == "deleteResource") {
					top.preview_fs.gallery_buttonbar.location.href="<%=wp.getJsp().link("link_buttonbar.jsp")%>";
					top.preview_fs.gallery_preview.location.href="<%=wp.getJsp().link("link_preview.jsp")%>";
				}
				if (previewUri != null) {
			  		top.preview_fs.gallery_buttonbar.location.href="<%=wp.getJsp().link("link_buttonbar.jsp")%>?<%= wp.PARAM_FIELDID %>="+field_id+"&<%= wp.PARAM_DIALOGMODE %>="+dialogmode+"&resourcepath="+previewUri;
			  		top.preview_fs.gallery_preview.location.href="<%=wp.getJsp().link("link_preview.jsp")%>?resourcepath="+previewUri;
				}
			} catch(e) {

			}
		}
		
		function preview(uri) {
			top.gallery_fs.gallery_head.previewUri = uri;
			var dialogmode = top.gallery_fs.gallery_head.document.forms['list'].<%= wp.PARAM_DIALOGMODE %>.value;
			top.preview_fs.gallery_buttonbar.location.href="<%=wp.getJsp().link("link_buttonbar.jsp")%>?<%= wp.PARAM_FIELDID %>=<%= wp.getParamFieldId() %>&<%= wp.PARAM_DIALOGMODE %>="+dialogmode+"&resourcepath="+uri;
			top.preview_fs.gallery_preview.location.href="<%=wp.getJsp().link("link_preview.jsp")%>?resourcepath="+uri;					
		}												
	//-->
	</script>
</head>

<body class="dialog" unselectable="on" onload="refresh();">
<table border="0" cellpadding="0" cellspacing="0" class="maxwidth">
<tr>
	<td class="headline">&nbsp;</td>
	<td class="headline" width="25%"><%= wp.key("label.name") %></td>
	<td class="headline" width="45%"><%= wp.key("label.title") %></td>
	<td class="headline" width="30%"><%= wp.key("input.linkto") %></td>	
</tr>
<%= wp.buildGalleryItems() %>
</table>

</body>
<%= wp.htmlEnd() %>