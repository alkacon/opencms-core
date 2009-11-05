<%@page session="false" import="org.opencms.jsp.CmsJspActionElement, org.opencms.workplace.CmsWorkplace"%>
<%@ taglib prefix="cms" uri="http://www.opencms.org/taglib/cms"%><%
  CmsJspActionElement jsp = new CmsJspActionElement(pageContext, request, response);
  pageContext.setAttribute("cms", jsp);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/lib/jquery-1.3.2.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/lib/jquery-ui-1.8a1.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/lib/json2.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/lib/jquery.jHelperTip.1.0.js")%>" ></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.directinput.js")%>"></script>
    <script type="text/javascript">
      var cms = {
         data: {
            "EDITOR_URL": "<cms:link>%(link.weak:/system/workplace/editors/editor.jsp:8973b380-11b7-11db-91cd-fdbae480bac9)</cms:link>",
            "SERVER_URL": "<cms:link>%(link.weak:/system/workplace/editors/sitemap/server.jsp:b848e330-c865-11de-a457-ab20365f6268)</cms:link>",
            "CURRENT_URI": "${cms.requestContext.uri}",
            "LOCALE": "${cms.requestContext.locale}"
         },
         html: {},
         messages: {},
         publish: {},
         sitemap: {}
      };
    </script>
    <script type="text/javascript" src="<cms:link>%(link.weak:/system/workplace/editors/sitemap/cms.messages.jsp:b829e977-c865-11de-a457-ab20365f6268)</cms:link>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.html.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.data.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.publish.js")%>"></script>
    <script type="text/javascript" src="<%=CmsWorkplace.getResourceUri("editors/sitemap/js/cms.sitemap.js")%>"></script>
    <script type="text/javascript">
      $(document).ready(function() {
         cms.sitemap.initSitemap();
      });
    </script>
    <title>Sitemap demo</title>
    <link rel="stylesheet" type="text/css" media="screen" href="<%=CmsWorkplace.getResourceUri("editors/sitemap/css/custom-theme/jquery-ui-1.7.2.custom.css")%>" />
    <link rel="stylesheet" type="text/css" media="screen" href="<%=CmsWorkplace.getResourceUri("editors/sitemap/css/sitemap.css")%>" />
    <style type="text/css">
      * {
          font-family: Helvetica, Arial, sans-serif;
          margin: 0px;
          padding: 0px;
      }
      
      ul {
          display: block;
          list-style-image: none;
          list-style-type: none;
          margin: 0 0 0 30px;
          padding: 0px;
      }
      
      li {
          display: block;
          float: left;
          clear: left;
          margin: 0px;
          padding: 0px;
          width: 100%;
      }
      
      li div.cms-sitemap-item {
          width: 290px;
          margin-left: 20px;
          position: relative;
          padding: 1px 0px;
      }
      
      #toolbar .cms-scrolling li div.cms-sitemap-item {
          margin: 3px 0px;
          position: static;
      }
      
      #toolbar ul {
          width: 290px;
          margin: auto;
      }
      
      #toolbar .cms-scrolling li div.cms-dropzone, #toolbar .cms-scrolling li ul, #toolbar .cms-scrolling span.cms-opener {
          display: none;
      }
      
      li div.cms-dropzone {
          height: 6px;
          margin-left: 15px;
      }
      
      #toolbar .cms-scrolling {
          position: static;
      }
      
      #toolbar .cms-scrolling div.cms-sitemap-item {
          position: static;
      }
      
      #favorite-drop-list {
          height: 25px;
          margin-bottom: 15px !important;
      }
      
      #favorite-drop-list.cms-hovered {
          background-color: #6E9CE0;
      }
      
      .cms-sitemap-item h3 {
          margin-left: 16px;
          line-height: 23px;
      }
      
      #toolbar .cms-scrolling li.cms-subtree h3 {
          background: transparent url(css/images/16x16/text_tree.png) no-repeat scroll 98% 70%;
      }
      
      .cms-sitemap-item a.cms-move {
          cursor: move;
      }
      
      input.cms-direct-input, input.cms-direct-input:focus, input.cms-direct-input:active {
          border: none;
          background: transparent;
      }
      
      span.cms-opener {
          cursor: pointer;
          display: block;
          float: left;
          width: 10px;
          height: 25px;
          user-select: none;
          -moz-user-select: none;
          background: transparent url(css/images/collapsable.gif) no-repeat scroll 50% 50%;
      }
      
      .cms-closed span.cms-opener, .cms-force-closed span.cms-opener {
          background-image: url(css/images/expandable.gif);
      }
      
      div.cms-dragging {
          opacity: 0.5;
          filter: alpha( opacity = 50);
      }
      
      body {
          background: #e1e1e1;
          
      }
      body:last-child{
          overflow: scroll;
      }
      #cms-sitemap {
          margin: 0px auto;
          width: 930px;
      }
      
      #cms-main {
          margin: 50px auto;
          width: 950px;
      }
      
      #cms-main h1.cms-headline {
          font-size: 30px;
          font-weight: normal;
          margin-bottom: 5px;
      }
      
      #cms-main div.cms-box {
          border: 2px solid #A9A9A9;
          padding: 10px 0px 60px 0px;
          background: #ffffff;
      }
      
      #cms-sitemap div.cms-hovered {
          background: #CCCCCC url(custom-theme/images/ui-bg_glass_75_cccccc_1x400.png) repeat-x scroll 50% 50%;
      }
      
      .cms-closed ul, .cms-force-closed ul {
          display: none;
      }
      
      .cms-additional-info{
          display: none;
          font-size: 12px;
          line-height: 16px;
          margin: 2px 10px 0 16px;
          overflow: hidden;
          white-space: nowrap;
      }
      .cms-additional-info span{
          font-weight: bold;
          margin-left: 10px;
      }
      .cms-icon-triangle, div.cms-dragging div.cms-icon-triangle, li.ui-draggable-dragging div.cms-icon-triangle{
          background-position:-32px -16px;
          margin-top: 3px;
      }
      .cms-additional-show .cms-additional-info{
          display: block;
      }
      .cms-additional-show .cms-icon-triangle{
          background-position:-64px -16px;
      }
      
      div.cms-dragging div.cms-additional-info, li.ui-draggable-dragging div.cms-additional-info{
          display: none;
      }
      
      #jHelperTipAttrContainer {
          line-height: 14px;
          font-size: 12px;
      }
    </style>
  </head>
  <body>
    <div id="cms-main">
      <h1 class="cms-headline">Sitemap-Editor</h1>
      <div class="cms-box ui-corner-all">
        <ul id="cms-sitemap">
        </ul>
        <div class="cms-clearer">
        </div>
      </div>
    </div>
  </body>
</html>
