<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryHtmls wp = new CmsGalleryHtmls(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
	<style type="text/css">
	<!--
		td.list { white-space: nowrap; padding-left: 2px; }
		
		td.headline { padding: 1px; white-space: nowrap; background:Menu; border-right: 1px solid ThreedDarkShadow; border-top: 1px solid ThreeDHighlight; border-bottom: 1px solid ThreedDarkShadow; border-left: 1px solid ThreeDHighlight; }
	//-->
	</style>	
	<script language="javascript">
	<!--
		
		/**
		 * Pastes the content of the specified resource to the current position of the editor 
		 */
		function pasteContent() {
		
				top.window.opener.insertHtml(document.getElementById("icontent").innerHTML);		
				top.window.close();
		}
		
		function deleteResource(uri) {
			top.gallery_fs.gallery_list.location.href="/opencms/opencms/system/workplace/commons/delete.jsp?resource="+uri;			
		}
		
	//-->
	</script>	
</head>
<body class="dialog" height="100%" unselectable="on">
<form class="nomargin" name="form">
<%= wp.buildGalleryItemPreview() %>
</form>
</body>
<%= wp.htmlEnd() %>