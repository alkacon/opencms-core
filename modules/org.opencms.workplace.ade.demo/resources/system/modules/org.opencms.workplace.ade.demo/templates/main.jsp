<%@page buffer="none" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title><cms:property name="Title" file="search" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" />" >
	<meta name="keywords" content="<cms:property name="Keywords" file="search" />" >
	<meta http-equiv="Content-Type" content="text/html; charset=${cms.requestContext.encoding}" >
	<meta name="robots" content="index, follow" >
	<meta name="revisit-after" content="7 days" >

	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/menus/style2/style.css</cms:link>" rel="stylesheet" type="text/css"> 
	
	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/css/nav_left.css</cms:link>" rel="stylesheet" type="text/css">		
		
	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/css/style.css</cms:link>" rel="stylesheet" type="text/css">

	<link href="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/css/patch_ie.css</cms:link>" rel="stylesheet" type="text/css">

	<cms:enable-ade/>
</head>

<body>
	<div id="window">
		<div id="page">
	  		
			<!-- begin: header -->
		    <div id="header">
		    	<div id="topnav">
		    		<a href="#content" class="skip">Skip to Main Content</a>
		    	</div>
<div style="padding: 10px 10px 5px; height: 55px;">
<div style="margin: 0pt; padding: 0pt; float: left;"><a href="http://www.alkacon.com"><img id="iimg_369039394" src="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/images/logo_alkacon_png24.png</cms:link>" title="Go to the Alkacon Software website" alt="Alkacon Software" /></a></div>
<div style="margin: 1pt; padding: 0pt; float: right;"><a href="http://www.opencms.org"><img id="iimg_364996162" src="<cms:link>/system/modules/org.opencms.workplace.ade.demo/resources/images/logo_opencms_png24.png</cms:link>" title="Go to the OpenCms website" alt="Logo OpenCms" /></a></div>
<div style="margin: 0px 210px 0pt 160px;">
<p style="margin: 1pt; float: right;"><h3 style="margin: 0pt;">OpenCms 7.5 Demo Website</h3>
<p style="margin: 1pt;"><i>ADE Demo</i></p>
</div>
</div>
			</div>
			<!-- end: header -->
			
			<!-- begin: main navigation -->
			<cms:include file="%(link.weak:/system/modules/org.opencms.workplace.ade.demo/elements/nav_style2.jsp:4d795002-961c-11de-9854-dd9f629b113b)"/>
			<!-- end: main navigation -->
			
			<!-- begin: breadcrumb -->
			<div id="breadcrumb">
				<cms:include file="%(link.weak:/system/modules/org.opencms.workplace.ade.demo/elements/breadcrumb.jsp:43ce7d4d-961c-11de-9854-dd9f629b113b)" />
			</div>
			<!-- end: breadcrumb -->
			
			<!-- begin: content area #main -->
			<div id="main">
			
			<!-- begin: left column -->
			<div id="col1">
				<div id="col1_content" class="clearfix">
						<!-- include the left navigation menu -->
							<cms:include file="%(link.weak:/system/modules/org.opencms.workplace.ade.demo/elements/nav_left.jsp:dbd121cd-961c-11de-9854-dd9f629b113b)" />
						
						<div id="left">
						<!-- include the boxes on the left side -->
						<cms:container name="left" type="leftColumn" maxElements="3" />
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
					
					<!-- include the boxes on the right side -->
						<div id="right">
						<!-- include the boxes on the right side -->
						<cms:container name="right" type="rightColumn" maxElements="3" />
						</div>
               </div>
            </div>
            <!-- end: right column -->
			
			<!-- begin: center column -->
			<div id="col2">
				<div id="col2_content" class="clearfix">
					<!-- anchor for accessible link to main content -->
					<a id="content" name="content"></a>
						<div id="maincnt">
					<cms:container name="maincnt" type="mainColumn" maxElements="2" />
						</div>
				</div>
				<div class="clear">&nbsp;</div>
			</div>
			<!-- end: center column -->
			<br clear="all" />
			<!-- begin: bottom column -->
			<div id="bottom_cont">
				<cms:container name="bottom_cont" type="bottomColumn" maxElements="3" />
			</div>
			<!-- end: bottom column -->
		</div>
		<!-- end: content area -->
		
		<!-- begin: #footer -->
		<div id="footer">
			<div style="padding: 2px 0px 0px; height: 14px; text-align: center;">Build with <a href="http://www.opencms.org/" title="Go to the OpenCms website">OpenCms - The Open Source CMS</a>, provided by <a title="Go to the Alkacon Software website" href="http://www.alkacon.com">Alkacon Software - The OpenCms Experts</a></div>
		</div>
		<!-- end: #footer -->
		
	</div>
	<!-- end: #page -->
</div>
</body>
</html>
