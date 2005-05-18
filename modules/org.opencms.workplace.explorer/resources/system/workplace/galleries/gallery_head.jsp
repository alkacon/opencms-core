<%@ page import="org.opencms.jsp.*, 
					  org.opencms.workplace.CmsWorkplaceManager,
					  org.opencms.util.CmsStringUtil,
					  org.opencms.workplace.galleries.*" buffer="none" session="false" %>

<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	
	// get gallery instance
	A_CmsGallery wp = A_CmsGallery.createInstance(cms);
	
%><%= wp.htmlStart(null) %>

<script type="text/javascript">
<!--
	var previewUri = null;
	var action;
	function displayGallery() {
		var mainForm = document.forms["main"];
		<% if (wp.galleriesExists()) { %>
			if (mainForm.gallerypath.value != '<%= wp.getParamGalleryPath() %>') {
				if (mainForm.<%= wp.PARAM_PAGE %> != null) {
					mainForm.<%= wp.PARAM_PAGE %>.options[0].selected = true;
				}
			}
			mainForm.submit();			
		<% } else { %>
			alert("<%=wp.getNoGalleryErrorMsg()%>");
			top.window.close();			
		<% } %>
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
	
	function wizard() {
		var currentfolder = document.main.gallerypath.value;
		top.gallery_fs.gallery_list.location.href="<%= wp.getWizardUrl()%>"+currentfolder;		
	}
//-->
</script>

</head>

<body onload="<%=wp.getBodyOnload()%>" style="background-color: ThreeDFace; margin: 0; padding: 2px;">

<form name="main" action="<%= cms.link("gallery_fs_head.jsp") %>" target="gallery_fs" method="post" class="nomargin">
<input type="hidden" name="<%= wp.PARAM_DIALOGMODE %>" value="<%= wp.getParamDialogMode() %>">
<input type="hidden" name="<%= wp.PARAM_FIELDID %>" value="<%= wp.getParamFieldId() %>">
<table border="0" cellpadding="1" cellspacing="0" width="100%">
<tr>
	<td colspan="2"><%= wp.buildGallerySelectBox() %></td>
</tr>
<tr>
	<td class="maxwidth">
		<table class="maxwidth" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<%= wp.wizardButton() %>
			<%= wp.buttonBarSpacer(5) %>
			<td class="maxwidth"><input type="text" style="width: 98%" name="<%= wp.PARAM_SEARCHWORD %>" id="<%= wp.PARAM_SEARCHWORD %>" value="<%= wp.getParamSearchWord() %>"></td>			
			<%= wp.searchButton() %>			
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

<form name="list" action="<%= cms.link("gallery_list.jsp") %>" method="post" class="nomargin" target="gallery_list">
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