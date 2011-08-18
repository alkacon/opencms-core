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
		
	<link href="<cms:link>/system/modules/org.opencms.dev.demo/resources/css/formatter_demo.css</cms:link>" rel="stylesheet" type="text/css">	
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
	    			<div>
	    				<h2>Container page demo with different formatters.</h2>
	    				<br/>
	    				<p> Use the Dev Demo Article for this demo. The xsd file for the Dev Demo Article content type: <br/><i>/system/modules/org.opencms.dev.demo/schemas/article.xsd</i></p>	    				
	    				<br/>
	    				<p> The template file for this containerpage: <br/><i>/system/modules/org.opencms.dev.demo/templates/formatter_demo.jsp</i></p>
						<br/>
						<p> The formatters for this example: <br/><i>/system/modules/org.opencms.dev.demo/formatters/article/formatter-demo/</i></p>
						<br/>
						<p> The used formatter for this example are configured in the sub sitemap configuration of this example: <br/><i>/sites/default/dev-demo/formatter-demo/.content/.config</i></p>					
				</div>
				<div>&nbsp;</div>
				<div class="container-description narrow">Container with width="200" attribute set in the template file.</div>				
				<div class="container-showcase narrow">
					<cms:container name="min_width" width="200" maxElements="1" />
				</div>
				
				<div  class="container-description middle" >Container with width="400" set in the template file.</div>
				
				<div class="container-showcase middle" >			
					<cms:container name="min_max_width" width="400" maxElements="2" />
				</div>
				
				<div class="container-description wide" >Container with width="850" set in the template file.</div>
				
				<div class="container-showcase wide" >
					<cms:container name="max_width" width="850" maxElements="3" />
				</div>
				
				
				<div class="container-description type-left-description">
					Container with type="leftbox" set in the template file.	
				</div>
				<div class="container-description type-right-description" >
					Container with type="rightbox" set in the template file.
				</div>
				<div class="container-showcase type-left-container" >
					<cms:container name="leftbox_type" type="leftbox" maxElements="1" />
				</div>
				
				<div class="container-showcase type-right-container" >
					<cms:container name="rightbox_type" type="rightbox" maxElements="1" />
				</div>				  			   
		     	</div>
			    <!-- end: content area -->
		
	    		<!-- begin: #footer -->
	    		<cms:include file="%(link.weak:/system/modules/org.opencms.dev.demo/elements/site-elements/footer.jsp:31bf1d94-764e-11e0-a23b-8fedaf1e6b74)" />
		    	<!-- end: #footer -->
		</div>
	<!-- end: #page -->
	</div>
</body></html>