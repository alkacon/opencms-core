<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %><%	

	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryImages wp = new CmsGalleryImages(pageContext, request, response);
	
%><%= wp.htmlStart(null) %>

<script type="text/javascript">
<!--
	function displayGallery() {
		var mainForm = document.forms["main"];
		mainForm.submit();
	}
	
	// displays initial gallery item list in list frame (needed when popup is loaded the first time)
	function displayFirstGallery() {
		var listForm = document.forms["list"];
		if (listForm.elements["<%= wp.PARAM_GALLERYPATH %>"].value == "") {
			var mainForm = document.forms["main"];
			listForm.elements["<%= wp.PARAM_GALLERYPATH %>"].value = mainForm.elements["<%= wp.PARAM_GALLERYPATH %>"].value;
			//listForm.elements["<%= wp.PARAM_PAGE %>"].value = mainForm.elements["<%= wp.PARAM_PAGE %>"].value;
			//listForm.elements["<%= wp.PARAM_SEARCHWORD %>"].value = mainForm.elements["<%= wp.PARAM_SEARCHWORD %>"].value;
			listForm.submit();
		}
	}
//-->
</script>

</head>

<body style="background-color: Menu; margin: 0; padding: 2px;">

<form name="main" action="<%= cms.link("img_fs_sub.jsp") %>" target="gallery_fs" method="post" class="nomargin">
<input type="hidden" name="<%= wp.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
<input type="hidden" name="<%= wp.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">

<table border="0" cellpadding="1" cellspacing="0" width="100%">
<tr>
	<td colspan="2"><%= wp.key("label.imagelist") %></td>
</tr>
<tr>
	<td colspan="2"><%= wp.buildSelectGallery("imagegallery") %></td>
</tr>
<%--
<tr>
	<td>
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td><input type="text" name="<%= wp.PARAM_SEARCHWORD %>" id="<%= wp.PARAM_SEARCHWORD %>" value="<%= wp.getParamSearchWord() %>"></td>
			<%= wp.buttonBarSpacer(5) %>
			<%= wp.button("javascript:displayGallery();", null, "search", "input.search", 0) %>
		</tr>
		</table>
	</td>
	<td align="right">
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<%= wp.button("javascript:previousPage();", null, "undo", "input.prev", 0) %>
			<%= wp.button("javascript:nextPage();", null, "redo", "input.next", 0) %>
			<%= wp.buttonBarSpacer(5) %>
			<%= wp.button("javascript:upload();", null, "wizard", "input.upload", 0) %>
		</tr>
		</table>
	</td>
</tr>
--%>
</table>

</form>

<form name="list" action="<%= cms.link("img_list.jsp") %>" method="post" class="nomargin" target="gallery_list">
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