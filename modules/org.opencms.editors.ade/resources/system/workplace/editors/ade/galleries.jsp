<%@ page import="org.opencms.workplace.galleries.*" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %><%
String adeResourcePath = org.opencms.workplace.CmsWorkplace.getSkinUri() + "editors/ade/";
%>
<jsp:useBean id="gallery" class="org.opencms.workplace.galleries.CmsGallerySearchServer">
 <% gallery.init(pageContext, request, response); %>
 </jsp:useBean><%
 	if (request.getParameter(CmsGallerySearchServer.ReqParam.ACTION.getName())!= null){
 	    gallery.serve();
 	}else{ 
// this is the initial call to the gallery so build the html first
%><!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script type="text/javascript" src="<%= adeResourcePath %>lib/jquery-1.3.2.js"></script>
<script type="text/javascript" src="<%= adeResourcePath %>lib/jquery-ui-1.8a1.js"></script>
<script type="text/javascript" src="<%= adeResourcePath %>lib/json2.js"></script>
<script type="text/javascript" src="<%= adeResourcePath %>lib/jquery.pagination.js"></script>
<script type="text/javascript">
var cms = { data: { GALLERY_SERVER_URL: "${gallery.galleryUri}"}, html: {}, util: {} , previewhandler:{}, imagepreviewhandler: {}, galleries: {}, messages: {} };
</script>
<script type="text/javascript" src="<cms:link>/system/workplace/editors/ade/cms.messages.jsp</cms:link>"></script>
<script type="text/javascript" src="<%= adeResourcePath %>js/cms.html.js"></script>
<script type="text/javascript" src="<%= adeResourcePath %>js/cms.util.js"></script>
<script type="text/javascript" src="<%=  adeResourcePath %>js/cms.selectbox.js"></script>
<script type="text/javascript" src="<%= adeResourcePath %>js/cms.galleries.js"></script>
<script type="text/javascript" src="<%= adeResourcePath %>js/cms.previewhandler.js"></script>
<%=gallery.getAdditionalJavaScript() %>
<script type="text/javascript">  

(function($){	 	

 	// read and handle the request params
    var tabs = ${gallery.listConfig};	
    var requestData = ${gallery.initialSearch};
    $.extend(cms.galleries.initValues, ${gallery.additionalImageParams});
    cms.galleries.initValues['dialogMode'] = "${(not empty param.dialogmode) ? param.dialogmode : 'null'}";
    cms.galleries.initValues['fieldId'] = "${(not empty param.fieldid) ? param.fieldid : 'null'}";
    cms.galleries.initValues['path'] = "${(not empty param.path) ? param.path : 'null'}";
    if (cms.galleries.initValues['dialogMode'] == 'editor') {
		loadSelection();
		//post resquest synchron
		$.ajax({
			'async': false,
            'url': cms.data.GALLERY_SERVER_URL,
            'data': {
               'action': 'vfspath',
               'data': JSON.stringify({
                  'linkpath': cms.galleries.initValues.linkpath
               })
            },
            'type': 'POST',
            'dataType': 'json',
            'success': function (data) {
					cms.galleries.initValues.path = data.path;
                }
         }); 
    }
    $(function() {                
        cms.galleries.initAddDialog(tabs, requestData);                                              
	});
})(jQuery);
</script> 
<title>Search demo</title>
<link rel="stylesheet" type="text/css" media="screen" href="<%= adeResourcePath %>css/custom-theme/jquery-ui-1.7.2.custom.css" />
<link rel="stylesheet" type="text/css" media="screen" href="<%= adeResourcePath %>css/advanced_direct_edit.css" />
<link rel="stylesheet" type="text/css" media="screen" href="<%= adeResourcePath %>css/galleries.css" />
<!--[if IE 7]>
<style>
div.cms-selectbox{
    zoom: 1;
    display: inline;
    }
</style>
<![endif]-->
	</head>
	<body style="margin:1px; padding:0px;">
	    <div id="cms-gallery-main"> 
		    <div id="cms-gallery-tabs">
                <ul>
                     <li><a href="#tabs-result">Search results</a></li>
                     <li><a href="#tabs-types">Type</a></li>
                     <li><a href="#tabs-galleries">Galleries</a></li>
                     <li><a href="#tabs-categories">Categories</a></li>
                     <li><a href="#tabs-fulltextsearch">Full Text Search</a></li>
                </ul>
            </div>            
        </div>
	</body>
</html>
<% } %>