<%@ page import="org.opencms.util.CmsStringUtil, org.opencms.workplace.galleries.*" %><%

CmsImageGalleryExtended wp = new CmsImageGalleryExtended(pageContext, request, response);

String galleryResourcePath = org.opencms.workplace.CmsWorkplace.getSkinUri() + "components/imagegallery/";
String jQueryResourcePath = org.opencms.workplace.CmsWorkplace.getSkinUri() + "jquery/";
String jsIntegratorQuery = "";

%><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>

<title><%= wp.key(Messages.GUI_TITLE_IMAGEGALLERY_0) %></title>

<link rel="stylesheet" type="text/css" href="<%= galleryResourcePath %>css/dialog.css" />
<link rel="stylesheet" type="text/css" href="<%= galleryResourcePath %>css/tabs.css" />
<link rel="stylesheet" type="text/css" href="<%= jQueryResourcePath %>css/thickbox/thickbox.css" />
<% if (wp.isModeEditor()) { %>
<link rel="stylesheet" type="text/css" href="<%= galleryResourcePath %>css/editor.css" />
<% } %>
<!--[if lte IE 7]>
  <link rel="stylesheet" type="text/css" href="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/imagegallery/css/ie.css" />
<![endif]-->

<script type="text/javascript" src="<%= jQueryResourcePath %>packed/jquery.js"></script>
<script type="text/javascript" src="<%= jQueryResourcePath %>packed/jquery.pagination.js"></script>
<script type="text/javascript" src="<%= jQueryResourcePath %>packed/thickbox.js"></script>
<script type="text/javascript" src="<%= jQueryResourcePath %>packed/jquery.ui.js"></script>

<script type="text/javascript" src="<%= wp.getJsp().link("js/localization.js?locale=" + wp.getLocale()) %>"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/jquery.jeditable.pack.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/jquery.jHelperTip.1.0.min.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/formatselections.js"></script>
<script type="text/javascript" src="<%= galleryResourcePath %>js/galleryfunctions.js"></script>

<script type="text/javascript">

var vfsPathAjaxJsp = "<%= wp.getJsp().link("/system/workplace/galleries/imagegallery/ajaxcalls.jsp") %>";
var vfsPathPrefixImages = "<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>components/imagegallery/img/";

var initValues;

<% wp.getJsp().includeSilent("js/integrator_" + wp.getMode() + ".js", null); %>

</script>

</head>
<body id="imgdialog">

