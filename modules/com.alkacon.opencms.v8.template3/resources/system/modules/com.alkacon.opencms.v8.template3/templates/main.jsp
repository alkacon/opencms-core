<%@page buffer="none" session="false" taglibs="c,cms,fn" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html><head>
	<title><cms:info property="opencms.title" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" default="" />">
	<meta name="keywords" content="<cms:property name="Keywords" file="search" default="" />">
	<meta http-equiv="Content-Type" content="text/html; charset=${cms:vfs(pageContext).requestContext.encoding}">
	<meta name="robots" content="index, follow">
	<meta name="revisit-after" content="7 days">
	
	<cms:enable-ade/>
	
	<c:set var="cols"><cms:property name="style.columns" file="search" default="3" /></c:set>

	<!-- insert stylesheets needed for the template -->
	<link href="<cms:link>%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/resources/css/form.css:3b612cb3-6b29-11e0-bc98-e363b206b5de)</cms:link>" rel="stylesheet" type="text/css"></link>
	<link href="<cms:link>%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/resources/css/nav_left.css:3b6858a5-6b29-11e0-bc98-e363b206b5de)</cms:link>" rel="stylesheet" type="text/css"></link>
	<link href="<cms:link>%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/resources/menus/style2/style.css:617d2fd5-6b29-11e0-bc98-e363b206b5de)</cms:link>" rel="stylesheet" type="text/css"></link>
	<link href="<cms:link>%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/resources/css/style.css:24fe3f11-6b2c-11e0-bc98-e363b206b5de)?cols=${cols}&amp;style=<cms:property name="style.layout" file="search" /></cms:link>" rel="stylesheet" type="text/css"></link>
		
	<!--[if lte IE 6]>
		<link href="<cms:link>%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/resources/css/patch_ie.css:3b6f5d87-6b29-11e0-bc98-e363b206b5de)</cms:link>" rel="stylesheet" type="text/css"></link>
	<![endif]-->
    	
	<cms:headincludes type="javascript" />
	<cms:headincludes type="css" /> 
		
</head><body>
	<div id="window">
		<div id="page">
	  		
		<!-- begin: header -->
		<div id="header">
		    	<div id="topnav">
		    		<a href="#content" class="skip">Skip to Main Content</a>		    		
		    	</div>		    	
		    	<cms:container name="headercontainer" type="header" maxElements="1" />    	
		</div>
		<!-- end: header -->
		
		<!-- begin: main navigation -->
		<cms:include file="%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/elements/menu/nav_main.jsp:991d83b5-6b62-11e0-92d8-e363b206b5de)" />
		<!-- end: main navigation -->
			
		<!-- begin: breadcrumb -->
		<div id="breadcrumb">
		<cms:include file="%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/elements/menu/nav_breadcrumb.jsp:358b431b-6b66-11e0-92d8-e363b206b5de)" />					   
		</div>
		<!-- end: breadcrumb -->

		<!-- begin: content area #main -->
		<div id="main">
			
	  		<!-- begin: left column -->
		  	<div id="col1">
				<div id="col1_content" class="clearfix">	
			  	<!-- include the left navigation menu -->
				<cms:include file="%(link.weak:/system/modules/com.alkacon.opencms.v8.template3/elements/menu/nav_side.jsp:715f894d-6b35-11e0-bc98-e363b206b5de)" />
		  		<!-- include the boxes on the left side -->
	        		<cms:container name="leftcontainer" type="left" width="230" maxElements="8" />
				</div>
	  		</div>
	  		<!-- end: left column -->

	  		<!-- begin: right column -->
	  		<div id="col3">
	  			<div id="col3_content" class="clearfix">
			          <!-- include the boxes on the right side -->
			          <cms:container name="rightcontainer" type="right" width="230" maxElements="8" />
		        	</div>
	      		</div>
	      		<!-- end: right column -->

	  		<!-- begin: center column -->
	  		<div id="col2">
		  		<div id="col2_content" class="clearfix">
					<!-- anchor for accessible link to main content -->
					<a id="content" name="content"></a>
					<cms:container name="centercontainer" type="center" width="450" maxElements="8" detailview="true"/>
				</div>
	  			<div class="clear">&nbsp;</div> 
			</div>
			<!-- end: center column -->
		</div>
		<!-- end: content area -->
		
		<!-- begin: #footer -->
		<div id="footer">
			<cms:container name="footercontainer" type="footer" maxElements="1" />
	  	</div>
		<!-- end: #footer -->
	</div>
	<!-- end: #page -->
</div>
</body></html>