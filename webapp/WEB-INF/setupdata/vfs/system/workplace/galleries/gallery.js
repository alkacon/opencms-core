<script language="javascript">
<!--
	function escapeBrackets(s) {
		var searchResult = s.search(/\[.+/);
		if(searchResult != -1) {
			// cut the first '['
			s = s.substring(1,s.length);
		}
		searchResult = s.search(/.+\]/);
		if(searchResult != -1) {
			// cut the last ']'
			s = s.substring(0,s.length-1);
		}

		return s;
	}

	function link(uri, title, desc) {
		<% 	
			if (wp.MODE_WIDGET.equals(wp.getParamDialogMode())) {
		%>
			pasteLink(uri, title, desc);
		<% 
			} else { 
		%>
			if (top.window.opener.hasSelectedText() == true) {
				// text selected.
				setLink(uri);
			} else {
				pasteLink(uri, title, desc);
			}
		<% 
			} 
		%>
	}

	
	/**
	 * Sets the calculated link to the calling editor. This Function will be called when hasSelectedText() returns true
	 */
	function setLink(uri, title) {
		var linkInformation = new Object();
		linkInformation["type"] = "link";
		linkInformation["href"] = uri;
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
			result += escapeBrackets(desc);
			result += "\" target=\"";
			result += document.form.linktarget.options[document.form.linktarget.selectedIndex].value;
			result += "\">";
			result += escapeBrackets(title);
			result += "</a>";
			top.window.opener.insertHtml(result);
	<%
		}
	%>
		top.window.close();
	}
	
	/**
	 * Pastes an Image to the current position of the editor 
	 */
	function pasteImage(uri, title, desc) {
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
			var result = "<img src=\"";
			result += uri;
			result += "\" title=\"";
			result += escapeBrackets(title);
			result += "\" alt=\"";
			result += escapeBrackets(title);
			result += "\" border=\"0\">";
			top.window.opener.insertHtml(result);
	<%
		}
	%>
		top.window.close();
	}
	
	/**
	 * Pastes the content of the specified resource to the current position of the editor 
	 */
	function pasteContent() {		
			top.window.opener.insertHtml(top.preview_fs.gallery_preview.document.getElementById("icontent").innerHTML);		
			top.window.close();
	}
	
	function deleteResource(uri) {
		top.gallery_fs.gallery_head.action = "deleteResource";
		top.gallery_fs.gallery_list.location.href="<%=wp.getJsp().link(wp.C_PATH_DIALOGS+"delete.jsp")%>?resource="+uri;				
	}
	
	function publishResource(uri) {
		top.gallery_fs.gallery_list.location.href="<%=wp.getJsp().link(wp.C_PATH_DIALOGS+"publishresource.jsp")%>?resource="+uri;				
	}
	
	function editProperty(uri) {			
		// do submit only if there are changes
		if (document.form.title.value != document.form.<%= wp.PARAM_PROPERTYVALUE %>.value) {
			top.gallery_fs.gallery_head.action = "editProperty";
			document.form.<%= wp.PARAM_PROPERTYVALUE %>.value = document.form.title.value;
			document.form.submit();
			top.gallery_fs.gallery_head.displayGallery();		
			top.preview_fs.gallery_preview.reload();
		}		
	}	

	
//-->
</script>