<div id="tabs">
            <ul>
            	<% if (wp.isModeWidget() || wp.isModeEditor()) { %>
                <li><a href="#preview"><span><%= wp.key(Messages.GUI_IMAGEGALLERY_TAB_PREVIEW_0) %></span></a></li>
                <% } %>
                <li><a href="#galleries"><span><%= wp.key(Messages.GUI_IMAGEGALLERY_TAB_GALLERIES_0) %></span></a></li>
                <% if (wp.isModeWidget() || wp.isModeEditor()) { %>
                <li><a href="#categories"><span><%= wp.key(Messages.GUI_IMAGEGALLERY_TAB_CATEGORIES_0) %></span></a></li>
                <% } %>
                <% if (wp.isModeEditor()) { %>
                <li><a href="#advanced"><span><%= wp.key(Messages.GUI_IMAGEGALLERY_TAB_ADVANCED_0) %></span></a></li>
                <% } %>
            </ul>
	<%
	if (wp.isModeWidget()) { %>
		<%@ include file="%(link.strong:/system/workplace/galleries/imagegallery/html/tabs_widget.html:bb9bf84b-a42c-11dd-a77f-55b439b85a0e)" %><%
	} else if (wp.isModeEditor()) { %>
		<%@ include file="%(link.strong:/system/workplace/galleries/imagegallery/html/tabs_editor.html:453c7b4e-a430-11dd-a77f-55b439b85a0e)" %><%
	}
	%>

	<div id="galleries">
		<div id="galleryfolders"><div class="head"><%= wp.key(Messages.GUI_IMAGEGALLERY_GALLERIES_HL_AVAILABLE_0) %></div><div id="galleryfolderlist"></div></div>
		<div id="galleryimages">
			<div id="galleryimagebuttons">
				<button type="button" id="galleryuploadbutton" onclick="$('#galleryimageuploadlink').click();"><%= wp.key(Messages.GUI_IMAGEGALLERY_GALLERIES_BUTTON_UPLOAD_0) %></button><a href="#" class="thickbox" id="galleryimageuploadlink"></a>
				<button type="button" onclick="showGalleryFolders();"><%= wp.key(Messages.GUI_IMAGEGALLERY_GALLERIES_BUTTON_SHOW_0) %></button>
				<button type="button" id="gallerypublishbutton" onclick="$('#gallerypublishlink').click();" disabled="disabled"><%= wp.key(Messages.GUI_IMAGEGALLERY_GALLERIES_BUTTON_PUBLISH_0) %></button><a href="#" class="thickbox" id="gallerypublishlink"></a>
			</div>
			<div id="galleryimagelist"></div>
			<div id="galleryimageinfo">
				<table cellspacing="0" cellpadding="0" border="0">
					<tr>
						<td style="width: 59%;" colspan="2" class="imageinfoheadline">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_HEADLINE_0) %>
							<button id="galleryimageselectbutton" onclick="setActiveImage(galleryImages.markedImage, 'gallery');" type="button" title="<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_SELECT_0) %>">
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/apply.png" />
							</button>
							<button id="galleryimagepublishbutton" onclick="publishImage(galleryImages.markedImage, 'gallery');" type="button" title="<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_PUBLISH_0) %>">
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/publish.png" />
							</button>
						</td>
						<td style="width: 40%;" colspan="2" class="imageinfostate">
							<span id="galleryimagestate"></span>
						</td>
					</tr>
					<tr>
						<td style="width: 14%;" class="galleryimageinfotitle">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_TITLE_0) %>
						</td>
						<td style="width: 85%;" class="galleryimageinfotitle" colspan="3"><div id="galleryimagetitlewrapper"><div id="galleryimagetitle"></div></div></td>
					</tr>
					<tr>
						<td style="width: 14%;">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_NAME_0) %>
						</td>
						<td id="galleryimagename" style="width: 45%;"></td>
						<td style="width: 14%;">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_DATE_CREATED_0) %>
						</td>
						<td id="galleryimagedc" style="width: 26%;"></td>
					</tr>
					<tr>
						<td>
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_TYPE_0) %>
						</td>
						<td id="galleryimagetype">&nbsp;</td>
						<td>
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_DATE_MODIFIED_0) %>
						</td>
						<td id="galleryimagedm">&nbsp;</td>
					</tr>
				</table>
			</div>
		</div>
	</div>
	<%
	if (wp.isModeWidget() || wp.isModeEditor()) { %>
	<div id="categories">
		<div id="categoryfolders"><div class="head"><%= wp.key(Messages.GUI_IMAGEGALLERY_CATEGORIES_HL_AVAILABLE_0) %></div><div id="categoryfolderlist"></div></div>
		<div id="categoryimages">
			<div id="categoryimagebuttons">
				<button type="button" onclick="showCategoryFolders();"><%= wp.key(Messages.GUI_IMAGEGALLERY_CATEGORIES_BUTTON_SHOW_0) %></button>
			</div>
			<div id="categoryimagelist"></div>
			<div id="categoryimageinfo">
				<table cellspacing="0" cellpadding="0" border="0">
					<tr>
						<td style="width: 59%;" colspan="2" class="imageinfoheadline">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_HEADLINE_0) %>
							<button id="categoryimageselectbutton" onclick="setActiveImage(categoryImages.markedImage, 'category');" type="button" title="<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_SELECT_0) %>">
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/apply.png" />
							</button>
							<button id="categoryimagepublishbutton" onclick="publishImage(categoryImages.markedImage, 'category');" type="button" title="<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_PUBLISH_0) %>">
								<img src="<%= org.opencms.workplace.CmsWorkplace.getSkinUri() %>buttons/publish.png" />
							</button>
						</td>
						<td style="width: 40%;" colspan="2" class="imageinfostate">
							<span id="categoryimagestate"></span>
						</td>
					</tr>
					<tr>
						<td style="width: 14%;" class="categoryimageinfotitle">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_TITLE_0) %>
						</td>
						<td style="width: 85%;" class="categoryimageinfotitle" colspan="3"><div id="categoryimagetitle"></div></td>
					</tr>
					<tr>
						<td style="width: 14%;">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_NAME_0) %>
						</td>
						<td style="width: 45%;" id="categoryimagename"></td>
						<td style="width: 14%;">
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_DATE_CREATED_0) %>
						</td>
						<td style="width: 26%;" id="categoryimagedc"></td>
					</tr>
					<tr>
						<td>
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_TYPE_0) %>
						</td>
						<td id="categoryimagetype">&nbsp;</td>
						<td>
							<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_DATE_MODIFIED_0) %>
						</td>
						<td id="categoryimagedm">&nbsp;</td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</div>
	<%
	}
	%>
	<div id="closebutton">
		<button type="button" onclick="window.close();"><%= wp.key(Messages.GUI_IMAGEGALLERY_BUTTON_CLOSE_0) %></button>
	</div>
	<a href="#" class="thickbox" id="resourcepublishlink"></a>
</body>

</html>