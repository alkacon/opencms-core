<%@page buffer="none" session="false" import="org.opencms.frontend.template3.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%

	// The main template JSP of template3. 
	// This template is highly configurable in style and layout. The configuration
	// is done in several configuration files of the types "t3style" and "t3options". 
	// Those configuration files are managed by the
	// class CmsTemplateLayout.
	//
	// For details on the CmsTemplateLayout class, see the source code which can
	// be found at the following VFS location:
	// /system/modules/org.opencms.frontend.template3/java_src/CmsTemplateLayout.java

	CmsTemplateLayout cms = new CmsTemplateLayout(pageContext, request, response);
	pageContext.setAttribute("cms", cms);
%>
<cms:template element="head">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title><cms:property name="Title" file="search" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" />" >
	<meta name="keywords" content="<cms:property name="Keywords" file="search" />" >
	<meta http-equiv="Content-Type" content="text/html; charset=${cms.requestContext.encoding}" >
	<meta name="robots" content="index, follow" >
	<meta name="revisit-after" content="7 days" >

	<!-- insert stylesheets needed for the template -->
	<c:forEach items="${cms.stylesheets}" var="cssFile">
		<link href="<cms:link>${cssFile}</cms:link>" rel="stylesheet" type="text/css">		
	</c:forEach>
		
	<link href="<cms:link>../resources/css/style.css?style=${cms.stylePath}</cms:link>" rel="stylesheet" type="text/css">
	<!--[if lte IE 6]>
		<link href="<cms:link>%(link.weak:/system/modules/org.opencms.frontend.template3/resources/css/patch_ie.css:c3b389e1-9c7a-11de-adde-dd9f629b113b)</cms:link>" rel="stylesheet" type="text/css">
    	<![endif]-->

	<!-- insert individual stylesheet -->
	<c:set var="stylesheet"><cms:property name="stylesheet" file="search"/></c:set>
	<c:if test="${!empty stylesheet}">
		<link href="<cms:link>${stylesheet}</cms:link>" rel="stylesheet" type="text/css">
	</c:if>

	<cms:editable/>
</head>

<body>
	<div id="window">
		<div id="page">
	  		
			<!-- begin: header -->
		    <div id="header">
		    	<div id="topnav">
		    		<a href="#content" class="skip">Skip to Main Content</a>
		    		<c:if test="${!cms.options.value['TopNav'].isEmptyOrWhitespaceOnly}">
		    			<c:out value="${cms.options.value['TopNav']}" escapeXml="false" />
		    		</c:if>
		    	</div>
		    	<c:if test="${!cms.options.value['Header'].isEmptyOrWhitespaceOnly}">
		    		<c:out value="${cms.options.value['Header'].resolveMacros}" escapeXml="false" />
		    	</c:if>
			</div>
			<!-- end: header -->
			
			<!-- begin: main navigation -->
			<c:choose>
				<c:when test="${!empty cms.styleValue['nav.main']}">
					<cms:include file="${cms.styleValue['nav.main']}"/>
				</c:when>
				<c:otherwise>
					<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.template3/elements/menu/nav_style2.jsp:c3377be8-9c7a-11de-adde-dd9f629b113b)"/>
				</c:otherwise>
			</c:choose>
			<!-- end: main navigation -->
			
			<!-- begin: breadcrumb -->
			<div id="breadcrumb">
				<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.template3/elements/breadcrumb.jsp:c3032562-9c7a-11de-adde-dd9f629b113b)" />
			</div>
			<!-- end: breadcrumb -->
			
			<!-- begin: content area #main -->
			<div id="main">
			
			<!-- begin: left column -->
			<div id="col1">
				<div id="col1_content" class="clearfix">
					<!-- include the left navigation menu -->
					<cms:include file="%(link.weak:/system/modules/org.opencms.frontend.template3/elements/menu/nav_left.jsp:c318822c-9c7a-11de-adde-dd9f629b113b)" />
						
					<!-- include the boxes on the left side -->
					<div id="leftcnt">

					</div>
				</div>
			</div>
			<!-- end: left column -->
			
			<!-- begin: right column -->
			<div id="col3">
				<div id="col3_content" class="clearfix">
					<c:catch>
						<c:set var="page" value="${cms:vfs(pageContext).readXml[cms:vfs(pageContext).context.uri]}" />
					</c:catch>
					
					<div id="rightcnt">

					</div>
               			</div>
            		</div>
            		<!-- end: right column -->
			
			<!-- begin: center column -->
			<div id="col2">
				<div id="col2_content" class="clearfix">
					<!-- anchor for accessible link to main content -->
					<a id="content" name="content"></a>
</cms:template>
<cms:template element="body">
	<cms:include element="body" editable="true"/>
</cms:template>
<cms:template element="foot">

					<!-- include the second body from an xml page, if available -->
					<c:if test="${page.hasValue['body2']}">
						<cms:include element="body2" editable="true"/>
					</c:if>
					<!-- include the boxes in the center -->
					<div id="centercnt">

					</div>
				</div>
				<div class="clear">&nbsp;</div>
			</div>
			<!-- end: center column -->
		</div>
		<!-- end: content area -->
		
		<!-- begin: #footer -->
		<div id="footer">
			<c:if test="${!cms.options.value['Footer'].isEmptyOrWhitespaceOnly}">
		    	<c:out value="${cms.options.value['Footer'].resolveMacros}" escapeXml="false" />
		    </c:if>
		</div>
		<!-- end: #footer -->
		
	</div>
	<!-- end: #page -->
</div>
</body>
</html>
</cms:template>
