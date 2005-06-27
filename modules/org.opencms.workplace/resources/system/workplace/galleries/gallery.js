<script language="javascript">
<!--
	function escapeBrackets(s) {
		var searchResultStart = s.search(/\[.+/);
		var searchResultEnd = s.search(/.+\]/);
		var cut = (searchResultStart == 0 && searchResultEnd != -1 && s.charAt(s.length - 1) == ']');
		if (cut) {
			// cut off the first '['
			s = s.substring(1,s.length);
			// cut off the last ']'
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
				setLink(uri, title);
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
		linkInformation["title"] = title;
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
		var fieldId = top.gallery_fs.gallery_head.document.forms["main"].<%= wp.PARAM_FIELDID %>.value;
		top.window.opener.document.getElementById(fieldId).value  = uri;
		try {
			// toggle preview icon if possible
			top.window.opener.checkPreview(fieldId);
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
		var fieldId = top.gallery_fs.gallery_head.document.forms["main"].<%= wp.PARAM_FIELDID %>.value;
		top.window.opener.document.getElementById(fieldId).value  = uri;
		try {
			// toggle preview icon if possible
			top.window.opener.checkPreview(fieldId);
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
	function pasteContent(newContent) {
	<% 	
		if (wp.MODE_WIDGET.equals(wp.getParamDialogMode())) {
	%>
		var fieldId = top.gallery_fs.gallery_head.document.forms["main"].<%= wp.PARAM_FIELDID %>.value;
		top.window.opener.document.getElementById(fieldId).value  = newContent;
	<% 	
                if (wp instanceof CmsTableGallery) {
	%>
			top.window.opener.checkTableContent(fieldId);        
	<%
		} else if (wp instanceof CmsHtmlGallery) {
	%>
			top.window.opener.checkHtmlContent(fieldId);
	<%	
		} } else { 
	%>		
		top.window.opener.insertHtml(top.preview_fs.gallery_preview.document.getElementById("icontent").innerHTML);		
	<%
		}
	%>
		top.window.close();
	}
	
	function deleteResource(uri) {
		top.gallery_fs.gallery_head.action = "deleteResource";
		top.gallery_fs.gallery_list.location.href="<%=wp.getJsp().link(wp.PATH_DIALOGS+"delete.jsp")%>?resource="+uri;				
	}
	
	function publishResource(uri) {
		top.gallery_fs.gallery_list.location.href="<%=wp.getJsp().link(wp.PATH_DIALOGS+"publishresource.jsp")%>?resource="+uri;				
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