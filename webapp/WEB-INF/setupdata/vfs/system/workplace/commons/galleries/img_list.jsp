<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %><%	

	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryImages wp = new CmsGalleryImages(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>
	<style type="text/css">
		a { text-decoration: none; color: #000; }
		a:hover { text-decoration: underline; color: #000088; }
		
		td.imglist { white-space: nowrap; padding-left: 2px; }
		
		td.headline { padding: 1px; white-space: nowrap; background:Menu; border-right: 1px solid ThreedDarkShadow; border-top: 1px solid ThreeDHighlight; border-bottom: 1px solid ThreedDarkShadow; border-left: 1px solid ThreeDHighlight; }
	</style>
	
	<script type="text/javascript">
	<!--
		function previewImage(imageUri, imageTitle) {
			var previewDoc = top.gallery_preview.document;
			previewDoc.getElementById("imgnode").src = imageUri;
			previewDoc.getElementById("imgtitle").childNodes[0].nodeValue = imageTitle;
			previewDoc.getElementById("imgdiv").className = "show";
		}
		
		function pasteImage(imageUri, imageTitle, imageDesc) {
		<% 	
		if (wp.MODE_WIDGET.equals(wp.getParamDialogMode())) {
		%>
			top.window.opener.document.getElementById("<%= wp.getParamFieldId() %>").value  = imageUri;
			try {
				// toggle preview icon if possible
				top.window.opener.checkPreview("<%= wp.getParamFieldId() %>");
			} catch (e) {}
		<%	
		} else { %>
			var result = "<img src=\"";
			result += imageUri;
			result += "\" title=\"";
			result += imageTitle;
			result += "\" alt=\"";
			result += imageDesc;
			result += "\" border=\"0\">";
			top.window.opener.insertHtml(result);
		<%
		}
		%>
			top.window.close();
		}
	//-->
	</script>
</head>

<body class="dialog" unselectable="on">
<table border="0" cellpadding="0" cellspacing="0" class="maxwidth">
<tr>
	<td class="headline">&nbsp;</td>
	<td class="headline maxwidth"><%= wp.key("label.title") %></td>
	<td class="headline"><%= wp.key("label.name") %></td>
	<td class="headline" style="text-align: right;"><%= wp.key("label.size") %></td>	
</tr>
<%= wp.buildGalleryList() %>
</table>

</body>
<%= wp.htmlEnd() %>