<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryDownloads wp = new CmsGalleryDownloads(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
	<style type="text/css">
	<!--
		a { text-decoration: none; color: #000; }
		a:hover { text-decoration: underline; color: #000088; }
		
		td.list { white-space: nowrap; padding-left: 2px; }
		
		td.headline { padding: 1px; white-space: nowrap; background:Menu; border-right: 1px solid ThreedDarkShadow; border-top: 1px solid ThreeDHighlight; border-bottom: 1px solid ThreedDarkShadow; border-left: 1px solid ThreeDHighlight; }
	//-->
	</style>	
	<script language="javascript">
	<!--
		
		function link(uri, title, desc) {
			 if (top.window.opener.hasSelectedText() == true) {
				// text selected.
				setLink(uri, title);
			 } else {
				pasteLink(uri, title, desc);
			 }
		}
		/**
		 * Sets the calculated link to the calling editor. This Function will be called when hasSelectedText() returns true
		 */
		function setLink(uri, title) {
			var linkInformation = new Object();
			linkInformation["type"] = "link";
			linkInformation["href"] = uri;
			linkInformation["name"] = title;
			linkInformation["target"] = document.form.linktarget.options[document.form.linktarget.selectedIndex].value;
			linkInformation["style"] = "";
			linkInformation["class"] = "";
			top.window.opener.getSelectedLink();
			top.window.opener.createLink(linkInformation);
			top.window.close();
		}	

		/**
		 * Pastes a link to the current position of the editor 
		 */
		function pasteLink(uri, title, desc) {
		<% 	
			if (wp.MODE_WIDGET.equals(wp.getParamDialogMode())) {
		%>
			top.window.opener.document.getElementById("<%= wp.getParamFieldId() %>").value  = uri;
			try {
				// toggle preview icon if possible
				top.window.opener.checkPreview("<%= wp.getParamFieldId() %>");
			} catch (e) {}
		<%	
			} else { 
		%>
				var result = "<a href=\"";
				result += uri;
				result += "\" title=\"";
				result += desc;
				result += "\" target=\"";
				result += document.form.linktarget.options[document.form.linktarget.selectedIndex].value;				
				result += "\">";
				result += title;
				result += "</a>";
				top.window.opener.insertHtml(result);
		<%
			}
		%>
			top.window.close();
		}
		
		function deleteResource(uri) {
			top.gallery_fs.gallery_list.location.href="/opencms/opencms/system/workplace/commons/delete.jsp?resource="+uri;			
		}
		
	//-->
	</script>	
</head>
<body class="dialog" style="background-color: ThreeDFace;" height="100%" unselectable="on">
<form class="nomargin" name="form">
<%= wp.buildGalleryItemPreview() %>
</form>
</body>
<%= wp.htmlEnd() %>