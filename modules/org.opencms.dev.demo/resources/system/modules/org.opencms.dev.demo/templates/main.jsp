<%@page buffer="none" session="false" taglibs="cms" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html><head>
  <!-- Use cms:info tag to read the title -->
  <title><cms:info property="opencms.title" /></title>
	<meta name="description" content="<cms:property name="Description" file="search" default="" />">
	<meta name="keywords" content="<cms:property name="Keywords" file="search" default="" />">
	<meta http-equiv="Content-Type" content="text/html; charset=<cms:property name="Keywords" file="search" default="" />">
	<meta name="robots" content="index, follow">
	<meta name="revisit-after" content="7 days">
	
	<cms:enable-ade/>

	<!-- insert stylesheets needed for the template -->
	<link href="<cms:link>/system/modules/org.opencms.dev.demo/resources/css/form.css</cms:link>" rel="stylesheet" type="text/css">
	<link href="<cms:link>/system/modules/org.opencms.dev.demo/resources/css/nav_left.css</cms:link>" rel="stylesheet" type="text/css">
	<link href="<cms:link>/system/modules/org.opencms.dev.demo/resources/menus/style2/style.css</cms:link>" rel="stylesheet" type="text/css">
	<link href="<cms:link>/system/modules/org.opencms.dev.demo/resources/css/style_greyblue.css</cms:link>" rel="stylesheet" type="text/css">

		
	<!--[if lte IE 6]>
		<link href="<cms:link>%(link.weak:/system/modules/org.opencms.dev.demo/resources/css/patch_ie.css:d4d0e2ef-673d-11e0-8b6f-0760e8fc984c)</cms:link>" rel="stylesheet" type="text/css">
    	<![endif]-->
    	
    	<cms:headincludes type="javascript" />
	<cms:headincludes type="css" /> 
		
</head><body>
	<div id="window">
		<div id="page">
	  		
			<!-- begin: header -->
	    		<cms:include file="%(link.weak:/system/modules/org.opencms.dev.demo/elements/site-elements/header.jsp:54b40c81-764d-11e0-a23b-8fedaf1e6b74)" />
	    		<!-- end: header -->
		
	    		<!-- begin: main navigation -->										
	    		<cms:include file="%(link.weak:/system/modules/org.opencms.dev.demo/elements/site-elements/nav_main.jsp:195490ec-763d-11e0-a23b-8fedaf1e6b74)" />
	    		<!-- end: main navigation -->

									
			<!-- begin: content area #main -->
			<div id="main">
			<!-- begin: left column -->
		  	<div id="col1">
				<div id="col1_content" class="clearfix">	
			  		<!-- include the left navigation menu -->
					<cms:include file="%(link.weak:/system/modules/org.opencms.dev.demo/elements/site-elements/nav_side.jsp:2024e7ea-7647-11e0-a23b-8fedaf1e6b74)" />
		  			<!-- define container on the left side -->
	        			<cms:container name="leftcolumn" type="side" width="230" maxElements="5" />
				</div>
	  		</div>
	  		<!-- end: left column -->  
	  					

			<!-- begin: right column -->
	  		<div id="col3">
	  			<div id="col3_content" class="clearfix">
			          <!-- define container on the right side -->
			          <cms:container name="rightcolumn" type="side" width="230" maxElements="8" />
		        	</div>
	      		</div>
	      		<!-- end: right column -->

	  		<!-- begin: center column -->
	  		<div id="col2">
		  		<div id="col2_content" class="clearfix">
					<!-- define container in the center -->
					<cms:container name="centercolumn" type="center" width="450" maxElements="6" detailview="true"/>
				</div>
	  			<div class="clear">&nbsp;</div> 
			</div>
			<!-- end: center column -->
	  	</div>
		<!-- end: content area -->
		
		<!-- begin: #footer -->		    		
		<cms:include file="%(link.weak:/system/modules/org.opencms.dev.demo/elements/site-elements/footer.jsp:31bf1d94-764e-11e0-a23b-8fedaf1e6b74)" />
		<!-- end: #footer -->
	</div>
	<!-- end: #page -->
</div>
</body></html>