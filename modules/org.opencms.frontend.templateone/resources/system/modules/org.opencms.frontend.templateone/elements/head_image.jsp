<%@page buffer="none" session="false" import="org.opencms.frontend.templateone.*,org.opencms.jsp.*" %><%

// initialize action element to access the API
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

// get show element parameters from request
boolean showHeadImage = Boolean.valueOf(request.getParameter("showheadimage")).booleanValue();

if (showHeadImage) {

	// calculate image link
	String imageLink = request.getParameter("imagelink");
	boolean showLink = !"".equals(imageLink) && !"none".equals(imageLink);

	if (showLink && imageLink.startsWith("/")) {
		// calculate absolute path (internal link)
		imageLink = cms.link(imageLink);
	}

	%><div class="imagehead"><%
	if (showLink) {
		%><a href="<%= imageLink %>"><span class="imagelink"></span></a><%
	}
	%></div><%

}

String headElemUri = request.getParameter("headelemuri");
if (!CmsTemplateBean.PROPERTY_VALUE_NONE.equals(headElemUri)) {

	%><div class="edithead"><% cms.includeSilent(headElemUri, "text1", true); %></div><%

}

%>