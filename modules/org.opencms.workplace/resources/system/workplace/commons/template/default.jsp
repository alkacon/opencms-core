<%@page buffer="none" session="false" import="org.opencms.main.*,
	org.opencms.file.*,
	org.opencms.jsp.*,
	org.opencms.jsp.util.*,
	org.opencms.ade.galleries.*,
	org.opencms.file.types.CmsResourceTypeXmlContainerPage" %><%@ 
	taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%@ 
	taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ 
	taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@
	taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><%

	CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
	CmsObject cmsObj = cms.getCmsObject();
	CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(request);
	pageContext.setAttribute("standardContext", standardContext);
	pageContext.setAttribute("navlist", cms.getNavigation().getNavigationForFolder());
	pageContext.setAttribute("isContainerPage", CmsResourceTypeXmlContainerPage.isContainerPage(cmsObj.readResource(cms.getRequestContext().getUri())));
%>
<cms:template element="head">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title><cms:property name="Title" file="search" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" />" />
	<meta name="keywords" content="<cms:property name="Keywords" file="search" />" />
	<meta http-equiv="Content-Type" content="text/html; charset=<cms:property name="content-encoding" file="search" default="<%= OpenCms.getSystemInfo().getDefaultEncoding() %>"/>" />
	<meta name="robots" content="index, follow" />
	<meta name="revisit-after" content="7 days" />

	<link href="<cms:link>../style/template.css</cms:link>" rel="stylesheet" type="text/css"/>

	<cms:editable provider="org.opencms.workplace.editors.directedit.CmsDirectEditJQueryProvider"/>
</head>


<fmt:setBundle basename="org.opencms.workplace.messages" var="messages" scope="request" />
<c:set var="warningMessage"><fmt:message key="ERR_DEFAULT_TEMPLATE_WARNING_0" bundle="${messages}"/></c:set>
<c:set var="preview" value="${standardContext.previewFormatter[cms:vfs(pageContext).context.uri]}" />
<c:choose>
	<c:when test="${!empty preview}">
		<c:set var="warningMessage"><fmt:message key="ERR_DEFAULT_TEMPLATE_ADE_WARNING_0" bundle="${messages}" /></c:set>
	</c:when>
</c:choose>

<body>
	<div id="window">
		<div id="page">	  		
			<!-- begin: header -->
			<div id="header">
				<div id="topnav">
					<a href="#content" class="skip">Skip to Main Content</a>
				</div>
				<div class="left">
					<h1><cms:property name="Title"/></h1>
					<cms:property name="Description"/>
				</div>
				<div class="right">
					<a href="http://www.opencms.org"><img src="<cms:link>../../resources/commons/login_logo.png</cms:link>" alt="OpenCms Logo"></a>
				</div>
				<div class="clear">&nbsp;</div>
			</div>
			<!-- end: header -->
			
			<!-- begin: content area #main -->
			<div id="main">
			
				<!-- begin: left column -->
				<div id="col1">
					<div id="col1_content" class="clearfix">
						<div id="nav_left">
							<ul>
								<c:forEach items="${navlist}" var="elem">
									<li><a href="<cms:link>${elem.resourceName}</cms:link>">${elem.navText}</a></li>          			
								</c:forEach>
							</ul>
						</div>
					</div>
				</div>
				<!-- end: left column -->
			
				<!-- begin: right column -->
				<div id="col3">
					<div id="col3_content" class="clearfix">
					</div>
				</div>
				<!-- end: right column -->
			
				<!-- begin: center column -->
				<div id="col2">
					<div id="col2_content" class="clearfix">
						<!-- anchor for accessible link to main content -->
						<a id="content" name="content"></a>
						<div id="warning">${warningMessage}</div>
</cms:template>
<cms:template element="body">
	<c:catch>
		<c:set var="xml" value="${cms:vfs(pageContext).readXml[cms:vfs(pageContext).context.uri]}" />
		<c:set var="preview" value="${standardContext.previewFormatter[cms:vfs(pageContext).context.uri]}" />
	</c:catch>
	<c:choose>
		<c:when test="${!empty preview}">
			<%=CmsPreviewService.getPreviewContent(request, response, cmsObj, cmsObj.readResource(cms.getRequestContext().getUri()),cms.getRequestContext().getLocale())%>
		</c:when>
		<c:when test="${!empty xml && !isContainerPage}">
			<c:forEach items="${xml.names}" var="element">
				<cms:template ifexists="${element}">
					<cms:include element="${element}" editable="true"/>
				</cms:template>
			</c:forEach>
		</c:when>
	</c:choose>
</cms:template>
<cms:template element="foot">
          </div>
					<div class="clear">&nbsp;</div>
				</div>
				<!-- end: center column -->
			</div>
			<!-- end: content area -->
		
			<!-- begin: #footer -->
			<div id="footer">
				Built with <a href="http://www.opencms.org/">OpenCms - The Open Source Cms</a>, provided by <a href="http://www.alkacon.com" target="_self" title="Alkacon Software GmbH">Alkacon Software GmbH &amp; Co. KG - The OpenCms Experts</a>
			</div>
			<!-- end: #footer -->
		
		</div>
		<!-- end: #page -->
	</div>
	<!-- end: #window -->
</body>
</html>
</cms:template>
