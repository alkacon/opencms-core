<%@ page import="org.opencms.jsp.*, org.opencms.workplace.commons.*" buffer="none" session="false" %>
<%	
	// initialize action element for link substitution
	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	// initialize the workplace class
	CmsGalleryHtmls wp = new CmsGalleryHtmls(pageContext, request, response);	
%>
<%= wp.htmlStart(null) %>
		
</head>
<body class="dialog" height="100%" unselectable="on">
<div width: 100%; margin-top: 5px">
<%= wp.buildGalleryItemPreview() %>
</div>
</body>
<%= wp.htmlEnd() %>