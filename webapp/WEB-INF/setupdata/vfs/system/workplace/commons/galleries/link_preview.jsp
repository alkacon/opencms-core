<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryLinks wp = new CmsGalleryLinks(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
	<link rel="stylesheet" type="text/css" href="gallery.css">	
</head>
<body class="dialog" style="background-color: ThreeDFace;" height="100%" unselectable="on">
<div style="text-align: center; width: 100%; margin-top: 5px">
<%= wp.buildGalleryItemPreview() %>
</div>
</body>
<%= wp.htmlEnd() %>