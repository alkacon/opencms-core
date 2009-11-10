<%@ page import="org.opencms.jsp.*" %><%
    CmsJspActionElement wp = new CmsJspActionElement(pageContext, request, response);
%>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script type="text/javascript" src="lib/jquery-1.3.2.js"></script>
<script type="text/javascript" src="lib/jquery-ui-1.8a1.js"></script>
<script type="text/javascript" src="lib/json2.js"></script>
<script type="text/javascript" src="lib/jquery.pagination.js"></script>
<script type="text/javascript">
var cms = { html: {}, galleries: {}, messages: {} };
</script>
<script type="text/javascript" src="/opencms/opencms/system/workplace/editors/ade/cms.messages.jsp"></script>
<script type="text/javascript" src="js/cms.html.js"></script>
<script type="text/javascript" src="js/cms.selectbox.js"></script>
<script type="text/javascript" src="js/cms.galleries.js"></script>
<script type="text/javascript">  
    vfsPathAjaxJsp = "<%= wp.link("/system/workplace/galleries/gallerySearch.jsp") %>";  
    
    $(function() {        
        cms.galleries.initAddDialog();                
	});
</script> 
<title>Search demo</title>
<link rel="stylesheet" type="text/css" media="screen" href="css/custom-theme/jquery-ui-1.7.2.custom.css" />
<link rel="stylesheet" type="text/css" media="screen" href="css/advanced_direct_edit.css" />
<style type="text/css">

    *{
	    font-family: Helvetica, Arial, sans-serif;        
	}
    
    #cms-gallery-main{ 
       /*  margin: 50px auto; */
        width: 650px;
        /* border: 2px solid #A9A9A9;*/ 
        font-size: 12px;
        padding: 10px 0px 60px 0px;
        background: #ffffff;
    }
    
    /* Style Extension for the ui tabs */
    .ui-tabs .ui-tabs-nav li a {       
        padding:3px 5px 4px;
    }
    
    .ui-tabs {
        height: 440px;
    }
    
    .result-tab-scrolling {
        height: 290px;
    }
    
    .criteria-tab-scrolling {
        height: 350px;
    }
    
    /* List for search tabs */
    .cms-list-scrolling {                
        border:1px solid #AAAAAA;
        /*margin:0 0 7px 7px;*/        
        overflow:auto;
        width:600px;
    }
     
    .cms-list-scrolling-innner {
        margin: 0px;
        padding: 0px;
    }
    
    /** Item in the search list */
    li.cms-list {
   /*     display: inline-block; */
        margin-right: 5px;
        margin-top: 2px;
        list-style-image:none;
        list-style-position:outside;
        list-style-type:none;
        cursor: pointer;
        /* HACK: To force hasLayout: true in IE7 */
        zoom: 1;
     /*    clear: left; */
    }
    
    .cms-list-checkbox {
        width: 32px; 
        height: 32px; 
        background: transparent url(css/images/checkbox.gif) no-repeat scroll -7px -10px; 
        float: left;    
    }
    
    .cms-list-image {
        width: 32px; 
        height: 32px; 
        background: transparent url(../../filetypes/folder.gif) no-repeat scroll center center; 
        float: left;
    }
    
    .cms-list-item { 
        /*width: 540px; */
      /*  width: 420px; */
        height: 32px; 
        margin-left: 32px;
        /* float: left; */
    }
    
    .cms-list-itemcontent {
        /*width: 500px;*/
      /*   width: 380px; */        
        height: 34px; 
        margin-left:34px;
        /* float: left; */
    }
    
    div.cms-list-title {
    /*     clear:left; */ 
        height:16px;
        font-weight:bold;
        overflow-x:hidden;
        overflow-y:hidden;
        /* width:400px; */
    }
    
    div.cms-list-url {
        height:16px;
        overflow-x:hidden;
        overflow-y:hidden;
    /*     width:400px; */
    }  
    
    /** Hover and active state for items in the search list */
    li.cms-list-item-active div.cms-list-item { 
        border: 1px solid #fcefa1; 
        background: #fbf9ee url(css/custom-theme/images/ui-bg_glass_55_fbf9ee_1x400.png) 50% 50% repeat-x; 
        color: #363636; 
    }   
    
    li.cms-result-list-item div.ui-state-hover, .cms-list div.ui-state-hover, span.cms-drop-down .ui-state-hover{
    	background-image: url(css/custom-theme/images/ui-bg_glass_75_cccccc_1x400.png);
    }

    .cms-result-list-item div.ui-state-default, .cms-list div.ui-state-default, .cms-drop-down .ui-state-default{
    	background-image: url(css/custom-theme/images/ui-bg_glass_75_e6e6e6_1x400.png);
    }    
   
   .cms-list-item-hover div.cms-list-item { 
        border-color: #5ca718; 
   }
   
    .cms-list-item-active div.cms-list-checkbox,
    .cms-list-item-active { 
       background-position: -7px -210px;
    }
   
   .cms-list-item-hover div.cms-list-checkbox { 
       background-position: -7px -110px;
   }
   
   .cms-list-item-active.cms-list-item-hover div.cms-list-checkbox { 
       background-position: -7px -310px;
   }
    
    /** Input fields */
    input.ui-widget-content{
    	padding: 1px;
    }
    
    /** Result tab */
    #tabs-result .cms-searchquery{
    	display:inline-block;
        padding:2px;
        margin: 1px;
        position:relative;            
    }
    
           
    .cms-result-criteria {
        margin: 5px 7px 10px 2px; 
    }
    
    span.cms-search-title {       
        display:inline-block;
        margin-left:2px;
        margin-right:10px;
    }
    
    span.cms-search-remove {        
        background:transparent url(../../buttons/exit.png) no-repeat scroll center center;
        display:inline-block;
        height:19px;
        width:19px;
    }
    
    span.cms-search-remove:hover {
        background-color: #a8adb4;
    }
    
    .cms-result-list-item {
        list-style-image:none;
        list-style-position:outside;
        list-style-type:none;
        margin: 2px 2px 0px 4px;
    }
    
    
    
    .cms-result-list-title {
        font-weight:bold; 
        overflow:hidden;
    }
    
    .cms-result-list-path {        
        overflow:hidden;
    }

    /** Option area over and under the criteria list */
    .cms-list-options{
    	font-size:13px;
        display:inline-block;
        /*margin:10px 2px 10px 7px;*/
       margin: 0px 0px 5px 0px;
        width:600px;
    }           
    
    .cms-list-options span.cms-drop-down {
        float: left;    
    }
    
    .cms-list-options span.cms-ft-search,
    .cms-list-options button.cms-item-right {
        float: right;
    }
    
    .cms-list-options button {
        cursor: pointer;
        width: auto;
    }
    
    .cms-ft-search label, .cms-drop-down label {
        margin-right: 10px;
    }
    
    .cms-drop-down {        
        /* HACK: To force hasLayout: true in IE7 */
        zoom: 1;
    }
    
    .cms-drop-down select {
        width: 150px;
    }
    
    /** Full Text Search tab*/
   
   .cms-search-panel {
        border: 1px solid #AAAAAA;
        height: 378px;
        width: 600px; 
    }
    
    .cms-search-options {
        display:inline-block;
        margin:10px 2px 10px 7px;
        width:590px;
        position: relative;
    }
    
    .cms-search-options div.cms-checkbox-label {
        float: left; 
        line-height: 32px; 
        margin-right: 10px;
    }
     
    .cms-search-options span.cms-item-left,
    .cms-search-options button.cms-item-left{
        float: left;            
    }
    
    .cms-search-options button.cms-item-left-bottom{
        position:relative; 
        bottom: -190px;
    }
        
    .cms-search-options div.cms-item-left {
        float: left;
        line-height: 32px;
        margin-right: 10px;
    }
        
    .cms-search-options span.cms-item-right,
    .cms-search-options button.cms-item-right {
        float: right;
    }
    
    .cms-search-options button {
        cursor: pointer;
        width: auto;
    }
    
    .cms-search-options span label,
    .cms-search-options label {
        margin-right: 10px;
    }
    
    .cms-input-date input{
        width: 110px;
        margin-right: 20px;
    }
    
    /** Categories levels*/         
   .cms-list.cms-active-level.cms-level-1 {
       margin-left: 25px;
   }
      
   .cms-list.cms-active-level.cms-level-2 {
       margin-left: 40px;
   }
      
   .cms-list.cms-active-level.cms-level-3 {
       margin-left: 55px;
   }
   
    /** Pagination plugin*/
    div.result-pagination {
    	height: 24px;
    	font-weight: bold;
    	text-align: center;
    	padding-top: 3px;
    	display: block;
    }
    
    div.result-pagination a, 
    div.result-pagination a, 
    div.result-pagination span.current, 
    div.result-pagination span.current {
    	margin-right: 6px;
    	padding: 2px 3px;
    	text-decoration: none;
    	display: inline-block;
    } 
    
    div.result-pagination a, 
    div.result-pagination a {
        -moz-border-radius: 4px;
        -webkit-border-radius: 4px;
    	border:1px solid #999999;
        color:#222222;
    }
    
    div.result-pagination a:hover, 
    div.result-pagination a:hover {
    	border: 1px solid #999999;
    }
    
    div.result-pagination span.current, 
    div.result-pagination span.current {
        -moz-border-radius: 4px;
        -webkit-border-radius: 4px;
    	border: 1px solid #999999;
        background: #CCCCCC;
        color:#222222;    	
    }

    
</style>
<!--[if IE 7]>
<style>
div.cms-selectbox{
    zoom: 1;
    display: inline;
    }
</style>
<![endif]-->
	</head>
	<body>
	    <div id="cms-gallery-main"> 
		    <div id="tabs">
                <ul>
                     <li><a href="#tabs-result">Search results</a></li>
                     <li><a href="#tabs-types">Type</a></li>
                     <li><a href="#tabs-galleries">Galleries</a></li>
                     <li><a href="#tabs-categories">Categories</a></li>
                     <li><a href="#tabs-fulltextsearch">Full Text Search</a></li>
                </ul>
               <!--<div class="" id="tabs-fulltextsearch">
                  <p>tabs-fulltextsearch.</p>
               </div>-->
            </div>
        </div>
	</body>
</html>